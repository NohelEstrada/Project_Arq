// Import real code from the project
import { setActivePinia, createPinia } from 'pinia';
import { useUserStore } from '../src/stores/userStore';

describe('userStore', () => {
  beforeEach(() => {
    // Create a fresh pinia instance for each test
    setActivePinia(createPinia());
    // Clear localStorage before each test
    localStorage.clear();
  });

  test('should set user and save to localStorage', () => {
    const store = useUserStore();
    const testUser = { id: 1, name: 'John Doe', role: 'admin' };
    
    store.setUser(testUser);
    
    expect(store.user).toEqual(testUser);
    expect(localStorage.getItem('user')).toBeTruthy();
    expect(localStorage.getItem('session')).toBeTruthy();
  });

  test('should get user from state', () => {
    const store = useUserStore();
    const testUser = { id: 2, name: 'Jane Doe', role: 'user' };
    
    store.setUser(testUser);
    const retrievedUser = store.getUser();
    
    expect(retrievedUser).toEqual(testUser);
    expect(retrievedUser.id).toBe(2);
  });

  test('should check if user is admin', () => {
    const store = useUserStore();
    
    // Set admin user
    store.setUser({ id: 1, name: 'Admin User', role: 'admin' });
    expect(store.isAdmin()).toBe(true);
    
    // Set regular user
    store.setUser({ id: 2, name: 'Regular User', role: 'user' });
    expect(store.isAdmin()).toBe(false);
  });

  test('should logout and clear state', () => {
    const store = useUserStore();
    const testUser = { id: 1, name: 'Test User', role: 'admin' };
    
    store.setUser(testUser);
    expect(store.user).toEqual(testUser);
    
    store.logout();
    
    expect(store.user).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(localStorage.getItem('session')).toBeNull();
  });
});

