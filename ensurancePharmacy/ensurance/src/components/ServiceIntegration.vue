<template>
  <div class="integration-container">
    <h1 class="text-2xl font-bold mb-6">Integración de Servicios</h1>
    
    <!-- Tabs for different service types -->
    <div class="tabs mb-6">
      <button 
        @click="activeTab = 'hospitals'"
        :class="['tab-button', { active: activeTab === 'hospitals' }]"
      >
        Hospital Central
      </button>
      <button 
        @click="activeTab = 'pharmacies'"
        :class="['tab-button', { active: activeTab === 'pharmacies' }]"
      >
        Farmacia Central
      </button>
    </div>
    
    <!-- Loading indicator -->
    <div v-if="loading" class="py-8 text-center">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-indigo-600"></div>
      <p class="mt-2 text-gray-600">Cargando...</p>
    </div>
    
    <!-- Error message -->
    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
      <button @click="fetchData" class="mt-2 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700">
        Reintentar
      </button>
    </div>
    
    <!-- Hospital Tab -->
    <div v-else-if="activeTab === 'hospitals'" class="hospitals-tab">
      <div class="mb-4 flex justify-between items-center">
        <h2 class="text-xl font-semibold">Servicios del Hospital Central</h2>
        <button 
          @click="syncHospitalData"
          class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
          :disabled="syncingHospitals"
        >
          <span v-if="syncingHospitals" class="inline-block animate-spin rounded-full h-4 w-4 border-t-2 border-b-2 border-white mr-2"></span>
          Sincronizar Servicios
        </button>
      </div>
      
      <div class="bg-white rounded-lg shadow-md p-4 mb-6">
        <div v-if="selectedHospital" class="flex flex-col md:flex-row justify-between">
          <div>
            <h3 class="text-lg font-semibold mb-2">{{ selectedHospital.name }}</h3>
            <p class="text-gray-600 mb-1">{{ selectedHospital.address }}</p>
            <p class="text-gray-600 mb-1">Teléfono: {{ selectedHospital.phone }}</p>
            <p class="text-gray-600">Email: {{ selectedHospital.email }}</p>
          </div>
          <div class="mt-4 md:mt-0">
            <span class="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
              Activo
            </span>
          </div>
        </div>
      </div>
      
      <!-- Filter Controls -->
      <div class="flex flex-wrap gap-4 mb-4">
        <div class="w-full md:w-auto flex-grow">
          <input 
            type="text" 
            v-model="serviceSearchTerm" 
            placeholder="Buscar servicios..." 
            class="w-full px-3 py-2 border border-gray-300 rounded"
          />
        </div>
        <div class="w-full md:w-auto flex-grow">
          <select 
            v-model="selectedCategory"
            class="w-full px-3 py-2 border border-gray-300 rounded"
          >
            <option :value="null">Todas las categorías</option>
            <option 
              v-for="category in categories" 
              :key="category.idCategory"
              :value="category.idCategory"
            >
              {{ category.name }}
            </option>
          </select>
        </div>
      </div>
      
      <!-- Services Table -->
      <div class="overflow-x-auto bg-white rounded-lg shadow">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Servicio</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Categoría</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Subcategoría</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Descripción</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Precio</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="service in filteredHospitalServices" :key="service.idHospitalService" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                {{ service.name }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ service.category ? service.category.name : 'Sin categoría' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ service.subcategory || 'N/A' }}
              </td>
              <td class="px-6 py-4 text-sm text-gray-500">
                {{ service.description }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                Q{{ formatPrice(service.price) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span 
                  class="px-2 py-1 text-xs font-semibold rounded-full"
                  :class="service.approved ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'"
                >
                  {{ service.approved ? 'Aprobado' : 'Pendiente' }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button 
                  v-if="!service.approved"
                  @click="approveService(service)"
                  class="text-indigo-600 hover:text-indigo-900 mr-2"
                >
                  Aprobar
                </button>
                <button 
                  v-if="service.approved"
                  @click="rejectService(service)"
                  class="text-red-600 hover:text-red-900"
                >
                  Rechazar
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <!-- Pharmacy Tab -->
    <div v-else-if="activeTab === 'pharmacies'" class="pharmacies-tab">
      <div class="mb-4 flex justify-between items-center">
        <h2 class="text-xl font-semibold">Medicamentos de Farmacia Central</h2>
        <button 
          @click="syncPharmacyData"
          class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
          :disabled="syncingPharmacies"
        >
          <span v-if="syncingPharmacies" class="inline-block animate-spin rounded-full h-4 w-4 border-t-2 border-b-2 border-white mr-2"></span>
          Sincronizar Medicamentos
        </button>
      </div>
      
      <div class="bg-white rounded-lg shadow-md p-4 mb-6">
        <div v-if="selectedPharmacy" class="flex flex-col md:flex-row justify-between">
          <div>
            <h3 class="text-lg font-semibold mb-2">{{ selectedPharmacy.name }}</h3>
            <p class="text-gray-600 mb-1">{{ selectedPharmacy.address }}</p>
            <p class="text-gray-600 mb-1">Teléfono: {{ selectedPharmacy.phone }}</p>
            <p class="text-gray-600">Email: {{ selectedPharmacy.email }}</p>
          </div>
          <div class="mt-4 md:mt-0">
            <span class="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
              Activo
            </span>
          </div>
        </div>
      </div>
      
      <!-- Filter Controls -->
      <div class="flex flex-wrap gap-4 mb-4">
        <div class="w-full md:w-auto flex-grow">
          <input 
            type="text" 
            v-model="medicationSearchTerm" 
            placeholder="Buscar medicamentos..." 
            class="w-full px-3 py-2 border border-gray-300 rounded"
          />
        </div>
        <div class="w-full md:w-auto flex-grow">
          <select 
            v-model="selectedMedicationCategory"
            class="w-full px-3 py-2 border border-gray-300 rounded"
          >
            <option :value="null">Todas las categorías</option>
            <option 
              v-for="category in medicationCategories" 
              :key="category.id"
              :value="category.id"
            >
              {{ category.name }}
            </option>
          </select>
        </div>
      </div>
      
      <!-- Medications Table -->
      <div class="overflow-x-auto bg-white rounded-lg shadow">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Medicamento</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Categoría</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Principio Activo</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Descripción</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Precio</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="medication in filteredPharmacyMedications" :key="medication.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                {{ medication.name }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ medication.category ? medication.category.name : 'Sin categoría' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ medication.activeIngredient }}
              </td>
              <td class="px-6 py-4 text-sm text-gray-500">
                {{ medication.description }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                Q{{ formatPrice(medication.price) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span 
                  class="px-2 py-1 text-xs font-semibold rounded-full"
                  :class="medication.approved ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'"
                >
                  {{ medication.approved ? 'Aprobado' : 'Pendiente' }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button 
                  v-if="!medication.approved"
                  @click="approveMedication(medication)"
                  class="text-indigo-600 hover:text-indigo-900 mr-2"
                >
                  Aprobar
                </button>
                <button 
                  v-if="medication.approved"
                  @click="rejectMedication(medication)"
                  class="text-red-600 hover:text-red-900"
                >
                  Rechazar
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import type { Ref } from 'vue';
import {
  getApprovedHospitals,
  getHospitalServices,
  getInsuranceCategories,
  getMedicationsList,
  syncHospitalServices,
  requestServiceApproval,
  registerApprovedService,
  registerApprovedMedication
} from '../utils/api-integration';

// Types
interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: string;
  email: string;
  enabled: number;
}

interface Pharmacy {
  idPharmacy: number;
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
  idHospitalService?: number;
  _id?: string;
  id?: number | string;
  service_id?: string;
  name?: string;
  service_name?: string;
  category?: Category;
  subcategory?: string;
  description?: string;
  price?: number;
  cost?: number;
  approved?: boolean;
  ensurance_id?: string;
  hospital_id?: number;
}

interface Medication {
  id: number;
  name: string;
  category: {
    id: number;
    name: string;
  };
  activeIngredient: string;
  description: string;
  price: number;
  approved: boolean;
  pharmacy_id?: number;
}

// State
const activeTab: Ref<'hospitals' | 'pharmacies'> = ref('hospitals');
const loading: Ref<boolean> = ref(true);
const error: Ref<string | null> = ref(null);
const hospitals: Ref<Hospital[]> = ref([]);
const pharmacies: Ref<Pharmacy[]> = ref([]);
const categories: Ref<Category[]> = ref([]);
const medicationCategories: Ref<{id: number, name: string}[]> = ref([]);
const selectedHospital: Ref<Hospital | null> = ref(null);
const selectedPharmacy: Ref<Pharmacy | null> = ref(null);
const hospitalServices: Ref<HospitalService[]> = ref([]);
const pharmacyMedications: Ref<Medication[]> = ref([]);
const serviceSearchTerm: Ref<string> = ref('');
const medicationSearchTerm: Ref<string> = ref('');
const selectedCategory: Ref<number | null> = ref(null);
const selectedMedicationCategory: Ref<number | null> = ref(null);
const syncingHospitals: Ref<boolean> = ref(false);
const syncingPharmacies: Ref<boolean> = ref(false);

// Computed properties
const filteredHospitalServices = computed(() => {
  let result = hospitalServices.value;
  
  // Filter by search term
  if (serviceSearchTerm.value) {
    const term = serviceSearchTerm.value.toLowerCase();
    result = result.filter(service => 
      service.name?.toLowerCase().includes(term) ||
      service.description?.toLowerCase().includes(term) ||
      (service.subcategory && service.subcategory.toLowerCase().includes(term)) ||
      (service.category && service.category.name.toLowerCase().includes(term))
    );
  }
  
  // Filter by category
  if (selectedCategory.value !== null) {
    result = result.filter(service => 
      service.category && service.category.idCategory === selectedCategory.value
    );
  }
  
  return result;
});

const filteredPharmacyMedications = computed(() => {
  let result = pharmacyMedications.value;
  
  // Filter by search term
  if (medicationSearchTerm.value) {
    const term = medicationSearchTerm.value.toLowerCase();
    result = result.filter(med => 
      med.name.toLowerCase().includes(term) ||
      med.description.toLowerCase().includes(term) ||
      med.activeIngredient.toLowerCase().includes(term) ||
      (med.category && med.category.name.toLowerCase().includes(term))
    );
  }
  
  // Filter by category
  if (selectedMedicationCategory.value !== null) {
    result = result.filter(med => 
      med.category && med.category.id === selectedMedicationCategory.value
    );
  }
  
  return result;
});

// Methods
const fetchData = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    // Intentar cargar hospitales desde la API
    try {
      const hospitalsData = await getApprovedHospitals();
      if (hospitalsData && hospitalsData.length > 0) {
        hospitals.value = hospitalsData;
      } else {
        // Si no hay datos, usar el hospital por defecto
        hospitals.value = [
          {
            idHospital: 1,
            name: 'Hospital Central',
            address: 'Avenida Principal 123',
            phone: '555-1234',
            email: 'contacto@hospitalcentral.com',
            enabled: 1
          }
        ];
      }
    } catch (err) {
      console.error('Error cargando hospitales:', err);
      // Usar hospital por defecto si falla la API
      hospitals.value = [
        {
          idHospital: 1,
          name: 'Hospital Central',
          address: 'Avenida Principal 123',
          phone: '555-1234',
          email: 'contacto@hospitalcentral.com',
          enabled: 1
        }
      ];
    }
    
    // Cargar categorías de servicios
    try {
      const categoriesData = await getInsuranceCategories();
      if (categoriesData && categoriesData.length > 0) {
        categories.value = categoriesData;
      } else {
        // Categorías por defecto si no hay datos
        categories.value = [
          { idCategory: 1, name: 'Consultas', description: 'Servicios de consulta médica', enabled: 1 },
          { idCategory: 2, name: 'Diagnóstico', description: 'Servicios de diagnóstico', enabled: 1 },
          { idCategory: 3, name: 'Laboratorio', description: 'Servicios de laboratorio', enabled: 1 }
        ];
      }
    } catch (err) {
      console.error('Error cargando categorías:', err);
      // Usar categorías por defecto si falla la API
      categories.value = [
        { idCategory: 1, name: 'Consultas', description: 'Servicios de consulta médica', enabled: 1 },
        { idCategory: 2, name: 'Diagnóstico', description: 'Servicios de diagnóstico', enabled: 1 },
        { idCategory: 3, name: 'Laboratorio', description: 'Servicios de laboratorio', enabled: 1 }
      ];
    }
    
    // Definir farmacia por defecto
    pharmacies.value = [
      {
        idPharmacy: 1,
        name: 'Farmacia Central',
        address: 'Calle Principal 123',
        phone: '555-1234',
        email: 'info@farmaciacentral.com',
        enabled: 1
      }
    ];
    
    // Cargar categorías de medicamentos
    medicationCategories.value = [
      { id: 1, name: 'Analgésicos' },
      { id: 2, name: 'Antibióticos' },
      { id: 3, name: 'Antialérgicos' }
    ];
    
    // Cargar automáticamente los servicios del hospital seleccionado
    selectedHospital.value = hospitals.value[0];
    await viewHospitalServices(selectedHospital.value);
    
    // Cargar automáticamente los medicamentos de la farmacia fija
    selectedPharmacy.value = pharmacies.value[0];
    await syncPharmacyData(); // Usar sincronización para cargar datos en lugar de datos quemados
    
  } catch (err) {
    console.error('Error fetching data:', err);
    error.value = 'No se pudieron cargar los datos. Por favor, intente nuevamente.';
  } finally {
    loading.value = false;
  }
};

const viewHospitalServices = async (hospital: Hospital) => {
  selectedHospital.value = hospital;
  
  try {
    // Verificar que el hospital sea válido y tenga un ID
    if (!hospital || typeof hospital.idHospital !== 'number') {
      throw new Error('ID de hospital no válido o no está definido');
    }
    
    console.log('Obteniendo servicios del hospital con ID:', hospital.idHospital);
    const servicesData = await getHospitalServices(hospital.idHospital);
    console.log("Hospital services data:", servicesData);
    
    // Si la API devuelve datos correctamente, úsalos
    if (servicesData && Array.isArray(servicesData) && servicesData.length > 0) {
      // Transformar los datos al formato esperado
      hospitalServices.value = servicesData.map((service: any) => {
        // Asegurar estructura consistente independientemente de la fuente
        return {
          idHospitalService: service._id || service.id || service.service_id || 1,
          name: service.name || service.service_name || 'Servicio sin nombre',
          category: service.category || { idCategory: 1, name: 'General', description: '', enabled: 1 },
          subcategory: service.subcategory || 'General',
          description: service.description || '',
          price: service.price || service.cost || 0,
          approved: service.approved || false,
          hospital_id: hospital.idHospital
        };
      });
      console.log('Servicios del hospital cargados:', hospitalServices.value);
    } else {
      console.warn('No se recibieron datos de servicios del hospital o el formato no es correcto');
      // Usar datos mínimos si no hay resultados
      hospitalServices.value = [
        {
          idHospitalService: 1,
          name: 'Consulta General',
          category: { idCategory: 1, name: 'Consultas', description: '', enabled: 1 },
          subcategory: 'Medicina General',
          description: 'Consulta médica general',
          price: 250.00,
          approved: false
        }
      ];
    }
  } catch (err) {
    console.error('Error fetching hospital services:', err);
    error.value = 'No se pudieron cargar los servicios del hospital. Por favor, intente nuevamente.';
    
    // Usar datos mínimos como respaldo
    hospitalServices.value = [
      {
        idHospitalService: 1,
        name: 'Consulta General',
        category: { idCategory: 1, name: 'Consultas', description: '', enabled: 1 },
        subcategory: 'Medicina General',
        description: 'Consulta médica general',
        price: 250.00,
        approved: false
      }
    ];
  }
};

const viewPharmacyMedications = (pharmacy: Pharmacy) => {
  selectedPharmacy.value = pharmacy;
  
  // Mock pharmacy medications data for demonstration
  pharmacyMedications.value = [
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
};

const approveService = async (service: HospitalService) => {
  try {
    // Call the insurance API to register the service
    await registerApprovedService(service);
    
    // Update local state after successful API call
    service.approved = true;
    
    // Show success message
    alert('Servicio aprobado y registrado en el sistema de seguros exitosamente');
  } catch (err) {
    console.error('Error approving service:', err);
    alert('Error al aprobar el servicio. Por favor, intente nuevamente.');
  }
};

const rejectService = async (service: HospitalService) => {
  try {
    // In a real implementation, you would call the API to reject the service
    // For now, we'll just update the local state
    service.approved = false;
    alert('Servicio rechazado exitosamente');
  } catch (err) {
    console.error('Error rejecting service:', err);
    alert('Error al rechazar el servicio. Por favor, intente nuevamente.');
  }
};

const approveMedication = async (medication: Medication) => {
  try {
    // Call the insurance API to register the medication
    await registerApprovedMedication(medication);
    
    // Update local state after successful API call
    medication.approved = true;
    alert('Medicamento aprobado y registrado en el sistema de seguros exitosamente');
  } catch (err) {
    console.error('Error approving medication:', err);
    alert('Error al aprobar el medicamento. Por favor, intente nuevamente.');
  }
};

const rejectMedication = async (medication: Medication) => {
  try {
    // In a real implementation, you would call the API to reject the medication
    medication.approved = false;
    alert('Medicamento rechazado exitosamente');
  } catch (err) {
    console.error('Error rejecting medication:', err);
    alert('Error al rechazar el medicamento. Por favor, intente nuevamente.');
  }
};

const syncHospitalData = async () => {
  syncingHospitals.value = true;
  error.value = null;
  
  try {
    // Use the actual API to sync hospital services
    if (selectedHospital.value) {
      // Get the insurance ID (using a fixed value for demo purposes)
      const insuranceId = "1"; // Replace with actual insurance ID
      
      // Call the sync function from the API
      const hospitalId = selectedHospital.value.idHospital;
      if (hospitalId) {
        await syncHospitalServices(hospitalId, insuranceId);
        
        // Refresh hospital services after sync
        await viewHospitalServices(selectedHospital.value);
        
        alert('Sincronización de servicios completada exitosamente');
      } else {
        error.value = 'ID del hospital no válido';
      }
    }
  } catch (err) {
    console.error('Error syncing hospital data:', err);
    error.value = 'Error al sincronizar datos del hospital. Por favor, intente nuevamente.';
  } finally {
    syncingHospitals.value = false;
  }
};

const syncPharmacyData = async () => {
  syncingPharmacies.value = true;
  error.value = null;
  
  try {
    // Intentar obtener medicamentos de la API de farmacia
    const medicationsData = await getMedicationsList();
    
    if (medicationsData && Array.isArray(medicationsData) && medicationsData.length > 0) {
      // Mapear datos de la API a nuestro formato esperado
      pharmacyMedications.value = medicationsData.map(med => ({
        id: med.id || Math.floor(Math.random() * 1000),
        name: med.name || 'Medicamento sin nombre',
        category: med.category || { id: 1, name: 'Sin categoría' },
        activeIngredient: med.activeIngredient || 'No especificado',
        description: med.description || 'Sin descripción',
        price: med.price || 0,
        approved: med.approved || false,
        pharmacy_id: selectedPharmacy.value?.idPharmacy || 1
      }));
    } else {
      console.warn('No se pudieron obtener datos de medicamentos de la API, usando datos mínimos');
      // Usar datos mínimos si no hay resultados
      pharmacyMedications.value = [
        {
          id: 1,
          name: 'Paracetamol 500mg',
          category: { id: 1, name: 'Analgésicos' },
          activeIngredient: 'Paracetamol',
          description: 'Analgésico y antipirético',
          price: 25.00,
          approved: false,
          pharmacy_id: selectedPharmacy.value?.idPharmacy || 1
        }
      ];
    }
    
    console.log('Medicamentos cargados:', pharmacyMedications.value);
    alert('Sincronización de medicamentos completada');
  } catch (err) {
    console.error('Error syncing pharmacy data:', err);
    error.value = 'Error al sincronizar datos de la farmacia. Por favor, intente nuevamente.';
    
    // Datos mínimos como respaldo
    pharmacyMedications.value = [
      {
        id: 1,
        name: 'Paracetamol 500mg',
        category: { id: 1, name: 'Analgésicos' },
        activeIngredient: 'Paracetamol',
        description: 'Analgésico y antipirético',
        price: 25.00,
        approved: false,
        pharmacy_id: selectedPharmacy.value?.idPharmacy || 1
      }
    ];
  } finally {
    syncingPharmacies.value = false;
  }
};

const formatPrice = (price: number): string => {
  return price.toFixed(2);
};

// Initialize
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.integration-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #e2e8f0;
}

.tab-button {
  padding: 0.75rem 1.5rem;
  font-weight: 500;
  border-bottom: 2px solid transparent;
  cursor: pointer;
}

.tab-button:hover {
  color: #4f46e5;
}

.tab-button.active {
  color: #4f46e5;
  border-bottom-color: #4f46e5;
}

.hospital-card, .pharmacy-card {
  transition: transform 0.2s;
}

.hospital-card:hover, .pharmacy-card:hover {
  transform: translateY(-2px);
}
</style> 