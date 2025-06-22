import React from 'react';
import { render, screen } from '@testing-library/react';
import Interviewers from './Interviewers';
import { AuthProvider } from '../../context/AuthContext';
import { MemoryRouter } from 'react-router-dom';
import axios from 'axios';

jest.mock('axios');

const mockInterviewers = [
  {
    id: 1,
    name: 'Jane Smith',
    email: 'jane@example.com',
    assignedInterviews: 3,
    status: 'Active',
  },
];

(axios.get as jest.Mock).mockResolvedValue({ data: mockInterviewers });

test('renders Interviewers page with correct heading and new interviewer button', async () => {
  render(
    <AuthProvider>
      <MemoryRouter>
        <Interviewers />
      </MemoryRouter>
    </AuthProvider>
  );
  // Check for the actual heading text
  expect(await screen.findByRole('heading', { name: /interviewers/i })).toBeInTheDocument();
  // Check for the '+ New Interviewer' button
  expect(await screen.findByRole('button', { name: /new interviewer/i })).toBeInTheDocument();
}); 