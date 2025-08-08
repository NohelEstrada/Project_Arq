<template>
  <div class="p-4">
    <h1 class="text-xl font-bold mb-4">Prueba de CORS</h1>
    
    <div class="space-y-4">
      <div>
        <button 
          @click="testHospitalCors" 
          class="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          :disabled="loading.hospital"
        >
          <span v-if="loading.hospital" class="inline-block animate-spin rounded-full h-4 w-4 border-t-2 border-b-2 border-white mr-2"></span>
          Probar CORS con Hospital
        </button>
        
        <div v-if="results.hospital" class="mt-2 p-3 bg-gray-100 rounded">
          <pre>{{ JSON.stringify(results.hospital, null, 2) }}</pre>
        </div>
        
        <div v-if="errors.hospital" class="mt-2 p-3 bg-red-100 text-red-700 rounded">
          {{ errors.hospital }}
        </div>
      </div>
      
      <div>
        <button 
          @click="testInsuranceCors" 
          class="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
          :disabled="loading.insurance"
        >
          <span v-if="loading.insurance" class="inline-block animate-spin rounded-full h-4 w-4 border-t-2 border-b-2 border-white mr-2"></span>
          Probar CORS con Seguro
        </button>
        
        <div v-if="results.insurance" class="mt-2 p-3 bg-gray-100 rounded">
          <pre>{{ JSON.stringify(results.insurance, null, 2) }}</pre>
        </div>
        
        <div v-if="errors.insurance" class="mt-2 p-3 bg-red-100 text-red-700 rounded">
          {{ errors.insurance }}
        </div>
      </div>
      
      <div class="mt-4">
        <h2 class="text-lg font-semibold mb-2">Configuración Actual:</h2>
        <div class="p-3 bg-gray-100 rounded">
          <p><strong>HOSPITAL_API_URL:</strong> {{ hospitalApiUrl }}</p>
          <p><strong>INSURANCE_API_URL:</strong> {{ insuranceApiUrl }}</p>
          <p><strong>VITE_IP:</strong> {{ viteIp }}</p>
        </div>
      </div>
      
      <div class="mt-4">
        <h2 class="text-lg font-semibold mb-2">Problemas comunes:</h2>
        <ul class="list-disc pl-5 space-y-1">
          <li>Asegúrate que el servidor de Django tenga <code>corsheaders</code> instalado y configurado</li>
          <li>Verifica que ALLOWED_HOSTS incluya tu IP ({{ viteIp }})</li>
          <li>Comprueba que CORS_ALLOWED_ORIGINS incluya http://{{ viteIp }}:5173</li>
          <li>El servidor de Java debe tener headers CORS configurados en sus respuestas</li>
          <li>Asegúrate que todos los servidores estén en ejecución</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { testCorsHospital, testCorsInsurance } from '../utils/api-integration';

const loading = ref({
  hospital: false,
  insurance: false
});

const results = ref({
  hospital: null as any,
  insurance: null as any
});

const errors = ref({
  hospital: null as string | null,
  insurance: null as string | null
});

const hospitalApiUrl = import.meta.env.VITE_HOSPITAL_API_URL || 'No configurado';
const insuranceApiUrl = import.meta.env.VITE_INSURANCE_API_URL || 'No configurado';
const viteIp = import.meta.env.VITE_IP || 'No configurado';

const testHospitalCors = async () => {
  loading.value.hospital = true;
  errors.value.hospital = null;
  results.value.hospital = null;
  
  try {
    const result = await testCorsHospital();
    results.value.hospital = result;
  } catch (err: any) {
    errors.value.hospital = err.message || 'Error al probar CORS con Hospital';
  } finally {
    loading.value.hospital = false;
  }
};

const testInsuranceCors = async () => {
  loading.value.insurance = true;
  errors.value.insurance = null;
  results.value.insurance = null;
  
  try {
    const result = await testCorsInsurance();
    results.value.insurance = result;
  } catch (err: any) {
    errors.value.insurance = err.message || 'Error al probar CORS con Seguro';
  } finally {
    loading.value.insurance = false;
  }
};

onMounted(() => {
  console.log('CorsTest component mounted');
});
</script> 