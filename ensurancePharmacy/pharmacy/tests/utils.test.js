// Import real code from the project
import { authService } from '../src/services/authService';

describe('authService', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();
  });

  test('should logout and clear localStorage', () => {
    // Set some user data
    localStorage.setItem('user', JSON.stringify({ id: 1, name: 'Test User' }));
    
    // Call logout
    authService.logout();
    
    // Verify localStorage is cleared
    expect(localStorage.getItem('user')).toBeNull();
  });

  test('should get current user from localStorage', () => {
    const testUser = { id: 1, name: 'Test User', role: 'admin' };
    localStorage.setItem('user', JSON.stringify(testUser));
    
    const user = authService.getCurrentUser();
    
    expect(user).toEqual(testUser);
    expect(user.id).toBe(1);
    expect(user.role).toBe('admin');
  });

  test('should return null when no user is stored', () => {
    const user = authService.getCurrentUser();
    expect(user).toBeNull();
  });
});
