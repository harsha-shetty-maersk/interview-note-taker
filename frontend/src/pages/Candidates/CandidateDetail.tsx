import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { Candidate } from '../../types/Candidate';
import { getCandidateById, updateCandidate, deleteCandidate } from '../../services/candidateService';
import CandidateForm from './CandidateForm';
import { CandidateFormValues } from '../../types/CandidateFormValues';

const CandidateDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [candidate, setCandidate] = useState<Candidate | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const fetchCandidate = () => {
    if (!id) return;
    setLoading(true);
    getCandidateById(Number(id))
      .then(setCandidate)
      .catch(() => setError('Failed to load candidate.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchCandidate();
    // eslint-disable-next-line
  }, [id]);

  const handleEditCandidate = async (values: CandidateFormValues) => {
    if (!id) return;
    setFormLoading(true);
    setFormError(null);
    try {
      await updateCandidate(Number(id), values);
      setShowModal(false);
      fetchCandidate();
    } catch {
      setFormError('Failed to update candidate.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeleteCandidate = async () => {
    if (!id) return;
    setDeleteLoading(true);
    setDeleteError(null);
    try {
      await deleteCandidate(Number(id));
      setShowDeleteModal(false);
      navigate('/candidates');
    } catch {
      setDeleteError('Failed to delete candidate.');
    } finally {
      setDeleteLoading(false);
    }
  };

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <Link to="/candidates" className="text-indigo-600 hover:underline mb-4 inline-block">&larr; Back to Candidates</Link>
      {loading && <div className="text-gray-500">Loading...</div>}
      {error && <div className="text-red-600">{error}</div>}
      {!loading && !error && candidate && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h1 className="text-2xl font-bold">{candidate.fullName}</h1>
            <div className="flex gap-2">
              <button
                className="btn-secondary"
                onClick={() => setShowModal(true)}
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
          <div className="mb-2"><span className="font-semibold">Email:</span> {candidate.email}</div>
          <div className="mb-2"><span className="font-semibold">Phone:</span> {candidate.phone}</div>
          {candidate.position && <div className="mb-2"><span className="font-semibold">Position:</span> {candidate.position}</div>}
          {candidate.experience && <div className="mb-2"><span className="font-semibold">Experience:</span> {candidate.experience} years</div>}
          <div className="mb-2">
            <span className="font-semibold">Status:</span>
            <span className="ml-2 px-2 py-1 rounded text-xs font-medium bg-indigo-100 text-indigo-700">{candidate.status}</span>
          </div>
        </div>
      )}
      {/* Modal for Edit Candidate */}
      {showModal && candidate && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md relative">
            <button
              className="absolute top-2 right-2 text-gray-400 hover:text-gray-600 text-2xl"
              onClick={() => setShowModal(false)}
              aria-label="Close"
            >
              &times;
            </button>
            <h2 className="text-xl font-bold mb-4">Edit Candidate</h2>
            {formError && <div className="text-red-600 mb-2">{formError}</div>}
            <CandidateForm
              initialValues={{
                firstName: candidate.firstName,
                lastName: candidate.lastName,
                email: candidate.email,
                phone: candidate.phone,
                position: candidate.position || '',
                status: candidate.status,
              }}
              onSubmit={handleEditCandidate}
              loading={formLoading}
              submitLabel="Save"
            />
          </div>
        </div>
      )}
      {/* Modal for Delete Candidate */}
      {showDeleteModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md relative">
            <button
              className="absolute top-2 right-2 text-gray-400 hover:text-gray-600 text-2xl"
              onClick={() => setShowDeleteModal(false)}
              aria-label="Close"
            >
              &times;
            </button>
            <h2 className="text-xl font-bold mb-4">Delete Candidate</h2>
            <p className="mb-4">Are you sure you want to delete this candidate? This action cannot be undone.</p>
            {deleteError && <div className="text-red-600 mb-2">{deleteError}</div>}
            <div className="flex gap-2">
              <button
                className="btn-danger flex-1"
                onClick={handleDeleteCandidate}
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

export default CandidateDetail; 