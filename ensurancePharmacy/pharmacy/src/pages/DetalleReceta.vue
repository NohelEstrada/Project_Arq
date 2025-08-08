<template>
  <div class="recipe-detail-container">
    <h1 class="title">Detalle de la Receta</h1>
    
    <!-- Indicador de carga -->
    <div v-if="isLoading" class="loading-section">
      <div class="loading-spinner"></div>
      <p>Cargando receta...</p>
    </div>
    
    <!-- Mensaje de error si no se encuentra la receta -->
    <div v-else-if="!receta" class="error-section">
      <p>{{ error || 'No se encontró la receta.' }}</p>
      <button @click="fetchReceta" class="retry-button">Intentar de nuevo</button>
    </div>
    
    <!-- Detalle de la receta -->
    <div v-else class="recipe-card">
      <div class="recipe-header">
        <h2>Receta #{{ receta.id.prescriptionId }}</h2>
        <span class="recipe-date">{{ receta.prescription.date }}</span>
      </div>
      
      <div class="recipe-info">
        <div class="info-section">
          <h3>Información General</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">Doctor:</span>
              <span class="value">{{ receta.prescription.doctor.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">Diagnóstico:</span>
              <span class="value">{{ receta.prescription.diagnosis }}</span>
            </div>
            <div class="info-item">
              <span class="label">Paciente:</span>
              <span class="value">{{ receta.prescription.user.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">Email:</span>
              <span class="value">{{ receta.prescription.user.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">Hospital:</span>
              <span class="value">{{ receta.prescription.hospital.name }}</span>
            </div>
          </div>
        </div>
        
        <div class="medications-section">
          <h3>Medicamentos</h3>
          <div v-if="receta.medicines && receta.medicines.length" class="medications-table">
            <table>
              <thead>
                <tr>
                  <th>Medicamento</th>
                  <th>Concentración</th>
                  <th>Presentación</th>
                  <th>Dosis</th>
                  <th>Frecuencia</th>
                  <th>Duración</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="medicine in receta.medicines" :key="medicine.idMedicine">
                  <td>{{ medicine.name }}</td>
                  <td>{{ medicine.concentration }}</td>
                  <td>{{ medicine.presentacion }}</td>
                  <td>{{ medicine.dosis }}</td>
                  <td>{{ medicine.frecuencia }} horas</td>
                  <td>{{ medicine.duracion }} días</td>
                </tr>
              </tbody>
            </table>
          </div>
          <p v-else class="no-medicines">No hay medicamentos registrados en esta receta.</p>
        </div>
        
        <div class="recipe-actions">
          <button @click="goBack" class="back-button">Volver</button>
          <button @click="procesarCompra" class="buy-button" v-if="puedeComprar">Comprar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import ApiService from '../services/ApiService';  

const route = useRoute();
const router = useRouter();

// Estado
const isLoading = ref(true);
const receta = ref(null);
const error = ref(null);

// Propiedades computadas
const puedeComprar = computed(() => {
  return receta.value && receta.value.medicines && receta.value.medicines.length > 0;
});

function goBack() {
  router.back();
}

function procesarCompra() {
  // Redireccionar a la página de compra con la receta
  router.push({
    name: 'PrescriptionPay',
    params: { id: receta.value.id.prescriptionId }
  });
}

async function fetchReceta() {
  isLoading.value = true;
  error.value = null;
  
  try {
    const prescriptionId = route.params.id;
    if (!prescriptionId) {
      throw new Error('ID de receta no proporcionado');
    }
    
    // Usar la misma API que funciona en PrescriptionPay
    const response = await axios.get(ApiService.getPharmacyApiUrl("/prescription_medicines"));
    const allData = response.data;
    
    // Filtrar solo las recetas que coinciden con el id de la receta
    const filtered = allData.filter(p => p.id.prescriptionId == prescriptionId);

    if (filtered.length > 0) {
      // Agrupar los datos por prescriptionId para consolidar las medicinas
      const grouped = {};
      filtered.forEach(p => {
        const prescId = p.id.prescriptionId;
        if (!grouped[prescId]) {
          grouped[prescId] = {
            ...p, // Copiar todas las propiedades
            medicines: []
          };
        }
        grouped[prescId].medicines.push({
          ...p.medicine,
          dosis: p.dosis,
          frecuencia: p.frecuencia,
          duracion: p.duracion
        });
      });
      
      receta.value = grouped[prescriptionId];
      console.log('Receta procesada:', receta.value);
    } else {
      error.value = 'No se encontró la receta.';
      receta.value = null;
    }
  } catch (err) {
    console.error('Error al obtener la receta:', err);
    error.value = err.message || 'Error al cargar la receta';
    receta.value = null;
  } finally {
    isLoading.value = false;
  }
}

// Cargar la receta al montar el componente
onMounted(() => {
  fetchReceta();
});
</script>

<style scoped>
.recipe-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
}

.title {
  text-align: center;
  font-size: 2rem;
  color: #1e40af;
  margin-bottom: 2rem;
}

.loading-section, .error-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #1e40af;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.retry-button {
  padding: 0.5rem 1rem;
  background-color: #1e40af;
  color: white;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  margin-top: 1rem;
}

.recipe-card {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.recipe-header {
  background-color: #1e40af;
  color: white;
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recipe-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.recipe-date {
  font-size: 0.875rem;
  opacity: 0.9;
}

.recipe-info {
  padding: 1.5rem;
}

.info-section, .medications-section {
  margin-bottom: 2rem;
}

.info-section h3, .medications-section h3 {
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 0.5rem;
  margin-bottom: 1rem;
  color: #1e40af;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.label {
  font-size: 0.875rem;
  color: #64748b;
  margin-bottom: 0.25rem;
}

.value {
  font-weight: 500;
}

.medications-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 0.75rem;
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
}

th {
  background-color: #f1f5f9;
  font-weight: 600;
}

.no-medicines {
  color: #64748b;
  font-style: italic;
  text-align: center;
  padding: 1rem;
}

.recipe-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 2rem;
}

.back-button, .buy-button {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
}

.back-button {
  background-color: #e2e8f0;
  color: #334155;
}

.buy-button {
  background-color: #16a34a;
  color: white;
}

@media (max-width: 640px) {
  .recipe-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .recipe-date {
    margin-top: 0.5rem;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .recipe-actions {
    flex-direction: column;
    gap: 1rem;
  }
  
  .back-button, .buy-button {
    width: 100%;
  }
}
</style> 