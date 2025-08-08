<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import axios from "axios";

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
interface User {
  idUser: number;
  name: string;
  cui: number;
  phone: string;
  email: string;
  address: string;
  birthDate: string;
  role: string;
  enabled: number;
  paidService: boolean | null;
  expirationDate: string;
  policyId: number | null;
  policy: Policy | null;
  password?: string;
  username: string;
  // Campos adicionales
  policyNumber?: string;
  hospitalUserId?: string;
}

interface Transaction {
  idTransaction: number;
  transDate: string;
  total: number;
  copay: number;
  transactionComment: string;
  result: string;
  covered: number;
  completed: boolean;
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

interface Policy {
  idPolicy: number;
  percentage: number;
  creationDate: string;
  expDate: string;
  cost: number;
  enabled: number;
}

interface Service {
  _id: string;
  name: string;
  doctor?: string;
  reason?: string;
  date: string;
  total?: number;
  description?: string;
}

// Obtener la configuración del hospital predeterminado
const defaultHospital = getDefaultHospital();
const ip = import.meta.env.VITE_IP || "localhost";
// Usar el puerto del hospital predeterminado o 5050 como fallback
const DEFAULT_PORT = defaultHospital?.port || '5050';

// Estado
const users = ref<User[]>([]);
const selectedUser = ref<User | null>(null);
const userTransactions = ref<Transaction[]>([]);
const loading = ref(true);
const error = ref("");
const success = ref("");
const searchQuery = ref("");
const hospitalServices = ref<Service[]>([]);
const hospitalUserDetails = ref<any>(null);
const availablePolicies = ref<Policy[]>([]);
const loadingPolicies = ref(false);
import { getInsuranceApiUrl } from "../../utils/api";
// Estado del modal
const showUserModal = ref(false);
const activeTab = ref("profile");

// Configuración de IPs
const possibleIPs = [ip];
const HOSPITAL_API_URL = `http://${ip}:${DEFAULT_PORT}`;
const INSURANCE_API_BASE = getInsuranceApiUrl("/");

// Información sobre el hospital predeterminado
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

// Cargar todos los usuarios
const fetchUsers = async () => {
  try {
    loading.value = true;
    const response = await tryMultipleIPs('/users', 'GET');
    users.value = response.data.filter((user: User) => 
      user.role === 'patient' || !user.role || user.role.trim() === '');
    
    // Verificar expiración de servicios para todos los usuarios
    users.value.forEach(user => {
      checkServiceExpiration(user);
    });
    
    // Si algún usuario ha cambiado, actualizar en el backend
    const usersToUpdate = users.value.filter(user => {
      // Si el servicio estaba pagado pero ha expirado
      if (user.paidService === false && user.expirationDate && new Date(user.expirationDate) < new Date()) {
        return true;
      }
      return false;
    });
    
    // Actualizar usuarios expirados en el backend
    for (const user of usersToUpdate) {
      try {
        await tryMultipleIPs(`/users/${user.idUser}`, 'PUT', user);
        console.log(`Usuario ${user.name} actualizado: servicio expirado`);
      } catch (updateErr) {
        console.error(`Error al actualizar servicio expirado del usuario ${user.idUser}:`, updateErr);
      }
    }
  } catch (err: any) {
    error.value = "Error al cargar la lista de usuarios";
    console.error(err);
  } finally {
    loading.value = false;
  }
};

// Verificar estado de pago del servicio
const checkServiceExpiration = (user: User) => {
  // Si no tiene fecha de expiración o el servicio no está definido como pagado, no hay nada que verificar
  if (!user.expirationDate || user.paidService !== true) return;
  
  const today = new Date();
  const expiration = new Date(user.expirationDate);
  
  if (expiration < today) {
    user.paidService = false;
    user.policyId = null;
  }
};

// Abrir modal para gestionar un usuario
const openUserModal = async (user: User) => {
  selectedUser.value = { ...user };
  
  // Verificar si el servicio ha expirado
  checkServiceExpiration(selectedUser.value);
  
  showUserModal.value = true;
  activeTab.value = "profile";
  
  // Cargar transacciones del usuario
  await fetchUserTransactions(user.idUser);
  
  // Intentar obtener datos del usuario en el sistema del hospital por email
  await fetchHospitalUserInfo(user.email);
};

// Cargar transacciones de un usuario
const fetchUserTransactions = async (userId: number) => {
  try {
    loading.value = true;
    const response = await tryMultipleIPs(`/transactions?userId=${userId}`, 'GET');
    userTransactions.value = response.data || [];
  } catch (err: any) {
    console.error("Error al cargar transacciones del usuario:", err);
    userTransactions.value = [];
  } finally {
    loading.value = false;
  }
};

// Obtener información del usuario en el sistema del hospital
const fetchHospitalUserInfo = async (email: string) => {
  try {
    // Intentar obtener usuario por email desde el hospital
    const response = await axios.get(getInsuranceApiUrl(`/users?email=${email}`));
    
    // Verificar si hay datos y encontrar el usuario por email
    if (response.data) {
      let foundUser = null;
      
      // La API puede retornar datos en diferentes formatos, intentar ambos
      if (Array.isArray(response.data)) {
        foundUser = response.data.find((u: any) => u.email === email);
      } else if (response.data.appointments && Array.isArray(response.data.appointments)) {
        foundUser = response.data.appointments.find((u: any) => u.email === email);
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
    const servicesResponse = await axios.get(getInsuranceApiUrl(`/appointments/patient/${hospitalUserId}`));
    
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

// Guardar cambios en los datos del usuario
const saveUserProfile = async () => {
  if (!selectedUser.value) return;
  
  try {
    loading.value = true;
    
    // Crear un objeto para enviar al servidor sin el campo policyId
    const userToSend = {
      idUser: selectedUser.value.idUser,
      name: selectedUser.value.name,
      cui: selectedUser.value.cui,
      phone: selectedUser.value.phone,
      email: selectedUser.value.email,
      address: selectedUser.value.address,
      birthDate: selectedUser.value.birthDate,
      role: selectedUser.value.role,
      enabled: selectedUser.value.enabled,
      paidService: selectedUser.value.paidService,
      expirationDate: selectedUser.value.expirationDate,
      // Si hay policyId, incluir el objeto policy
      policy: selectedUser.value.policyId ? { idPolicy: selectedUser.value.policyId } : null,
      password: selectedUser.value.password
    };
    
    const response = await tryMultipleIPs(`/users/${userToSend.idUser}`, 'PUT', userToSend);
    
    // Actualizar la lista de usuarios
    const index = users.value.findIndex(u => u.idUser === selectedUser.value?.idUser);
    if (index !== -1) {
      users.value[index] = response.data;
    }
    
    success.value = "Información de usuario actualizada correctamente";
    setTimeout(() => {
      success.value = "";
    }, 3000);
  } catch (err: any) {
    error.value = "Error al actualizar la información del usuario";
    console.error(err);
  } finally {
    loading.value = false;
  }
};

// Cerrar modal
const closeModal = () => {
  showUserModal.value = false;
  selectedUser.value = null;
  userTransactions.value = [];
  hospitalServices.value = [];
  hospitalUserDetails.value = null;
};

// Filtrar usuarios por nombre o email
const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value;
  
  const query = searchQuery.value.toLowerCase();
  return users.value.filter(user => 
    user.name.toLowerCase().includes(query) || 
    user.email.toLowerCase().includes(query) ||
    (user.cui && user.cui.toString().includes(query))
  );
});

// Filtrar transacciones completadas
const filteredTransactions = computed(() => {
  return userTransactions.value.filter(transaction => transaction.completed === true);
});

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

// Obtener color para estatus de servicio
const getServiceStatusColor = (paidService: boolean | null) => {
  if (paidService === true) return 'bg-green-100 text-green-800';
  if (paidService === false) return 'bg-red-100 text-red-800';
  return 'bg-gray-100 text-gray-800'; // Para valor null
};

// Obtener texto para estatus de servicio
const getServiceStatusText = (paidService: boolean | null) => {
  if (paidService === true) return 'Activo';
  if (paidService === false) return 'Inactivo';
  return 'No definido'; // Para valor null
};

// Función para cargar pólizas disponibles
const fetchPolicies = async () => {
  try {
    loadingPolicies.value = true;
    error.value = "";
    
    const response = await tryMultipleIPs('/policy', 'GET');
    availablePolicies.value = response.data || [];
    
    console.log("Pólizas disponibles:", availablePolicies.value);
  } catch (err) {
    console.error("Error al cargar pólizas:", err);
    error.value = "Error al cargar la lista de pólizas";
  } finally {
    loadingPolicies.value = false;
  }
};

// Obtener la póliza seleccionada actual
const selectedPolicy = computed(() => {
  if (!selectedUser.value || !selectedUser.value.policyId) return null;
  return availablePolicies.value.find(policy => policy.idPolicy === selectedUser.value?.policyId) || null;
});

// Formatear precio
const formatPrice = (price: number): string => {
  return `Q${price.toFixed(2)}`;
};

// Seleccionar póliza
const selectPolicy = (policyId: number | null) => {
  if (selectedUser.value) {
    selectedUser.value.policyId = policyId;
    selectedUser.value.policy = policyId ? availablePolicies.value.find(p => p.idPolicy === policyId) || null : null;
  }
};

// Cargar datos al iniciar
onMounted(() => {
  fetchUsers();
  fetchPolicies();
});
</script>

<template>
  <div class="container mx-auto p-6">
    <h1 class="text-2xl font-bold mb-6">Administración de Clientes</h1>
    
    <!-- Información del hospital por defecto -->
    <div v-if="defaultHospital" class="bg-blue-50 p-3 rounded mb-4 border border-blue-200">
      <div class="flex items-center">
        <span class="text-blue-700">{{ usingDefaultHospital }}</span>
      </div>
    </div>
    
    <!-- Mensajes de éxito o error -->
    <div v-if="success" class="bg-green-100 text-green-700 p-3 mb-4 rounded">{{ success }}</div>
    <div v-if="error" class="bg-red-100 text-red-700 p-3 mb-4 rounded">{{ error }}</div>
    
    <!-- Búsqueda -->
    <div class="mb-6">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Buscar por nombre, email o CUI..."
        class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>
    
    <!-- Lista de usuarios -->
    <div class="bg-white shadow-md rounded-lg overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">CUI</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Teléfono</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Servicio</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-if="loading">
            <td colspan="6" class="px-6 py-4 text-center">Cargando...</td>
          </tr>
          <tr v-else-if="filteredUsers.length === 0">
            <td colspan="6" class="px-6 py-4 text-center">No se encontraron usuarios</td>
          </tr>
          <tr v-for="user in filteredUsers" :key="user.idUser" class="hover:bg-gray-100">
            <td class="px-6 py-4 whitespace-nowrap">{{ user.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ user.email }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ user.cui }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ user.phone }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span 
                :class="getServiceStatusColor(user.paidService)"
                class="px-2 py-1 rounded-full text-xs font-medium"
              >
                {{ getServiceStatusText(user.paidService) }}
              </span>
              <span v-if="user.paidService && user.expirationDate" class="ml-2 text-xs text-gray-500">
                Vence: {{ formatDate(user.expirationDate) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <button
                @click="openUserModal(user)"
                class="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded-md"
              >
                Gestionar
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Modal de gestión de usuario -->
    <div v-if="showUserModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg w-full max-w-4xl mx-4 max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <div class="flex justify-between items-center mb-6">
            <h2 class="text-xl font-semibold">Gestión de Cliente: {{ selectedUser?.name }}</h2>
            <button @click="closeModal" class="text-gray-500 hover:text-gray-700">
              <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          
          <!-- Tabs -->
          <div class="border-b mb-6">
            <nav class="flex space-x-8">
              <button
                @click="activeTab = 'profile'"
                :class="[
                  'py-2 px-1 border-b-2 font-medium text-sm',
                  activeTab === 'profile'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                ]"
              >
                Perfil
              </button>
              <button
                @click="activeTab = 'services'"
                :class="[
                  'py-2 px-1 border-b-2 font-medium text-sm',
                  activeTab === 'services'
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                ]"
              >
                Servicios cubiertos
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
                Servicios hospital
              </button>
            </nav>
          </div>
          
          <!-- Contenido de cada tab -->
          <div v-if="activeTab === 'profile' && selectedUser">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700 mb-1">Nombre completo</label>
                <input
                  v-model="selectedUser.name"
                  type="text"
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              
              <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700 mb-1">CUI</label>
                <input
                  v-model="selectedUser.cui"
                  type="number"
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              
              <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input
                  v-model="selectedUser.email"
                  type="email"
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              
              <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                <input
                  v-model="selectedUser.phone"
                  type="tel"
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              
              <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700 mb-1">Dirección</label>
                <input
                  v-model="selectedUser.address"
                  type="text"
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              
              <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700 mb-1">Fecha de nacimiento</label>
                <input
                  v-model="selectedUser.birthDate"
                  type="date"
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              
              <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700 mb-1">Estado de la cuenta</label>
                <select
                  v-model="selectedUser.enabled"
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                >
                  <option :value="1">Activo</option>
                  <option :value="0">Inactivo</option>
                </select>
              </div>
              
              <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700 mb-1">Servicio pagado</label>
                <div class="flex items-center space-x-6">
                  <div class="flex items-center">
                    <input
                      type="radio" 
                      :value="true"
                      v-model="selectedUser.paidService"
                      id="service-active"
                      class="h-4 w-4 text-blue-600 border-gray-300 focus:ring-blue-500"
                    />
                    <label for="service-active" class="ml-2 text-sm text-gray-600">Activo</label>
                  </div>
                  <div class="flex items-center">
                    <input
                      type="radio" 
                      :value="false"
                      v-model="selectedUser.paidService"
                      id="service-inactive"
                      class="h-4 w-4 text-blue-600 border-gray-300 focus:ring-blue-500"
                    />
                    <label for="service-inactive" class="ml-2 text-sm text-gray-600">Inactivo</label>
                  </div>
                  <div class="flex items-center">
                    <input
                      type="radio" 
                      :value="null"
                      v-model="selectedUser.paidService"
                      id="service-undefined"
                      class="h-4 w-4 text-blue-600 border-gray-300 focus:ring-blue-500"
                    />
                    <label for="service-undefined" class="ml-2 text-sm text-gray-600">No definido</label>
                  </div>
                </div>
              </div>
              
              <div class="mb-4" v-if="selectedUser.paidService === true">
                <label class="block text-sm font-medium text-gray-700 mb-1">Fecha de expiración del servicio</label>
                <input
                  v-model="selectedUser.expirationDate"
                  type="date"
                  :min="new Date().toISOString().split('T')[0]"
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
                <p class="mt-1 text-sm text-gray-500">
                  Al vencer esta fecha, el servicio se desactivará automáticamente.
                </p>
              </div>
              
              <div class="mb-4" v-if="selectedUser.paidService === true">
                <label class="block text-sm font-medium text-gray-700 mb-1">Póliza</label>
                
                <!-- Indicador de carga para las pólizas -->
                <div v-if="loadingPolicies" class="flex items-center text-gray-500 py-2 px-4 border border-gray-300 rounded-md">
                  <div class="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-blue-600 mr-2"></div>
                  <span>Cargando pólizas disponibles...</span>
                </div>
                
                <!-- Mensaje si no hay pólizas -->
                <div v-else-if="availablePolicies.length === 0" class="bg-yellow-50 border-l-4 border-yellow-400 p-4 mb-4">
                  <p class="text-yellow-800">
                    No hay pólizas disponibles. Contacte con un administrador.
                  </p>
                  <button 
                    @click="fetchPolicies" 
                    class="mt-2 px-3 py-1 text-xs bg-yellow-100 text-yellow-800 rounded-md hover:bg-yellow-200"
                  >
                    Recargar Pólizas
                  </button>
                </div>
                
                <!-- Lista de pólizas disponibles -->
                <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-3 mt-2">
                  <div 
                    v-for="policy in availablePolicies" 
                    :key="policy.idPolicy"
                    @click="selectPolicy(policy.idPolicy)"
                    class="cursor-pointer border rounded-lg p-3 transition-colors"
                    :class="selectedUser.policyId === policy.idPolicy ? 
                      'bg-blue-50 border-blue-500' : 
                      'border-gray-300 hover:bg-gray-50'"
                  >
                    <div class="flex justify-between items-center">
                      <div>
                        <div class="font-semibold">
                          {{ policy.percentage }}% 
                          <span class="text-sm font-normal text-gray-600">
                            <span v-if="policy.enabled === 0" class="text-red-500">(Inactiva)</span>
                          </span>
                        </div>
                        <div class="text-sm text-gray-600">Vence: {{ formatDate(policy.expDate) }}</div>
                      </div>
                      <div class="font-medium">
                        {{ formatPrice(policy.cost) }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="flex justify-end mt-6">
              <button
                @click="saveUserProfile"
                class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md"
                :disabled="loading"
              >
                {{ loading ? 'Guardando...' : 'Guardar cambios' }}
              </button>
            </div>
          </div>
          
          <div v-if="activeTab === 'services'">
            <h3 class="text-lg font-semibold mb-4">Servicios cubiertos por el seguro</h3>
            
            <div v-if="loading" class="text-center py-6">
              <p>Cargando transacciones...</p>
            </div>
            
            <div v-else-if="filteredTransactions.length === 0" class="bg-gray-100 p-6 text-center rounded-md">
              <p>No se encontraron transacciones completadas para este usuario.</p>
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
                  <tr v-for="transaction in filteredTransactions" :key="transaction.idTransaction" class="hover:bg-gray-50">
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
            <h3 class="text-lg font-semibold mb-4">Servicios del hospital</h3>
            
            <div v-if="!hospitalUserDetails" class="bg-yellow-100 p-4 rounded-md mb-4">
              <p>No se encontró información de este usuario en el sistema del hospital.</p>
            </div>
            
            <div v-else class="bg-blue-50 p-4 rounded-md mb-4">
              <h4 class="font-semibold">Datos del usuario en el hospital:</h4>
              <p><strong>ID:</strong> {{ hospitalUserDetails._id }}</p>
              <p><strong>Nombre:</strong> {{ hospitalUserDetails.name || hospitalUserDetails.username }}</p>
              <p><strong>Email:</strong> {{ hospitalUserDetails.email }}</p>
            </div>
            
            <div v-if="hospitalServices.length === 0" class="bg-gray-100 p-6 text-center rounded-md">
              <p>No se encontraron servicios del hospital para este usuario.</p>
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
                    <td class="px-4 py-3 whitespace-nowrap">Q{{ service.total?.toFixed(2) || 'N/A' }}</td>
                    <td class="px-4 py-3">{{ service.description }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template> 