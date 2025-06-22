import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Candidate } from '../../types/Candidate';
import { getCandidates, createCandidate, updateCandidate } from '../../services/candidateService';
import CandidateForm from './CandidateForm';
import CandidateDetail from './CandidateDetail';

const statusOptions = ['All', 'Active', 'Inactive', 'Archived'];

const Candidates: React.FC = () => {
  const [candidates, setCandidates] = useState<Candidate[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('All');
  const navigate = useNavigate();
  const [selectedCandidate, setSelectedCandidate] = useState<Candidate | null>(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [editFormLoading, setEditFormLoading] = useState(false);
  const [editFormError, setEditFormError] = useState<string | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const fetchCandidates = () => {
    setLoading(true);
    getCandidates()
      .then((data) => {
        // Defensive: handle both paginated and array responses
        if (data && Array.isArray(data.content)) {
          setCandidates(data.content);
        } else if (Array.isArray(data)) {
          setCandidates(data);
        } else {
          setCandidates([]);
        }
      })
      .catch(() => setError('Failed to load candidates.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchCandidates();
  }, []);

  const handleAddCandidate = async (values: any) => {
    setFormLoading(true);
    setFormError(null);
    try {
      await createCandidate(values);
      setShowModal(false);
      fetchCandidates();
    } catch {
      setFormError('Failed to add candidate.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleToggleStatus = async (candidate: Candidate) => {
    try {
      await updateCandidate(candidate.id, {
        firstName: candidate.firstName,
        lastName: candidate.lastName,
        email: candidate.email,
        phone: candidate.phone,
        position: candidate.position || '',
        status: candidate.status,
      });
      fetchCandidates();
    } catch (err) {
      alert('Failed to update status');
    }
  };

  const handleView = (candidate: Candidate) => {
    navigate(`/candidates/${candidate.id}`);
  };

  const handleEdit = (candidate: Candidate) => {
    setSelectedCandidate({ ...candidate, position: candidate.position || '' });
    setShowEditModal(true);
    setEditFormError(null);
  };

  const handleDelete = (candidate: Candidate) => {
    setSelectedCandidate(candidate);
    setShowDeleteModal(true);
    setDeleteError(null);
  };

  const handleEditCandidate = async (values: any) => {
    if (!selectedCandidate) return;
    setEditFormLoading(true);
    setEditFormError(null);
    try {
      await updateCandidate(selectedCandidate.id, { ...values, position: values.position || '' });
      setShowEditModal(false);
      setSelectedCandidate(null);
      fetchCandidates();
    } catch {
      setEditFormError('Failed to update candidate.');
    } finally {
      setEditFormLoading(false);
    }
  };

  const handleDeleteCandidate = async () => {
    if (!selectedCandidate) return;
    setDeleteLoading(true);
    setDeleteError(null);
    try {
      await updateCandidate(selectedCandidate.id, { ...selectedCandidate, position: selectedCandidate.position || '', status: 'Archived' });
      setShowDeleteModal(false);
      setSelectedCandidate(null);
      fetchCandidates();
    } catch {
      setDeleteError('Failed to delete candidate.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const filteredCandidates = Array.isArray(candidates)
    ? candidates.filter((candidate) => {
        const fullName = candidate.fullName || '';
        const email = candidate.email || '';
        const phone = candidate.phone || '';
        const matchesSearch =
          fullName.toLowerCase().includes(search.toLowerCase()) ||
          email.toLowerCase().includes(search.toLowerCase()) ||
          phone.toLowerCase().includes(search.toLowerCase());
        const matchesStatus = status === 'All' || candidate.status === status;
        return matchesSearch && matchesStatus;
      })
    : [];

  const getStatusDisplay = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'ACTIVE':
        return 'Active';
      case 'INACTIVE':
        return 'Inactive';
      case 'ARCHIVED':
        return 'Archived';
      default:
        return status || 'Unknown';
    }
  };

  const getStatusClass = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-700';
      case 'INACTIVE':
        return 'bg-yellow-100 text-yellow-700';
      case 'ARCHIVED':
        return 'bg-gray-100 text-gray-700';
      default:
        return 'bg-indigo-100 text-indigo-700';
    }
  };

  return (
    <div className="pt-8">
      <div className="flex flex-wrap gap-4 items-center mb-8 justify-between">
        <h1 className="text-3xl font-bold mb-0">Candidates</h1>
        <button
          className="btn-primary px-4 py-2 text-sm min-w-[140px]"
          onClick={() => setShowModal(true)}
        >
          + New Candidate
        </button>
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
            placeholder="Search by name, email, or phone"
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
      {error && <div className="text-red-600">{error}</div>}
      {!loading && !error && (
        <div className="overflow-x-auto">
          <table className="min-w-full bg-white rounded-lg shadow-md">
            <thead>
              <tr className="bg-gray-100 text-left">
                <th className="py-3 px-4 font-semibold">Name</th>
                <th className="py-3 px-4 font-semibold">Email</th>
                <th className="py-3 px-4 font-semibold">Phone</th>
                <th className="py-3 px-4 font-semibold">Status</th>
                <th className="py-3 px-4 font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredCandidates.length === 0 ? (
                <tr>
                  <td colSpan={4} className="py-6 text-center text-gray-500">No candidates found.</td>
                </tr>
              ) : (
                filteredCandidates.map((candidate) => (
                  <tr
                    key={candidate.id}
                    className="border-b hover:bg-indigo-50 transition-colors cursor-pointer"
                    onClick={() => handleView(candidate)}
                  >
                    <td className="py-2 px-4">{candidate.fullName}</td>
                    <td className="py-2 px-4">{candidate.email}</td>
                    <td className="py-2 px-4">{candidate.phone}</td>
                    <td className="py-2 px-4">
                      <select
                        className={`input-field font-medium ${getStatusClass(candidate.status)}`}
                        value={getStatusDisplay(candidate.status)}
                        onClick={e => e.stopPropagation()}
                        onChange={async e => {
                          const selected = e.target.value;
                          let newStatus = selected.toUpperCase();
                          if (newStatus === 'ALL') return; // skip
                          await handleToggleStatus({ ...candidate, status: newStatus });
                        }}
                      >
                        {statusOptions.filter(opt => opt !== 'All').map(opt => (
                          <option key={opt} value={opt}>{opt}</option>
                        ))}
                      </select>
                    </td>
                    <td className="py-2 px-4 flex gap-2">
                      <button
                        className="text-blue-600 hover:underline text-sm"
                        onClick={e => { e.stopPropagation(); handleEdit(candidate); }}
                      >Edit</button>
                      <button
                        className="text-red-600 hover:underline text-sm"
                        onClick={e => { e.stopPropagation(); handleDelete(candidate); }}
                      >Delete</button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal for Add Candidate */}
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
            <h2 className="text-xl font-bold mb-4">Add Candidate</h2>
            {formError && <div className="text-red-600 mb-2">{formError}</div>}
            <CandidateForm
              onSubmit={handleAddCandidate}
              loading={formLoading}
              submitLabel="Add"
            />
          </div>
        </div>
      )}

      {/* Modal for Edit Candidate */}
      {showEditModal && selectedCandidate && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md relative">
            <button
              className="absolute top-2 right-2 text-gray-400 hover:text-gray-600 text-2xl"
              onClick={() => setShowEditModal(false)}
              aria-label="Close"
            >
              &times;
            </button>
            <h2 className="text-xl font-bold mb-4">Edit Candidate</h2>
            {editFormError && <div className="text-red-600 mb-2">{editFormError}</div>}
            <CandidateForm
              initialValues={{ ...selectedCandidate, position: selectedCandidate.position || '' }}
              onSubmit={handleEditCandidate}
              loading={editFormLoading}
              submitLabel="Save"
            />
          </div>
        </div>
      )}

      {/* Modal for Delete Candidate */}
      {showDeleteModal && selectedCandidate && (
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

export default Candidates; 