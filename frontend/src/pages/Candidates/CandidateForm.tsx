import React, { useState } from 'react';
import { CandidateFormValues } from '../../types/CandidateFormValues';

interface CandidateFormProps {
  initialValues?: CandidateFormValues;
  onSubmit: (values: CandidateFormValues) => void;
  loading?: boolean;
  submitLabel?: string;
}

const defaultValues: CandidateFormValues = {
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  position: '',
  status: 'Active',
};

const inputClasses =
  "w-full px-3 py-2 border border-gray-300 rounded-md bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 shadow-sm";

const CandidateForm: React.FC<CandidateFormProps> = ({ initialValues, onSubmit, loading, submitLabel }) => {
  const [values, setValues] = useState<CandidateFormValues>(initialValues || defaultValues);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setValues({ ...values, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(values);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4" autoComplete="off">
      <div>
        <label className="form-label mb-2 block">First Name</label>
        <input
          type="text"
          name="firstName"
          value={values.firstName}
          onChange={handleChange}
          className={inputClasses + " mt-1"}
          required
          autoComplete="off"
        />
      </div>
      <div>
        <label className="form-label mb-2 block">Last Name</label>
        <input
          type="text"
          name="lastName"
          value={values.lastName}
          onChange={handleChange}
          className={inputClasses + " mt-1"}
          required
          autoComplete="off"
        />
      </div>
      <div>
        <label className="form-label mb-2 block">Email</label>
        <input
          type="email"
          name="email"
          value={values.email}
          onChange={handleChange}
          className={inputClasses + " mt-1"}
          required
          autoComplete="off"
        />
      </div>
      <div>
        <label className="form-label mb-2 block">Phone</label>
        <input
          type="text"
          name="phone"
          value={values.phone}
          onChange={handleChange}
          className={inputClasses + " mt-1"}
          required
          autoComplete="off"
        />
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
          autoComplete="off"
        />
      </div>
      <div>
        <label className="form-label mb-2 block">Status</label>
        <select
          name="status"
          value={values.status}
          onChange={handleChange}
          className={inputClasses + " mt-1"}
          autoComplete="off"
        >
          <option value="Active">Active</option>
          <option value="Inactive">Inactive</option>
          <option value="Archived">Archived</option>
        </select>
      </div>
      <button
        type="submit"
        className="btn-primary w-full flex justify-center items-center mt-4"
        disabled={loading}
      >
        {loading ? 'Saving...' : submitLabel || 'Save'}
      </button>
    </form>
  );
};

export default CandidateForm; 