<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import type { Ref } from "vue";
import { getInsuranceApiUrl } from "../../utils/api";
import axios from "axios";
import { useRoute, useRouter } from "vue-router";

// Interfaces
interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: string;
  email: string;
  enabled: number;
}

interface Category {
  idCategory: number;
  name: string;
  description: string;
  enabled: number;
}

interface InsuranceService {
  idInsuranceService: number;
  name: string;
  description: string;
  price: number;
  category: Category;
  subcategory: string;
  enabled: number;
}

interface HospitalService {
  idHospitalService: number;
  hospital: Hospital;
  insuranceService: InsuranceService;
  approved: number;
  approvalDate: string;
  notes: string;
}

// Estado
const hospital: Ref<Hospital | null> = ref(null);
const approvedServices: Ref<HospitalService[]> = ref([]);
const loading: Ref<boolean> = ref(true);
const error: Ref<string> = ref("");
const searchTerm: Ref<string> = ref("");
const selectedCategory: Ref<number | null> = ref(null);
const categories: Ref<{ [key: number]: string }> = ref({});
const route = useRoute();
const router = useRouter();
const ip = import.meta.env.VITE_IP;

// Obtener ID del hospital de la URL
const hospitalId = computed(() => {
  const id = route.query.hospitalId;
  return id ? parseInt(id as string) : null;
});

// Servicios filtrados por categoría y término de búsqueda
const filteredServices = computed(() => {
  let result = approvedServices.value;
  
  // Filtrar por categoría si hay alguna seleccionada
  if (selectedCategory.value !== null) {
    result = result.filter(service => 
      service.insuranceService.category && 
      service.insuranceService.category.idCategory === selectedCategory.value
    );
  }
  
  // Filtrar por término de búsqueda
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase();
    result = result.filter(service => 
      service.insuranceService.name.toLowerCase().includes(term) ||
      service.insuranceService.description.toLowerCase().includes(term) ||
      service.insuranceService.subcategory.toLowerCase().includes(term) ||
      (service.insuranceService.category && service.insuranceService.category.name.toLowerCase().includes(term))
    );
  }
  
  return result;
});

// Agrupar servicios por subcategoría
const servicesBySubcategory = computed(() => {
  const grouped: Record<string, HospitalService[]> = {};
  
  filteredServices.value.forEach(service => {
    const subcategory = service.insuranceService.subcategory || 'Sin subcategoría';
    if (!grouped[subcategory]) {
      grouped[subcategory] = [];
    }
    grouped[subcategory].push(service);
  });
  
  return grouped;
});

// Extraer las categorías únicas de los servicios aprobados
const extractCategories = () => {
  const categoryMap: { [key: number]: string } = {};
  
  approvedServices.value.forEach(service => {
    if (service.insuranceService.category) {
      const category = service.insuranceService.category;
      categoryMap[category.idCategory] = category.name;
    }
  });
  
  categories.value = categoryMap;
};

// Cargar datos del hospital y sus servicios aprobados
const fetchData = async () => {
  if (!hospitalId.value) {
    error.value = "No se ha especificado un hospital.";
    loading.value = false;
    return;
  }
  
  try {
    loading.value = true;
    error.value = "";
    
    // Cargar datos del hospital
    const hospitalResponse = await axios.get(getInsuranceApiUrl(`/hospital?id=${hospitalId.value}`));
    hospital.value = hospitalResponse.data;
    
    // Cargar servicios aprobados para este hospital
    const servicesResponse = await axios.get(getInsuranceApiUrl(`/hospital-services?hospital=${hospitalId.value}`));
    approvedServices.value = servicesResponse.data;
    
    // Extraer categorías
    extractCategories();
  } catch (err: any) {
    console.error("Error al cargar datos:", err);
    error.value = "No se pudieron cargar los datos. Por favor, intente nuevamente.";
  } finally {
    loading.value = false;
  }
};

// Formatear precio
const formatPrice = (price: number) => {
  return `Q${price.toFixed(2)}`;
};

// Formatear fecha
const formatDate = (dateString: string) => {
  if (!dateString) return "N/A";
  const date = new Date(dateString);
  return date.toLocaleDateString();
};

// Volver a la lista de hospitales
const backToHospitals = () => {
  router.push('/catalog/hospitals');
};

// Inicializar
onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <!-- Botón para volver -->
    <button 
      @click="backToHospitals"
      class="mb-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-indigo-700 bg-indigo-100 hover:bg-indigo-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd" />
      </svg>
      Volver a hospitales
    </button>
    
    <div v-if="hospital" class="mb-6">
      <h1 class="text-2xl font-bold">Servicios disponibles en {{ hospital.name }}</h1>
      <p class="text-gray-600 mt-1">{{ hospital.address }}</p>
    </div>
    
    <!-- Error global -->
    <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
      <p>{{ error }}</p>
    </div>
    
    <!-- Filtros -->
    <div class="bg-white shadow-md rounded-lg p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Buscar servicio</label>
          <input
            v-model="searchTerm"
            type="text"
            placeholder="Buscar por nombre, descripción..."
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          />
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Filtrar por categoría</label>
          <select
            v-model="selectedCategory"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          >
            <option :value="null">Todas las categorías</option>
            <option 
              v-for="(name, id) in categories" 
              :key="id"
              :value="Number(id)"
            >
              {{ name }}
            </option>
          </select>
        </div>
      </div>
    </div>
    
    <!-- Spinner de carga -->
    <div v-if="loading" class="bg-white shadow-md rounded-lg p-6 text-center">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-indigo-600"></div>
      <p class="mt-2 text-gray-600">Cargando servicios...</p>
    </div>
    
    <!-- Sin resultados -->
    <div v-else-if="approvedServices.length === 0" class="bg-white shadow-md rounded-lg p-6 text-center text-gray-500">
      <p>Este hospital no tiene servicios aprobados por el seguro.</p>
    </div>
    
    <!-- Sin resultados de búsqueda -->
    <div v-else-if="filteredServices.length === 0" class="bg-white shadow-md rounded-lg p-6 text-center text-gray-500">
      <p>No se encontraron servicios con los filtros aplicados.</p>
    </div>
    
    <!-- Listado de servicios agrupados por subcategoría -->
    <div v-else class="space-y-6">
      <div 
        v-for="(services, subcategory) in servicesBySubcategory" 
        :key="subcategory"
        class="bg-white shadow-md rounded-lg overflow-hidden"
      >
        <div class="p-4 bg-gray-50 border-b">
          <h2 class="text-lg font-semibold">{{ subcategory }}</h2>
        </div>
        
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Servicio</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Categoría</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Descripción</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Precio</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha de aprobación</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="service in services" :key="service.idHospitalService" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {{ service.insuranceService.name }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ service.insuranceService.category ? service.insuranceService.category.name : 'Sin categoría' }}
                </td>
                <td class="px-6 py-4 text-sm text-gray-500">
                  {{ service.insuranceService.description }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ formatPrice(service.insuranceService.price) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ formatDate(service.approvalDate) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template> 