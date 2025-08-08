<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import axios from "axios";

interface Category {
  idCategory: number;
  name: string;
  enabled: number;
}

interface HospitalServiceItem {
  hospitalServiceId: string;
  name: string;
  imported: boolean;
  insuranceServiceId?: number;
  categories?: string[];
  subcategories?: string[];
  copay?: number;
  pay?: number;
  total?: number;
}

// Función para obtener el hospital predeterminado
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

// Obtener la configuración del hospital predeterminado
const defaultHospital = getDefaultHospital();
const ip = import.meta.env.VITE_IP || "localhost";
// Usar el puerto del hospital predeterminado o 5050 como fallback
const DEFAULT_PORT = defaultHospital?.port || '5050';

// Estado
const hospitalServices = ref<HospitalServiceItem[]>([]);
const categories = ref<Category[]>([]);
const loading = ref(true);
const error = ref("");
const success = ref("");

// Configuración de IPs para la conexión con el backend
const primaryIP = ip;
const fallbackIPs = ["localhost", "127.0.0.1", "192.168.0.4", "172.16.57.55"];
const possibleIPs = [primaryIP, ...fallbackIPs.filter(ip => ip !== primaryIP)];
const HOSPITAL_API_URL = `http://${primaryIP}:${DEFAULT_PORT}`;

// Estado del modal de aprobación
const showApprovalModal = ref(false);
const selectedService = ref<HospitalServiceItem | null>(null);
const selectedCategoryId = ref<number | null>(null);
const selectedSubcategoryId = ref<number | null>(null);
const coveragePercentage = ref(80); // porcentaje por defecto
const serviceDescription = ref("");
const modalError = ref("");

// Filtro de búsqueda
const searchQuery = ref("");

// Servicios filtrados
const filteredServices = computed(() => {
  if (!searchQuery.value) {
    return hospitalServices.value;
  }
  
  const query = searchQuery.value.toLowerCase();
  return hospitalServices.value.filter(service => 
    service.name.toLowerCase().includes(query)
  );
});

// Estado de conexión
const connectionIP = ref<string | null>(null);

// Función para probar múltiples IPs con fallback
async function tryMultipleIPs(endpoint: string, method: string = 'GET', data: any = null) {
  // Primero intentar con IP configurada en .env
  let lastError = null;
  
  for (const serverIP of possibleIPs) {
    try {
      console.log(`Intentando ${method} a http://${serverIP}:8080/api${endpoint}`);
      const url = `http://${serverIP}:8080/api${endpoint}`;
      const response = await axios({ 
        method, 
        url, 
        data, 
        timeout: 10000, // 10 segundos
        headers: method !== 'GET' ? { 'Content-Type': 'application/json' } : undefined
      });
      
      // Si llegamos aquí, la conexión fue exitosa
      if (!connectionIP.value) {
        connectionIP.value = serverIP;
        console.log(`Conexión exitosa a ${serverIP}`);
      }
      
      return response;
    } catch (error: any) {
      lastError = error;
      console.error(`Error con IP ${serverIP}:`, error.message);
    }
  }
  
  // Si llegamos aquí, ninguna IP funcionó
  if (lastError?.code === 'ECONNABORTED') {
    throw new Error(`Tiempo de espera agotado al conectar con los servidores. Verifique que al menos un servidor esté ejecutándose.`);
  } else if (lastError?.response) {
    throw new Error(`El servidor respondió con un error: ${lastError.response.status} - ${lastError.response.statusText}`);
  } else if (lastError?.request) {
    throw new Error(`No se recibió respuesta de ningún servidor. Verifique la conectividad de red y que al menos un servidor esté ejecutándose.`);
  } else {
    throw new Error(`Error al configurar la solicitud: ${lastError?.message || 'Error desconocido'}`);
  }
}

// Cargar datos
const fetchHospitalServices = async () => {
  try {
    loading.value = true;
    error.value = "";
    
    const response = await tryMultipleIPs('/insurance-services/hospital-services', 'GET');
    hospitalServices.value = response.data || [];
    console.log("Servicios obtenidos:", hospitalServices.value);
  } catch (err) {
    console.error("Error al cargar servicios del hospital:", err);
    error.value = "Error al cargar servicios del hospital. Por favor, intente nuevamente.";
    hospitalServices.value = [];
  } finally {
    loading.value = false;
  }
};

const fetchCategories = async () => {
  try {
    const response = await tryMultipleIPs('/category', 'GET');
    categories.value = response.data || [];
  } catch (err) {
    console.error("Error al cargar categorías:", err);
    error.value = "Error al cargar categorías. Por favor, intente nuevamente.";
    categories.value = [];
  }
};

// Abrir modal para aprobar servicio
const openApprovalModal = (service: HospitalServiceItem) => {
  selectedService.value = service;
  selectedCategoryId.value = null;
  selectedSubcategoryId.value = null;
  coveragePercentage.value = 80;
  serviceDescription.value = "";
  modalError.value = "";
  showApprovalModal.value = true;
};

// Cerrar modal
const closeApprovalModal = () => {
  showApprovalModal.value = false;
  selectedService.value = null;
};

// Aprobar servicio
const approveService = async () => {
  if (!selectedService.value || !selectedCategoryId.value || !selectedSubcategoryId.value) {
    modalError.value = "Por favor, complete todos los campos obligatorios.";
    return;
  }
  
  try {
    loading.value = true;
    modalError.value = "";
    
    const serviceData = {
      hospitalServiceId: selectedService.value.hospitalServiceId,
      categoryId: selectedCategoryId.value,
      subcategoryId: selectedSubcategoryId.value,
      coveragePercentage: coveragePercentage.value,
      description: serviceDescription.value || `Servicio importado del hospital: ${selectedService.value.name}`
    };
    
    await tryMultipleIPs('/insurance-services/approve-hospital-service', 'POST', serviceData);
    
    // Actualizar la lista de servicios
    await fetchHospitalServices();
    
    success.value = "Servicio aprobado correctamente.";
    closeApprovalModal();
    
    // Limpiar mensaje de éxito después de 3 segundos
    setTimeout(() => {
      success.value = "";
    }, 3000);
  } catch (err) {
    console.error("Error al aprobar servicio:", err);
    modalError.value = "Error al aprobar el servicio. Por favor, intente nuevamente.";
  } finally {
    loading.value = false;
  }
};

// Test connection to all possible servers
const testConnection = async () => {
  testingConnection.value = true;
  connectionStatus.value = null;
  const results: Record<string, boolean> = {};
  
  // Probar cada IP por separado
  for (const ip of possibleIPs) {
    try {
      // Intenta un endpoint simple o healthcheck
      await axios.get(`http://${ip}:8080/api/healthcheck`, {
        timeout: 3000
      });
      results[ip] = true;
    } catch (err) {
      try {
        // Intentar con otro endpoint como fallback
        await axios.get(`http://${ip}:8080/api/users/count`, {
          timeout: 3000
        });
        results[ip] = true;
      } catch (err2) {
        results[ip] = false;
      }
    }
  }
  
  // Actualizar UI con resultados
  connectionTestResults.value = results;
  
  // Si al menos un servidor está disponible, considerar la prueba exitosa
  connectionStatus.value = Object.values(results).some(r => r) ? 'success' : 'error';
  
  testingConnection.value = false;
};

// Cargar datos iniciales
onMounted(async () => {
  try {
    await Promise.all([fetchHospitalServices(), fetchCategories()]);
  } catch (err) {
    console.error("Error al cargar datos iniciales:", err);
    error.value = "Error al cargar datos. Por favor, recargue la página.";
  }
});

const testingConnection = ref(false);
const connectionStatus = ref<'success' | 'error' | null>(null);

// Estado para resultados de pruebas de conexión
const connectionTestResults = ref<Record<string, boolean>>({});

// Información sobre el hospital predeterminado
const usingDefaultHospital = computed(() => {
  return defaultHospital 
    ? `Hospital seleccionado: ${defaultHospital.name} (Puerto: ${defaultHospital.port || '5050'})` 
    : 'No hay hospital predeterminado seleccionado';
});
</script>

<template>
  <div class="container mx-auto p-6">
    <h1 class="text-2xl font-bold mb-6">Importar Servicios de Hospital</h1>
    
    <!-- Información del hospital por defecto -->
    <div v-if="defaultHospital" class="bg-blue-50 p-3 rounded mb-4 border border-blue-200">
      <div class="flex items-center">
        <span class="text-blue-700">{{ usingDefaultHospital }}</span>
      </div>
    </div>
    
    <div v-if="success" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4" role="alert">
      <p>{{ success }}</p>
    </div>
    
    <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4" role="alert">
      <p>{{ error }}</p>
      
      <!-- Información de diagnóstico -->
      <div class="mt-3 text-sm">
        <p class="font-semibold">Información de diagnóstico:</p>
        <ul class="list-disc pl-5 mt-1">
          <li>IP principal (VITE_IP): {{ primaryIP }}</li>
          <li>URL de API del seguro: http://[servidor]:8080/api</li>
          <li>URL de API del hospital: {{ HOSPITAL_API_URL }}</li>
          <li v-if="connectionIP">Servidor conectado actualmente: {{ connectionIP }}</li>
        </ul>
        
        <div class="mt-3">
          <button 
            @click="testConnection" 
            class="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600 text-sm"
            :disabled="testingConnection"
          >
            {{ testingConnection ? 'Probando servidores...' : 'Probar conexión a servidores' }}
          </button>
          
          <div v-if="Object.keys(connectionTestResults).length > 0" class="mt-2 p-2 bg-gray-50 rounded">
            <p class="font-medium">Resultados por servidor:</p>
            <ul class="mt-1">
              <li v-for="(result, ip) in connectionTestResults" :key="ip" class="flex items-center">
                <span :class="result ? 'text-green-600' : 'text-red-600'" class="mr-2">
                  {{ result ? '✅' : '❌' }}
                </span>
                <span>{{ ip }}:8080 - {{ result ? 'Conectado' : 'No disponible' }}</span>
              </li>
            </ul>
          </div>
          
          <div v-if="connectionStatus" class="mt-2">
            <p v-if="connectionStatus === 'success'" class="text-green-600">✅ Al menos un servidor está disponible</p>
            <p v-else-if="connectionStatus === 'error'" class="text-red-600">❌ No se pudo conectar a ningún servidor</p>
          </div>
        </div>
        
        <div class="mt-3">
          <p>Sugerencias:</p>
          <ul class="list-disc pl-5 mt-1">
            <li>Verifique que el servidor backend esté ejecutándose en el puerto 8080</li>
            <li>Compruebe que la IP en el archivo .env sea correcta (actual: {{ primaryIP }})</li>
            <li>Revise los logs del servidor backend para detectar posibles errores</li>
            <li>Compruebe su conexión de red</li>
            <li>Intente reiniciar el servidor backend si está disponible</li>
          </ul>
          
          <div class="mt-2 p-2 bg-yellow-50 text-yellow-800 rounded">
            <p class="font-medium">Nota:</p>
            <p>El sistema intentará automáticamente conectarse a servidores alternativos si no puede conectarse al principal.</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Filtro de búsqueda -->
    <div class="mb-4">
      <input 
        v-model="searchQuery"
        type="text"
        placeholder="Buscar servicios por nombre..."
        class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
      />
    </div>
    
    <!-- Spinner de carga -->
    <div v-if="loading" class="text-center py-8">
      <div class="spinner"></div>
      <p class="mt-2 text-gray-600">Cargando servicios del hospital...</p>
    </div>
    
    <!-- Mensaje cuando no hay servicios -->
    <div v-else-if="hospitalServices.length === 0" class="text-center py-8">
      <p class="text-gray-600">No se encontraron servicios en el hospital.</p>
      <div class="mt-4 max-w-2xl mx-auto">
        <button 
          @click="testConnection" 
          class="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 mr-2"
          :disabled="testingConnection"
        >
          {{ testingConnection ? 'Probando...' : 'Diagnosticar conexión' }}
        </button>
        <button 
          @click="fetchHospitalServices" 
          class="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
        >
          Intentar nuevamente
        </button>
      </div>
    </div>
    
    <!-- Lista de servicios -->
    <div v-else class="overflow-x-auto">
      <table class="min-w-full bg-white border border-gray-200">
        <thead>
          <tr>
            <th class="px-4 py-2 border-b text-left">Nombre del Servicio</th>
            <th class="px-4 py-2 border-b text-right">Costo Total (Q)</th>
            <th class="px-4 py-2 border-b text-center">Estado</th>
            <th class="px-4 py-2 border-b text-center">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="service in filteredServices" :key="service.hospitalServiceId" class="hover:bg-gray-50">
            <td class="px-4 py-2 border-b">{{ service.name }}</td>
            <td class="px-4 py-2 border-b text-right">{{ (service.total || 0).toFixed(2) }}</td>
            <td class="px-4 py-2 border-b text-center">
              <span 
                :class="service.imported ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'"
                class="px-2 py-1 rounded-full text-xs font-medium"
              >
                {{ service.imported ? 'Importado' : 'Pendiente' }}
              </span>
            </td>
            <td class="px-4 py-2 border-b text-center">
              <button 
                v-if="!service.imported"
                @click="openApprovalModal(service)"
                class="bg-blue-600 hover:bg-blue-700 text-white text-sm px-3 py-1 rounded"
              >
                Aprobar
              </button>
              <span v-else class="text-green-700">Servicio ya aprobado</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Modal de aprobación -->
    <div v-if="showApprovalModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-xl mx-4">
        <h2 class="text-xl font-semibold mb-4">Aprobar Servicio del Hospital</h2>
        
        <div v-if="modalError" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4" role="alert">
          <p>{{ modalError }}</p>
        </div>
        
        <div v-if="selectedService" class="mb-4">
          <p class="mb-2"><strong>Nombre:</strong> {{ selectedService.name }}</p>
          <p class="mb-2"><strong>Costo Total:</strong> Q{{ (selectedService.total || 0).toFixed(2) }}</p>
        </div>
        
        <form @submit.prevent="approveService" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Descripción (opcional)</label>
            <textarea 
              v-model="serviceDescription" 
              rows="2"
              placeholder="Descripción del servicio en el seguro"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            ></textarea>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Categoría *</label>
              <select 
                v-model="selectedCategoryId"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              >
                <option :value="null">Seleccione...</option>
                <option 
                  v-for="category in categories.filter(c => c.enabled === 1)" 
                  :key="category.idCategory"
                  :value="category.idCategory"
                >
                  {{ category.name }}
                </option>
              </select>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Subcategoría *</label>
              <select 
                v-model="selectedSubcategoryId"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              >
                <option :value="null">Seleccione...</option>
                <option 
                  v-for="category in categories.filter(c => c.enabled === 1)" 
                  :key="category.idCategory"
                  :value="category.idCategory"
                >
                  {{ category.name }}
                </option>
              </select>
            </div>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Porcentaje de Cobertura (%) *
            </label>
            <input 
              v-model.number="coveragePercentage"
              type="number"
              min="0"
              max="100"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          
          <div class="flex justify-end space-x-3 mt-4">
            <button
              type="button"
              @click="closeApprovalModal"
              class="px-4 py-2 bg-gray-300 text-gray-800 rounded hover:bg-gray-400"
            >
              Cancelar
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              :disabled="loading"
            >
              {{ loading ? 'Aprobando...' : 'Aprobar Servicio' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 4px solid #3498db;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style> 