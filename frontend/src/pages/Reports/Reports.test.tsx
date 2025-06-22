import React from 'react';
import { render, screen } from '@testing-library/react';
import Reports from './Reports';
import { AuthProvider } from '../../context/AuthContext';
import { MemoryRouter } from 'react-router-dom';

test('renders Reports page heading', () => {
  render(
    <AuthProvider>
      <MemoryRouter>
        <Reports />
      </MemoryRouter>
    </AuthProvider>
  );
  expect(screen.getByRole('heading', { name: /reports/i })).toBeInTheDocument();
}); 