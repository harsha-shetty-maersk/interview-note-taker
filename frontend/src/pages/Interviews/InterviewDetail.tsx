import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { Interview } from '../../services/interviewService';
import { getInterviews, deleteInterview } from '../../services/interviewService';
import dayjs from 'dayjs';

const InterviewDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [interview, setInterview] = useState<Interview | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    getInterviews()
      .then((data) => {
        const list = Array.isArray(data.content) ? data.content : Array.isArray(data) ? data : [];
        const found = list.find((i: Interview) => String(i.id) === String(id));
        setInterview(found || null);
      })
      .catch(() => setError('Failed to load interview.'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleDeleteInterview = async () => {
    if (!id) return;
    setDeleteLoading(true);
    setDeleteError(null);
    try {
      await deleteInterview(Number(id));
      setShowDeleteModal(false);
      navigate('/interviews');
    } catch {
      setDeleteError('Failed to delete interview.');
    } finally {
      setDeleteLoading(false);
    }
  };

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <Link to="/interviews" className="text-indigo-600 hover:underline mb-4 inline-block">&larr; Back to Interviews</Link>
      {loading && <div className="text-gray-500">Loading...</div>}
      {error && <div className="text-red-600">{error}</div>}
      {!loading && !error && interview && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h1 className="text-2xl font-bold">{interview.candidateName}</h1>
            <div className="flex gap-2">
              <button
                className="btn-secondary"
                onClick={() => navigate(`/interviews/${interview.id}/edit`)}
              >
                Edit
              </button>
              <button
                className="btn-danger"
                onClick={() => setShowDeleteModal(true)}
              >
                Delete
              </button>
            </div>
          </div>
          <div className="mb-2"><span className="font-semibold">Interviewer:</span> {interview.interviewerName}</div>
          <div className="mb-2"><span className="font-semibold">Scheduled Date:</span> {dayjs(interview.date).format('YYYY-MM-DD')}</div>
          <div className="mb-2"><span className="font-semibold">Scheduled Time:</span> {dayjs(interview.date).format('HH:mm')}</div>
          <div className="mb-2"><span className="font-semibold">Status:</span> <span className="ml-2 px-2 py-1 rounded text-xs font-medium bg-indigo-100 text-indigo-700">{interview.status}</span></div>
        </div>
      )}
      {/* Modal for Delete Interview */}
      {showDeleteModal && interview && (
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
            {deleteError && <div className="text-red-600 mb-2">{deleteError}</div>}
            <div className="flex gap-2">
              <button
                className="btn-danger flex-1"
                onClick={handleDeleteInterview}
                disabled={deleteLoading}
              >
                {deleteLoading ? 'Deleting...' : 'Delete'}
              </button>
              <button
                className="btn-secondary flex-1"
                onClick={() => setShowDeleteModal(false)}
                disabled={deleteLoading}
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

export default InterviewDetail; 