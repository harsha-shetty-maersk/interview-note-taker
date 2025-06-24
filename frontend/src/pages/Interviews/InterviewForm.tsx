import React, { useState, useEffect } from 'react';

export interface InterviewFormValues {
  candidateId: string;
  position: string;
  date: string;
  time: string;
  duration: string;
  interviewerId: string;
  status: string;
}

interface InterviewFormProps {
  candidates: { id: string; name: string; position: string }[];
  interviewers: { id: string; name: string }[];
  onSubmit: (values: InterviewFormValues) => void;
  loading?: boolean;
  error?: string | null;
  submitLabel?: string;
  isCompleted?: boolean;
}

const statusOptions = [
  { label: 'Scheduled', value: 'SCHEDULED' },
  { label: 'Completed', value: 'COMPLETED' },
];

const inputClasses =
  "w-full px-3 py-2 border border-gray-300 rounded-md bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 shadow-sm";

const InterviewForm: React.FC<InterviewFormProps> = ({ candidates, interviewers, onSubmit, loading, error, submitLabel, isCompleted }) => {
  const getDefaultDate = () => {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return d.toISOString().slice(0, 10);
  };
  const getDefaultInterviewer = (interviewers: { id: string; name: string }[]) =>
    interviewers.length > 0 ? interviewers[0].id : '';

  const [values, setValues] = useState<InterviewFormValues>({
    candidateId: '',
    position: '',
    date: getDefaultDate(),
    time: '10:00',
    duration: '60',
    interviewerId: getDefaultInterviewer(interviewers),
    status: 'SCHEDULED',
  });
  const [formError, setFormError] = useState<string | null>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setValues({ ...values, [e.target.name]: e.target.value });
    setFormError(null);
  };

  const handleCandidateChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selected = candidates.find(c => c.id === e.target.value);
    setValues({
      ...values,
      candidateId: e.target.value,
      position: selected ? selected.position : '',
    });
    setFormError(null);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Validation
    if (!values.candidateId || !values.position || !values.date || !values.time || !values.duration || !values.interviewerId || !values.status) {
      setFormError('All fields are required.');
      return;
    }
    if (isNaN(Number(values.duration)) || Number(values.duration) <= 0) {
      setFormError('Duration must be a positive number.');
      return;
    }
    const interviewDateTime = new Date(`${values.date}T${values.time}`);
    if (isNaN(interviewDateTime.getTime()) || interviewDateTime < new Date()) {
      setFormError('Please select a valid future date and time.');
      return;
    }
    setFormError(null);
    onSubmit(values);
  };

  useEffect(() => {
    setValues(v => ({ ...v, interviewerId: getDefaultInterviewer(interviewers) }));
  }, [interviewers]);

  return (
    <form onSubmit={handleSubmit} className="space-y-4" autoComplete="off">
      {formError && <div className="text-red-600 mb-2">{formError}</div>}
      {error && <div className="text-red-600 mb-2">{error}</div>}
      <div>
        <label className="form-label mb-2 block">Candidate</label>
        <select
          name="candidateId"
          value={values.candidateId}
          onChange={handleCandidateChange}
          className={inputClasses + " mt-1"}
          required
        >
          <option value="">Select candidate</option>
          {candidates.map(c => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
      </div>
      <div>
        <label className="form-label mb-2 block">Position</label>
        <input
          type="text"
          name="position"
          value={values.position}
          onChange={handleChange}
          className={inputClasses + " mt-1"}
          required
        />
      </div>
      <div className="flex gap-4">
        <div className="flex-1">
          <label htmlFor="date" className="form-label mb-2 block">Date</label>
          <input
            id="date"
            type="date"
            name="date"
            value={values.date}
            onChange={e => {
              const today = new Date().toISOString().slice(0, 10);
              if (e.target.value < today) {
                handleChange({
                  ...e,
                  target: {
                    ...e.target,
                    value: today,
                  },
                });
              } else {
                handleChange(e);
              }
            }}
            className={inputClasses + " mt-1"}
            required
            min={new Date().toISOString().slice(0, 10)}
            disabled={!!isCompleted}
          />
        </div>
        <div className="flex-1">
          <label htmlFor="time" className="form-label mb-2 block">Time</label>
          <input
            id="time"
            type="time"
            name="time"
            value={values.time}
            onChange={handleChange}
            className={inputClasses + " mt-1"}
            required
            disabled={!!isCompleted}
          />
        </div>
      </div>
      <div>
        <label className="form-label mb-2 block">Duration (minutes)</label>
        <input
          type="number"
          name="duration"
          value={values.duration}
          onChange={handleChange}
          className={inputClasses + " mt-1"}
          min={1}
          required
        />
      </div>
      <div>
        <label className="form-label mb-2 block">Interviewer</label>
        <select
          name="interviewerId"
          value={values.interviewerId}
          onChange={handleChange}
          className={inputClasses + " mt-1"}
          required
        >
          <option value="">Select interviewer</option>
          {interviewers.map(i => (
            <option key={i.id} value={i.id}>{i.name}</option>
          ))}
        </select>
      </div>
      <div>
        <label className="form-label mb-2 block">Status</label>
        <select
          name="status"
          value={values.status}
          onChange={handleChange}
          className={inputClasses + " mt-1"}
          required
        >
          {statusOptions.map(opt => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      </div>
      <button
        type="submit"
        className="btn-primary w-full flex justify-center items-center mt-4"
        disabled={loading}
      >
        {loading ? 'Saving...' : submitLabel || 'Create Interview'}
      </button>
    </form>
  );
};

export default InterviewForm; 