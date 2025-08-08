<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import type { Ref } from "vue";
import { getInsuranceApiUrl } from "../../utils/api";
import axios, { type AxiosResponse } from "axios";
import router from "../../router";
import eventBus from '../../eventBus';

interface PolicyData {
  idPolicy: number;
  percentage: number;
  creationDate: string;
  expDate: string;
  cost: number;
  enabled: number;
}

interface RegisterData {
  name: string;
  cui: number;
  phone: string;
  email: string;
  address: string;
  birthDate: string;
  role: string;
  policy: {
    idPolicy: number;
  };
  enabled: number;
  password: string;
}

const name: Ref<string> = ref<string>("");
const cui: Ref<string> = ref<string>("");
const phone: Ref<string> = ref<string>("");
const email: Ref<string> = ref<string>("");
const address: Ref<string> = ref<string>("");
const birthDate: Ref<string> = ref<string>("");
const password: Ref<string> = ref<string>("");
const confirmPassword: Ref<string> = ref<string>("");
const selectedPolicyId: Ref<number | null> = ref<number | null>(null);
const directActivation: Ref<boolean> = ref<boolean>(true); // Activar directamente
const loading: Ref<boolean> = ref<boolean>(false);
const error: Ref<string> = ref<string>("");
const success: Ref<boolean> = ref<boolean>(false);
const successMessage: Ref<string> = ref<string>("");
const registeredUserId: Ref<number | null> = ref<number | null>(null);
const availablePolicies: Ref<PolicyData[]> = ref<PolicyData[]>([]);
const loadingPolicies: Ref<boolean> = ref<boolean>(true);
const ip = import.meta.env.VITE_IP;

// Devuelve la política seleccionada
const selectedPolicy = computed(() => {
  if (!selectedPolicyId.value) return null;
  return availablePolicies.value.find(policy => policy.idPolicy === selectedPolicyId.value) || null;
});

// Formatear precio
const formatPrice = (price: number): string => {
  return `Q${price.toFixed(2)}`;
};

// Cargar pólizas disponibles
const fetchPolicies = async () => {
  try {
    loadingPolicies.value = true;
    error.value = "";
    console.log(`Intentando obtener pólizas de: ${getInsuranceApiUrl("/policy")}`);
    
    const response = await axios.get(getInsuranceApiUrl("/policy"));
    console.log("Respuesta completa de pólizas:", response.data);
    
    if (!response.data || response.data.length === 0) {
      console.warn("No se encontraron pólizas en la respuesta. Intentando crear pólizas estándar...");
      await createStandardPolicies();
      // Intentar obtener las pólizas de nuevo después de crearlas
      const newResponse = await axios.get(getInsuranceApiUrl("/policy"));
      console.log("Nuevas pólizas después de crear:", newResponse.data);
      
      if (!newResponse.data || newResponse.data.length === 0) {
        error.value = "No se pudieron crear pólizas. Contacte al administrador.";
        return;
      }
      
      // Usar todas las pólizas sin filtrar
      availablePolicies.value = newResponse.data;
      console.log("Usando todas las pólizas disponibles:", availablePolicies.value);
    } else {
      // Usar todas las pólizas sin filtrar
      availablePolicies.value = response.data;
      // Mostrar info de cada póliza para depuración
      availablePolicies.value.forEach(policy => {
        console.log(`Póliza ID ${policy.idPolicy}, porcentaje: ${policy.percentage}, activa: ${policy.enabled === 1 ? 'Sí' : 'No'}`);
      });
    }
    
    console.log("Pólizas disponibles:", availablePolicies.value);
    
    // Si hay pólizas disponibles, seleccionar la primera por defecto
    if (availablePolicies.value.length > 0) {
      selectedPolicyId.value = availablePolicies.value[0].idPolicy;
      console.log(`Seleccionada póliza ID: ${selectedPolicyId.value}`);
    } else {
      console.warn("No hay pólizas disponibles");
      error.value = "No hay pólizas disponibles en el sistema. Contacte al administrador.";
    }
  } catch (err: any) {
    console.error("Error al cargar pólizas:", err);
    if (err.response) {
      console.error("Error de respuesta del servidor:", err.response.status, err.response.data);
      error.value = `Error al cargar pólizas: ${err.response.status} - ${err.response.data?.message || 'Error del servidor'}`;
    } else if (err.request) {
      console.error("No se recibió respuesta del servidor");
      error.value = "No se pudo conectar con el servidor. Verifique que el servidor esté funcionando.";
    } else {
      console.error("Error:", err.message);
      error.value = `Error al cargar pólizas: ${err.message}`;
    }
  } finally {
    loadingPolicies.value = false;
  }
};

// Crear pólizas estándar (70% y 90%)
const createStandardPolicies = async () => {
  try {
    // Crear póliza básica (70%)
    await createStandardPolicy(70, 300);
    
    // Crear póliza premium (90%)
    await createStandardPolicy(90, 400);
    
    return true;
  } catch (err) {
    console.error("Error al crear pólizas estándar:", err);
    return false;
  }
};

// Crear una póliza estándar
const createStandardPolicy = async (percentage: number, cost: number) => {
  try {
    const today = new Date();
    const expDate = new Date(today);
    expDate.setFullYear(today.getFullYear() + 1);
    
    const policyData = {
      percentage: percentage,
      creationDate: today.toISOString().split('T')[0],
      expDate: expDate.toISOString().split('T')[0],
      cost: cost,
      enabled: 1
    };
    
    const response = await axios.post(getInsuranceApiUrl("/policy"), policyData);
    console.log(`Póliza ${percentage}% creada:`, response.data);
    return response.data;
  } catch (err) {
    console.error(`Error al crear póliza ${percentage}%:`, err);
    throw err;
  }
};

// Generar contraseña aleatoria
const generateRandomPassword = (length = 10): string => {
  const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  let password = "";
  for (let i = 0; i < length; i++) {
    const randomIndex = Math.floor(Math.random() * charset.length);
    password += charset.charAt(randomIndex);
  }
  return password;
};

// Enviar email de bienvenida con datos de acceso
const sendWelcomeEmail = async (userEmail: string, userName: string, userPassword: string): Promise<boolean> => {
  try {
    const policy = selectedPolicy.value;
    if (!policy) return false;
    
    await axios.post(getInsuranceApiUrl("/notifications/email"), {
      to: userEmail,
      subject: "Bienvenido a Ensurance - Datos de Acceso",
      body: `
        <h2>¡Bienvenido a Ensurance, ${userName}!</h2>
        <p>Tu cuenta ha sido creada exitosamente con una póliza del ${policy.percentage}% de cobertura.</p>
        <p>Aquí están tus datos de acceso:</p>
        <ul>
          <li><strong>Email:</strong> ${userEmail}</li>
          <li><strong>Contraseña temporal:</strong> ${userPassword}</li>
        </ul>
        <p>Te recomendamos cambiar tu contraseña la primera vez que inicies sesión.</p>
        <p>Atentamente,<br>El equipo de Ensurance</p>
      `
    });
    
    return true;
  } catch (error) {
    console.error("Error al enviar email:", error);
    return false;
  }
};

// Registrar cliente
const registerClient = async () => {
  try {
    // Validar si hay una póliza seleccionada
    if (!selectedPolicyId.value) {
      error.value = "Debe seleccionar una póliza";
      return;
    }
    
    // Validar contraseñas si no se generan automáticamente
    if (!password.value) {
      password.value = generateRandomPassword();
      confirmPassword.value = password.value;
    } else if (password.value !== confirmPassword.value) {
      error.value = "Las contraseñas no coinciden";
      return;
    }

    // Validar CUI
    const cuiNumber = parseInt(cui.value);
    if (isNaN(cuiNumber)) {
      error.value = "El CUI debe ser un número";
      return;
    }

    loading.value = true;
    error.value = "";

    // Datos usuario
    const birthDateObj = new Date(birthDate.value);
    
    const registerData: RegisterData = {
      name: name.value,
      cui: cuiNumber,
      phone: phone.value,
      email: email.value,
      address: address.value,
      birthDate: birthDateObj.toISOString().split('T')[0],
      role: "patient", // Rol paciente por defecto
      policy: {
        idPolicy: selectedPolicyId.value
      },
      enabled: directActivation.value ? 1 : 0, // Activar según configuración
      password: password.value
    };

    const response: AxiosResponse<any> = await axios.post(
      getInsuranceApiUrl("/users"),
      registerData
    );

    if (response.status === 201 && response.data && response.data.idUser) {
      registeredUserId.value = response.data.idUser;
      
      // Enviar email con credenciales
      await sendWelcomeEmail(email.value, name.value, password.value);
      
      success.value = true;
      successMessage.value = directActivation.value
        ? "Cliente registrado y activado exitosamente. Se han enviado las credenciales de acceso por correo electrónico."
        : "Cliente registrado exitosamente. Será activado por un administrador.";
      
      // Limpiar formulario
      resetForm();
    } else {
      error.value = "Error en el registro. Por favor intente de nuevo.";
    }
  } catch (err: any) {
    error.value =
      err.response?.data?.message || "Error de conexión. Por favor intente de nuevo.";
    console.error(err);
  } finally {
    loading.value = false;
  }
};

// Limpiar formulario
const resetForm = () => {
  name.value = "";
  cui.value = "";
  phone.value = "";
  email.value = "";
  address.value = "";
  birthDate.value = "";
  password.value = "";
  confirmPassword.value = "";
  
  // No reiniciar la póliza seleccionada para mantener la última selección
};

// Generar contraseña aleatoria
const generatePassword = () => {
  password.value = generateRandomPassword();
  confirmPassword.value = password.value;
};

// Inicializar
onMounted(() => {
  fetchPolicies();
  
  // Verificar si el usuario es empleado o administrador
  const user = JSON.parse(localStorage.getItem("user") || "null");
  if (!user || (user.role !== "employee" && user.role !== "admin")) {
    router.push("/home");
  }
});
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold mb-6">Registrar Nuevo Cliente</h1>
    
    <!-- Mensaje de éxito -->
    <div v-if="success" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-6">
      <p class="font-semibold">¡Cliente registrado exitosamente!</p>
      <p>{{ successMessage }}</p>
      <button 
        @click="success = false"
        class="mt-3 bg-green-500 hover:bg-green-600 text-white py-2 px-4 rounded"
      >
        Registrar otro cliente
      </button>
    </div>
    
    <!-- Formulario de registro -->
    <div v-if="!success" class="bg-white shadow-md rounded-lg p-6">
      <form @submit.prevent="registerClient" class="space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Datos personales -->
          <div class="space-y-6">
            <h2 class="text-lg font-semibold border-b pb-2">Datos Personales</h2>
            
            <div>
              <label for="name" class="block text-sm font-medium text-gray-700">Nombre Completo</label>
              <input
                id="name"
                v-model="name"
                type="text"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="Nombre completo del cliente"
              />
            </div>
            
            <div>
              <label for="cui" class="block text-sm font-medium text-gray-700">CUI/DPI</label>
              <input
                id="cui"
                v-model="cui"
                type="text"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="12345678901"
              />
            </div>
            
            <div>
              <label for="birthDate" class="block text-sm font-medium text-gray-700">Fecha de Nacimiento</label>
              <input
                id="birthDate"
                v-model="birthDate"
                type="date"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>
            
            <div>
              <label for="phone" class="block text-sm font-medium text-gray-700">Teléfono</label>
              <input
                id="phone"
                v-model="phone"
                type="text"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="+502 12345678"
              />
            </div>
            
            <div>
              <label for="email" class="block text-sm font-medium text-gray-700">Email</label>
              <input
                id="email"
                v-model="email"
                type="email"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="cliente@email.com"
              />
            </div>
            
            <div>
              <label for="address" class="block text-sm font-medium text-gray-700">Dirección</label>
              <input
                id="address"
                v-model="address"
                type="text"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="Dirección completa"
              />
            </div>
          </div>
          
          <!-- Datos de póliza y cuenta -->
          <div class="space-y-6">
            <h2 class="text-lg font-semibold border-b pb-2">Póliza y Cuenta</h2>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Seleccionar Póliza</label>
              
              <!-- Indicador de carga para las pólizas -->
              <div v-if="loadingPolicies" class="flex items-center text-gray-500 py-2 px-4 border border-gray-300 rounded-md">
                <div class="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-indigo-600 mr-2"></div>
                <span>Cargando pólizas disponibles...</span>
              </div>
              
              <!-- Mensaje de error -->
              <div v-else-if="error && availablePolicies.length === 0" class="bg-red-50 border-l-4 border-red-400 p-4 mb-4">
                <div class="flex">
                  <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
                    </svg>
                  </div>
                  <div class="ml-3">
                    <p class="text-sm text-red-700">{{ error }}</p>
                    <p class="mt-2 text-sm text-red-700">Por favor contacte con un administrador o intente nuevamente más tarde.</p>
                    <button 
                      @click="fetchPolicies" 
                      class="mt-2 px-3 py-1 text-xs bg-red-100 text-red-800 rounded-md hover:bg-red-200"
                    >
                      Reintentar
                    </button>
                  </div>
                </div>
              </div>
              
              <!-- Mensaje informativo sobre pólizas inactivas -->
              <div v-if="availablePolicies.some(p => p.enabled === 0)" class="bg-yellow-50 border-l-4 border-yellow-400 p-3 mb-3 text-sm">
                <p class="text-yellow-800">
                  <strong>Atención:</strong> Algunas pólizas están inactivas (marcadas en rojo) pero aún pueden ser seleccionadas.
                </p>
              </div>
              
              <!-- Mensaje si no hay pólizas -->
              <div v-if="availablePolicies.length === 0" class="bg-yellow-50 border-l-4 border-yellow-400 p-4 mb-4">
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
              <div v-if="availablePolicies.length > 0" class="grid grid-cols-1 gap-4">
                <div 
                  v-for="policy in availablePolicies" 
                  :key="policy.idPolicy"
                  @click="selectedPolicyId = policy.idPolicy"
                  class="cursor-pointer border rounded-lg p-4 transition-colors"
                  :class="selectedPolicyId === policy.idPolicy ? 
                    'bg-blue-50 border-blue-500' : 
                    'border-gray-300 hover:bg-gray-50'"
                >
                  <div class="flex justify-between items-center">
                    <div>
                      <div class="font-semibold text-lg">
                        {{ policy.percentage }}% 
                        <span class="text-sm font-normal text-gray-600">
                          <span v-if="policy.enabled === 0" class="text-red-500">(Inactiva)</span>
                        </span>
                      </div>
                      <div class="text-sm text-gray-600">Válida hasta: {{ policy.expDate }}</div>
                      <div class="text-xs text-gray-500 mt-1">ID: {{ policy.idPolicy }}</div>
                    </div>
                    <div class="font-medium text-lg">
                      {{ formatPrice(policy.cost) }} <span class="text-sm text-gray-600">/ mes</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="mt-2 text-sm text-gray-600">
                <p>Selecciona el plan de seguro que desea adquirir el cliente.</p>
              </div>
            </div>
            
            <div class="pt-4">
              <div class="flex items-center mb-4">
                <input
                  id="directActivation"
                  v-model="directActivation"
                  type="checkbox"
                  class="h-4 w-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
                />
                <label for="directActivation" class="ml-2 block text-sm text-gray-700">
                  Activar cuenta inmediatamente
                </label>
              </div>
              <p class="text-sm text-gray-600">Si esta opción está marcada, el cliente podrá acceder al sistema inmediatamente.</p>
            </div>
            
            <div class="pt-4">
              <label class="block text-sm font-medium text-gray-700 mb-2">Contraseña</label>
              <div class="space-y-3">
                <div>
                  <div class="flex items-center justify-between">
                    <label for="password" class="block text-sm text-gray-700">Contraseña</label>
                    <button
                      type="button"
                      @click="generatePassword"
                      class="text-xs text-indigo-600 hover:text-indigo-900"
                    >
                      Generar contraseña automática
                    </button>
                  </div>
                  <input
                    id="password"
                    v-model="password"
                    type="text"
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                    placeholder="Contraseña o generar automáticamente"
                  />
                </div>
                
                <div v-if="password">
                  <label for="confirmPassword" class="block text-sm text-gray-700">Confirmar Contraseña</label>
                  <input
                    id="confirmPassword"
                    v-model="confirmPassword"
                    type="text"
                    class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                  />
                </div>
                
                <p class="text-sm text-gray-600">
                  La contraseña será enviada al correo electrónico del cliente.
                </p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Mensaje de error -->
        <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mt-4">
          <p>{{ error }}</p>
        </div>
        
        <!-- Botones -->
        <div class="flex justify-end space-x-3 pt-4">
          <button
            type="button"
            @click="router.push('/home')"
            class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            Cancelar
          </button>
          <button
            type="submit"
            class="px-4 py-2 border border-transparent rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
            :disabled="loading || availablePolicies.length === 0"
          >
            <span v-if="loading">Registrando...</span>
            <span v-else>Registrar Cliente</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template> 