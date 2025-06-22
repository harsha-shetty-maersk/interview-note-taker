import '@testing-library/jest-dom';

// Mock axios for all tests to avoid ESM import issues
jest.mock('axios', () => ({
  create: () => ({
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  }),
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn(),
}));

// Mock canvas getContext globally for jsPDF/html2canvas compatibility
Object.defineProperty(window.HTMLCanvasElement.prototype, 'getContext', {
  value: () => {},
}); 