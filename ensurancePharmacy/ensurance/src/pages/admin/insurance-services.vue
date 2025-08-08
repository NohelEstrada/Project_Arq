<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getInsuranceApiUrl } from "../../utils/api";
import axios from "axios";

interface Category {
  idCategory: number;
  name: string;
  enabled: number;
}

interface InsuranceService {
  idInsuranceService: number;
  name: string;
  description: string;
  category: Category;
  subcategory: Category;
  price: number;
  coveragePercentage: number;
  enabled: number;
}

// Inicializar services como un array vacío para evitar errores de null
const services = ref<InsuranceService[]>([]);
const categories = ref<Category[]>([]);
const loading = ref(true);
const error = ref("");
const success = ref("");
const ip = import.meta.env.VITE_IP || "localhost";

// Datos para nuevo/editar servicio
const editMode = ref(false);
const modalOpen = ref(false);
const currentService = ref<Partial<InsuranceService>>({});
const selectedCategoryId = ref<number | null>(null);
const selectedSubcategoryId = ref<number | null>(null);

// Cargar datos
const fetchServices = async () => {
  try {
    loading.value = true;
    error.value = "";
    
    const response = await axios.get(getInsuranceApiUrl("/insurance-services"));
    if (response.data) {
      services.value = response.data;
    } else {
      services.value = []; // Asegurar que sea un array vacío si no hay datos
      console.warn("La respuesta del servidor no contiene datos de servicios");
    }
  } catch (err) {
    console.error("Error al cargar servicios:", err);
    error.value = "Error al cargar los servicios. Por favor, intente nuevamente.";
    services.value = []; // Mantener como array vacío en caso de error
  } finally {
    loading.value = false;
  }
};

const fetchCategories = async () => {
  try {
    const response = await axios.get(getInsuranceApiUrl("/category"));
    if (response.data) {
      categories.value = response.data;
    } else {
      categories.value = []; // Asegurar que sea un array vacío si no hay datos
      console.warn("La respuesta del servidor no contiene datos de categorías");
    }
  } catch (err) {
    console.error("Error al cargar categorías:", err);
    error.value = "Error al cargar las categorías.";
    categories.value = []; // Mantener como array vacío en caso de error
  }
};

const openCreateModal = () => {
  editMode.value = false;
  currentService.value = {
    name: "",
    description: "",
    price: 0,
    coveragePercentage: 80,
    enabled: 1
  };
  selectedCategoryId.value = null;
  selectedSubcategoryId.value = null;
  modalOpen.value = true;
};

const openEditModal = (service: InsuranceService) => {
  editMode.value = true;
  currentService.value = { ...service };
  selectedCategoryId.value = service.category?.idCategory || null;
  selectedSubcategoryId.value = service.subcategory?.idCategory || null;
  modalOpen.value = true;
};

const closeModal = () => {
  modalOpen.value = false;
  currentService.value = {};
  selectedCategoryId.value = null;
  selectedSubcategoryId.value = null;
};

const saveService = async () => {
  if (!currentService.value.name || !selectedCategoryId.value || !selectedSubcategoryId.value) {
    error.value = "Por favor, complete todos los campos obligatorios.";
    return;
  }
  
  try {
    loading.value = true;
    error.value = "";
    
    const serviceData = {
      ...currentService.value,
      categoryId: selectedCategoryId.value,
      subcategoryId: selectedSubcategoryId.value
    };
    
    let response;
    if (editMode.value && currentService.value.idInsuranceService) {
      // Actualizar servicio existente
      response = await axios.put(
        getInsuranceApiUrl(`/insurance-services/${currentService.value.idInsuranceService}`),
        serviceData
      );
      success.value = "Servicio actualizado correctamente.";
    } else {
      // Crear nuevo servicio
      response = await axios.post(
        getInsuranceApiUrl("/insurance-services"),
        serviceData
      );
      success.value = "Servicio creado correctamente.";
    }
    
    // Recargar los servicios
    await fetchServices();
    closeModal();
    
    // Limpiar mensaje de éxito después de 3 segundos
    setTimeout(() => {
      success.value = "";
    }, 3000);
  } catch (err) {
    console.error("Error al guardar el servicio:", err);
    error.value = "Error al guardar el servicio. Por favor, intente nuevamente.";
  } finally {
    loading.value = false;
  }
};

const toggleServiceStatus = async (service: InsuranceService) => {
  try {
    loading.value = true;
    error.value = "";
    
    const updatedService = {
      enabled: service.enabled === 1 ? 0 : 1
    };
    
    await axios.put(
      getInsuranceApiUrl(`/insurance-services/${service.idInsuranceService}`),
      updatedService
    );
    
    // Actualizar estado en la vista
    service.enabled = updatedService.enabled;
    
    success.value = `Servicio ${updatedService.enabled === 1 ? 'activado' : 'desactivado'} correctamente.`;
    
    // Limpiar mensaje de éxito después de 3 segundos
    setTimeout(() => {
      success.value = "";
    }, 3000);
  } catch (err) {
    console.error("Error al cambiar estado del servicio:", err);
    error.value = "Error al cambiar el estado del servicio.";
  } finally {
    loading.value = false;
  }
};

const deleteService = async (service: InsuranceService) => {
  if (!confirm(`¿Está seguro de eliminar el servicio "${service.name}"?`)) {
    return;
  }
  
  try {
    loading.value = true;
    error.value = "";
    
    await axios.delete(getInsuranceApiUrl(`/insurance-services/${service.idInsuranceService}`));
    
    // Eliminar de la lista local
    services.value = services.value.filter(s => s.idInsuranceService !== service.idInsuranceService);
    
    success.value = "Servicio eliminado correctamente.";
    
    // Limpiar mensaje de éxito después de 3 segundos
    setTimeout(() => {
      success.value = "";
    }, 3000);
  } catch (err) {
    console.error("Error al eliminar el servicio:", err);
    error.value = "Error al eliminar el servicio.";
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  try {
    await Promise.all([fetchServices(), fetchCategories()]);
  } catch (err) {
    console.error("Error al cargar datos iniciales:", err);
    error.value = "Error al cargar datos. Por favor, recargue la página.";
  }
});
</script>

<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-6">Catálogo de Servicios del Seguro</h1>
    
    <div v-if="success" class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4" role="alert">
      <p>{{ success }}</p>
    </div>
    
    <div v-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4" role="alert">
      <p>{{ error }}</p>
    </div>
    
    <div class="mb-4 flex justify-between items-center">
      <button
        @click="openCreateModal"
        class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
      >
        Agregar Servicio
      </button>
    </div>
    
    <div v-if="loading" class="text-center py-8">
      <div class="spinner"></div>
      <p class="mt-2 text-gray-600">Cargando servicios...</p>
    </div>
    
    <!-- Usando v-else-if para asegurar que services exista y tenga longitud 0 -->
    <div v-else-if="!services || services.length === 0" class="text-center py-8">
      <p class="text-gray-600">No hay servicios registrados en el catálogo.</p>
    </div>
    
    <!-- Usando v-else para mostrar la tabla solo cuando services existe y tiene elementos -->
    <div v-else class="overflow-x-auto">
      <table class="min-w-full bg-white border border-gray-200">
        <thead>
          <tr>
            <th class="px-4 py-2 border-b text-left">Nombre</th>
            <th class="px-4 py-2 border-b text-left">Descripción</th>
            <th class="px-4 py-2 border-b text-left">Categoría</th>
            <th class="px-4 py-2 border-b text-left">Subcategoría</th>
            <th class="px-4 py-2 border-b text-right">Precio (Q)</th>
            <th class="px-4 py-2 border-b text-center">Cobertura (%)</th>
            <th class="px-4 py-2 border-b text-center">Estado</th>
            <th class="px-4 py-2 border-b text-center">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="service in services" :key="service.idInsuranceService" class="hover:bg-gray-50">
            <td class="px-4 py-2 border-b">{{ service.name }}</td>
            <td class="px-4 py-2 border-b">{{ service.description }}</td>
            <td class="px-4 py-2 border-b">{{ service.category?.name || 'N/A' }}</td>
            <td class="px-4 py-2 border-b">{{ service.subcategory?.name || 'N/A' }}</td>
            <td class="px-4 py-2 border-b text-right">{{ service.price?.toFixed(2) || '0.00' }}</td>
            <td class="px-4 py-2 border-b text-center">{{ service.coveragePercentage || '0' }}%</td>
            <td class="px-4 py-2 border-b text-center">
              <span 
                :class="service.enabled === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                class="px-2 py-1 rounded-full text-xs font-medium"
              >
                {{ service.enabled === 1 ? 'Activo' : 'Inactivo' }}
              </span>
            </td>
            <td class="px-4 py-2 border-b text-center">
              <button 
                @click="openEditModal(service)"
                class="text-blue-600 hover:text-blue-800 mr-2"
              >
                Editar
              </button>
              <button 
                @click="toggleServiceStatus(service)"
                :class="service.enabled === 1 ? 'text-red-600 hover:text-red-800' : 'text-green-600 hover:text-green-800'"
                class="mr-2"
              >
                {{ service.enabled === 1 ? 'Desactivar' : 'Activar' }}
              </button>
              <button 
                @click="deleteService(service)"
                class="text-red-600 hover:text-red-800"
              >
                Eliminar
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Modal para crear/editar servicio -->
    <div v-if="modalOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-xl">
        <h2 class="text-xl font-semibold mb-4">
          {{ editMode ? 'Editar Servicio' : 'Crear Nuevo Servicio' }}
        </h2>
        
        <form @submit.prevent="saveService" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700">Nombre</label>
            <input 
              v-model="currentService.name" 
              type="text" 
              required
              class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700">Descripción</label>
            <textarea 
              v-model="currentService.description" 
              rows="3"
              class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            ></textarea>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Categoría</label>
              <select 
                v-model="selectedCategoryId"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              >
                <option :value="null">Seleccione una categoría</option>
                <option 
                  v-for="category in categories.filter(c => c.enabled === 1)" 
                  :key="category.idCategory"
                  :value="category.idCategory"
                >
                  {{ category.name }}
                </option>
              </select>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700">Subcategoría</label>
              <select 
                v-model="selectedSubcategoryId"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              >
                <option :value="null">Seleccione una subcategoría</option>
                <option 
                  v-for="category in categories.filter(c => c.enabled === 1)" 
                  :key="category.idCategory"
                  :value="category.idCategory"
                >
                  {{ category.name }}
                </option>
              </select>
            </div>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Precio (Q)</label>
              <input 
                v-model.number="currentService.price" 
                type="number"
                min="0"
                step="0.01" 
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700">Porcentaje de Cobertura (%)</label>
              <input 
                v-model.number="currentService.coveragePercentage" 
                type="number"
                min="0"
                max="100"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>
          
          <div class="flex items-center">
            <input 
              id="enabled"
              v-model="currentService.enabled" 
              type="checkbox"
              :true-value="1"
              :false-value="0"
              class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label for="enabled" class="ml-2 block text-sm text-gray-900">Servicio activo</label>
          </div>
          
          <div class="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              @click="closeModal"
              class="px-4 py-2 bg-gray-300 text-gray-800 rounded hover:bg-gray-400"
            >
              Cancelar
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              :disabled="loading"
            >
              {{ loading ? 'Guardando...' : 'Guardar' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border-left-color: #3b82f6;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style> 