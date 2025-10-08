// Import real code from the project
import ApiService from '../src/services/ApiService';

describe('ApiService', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();
  });

  test('should build pharmacy API URLs correctly', () => {
    const url = ApiService.getPharmacyApiUrl('medicines');
    expect(url).toContain('8081');
    expect(url).toContain('/api2/medicines');
    expect(url).toMatch(/http:\/\/.*:8081\/api2\/medicines/);
  });

  test('should build ensurance API URLs correctly', () => {
    const url = ApiService.getEnsuranceApiUrl('policies');
    expect(url).toContain('8080');
    expect(url).toContain('/api2/policies');
    expect(url).toMatch(/http:\/\/.*:8080\/api2\/policies/);
  });

  test('should handle endpoints with leading slash', () => {
    const url1 = ApiService.getPharmacyApiUrl('/categories');
    const url2 = ApiService.getPharmacyApiUrl('categories');
    expect(url1).toBe(url2);
  });

  test('should configure custom API ports', () => {
    ApiService.configureApiPorts({
      pharmacy: '9001',
      ensurance: '9002'
    });
    
    const pharmacyUrl = ApiService.getPharmacyApiUrl('test');
    const ensuranceUrl = ApiService.getEnsuranceApiUrl('test');
    
    expect(pharmacyUrl).toContain('9001');
    expect(ensuranceUrl).toContain('9002');
  });
});
