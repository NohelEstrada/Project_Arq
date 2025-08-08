<!-- eslint-disable vue/multi-word-component-names -->
<script setup>
import { computed } from 'vue';
import { useUserStore } from '@/stores/userStore';

const userStore = useUserStore();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
// Computed para verificar el estado del usuario
const isLoggedIn = computed(() => userStore.user !== null);
const isAdmin = computed(() => userStore.user?.role === 'admin');
</script>

<template>


  <!-- Contenido dinámico según el rol -->
  <main>
    <section v-if="isAdmin">
      <h2>Panel de Administración</h2>
      <p>Bienvenido Administrador. Aquí puedes gestionar el inventario y los seguros.</p>
    </section>

    <section v-else-if="isLoggedIn">
      <h2>Bienvenido a tu Panel</h2>
      <p>Aquí puedes ver tus recetas y tu historial de compras.</p>
    </section>

    <section v-else>
      <h2>Bienvenido</h2>
      <p>Por favor, inicia sesión para acceder a más funciones.</p>
    </section>
  </main>
</template>

<style scoped>
/* Estilos generales */
.header {
  background: #1e40af;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
}

.nav ul {
  list-style: none;
  display: flex;
  gap: 20px;
}

.nav a {
  color: white;
  text-decoration: none;
  font-weight: bold;
}

main {
  padding: 20px;
}
</style>