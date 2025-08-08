<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import type { Ref } from "vue";
import { getInsuranceApiUrl } from "../../utils/api";
import axios from "axios";
import { useRouter } from "vue-router";

// Interfaces
interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: string;
  email: string;
  enabled: number;
}

// Estado
const hospitals: Ref<Hospital[]> = ref([]);
const loading: Ref<boolean> = ref(true);
const error: Ref<string> = ref("");
const searchTerm: Ref<string> = ref("");
const router = useRouter();
const ip = import.meta.env.VITE_IP;

// Hospitales filtrados por término de búsqueda
const filteredHospitals = computed(() => {
  if (!searchTerm.value) return hospitals.value;
  
  const term = searchTerm.value.toLowerCase();
  return hospitals.value.filter(hospital => 
    hospital.name.toLowerCase().includes(term) ||
    hospital.address.toLowerCase().includes(term) ||
    hospital.email.toLowerCase().includes(term) ||
    hospital.phone.includes(term)
  );
});

// Cargar hospitales
const fetchHospitals = async () => {
  try {
    loading.value = true;
    error.value = "";
    
    const response = await axios.get(getInsuranceApiUrl("/hospital"));
    // Filtrar solo hospitales activos
    hospitals.value = response.data.filter((hospital: Hospital) => hospital.enabled === 1);
  } catch (err: any) {
    console.error("Error al cargar hospitales:", err);
    error.value = "No se pudieron cargar los hospitales. Por favor, intente nuevamente.";
  } finally {
    loading.value = false;
  }
};

// Ver servicios disponibles en un hospital específico
const viewHospitalServices = (hospitalId: number) => {
  router.push(`/catalog/hospital-services?hospitalId=${hospitalId}`);
};

// Inicializar
onMounted(() => {
  fetchHospitals();
});
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold mb-6">Catálogo de Hospitales Aprobados</h1>
    
    <!-- Error global -->
    <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
    </div>
    
    <!-- Buscador -->
    <div class="bg-white shadow-md rounded-lg p-6 mb-6">
      <label class="block text-sm font-medium text-gray-700 mb-1">Buscar hospital</label>
      <input
        v-model="searchTerm"
        type="text"
        placeholder="Buscar por nombre, dirección, teléfono..."
        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
      />
    </div>
    
    <!-- Spinner de carga -->
    <div v-if="loading" class="bg-white shadow-md rounded-lg p-6 text-center">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-indigo-600"></div>
      <p class="mt-2 text-gray-600">Cargando hospitales...</p>
    </div>
    
    <!-- Sin resultados -->
    <div v-else-if="filteredHospitals.length === 0" class="bg-white shadow-md rounded-lg p-6 text-center text-gray-500">
      <p>No se encontraron hospitales con los filtros aplicados.</p>
    </div>
    
    <!-- Listado de hospitales -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="hospital in filteredHospitals" 
        :key="hospital.idHospital"
        class="bg-white shadow-md rounded-lg overflow-hidden hover:shadow-lg transition-shadow"
      >
        <div class="p-4 bg-gray-50 border-b">
          <h2 class="text-lg font-semibold">{{ hospital.name }}</h2>
        </div>
        
        <div class="p-6">
          <div class="space-y-4">
            <div>
              <p class="text-sm font-medium text-gray-500">Dirección</p>
              <p class="text-gray-900">{{ hospital.address }}</p>
            </div>
            
            <div>
              <p class="text-sm font-medium text-gray-500">Teléfono</p>
              <p class="text-gray-900">{{ hospital.phone }}</p>
            </div>
            
            <div>
              <p class="text-sm font-medium text-gray-500">Email</p>
              <p class="text-gray-900">{{ hospital.email }}</p>
            </div>
            
            <div class="pt-4">
              <button
                @click="viewHospitalServices(hospital.idHospital)"
                class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
              >
                Ver servicios disponibles
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template> 