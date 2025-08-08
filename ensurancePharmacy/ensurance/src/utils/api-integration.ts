import axios from 'axios';

// Define interfaces for our data types
interface InsuranceService {
  idInsuranceService: number;
  name: string;
  description: string;
  price: number;
  category: Category;
  subcategory: string;
  enabled: number;
}

interface Category {
  idCategory: number;
  name: string;
  description: string;
  enabled: number;
}

interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: string;
  email: string;
  enabled: number;
  port: string;
}

interface HospitalService {
  service_id: string;
  ensurance_id: string;
  description: string;
  cost: number;
  _id: string;
}

interface ApprovalData {
  userId: number;
  hospitalId: number;
  serviceId: string;
  serviceName: string;
  serviceDescription: string;
  serviceCost: number;
}

// Environment variables with fallbacks
const INSURANCE_IP = import.meta.env.VITE_IP || 'localhost';
const HOSPITAL_IP = 'localhost'; // Forzar el uso de localhost para hospital
const PHARMACY_IP = import.meta.env.VITE_IP || 'localhost';

const INSURANCE_API_URL = `http://${INSURANCE_IP}:8080/api`;
const HOSPITAL_API_URL = `http://localhost:8000`; // Usar localhost explícitamente
const PHARMACY_API_URL = `http://${PHARMACY_IP}:8081/api`;

// Añadir interceptores Axios para depuración
axios.interceptors.request.use(request => {
  console.log('Starting Request', {
    url: request.url,
    method: request.method,
    data: request.data,
    headers: request.headers
  });
  return request;
});

axios.interceptors.response.use(
  response => {
    console.log('Response:', {
      status: response.status,
      data: response.data,
      headers: response.headers
    });
    return response;
  },
  error => {
    console.error('Response Error:', {
      message: error.message,
      config: error.config ? {
        url: error.config.url,
        method: error.config.method,
        data: error.config.data
      } : 'No config',
      response: error.response ? {
        status: error.response.status,
        data: error.response.data,
        headers: error.response.headers
      } : 'No response'
    });
    return Promise.reject(error);
  }
);

// Insurance system API endpoints
const getInsuranceServices = async (): Promise<InsuranceService[]> => {
  try {
    const response = await axios.get(`${INSURANCE_API_URL}/insurance-services`);
    return response.data;
  } catch (error) {
    console.error('Error fetching insurance services:', error);
    throw error;
  }
};

const getInsuranceCategories = async (): Promise<Category[]> => {
  try {
    const response = await axios.get(`${INSURANCE_API_URL}/category`);
    return response.data;
  } catch (error) {
    console.error('Error fetching insurance categories:', error);
    throw error;
  }
};

const getApprovedHospitals = async (): Promise<Hospital[]> => {
  try {
    const response = await axios.get(`http://${INSURANCE_IP}:8080/api/hospital`);
    return response.data;
  } catch (error) {
    console.error('Error fetching approved hospitals:', error);
    throw error;
  }
};

// Hospital system API endpoints
const getHospitalServices = async (hospitalId?: number, hospitalPort?: string): Promise<HospitalService[]> => {
  try {
    console.log(`Fetching services for hospital ID: ${hospitalId || 'default'}`);
    
    if (!hospitalId) {
      console.error('No hospital ID provided');
      return [];
    }

    // Verificar si hay un puerto en los parámetros, si no, intentar obtener del hospital predeterminado
    let port = hospitalPort;
    if (!port) {
      const defaultHospital = getDefaultHospital();
      if (defaultHospital && defaultHospital.idHospital === hospitalId && defaultHospital.port) {
        port = defaultHospital.port;
        console.log(`Using default hospital port: ${port}`);
      }
    }

    // Configurar headers
    const headers: Record<string, string> = {};
    if (port) {
      headers['X-Hospital-Port'] = port;
    }

    // Usar el nuevo proxy que maneja el puerto automáticamente
    const response = await axios.get(
      `http://${INSURANCE_IP}:8080/api/hospital-proxy/${hospitalId}/api/services/`,
      { headers }
    );
    
    console.log('Hospital services response:', response.data);
    if (response.data) {
      return Array.isArray(response.data) ? response.data : [response.data];
    }
    
    // Si la respuesta es vacía, intentar con otra ruta
    try {
      const alternativeResponse = await axios.get(
        `http://${INSURANCE_IP}:8080/api/hospital-proxy/${hospitalId}/services/`,
        { headers }
      );
      console.log('Alternative hospital services response:', alternativeResponse.data);
      if (alternativeResponse.data) {
        return Array.isArray(alternativeResponse.data) ? alternativeResponse.data : [alternativeResponse.data];
      }
    } catch (altErr) {
      console.log('Alternative proxy endpoint failed:', altErr);
    }
    
    // Si todas las consultas fallan, devolver un array vacío
    console.warn('No se pudo obtener servicios del hospital, devolviendo array vacío');
    return [];
  } catch (error) {
    console.error('Error fetching hospital services:', error);
    return [];
  }
};

// Función para obtener el hospital predeterminado del localStorage
const getDefaultHospital = () => {
  try {
    const storedHospital = localStorage.getItem('defaultHospital');
    if (storedHospital) {
      return JSON.parse(storedHospital);
    }
    return null;
  } catch (error) {
    console.error('Error al obtener el hospital predeterminado:', error);
    return null;
  }
};

const getHospitalCategories = async (hospitalId?: number, hospitalPort?: string): Promise<Category[]> => {
  try {
    if (!hospitalId) {
      console.error('No hospital ID provided');
      return [];
    }

    // Verificar si hay un puerto en los parámetros, si no, intentar obtener del hospital predeterminado
    let port = hospitalPort;
    if (!port) {
      const defaultHospital = getDefaultHospital();
      if (defaultHospital && defaultHospital.idHospital === hospitalId && defaultHospital.port) {
        port = defaultHospital.port;
        console.log(`Using default hospital port: ${port}`);
      }
    }

    // Configurar headers
    const headers: Record<string, string> = {};
    if (port) {
      headers['X-Hospital-Port'] = port;
    }

    // Usar el nuevo proxy que maneja el puerto automáticamente
    const response = await axios.get(
      `http://${INSURANCE_IP}:8080/api/hospital-proxy/${hospitalId}/api/categories/`,
      { headers }
    );
    
    return response.data;
  } catch (error) {
    console.error('Error fetching hospital categories:', error);
    throw error;
  }
};

// Pharmacy system API endpoints
const getMedicationsList = async () => {
  try {
    console.log('Fetching medications from pharmacy system');
    
    // Try different endpoint formats for the pharmacy system
    try {
      const response = await axios.get(`${PHARMACY_API_URL}/medicines`);
      console.log('Medications response:', response.data);
      return response.data;
    } catch (mainError) {
      console.log('Main medicines endpoint failed, trying alternative:', mainError);
      
      // Try alternative endpoint formats
      try {
        const altResponse = await axios.get(`${PHARMACY_API_URL}/medications`);
        console.log('Alternative medications response:', altResponse.data);
        return altResponse.data;
      } catch (altError) {
        console.log('Alternative medicines endpoint failed:', altError);
      }
      
      try {
        const directResponse = await axios.get(`${PHARMACY_API_URL}/pharmacy/products`);
        console.log('Direct medications response:', directResponse.data);
        return directResponse.data;
      } catch (directError) {
        console.log('Direct products endpoint failed:', directError);
      }
    }
    
    // If all fails, return empty array
    console.warn('All medication endpoints failed, returning mock data');
    return [
      {
        id: 1,
        name: 'Paracetamol 500mg',
        category: { id: 1, name: 'Analgésicos' },
        activeIngredient: 'Paracetamol',
        description: 'Analgésico y antipirético',
        price: 25.00,
        approved: true
      },
      {
        id: 2,
        name: 'Amoxicilina 500mg',
        category: { id: 2, name: 'Antibióticos' },
        activeIngredient: 'Amoxicilina',
        description: 'Antibiótico de amplio espectro',
        price: 35.00,
        approved: true
      },
      {
        id: 3,
        name: 'Loratadina 10mg',
        category: { id: 3, name: 'Antialérgicos' },
        activeIngredient: 'Loratadina',
        description: 'Antihistamínico',
        price: 30.00,
        approved: false
      }
    ];
  } catch (error) {
    console.error('Error fetching medications list:', error);
    return [];
  }
};

const verifyMedicationCoverage = async (medicationId: string, insuranceId: string) => {
  try {
    const response = await axios.get(`${INSURANCE_API_URL}/medication-coverage`, {
      params: {
        medicationId,
        insuranceId
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error verifying medication coverage:', error);
    throw error;
  }
};

// Service approval functions
const requestServiceApproval = async (approvalData: ApprovalData) => {
  try {
    const response = await axios.post(`${INSURANCE_API_URL}/service-approvals/request`, approvalData);
    return response.data;
  } catch (error) {
    console.error('Error requesting service approval:', error);
    throw error;
  }
};

const checkApprovalStatus = async (approvalCode: string) => {
  try {
    const response = await axios.get(`${INSURANCE_API_URL}/service-approvals/check/${approvalCode}`);
    return response.data;
  } catch (error) {
    console.error('Error checking approval status:', error);
    throw error;
  }
};

// Integration utilities
const syncHospitalServices = async (hospitalId: number, insuranceId: string, hospitalPort?: string) => {
  try {
    // Verificar si hay un puerto en los parámetros, si no, intentar obtener del hospital predeterminado
    let port = hospitalPort;
    if (!port) {
      const defaultHospital = getDefaultHospital();
      if (defaultHospital && defaultHospital.idHospital === hospitalId && defaultHospital.port) {
        port = defaultHospital.port;
        console.log(`Using default hospital port: ${port}`);
      }
    }

    // Get services from hospital system
    const hospitalServices = await getHospitalServices(hospitalId, port);
    
    // Configurar headers
    const headers: Record<string, string> = {};
    if (port) {
      headers['X-Hospital-Port'] = port;
    }
    
    // Transform to insurance system format and import
    const transformedServices = hospitalServices.map((service: HospitalService) => ({
      service_id: service.service_id,
      ensurance_id: insuranceId,
      description: service.description,
      cost: service.cost
    }));
    
    // Send to insurance system using the proxy
    const response = await axios.post(
      `http://${INSURANCE_IP}:8080/api/hospital-proxy/${hospitalId}/api/services_ensurance/import/`,
      { services: transformedServices },
      { headers }
    );
    
    return response.data;
  } catch (error) {
    console.error('Error syncing hospital services:', error);
    throw error;
  }
};

// Función de prueba para verificar CORS
const testCorsHospital = async (hospitalId: number, hospitalPort?: string): Promise<any> => {
  try {
    // Verificar si hay un puerto en los parámetros, si no, intentar obtener del hospital predeterminado
    let port = hospitalPort;
    if (!port) {
      const defaultHospital = getDefaultHospital();
      if (defaultHospital && defaultHospital.idHospital === hospitalId && defaultHospital.port) {
        port = defaultHospital.port;
        console.log(`Using default hospital port: ${port}`);
      }
    }

    // Configurar headers
    const headers: Record<string, string> = {};
    if (port) {
      headers['X-Hospital-Port'] = port;
    }

    // Usar el nuevo proxy que maneja el puerto automáticamente
    const response = await axios.get(
      `http://${INSURANCE_IP}:8080/api/hospital-proxy/${hospitalId}/cors-test/`,
      { headers }
    );
    
    console.log('CORS test successful:', response.data);
    return response.data;
  } catch (error) {
    console.error('CORS test failed:', error);
    throw error;
  }
};

// Función de prueba para verificar CORS con la API de seguros
const testCorsInsurance = async (): Promise<any> => {
  try {
    // El backend de seguros está en Java, intentamos una URL que sabemos que existe
    const response = await axios.get(`http://${INSURANCE_IP}:8080/api/hospital`);
    console.log('Insurance API CORS test successful:', response.data);
    return response.data;
  } catch (error) {
    console.error('Insurance API CORS test failed:', error);
    throw error;
  }
};

// Register approved service with insurance system
const registerApprovedService = async (serviceData: any) => {
  try {
    // Obtener datos del servicio y asegurar que tenga los campos necesarios
    const transformedData = {
      name: serviceData.name || '',
      description: serviceData.description || '',
      price: serviceData.price || serviceData.cost || 0,
      enabled: 1,
      hospital_id: serviceData.hospital_id || 1,
      category_id: serviceData.category?.idCategory || 1
    };
    
    // Registrar en el sistema de seguros (endpoint correcto)
    console.log('Registrando servicio aprobado:', transformedData);
    const response = await axios.post(`${INSURANCE_API_URL}/insurance-services/register`, transformedData);
    return response.data;
  } catch (error) {
    console.error('Error registering approved service:', error);
    throw error;
  }
};

// Register approved medication with insurance system
const registerApprovedMedication = async (medicationData: any) => {
  try {
    // Transform medication data to insurance system format
    const transformedData = {
      name: medicationData.name || '',
      description: medicationData.description || '',
      active_ingredient: medicationData.activeIngredient || '',
      price: medicationData.price || 0,
      enabled: 1,
      pharmacy_id: medicationData.pharmacy_id || 1,
      category_id: medicationData.category?.id || 1
    };
    
    // Call insurance API to register medication (endpoint correcto)
    console.log('Registrando medicamento aprobado:', transformedData);
    const response = await axios.post(`${INSURANCE_API_URL}/pharmacy-medications/register`, transformedData);
    return response.data;
  } catch (error) {
    console.error('Error registering approved medication:', error);
    throw error;
  }
};

export {
  getInsuranceServices,
  getInsuranceCategories,
  getApprovedHospitals,
  getHospitalServices,
  getHospitalCategories,
  getMedicationsList,
  verifyMedicationCoverage,
  requestServiceApproval,
  checkApprovalStatus,
  syncHospitalServices,
  testCorsHospital,
  testCorsInsurance,
  registerApprovedService,
  registerApprovedMedication
}; 