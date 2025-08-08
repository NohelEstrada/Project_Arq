<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import axios from "axios";
import { getInsuranceApiUrl } from "../utils/api";
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

// Interfaces
interface Transaction {
  idTransaction: number;
  transDate: string;
  total: number;
  copay: number;
  transactionComment: string;
  result: string;
  covered: number;
  auth: string;
  hospital: {
    idHospital: number;
    name: string;
  };
}

interface HospitalService {
  _id: string;
  name: string;
  total: number;
  description?: string;
  date: string;
}

// Estado
const user = JSON.parse(localStorage.getItem('user') || 'null');
const userTransactions = ref<Transaction[]>([]);
const hospitalServices = ref<HospitalService[]>([]);
const hospitalUserDetails = ref<any>(null);
const loading = ref(false);
const error = ref("");
const activeTab = ref("insurance");

// Obtener información del hospital predeterminado
const defaultHospital = getDefaultHospital();
const ip = import.meta.env.VITE_IP || "localhost";
// Usar el puerto del hospital predeterminado o 5050 como fallback
const DEFAULT_PORT = defaultHospital?.port || '5050';
// Configuración de IPs
const possibleIPs = [ip];
const HOSPITAL_API_URL = `http://${ip}:${DEFAULT_PORT}`;

// Información sobre el hospital predeterminado para mostrar
const usingDefaultHospital = computed(() => {
  return defaultHospital 
    ? `Hospital seleccionado: ${defaultHospital.name} (Puerto: ${defaultHospital.port || '5050'})` 
    : 'No hay hospital predeterminado seleccionado';
});

// Función para probar múltiples IPs
async function tryMultipleIPs(endpoint: string, method: string = 'GET', data: any = null) {
  const serverIP = import.meta.env.VITE_IP || "localhost";
  try {
    const url = getInsuranceApiUrl(endpoint);
    console.log(`Intentando ${method} a ${url}`);
    const response = await axios({ method, url, data, timeout: 3000 });
    return response;
  } catch (error: any) {
    console.error(`Error con IP ${serverIP}:`, error.message);
    throw new Error("No se pudo conectar con el servidor");
  }
}

// Cargar transacciones del usuario
const fetchUserTransactions = async () => {
  if (!user || !user.idUser) {
    error.value = "No se encontró información del usuario, por favor inicie sesión nuevamente";
    return;
  }
  
  try {
    loading.value = true;
    const response = await tryMultipleIPs(`/transactions?userId=${user.idUser}`, 'GET');
    userTransactions.value = response.data || [];
  } catch (err: any) {
    console.error("Error al cargar transacciones del usuario:", err);
    error.value = "Error al cargar los servicios. Por favor, intente de nuevo más tarde.";
    userTransactions.value = [];
  } finally {
    loading.value = false;
  }
};

// Obtener información del usuario en el sistema del hospital
const fetchHospitalUserInfo = async () => {
  if (!user || !user.email) return;
  
  try {
    // Intentar obtener usuario por email desde el hospital
    const response = await axios.get(`${HOSPITAL_API_URL}/users?email=${user.email}`);
    
    // Verificar si hay datos y encontrar el usuario por email
    if (response.data) {
      let foundUser = null;
      
      // La API puede retornar datos en diferentes formatos, intentar ambos
      if (Array.isArray(response.data)) {
        foundUser = response.data.find((u: any) => u.email === user.email);
      } else if (response.data.appointments && Array.isArray(response.data.appointments)) {
        foundUser = response.data.appointments.find((u: any) => u.email === user.email);
      }
      
      if (foundUser) {
        hospitalUserDetails.value = foundUser;
        
        // Si encontramos el usuario, obtener sus servicios del hospital
        fetchHospitalUserServices(foundUser._id);
      } else {
        hospitalUserDetails.value = null;
        hospitalServices.value = [];
      }
    }
  } catch (err) {
    console.error("Error al buscar usuario en el hospital:", err);
    hospitalUserDetails.value = null;
    hospitalServices.value = [];
  }
};

// Obtener servicios del usuario en el hospital
const fetchHospitalUserServices = async (hospitalUserId: string) => {
  try {
    // Obtener las citas/servicios del usuario en el hospital
    const servicesResponse = await axios.get(`${HOSPITAL_API_URL}/api/appointments/patient/${hospitalUserId}`);
    
    if (servicesResponse.data && Array.isArray(servicesResponse.data)) {
      hospitalServices.value = servicesResponse.data.map((appointment: any) => ({
        _id: appointment._id,
        name: `Cita con ${appointment.doctor?.name || 'Doctor'}`,
        date: new Date(appointment.start).toLocaleDateString(),
        total: appointment.cost || 0,
        description: appointment.reason || 'Sin descripción'
      }));
    }
  } catch (err) {
    console.error("Error al cargar servicios del hospital:", err);
    hospitalServices.value = [];
  }
};

// Formatear fecha
const formatDate = (dateString: string) => {
  if (!dateString) return "N/A";
  return new Date(dateString).toLocaleDateString();
};

// Obtener color para estatus de cobertura
const getCoveredStatusColor = (covered: number) => {
  return covered === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800';
};

// Obtener texto para estatus de cobertura
const getCoveredStatusText = (covered: number) => {
  return covered === 1 ? 'Cubierto' : 'No cubierto';
};

// Cargar datos iniciales
onMounted(() => {
  if (!user) {
    error.value = "Debe iniciar sesión para ver sus servicios";
    return;
  }
  
  fetchUserTransactions();
  fetchHospitalUserInfo();
});
</script>

<template>
  <div class="container mx-auto p-6">
    <h1 class="text-2xl font-bold mb-6">Mis Servicios Médicos</h1>
    
    <!-- Información del hospital por defecto -->
    <div v-if="defaultHospital" class="bg-blue-50 p-3 rounded mb-4 border border-blue-200">
      <div class="flex items-center">
        <span class="text-blue-700">{{ usingDefaultHospital }}</span>
      </div>
    </div>
    
    <!-- Mensajes de error -->
    <div v-if="error" class="bg-red-100 text-red-700 p-3 mb-4 rounded">{{ error }}</div>
    
    <!-- Mensaje de no autenticado -->
    <div v-if="!user" class="bg-yellow-100 text-yellow-700 p-4 rounded">
      <p>Debe iniciar sesión para ver sus servicios médicos.</p>
    </div>
    
    <div v-else>
      <!-- Tabs -->
      <div class="border-b mb-6">
        <nav class="flex space-x-8">
          <button
            @click="activeTab = 'insurance'"
            :class="[
              'py-2 px-1 border-b-2 font-medium text-sm',
              activeTab === 'insurance'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            ]"
          >
            Servicios cubiertos por el seguro
          </button>
          <button
            @click="activeTab = 'hospital'"
            :class="[
              'py-2 px-1 border-b-2 font-medium text-sm',
              activeTab === 'hospital'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            ]"
          >
            Mis servicios en hospitales
          </button>
        </nav>
      </div>
      
      <!-- Contenido de los tabs -->
      <div v-if="activeTab === 'insurance'">
        <h2 class="text-xl font-semibold mb-4">Servicios cubiertos por mi seguro</h2>
        
        <div v-if="loading" class="text-center py-6">
          <div class="spinner"></div>
          <p class="mt-4 text-gray-600">Cargando sus servicios...</p>
        </div>
        
        <div v-else-if="userTransactions.length === 0" class="bg-gray-100 p-6 text-center rounded-md">
          <p>No se encontraron servicios cubiertos por el seguro.</p>
        </div>
        
        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Hospital</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Copago</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Comentario</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Resultado</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="transaction in userTransactions" :key="transaction.idTransaction" class="hover:bg-gray-50">
                <td class="px-4 py-3 whitespace-nowrap">{{ formatDate(transaction.transDate) }}</td>
                <td class="px-4 py-3 whitespace-nowrap">{{ transaction.hospital?.name || 'N/A' }}</td>
                <td class="px-4 py-3 whitespace-nowrap">Q{{ transaction.total.toFixed(2) }}</td>
                <td class="px-4 py-3 whitespace-nowrap">Q{{ transaction.copay.toFixed(2) }}</td>
                <td class="px-4 py-3 whitespace-nowrap">
                  <span :class="['px-2 py-1 rounded-full text-xs font-medium', getCoveredStatusColor(transaction.covered)]">
                    {{ getCoveredStatusText(transaction.covered) }}
                  </span>
                </td>
                <td class="px-4 py-3">{{ transaction.transactionComment }}</td>
                <td class="px-4 py-3">{{ transaction.result }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <div v-if="activeTab === 'hospital'">
        <h2 class="text-xl font-semibold mb-4">Mis servicios en hospitales</h2>
        
        <div v-if="!hospitalUserDetails" class="bg-yellow-100 p-4 rounded-md mb-4">
          <p>No se encontró su información en el sistema del hospital. Es posible que no haya utilizado servicios del hospital hasta el momento o que esté registrado con una dirección de correo diferente.</p>
        </div>
        
        <div v-else-if="hospitalServices.length === 0" class="bg-gray-100 p-6 text-center rounded-md">
          <p>No se encontraron servicios del hospital para su cuenta.</p>
        </div>
        
        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Servicio</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Costo</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Detalles</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="service in hospitalServices" :key="service._id" class="hover:bg-gray-50">
                <td class="px-4 py-3">{{ service.name }}</td>
                <td class="px-4 py-3 whitespace-nowrap">{{ service.date }}</td>
                <td class="px-4 py-3 whitespace-nowrap">Q{{ service.total.toFixed(2) }}</td>
                <td class="px-4 py-3">{{ service.description }}</td>
              </tr>
            </tbody>
          </table>
        </div>
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