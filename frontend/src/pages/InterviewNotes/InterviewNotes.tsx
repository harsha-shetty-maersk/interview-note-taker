import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axios from 'axios';
import authService from '../../services/authService';
import dayjs from 'dayjs';

interface TimestampedNote {
  id: string;
  timestamp: string;
  content: string;
}

interface Interview {
  id: number;
  candidateId: number;
  candidateName: string;
  position: string;
  date: string;
  status: string;
  interviewerId: number;
  interviewerName: string;
  duration: number;
  notes: string;
  overallScore: number;
  finalRecommendation: string;
}

interface ScoringSection {
  technicalSkills: { score: number; details: string };
  problemSolving: { score: number; details: string };
  communication: { score: number; details: string };
  culturalFit: { score: number; details: string };
}

const InterviewNotes: React.FC = () => {
  const { interviewId } = useParams<{ interviewId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [interview, setInterview] = useState<Interview | null>(null);
  const [interviews, setInterviews] = useState<Interview[]>([]);
  const [notes, setNotes] = useState<TimestampedNote[]>([]);
  const [currentNote, setCurrentNote] = useState('');
  const [scoring, setScoring] = useState<ScoringSection>({
    technicalSkills: { score: 0, details: '' },
    problemSolving: { score: 0, details: '' },
    communication: { score: 0, details: '' },
    culturalFit: { score: 0, details: '' }
  });
  const [finalRecommendation, setFinalRecommendation] = useState('');
  const [recommendationExplanation, setRecommendationExplanation] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const notesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (interviewId) {
      fetchInterview();
    } else {
      fetchInterviews();
    }
  }, [interviewId]);

  useEffect(() => {
    // Auto-scroll to bottom when new notes are added
    notesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [notes]);

  const fetchInterviews = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/interviews', {
        headers: authService.getAuthHeaders()
      });
      // Handle paginated response - extract content array
      const interviewsData = response.data.content || response.data;
      setInterviews(Array.isArray(interviewsData) ? interviewsData : []);
    } catch (err) {
      setError('Failed to load interviews.');
    } finally {
      setLoading(false);
    }
  };

  const fetchInterview = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`/api/interviews/${interviewId}`, {
        headers: authService.getAuthHeaders()
      });
      setInterview(response.data);
      
      // Load existing notes if any
      if (response.data.notes) {
        try {
          const parsedNotes = JSON.parse(response.data.notes);
          if (parsedNotes.timestampedNotes && Array.isArray(parsedNotes.timestampedNotes)) {
            setNotes(parsedNotes.timestampedNotes);
          }
          if (parsedNotes.scoring) {
            setScoring(parsedNotes.scoring);
          }
          if (parsedNotes.finalRecommendation) {
            setFinalRecommendation(parsedNotes.finalRecommendation);
          }
          if (parsedNotes.recommendationExplanation) {
            setRecommendationExplanation(parsedNotes.recommendationExplanation);
          }
        } catch (e) {
          // If notes are not in JSON format, treat as single note
          if (response.data.notes.trim()) {
            setNotes([{
              id: '1',
              timestamp: dayjs().format('HH:mm'),
              content: response.data.notes
            }]);
          }
        }
      }
    } catch (err) {
      setError('Failed to load interview details.');
    } finally {
      setLoading(false);
    }
  };

  const addTimestampedNote = () => {
    if (currentNote.trim()) {
      const newNote: TimestampedNote = {
        id: Date.now().toString(),
        timestamp: dayjs().format('HH:mm'),
        content: currentNote.trim()
      };
      setNotes([...notes, newNote]);
      setCurrentNote('');
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      addTimestampedNote();
    }
  };

  const updateScoring = (section: keyof ScoringSection, field: 'score' | 'details', value: string | number) => {
    setScoring(prev => ({
      ...prev,
      [section]: {
        ...prev[section],
        [field]: value
      }
    }));
  };

  const calculateOverallScore = () => {
    const scores = [
      scoring.technicalSkills.score,
      scoring.problemSolving.score,
      scoring.communication.score,
      scoring.culturalFit.score
    ].filter(score => score > 0);
    
    return scores.length > 0 ? scores.reduce((sum, score) => sum + score, 0) / scores.length : 0;
  };

  const saveNotes = async () => {
    try {
      setSaving(true);
      setError(null);
      setSuccess(null);

      // Fetch the full interview object first
      const response = await axios.get(`/api/interviews/${interviewId}`, {
        headers: authService.getAuthHeaders()
      });
      const fullInterview = response.data;

      const overallScore = calculateOverallScore();
      const notesData = {
        timestampedNotes: notes,
        scoring: scoring,
        finalRecommendation: finalRecommendation,
        recommendationExplanation: recommendationExplanation
      };

      // Merge new fields into the full interview object
      const payload = {
        ...fullInterview,
        notes: JSON.stringify(notesData),
        overallScore: overallScore,
        finalRecommendation: finalRecommendation,
        status: 'COMPLETED'
      };

      await axios.put(`/api/interviews/${interviewId}`, payload, {
        headers: authService.getAuthHeaders()
      });

      setSuccess('Notes saved successfully!');
      // Refresh the interview data to show updated information
      await fetchInterview();
    } catch (err) {
      setError('Failed to save notes.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="pt-8">
        <div className="text-gray-500">Loading interview details...</div>
      </div>
    );
  }

  // Show interview selection if no interviewId is provided
  if (!interviewId) {
    return (
      <div className="pt-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-4">Round Up - Select Interview</h1>
          <p className="text-gray-600">Choose an interview to take notes for:</p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg">
            {error}
          </div>
        )}

        {interviews.length === 0 ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <p className="text-gray-500 mb-4">No interviews assigned to you.</p>
            <button 
              onClick={() => navigate('/interviews')}
              className="btn-primary"
            >
              View All Interviews
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {interviews.map((interview) => (
              <div key={interview.id} className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
                <div className="mb-4">
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">
                    {interview.candidateName}
                  </h3>
                  <p className="text-gray-600 mb-1">{interview.position}</p>
                  <p className="text-sm text-gray-500">
                    {dayjs(interview.date).format('MMM DD, YYYY [at] h:mm A')}
                  </p>
                </div>
                
                <div className="mb-4">
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                    interview.status === 'SCHEDULED' ? 'bg-blue-100 text-blue-800' :
                    interview.status === 'IN_PROGRESS' ? 'bg-yellow-100 text-yellow-800' :
                    interview.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                    'bg-gray-100 text-gray-800'
                  }`}>
                    {interview.status.replace('_', ' ')}
                  </span>
                </div>

                <div className="flex space-x-2">
                  <button
                    onClick={() => navigate(`/interview-notes/${interview.id}`)}
                    className="flex-1 btn-primary text-sm"
                  >
                    {interview.status === 'COMPLETED' ? 'View Notes' : 'Take Notes'}
                  </button>
                  <button
                    onClick={() => navigate(`/interviews/${interview.id}`)}
                    className="btn-secondary text-sm"
                  >
                    Details
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    );
  }

  if (error) {
    return (
      <div className="pt-8">
        <div className="text-red-600 mb-4">{error}</div>
        <button 
          onClick={() => navigate('/interviews')}
          className="btn-secondary"
        >
          Back to Interviews
        </button>
      </div>
    );
  }

  if (!interview) {
    return (
      <div className="pt-8">
        <div className="text-red-600 mb-4">Interview not found.</div>
        <button 
          onClick={() => navigate('/interviews')}
          className="btn-secondary"
        >
          Back to Interviews
        </button>
      </div>
    );
  }

  return (
    <div className="pt-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-3xl font-bold">{interview.candidateName}</h1>
          <div className="flex space-x-2">
            <button 
              onClick={() => navigate('/interview-notes')}
              className="btn-secondary"
            >
              Back to Selection
            </button>
            <button 
              onClick={() => navigate('/interviews')}
              className="btn-secondary"
            >
              Back to Interviews
            </button>
          </div>
        </div>
        
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Position</label>
              <p className="text-gray-900">{interview.position}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Interviewer</label>
              <p className="text-gray-900">{interview.interviewerName}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Date & Time</label>
              <p className="text-gray-900">
                {dayjs(interview.date).format('MMM DD, YYYY [at] h:mm A')}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Success/Error Messages */}
      {success && (
        <div className="mb-6 p-4 bg-green-100 border border-green-400 text-green-700 rounded-lg">
          {success}
        </div>
      )}
      {error && (
        <div className="mb-6 p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Live Notes Section */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4">Live Interview Notes</h2>
          
          {/* Notes Display */}
          <div className="bg-gray-50 rounded-lg p-4 mb-4 h-96 overflow-y-auto">
            {notes.length === 0 ? (
              <p className="text-gray-500 text-center">No notes yet. Start typing below...</p>
            ) : (
              <div className="space-y-3">
                {notes.map((note) => (
                  <div key={note.id} className="bg-white rounded-lg p-3 shadow-sm">
                    <div className="flex items-start justify-between mb-2">
                      <span className="text-sm font-medium text-indigo-600 bg-indigo-100 px-2 py-1 rounded">
                        {note.timestamp}
                      </span>
                    </div>
                    <p className="text-gray-800 whitespace-pre-wrap">{note.content}</p>
                  </div>
                ))}
                <div ref={notesEndRef} />
              </div>
            )}
          </div>

          {/* Note Input */}
          <div className="space-y-3">
            <textarea
              placeholder="Type your notes here... (Press Enter to add, Shift+Enter for new line)"
              value={currentNote}
              onChange={(e) => setCurrentNote(e.target.value)}
              onKeyPress={handleKeyPress}
              className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
              rows={4}
            />
            <div className="flex justify-between items-center">
              <button
                onClick={addTimestampedNote}
                className="btn-primary"
                disabled={!currentNote.trim()}
              >
                Add Note
              </button>
              <span className="text-sm text-gray-500">
                {currentNote.length} characters
              </span>
            </div>
          </div>
        </div>

        {/* Scoring and Assessment Section */}
        <div className="space-y-6">
          {/* Scoring Sections */}
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-semibold mb-4">Scoring & Assessment</h2>
            
            <div className="space-y-4">
              {[
                { key: 'technicalSkills', label: 'Technical Skills' },
                { key: 'problemSolving', label: 'Problem Solving' },
                { key: 'communication', label: 'Communication' },
                { key: 'culturalFit', label: 'Cultural Fit' }
              ].map((section) => (
                <div key={section.key} className="border rounded-lg p-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    {section.label}
                  </label>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs text-gray-600 mb-1">Score (1-10)</label>
                      <input
                        type="number"
                        min="1"
                        max="10"
                        value={scoring[section.key as keyof ScoringSection].score || ''}
                        onChange={(e) => updateScoring(section.key as keyof ScoringSection, 'score', Number(e.target.value))}
                        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-600 mb-1">Details (max 500 chars)</label>
                      <textarea
                        placeholder={`${section.label} details...`}
                        value={scoring[section.key as keyof ScoringSection].details}
                        onChange={(e) => updateScoring(section.key as keyof ScoringSection, 'details', e.target.value)}
                        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
                        rows={2}
                        maxLength={500}
                      />
                      <div className="text-xs text-gray-500 text-right mt-1">
                        {scoring[section.key as keyof ScoringSection].details.length}/500
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Overall Score Display */}
            <div className="mt-4 p-3 bg-indigo-50 rounded-lg">
              <div className="flex justify-between items-center">
                <span className="font-medium text-indigo-800">Overall Score:</span>
                <span className="text-2xl font-bold text-indigo-600">
                  {calculateOverallScore().toFixed(1)}/10
                </span>
              </div>
            </div>
          </div>

          {/* Final Recommendation */}
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-semibold mb-4">Final Recommendation</h2>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Recommendation</label>
                <select
                  value={finalRecommendation}
                  onChange={(e) => setFinalRecommendation(e.target.value)}
                  className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  <option value="">Select recommendation...</option>
                  <option value="STRONG_HIRE">Strong Hire</option>
                  <option value="HIRE">Hire</option>
                  <option value="WEAK_HIRE">Weak Hire</option>
                  <option value="NO_HIRE">No Hire</option>
                  <option value="HOLD">Hold</option>
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Explanation (max 1000 characters)
                </label>
                <textarea
                  placeholder="Provide detailed explanation for your recommendation..."
                  value={recommendationExplanation}
                  onChange={(e) => setRecommendationExplanation(e.target.value)}
                  className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
                  rows={4}
                  maxLength={1000}
                />
                <div className="text-xs text-gray-500 text-right mt-1">
                  {recommendationExplanation.length}/1000
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Save Button */}
      <div className="mt-8 flex justify-end space-x-4">
        <button
          onClick={() => navigate('/interviews')}
          className="btn-secondary"
          disabled={saving}
        >
          Cancel
        </button>
        <button
          onClick={saveNotes}
          className="btn-primary"
          disabled={saving}
        >
          {saving ? 'Saving...' : 'Save Notes'}
        </button>
      </div>
    </div>
  );
};

export default InterviewNotes; 