<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <header class="navbar">
    <div class="navbar-content">
      <!-- Logo -->
      <img src="@/assets/logo.png" alt="Logo" class="logo" />

      <!-- Menú de Navegación (versión desktop) -->
      <nav class="nav-links hidden md:flex">
        <router-link to="/" class="nav-item">Inicio</router-link>
        <router-link to="/catalogo" class="nav-item"
          >Catálogo de Productos</router-link
        >
        <router-link to="/contact" class="nav-item">Contacto</router-link>
        <router-link to="/cart" class="nav-item">Carrito</router-link>

        <!-- Enlace SOLO para administradores -->
        <router-link
          v-if="isLoggedIn && userStore.getUser().role === 'admin'"
          to="/admindash"
          class="nav-item admin-link"
        >
          📊 Dashboard Admin
        </router-link>

        <router-link
          v-if="isLoggedIn && userStore.getUser().role === 'admin'"
          to="/create-product"
          class="nav-item"
        >
          Crear Producto
        </router-link>

        <!-- Configuración de puertos (disponible para todos) -->
        <button
          @click="openPortSelector"
          class="nav-item admin-config-button"
        >
          ⚙️ Configurar Puertos
        </button>

        <router-link to="/prescriptions" class="nav-item">
          Ver Recetas
        </router-link>

        <!-- Si el usuario está loggeado -->
        <template v-if="isLoggedIn">
          <!-- Muestra el rol -->
          <span class="logged-user-message">
            Rol: {{ userStore.getUser().role }}
          </span>
          <!-- Botón para cerrar sesión -->
          <button @click="logout" class="login-button">Cerrar Sesión</button>
        </template>

        <!-- Si NO está loggeado, muestra el botón de Iniciar Sesión -->
        <template v-else>
          <router-link to="/login" class="login-button"
            >🔑 Iniciar Sesión</router-link
          >
        </template>
      </nav>

      <!-- Botón de menú hamburguesa (versión móvil) -->
      <button @click="toggleMenu" class="menu-button md:hidden">☰</button>
    </div>

    <!-- Menú desplegable en móviles -->
    <div v-if="mobileMenuOpen" class="mobile-menu">
      <router-link to="/" class="mobile-item" @click="toggleMenu"
        >Inicio</router-link
      >
      <router-link to="/catalogo" class="mobile-item" @click="toggleMenu"
        >Catálogo de Productos</router-link
      >
     
      <!-- Configuración de puertos (móvil - disponible para todos) -->
      <button
        @click="openPortSelector(); toggleMenu();"
        class="mobile-item admin-config-button"
      >
        ⚙️ Configurar Puertos
      </button>

      <!-- Enlace SOLO para administradores (móvil) -->
      <router-link
        v-if="isLoggedIn && userStore.getUser().role === 'admin'"
        to="/admindash"
        class="mobile-item admin-link"
        @click="toggleMenu"
      >
        📊 Dashboard Admin
      </router-link>

      <!-- Si está loggeado, muestra rol y logout (móvil) -->
      <template v-if="isLoggedIn">
        <span class="logged-user-message" style="color: white">
          Rol: {{ userStore.user.role }}
        </span>
        <button
          @click="
            logout();
            toggleMenu();
          "
          class="mobile-login"
        >
          Cerrar Sesión
        </button>
      </template>
      <!-- Si NO está loggeado, login (móvil) -->
      <template v-else>
        <router-link to="/login" class="mobile-login" @click="toggleMenu">
          🔑 Iniciar Sesión
        </router-link>
      </template>
    </div>
  </header>
</template>

<script>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/userStore";

export default {
  name: 'AppHeader',
  
  emits: ['open-port-selector'],
  
  setup(props, { emit }) {
    // Menú móvil
    const mobileMenuOpen = ref(false);
    const toggleMenu = () => {
      mobileMenuOpen.value = !mobileMenuOpen.value;
    };
    
    // Store de usuario
    const userStore = useUserStore();
    const router = useRouter();
    
    const isLoggedIn = computed(() => {
      const user = userStore.getUser();
      return Object.keys(user).length !== 0;
    });
    
    // Función para abrir el selector de puertos
    const openPortSelector = () => {
      // Emite un evento que será capturado por App.vue
      emit('open-port-selector');
    };
    
    // Cerrar sesión
    const logout = () => {
      userStore.logout();
      // Limpiar todas las claves de sesión del localStorage
      localStorage.removeItem("session");
      localStorage.removeItem("user");
      localStorage.removeItem("role");
      console.log("Sesión cerrada: localStorage limpiado");
      
      router.push("/");
      setTimeout(() => {
        window.location.reload();
      }, 100);
    };
    
    return {
      mobileMenuOpen,
      toggleMenu,
      userStore,
      isLoggedIn,
      openPortSelector,
      logout
    };
  }
}
</script>

<style scoped>
/* Ajusta estos estilos a tu gusto */
.navbar {
  background: #1e40af;
  padding: 15px 30px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.navbar-content {
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  height: 50px;
}

.nav-links {
  display: flex;
  gap: 20px;
}

.nav-item {
  color: white;
  font-size: 16px;
  font-weight: bold;
  text-decoration: none;
  transition: color 0.3s;
}

.nav-item:hover {
  color: yellow;
}

.admin-config-button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 0;
}

.login-button {
  background: white;
  color: #1e40af;
  padding: 8px 15px;
  border-radius: 5px;
  font-weight: bold;
  margin-left: 1rem;
  text-decoration: none;
}

.logged-user-message {
  color: white;
  font-weight: bold;
  margin-right: 1rem;
}

.menu-button {
  font-size: 24px;
  color: white;
  border: none;
  background: transparent;
  cursor: pointer;
}

.mobile-menu {
  background: #1e40af;
  position: absolute;
  top: 60px;
  left: 0;
  width: 100%;
  flex-direction: column;
  text-align: center;
  padding: 15px 0;
}

.mobile-item {
  color: white;
  font-size: 18px;
  padding: 10px;
  text-decoration: none;
}

.mobile-item:hover {
  background: rgba(255, 255, 255, 0.2);
}

.mobile-login {
  background: white;
  color: #1e40af;
  padding: 10px;
  text-decoration: none;
  font-weight: bold;
  margin-top: 0.5rem;
}
</style>
