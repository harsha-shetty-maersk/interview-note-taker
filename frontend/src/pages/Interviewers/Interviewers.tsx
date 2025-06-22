import React, { useState, useEffect } from 'react';
import authService, { RegisterRequest } from '../../services/authService';
import axios from 'axios';
import InterviewerForm, { InterviewerFormValues } from './InterviewerForm';
import { getInterviews } from '../../services/interviewService';

const Interviewers: React.FC = () => {
  const [showModal, setShowModal] = useState(false);
  const [interviewers, setInterviewers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const userRole = 'ADMIN'; // TODO: get from auth context
  const [viewInterviewer, setViewInterviewer] = useState<any | null>(null);
  const [editInterviewer, setEditInterviewer] = useState<any | null>(null);
  const [editForm, setEditForm] = useState<any | null>(null);
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<any | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);
  const statusOptions = ['Active', 'Inactive'];
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('All');
  const [interviewCounts, setInterviewCounts] = useState<{ [key: string]: number }>({});

  const fetchInterviewers = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get('/api/users?role=INTERVIEWER', { headers: authService.getAuthHeaders() });
      setInterviewers(response.data);
    } catch (err) {
      setError('Failed to load interviewers.');
      setInterviewers([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchInterviewCounts = async () => {
    try {
      const data = await getInterviews();
      const list = Array.isArray(data.content) ? data.content : Array.isArray(data) ? data : [];
      const counts: { [key: string]: number } = {};
      list.forEach((interview: any) => {
        if (interview.interviewerId) {
          counts[interview.interviewerId] = (counts[interview.interviewerId] || 0) + 1;
        }
      });
      setInterviewCounts(counts);
    } catch {
      setInterviewCounts({});
    }
  };

  useEffect(() => {
    fetchInterviewers();
    fetchInterviewCounts();
  }, []);

  const handleView = async (id: string) => {
    try {
      const response = await axios.get(`/api/users/${id}`, { headers: authService.getAuthHeaders() });
      setViewInterviewer(response.data);
    } catch {
      setViewInterviewer(null);
    }
  };

  const handleEdit = async (id: string) => {
    try {
      const response = await axios.get(`/api/users/${id}`, { headers: authService.getAuthHeaders() });
      setEditInterviewer(response.data);
      setEditForm({
        firstName: response.data.firstName || '',
        lastName: response.data.lastName || '',
        email: response.data.email || '',
        enabled: response.data.enabled,
      });
      setEditError(null);
    } catch {
      setEditInterviewer(null);
    }
  };

  const handleEditFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setEditForm({ ...editForm, [e.target.name]: e.target.value });
    setEditError(null);
  };

  const handleEditFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setEditLoading(true);
    setEditError(null);
    try {
      await axios.put(`/api/users/${editInterviewer.id}`, editForm, { headers: authService.getAuthHeaders() });
      setEditInterviewer(null);
      setEditForm(null);
      fetchInterviewers();
    } catch (err: any) {
      setEditError(err?.response?.data?.message || 'Failed to update interviewer.');
    } finally {
      setEditLoading(false);
    }
  };

  const handleDelete = (interviewer: any) => {
    setDeleteTarget(interviewer);
    setDeleteError(null);
  };

  const handleConfirmDelete = async () => {
    if (!deleteTarget) return;
    setDeleteLoading(true);
    setDeleteError(null);
    try {
      await axios.delete(`/api/users/${deleteTarget.id}`, { headers: authService.getAuthHeaders() });
      setDeleteTarget(null);
      fetchInterviewers();
    } catch (err: any) {
      setDeleteError(err?.response?.data?.message || 'Failed to delete interviewer.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handleCreateInterviewer = async (values: InterviewerFormValues) => {
    setCreateLoading(true);
    setCreateError(null);
    try {
      await authService.register({ ...values });
      setShowModal(false);
      fetchInterviewers();
    } catch (err: any) {
      setCreateError(err?.response?.data?.message || 'Failed to create interviewer.');
    } finally {
      setCreateLoading(false);
    }
  };

  const handleEditInterviewer = async (values: InterviewerFormValues) => {
    setEditLoading(true);
    setEditError(null);
    try {
      await axios.put(`/api/users/${editInterviewer.id}`, {
        firstName: values.firstName,
        lastName: values.lastName,
        email: values.email,
        enabled: values.status === 'Active',
      }, { headers: authService.getAuthHeaders() });
      setEditInterviewer(null);
      setEditForm(null);
      fetchInterviewers();
    } catch (err: any) {
      setEditError(err?.response?.data?.message || 'Failed to update interviewer.');
    } finally {
      setEditLoading(false);
    }
  };

  const handleToggleStatus = async (interviewer: any, newStatus: string) => {
    try {
      await axios.put(`/api/users/${interviewer.id}`, {
        firstName: interviewer.firstName,
        lastName: interviewer.lastName,
        email: interviewer.email,
        enabled: newStatus === 'Active',
      }, { headers: authService.getAuthHeaders() });
      fetchInterviewers();
    } catch (err) {
      alert('Failed to update status');
    }
  };

  const getStatusClass = (status: string) => {
    switch (status) {
      case 'Active':
        return 'bg-green-100 text-green-700';
      case 'Inactive':
        return 'bg-yellow-100 text-yellow-700';
      default:
        return 'bg-indigo-100 text-indigo-700';
    }
  };

  const filteredInterviewers = interviewers.filter((interviewer) => {
    const fullName = `${interviewer.firstName || ''} ${interviewer.lastName || ''}`.trim();
    const email = interviewer.email || '';
    const matchesSearch =
      fullName.toLowerCase().includes(search.toLowerCase()) ||
      email.toLowerCase().includes(search.toLowerCase());
    const interviewerStatus = interviewer.enabled ? 'Active' : 'Inactive';
    const matchesStatus = status === 'All' || interviewerStatus === status;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="pt-8">
      <div className="flex flex-wrap gap-4 items-center mb-8 justify-between">
        <h1 className="text-3xl font-bold mb-0">Interviewers</h1>
        {userRole === 'ADMIN' && (
          <button
            className="btn-primary px-4 py-2 text-sm min-w-[140px]"
            onClick={() => setShowModal(true)}
          >
            + New Interviewer
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
            placeholder="Search by name or email"
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
          <option value="All">All</option>
          {statusOptions.map(opt => (
            <option key={opt} value={opt}>{opt}</option>
          ))}
        </select>
      </div>
      <div className="overflow-x-auto">
        <table className="min-w-full bg-white rounded-lg shadow-md">
          <thead>
            <tr className="bg-gray-100 text-left">
              <th className="py-3 px-4 font-semibold">Name</th>
              <th className="py-3 px-4 font-semibold">Email</th>
              <th className="py-3 px-4 font-semibold">Assigned Interviews</th>
              <th className="py-3 px-4 font-semibold">Status</th>
              <th className="py-3 px-4 font-semibold">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={5} className="py-6 text-center text-gray-500">Loading...</td></tr>
            ) : error ? (
              <tr><td colSpan={5} className="py-6 text-center text-red-600">{error}</td></tr>
            ) : filteredInterviewers.length === 0 ? (
              <tr><td colSpan={5} className="py-6 text-center text-gray-500">No interviewers found.</td></tr>
            ) : (
              filteredInterviewers.map((interviewer) => (
                <tr key={interviewer.id} className="border-b hover:bg-indigo-50 transition-colors cursor-pointer"
                  onClick={() => setViewInterviewer(interviewer)}>
                  <td className="py-2 px-4">{interviewer.firstName} {interviewer.lastName}</td>
                  <td className="py-2 px-4">{interviewer.email}</td>
                  <td className="py-2 px-4">{interviewCounts[interviewer.id] || 0}</td>
                  <td className="py-2 px-4">
                    <select
                      className={`input-field font-medium ${getStatusClass(interviewer.enabled ? 'Active' : 'Inactive')}`}
                      value={interviewer.enabled ? 'Active' : 'Inactive'}
                      onClick={e => e.stopPropagation()}
                      onChange={async e => {
                        const selected = e.target.value;
                        await handleToggleStatus(interviewer, selected);
                      }}
                    >
                      {statusOptions.map(opt => (
                        <option key={opt} value={opt}>{opt}</option>
                      ))}
                    </select>
                  </td>
                  <td className="py-2 px-4">
                    {userRole === 'ADMIN' && <button className="text-blue-600 hover:underline text-sm mr-2" onClick={e => { e.stopPropagation(); handleEdit(interviewer.id); }}>Edit</button>}
                    {userRole === 'ADMIN' && <button className="text-red-600 hover:underline text-sm" onClick={e => { e.stopPropagation(); handleDelete(interviewer); }}>Delete</button>}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      {/* Modal for New Interviewer */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md relative">
            <button
              className="absolute top-2 right-2 text-gray-400 hover:text-gray-600 text-2xl"
              onClick={() => { setShowModal(false); setCreateError(null); }}
              aria-label="Close"
            >
              &times;
            </button>
            <h2 className="text-xl font-bold mb-4">Create Interviewer</h2>
            <InterviewerForm
              onSubmit={handleCreateInterviewer}
              loading={createLoading}
              error={createError}
              submitLabel="Create Interviewer"
            />
          </div>
        </div>
      )}
      {/* View Interviewer Modal */}
      {viewInterviewer && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md relative">
            <button className="absolute top-2 right-2 text-gray-400 hover:text-gray-600 text-2xl" onClick={() => setViewInterviewer(null)} aria-label="Close">&times;</button>
            <h2 className="text-xl font-bold mb-4">Interviewer Details</h2>
            <div className="mb-2"><b>Name:</b> {viewInterviewer.firstName} {viewInterviewer.lastName}</div>
            <div className="mb-2"><b>Email:</b> {viewInterviewer.email}</div>
            <div className="mb-2"><b>Username:</b> {viewInterviewer.username}</div>
            <div className="mb-2"><b>Status:</b> {viewInterviewer.enabled ? 'Active' : 'Inactive'}</div>
            <div className="mb-2"><b>Role:</b> {viewInterviewer.role}</div>
            <div className="mb-2"><b>Assigned Interviews:</b> {viewInterviewer.assigned || 0}</div>
            <div className="mb-2"><b>Created At:</b> {viewInterviewer.createdAt}</div>
            <div className="mb-2"><b>Updated At:</b> {viewInterviewer.updatedAt}</div>
          </div>
        </div>
      )}
      {/* Edit Interviewer Modal */}
      {editInterviewer && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md relative">
            <button className="absolute top-2 right-2 text-gray-400 hover:text-gray-600 text-2xl" onClick={() => setEditInterviewer(null)} aria-label="Close">&times;</button>
            <h2 className="text-xl font-bold mb-4">Edit Interviewer</h2>
            <InterviewerForm
              initialValues={{
                username: editInterviewer.username || '',
                email: editInterviewer.email || '',
                password: '', // Don't show password for edit
                firstName: editInterviewer.firstName || '',
                lastName: editInterviewer.lastName || '',
                status: editInterviewer.enabled ? 'Active' : 'Inactive',
              }}
              onSubmit={handleEditInterviewer}
              loading={editLoading}
              error={editError}
              submitLabel="Save Changes"
            />
          </div>
        </div>
      )}
      {/* Delete Interviewer Modal */}
      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md relative">
            <button className="absolute top-2 right-2 text-gray-400 hover:text-gray-600 text-2xl" onClick={() => setDeleteTarget(null)} aria-label="Close">&times;</button>
            <h2 className="text-xl font-bold mb-4">Delete Interviewer</h2>
            <p className="mb-4">Are you sure you want to delete this interviewer? This action cannot be undone.</p>
            {deleteError && <div className="text-red-600 mb-2">{deleteError}</div>}
            <div className="flex gap-2">
              <button className="btn-danger flex-1" onClick={handleConfirmDelete} disabled={deleteLoading}>{deleteLoading ? 'Deleting...' : 'Delete'}</button>
              <button className="btn-secondary flex-1" onClick={() => setDeleteTarget(null)} disabled={deleteLoading}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Interviewers; 