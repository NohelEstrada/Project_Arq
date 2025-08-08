<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { configureApiPorts, loadPortConfiguration } from '../utils/api';

// Estado
const ensurancePort = ref('8080');
const pharmacyPort = ref('8081');
const showDialog = ref(true);
const savePreference = ref(true);
const ip = import.meta.env.VITE_IP || 'localhost';

// Inicializar con la configuración guardada (si existe)
onMounted(() => {
  // Cargar configuración guardada
  const savedConfig = localStorage.getItem("apiPortConfig");
  if (savedConfig) {
    try {
      const config = JSON.parse(savedConfig);
      if (config.ensurance) ensurancePort.value = config.ensurance;
      if (config.pharmacy) pharmacyPort.value = config.pharmacy;
    } catch (error) {
      console.warn("Error al cargar configuración de puertos:", error);
    }
  }
  
  // Cargar puertos en el sistema
  loadPortConfiguration();
});

// Guardar configuración y cerrar
const saveConfiguration = () => {
  // Validar puertos
  if (!isValidPort(ensurancePort.value) || !isValidPort(pharmacyPort.value)) {
    alert('Por favor ingrese puertos válidos (1024-65535)');
    return;
  }
  
  // Configurar puertos
  configureApiPorts({
    ensurance: ensurancePort.value,
    pharmacy: pharmacyPort.value
  });
  
  // Guardar preferencia para no mostrar de nuevo si está marcado
  if (savePreference.value) {
    localStorage.setItem("skipPortSelector", "true");
  } else {
    localStorage.removeItem("skipPortSelector");
  }
  
  showDialog.value = false;
};

// Validar que sea un puerto válido
function isValidPort(port: string): boolean {
  const num = parseInt(port);
  return !isNaN(num) && num >= 1024 && num <= 65535;
}
</script>

<template>
  <div v-if="showDialog" class="fixed inset-0 z-50 overflow-y-auto bg-gray-800 bg-opacity-75 flex items-center justify-center">
    <div class="bg-white rounded-lg max-w-md w-full p-6 shadow-xl">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl font-bold text-gray-800">Configuración de Puertos</h2>
      </div>
      
      <p class="text-gray-600 mb-4">
        Configure los puertos para conectarse a los servidores de backend.
      </p>
      
      <div class="space-y-4">
        <div>
          <label for="ensurancePort" class="block text-sm font-medium text-gray-700">Puerto de Ensurance:</label>
          <div class="mt-1 flex rounded-md shadow-sm">
            <span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500">
              http://{{ ip }}:
            </span>
            <input
              id="ensurancePort"
              v-model="ensurancePort"
              type="text"
              class="focus:ring-indigo-500 focus:border-indigo-500 flex-1 block w-full rounded-none rounded-r-md sm:text-sm border-gray-300 px-3 py-2 border"
              placeholder="8080"
            />
          </div>
          <p class="mt-1 text-sm text-gray-500">Puerto predeterminado: 8080</p>
        </div>
        
        <div>
          <label for="pharmacyPort" class="block text-sm font-medium text-gray-700">Puerto de Pharmacy:</label>
          <div class="mt-1 flex rounded-md shadow-sm">
            <span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500">
              http://{{ ip }}:
            </span>
            <input
              id="pharmacyPort"
              v-model="pharmacyPort"
              type="text"
              class="focus:ring-indigo-500 focus:border-indigo-500 flex-1 block w-full rounded-none rounded-r-md sm:text-sm border-gray-300 px-3 py-2 border"
              placeholder="8081"
            />
          </div>
          <p class="mt-1 text-sm text-gray-500">Puerto predeterminado: 8081</p>
        </div>
        
        <div class="flex items-center">
          <input
            id="savePreference"
            v-model="savePreference"
            type="checkbox"
            class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
          />
          <label for="savePreference" class="ml-2 block text-sm text-gray-700">
            Recordar mi elección (no mostrar de nuevo)
          </label>
        </div>
      </div>
      
      <div class="mt-5 sm:mt-6">
        <button
          @click="saveConfiguration"
          class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:text-sm"
        >
          Guardar configuración
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Estilos específicos del componente */
</style> 