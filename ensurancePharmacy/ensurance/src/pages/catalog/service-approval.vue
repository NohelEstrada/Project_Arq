<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold mb-6">Solicitar Aprobación de Servicio</h1>
    
    <!-- Error message -->
    <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
    </div>
    
    <!-- Success message -->
    <div v-if="success" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-6">
      <div class="flex items-center">
        <svg class="h-6 w-6 text-green-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        <p class="font-semibold">Aprobación Exitosa</p>
      </div>
      <p class="mt-2">El servicio ha sido aprobado correctamente.</p>
      <div class="mt-4 bg-white p-4 rounded-lg shadow-sm">
        <h3 class="font-semibold text-lg mb-2">Detalles de la Aprobación</h3>
        <p><strong>Código de Aprobación:</strong> {{ approvalData.approvalCode }}</p>
        <p><strong>Servicio:</strong> {{ approvalData.serviceName }}</p>
        <p><strong>Costo Total:</strong> Q{{ formatPrice(approvalData.serviceCost) }}</p>
        <p><strong>Monto Cubierto:</strong> Q{{ formatPrice(approvalData.coveredAmount) }}</p>
        <p><strong>Monto del Paciente:</strong> Q{{ formatPrice(approvalData.patientAmount) }}</p>
        <p><strong>Fecha de Aprobación:</strong> {{ formatDate(approvalData.approvalDate) }}</p>
      </div>
      <div class="mt-4">
        <button 
          @click="resetForm"
          class="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
        >
          Nueva Solicitud
        </button>
      </div>
    </div>
    
    <!-- Form -->
    <div v-if="!success" class="bg-white shadow-md rounded-lg p-6">
      <form @submit.prevent="submitForm">
        <div class="mb-6">
          <h2 class="text-xl font-semibold mb-4">Información del Paciente</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Paciente</label>
              <select 
                v-model="form.userId"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                required
              >
                <option :value="null">Seleccione un paciente</option>
                <option 
                  v-for="patient in patients" 
                  :key="patient.id"
                  :value="patient.id"
                >
                  {{ patient.name }}
                </option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">No. de Póliza</label>
              <input 
                type="text" 
                v-model="form.policyNumber"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                required
              />
            </div>
          </div>
        </div>
        
        <div class="mb-6">
          <h2 class="text-xl font-semibold mb-4">Información del Hospital</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Hospital</label>
              <select 
                v-model="form.hospitalId"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                required
                @change="loadHospitalServices"
              >
                <option :value="null">Seleccione un hospital</option>
                <option 
                  v-for="hospital in hospitals" 
                  :key="hospital.idHospital"
                  :value="hospital.idHospital"
                >
                  {{ hospital.name }}
                </option>
              </select>
            </div>
          </div>
        </div>
        
        <div class="mb-6">
          <h2 class="text-xl font-semibold mb-4">Información del Servicio</h2>
          <div v-if="loading" class="py-4 text-center">
            <div class="inline-block animate-spin rounded-full h-6 w-6 border-t-2 border-b-2 border-indigo-600"></div>
            <p class="mt-2 text-gray-600">Cargando servicios...</p>
          </div>
          <div v-else-if="form.hospitalId && !services.length" class="py-4 text-center text-gray-500">
            No hay servicios disponibles para este hospital.
          </div>
          <div v-else-if="form.hospitalId" class="grid grid-cols-1 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Servicio</label>
              <select 
                v-model="form.serviceId"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                required
                @change="updateServiceDetails"
              >
                <option :value="null">Seleccione un servicio</option>
                <option 
                  v-for="service in services" 
                  :key="service.idHospitalService"
                  :value="service.idHospitalService"
                >
                  {{ service.name }} - {{ service.category.name }} - Q{{ formatPrice(service.price) }}
                </option>
              </select>
            </div>
            
            <div v-if="form.serviceId">
              <div class="border border-gray-200 rounded-md p-4 bg-gray-50">
                <h3 class="font-semibold mb-2">Detalles del Servicio</h3>
                <p><strong>Nombre:</strong> {{ selectedService?.name }}</p>
                <p><strong>Categoría:</strong> {{ selectedService?.category?.name }}</p>
                <p><strong>Descripción:</strong> {{ selectedService?.description }}</p>
                <p><strong>Precio:</strong> Q{{ formatPrice(selectedService?.price || 0) }}</p>
                
                <div class="mt-4 pt-4 border-t border-gray-200">
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label class="block text-sm font-medium text-gray-700 mb-1">Cobertura del Seguro (%)</label>
                      <input 
                        type="number" 
                        v-model="form.coveragePercentage"
                        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                        min="0"
                        max="100"
                        required
                        @input="calculateAmounts"
                      />
                    </div>
                  </div>
                  
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                    <div>
                      <label class="block text-sm font-medium text-gray-700 mb-1">Monto Cubierto</label>
                      <div class="w-full px-3 py-2 border border-gray-200 bg-gray-100 rounded-md">
                        Q{{ formatPrice(form.coveredAmount) }}
                      </div>
                    </div>
                    <div>
                      <label class="block text-sm font-medium text-gray-700 mb-1">Monto del Paciente</label>
                      <div class="w-full px-3 py-2 border border-gray-200 bg-gray-100 rounded-md">
                        Q{{ formatPrice(form.patientAmount) }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="flex justify-end mt-6">
          <button 
            type="button"
            @click="resetForm"
            class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 bg-white hover:bg-gray-50 mr-2"
          >
            Cancelar
          </button>
          <button 
            type="submit"
            class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
            :disabled="loading || submitting"
          >
            <span v-if="submitting" class="inline-block animate-spin rounded-full h-4 w-4 border-t-2 border-b-2 border-white mr-2"></span>
            Solicitar Aprobación
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { Ref } from 'vue';
import { getApprovedHospitals, getHospitalServices, requestServiceApproval } from '../../utils/api-integration';

// Types
interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: string;
  email: string;
  enabled: number;
}

interface Category {
  idCategory: number;
  name: string;
  description: string;
  enabled: number;
}

interface HospitalService {
  idHospitalService: number;
  name: string;
  category: Category;
  subcategory: string;
  description: string;
  price: number;
  approved: boolean;
}

interface Patient {
  id: number;
  name: string;
  email: string;
  policyNumber: string;
}

interface ApprovalData {
  approvalId: number;
  approvalCode: string;
  serviceName: string;
  serviceCost: number;
  coveredAmount: number;
  patientAmount: number;
  approvalDate: string;
}

// State
const hospitals: Ref<Hospital[]> = ref([]);
const services: Ref<HospitalService[]> = ref([]);
const patients: Ref<Patient[]> = ref([]);
const loading: Ref<boolean> = ref(false);
const submitting: Ref<boolean> = ref(false);
const error: Ref<string | null> = ref(null);
const success: Ref<boolean> = ref(false);
const approvalData: Ref<ApprovalData> = ref({} as ApprovalData);

// Form state
const form = ref({
  userId: null as number | null,
  policyNumber: '',
  hospitalId: null as number | null,
  serviceId: null as number | null,
  serviceName: '',
  serviceDescription: '',
  serviceCost: 0,
  coveragePercentage: 80, // Default coverage percentage
  coveredAmount: 0,
  patientAmount: 0
});

// Computed
const selectedService = computed(() => {
  if (!form.value.serviceId) return null;
  return services.value.find(s => s.idHospitalService === form.value.serviceId) || null;
});

// Methods
const fetchInitialData = async () => {
  try {
    const hospitalsData = await getApprovedHospitals();
    hospitals.value = hospitalsData;
    
    // Mock patients data for demonstration
    patients.value = [
      { id: 1, name: 'Juan Pérez', email: 'juan@example.com', policyNumber: 'POL-12345' },
      { id: 2, name: 'María García', email: 'maria@example.com', policyNumber: 'POL-23456' },
      { id: 3, name: 'Carlos López', email: 'carlos@example.com', policyNumber: 'POL-34567' }
    ];
  } catch (err) {
    console.error('Error fetching initial data:', err);
    error.value = 'No se pudieron cargar los datos iniciales. Por favor, intente nuevamente.';
  }
};

const loadHospitalServices = async () => {
  if (!form.value.hospitalId) {
    services.value = [];
    form.value.serviceId = null;
    return;
  }
  
  loading.value = true;
  error.value = null;
  
  try {
    const servicesData = await getHospitalServices(form.value.hospitalId);
    
    // Mock services data for demonstration
    services.value = [
      {
        idHospitalService: 1,
        name: 'Consulta General',
        category: { idCategory: 1, name: 'Consultas', description: '', enabled: 1 },
        subcategory: 'Medicina General',
        description: 'Consulta médica general',
        price: 250.00,
        approved: true
      },
      {
        idHospitalService: 2,
        name: 'Radiografía',
        category: { idCategory: 2, name: 'Diagnóstico', description: '', enabled: 1 },
        subcategory: 'Rayos X',
        description: 'Radiografía general',
        price: 350.00,
        approved: true
      },
      {
        idHospitalService: 3,
        name: 'Análisis de Sangre',
        category: { idCategory: 3, name: 'Laboratorio', description: '', enabled: 1 },
        subcategory: 'Hematología',
        description: 'Análisis completo de sangre',
        price: 200.00,
        approved: true
      }
    ];
  } catch (err) {
    console.error('Error loading hospital services:', err);
    error.value = 'No se pudieron cargar los servicios del hospital. Por favor, intente nuevamente.';
  } finally {
    loading.value = false;
  }
};

const updateServiceDetails = () => {
  if (!selectedService.value) {
    form.value.serviceName = '';
    form.value.serviceDescription = '';
    form.value.serviceCost = 0;
    form.value.coveredAmount = 0;
    form.value.patientAmount = 0;
    return;
  }
  
  form.value.serviceName = selectedService.value.name;
  form.value.serviceDescription = selectedService.value.description;
  form.value.serviceCost = selectedService.value.price;
  
  calculateAmounts();
};

const calculateAmounts = () => {
  const percentage = form.value.coveragePercentage / 100;
  form.value.coveredAmount = form.value.serviceCost * percentage;
  form.value.patientAmount = form.value.serviceCost - form.value.coveredAmount;
};

const submitForm = async () => {
  submitting.value = true;
  error.value = null;
  
  try {
    // Validate form
    if (!form.value.userId || !form.value.hospitalId || !form.value.serviceId) {
      error.value = 'Por favor, complete todos los campos requeridos.';
      return;
    }
    
    // Prepare approval request data
    const approvalRequest = {
      userId: form.value.userId,
      hospitalId: form.value.hospitalId,
      serviceId: String(form.value.serviceId),
      serviceName: form.value.serviceName,
      serviceDescription: form.value.serviceDescription,
      serviceCost: form.value.serviceCost
    };
    
    // In a real implementation, this would call the insurance API
    // For this example, we'll simulate a successful response
    // const response = await requestServiceApproval(approvalRequest);
    
    // Mock response
    const response = {
      success: true,
      approvalId: 12345,
      approvalCode: 'APR-' + Math.floor(100000 + Math.random() * 900000),
      serviceName: form.value.serviceName,
      serviceCost: form.value.serviceCost,
      coveredAmount: form.value.coveredAmount,
      patientAmount: form.value.patientAmount,
      approvalDate: new Date().toISOString()
    };
    
    if (response.success) {
      approvalData.value = response;
      success.value = true;
    } else {
      error.value = 'No se pudo procesar la solicitud de aprobación. Por favor, intente nuevamente.';
    }
  } catch (err) {
    console.error('Error submitting approval request:', err);
    error.value = 'Error al procesar la solicitud. Por favor, intente nuevamente.';
  } finally {
    submitting.value = false;
  }
};

const resetForm = () => {
  form.value = {
    userId: null,
    policyNumber: '',
    hospitalId: null,
    serviceId: null,
    serviceName: '',
    serviceDescription: '',
    serviceCost: 0,
    coveragePercentage: 80,
    coveredAmount: 0,
    patientAmount: 0
  };
  success.value = false;
  error.value = null;
};

const formatPrice = (price: number): string => {
  return price.toFixed(2);
};

const formatDate = (dateString: string): string => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

// Initialize
onMounted(() => {
  fetchInitialData();
});
</script> 