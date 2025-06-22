import React from 'react';
import { render, screen } from '@testing-library/react';
import InterviewNotes from './InterviewNotes';
import { AuthProvider } from '../../context/AuthContext';
import { MemoryRouter } from 'react-router-dom';
import axios from 'axios';

jest.mock('axios');

const mockNotes = [
  {
    id: 1,
    notes: 'Great communication skills',
    interview: { id: 1, candidate: { name: 'John Doe' } },
    createdAt: '2024-06-22T10:00:00Z',
  },
];

(axios.get as jest.Mock).mockResolvedValue({ data: mockNotes });

test('renders InterviewNotes page with correct heading and empty state', async () => {
  render(
    <AuthProvider>
      <MemoryRouter>
        <InterviewNotes />
      </MemoryRouter>
    </AuthProvider>
  );
  // Check for the actual heading text
  expect(await screen.findByRole('heading', { name: /round up - select interview/i })).toBeInTheDocument();
  // Check for the empty state message
  expect(await screen.findByText(/no interviews assigned to you/i)).toBeInTheDocument();
}); 