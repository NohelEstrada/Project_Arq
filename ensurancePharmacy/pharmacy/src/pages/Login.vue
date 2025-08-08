<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="login-container">
    <div class="login-image"></div>

    <div class="login-box">
      <div class="logo-container">
        <img src="@/assets/logo.png" alt="Farmacia Logo" class="logo" />
      </div>

      <h2 class="text-2xl font-bold text-center text-blue-800 mb-4">
        Iniciar Sesión
      </h2>

      <!-- Mensaje de error si lo hay -->
      <div v-if="errorMessage" class="mb-4 text-red-600 text-center">
        {{ errorMessage }}
      </div>

      <!-- Formulario -->
      <form @submit.prevent="()=>login()">
        <!-- E-mail -->
        <div class="mb-4">
          <label class="block text-gray-700">E-mail</label>
          <div class="input-group">
            <input
              v-model="email"
              type="text"
              class="input-field"
              required
            />
            <span class="icon">👤</span>
          </div>
        </div>
        
        <!-- Contraseña -->
        <div class="mb-4">
          <label class="block text-gray-700">Contraseña</label>
          <div class="input-group">
            <input
              v-model="password"
              type="password"
              class="input-field"
              required
            />
            <span class="icon">🔒</span>
          </div>
        </div>

        <!-- Botón de inicio de sesión -->
        <button type="submit" class="login-button">
          Iniciar sesión →
        </button>
      </form>

      <!-- Enlace para registrarse -->
      <div class="text-center mt-4">
        <router-link to="/register" class="text-blue-500 hover:underline">
          Regístrarse aquí
        </router-link>
      </div>
    </div>
  </div>

  <!-- Modal para mostrar el tipo de usuario -->
  <div v-if="showModal" class="modal">
    <div class="modal-content">
      <span class="close" @click="showModal = false">&times;</span>
      <p>Tipo de usuario: {{ userType }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useUserStore } from '@/stores/userStore'
const userStore = useUserStore();
import ApiService from '../services/ApiService';

const router = useRouter();

// Campos reactivamente enlazados al formulario
const email = ref('');
const password = ref('');
const errorMessage = ref('');
const showModal = ref(false);
const userType = ref('');

// Función principal de login
const login = async () => {
  errorMessage.value = '';
  
  try {
    console.log("Intentando iniciar sesión con:", email.value);
    
    // Petición POST al backend
    const response = await axios.post(ApiService.getPharmacyApiUrl("/login"), {
      email: email.value,
      password: password.value
    });

    // Verificar si la respuesta tiene datos
    if (!response.data) {
      throw new Error("La respuesta no contiene datos");
    }

    // Después de la petición POST exitosa
    console.log("Login exitoso:", response.data);

    // Guardar el usuario en el store y localStorage
    const userData = response.data;
    
    // Sanitizar y verificar los datos
    if (!userData.role) {
      userData.role = 'user'; // Rol por defecto si no viene
    }
    
    // Guardar en el store
    userStore.setUser(userData);

    // Guardar explícitamente en localStorage (respaldo adicional)
    localStorage.setItem('user', JSON.stringify(userData));
    localStorage.setItem('role', userData.role);
    console.log("Usuario guardado en localStorage con rol:", userData.role);

    // Verificar el rol y redirigir según corresponda
    if (userData.role === 'admin') {
      console.log("Redirigiendo a usuario admin a dashboard administrativo");
      router.push('/admindash');
    } else {
      console.log("Redirigiendo a usuario regular a inicio");
      router.push('/');
    }
  } catch (error) {
    console.error("Error en el login:", error);
    errorMessage.value = 'Credenciales incorrectas o error en el servidor.';
  }
};
</script>

<style scoped>
/* Ejemplo de estilos, ajusta a tu gusto. */
.login-container {
  display: flex;
  height: 100vh;
  background-color: #f8f9fa;
}
.login-image {
  flex: 1;
  background-image: url(@/assets/farmacialog.jpg);
  background-position: center;
  background-size: cover;
}
.login-box {
  flex: 1;
  background: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 50px;
}
.logo {
  max-width: 150px;
}
.input-group {
  position: relative;
  width: 100%;
}
.input-field {
  width: 100%;
  padding: 12px 40px 12px 12px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 16px;
}
.icon {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
}
.login-button {
  width: 100%;
  background: #1e40af;
  color: white;
  padding: 12px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 1rem;
}
.login-button:hover {
  background: #1e3a8a;
}

.modal {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  z-index: 1;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}

.close {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 24px;
  cursor: pointer;
}
</style>