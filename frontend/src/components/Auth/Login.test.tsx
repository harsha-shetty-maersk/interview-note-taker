import React from 'react';
import { render, screen } from '@testing-library/react';
import Login from './Login';
import { AuthProvider } from '../../context/AuthContext';
import { MemoryRouter } from 'react-router-dom';

test('renders Login component', () => {
  render(
    <AuthProvider>
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    </AuthProvider>
  );
  expect(screen.getByText(/sign in to your account/i)).toBeInTheDocument();
}); 