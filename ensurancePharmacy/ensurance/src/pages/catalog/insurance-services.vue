<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import type { Ref } from "vue";
import { getInsuranceApiUrl } from "../../utils/api";
import axios from "axios";

// Interfaces
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
  subcategory: Category;
  enabled: number;
}

// Estado
const services: Ref<InsuranceService[]> = ref([]);
const categories: Ref<Category[]> = ref([]);
const loading: Ref<boolean> = ref(true);
const error: Ref<string> = ref("");
const searchTerm: Ref<string> = ref("");
const selectedCategory: Ref<number | null> = ref(null);
const ip = import.meta.env.VITE_IP;

// Servicios filtrados por categoría y término de búsqueda
const filteredServices = computed(() => {
  let result = services.value;
  
  // Filtrar por categoría si hay alguna seleccionada
  if (selectedCategory.value !== null) {
    result = result.filter(service => service.category && service.category.idCategory === selectedCategory.value);
  }
  
  // Filtrar por término de búsqueda
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase();
    result = result.filter(service => 
      service.name.toLowerCase().includes(term) ||
      service.description.toLowerCase().includes(term) ||
      (service.subcategory && service.subcategory.name && service.subcategory.name.toLowerCase().includes(term)) ||
      (service.category && service.category.name.toLowerCase().includes(term))
    );
  }
  
  return result;
});

// Función para obtener el nombre de la subcategoría de manera segura
const getSubcategoryName = (subcategory: any): string => {
  if (!subcategory) return 'Sin subcategoría';
  if (typeof subcategory === 'string') return subcategory;
  if (subcategory && subcategory.name) return subcategory.name;
  return 'Sin subcategoría';
};

// Agrupar servicios por subcategoría
const servicesBySubcategory = computed(() => {
  const grouped: Record<string, InsuranceService[]> = {};
  
  filteredServices.value.forEach(service => {
    const subcategoryName = getSubcategoryName(service.subcategory);
    if (!grouped[subcategoryName]) {
      grouped[subcategoryName] = [];
    }
    grouped[subcategoryName].push(service);
  });
  
  return grouped;
});

// Cargar categorías y servicios
const fetchData = async () => {
  try {
    loading.value = true;
    error.value = "";
    
    const [categoriesResponse, servicesResponse] = await Promise.all([
      axios.get(getInsuranceApiUrl("/category")),
      axios.get(getInsuranceApiUrl("/insurance-services"))
    ]);
    
    categories.value = categoriesResponse.data;
    services.value = servicesResponse.data;
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

// Inicializar
onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold mb-6">Catálogo de Servicios Cubiertos por el Seguro</h1>
    
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
              v-for="category in categories" 
              :key="category.idCategory"
              :value="category.idCategory"
            >
              {{ category.name }}
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
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="service in services" :key="service.idInsuranceService" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {{ service.name }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ service.category ? service.category.name : 'Sin categoría' }}
                </td>
                <td class="px-6 py-4 text-sm text-gray-500">
                  {{ service.description }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ formatPrice(service.price) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span 
                    class="px-2 py-1 text-xs font-semibold rounded-full"
                    :class="service.enabled === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                  >
                    {{ service.enabled === 1 ? 'Activo' : 'Inactivo' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template> 