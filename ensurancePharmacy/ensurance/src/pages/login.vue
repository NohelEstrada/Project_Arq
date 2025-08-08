<script setup lang="ts">
import { ref, onMounted } from "vue";
import type { Ref } from "vue";
import axios, { type AxiosResponse } from "axios";
import router from "../router";
import eventBus from '../eventBus';
import { checkMissingRequiredFields } from "../utils/profile-utils";
import { getInsuranceApiUrl } from "../utils/api";
// La prop msg es opcional
const props = defineProps<{ msg?: string }>();

interface LoginResponse {
  user: {
    id: number;
    name: string;
    email: string;
    role: string;
    enabled: number;
  };
  success: boolean;
  message: string;
}

const email: Ref<string> = ref<string>("");
const password: Ref<string> = ref<string>("");
const loading: Ref<boolean> = ref<boolean>(false);
const error: Ref<string> = ref<string>("");
const user: Ref<LoginResponse["user"] | null> = ref<
  LoginResponse["user"] | null
>(null);
const ip = import.meta.env.VITE_IP;
console.log("IP del servidor:", ip);

const login: () => Promise<void> = async (): Promise<void> => {
  try {
    loading.value = true;
    error.value = "";
    
    console.log("Intentando login con:", email.value);
    
    const response = await axios.post(getInsuranceApiUrl("/login"), {
      email: email.value,
      password: password.value,
    });
    
    console.log("Respuesta del servidor:", response.data);
    
    if (response.status === 200 && response.data) {
      // Verificar si ya existe información de perfil completado para este usuario
      const previousUserData = localStorage.getItem("user");
      let profileCompletedFlag = false;
      let profileData = null;
      
      if (previousUserData) {
        try {
          const prevUser = JSON.parse(previousUserData);
          if (prevUser && prevUser.email === response.data.email && prevUser.profile_completed) {
            profileCompletedFlag = true;
            profileData = prevUser.profile_data || null;
            console.log("Se encontró información de perfil completado para este usuario");
          }
        } catch (e) {
          console.error("Error al parsear datos de usuario previo:", e);
        }
      }
      
      // Combinar datos del servidor con datos de perfil previos si existen
      const userData = {
        ...response.data,
        ...(profileCompletedFlag ? { profile_completed: true } : {}),
        ...(profileData ? { profile_data: profileData } : {})
      };
      
      localStorage.setItem("user", JSON.stringify(userData));
      console.log("Usuario guardado en localStorage:", userData);
      console.log("Rol del usuario:", userData.role);
      console.log("Estado de activación:", userData.enabled);
      console.log("Estado de perfil completado:", userData.profile_completed);
      
      eventBus.emit('login');
      
      if (userData.enabled !== 1) {
        console.log("Redirigiendo a cuenta inactiva");
        router.push("/inactive-account");
      } else if (!profileCompletedFlag && checkMissingRequiredFields(userData)) {
        console.log("Redirigiendo a completar perfil");
        router.push("/profile-completion");
      } else {
        console.log("Redirigiendo a home");
        router.push("/home");
      }
    } else {
      error.value = "Error en el inicio de sesión. Por favor intente de nuevo.";
      console.error("Error en login:", response);
    }
  } catch (err: any) {
    console.error("Error en la petición:", err);
    
    if (err.response?.status === 401) {
      error.value = "Usuario o contraseña incorrectos";
    } else {
      error.value = "Error de conexión. Por favor intente de nuevo.";
    }
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  const profileStr = localStorage.getItem("user");
  console.log("Profile en localStorage:", profileStr);
  
  const profile = profileStr ? JSON.parse(profileStr) : null;
  
  if (profile && profile !== null && profile !== "null") {
    console.log("Usuario ya logueado, redirigiendo a home");
    router.push("/home");
  }
});
</script>

<template>
  <h1 class="text-2xl font-bold mb-6">{{ msg || "Bienvenido a Ensurance" }}</h1>

  <div
    v-if="!user"
    class="card bg-white shadow-md rounded-lg p-6 max-w-md mx-auto"
  >
    <h2 class="text-xl font-semibold mb-4">Login</h2>

    <form @submit.prevent="login" class="space-y-4">
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
          placeholder="your@email.com"
        />
      </div>

      <div>
        <label for="password" class="block text-sm font-medium text-gray-700"
          >Password</label
        >
        <input
          id="password"
          v-model="password"
          type="password"
          required
          class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
        />
      </div>

      <div v-if="error" class="text-red-500 text-sm">{{ error }}</div>

      <button
        type="submit"
        class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
        :disabled="loading"
      >
        <span v-if="loading">Logging in...</span>
        <span v-else>Login</span>
      </button>
    </form>
  </div>

  <div v-else class="card bg-white shadow-md rounded-lg p-6 max-w-md mx-auto">
    <h2 class="text-xl font-semibold mb-4">Welcome, {{ user.name }}!</h2>
    <p class="mb-2"><strong>Email:</strong> {{ user.email }}</p>
    <p class="mb-4"><strong>Role:</strong> {{ user.role }}</p>
    <button
      @click="user = null"
      class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
    >
      Logout
    </button>
  </div>
</template>

<style scoped>
.card {
  @apply transition-all duration-300;
}
</style>
