import React from 'react';
import { render, screen, act } from '@testing-library/react';
import Dashboard from './Dashboard';
import { AuthProvider } from '../../context/AuthContext';
import { MemoryRouter } from 'react-router-dom';
import axios from 'axios';

jest.mock('axios');

const mockStats = {
  totalCandidates: 10,
  totalInterviews: 5,
  completedInterviews: 3,
  pendingInterviews: 2,
};

(axios.get as jest.Mock).mockResolvedValue({ data: mockStats });

test('renders Dashboard page with mock stats', async () => {
  await act(async () => {
    render(
      <AuthProvider>
        <MemoryRouter>
          <Dashboard />
        </MemoryRouter>
      </AuthProvider>
    );
  });
  expect(await screen.findByRole('heading', { name: /dashboard/i })).toBeInTheDocument();
  const statLabel = await screen.findByText(/scheduled interviews/i);
  // Check that the next sibling contains the value '0'
  expect(statLabel.nextSibling).toHaveTextContent('0');
}); 