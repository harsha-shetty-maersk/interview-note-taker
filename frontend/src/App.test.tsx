import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';
import { AuthProvider } from './context/AuthContext';

test('renders main app without crashing', () => {
  // Mock canvas getContext for jsPDF/html2canvas
  Object.defineProperty(window.HTMLCanvasElement.prototype, 'getContext', {
    value: () => {},
  });
  render(
    <AuthProvider>
      <App />
    </AuthProvider>
  );
  // Check for a heading or login form
  expect(screen.getByText(/sign in to your account/i)).toBeInTheDocument();
}); 