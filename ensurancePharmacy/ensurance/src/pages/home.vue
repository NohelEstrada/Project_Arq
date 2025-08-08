<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();
const user = ref<any>(null);
const isAdmin = ref(false);
const isEmployee = ref(false);
const isLoading = ref(true);
const policyDetails = ref<any>(null);
const error = ref<string | null>(null);
const ip = import.meta.env.VITE_IP;
import { getInsuranceApiUrl } from "../utils/api";
const fetchUserPolicyDetails = async () => {
  if (!user.value || !user.value.policy || !user.value.policy.idPolicy) {
    isLoading.value = false;
    return;
  }
  
  try {
    const response = await axios.get(getInsuranceApiUrl(`/policy?id=${user.value.policy.idPolicy}`));
    if (response.data) {
      policyDetails.value = response.data;
    }
  } catch (err) {
    console.error("Error al cargar detalles de la póliza:", err);
    error.value = "No se pudieron cargar los detalles de tu póliza.";
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  // Obtener datos de usuario del localStorage
  const userData = localStorage.getItem("user");
  if (userData) {
    try {
      user.value = JSON.parse(userData);
      isAdmin.value = user.value.role === "admin";
      isEmployee.value = user.value.role === "employee";
      
      // Cargar detalles de la póliza
      fetchUserPolicyDetails();
    } catch (e) {
      console.error("Error al parsear datos de usuario:", e);
      isLoading.value = false;
    }
  } else {
    isLoading.value = false;
  }
});

// Formatear precio
const formatPrice = (price) => {
  if (!price) return "N/A";
  return `Q${price.toFixed(2)}`;
};

// Formatear fecha
const formatDate = (dateString) => {
  if (!dateString) return "N/A";
  const date = new Date(dateString);
  return date.toLocaleDateString();
};
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6">Bienvenido a Ensurance</h1>
    
    <!-- Información de usuario y póliza -->
    <div v-if="user" class="bg-white shadow-md rounded-lg overflow-hidden mb-8">
      <div class="p-6 bg-indigo-50 border-b border-indigo-100">
        <h2 class="text-xl font-semibold mb-2">{{ user.name }}</h2>
        <p class="text-gray-600 mb-1">Email: {{ user.email }}</p>
        <p class="text-gray-600">Rol: {{ user.role || 'Sin rol asignado' }}</p>
      </div>
      
      <!-- Detalles de la póliza -->
      <div v-if="user.role === 'patient'" class="p-6">
        <h3 class="text-lg font-semibold mb-3">Tu Póliza de Seguro</h3>
        
        <div v-if="isLoading" class="flex items-center space-x-3 text-gray-500">
          <div class="w-4 h-4 rounded-full border-2 border-t-transparent border-indigo-500 animate-spin"></div>
          <span>Cargando detalles...</span>
        </div>
        
        <div v-else-if="error" class="text-red-600">
          {{ error }}
        </div>
        
        <div v-else-if="policyDetails" class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <div class="mb-4">
              <h4 class="text-sm font-medium text-gray-500 mb-1">Porcentaje de cobertura</h4>
              <div class="flex items-center">
                <div class="text-2xl font-bold mr-2">{{ policyDetails.percentage }}%</div>
                <div 
                  class="px-3 py-1 text-sm font-semibold rounded-full"
                  :class="{
                    'bg-blue-100 text-blue-800': policyDetails.percentage === 70,
                    'bg-green-100 text-green-800': policyDetails.percentage === 90,
                    'bg-purple-100 text-purple-800': policyDetails.percentage !== 70 && policyDetails.percentage !== 90
                  }"
                >
                  {{ policyDetails.percentage === 90 ? 'Premium' : policyDetails.percentage === 70 ? 'Básica' : 'Personalizada' }}
                </div>
              </div>
            </div>
            
            <div>
              <h4 class="text-sm font-medium text-gray-500 mb-1">Costo mensual</h4>
              <p class="text-lg font-semibold">{{ formatPrice(policyDetails.cost) }}</p>
            </div>
          </div>
          
          <div>
            <div class="mb-4">
              <h4 class="text-sm font-medium text-gray-500 mb-1">Fecha de creación</h4>
              <p>{{ formatDate(policyDetails.creationDate) }}</p>
            </div>
            
            <div>
              <h4 class="text-sm font-medium text-gray-500 mb-1">Fecha de expiración</h4>
              <p>{{ formatDate(policyDetails.expDate) }}</p>
            </div>
          </div>
          
          <div class="col-span-1 md:col-span-2 mt-2 bg-blue-50 p-4 rounded-lg">
            <h4 class="font-medium mb-2">¿Qué significa?</h4>
            <p class="text-sm text-gray-700">
              Tu póliza cubre el <strong>{{ policyDetails.percentage }}%</strong> de los gastos médicos en servicios y medicamentos aprobados 
              por Ensurance en los hospitales y farmacias con convenio.
            </p>
          </div>
        </div>
        
        <div v-else class="text-gray-500">
          No hay información disponible sobre tu póliza.
        </div>
      </div>
    </div>
    
    <!-- Catálogos disponibles para todos los usuarios -->
    <div class="bg-white shadow-md rounded-lg p-6 mb-8">
      <h2 class="text-xl font-semibold mb-4">Catálogos de Seguros</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div 
          @click="router.push('/catalog/insurance-services')" 
          class="bg-blue-50 p-4 rounded-lg cursor-pointer hover:bg-blue-100 transition-colors"
        >
          <h3 class="font-semibold mb-2">Servicios Cubiertos</h3>
          <p class="text-sm text-gray-600">Consulta todos los servicios cubiertos por el seguro y sus precios</p>
        </div>
        
        <div 
          @click="router.push('/catalog/hospitals')" 
          class="bg-green-50 p-4 rounded-lg cursor-pointer hover:bg-green-100 transition-colors"
        >
          <h3 class="font-semibold mb-2">Hospitales Aprobados</h3>
          <p class="text-sm text-gray-600">Consulta los hospitales que tienen convenio con el seguro</p>
        </div>
      </div>
    </div>
    
    <!-- Panel de empleado -->
    <div v-if="isEmployee" class="bg-white shadow-md rounded-lg p-6 mb-8">
      <h2 class="text-xl font-semibold mb-4">Panel de Empleado</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div 
          @click="router.push('/employee/register-client')" 
          class="bg-teal-100 p-4 rounded-lg cursor-pointer hover:bg-teal-200 transition-colors"
        >
          <h3 class="font-semibold mb-2">Registrar Cliente</h3>
          <p class="text-sm text-gray-600">Registrar un nuevo cliente con su póliza</p>
        </div>
      </div>
    </div>
    
    <!-- Panel de administrador -->
    <div v-if="isAdmin" class="bg-white shadow-md rounded-lg p-6 mb-8">
      <h2 class="text-xl font-semibold mb-4">Panel de Administración</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div 
          @click="router.push('/admin/users')" 
          class="bg-blue-100 p-4 rounded-lg cursor-pointer hover:bg-blue-200 transition-colors"
        >
          <h3 class="font-semibold mb-2">Gestión de Usuarios</h3>
          <p class="text-sm text-gray-600">Administrar usuarios, asignar roles y activar cuentas</p>
        </div>
        
        <div 
          @click="router.push('/admin/insurance-services')" 
          class="bg-green-100 p-4 rounded-lg cursor-pointer hover:bg-green-200 transition-colors"
        >
          <h3 class="font-semibold mb-2">Catálogo de Servicios</h3>
          <p class="text-sm text-gray-600">Gestionar servicios cubiertos por el seguro</p>
        </div>
        
        <div 
          @click="router.push('/admin/hospital-services')" 
          class="bg-purple-100 p-4 rounded-lg cursor-pointer hover:bg-purple-200 transition-colors"
        >
          <h3 class="font-semibold mb-2">Hospitales y Servicios</h3>
          <p class="text-sm text-gray-600">Aprobar servicios para hospitales específicos</p>
        </div>
        
        <div 
          @click="router.push('/admin/policies')" 
          class="bg-amber-100 p-4 rounded-lg cursor-pointer hover:bg-amber-200 transition-colors"
        >
          <h3 class="font-semibold mb-2">Gestión de Pólizas</h3>
          <p class="text-sm text-gray-600">Administrar tipos de pólizas y porcentajes de cobertura</p>
        </div>
      </div>
    </div>
  </div>
</template>
