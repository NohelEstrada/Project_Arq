// Test utilities and helper functions
describe('Pharmacy Utils', () => {
  test('should validate email format correctly', () => {
    const validateEmail = (email) => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(email);
    };

    expect(validateEmail('user@example.com')).toBe(true);
    expect(validateEmail('invalid-email')).toBe(false);
    expect(validateEmail('test@unis.edu.gt')).toBe(true);
    expect(validateEmail('')).toBe(false);
  });

  test('should format currency correctly', () => {
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('es-GT', {
        style: 'currency',
        currency: 'GTQ'
      }).format(amount);
    };

    expect(formatCurrency(100)).toContain('100');
    expect(formatCurrency(1500.50)).toContain('1,500.50');
    expect(formatCurrency(0)).toContain('0');
  });
});
