<template>
  <div class="diagnostics-container p-6 bg-white rounded-lg shadow-lg">
    <h1 class="text-2xl font-bold mb-4">Diagnóstico de APIs</h1>
    
    <div class="mb-6">
      <p class="mb-2">Esta herramienta verifica las conexiones con los sistemas externos.</p>
      <button 
        @click="runAllTests" 
        :disabled="testing"
        class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 disabled:opacity-50"
      >
        {{ testing ? 'Probando...' : 'Probar todas las conexiones' }}
      </button>
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <!-- Insurance System -->
      <div class="border rounded-lg p-4">
        <h2 class="text-lg font-semibold mb-2">Sistema de Seguros</h2>
        <p class="text-gray-600 mb-2">Estado: 
          <span 
            :class="[
              'font-semibold',
              testResults.insurance === null ? 'text-gray-500' : 
              testResults.insurance ? 'text-green-500' : 'text-red-500'
            ]"
          >
            {{ 
              testResults.insurance === null ? 'No probado' : 
              testResults.insurance ? 'Conectado' : 'Error de conexión' 
            }}
          </span>
        </p>
        <p class="text-sm text-gray-500">URL: {{ insuranceUrl }}</p>
        <div v-if="testResults.insuranceDetails" class="mt-2 p-2 bg-gray-100 rounded text-xs overflow-auto max-h-24">
          {{ JSON.stringify(testResults.insuranceDetails, null, 2) }}
        </div>
        <div v-if="testResults.insuranceError" class="mt-2 p-2 bg-red-50 text-red-700 rounded text-xs overflow-auto max-h-24">
          {{ testResults.insuranceError }}
        </div>
        <button 
          @click="testInsuranceConnection" 
          :disabled="testing"
          class="mt-3 px-3 py-1 border border-indigo-500 text-indigo-500 rounded hover:bg-indigo-50 text-sm"
        >
          Probar
        </button>
      </div>
      
      <!-- Hospital System -->
      <div class="border rounded-lg p-4">
        <h2 class="text-lg font-semibold mb-2">Sistema Hospitalario</h2>
        <p class="text-gray-600 mb-2">Estado: 
          <span 
            :class="[
              'font-semibold',
              testResults.hospital === null ? 'text-gray-500' : 
              testResults.hospital ? 'text-green-500' : 'text-red-500'
            ]"
          >
            {{ 
              testResults.hospital === null ? 'No probado' : 
              testResults.hospital ? 'Conectado' : 'Error de conexión' 
            }}
          </span>
        </p>
        <p class="text-sm text-gray-500">URL: {{ hospitalUrl }}</p>
        <div v-if="testResults.hospitalDetails" class="mt-2 p-2 bg-gray-100 rounded text-xs overflow-auto max-h-24">
          {{ JSON.stringify(testResults.hospitalDetails, null, 2) }}
        </div>
        <div v-if="testResults.hospitalError" class="mt-2 p-2 bg-red-50 text-red-700 rounded text-xs overflow-auto max-h-24">
          {{ testResults.hospitalError }}
        </div>
        <button 
          @click="testHospitalConnection" 
          :disabled="testing"
          class="mt-3 px-3 py-1 border border-indigo-500 text-indigo-500 rounded hover:bg-indigo-50 text-sm"
        >
          Probar
        </button>
      </div>
      
      <!-- Pharmacy System -->
      <div class="border rounded-lg p-4">
        <h2 class="text-lg font-semibold mb-2">Sistema de Farmacia</h2>
        <p class="text-gray-600 mb-2">Estado: 
          <span 
            :class="[
              'font-semibold',
              testResults.pharmacy === null ? 'text-gray-500' : 
              testResults.pharmacy ? 'text-green-500' : 'text-red-500'
            ]"
          >
            {{ 
              testResults.pharmacy === null ? 'No probado' : 
              testResults.pharmacy ? 'Conectado' : 'Error de conexión' 
            }}
          </span>
        </p>
        <p class="text-sm text-gray-500">URL: {{ pharmacyUrl }}</p>
        <div v-if="testResults.pharmacyDetails" class="mt-2 p-2 bg-gray-100 rounded text-xs overflow-auto max-h-24">
          {{ JSON.stringify(testResults.pharmacyDetails, null, 2) }}
        </div>
        <div v-if="testResults.pharmacyError" class="mt-2 p-2 bg-red-50 text-red-700 rounded text-xs overflow-auto max-h-24">
          {{ testResults.pharmacyError }}
        </div>
        <button 
          @click="testPharmacyConnection" 
          :disabled="testing"
          class="mt-3 px-3 py-1 border border-indigo-500 text-indigo-500 rounded hover:bg-indigo-50 text-sm"
        >
          Probar
        </button>
      </div>
    </div>
    
    <div class="mt-6">
      <h2 class="text-lg font-semibold mb-3">Sugerencias de resolución de problemas:</h2>
      <ul class="list-disc pl-5 space-y-2">
        <li>Verifica que todas las APIs estén ejecutándose en sus respectivos puertos</li>
        <li>Confirma que las direcciones IP en el archivo .env sean correctas: 
          <code class="bg-gray-100 px-1 py-0.5 rounded">VITE_IP</code>, 
          <code class="bg-gray-100 px-1 py-0.5 rounded">VITE_HOSPITAL_API_URL</code>, 
          <code class="bg-gray-100 px-1 py-0.5 rounded">VITE_PHARMACY_API_URL</code>
        </li>
        <li>Revisa la configuración CORS en cada sistema para permitir solicitudes entre dominios</li>
        <li>Verifica los logs en cada sistema para buscar errores específicos</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { testCorsHospital, testCorsInsurance } from '../utils/api-integration';

// Constantes de configuración
const INSURANCE_IP = import.meta.env.VITE_IP || 'localhost';
const HOSPITAL_IP = import.meta.env.VITE_IP || 'localhost';
const PHARMACY_IP = import.meta.env.VITE_IP || 'localhost';

// URLs de las APIs
const INSURANCE_API_URL = `http://${INSURANCE_IP}:8080/api`;
const HOSPITAL_API_URL = `http://${HOSPITAL_IP}:5050`;
const PHARMACY_API_URL = `http://${PHARMACY_IP}:8080/api`;

// Readable URLs for display
const insuranceUrl = INSURANCE_API_URL;
const hospitalUrl = HOSPITAL_API_URL;
const pharmacyUrl = PHARMACY_API_URL;

// State
const testing = ref(false);
const testResults = ref({
  insurance: null as boolean | null,
  hospital: null as boolean | null,
  pharmacy: null as boolean | null,
  insuranceDetails: null as any,
  hospitalDetails: null as any,
  pharmacyDetails: null as any,
  insuranceError: null as string | null,
  hospitalError: null as string | null,
  pharmacyError: null as string | null
});

// Test Insurance connection
const testInsuranceConnection = async () => {
  testing.value = true;
  testResults.value.insurance = null;
  testResults.value.insuranceDetails = null;
  testResults.value.insuranceError = null;
  
  try {
    // Use the test function from the API integration
    const result = await testCorsInsurance();
    testResults.value.insurance = true;
    testResults.value.insuranceDetails = result;
  } catch (error) {
    console.error('Insurance connection test failed:', error);
    testResults.value.insurance = false;
    if (error instanceof Error) {
      testResults.value.insuranceError = error.message;
    } else {
      testResults.value.insuranceError = 'Error desconocido al conectar con el sistema de seguros';
    }
  } finally {
    testing.value = false;
  }
};

// Test Hospital connection
const testHospitalConnection = async () => {
  testing.value = true;
  testResults.value.hospital = null;
  testResults.value.hospitalDetails = null;
  testResults.value.hospitalError = null;
  
  try {
    // Use the test function from the API integration
    const result = await testCorsHospital();
    testResults.value.hospital = true;
    testResults.value.hospitalDetails = result;
  } catch (error) {
    console.error('Hospital connection test failed:', error);
    
    // Try a fallback endpoint
    try {
      const fallbackResponse = await axios.get(`${HOSPITAL_API_URL}/api/services/`);
      testResults.value.hospital = true;
      testResults.value.hospitalDetails = fallbackResponse.data;
    } catch (fallbackError) {
      testResults.value.hospital = false;
      if (error instanceof Error) {
        testResults.value.hospitalError = error.message;
      } else {
        testResults.value.hospitalError = 'Error desconocido al conectar con el sistema hospitalario';
      }
    }
  } finally {
    testing.value = false;
  }
};

// Test Pharmacy connection
const testPharmacyConnection = async () => {
  testing.value = true;
  testResults.value.pharmacy = null;
  testResults.value.pharmacyDetails = null;
  testResults.value.pharmacyError = null;
  
  try {
    const endpoints = [
      `${PHARMACY_API_URL}/medicines`,
      `${PHARMACY_API_URL}/medications`,
      `${PHARMACY_API_URL}/pharmacy/products`
    ];
    
    let connected = false;
    
    for (const endpoint of endpoints) {
      try {
        const response = await axios.get(endpoint);
        testResults.value.pharmacy = true;
        testResults.value.pharmacyDetails = response.data;
        connected = true;
        break;
      } catch (endpointError) {
        console.log(`Endpoint ${endpoint} test failed, trying next if available`);
      }
    }
    
    if (!connected) {
      throw new Error('Todos los endpoints de farmacia fallaron');
    }
  } catch (error) {
    console.error('Pharmacy connection test failed:', error);
    testResults.value.pharmacy = false;
    if (error instanceof Error) {
      testResults.value.pharmacyError = error.message;
    } else {
      testResults.value.pharmacyError = 'Error desconocido al conectar con el sistema de farmacia';
    }
  } finally {
    testing.value = false;
  }
};

// Run all tests sequentially
const runAllTests = async () => {
  testing.value = true;
  await testInsuranceConnection();
  await testHospitalConnection();
  await testPharmacyConnection();
  testing.value = false;
};

onMounted(() => {
  // Optionally run tests automatically when the component mounts
  // runAllTests();
});
</script>

<style scoped>
.diagnostics-container {
  max-width: 1200px;
  margin: 0 auto;
}
</style> 