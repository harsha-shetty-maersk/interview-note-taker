import React from 'react';
import { render, screen } from '@testing-library/react';
import Settings from './Settings';
import { AuthProvider } from '../../context/AuthContext';
import { MemoryRouter } from 'react-router-dom';

test('renders Settings page', () => {
  render(
    <AuthProvider>
      <MemoryRouter>
        <Settings />
      </MemoryRouter>
    </AuthProvider>
  );
  expect(screen.getByRole('heading', { name: /settings/i })).toBeInTheDocument();
}); 