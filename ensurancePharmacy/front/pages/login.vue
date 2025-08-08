<script setup lang="ts">
import {setProfile} from "~/composables/useProfile";

const dark = darkMode();

import { ref } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";

const email = ref("");
const password = ref("");
const errorMessage = ref("");
const router = useRouter();
const config = useRuntimeConfig();
const ip = config.public.ip;

const handleLogin = async () => {
  try {
    console.log(ip);
    notify({
      type: "loading",
      title: "Iniciando sesión",
      description: "Procesando tu solicitud, por favor espera...",
    });
    const response = await axios.post(
      `http://${ip}:8080/api/login`, // URL de tu endpoint de login
      {
        email: email.value,
        password: password.value,
      },
    );

    setUser(response.data.role);
    console.log("Login exitoso:", response.data);
    notify({
      type: "success",
      title: "Login exitoso",
      description: "Inicio de sesión exitoso.",
      timeout: 2000,
    });
    router.push("/");
    setProfile(response.data);
  } catch (error) {
    notify({
      type: "error",
      title: "Error en login",
      description: "Credenciales incorrectas o error en el servidor.",
      timeout: 2000,
    });
    console.error("Error en login:", error);
    errorMessage.value = "Credenciales incorrectas o error en el servidor.";
  }
};


</script>
<template>
  <main
    :class="[
      `bg-image-[url('/sign.jpg')] flex items-center justify-center`,
      dark ? 'dark' : '',
    ]"
  >
    <section
      class="card min-w-[350px] max-sm:mx-2 max-sm:w-full max-sm:flex-col"
    >
      <h2 class="title mb-6">Hi, Please Enter Your Credentials</h2>
      <form>
        <label for="email" class="label">E-Mail</label>
        <input
          type="email"
          id="email"
          required
          placeholder="email@example.com"
          class="field mb-6"
          v-model="email"
        />
        <label for="password" class="label">Password</label>
        <input
          type="password"
          id="password"
          required
          placeholder="********"
          class="field mb-6"
          v-model="password"
        />
        <button class="btn flex" @click.prevent="handleLogin">
          <svg
            class="me-2"
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill="currentColor"
          >
            <path
              d="m424-296 282-282-56-56-226 226-114-114-56 56 170 170Zm56 216q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"
            />
          </svg>
          Complete
        </button>
      </form>
    </section>
  </main>
</template>
