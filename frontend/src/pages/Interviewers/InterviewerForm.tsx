import React, { useState } from 'react';

export interface InterviewerFormValues {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  status: string;
}

interface InterviewerFormProps {
  initialValues?: InterviewerFormValues;
  onSubmit: (values: InterviewerFormValues) => void;
  loading?: boolean;
  error?: string | null;
  submitLabel?: string;
}

const defaultValues: InterviewerFormValues = {
  username: '',
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  status: 'Active',
};

const inputClasses =
  "w-full px-3 py-2 border border-gray-300 rounded-md bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 shadow-sm";

const InterviewerForm: React.FC<InterviewerFormProps> = ({ initialValues, onSubmit, loading, error, submitLabel }) => {
  const [values, setValues] = useState<InterviewerFormValues>(initialValues || defaultValues);
  const [formError, setFormError] = useState<string | null>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setValues({ ...values, [e.target.name]: e.target.value });
    setFormError(null);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!values.username || !values.email || !values.password || !values.firstName || !values.lastName) {
      setFormError('All fields are required.');
      return;
    }
    setFormError(null);
    onSubmit(values);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4" autoComplete="off">
      {formError && <div className="text-red-600 mb-2">{formError}</div>}
      {error && <div className="text-red-600 mb-2">{error}</div>}
      <div>
        <label className="form-label mb-2 block">Username</label>
        <input
          type="text"
          name="username"
          value={values.username}
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
        <label className="form-label mb-2 block">Password</label>
        <input
          type="password"
          name="password"
          value={values.password}
          onChange={handleChange}
          className={inputClasses + " mt-1"}
          required
          autoComplete="off"
        />
      </div>
      <div className="flex gap-4">
        <div className="flex-1">
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
        <div className="flex-1">
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

export default InterviewerForm; 