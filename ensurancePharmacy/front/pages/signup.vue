<script setup lang="ts">
const dark = darkMode();
import { ref } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";

const name = ref("");
const email = ref("");
const password = ref("");
const birthdate = ref("");
const address = ref("");
const phone = ref("");
const cui = ref("");
const errorMessage = ref("");
const config = useRuntimeConfig();
const ip = config.public.ip;
import { getInsuranceApiUrl } from "../../utils/api";
const createUser = async () => {
  try {
    notify({
      type: "loading",
      title: "Creating user",
      description: "Please wait...",
    });
    console.log(name, cui, phone, email, birthdate, address, password);

    const response = await axios.post(
      getInsuranceApiUrl("/users"), // URL de tu endpoint de login
      {
        name: name.value,
        cui: cui.value,
        phone: phone.value,
        email: email.value,
        birthDate: birthdate.value,
        address: address.value,
        password: password.value,
      },
    );

    // Por ejemplo, si la respuesta contiene el usuario autenticado:
    console.log("Creacion exitoso:", response.data);

    // Redirigir a la página principal o dashboard
    //router.push('/home');
    notify({
      type: "success",
      title: "User created",
      description: "User created successfully, Please way for admin approval",
    });
  } catch (error) {
    console.error("Error en login:", error);
          errorMessage.value = "Credenciales incorrectas o error en el servidor.";
    notify({
      type: "error",
      title: "Error creating user",
      description: "Error creating user",
    });
  }
};
</script>
<template>
  <main
    class="bg-image-[url(/sign.jpg)] flex w-full items-center justify-center"
  >
    <section
      class="card min-w-[350px] max-sm:mx-2 max-sm:w-full max-sm:flex-col"
    >
      <h2 class="title mb-6">Hi, please Sign Up</h2>
      <form>
        <label for="nombre" class="label">Name</label>
        <input
          type="text"
          id="nombre"
          required
          placeholder="Name"
          class="field mb-6"
          v-model="name"
        />
        <label for="email" class="label">E-Mail</label>
        <input
          type="email"
          id="email"
          placeholder="email@example.com"
          required
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
        <label for="date" class="label">Birth Date</label>
        <input
          type="date"
          id="date"
          required
          class="field mb-6"
          v-model="birthdate"
        />
        <label for="address" class="label">Address</label>
        <input
          type="text"
          id="address"
          required
          placeholder="5th avenue, 1234, New York, NY, 10001"
          class="field mb-6"
          v-model="address"
        />
        <label for="phone" class="label">Phone Number</label>
        <input
          type="number"
          id="phone"
          required
          placeholder="59588867"
          class="field mb-6"
          v-model="phone"
        />
        <label for="dpi" class="label">DPI Number</label>
        <input
          type="number"
          id="dpi"
          required
          placeholder="3603954160101"
          class="field mb-6"
          v-model="cui"
        />
        <button class="btn flex" @click.prevent="createUser">
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
