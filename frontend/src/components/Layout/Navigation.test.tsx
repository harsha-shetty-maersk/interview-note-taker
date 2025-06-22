import React from 'react';
import { render, screen } from '@testing-library/react';
import Navigation from './Navigation';
import { AuthProvider } from '../../context/AuthContext';
import { MemoryRouter } from 'react-router-dom';

test('renders Navigation component', () => {
  render(
    <AuthProvider>
      <MemoryRouter>
        <Navigation />
      </MemoryRouter>
    </AuthProvider>
  );
  // Check for at least one nav link
  expect(screen.getAllByRole('link').length).toBeGreaterThan(0);
}); 