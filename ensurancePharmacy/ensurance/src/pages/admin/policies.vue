<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import type { Ref } from "vue";
import { getInsuranceApiUrl } from "../../utils/api";
import axios from "axios";

// Interfaces
interface Policy {
  idPolicy: number;
  percentage: number;
  creationDate: string;
  expDate: string;
  cost: number;
  enabled: number;
}

// Estado
const policies: Ref<Policy[]> = ref([]);
const loading: Ref<boolean> = ref(true);
const error: Ref<string> = ref("");
const success: Ref<string> = ref("");
const selectedPolicy: Ref<Policy | null> = ref(null);
const showEditModal: Ref<boolean> = ref(false);
const ip = import.meta.env.VITE_IP;
const debugInfo: Ref<string> = ref("");

// Todas las pólizas ordenadas
const allPolicies = computed(() => {
  return [...policies.value].sort((a, b) => a.percentage - b.percentage);
});

// Cargar pólizas desde el backend
const fetchPolicies = async () => {
  try {
    loading.value = true;
    error.value = "";
    const response = await axios.get(getInsuranceApiUrl("/policy"));
    console.log("Pólizas obtenidas:", response.data);
    policies.value = response.data;
    
    // Si no hay pólizas, crear las estándar
    if (policies.value.length === 0) {
      await createStandardPolicy(70, 300);
      await createStandardPolicy(90, 400);
      
      // Recargar para obtener las nuevas pólizas creadas
      const updatedResponse = await axios.get(getInsuranceApiUrl("/policy"));
      policies.value = updatedResponse.data;
    }
  } catch (err: any) {
    console.error("Error al cargar pólizas:", err);
    error.value = "No se pudieron cargar las pólizas. Por favor, intente nuevamente.";
    if (err.response) {
      debugInfo.value = `Error: ${err.response.status} - ${JSON.stringify(err.response.data)}`;
    } else if (err.request) {
      debugInfo.value = `No hubo respuesta del servidor. Verifique que el servidor esté corriendo.`;
    } else {
      debugInfo.value = `Error: ${err.message}`;
    }
  } finally {
    loading.value = false;
  }
};

// Crear póliza estándar
const createStandardPolicy = async (percentage: number, cost: number) => {
  try {
    const today = new Date();
    const expDate = new Date(today);
    expDate.setFullYear(today.getFullYear() + 1);
    
    const policyData = {
      percentage: percentage,
      creationDate: today.toISOString().split('T')[0],
      expDate: expDate.toISOString().split('T')[0],
      cost: cost,
      enabled: 1
    };
    
    const response = await axios.post(getInsuranceApiUrl("/policy"), policyData);
    console.log("Póliza creada:", response.data);
    success.value = `Póliza del ${percentage}% creada exitosamente`;
  } catch (err: any) {
    console.error(`Error al crear póliza del ${percentage}%:`, err);
    error.value = `No se pudo crear la póliza del ${percentage}%. Por favor, intente nuevamente.`;
    if (err.response) {
      debugInfo.value = `Error: ${err.response.status} - ${JSON.stringify(err.response.data)}`;
    }
  }
};

// Abrir modal para editar póliza existente
const openEditModal = (policy: Policy) => {
  selectedPolicy.value = { ...policy };
  showEditModal.value = true;
};

// Guardar póliza (solo actualizar)
const savePolicy = async () => {
  if (!selectedPolicy.value) return;
  
  try {
    loading.value = true;
    error.value = "";
    success.value = "";
    debugInfo.value = "";
    
    // Validar que el porcentaje sea numérico
    const percentage = parseFloat(String(selectedPolicy.value.percentage));
    if (isNaN(percentage) || percentage <= 0 || percentage > 100) {
      error.value = "El porcentaje debe ser un número entre 1 y 100";
      loading.value = false;
      return;
    }
    
    // Asegurar que los datos sean correctos
    const policyToUpdate = {
      idPolicy: selectedPolicy.value.idPolicy,
      percentage: percentage,
      creationDate: selectedPolicy.value.creationDate,
      expDate: selectedPolicy.value.expDate,
      cost: parseFloat(String(selectedPolicy.value.cost)),
      enabled: selectedPolicy.value.enabled
    };
    
    console.log("Datos de póliza a actualizar:", JSON.stringify(policyToUpdate));
    
    // Actualizar póliza existente
    const response = await axios.put(getInsuranceApiUrl("/policy"), policyToUpdate);
    console.log("Respuesta del servidor:", response.data);
    debugInfo.value = `Respuesta: ${JSON.stringify(response.data)}`;
    success.value = "Póliza actualizada exitosamente";
    
    // Recargar la lista de pólizas
    await fetchPolicies();
    showEditModal.value = false;
  } catch (err: any) {
    console.error("Error al guardar póliza:", err);
    if (err.response) {
      console.error("Datos de error:", err.response.data);
      error.value = `Error del servidor: ${err.response.status} - ${err.response.data.message || err.response.statusText}`;
      debugInfo.value = `Error completo: ${JSON.stringify(err.response.data)}`;
    } else if (err.request) {
      error.value = "No se recibió respuesta del servidor";
      debugInfo.value = "Verifique que el servidor esté en ejecución";
    } else {
      error.value = "No se pudo actualizar la póliza. Por favor, intente nuevamente.";
      debugInfo.value = `Error: ${err.message}`;
    }
  } finally {
    loading.value = false;
  }
};

// Cambiar estado de póliza (activar/desactivar)
const togglePolicyStatus = async (policy: Policy) => {
  try {
    loading.value = true;
    error.value = "";
    success.value = "";
    
    const updatedPolicy = { 
      ...policy, 
      enabled: policy.enabled === 1 ? 0 : 1 
    };
    
    console.log("Actualizando estado de póliza:", JSON.stringify(updatedPolicy));
    
    const response = await axios.put(getInsuranceApiUrl("/policy"), updatedPolicy);
    console.log("Respuesta del servidor:", response.data);
    
    // Actualizar la póliza en la lista local
    const index = policies.value.findIndex(p => p.idPolicy === policy.idPolicy);
    if (index !== -1) {
      policies.value[index].enabled = updatedPolicy.enabled;
    }
    
    success.value = `Póliza ${updatedPolicy.enabled === 1 ? 'activada' : 'desactivada'} exitosamente`;
  } catch (err: any) {
    console.error("Error al cambiar estado de póliza:", err);
    if (err.response) {
      error.value = `Error del servidor: ${err.response.status} - ${err.response.data.message || err.response.statusText}`;
      debugInfo.value = `Error completo: ${JSON.stringify(err.response.data)}`;
    } else {
      error.value = "No se pudo actualizar el estado de la póliza. Por favor, intente nuevamente.";
    }
  } finally {
    loading.value = false;
  }
};

// Formatear porcentaje
const formatPercentage = (percentage: number) => {
  return `${percentage}%`;
};

// Formatear precio
const formatPrice = (price: number) => {
  return `Q${price.toFixed(2)}`;
};

// Formatear nombre descriptivo
const getTypeName = (percentage: number) => {
  return percentage === 70 ? "Básica" : 
         percentage === 90 ? "Premium" : 
         `Personalizada (${percentage}%)`;
};

// Cerrar modal
const closeModal = () => {
  showEditModal.value = false;
  selectedPolicy.value = null;
};

// Inicializar
onMounted(() => {
  fetchPolicies();
});
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">Administración de Pólizas</h1>
    </div>
    
    <!-- Mensajes -->
    <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
      <p v-if="debugInfo" class="mt-2 text-xs font-mono border-t border-red-300 pt-2">{{ debugInfo }}</p>
    </div>
    
    <div v-if="success" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-6">
      <p>{{ success }}</p>
    </div>
    
    <!-- Spinner de carga -->
    <div v-if="loading" class="bg-white shadow-md rounded-lg p-6 text-center">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-indigo-600"></div>
      <p class="mt-2 text-gray-600">Cargando pólizas...</p>
    </div>
    
    <!-- Tarjetas de pólizas -->
    <div v-else-if="allPolicies.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
      <div v-for="policy in allPolicies" :key="policy.idPolicy" 
          class="bg-white shadow-md rounded-lg overflow-hidden border-t-4"
          :class="{
            'border-blue-500': policy.percentage === 70,
            'border-green-500': policy.percentage === 90,
            'border-purple-500': policy.percentage !== 70 && policy.percentage !== 90
          }">
        <div class="p-6">
          <div class="flex justify-between items-start mb-4">
            <div>
              <h2 class="text-xl font-bold">
                {{ getTypeName(policy.percentage) }}
              </h2>
              <p class="text-gray-600">ID: {{ policy.idPolicy }}</p>
            </div>
            <span 
              class="px-3 py-1 text-sm font-semibold rounded-full"
              :class="policy.enabled === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
            >
              {{ policy.enabled === 1 ? 'Activa' : 'Inactiva' }}
            </span>
          </div>
          
          <div class="grid grid-cols-2 gap-4 mb-6">
            <div>
              <p class="text-sm text-gray-500">Porcentaje de Cobertura</p>
              <p class="text-lg font-semibold">{{ formatPercentage(policy.percentage) }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">Costo Mensual</p>
              <p class="text-lg font-semibold">{{ formatPrice(policy.cost) }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">Fecha de Creación</p>
              <p>{{ policy.creationDate }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">Fecha de Expiración</p>
              <p>{{ policy.expDate }}</p>
            </div>
          </div>
          
          <div class="flex space-x-3">
            <button
              @click="openEditModal(policy)"
              class="flex-1 px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
            >
              Editar Póliza
            </button>
            <button
              @click="togglePolicyStatus(policy)"
              class="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded hover:bg-gray-50"
              :class="policy.enabled === 0 ? 'bg-green-50 border-green-300 text-green-700' : 'bg-red-50 border-red-300 text-red-700'"
            >
              {{ policy.enabled === 1 ? 'Desactivar' : 'Activar' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Sin resultados -->
    <div v-else class="bg-white shadow-md rounded-lg p-6 text-center text-gray-500">
      <p>No hay pólizas registradas. Las pólizas estándar (70% y 90%) se crearán automáticamente al recargar la página.</p>
      <button 
        @click="fetchPolicies"
        class="mt-4 px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
      >
        Recargar Pólizas
      </button>
    </div>
    
    <!-- Modal de edición -->
    <div v-if="showEditModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
        <h2 class="text-xl font-semibold mb-4">
          Editar Póliza
        </h2>
        
        <div v-if="selectedPolicy" class="space-y-4">
          <div class="bg-yellow-50 border-l-4 border-yellow-400 p-4 text-yellow-700 mb-4">
            <p class="font-medium">ID de Póliza: {{ selectedPolicy.idPolicy }}</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Porcentaje de Cobertura</label>
            <input
              v-model="selectedPolicy.percentage"
              type="number"
              min="1"
              max="100"
              step="1"
              class="w-full px-3 py-2 border border-gray-300 rounded-md"
            />
            <p class="text-xs text-gray-500 mt-1">Ingrese un valor entre 1 y 100.</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Costo Mensual (Q)</label>
            <input
              v-model="selectedPolicy.cost"
              type="number"
              min="0"
              step="0.01"
              class="w-full px-3 py-2 border border-gray-300 rounded-md"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Fecha de Creación</label>
            <input
              v-model="selectedPolicy.creationDate"
              type="date"
              class="w-full px-3 py-2 border border-gray-300 rounded-md"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Fecha de Expiración</label>
            <input
              v-model="selectedPolicy.expDate"
              type="date"
              class="w-full px-3 py-2 border border-gray-300 rounded-md"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Estado</label>
            <select
              v-model="selectedPolicy.enabled"
              class="w-full px-3 py-2 border border-gray-300 rounded-md"
            >
              <option :value="1">Activa</option>
              <option :value="0">Inactiva</option>
            </select>
          </div>
          
          <div class="flex justify-end space-x-3 pt-4">
            <button
              @click="closeModal"
              class="px-4 py-2 border border-gray-300 rounded text-gray-700 hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              @click="savePolicy"
              class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
            >
              Guardar Cambios
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template> 