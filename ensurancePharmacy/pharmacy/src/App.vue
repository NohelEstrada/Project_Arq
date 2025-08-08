<template>
  <div id="app">
    <Header @open-port-selector="showPortSelector = true" />
    <router-view /> <!-- Aquí se mostrarán las páginas según la ruta -->
    
    <!-- Selector de puertos -->
    <PortSelector v-if="showPortSelector" @close="showPortSelector = false" />
  </div>
</template>

<script>
import Header from '@/components/Header.vue';
import PortSelector from '@/components/PortSelector.vue';
import ApiService from '@/services/ApiService';

export default {
  name: "App",
  components: { 
    Header,
    PortSelector
  },
  data() {
    return {
      showPortSelector: false
    };
  },
  created() {
    // Cargar configuración de puertos
    ApiService.loadPortConfiguration();
    
    // Verificar si se debe mostrar el selector de puertos
    const skipPortSelector = localStorage.getItem('skipPortSelector') === 'true';
    this.showPortSelector = !skipPortSelector;
  }
};
</script>

<style>
/* Estilos globales opcionales */
body {
  font-family: Arial, sans-serif;
  background-color: #f1f9ff;
  margin: 0;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
