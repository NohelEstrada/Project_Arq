<script setup lang="ts">
import { ref, onMounted } from "vue";
import type { Ref } from "vue";
import axios, { type AxiosResponse } from "axios";
import router from "../router";
import eventBus from '../eventBus';
import { getInsuranceApiUrl } from "../utils/api";
interface PolicyData {
  idPolicy?: number;
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
const selectedPolicyType: Ref<number> = ref<number>(70); // Por defecto 70%
const loading: Ref<boolean> = ref<boolean>(false);
const error: Ref<string> = ref<string>("");
const success: Ref<boolean> = ref<boolean>(false);
const availablePolicies: Ref<PolicyData[]> = ref<PolicyData[]>([]);
const ip = import.meta.env.VITE_IP;

// Cargar pólizas disponibles
const fetchPolicies = async () => {
  try {
    const response = await axios.get(getInsuranceApiUrl("/policy"));
    // Filtrar solo pólizas activas
    availablePolicies.value = response.data.filter((policy: PolicyData) => policy.enabled === 1);
  } catch (error) {
    console.error("Error al cargar pólizas:", error);
  }
};

// Póliza según el tipo seleccionado
const createDefaultPolicy = (): PolicyData => {
  const today = new Date();
  const expDate = new Date(today);
  expDate.setFullYear(today.getFullYear() + 1);
  
  // Calcular el costo basado en el porcentaje seleccionado
  const cost = selectedPolicyType.value === 90 ? 400 : 300;
  
  return {
    percentage: selectedPolicyType.value,
    creationDate: today.toISOString().split('T')[0],
    expDate: expDate.toISOString().split('T')[0],
    cost: cost,
    enabled: 1
  };
}

// Verificar si ya existe una póliza del porcentaje seleccionado
const findExistingPolicy = (): PolicyData | undefined => {
  return availablePolicies.value.find(policy => 
    policy.percentage === selectedPolicyType.value && policy.enabled === 1
  );
};

// Crear póliza
const createPolicy = async (policyData: PolicyData): Promise<number | null> => {
  try {
    // Primero verificar si ya existe una póliza con el mismo porcentaje
    const existingPolicy = findExistingPolicy();
    if (existingPolicy && existingPolicy.idPolicy) {
      return existingPolicy.idPolicy;
    }
    
    // Si no existe, crear una nueva
    const response = await axios.post(getInsuranceApiUrl("/policy"), policyData);
    if (response.status === 201 && response.data && response.data.idPolicy) {
      return response.data.idPolicy;
    }
    return null;
  } catch (error) {
    console.error("Error:", error);
    return null;
  }
}

// Enviar email
const sendWelcomeEmail = async (userEmail: string, userName: string): Promise<boolean> => {
  try {
    await axios.post(getInsuranceApiUrl("/notifications/email"), {
      to: userEmail,
      subject: "Bienvenido a Ensurance",
      body: `Hola ${userName}, gracias por registrarte en nuestro sistema. Tu cuenta será revisada por un administrador para su activación. Has elegido una póliza con cobertura del ${selectedPolicyType.value}%.`
    });
    
    await axios.post(getInsuranceApiUrl("/notifications/email"), {
      to: userEmail,
      subject: "Nuevo registro de usuario - Notificación de administrador",
      body: `Notificación de administrador: El usuario ${userName} (${userEmail}) se ha registrado en el sistema con una póliza del ${selectedPolicyType.value}% y está pendiente de activación.`
    });
    
    console.log(`Email enviado: ${userEmail}`);
    return true;
  } catch (error) {
    console.error("Error:", error);
    return false;
  }
}

const register: () => Promise<void> = async (): Promise<void> => {
  try {
    // Validar contraseñas
    if (password.value !== confirmPassword.value) {
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

    // Crear póliza o usar existente
    const policyData = createDefaultPolicy();
    const policyId = await createPolicy(policyData);
    
    if (!policyId) {
      error.value = "Error al crear la póliza. Inténtelo de nuevo.";
      loading.value = false;
      return;
    }
    
    console.log("Póliza ID:", policyId);

    // Datos usuario
    const birthDateObj = new Date(birthDate.value);
    
    const registerData: RegisterData = {
      name: name.value,
      cui: cuiNumber,
      phone: phone.value,
      email: email.value,
      address: address.value,
      birthDate: birthDateObj.toISOString().split('T')[0],
      role: "",
      policy: {
        idPolicy: policyId
      },
      enabled: 0,
      password: password.value
    };

    console.log("Usuario:", registerData);

    const response: AxiosResponse<any> = await axios.post(
      getInsuranceApiUrl("/users"),
      registerData
    );

    console.log("Respuesta:", response.data);

    if (response.status === 201 && response.data && response.data.idUser) {
      await sendWelcomeEmail(email.value, name.value);
      success.value = true;
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

// Inicializar
onMounted(() => {
  fetchPolicies();
});
</script>

<template>
  <div class="card bg-white shadow-md rounded-lg p-6 max-w-md mx-auto">
    <h2 class="text-xl font-semibold mb-4">Registrar Nuevo Usuario</h2>

    <div v-if="success" class="mb-6 p-4 bg-green-100 text-green-800 rounded-md">
      <p class="font-semibold">¡Registro exitoso!</p>
      <p>Tu cuenta ha sido creada y está pendiente de activación por un administrador. Recibirás un correo con más información.</p>
      <p class="mt-2">Por favor, revisa tu correo electrónico para obtener instrucciones adicionales.</p>
      <div class="mt-4">
        <router-link to="/login" class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
          Ir a la página de inicio de sesión
        </router-link>
      </div>
    </div>

    <form v-if="!success" @submit.prevent="register" class="space-y-4">
      <div>
        <label for="name" class="block text-sm font-medium text-gray-700"
          >Nombre Completo</label
        >
        <input
          id="name"
          v-model="name"
          type="text"
          required
          class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          placeholder="Nombre Completo"
        />
      </div>

      <div>
        <label for="cui" class="block text-sm font-medium text-gray-700"
          >CUI/DPI</label
        >
        <input
          id="cui"
          v-model="cui"
          type="text"
          required
          class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          placeholder="12345678901"
        />
      </div>

      <div>
        <label for="phone" class="block text-sm font-medium text-gray-700"
          >Teléfono</label
        >
        <input
          id="phone"
          v-model="phone"
          type="text"
          required
          class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          placeholder="+502 12345678"
        />
      </div>

      <div>
        <label for="email" class="block text-sm font-medium text-gray-700"
          >Email</label
        >
        <input
          id="email"
          v-model="email"
          type="email"
          required
          class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          placeholder="tu@email.com"
        />
      </div>

      <div>
        <label for="address" class="block text-sm font-medium text-gray-700"
          >Dirección</label
        >
        <input
          id="address"
          v-model="address"
          type="text"
          required
          class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          placeholder="Dirección completa"
        />
      </div>

      <div>
        <label for="birthDate" class="block text-sm font-medium text-gray-700"
          >Fecha de Nacimiento</label
        >
        <input
          id="birthDate"
          v-model="birthDate"
          type="date"
          required
          class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
        />
      </div>
      
      <div>
        <label for="policyType" class="block text-sm font-medium text-gray-700">
          Tipo de Póliza
        </label>
        <div class="mt-1 grid grid-cols-2 gap-4">
          <div
            @click="selectedPolicyType = 70"
            class="cursor-pointer border rounded-md p-4 text-center transition-colors"
            :class="selectedPolicyType === 70 ? 'bg-blue-50 border-blue-500' : 'border-gray-300 hover:bg-gray-50'"
          >
            <div class="font-semibold text-lg">70%</div>
            <div class="text-sm text-gray-600">Cobertura Básica</div>
            <div class="mt-2 font-medium">Q300 / mes</div>
          </div>
          <div
            @click="selectedPolicyType = 90"
            class="cursor-pointer border rounded-md p-4 text-center transition-colors"
            :class="selectedPolicyType === 90 ? 'bg-green-50 border-green-500' : 'border-gray-300 hover:bg-gray-50'"
          >
            <div class="font-semibold text-lg">90%</div>
            <div class="text-sm text-gray-600">Cobertura Premium</div>
            <div class="mt-2 font-medium">Q400 / mes</div>
          </div>
        </div>
        <div class="mt-2 text-sm text-gray-600">
          <p>Selecciona el porcentaje de cobertura para tus gastos médicos. A mayor cobertura, mayor costo mensual.</p>
        </div>
      </div>

      <div>
        <label for="password" class="block text-sm font-medium text-gray-700"
          >Contraseña</label
        >
        <input
          id="password"
          v-model="password"
          type="password"
          required
          class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
        />
      </div>

      <div>
        <label for="confirmPassword" class="block text-sm font-medium text-gray-700"
          >Confirmar Contraseña</label
        >
        <input
          id="confirmPassword"
          v-model="confirmPassword"
          type="password"
          required
          class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
        />
      </div>

      <div class="bg-blue-50 p-3 rounded-md">
        <p class="text-sm text-blue-800">
          <strong>Nota:</strong> Después de registrarte, tu cuenta deberá ser activada por un administrador antes de poder iniciar sesión.
        </p>
      </div>

      <div v-if="error" class="text-red-500 text-sm">{{ error }}</div>

      <button
        type="submit"
        class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
        :disabled="loading"
      >
        <span v-if="loading">Registrando...</span>
        <span v-else>Registrarse</span>
      </button>

      <div class="text-center mt-4">
        <router-link to="/login" class="text-sm text-indigo-600 hover:text-indigo-800">
          ¿Ya tienes una cuenta? Inicia sesión
        </router-link>
      </div>
    </form>
  </div>
</template>

<style scoped>
.card {
  @apply transition-all duration-300;
}
</style> 