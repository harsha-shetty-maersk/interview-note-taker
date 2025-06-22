import React, { useEffect, useState } from 'react';
import {
  getInterviews,
  getInterviewsByInterviewer,
  createInterview,
  updateInterview,
  deleteInterview,
  Interview,
} from '../../services/interviewService';
import { getCandidates } from '../../services/candidateService';
import InterviewForm, { InterviewFormValues } from './InterviewForm';
import authService from '../../services/authService';
import axios from 'axios';
import dayjs from 'dayjs'; // Ensure dayjs is installed: npm install dayjs
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

const statusOptions = ['All', 'Scheduled', 'In Progress', 'Completed', 'Cancelled'];

const Interviews: React.FC = () => {
  const [interviews, setInterviews] = useState<Interview[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState<'add' | 'edit'>('add');
  const [selectedInterview, setSelectedInterview] = useState<Interview | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<Interview | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('All');
  const { user } = useAuth();
  const [candidates, setCandidates] = useState<{ id: string; name: string; position: string }[]>([]);
  const [interviewers, setInterviewers] = useState<{ id: string; name: string }[]>([]);
  const [savingId, setSavingId] = useState<number | null>(null);

  const navigate = useNavigate();

  const fetchInterviews = async () => {
    setLoading(true);
    setError(null);
    try {
      let data;
      if (user?.role === 'INTERVIEWER') {
        // For interviewers, only fetch their assigned interviews
        data = await getInterviewsByInterviewer(user.id);
      } else {
        // For admins, fetch all interviews
        data = await getInterviews();
      }
      const list = Array.isArray(data) ? data : data.content || [];
      setInterviews(
        list.map((i: any) => ({
          ...i,
          date: i.scheduledDate, // map backend field to frontend field
          interviewerId: i.interviewerId, // single interviewer only
        }))
      );
    } catch (err) {
      setError('Failed to load interviews.');
    } finally {
      setLoading(false);
    }
  };

  const fetchCandidates = async () => {
    try {
      const data = await getCandidates();
      const list = Array.isArray(data.content) ? data.content : Array.isArray(data) ? data : [];
      setCandidates(
        list.map((c: any) => ({
          id: String(c.id),
          name: c.fullName || `${c.firstName} ${c.lastName}`,
          position: c.position || '',
        }))
      );
    } catch {
      setCandidates([]);
    }
  };

  const fetchInterviewers = async () => {
    try {
      const response = await axios.get('/api/users?role=INTERVIEWER', { headers: authService.getAuthHeaders() });
      setInterviewers(
        response.data.map((u: any) => ({
          id: String(u.id),
          name: u.firstName && u.lastName ? `${u.firstName} ${u.lastName}` : u.username,
        }))
      );
    } catch {
      setInterviewers([]);
    }
  };

  useEffect(() => {
    fetchInterviews();
    fetchCandidates();
    fetchInterviewers();
  }, []);

  const handleAdd = () => {
    setModalMode('add');
    setSelectedInterview(null);
    setShowModal(true);
    setFormError(null);
  };

  const handleEdit = (interview: Interview) => {
    setModalMode('edit');
    setSelectedInterview(interview);
    setShowModal(true);
    setFormError(null);
  };

  const handleDelete = (interview: Interview) => {
    setDeleteTarget(interview);
    setShowDeleteModal(true);
  };

  const handleFormSubmit = async (values: InterviewFormValues) => {
    setFormLoading(true);
    setFormError(null);
    try {
      // Build backend payload
      const scheduledDate = new Date(`${values.date}T${values.time}`);
      const payload = {
        candidateId: Number(values.candidateId),
        position: values.position,
        scheduledDate: scheduledDate.toISOString(),
        duration: Number(values.duration),
        status: values.status,
        interviewerId: Number(values.interviewerId),
        notes: '',
        overallScore: 0,
        finalRecommendation: null,
      };
      // Remove any undefined, null, or empty string fields from payload
      const cleanPayload = { ...payload } as Record<string, any>;
      Object.keys(cleanPayload).forEach(key => {
        if (
          cleanPayload[key] === undefined ||
          cleanPayload[key] === null ||
          (typeof cleanPayload[key] === 'string' && cleanPayload[key].trim() === '')
        ) {
          delete cleanPayload[key];
        }
      });
      if (modalMode === 'add') {
        await createInterview(cleanPayload);
      } else if (selectedInterview) {
        await updateInterview(selectedInterview.id, cleanPayload);
      }
      setShowModal(false);
      fetchInterviews();
    } catch (err) {
      setFormError('Failed to save interview.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!deleteTarget) return;
    setFormLoading(true);
    setFormError(null);
    try {
      await deleteInterview(deleteTarget.id);
      setShowDeleteModal(false);
      fetchInterviews();
    } catch (err) {
      setFormError('Failed to delete interview.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleInlineUpdate = async (id: number, updates: Partial<Interview>) => {
    setSavingId(id);
    try {
      const interview = interviews.find(i => i.id === id);
      if (!interview) return;
      const updated = { ...interview, ...updates };

      // Extract date and time for scheduledDate
      let date = dayjs(interview.date).format('YYYY-MM-DD');
      let time = dayjs(interview.date).format('HH:mm') || '10:00';
      if (updates.date) {
        date = dayjs(updates.date).format('YYYY-MM-DD');
      }
      // If you add time editing, handle updates.time here
      const scheduledDate = dayjs(`${date}T${time}`).toISOString();

      // Status: always uppercase
      const status = updates.status
        ? updates.status.toUpperCase()
        : interview.status;

      // Robustly extract interviewerId
      let interviewerId = null;
      if (updates.interviewerId !== undefined && updates.interviewerId !== null) {
        interviewerId = Number(updates.interviewerId);
      } else if (interview.interviewerId !== undefined && interview.interviewerId !== null) {
        interviewerId = Number(interview.interviewerId);
      }
      if (!interviewerId || isNaN(interviewerId)) {
        setSavingId(null);
        setError('Interview is missing interviewer. Please check the data.');
        return;
      }

      const candidateId = updates.candidateId !== undefined && updates.candidateId !== null
        ? Number(updates.candidateId)
        : Number(interview.candidateId);
      if (!candidateId || isNaN(candidateId)) {
        setSavingId(null);
        setError('Interview is missing candidate. Please check the data.');
        return;
      }

      const payload: any = {
        candidateId,
        position: updates.position !== undefined ? updates.position : interview.position,
        scheduledDate, // always ISO string with both date and time
        status,
        interviewerId, // single interviewer only
        duration: updates.duration !== undefined ? updates.duration : interview.duration,
      };

      await updateInterview(id, payload);
      await fetchInterviews();
    } finally {
      setSavingId(null);
    }
  };

  // Filter/search logic
  const filteredInterviews = interviews.filter((interview) => {
    const matchesSearch =
      interview.candidateName?.toLowerCase().includes(search.toLowerCase()) ||
      interview.position?.toLowerCase().includes(search.toLowerCase()) ||
      interview.interviewerName?.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = status === 'All' || interview.status === status;
    return matchesSearch && matchesStatus;
  });

  const getStatusDisplay = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'SCHEDULED':
        return 'Scheduled';
      case 'COMPLETED':
        return 'Completed';
      case 'CANCELLED':
        return 'Cancelled';
      default:
        return status || 'Unknown';
    }
  };

  const getStatusClass = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'SCHEDULED':
        return 'bg-yellow-100 text-yellow-700';
      case 'COMPLETED':
        return 'bg-green-100 text-green-700';
      case 'CANCELLED':
        return 'bg-gray-100 text-gray-700';
      default:
        return 'bg-indigo-100 text-indigo-700';
    }
  };

  const exportToPDF = async (interview: Interview) => {
    try {
      // Fetch the full interview details including notes
      const response = await axios.get(`/api/interviews/${interview.id}`, {
        headers: authService.getAuthHeaders()
      });
      const interviewData = response.data;

      // Create a temporary div to render the PDF content
      const pdfContent = document.createElement('div');
      pdfContent.style.padding = '20px';
      pdfContent.style.fontFamily = 'Arial, sans-serif';
      pdfContent.style.fontSize = '12px';
      pdfContent.style.lineHeight = '1.4';
      pdfContent.style.color = '#333';
      pdfContent.style.backgroundColor = 'white';
      pdfContent.style.width = '800px';

      // Parse notes data
      let notesData = null;
      let timestampedNotes = [];
      let scoring = {};
      let finalRecommendation = '';
      let recommendationExplanation = '';

      if (interviewData.notes) {
        try {
          const parsed = JSON.parse(interviewData.notes);
          if (parsed.timestampedNotes) {
            timestampedNotes = parsed.timestampedNotes;
            scoring = parsed.scoring || {};
            finalRecommendation = parsed.finalRecommendation || '';
            recommendationExplanation = parsed.recommendationExplanation || '';
          }
        } catch (e) {
          // If parsing fails, treat as plain text
          timestampedNotes = [{ timestamp: 'N/A', content: interviewData.notes }];
        }
      }

      // Build PDF content
      pdfContent.innerHTML = `
        <div style="text-align: center; margin-bottom: 30px;">
          <h1 style="color: #1f2937; margin-bottom: 10px;">Interview Report</h1>
          <h2 style="color: #4f46e5; margin-bottom: 20px;">${interview.candidateName}</h2>
        </div>

        <div style="margin-bottom: 30px;">
          <h3 style="color: #1f2937; border-bottom: 2px solid #e5e7eb; padding-bottom: 5px; margin-bottom: 15px;">Interview Details</h3>
          <table style="width: 100%; border-collapse: collapse;">
            <tr>
              <td style="padding: 8px; font-weight: bold; width: 150px;">Position:</td>
              <td style="padding: 8px;">${interview.position}</td>
            </tr>
            <tr>
              <td style="padding: 8px; font-weight: bold;">Interviewer:</td>
              <td style="padding: 8px;">${interview.interviewerName}</td>
            </tr>
            <tr>
              <td style="padding: 8px; font-weight: bold;">Date & Time:</td>
              <td style="padding: 8px;">${dayjs(interview.date).format('MMM DD, YYYY [at] h:mm A')}</td>
            </tr>
            <tr>
              <td style="padding: 8px; font-weight: bold;">Status:</td>
              <td style="padding: 8px;">${getStatusDisplay(interview.status)}</td>
            </tr>
            <tr>
              <td style="padding: 8px; font-weight: bold;">Overall Score:</td>
              <td style="padding: 8px;">${interviewData.overallScore || 0}/10</td>
            </tr>
          </table>
        </div>

        ${timestampedNotes.length > 0 ? `
        <div style="margin-bottom: 30px;">
          <h3 style="color: #1f2937; border-bottom: 2px solid #e5e7eb; padding-bottom: 5px; margin-bottom: 15px;">Interview Notes</h3>
          ${timestampedNotes.map((note: any) => `
            <div style="margin-bottom: 15px; padding: 10px; border: 1px solid #e5e7eb; border-radius: 5px;">
              <div style="font-weight: bold; color: #4f46e5; margin-bottom: 5px;">${note.timestamp}</div>
              <div style="white-space: pre-wrap;">${note.content}</div>
            </div>
          `).join('')}
        </div>
        ` : ''}

        ${Object.keys(scoring).length > 0 ? `
        <div style="margin-bottom: 30px;">
          <h3 style="color: #1f2937; border-bottom: 2px solid #e5e7eb; padding-bottom: 5px; margin-bottom: 15px;">Assessment Scores</h3>
          <table style="width: 100%; border-collapse: collapse; border: 1px solid #e5e7eb;">
            <thead>
              <tr style="background-color: #f9fafb;">
                <th style="padding: 10px; text-align: left; border: 1px solid #e5e7eb;">Category</th>
                <th style="padding: 10px; text-align: center; border: 1px solid #e5e7eb;">Score</th>
                <th style="padding: 10px; text-align: left; border: 1px solid #e5e7eb;">Details</th>
              </tr>
            </thead>
            <tbody>
              ${Object.entries(scoring).map(([key, value]: [string, any]) => `
                <tr>
                  <td style="padding: 10px; border: 1px solid #e5e7eb; font-weight: bold;">${key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}</td>
                  <td style="padding: 10px; border: 1px solid #e5e7eb; text-align: center;">${value.score || 0}/10</td>
                  <td style="padding: 10px; border: 1px solid #e5e7eb;">${value.details || 'N/A'}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
        ` : ''}

        ${finalRecommendation ? `
        <div style="margin-bottom: 30px;">
          <h3 style="color: #1f2937; border-bottom: 2px solid #e5e7eb; padding-bottom: 5px; margin-bottom: 15px;">Final Recommendation</h3>
          <div style="margin-bottom: 10px;">
            <strong>Recommendation:</strong> ${finalRecommendation.replace(/_/g, ' ')}
          </div>
          ${recommendationExplanation ? `
          <div>
            <strong>Explanation:</strong><br>
            <div style="margin-top: 5px; padding: 10px; background-color: #f9fafb; border-radius: 5px; white-space: pre-wrap;">${recommendationExplanation}</div>
          </div>
          ` : ''}
        </div>
        ` : ''}

        <div style="margin-top: 40px; padding-top: 20px; border-top: 2px solid #e5e7eb; text-align: center; color: #6b7280; font-size: 10px;">
          Generated on ${dayjs().format('MMM DD, YYYY [at] h:mm A')}
        </div>
      `;

      // Append to body temporarily
      document.body.appendChild(pdfContent);

      // Convert to canvas
      const canvas = await html2canvas(pdfContent, {
        scale: 2,
        useCORS: true,
        allowTaint: true,
        backgroundColor: '#ffffff'
      });

      // Remove temporary element
      document.body.removeChild(pdfContent);

      // Create PDF
      const imgData = canvas.toDataURL('image/png');
      const pdf = new jsPDF('p', 'mm', 'a4');
      const imgWidth = 210;
      const pageHeight = 295;
      const imgHeight = (canvas.height * imgWidth) / canvas.width;
      let heightLeft = imgHeight;

      let position = 0;

      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
      heightLeft -= pageHeight;

      while (heightLeft >= 0) {
        position = heightLeft - imgHeight;
        pdf.addPage();
        pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
        heightLeft -= pageHeight;
      }

      // Save PDF
      pdf.save(`Interview_${interview.candidateName.replace(/\s+/g, '_')}_${dayjs(interview.date).format('YYYY-MM-DD')}.pdf`);

    } catch (error) {
      console.error('Error generating PDF:', error);
      alert('Failed to generate PDF. Please try again.');
    }
  };

  return (
    <div className="pt-8">
      <div className="flex flex-wrap gap-4 items-center mb-8 justify-between">
        <h1 className="text-3xl font-bold mb-0">Interviews</h1>
        {user?.role === 'ADMIN' && (
          <button
            className="btn-primary px-4 py-2 text-sm min-w-[140px]"
            onClick={handleAdd}
          >
            + New Interview
          </button>
        )}
      </div>
      <div className="flex flex-wrap gap-4 items-center mb-8">
        <div className="relative w-64">
          <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
            <svg className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-4.35-4.35m0 0A7.5 7.5 0 104.5 4.5a7.5 7.5 0 0012.15 12.15z" />
            </svg>
          </span>
          <input
            type="text"
            placeholder="Search by candidate, position, or interviewer"
            className="pl-10 pr-4 py-2 w-full rounded-md border border-gray-300 bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 transition"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
        <select
          className="rounded-md border border-gray-300 bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 transition py-2 px-3 w-40"
          value={status}
          onChange={e => setStatus(e.target.value)}
        >
          {statusOptions.map(opt => (
            <option key={opt} value={opt}>{opt}</option>
          ))}
        </select>
      </div>
      {loading && <div className="text-gray-500">Loading...</div>}
      {error && <div className="text-red-600 mb-2">{error}</div>}
      <div className="overflow-x-auto">
        <table className="min-w-full bg-white rounded-lg shadow-md">
          <thead>
            <tr className="bg-gray-100 text-left">
              <th className="py-3 px-4 font-semibold">Candidate</th>
              <th className="py-3 px-4 font-semibold">Position</th>
              <th className="py-3 px-4 font-semibold">Date</th>
              <th className="py-3 px-4 font-semibold">Status</th>
              <th className="py-3 px-4 font-semibold">Interviewer</th>
              <th className="py-3 px-4 font-semibold">Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredInterviews.length === 0 ? (
              <tr>
                <td colSpan={6} className="py-6 text-center text-gray-500">No interviews found.</td>
              </tr>
            ) : (
              filteredInterviews.map((interview) => (
                <tr
                  key={interview.id}
                  className="border-b hover:bg-indigo-50 transition-colors cursor-pointer"
                  onClick={() => {
                    if (user?.role === 'INTERVIEWER') {
                      // For interviewers, navigate to notes page
                      navigate(`/interview-notes/${interview.id}`);
                    } else {
                      // For admins, navigate to detail page
                      navigate(`/interviews/${interview.id}`);
                    }
                  }}
                >
                  <td className="py-2 px-4">{interview.candidateName}</td>
                  <td className="py-2 px-4">{interview.position}</td>
                  <td className="py-2 px-4">
                    {user?.role === 'ADMIN' ? (
                      <input
                        type="date"
                        value={dayjs(interview.date).format('YYYY-MM-DD')}
                        disabled={savingId === interview.id}
                        className={`input-field font-medium ${savingId === interview.id ? 'opacity-60' : ''}`}
                        onClick={e => e.stopPropagation()}
                        onChange={e => handleInlineUpdate(interview.id, { date: dayjs(e.target.value).format('YYYY-MM-DDTHH:mm:ss') })}
                      />
                    ) : (
                      <span>{dayjs(interview.date).format('MMM DD, YYYY')}</span>
                    )}
                  </td>
                  <td className="py-2 px-4">
                    {user?.role === 'ADMIN' ? (
                      <select
                        className={`input-field font-medium ${getStatusClass(interview.status)}`}
                        value={getStatusDisplay(interview.status)}
                        disabled={savingId === interview.id}
                        onClick={e => e.stopPropagation()}
                        onChange={e => {
                          const selected = e.target.value;
                          let newStatus = selected.toUpperCase();
                          if (newStatus === 'ALL') return;
                          handleInlineUpdate(interview.id, { status: newStatus });
                        }}
                      >
                        {['Scheduled', 'Completed', 'Cancelled'].map(opt => (
                          <option key={opt} value={opt}>{opt}</option>
                        ))}
                      </select>
                    ) : (
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusClass(interview.status)}`}>
                        {getStatusDisplay(interview.status)}
                      </span>
                    )}
                  </td>
                  <td className="py-2 px-4">
                    {user?.role === 'ADMIN' ? (
                      <select
                        value={interview.interviewerId}
                        disabled={savingId === interview.id}
                        className={`input-field font-medium ${savingId === interview.id ? 'opacity-60' : ''}`}
                        onClick={e => e.stopPropagation()}
                        onChange={e => handleInlineUpdate(interview.id, { interviewerId: Number(e.target.value) })}
                      >
                        {interviewers.map(i => (
                          <option key={i.id} value={i.id}>{i.name}</option>
                        ))}
                      </select>
                    ) : (
                      <span>{interview.interviewerName}</span>
                    )}
                  </td>
                  <td className="py-2 px-4 flex gap-2">
                    {user?.role === 'ADMIN' && (
                      <>
                        <button className="text-blue-600 hover:underline text-sm"
                          onClick={e => { e.stopPropagation(); handleEdit(interview); }}
                        >Edit</button>
                        <button className="text-red-600 hover:underline text-sm"
                          onClick={e => { e.stopPropagation(); handleDelete(interview); }}
                        >Delete</button>
                      </>
                    )}
                    {user?.role === 'INTERVIEWER' && (
                      <button className="text-blue-600 hover:underline text-sm"
                        onClick={e => { e.stopPropagation(); navigate(`/interview-notes/${interview.id}`); }}
                      >Take Notes</button>
                    )}
                    <button className="text-green-600 hover:underline text-sm"
                      onClick={e => { e.stopPropagation(); exportToPDF(interview); }}
                    >Export PDF</button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      {/* Modal for Add/Edit Interview */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md relative">
            <button
              className="absolute top-2 right-2 text-gray-400 hover:text-gray-600 text-2xl"
              onClick={() => setShowModal(false)}
              aria-label="Close"
            >
              &times;
            </button>
            <h2 className="text-xl font-bold mb-4">{modalMode === 'add' ? 'Create Interview' : 'Edit Interview'}</h2>
            <InterviewForm
              candidates={candidates}
              interviewers={interviewers}
              onSubmit={handleFormSubmit}
              loading={formLoading}
              error={formError}
              submitLabel={modalMode === 'add' ? 'Create Interview' : 'Save Changes'}
            />
          </div>
        </div>
      )}
      {/* Modal for Delete Interview */}
      {showDeleteModal && deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md relative">
            <button
              className="absolute top-2 right-2 text-gray-400 hover:text-gray-600 text-2xl"
              onClick={() => setShowDeleteModal(false)}
              aria-label="Close"
            >
              &times;
            </button>
            <h2 className="text-xl font-bold mb-4">Delete Interview</h2>
            <p className="mb-4">Are you sure you want to delete this interview? This action cannot be undone.</p>
            {formError && <div className="text-red-600 mb-2">{formError}</div>}
            <div className="flex gap-2">
              <button
                className="btn-danger flex-1"
                onClick={handleConfirmDelete}
                disabled={formLoading}
              >
                {formLoading ? 'Deleting...' : 'Delete'}
              </button>
              <button
                className="btn-secondary flex-1"
                onClick={() => setShowDeleteModal(false)}
                disabled={formLoading}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Interviews; 