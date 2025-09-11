// Test API service functions
describe('Pharmacy API', () => {
  test('should build correct API endpoint URLs', () => {
    const buildApiUrl = (endpoint, port = 8082) => {
      const baseUrl = `http://localhost:${port}/api2`;
      return `${baseUrl}${endpoint.startsWith('/') ? endpoint : '/' + endpoint}`;
    };

    expect(buildApiUrl('/medicines')).toBe('http://localhost:8082/api2/medicines');
    expect(buildApiUrl('categories')).toBe('http://localhost:8082/api2/categories');
    expect(buildApiUrl('/orders', 8091)).toBe('http://localhost:8091/api2/orders');
  });

  test('should validate prescription data structure', () => {
    const validatePrescription = (prescription) => {
      return prescription && 
             prescription.id && 
             prescription.medicines && 
             Array.isArray(prescription.medicines) &&
             prescription.medicines.length > 0;
    };

    const validPrescription = {
      id: 'RX001',
      medicines: [
        { name: 'Acetaminofen', quantity: 2 },
        { name: 'Ibuprofeno', quantity: 1 }
      ]
    };

    const invalidPrescription = {
      id: 'RX002',
      medicines: []
    };

    expect(validatePrescription(validPrescription)).toBe(true);
    expect(validatePrescription(invalidPrescription)).toBe(false);
    expect(validatePrescription(null)).toBe(false);
  });
});
