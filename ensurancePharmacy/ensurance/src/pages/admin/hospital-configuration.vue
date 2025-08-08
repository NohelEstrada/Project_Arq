<script setup lang="ts">
import { ref, onMounted } from "vue";
import axios from "axios";
import { getInsuranceApiUrl } from "../../utils/api";

interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: string;
  email: string;
  enabled: number;
  port?: string;
}

// Estado
const hospitals = ref<Hospital[]>([]);
const loading = ref(true);
const saving = ref(false);
const error = ref("");
const success = ref("");
const ip = import.meta.env.VITE_IP || "localhost";
const defaultHospitalId = ref<number | null>(null);

// Carga de hospitales
const fetchHospitals = async () => {
  try {
    loading.value = true;
    const response = await axios.get(getInsuranceApiUrl("/hospital"));
    
    if (response.data) {
      hospitals.value = response.data;
      
      // Verificar si hay un hospital por defecto guardado
      const storedDefaultHospital = localStorage.getItem('defaultHospital');
      if (storedDefaultHospital) {
        const parsedData = JSON.parse(storedDefaultHospital);
        defaultHospitalId.value = parsedData.idHospital;
      }
    } else {
      hospitals.value = [];
      error.value = "No se encontraron hospitales";
    }
  } catch (err) {
    console.error("Error al cargar hospitales:", err);
    error.value = "Error al cargar los hospitales. Por favor, intente nuevamente.";
  } finally {
    loading.value = false;
  }
};

// Guardar cambios en el puerto de un hospital
const saveHospitalPort = async (hospital: Hospital) => {
  try {
    saving.value = true;
    error.value = "";
    success.value = "";
    
    // Validar que el puerto sea un número válido
    const port = parseInt(hospital.port || "0");
    if (isNaN(port) || port <= 0 || port > 65535) {
      error.value = "El puerto debe ser un número entre 1 y 65535";
      return;
    }
    
    // Enviar actualización al servidor (sin incluir el ID en la URL)
    const response = await axios.put(getInsuranceApiUrl("/hospital"), hospital);
    
    if (response.data) {
      // Actualizar el hospital en la lista local
      const index = hospitals.value.findIndex(h => h.idHospital === hospital.idHospital);
      if (index !== -1) {
        hospitals.value[index] = response.data;
      }
      
      // Si este es el hospital por defecto, actualizar también en localStorage
      if (defaultHospitalId.value === hospital.idHospital) {
        setDefaultHospital(hospital);
      }
      
      success.value = `Puerto actualizado correctamente para ${hospital.name}`;
      setTimeout(() => { success.value = ""; }, 3000);
    }
  } catch (err) {
    console.error("Error al guardar puerto del hospital:", err);
    error.value = "Error al guardar el puerto del hospital. Por favor, intente nuevamente.";
  } finally {
    saving.value = false;
  }
};

// Establecer un hospital como predeterminado
const setDefaultHospital = (hospital: Hospital) => {
  try {
    // Guardar el hospital seleccionado en localStorage
    localStorage.setItem('defaultHospital', JSON.stringify({
      idHospital: hospital.idHospital,
      name: hospital.name,
      port: hospital.port || "5050" // Si no tiene puerto, usar 5050 por defecto
    }));
    
    defaultHospitalId.value = hospital.idHospital;
    success.value = `Hospital ${hospital.name} establecido como predeterminado`;
    setTimeout(() => { success.value = ""; }, 3000);
  } catch (err) {
    console.error("Error al establecer hospital por defecto:", err);
    error.value = "Error al establecer el hospital por defecto.";
  }
};

// Habilitar/Deshabilitar un hospital
const toggleHospitalStatus = async (hospital: Hospital) => {
  try {
    saving.value = true;
    error.value = "";
    
    // Cambiar el estado del hospital
    const updatedHospital = { 
      ...hospital, 
      enabled: hospital.enabled === 1 ? 0 : 1 
    };
    
    // Enviar actualización al servidor (sin incluir el ID en la URL)
    const response = await axios.put(getInsuranceApiUrl("/hospital"), updatedHospital);
    
    if (response.data) {
      // Actualizar el hospital en la lista local
      const index = hospitals.value.findIndex(h => h.idHospital === hospital.idHospital);
      if (index !== -1) {
        hospitals.value[index] = response.data;
      }
      
      // Si este era el hospital por defecto y ahora está deshabilitado, eliminar la configuración por defecto
      if (defaultHospitalId.value === hospital.idHospital && updatedHospital.enabled === 0) {
        localStorage.removeItem('defaultHospital');
        defaultHospitalId.value = null;
      }
      
      success.value = `Hospital ${hospital.name} ${hospital.enabled === 1 ? 'deshabilitado' : 'habilitado'} correctamente`;
      setTimeout(() => { success.value = ""; }, 3000);
    }
  } catch (err) {
    console.error("Error al cambiar estado del hospital:", err);
    error.value = "Error al cambiar el estado del hospital. Por favor, intente nuevamente.";
  } finally {
    saving.value = false;
  }
};

// Probar conexión al hospital
const testHospitalConnection = async (hospital: Hospital) => {
  try {
    if (!hospital.port) {
      error.value = "El hospital no tiene un puerto configurado";
      return;
    }
    
    loading.value = true;
    error.value = "";
    success.value = "";
    
    // Lista de posibles endpoints para probar
    const endpoints = [
      '/api/health-check',
      '/api/services',
      '/services',
      '/api/status',
      '/status',
      '/'
    ];
    
    let connected = false;
    
    // Intentar varios endpoints posibles
    for (const endpoint of endpoints) {
      try {
        console.log(`Intentando conectar a: http://${ip}:${hospital.port}${endpoint}`);
        const response = await axios.get(
          `http://${ip}:${hospital.port}${endpoint}`, 
          { 
            timeout: 3000,
            validateStatus: (status) => status < 500 // Aceptar cualquier respuesta HTTP que no sea 5xx
          }
        );
        
        // Si llegamos aquí, hubo alguna respuesta del servidor
        connected = true;
        success.value = `Conexión exitosa al hospital ${hospital.name} (endpoint: ${endpoint})`;
        break;
      } catch (innerErr) {
        console.log(`Error al intentar endpoint ${endpoint}:`, innerErr);
        // Continuar con el siguiente endpoint
      }
    }
    
    if (!connected) {
      error.value = `No se pudo conectar al hospital ${hospital.name}. Verifique que el puerto sea correcto y el servidor esté activo.`;
    }
  } catch (err) {
    console.error(`Error al probar conexión con ${hospital.name}:`, err);
    error.value = `Error al conectar con ${hospital.name}. Verifique que el puerto sea correcto y el servidor esté activo.`;
  } finally {
    loading.value = false;
  }
};

// Cargar datos iniciales
onMounted(() => {
  fetchHospitals();
});
</script>

<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-6">Configuración de Hospitales</h1>
    
    <div v-if="success" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4" role="alert">
      <p>{{ success }}</p>
    </div>
    
    <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4" role="alert">
      <p>{{ error }}</p>
    </div>
    
    <div class="mb-4 flex justify-between items-center">
      <button
        @click="fetchHospitals"
        class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded flex items-center"
        :disabled="loading"
      >
        <span v-if="loading">Actualizando...</span>
        <span v-else>Actualizar Lista</span>
      </button>
    </div>
    
    <!-- Loading spinner -->
    <div v-if="loading && hospitals.length === 0" class="text-center py-8">
      <div class="spinner"></div>
      <p class="mt-2 text-gray-600">Cargando hospitales...</p>
    </div>
    
    <!-- Mensajes si no hay hospitales -->
    <div v-else-if="hospitals.length === 0" class="text-center py-8">
      <p class="text-gray-600">No se encontraron hospitales en el sistema.</p>
    </div>
    
    <!-- Hospital por defecto -->
    <div v-if="defaultHospitalId" class="mb-6 p-4 bg-blue-50 border border-blue-200 rounded">
      <h2 class="text-lg font-semibold mb-2">Hospital Predeterminado</h2>
      <p v-if="defaultHospitalId">
        <span class="font-medium">Hospital actualmente seleccionado: </span>
        {{ hospitals.find(h => h.idHospital === defaultHospitalId)?.name || 'Desconocido' }}
        <span class="text-blue-600 font-medium ml-2">
          (Puerto: {{ hospitals.find(h => h.idHospital === defaultHospitalId)?.port || '5050' }})
        </span>
      </p>
      <p class="text-gray-600 text-sm mt-1">
        Este hospital se utilizará automáticamente para todas las conexiones a APIs de hospitales.
      </p>
    </div>
    
    <!-- Tabla de hospitales -->
    <div v-if="hospitals.length > 0" class="overflow-x-auto">
      <table class="min-w-full bg-white border border-gray-200">
        <thead>
          <tr>
            <th class="px-4 py-2 border-b text-left">Hospital</th>
            <th class="px-4 py-2 border-b text-left">Dirección</th>
            <th class="px-4 py-2 border-b text-left">Contacto</th>
            <th class="px-4 py-2 border-b text-center">Puerto</th>
            <th class="px-4 py-2 border-b text-center">Estado</th>
            <th class="px-4 py-2 border-b text-center">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="hospital in hospitals" :key="hospital.idHospital" class="hover:bg-gray-50"
              :class="{ 'bg-blue-50': defaultHospitalId === hospital.idHospital }">
            <td class="px-4 py-2 border-b">
              {{ hospital.name }}
              <span v-if="defaultHospitalId === hospital.idHospital" 
                  class="ml-2 px-2 py-0.5 bg-blue-100 text-blue-800 text-xs rounded-full">
                Predeterminado
              </span>
            </td>
            <td class="px-4 py-2 border-b">{{ hospital.address }}</td>
            <td class="px-4 py-2 border-b">
              <div>{{ hospital.phone }}</div>
              <div class="text-sm text-gray-600">{{ hospital.email }}</div>
            </td>
            <td class="px-4 py-2 border-b">
              <div class="flex items-center space-x-2">
                <input 
                  v-model="hospital.port" 
                  type="text" 
                  class="w-24 px-2 py-1 border border-gray-300 rounded text-center"
                  placeholder="Puerto"
                />
                <button 
                  @click="saveHospitalPort(hospital)"
                  class="bg-green-500 hover:bg-green-600 text-white px-2 py-1 rounded text-sm"
                  :disabled="saving"
                >
                  Guardar
                </button>
              </div>
            </td>
            <td class="px-4 py-2 border-b text-center">
              <span 
                :class="hospital.enabled === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                class="px-2 py-1 rounded text-xs font-medium"
              >
                {{ hospital.enabled === 1 ? 'Activo' : 'Inactivo' }}
              </span>
            </td>
            <td class="px-4 py-2 border-b text-center">
              <div class="flex flex-col space-y-2 items-center">
                <button 
                  v-if="hospital.enabled === 1 && defaultHospitalId !== hospital.idHospital" 
                  @click="setDefaultHospital(hospital)"
                  class="bg-blue-500 hover:bg-blue-600 text-white px-2 py-1 rounded text-sm w-24"
                >
                  Seleccionar
                </button>
                <button 
                  v-if="defaultHospitalId === hospital.idHospital"
                  @click="defaultHospitalId = null; localStorage.removeItem('defaultHospital')"
                  class="bg-gray-500 hover:bg-gray-600 text-white px-2 py-1 rounded text-sm w-24"
                >
                  Deseleccionar
                </button>
                <button 
                  @click="toggleHospitalStatus(hospital)"
                  :class="hospital.enabled === 1 ? 'bg-red-500 hover:bg-red-600' : 'bg-green-500 hover:bg-green-600'"
                  class="text-white px-2 py-1 rounded text-sm w-24"
                >
                  {{ hospital.enabled === 1 ? 'Deshabilitar' : 'Habilitar' }}
                </button>
                <button 
                  @click="testHospitalConnection(hospital)"
                  class="bg-blue-500 hover:bg-blue-600 text-white px-2 py-1 rounded text-sm w-24"
                  :disabled="!hospital.port"
                >
                  Probar Conexión
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <div class="mt-8">
      <h2 class="text-xl font-semibold mb-4">Información de Configuración</h2>
      <div class="bg-gray-50 p-4 rounded border border-gray-200">
        <p class="mb-2">Para que la conexión con los hospitales funcione correctamente:</p>
        <ul class="list-disc pl-6 space-y-1">
          <li>Asegúrese de que el puerto configurado corresponda al puerto del API del hospital.</li>
          <li>El puerto debe estar abierto y accesible desde el servidor de seguros.</li>
          <li>El formato del puerto debe ser numérico (ejemplo: 8000, 5050, 3000).</li>
          <li>Verifique que la estructura de la API del hospital sea compatible con el sistema de seguros.</li>
          <li><strong>Hospital Predeterminado:</strong> Al seleccionar un hospital, su puerto se utilizará automáticamente en todas las APIs.</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border-left-color: #3b82f6;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style> 