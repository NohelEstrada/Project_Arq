// Jest setup file for Vue.js testing
// Global test setup and configuration

// Mock global objects that might be used in components
global.console = {
  ...console,
  // Suppress console.log in tests unless needed
  log: jest.fn(),
  warn: jest.fn(),
  error: jest.fn()
};

// Mock localStorage if needed
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};
global.localStorage = localStorageMock;

// Mock window.location if needed
delete window.location;
window.location = {
  href: 'http://localhost:8080',
  origin: 'http://localhost:8080',
  reload: jest.fn()
};
