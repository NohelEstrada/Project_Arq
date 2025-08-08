<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { getInsuranceApiUrl } from "../../utils/api";
interface ConfigurableAmount {
  idConfigurableAmount: number;
  prescriptionAmount: number;
}

// Estado
const config = ref<ConfigurableAmount | null>(null);
const newPrescriptionAmount = ref<number | null>(null);
const loading = ref(true);
const updating = ref(false);
const error = ref("");
const success = ref("");

// Configuración de IPs
const possibleIPs = [import.meta.env.VITE_IP || "localhost"];

// Función para probar múltiples IPs
async function tryMultipleIPs(endpoint: string, method: string = 'GET', data: any = null) {
  const serverIP = import.meta.env.VITE_IP || "localhost";
  try {
    const url = getInsuranceApiUrl(endpoint);
    console.log(`Intentando ${method} a ${url}`);
    const response = await axios({ method, url, data, timeout: 3000 });
    return response;
  } catch (error: any) {
    console.error(`Error con IP ${serverIP}:`, error.message);
    throw new Error("No se pudo conectar con el servidor");
  }
}

// Cargar configuración actual
const fetchConfig = async () => {
  try {
    loading.value = true;
    error.value = "";
    const response = await tryMultipleIPs('/configurable-amount/current');
    if (response.data) {
      config.value = response.data;
      newPrescriptionAmount.value = config.value?.prescriptionAmount ?? null;
    } else {
      config.value = null;
      newPrescriptionAmount.value = null;
      error.value = "No se encontró configuración disponible.";
    }
  } catch (err: any) {
    error.value = "Error al cargar la configuración actual.";
    console.error(err);
    config.value = null;
    newPrescriptionAmount.value = null;
  } finally {
    loading.value = false;
  }
};

// Actualizar configuración
const updateConfig = async () => {
  if (newPrescriptionAmount.value === null || newPrescriptionAmount.value < 0) {
    error.value = "El monto mínimo debe ser un número positivo.";
    return;
  }
  
  try {
    updating.value = true;
    error.value = "";
    success.value = "";
    
    const dataToSend = { 
        idConfigurableAmount: config.value?.idConfigurableAmount, // Enviar ID para que Hibernate sepa qué actualizar
        prescriptionAmount: newPrescriptionAmount.value 
    };
    
    const response = await tryMultipleIPs('/configurable-amount/update', 'PUT', dataToSend);
    config.value = response.data;
    newPrescriptionAmount.value = config.value?.prescriptionAmount ?? null;
    success.value = "Configuración actualizada correctamente.";
    setTimeout(() => success.value = "", 3000);
    
  } catch (err: any) {
    error.value = "Error al actualizar la configuración.";
    console.error(err);
  } finally {
    updating.value = false;
  }
};

// Cargar datos al iniciar
onMounted(() => {
  fetchConfig();
});

</script>

<template>
  <div class="container">
    <h1 class="title">Configuración del Sistema</h1>

    <!-- Mensajes de éxito o error -->
    <div v-if="success" class="success-message">{{ success }}</div>
    <div v-if="error" class="error-message">{{ error }}</div>

    <div class="config-card">
      <h2 class="subtitle">Monto Mínimo de Receta</h2>

      <div v-if="loading" class="loading-center">
        <p>Cargando configuración...</p>
      </div>
      
      <div v-else-if="!config" class="error-center">
        <p>No se pudo cargar la configuración.</p>
      </div>

      <div v-else>
        <p class="description">Monto mínimo actual para que una receta sea cubierta por el seguro:</p>
        <p class="amount-display">Q{{ config.prescriptionAmount.toFixed(2) }}</p>

        <div class="form-container">
          <label for="minAmount" class="form-label">Nuevo Monto Mínimo (Q)</label>
          <input 
            type="number" 
            id="minAmount"
            v-model.number="newPrescriptionAmount"
            min="0"
            step="0.01"
            class="form-input"
            placeholder="Ej: 250.00"
          />
          <button 
            @click="updateConfig"
            :disabled="updating || newPrescriptionAmount === null || newPrescriptionAmount === config.prescriptionAmount"
            class="save-button"
            :class="{ 'button-disabled': updating || newPrescriptionAmount === null || newPrescriptionAmount === config.prescriptionAmount }"
          >
            {{ updating ? 'Actualizando...' : 'Guardar Nuevo Monto' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 90%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 24px;
}

.success-message {
  background-color: #dcfce7;
  color: #166534;
  padding: 12px;
  margin-bottom: 16px;
  border-radius: 4px;
}

.error-message {
  background-color: #fee2e2;
  color: #b91c1c;
  padding: 12px;
  margin-bottom: 16px;
  border-radius: 4px;
}

.config-card {
  background-color: white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  padding: 24px;
}

.subtitle {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.loading-center {
  text-align: center;
  padding: 24px 0;
}

.error-center {
  text-align: center;
  padding: 24px 0;
}

.error-center p {
  color: #dc2626;
}

.description {
  margin-bottom: 8px;
  color: #4b5563;
}

.amount-display {
  font-size: 30px;
  font-weight: bold;
  color: #1d4ed8;
  margin-bottom: 24px;
}

.form-container {
  max-width: 384px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 4px;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
}

.save-button {
  margin-top: 12px;
  width: 100%;
  background-color: #2563eb;
  color: white;
  padding: 8px;
  border-radius: 6px;
}

.save-button:hover:not(.button-disabled) {
  background-color: #1d4ed8;
}

.button-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style> 