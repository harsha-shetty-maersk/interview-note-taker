import React from 'react';
import { render, screen } from '@testing-library/react';
import Candidates from './Candidates';
import { AuthProvider } from '../../context/AuthContext';
import { MemoryRouter } from 'react-router-dom';

test('renders Candidates page', () => {
  render(
    <AuthProvider>
      <MemoryRouter>
        <Candidates />
      </MemoryRouter>
    </AuthProvider>
  );
  // Check for the main heading
  expect(screen.getByRole('heading', { name: /candidates/i })).toBeInTheDocument();
}); 