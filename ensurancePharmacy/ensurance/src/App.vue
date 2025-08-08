<script setup lang="ts">
import { ref, provide, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import eventBus from './eventBus';
import PortSelector from './components/PortSelector.vue';
import { loadPortConfiguration } from './utils/api';

const router = useRouter();
const isLoggedIn = ref(false);
const userProfile = ref<any>(null);
const showPortSelector = ref(false);

// Verificar si se debe mostrar el selector de puertos
const checkShowPortSelector = () => {
  // No mostrar si el usuario eligió recordar su elección
  const skipPortSelector = localStorage.getItem('skipPortSelector') === 'true';
  showPortSelector.value = !skipPortSelector;
};

const checkAuth = () => {
  const profile = JSON.parse(localStorage.getItem("user") || "null");
  isLoggedIn.value = profile !== null && profile !== "null";
  userProfile.value = profile;
  console.log("Usuario:", profile);
  console.log("Es admin:", isAdmin.value);
};

// Computed para verificar si el usuario es administrador
const isAdmin = computed(() => {
  return userProfile.value && userProfile.value.role === "admin";
});

// Inicialización
onMounted(() => {
  // Cargar configuración de puertos
  loadPortConfiguration();
  
  // Verificar autenticación inicial
  checkAuth();
  
  // Verificar si se debe mostrar el selector de puertos
  checkShowPortSelector();
  
  // Escuchar eventos de login y logout
  eventBus.on('login', () => {
    checkAuth();
  });
  
  eventBus.on('logout', () => {
    checkAuth();
  });
});

// Mostrar el selector de puertos de forma manual
const openPortSelector = () => {
  showPortSelector.value = true;
};

function logout() {
  localStorage.removeItem("user");
  isLoggedIn.value = false;
  userProfile.value = null;
  eventBus.emit('logout');
  router.push("/login");
}

function navigateToUsers() {
  router.push("/admin/users");
}

function navigateToServices() {
  router.push("/admin/insurance-services");
}

function navigateToHospitals() {
  router.push("/admin/hospital-services");
}

function navigateToHospitalServicesImport() {
  router.push("/admin/hospital-services-import");
}

function navigateToInsuranceServicesCatalog() {
  router.push("/catalog/insurance-services");
}

function navigateToHospitalsCatalog() {
  router.push("/catalog/hospitals");
}

function navigateToPolicies() {
  router.push("/admin/policies");
}

function navigateToRegisterClient() {
  router.push("/employee/register-client");
}

function navigateToAppointments() {
  router.push("/appointments");
}

// Computed para verificar si el usuario es empleado
const isEmployee = computed(() => {
  return userProfile.value && userProfile.value.role === "employee";
});

// Añadir nuevas funciones de navegación
function navigateToClientManagement() {
  router.push("/admin/client-management");
}

function navigateToDailyAppointments() {
  router.push("/admin/daily-appointments");
}

function navigateToPrescriptionApprovals() {
  router.push("/admin/prescription-approvals");
}

function navigateToConfiguration() {
  router.push("/admin/configuration");
}

function navigateToHospitalConfiguration() {
  router.push("/admin/hospital-configuration");
}

function navigateToUserServices() {
  router.push("/user-services");
}
</script>
<template>
  <header
    class="flex justify-between items-center px-8 py-4 bg-gray-50 shadow-sm"
  >
    <div class="text-2xl font-bold">Ensurance</div>
    <nav class="flex gap-4">
      <div v-if="!isLoggedIn">
        <router-link
          to="/login"
          class="px-4 py-2 me-2 bg-blue-600 text-white rounded hover:bg-blue-800 transition-colors"
        >
          Login
        </router-link>
        <router-link
          to="/register"
          class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-800 transition-colors"
        >
          Register
        </router-link>
      </div>
      <div v-if="isLoggedIn" class="flex items-center">
        <router-link
          to="/home"
          class="px-4 py-2 me-2 bg-blue-600 text-white rounded hover:bg-blue-800 transition-colors"
        >
          Home
        </router-link>
        
        <!-- Menú de catálogos para todos los usuarios -->
        <div class="relative mx-2 group">
          <button class="px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-800 transition-colors flex items-center">
            Catálogos ▼
          </button>
          <div class="absolute hidden group-hover:block bg-white mt-2 py-2 rounded shadow-lg z-10 w-64 right-0">
            <button 
              @click="navigateToInsuranceServicesCatalog" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Servicios Cubiertos
            </button>
            <button 
              @click="navigateToHospitalsCatalog" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Hospitales Aprobados
            </button>
          </div>
        </div>

        <!-- Enlace a Citas -->
        <button 
          @click="navigateToAppointments" 
          class="px-4 py-2 mx-2 bg-amber-600 text-white rounded hover:bg-amber-800 transition-colors"
        >
          Citas Médicas
        </button>
        
        <!-- Menú de empleado -->
        <div v-if="isEmployee || isAdmin" class="relative mx-2 group">
          <button class="px-4 py-2 bg-teal-600 text-white rounded hover:bg-teal-800 transition-colors flex items-center">
            Clientes ▼
          </button>
          <div class="absolute hidden group-hover:block bg-white mt-2 py-2 rounded shadow-lg z-10 w-64 right-0">
            <button 
              @click="navigateToRegisterClient" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Registrar Nuevo Cliente
            </button>
          </div>
        </div>
        
        <!-- Menú de administración para admins -->
        <div v-if="isAdmin" class="relative mx-2 group">
          <button class="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-800 transition-colors flex items-center">
            Admin ▼
          </button>
          <div class="absolute hidden group-hover:block bg-white mt-2 py-2 rounded shadow-lg z-10 w-64 right-0">
            <!-- Añadir opción para configurar puertos -->
            <button 
              @click="openPortSelector" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100 border-b border-gray-200"
            >
              Configurar Puertos API
            </button>
            
            <button 
              @click="navigateToUsers" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Gestión de Usuarios
            </button>
            <button 
              @click="navigateToServices" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Catálogo de Servicios
            </button>
            <button 
              @click="navigateToHospitals" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Hospitales y Servicios
            </button>
            <button 
              @click="navigateToHospitalConfiguration" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Configuración de Hospitales
            </button>
            <button 
              @click="navigateToHospitalServicesImport" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Importar Servicios Hospital
            </button>
            <button 
              @click="navigateToPolicies" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Gestión de Pólizas
            </button>
            <button 
              @click="navigateToClientManagement" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Gestión de Clientes
            </button>
            <button 
              @click="navigateToDailyAppointments" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Citas Diarias
            </button>
            <button 
              @click="navigateToPrescriptionApprovals" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Aprobaciones de Recetas
            </button>
            <button 
              @click="navigateToConfiguration" 
              class="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
            >
              Configuración del Sistema
            </button>
          </div>
        </div>
        
        <!-- Añadir enlace a servicios del usuario -->
        <button 
          @click="navigateToUserServices" 
          class="px-4 py-2 mx-2 bg-indigo-600 text-white rounded hover:bg-indigo-800 transition-colors"
        >
          Mis Servicios
        </button>
        
        <button
          @click="logout"
          class="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-800 transition-colors"
        >
          Logout
        </button>
      </div>
    </nav>
  </header>
  <main class="min-h-[calc(100vh-136px)] p-4">
    <RouterView />
  </main>
  <footer
    class="py-4 px-8 text-center bg-gray-50 mt-auto border-t border-gray-200"
  >
    <p>&copy; 2025 Ensurance. All rights reserved and lefts too.</p>
  </footer>
  
  <!-- Selector de puertos -->
  <PortSelector v-if="showPortSelector" />
</template>

<style>
.group:hover .group-hover\:block {
  display: block;
}
</style>
