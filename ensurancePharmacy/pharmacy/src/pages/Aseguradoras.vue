<!-- eslint-disable vue/multi-word-component-names -->
<template>
  
  <div class="aseguradoras-container">
    <h2 class="title">Aseguradoras Afiliadas</h2>
    <div class="aseguradoras-grid">
      <div v-for="aseg in aseguradoras" :key="aseg.id" class="aseguradora-card">
        <img :src="aseg.logo" :alt="aseg.name" class="aseguradora-logo" />
        <h3 class="aseguradora-name">{{ aseg.name }}</h3>
        <p class="aseguradora-info">{{ aseg.info }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>

import { ref } from 'vue';

const aseguradoras = ref([
  { id: 1, name: 'Seguros Salud Total', logo: 'https://via.placeholder.com/150', info: 'Cobertura amplia en atención médica.' },
  { id: 2, name: 'Protección Médica', logo: 'https://via.placeholder.com/150', info: 'Planes accesibles para familias.' },
  { id: 3, name: 'Vida Segura', logo: 'https://via.placeholder.com/150', info: 'Atención médica y hospitalaria de calidad.' },
  // ...existing code for more aseguradoras if needed...
]);
</script>

<style scoped>
.aseguradoras-container {
  padding: 20px;
  background-color: #f8f9fa;
  text-align: center;
}

.title {
  font-size: 24px;
  color: #1e40af;
  font-weight: bold;
  margin-bottom: 20px;
}

.aseguradoras-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  padding: 20px;
}

.aseguradora-card {
  background: white;
  padding: 15px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.aseguradora-card:hover {
  transform: scale(1.05);
}

.aseguradora-logo {
  width: 100%;
  height: 150px;
  object-fit: contain;
}

.aseguradora-name {
  font-size: 18px;
  margin: 10px 0;
  color: #1e40af;
}

.aseguradora-info {
  font-size: 14px;
  color: #555;
}
</style>
