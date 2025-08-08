<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import type { Ref } from "vue";
import axios from "axios";
import router from "../router";
import { getInsuranceApiUrl } from "../utils/api";
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
  password: string;
  policy?: any; // Assuming policy is optional
}

// Estado
const user: Ref<User | null> = ref(null);
const loading: Ref<boolean> = ref(true);
const saving: Ref<boolean> = ref(false);
const error: Ref<string> = ref("");
const success: Ref<boolean> = ref(false);
const ip = import.meta.env.VITE_IP;

// Datos adicionales según el rol
const specialtyArea: Ref<string> = ref(""); // Para médicos
const licenseNumber: Ref<string> = ref(""); // Para médicos o farmacéuticos
const hospitalName: Ref<string> = ref(""); // Para personal médico
const insuranceNumber: Ref<string> = ref(""); // Para pacientes
const allergies: Ref<string> = ref(""); // Para pacientes
const medicalHistory: Ref<string> = ref(""); // Para pacientes
const systemAccess: Ref<string> = ref(""); // Para usuarios de interconexiones

// Determinar qué campos son obligatorios según el rol
const requiredFields = computed(() => {
  if (!user.value) return [];
  
  const fields = ["name", "email", "cui", "phone", "address", "birthDate"];
  
  switch (user.value.role) {
    case 'ADMIN':
      return [...fields, "licenseNumber"];
    case 'EMPLOYEE':
      return [...fields, "hospitalName"];
    case 'PATIENT':
      return [...fields, "insuranceNumber", "allergies"];
    case 'INTERCONNECTION':
      return [...fields, "systemAccess"];
    default:
      return fields;
  }
});

// Verificar si hay campos obligatorios pendientes
const hasMissingFields = computed(() => {
  if (!user.value) return true;
  
  // Verificar campos básicos
  for (const field of requiredFields.value) {
    if (field in user.value && !user.value[field]) {
      return true;
    }
    
    // Verificar campos adicionales específicos del rol
    switch (field) {
      case 'licenseNumber':
        if (!licenseNumber.value) return true;
        break;
      case 'specialtyArea':
        if (!specialtyArea.value) return true;
        break;
      case 'hospitalName':
        if (!hospitalName.value) return true;
        break;
      case 'insuranceNumber':
        if (!insuranceNumber.value) return true;
        break;
      case 'allergies':
        if (!allergies.value) return true;
        break;
      case 'medicalHistory':
        if (!medicalHistory.value) return true;
        break;
      case 'systemAccess':
        if (!systemAccess.value) return true;
        break;
    }
  }
  
  return false;
});

// Cargar datos del usuario
const loadUserData = async () => {
  try {
    loading.value = true;
    
    // Obtener el usuario de localStorage
    const storedUser = localStorage.getItem("user");
    if (!storedUser) {
      router.push("/login");
      return;
    }
    
    user.value = JSON.parse(storedUser);
    
    // Cargar información adicional si está disponible
    if (user.value.idUser) {
      const response = await axios.get(getInsuranceApiUrl(`/users/${user.value.idUser}`));
      if (response.data) {
        user.value = { ...user.value, ...response.data };
        
        // Actualizar el localStorage con los datos más recientes
        localStorage.setItem("user", JSON.stringify(user.value));
      }
    }
  } catch (err) {
    console.error("Error al cargar datos del usuario:", err);
    error.value = "No se pudieron cargar tus datos. Por favor intenta nuevamente.";
  } finally {
    loading.value = false;
  }
};

// Guardar el perfil completado
const saveProfile = async () => {
  if (hasMissingFields.value) {
    error.value = "Por favor completa todos los campos obligatorios.";
    return;
  }
  
  try {
    saving.value = true;
    error.value = "";
    
    // Crear objeto solo con los datos básicos que el backend puede manejar
    const updatedData = {
      idUser: user.value?.idUser,
      name: user.value?.name,
      cui: user.value?.cui,
      phone: user.value?.phone,
      email: user.value?.email,
      address: user.value?.address,
      birthDate: user.value?.birthDate,
      role: user.value?.role,
      enabled: user.value?.enabled,
      password: user.value?.password,
      // Incluir policy si existe en el objeto original
      policy: user.value?.policy,
      // No incluir profile_data ya que el backend no lo soporta
    };
    
    // Enviar datos actualizados al servidor
    const response = await axios.put(getInsuranceApiUrl(`/users/${user.value?.idUser}`), updatedData);
    
    if (response.status === 200) {
      // Actualizar el usuario en localStorage pero conservando los datos de perfil localmente
      const updatedUserWithProfile = {
        ...response.data,
        // Marcar el perfil como completado para evitar redirecciones en bucle
        profile_completed: true,
        profile_data: {
          specialtyArea: specialtyArea.value,
          licenseNumber: licenseNumber.value,
          hospitalName: hospitalName.value,
          insuranceNumber: insuranceNumber.value,
          allergies: allergies.value,
          medicalHistory: medicalHistory.value,
          systemAccess: systemAccess.value
        }
      };
      
      localStorage.setItem("user", JSON.stringify(updatedUserWithProfile));
      
      // Mostrar mensaje de éxito
      success.value = true;
      
      console.log("Perfil actualizado con éxito, redirigiendo en 2 segundos...");
      
      // Redirigir al dashboard después de 2 segundos de manera más explícita
      setTimeout(() => {
        console.log("Ejecutando redirección a /home");
        window.location.href = "/home";
      }, 2000);
    }
  } catch (err) {
    console.error("Error al guardar perfil:", err);
    error.value = "No se pudo guardar tu perfil. Por favor intenta nuevamente.";
  } finally {
    saving.value = false;
  }
};

// Inicializar
onMounted(() => {
  loadUserData();
});
</script>

<template>
  <div class="min-h-screen bg-gray-100 py-6 flex flex-col justify-center sm:py-12">
    <div class="relative py-3 sm:max-w-xl sm:mx-auto">
      <div class="relative px-4 py-10 bg-white mx-8 md:mx-0 shadow rounded-3xl sm:p-10">
        <div v-if="loading" class="text-center">
          <p>Cargando datos...</p>
        </div>
        
        <div v-else-if="success" class="text-center">
          <div class="mb-4 text-green-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 class="text-xl font-semibold mb-2">¡Perfil completado!</h2>
          <p class="text-gray-600">Serás redirigido al inicio en unos momentos...</p>
        </div>
        
        <div v-else>
          <div class="max-w-md mx-auto">
            <div class="text-center">
              <h1 class="text-2xl font-semibold text-gray-900">Completa tu perfil</h1>
              <p class="mt-2 text-gray-600">
                Para continuar usando el sistema, por favor completa los siguientes campos obligatorios.
              </p>
            </div>
            
            <div class="mt-8 space-y-6">
              <!-- Campos básicos -->
              <div>
                <label for="name" class="block text-sm font-medium text-gray-700">Nombre Completo</label>
                <input 
                  id="name" 
                  v-model="user.name" 
                  type="text" 
                  required 
                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                />
              </div>
              
              <div>
                <label for="email" class="block text-sm font-medium text-gray-700">Email</label>
                <input 
                  id="email" 
                  v-model="user.email" 
                  type="email" 
                  required 
                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                />
              </div>
              
              <div>
                <label for="phone" class="block text-sm font-medium text-gray-700">Teléfono</label>
                <input 
                  id="phone" 
                  v-model="user.phone" 
                  type="text" 
                  required 
                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                />
              </div>
              
              <div>
                <label for="address" class="block text-sm font-medium text-gray-700">Dirección</label>
                <input 
                  id="address" 
                  v-model="user.address" 
                  type="text" 
                  required 
                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                />
              </div>
              
              <!-- Campos específicos según el rol -->
              <div v-if="user.role === 'ADMIN' || user.role === 'EMPLOYEE'">
                <label for="licenseNumber" class="block text-sm font-medium text-gray-700">Número de Licencia</label>
                <input 
                  id="licenseNumber" 
                  v-model="licenseNumber" 
                  type="text" 
                  required 
                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                />
              </div>
              
              <div v-if="user.role === 'EMPLOYEE'">
                <label for="hospitalName" class="block text-sm font-medium text-gray-700">Nombre del Hospital</label>
                <input 
                  id="hospitalName" 
                  v-model="hospitalName" 
                  type="text" 
                  required 
                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                />
              </div>
              
              <div v-if="user.role === 'PATIENT'">
                <label for="insuranceNumber" class="block text-sm font-medium text-gray-700">Número de Seguro</label>
                <input 
                  id="insuranceNumber" 
                  v-model="insuranceNumber" 
                  type="text" 
                  required 
                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                />
              </div>
              
              <div v-if="user.role === 'PATIENT'">
                <label for="allergies" class="block text-sm font-medium text-gray-700">Alergias</label>
                <textarea 
                  id="allergies" 
                  v-model="allergies" 
                  required 
                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                  rows="3"
                ></textarea>
              </div>
              
              <div v-if="user.role === 'INTERCONNECTION'">
                <label for="systemAccess" class="block text-sm font-medium text-gray-700">Sistema de Acceso</label>
                <input 
                  id="systemAccess" 
                  v-model="systemAccess" 
                  type="text" 
                  required 
                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                />
              </div>
              
              <div v-if="error" class="text-red-500 text-sm">{{ error }}</div>
              
              <div>
                <button 
                  @click="saveProfile" 
                  :disabled="saving || hasMissingFields" 
                  class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                  :class="{ 'opacity-50 cursor-not-allowed': saving || hasMissingFields }"
                >
                  <span v-if="saving">Guardando...</span>
                  <span v-else>Guardar y Continuar</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Estilos adicionales si son necesarios */
</style> 