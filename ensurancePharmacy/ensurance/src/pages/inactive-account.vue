<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import eventBus from "../eventBus";

const router = useRouter();
const email = ref("");
const message = ref("");
const error = ref("");
const loading = ref(false);

onMounted(() => {
  // Obtener email del perfil
  const profile = JSON.parse(localStorage.getItem("user") || "null");
  if (profile && profile.email) {
    email.value = profile.email;
  }
});

const requestActivation = async () => {
  if (!email.value) {
    error.value = "Por favor ingrese un correo electrónico válido";
    return;
  }

  try {
    loading.value = true;
    error.value = "";
    message.value = "";
    
    // Simulamos el envío de una solicitud de activación
    // En un caso real, deberías hacer una petición al backend
    setTimeout(() => {
      message.value = "Solicitud enviada correctamente. Por favor, contacte a soporte si no recibe respuesta pronto.";
      loading.value = false;
    }, 1000);
    
  } catch (err) {
    error.value = "Ocurrió un error al enviar la solicitud. Por favor, intente nuevamente.";
    console.error("Error:", err);
    loading.value = false;
  }
};

const logout = () => {
  localStorage.removeItem("user");
  eventBus.emit('logout');
  router.push("/login");
};
</script>

<template>
  <div class="max-w-md mx-auto mt-8 p-6 bg-white rounded-lg shadow-lg">
    <div class="text-center mb-6">
      <h2 class="text-2xl font-bold text-red-600">Cuenta inactiva</h2>
      <p class="text-gray-600 mt-2">
        Su cuenta no ha sido activada aún. Por favor, espere a que un administrador la active o solicite la activación.
      </p>
    </div>

    <div v-if="message" class="mb-4 p-3 bg-green-100 text-green-700 rounded">
      {{ message }}
    </div>

    <div v-if="error" class="mb-4 p-3 bg-red-100 text-red-700 rounded">
      {{ error }}
    </div>

    <form @submit.prevent="requestActivation" class="space-y-4">
      <div>
        <label for="email" class="block text-sm font-medium text-gray-700 mb-1">
          Correo electrónico
        </label>
        <input
          id="email"
          v-model="email"
          type="email"
          placeholder="Su correo electrónico"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      <div>
        <button
          type="submit"
          :disabled="loading"
          class="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
          {{ loading ? "Enviando..." : "Solicitar activación" }}
        </button>
      </div>
    </form>

    <div class="mt-6 text-center">
      <button
        @click="logout"
        class="text-blue-600 hover:text-blue-800 underline"
      >
        Cerrar sesión
      </button>
    </div>
  </div>
</template>

<style scoped>
/* Estilos adicionales si son necesarios */
</style> 