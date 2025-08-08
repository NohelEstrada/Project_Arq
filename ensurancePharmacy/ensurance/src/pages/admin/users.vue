<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import type { Ref } from "vue";
import { getInsuranceApiUrl } from "../../utils/api";
import axios from "axios";
import router from "../../router";

// Definición de interfaces
interface User {
  idUser: number;
  name: string;
  cui: number;
  phone: string;
  email: string;
  address: string;
  birthDate: string;
  role: string;
  enabled: number;
  createdAt?: string; // Campo adicional que deberías añadir en el backend
}

// Estado de la página
const users: Ref<User[]> = ref([]);
const filteredUsers: Ref<User[]> = ref([]);
const loading: Ref<boolean> = ref(true);
const error: Ref<string> = ref("");
const selectedUser: Ref<User | null> = ref(null);
const showUserModal: Ref<boolean> = ref(false);

// Filtros
const emailFilter: Ref<string> = ref("");
const roleFilter: Ref<string> = ref("");
const dateFromFilter: Ref<string> = ref("");
const dateToFilter: Ref<string> = ref("");

// Paginación
const currentPage: Ref<number> = ref(1);
const itemsPerPage: Ref<number> = ref(10);

// Roles disponibles
const availableRoles = [
  { value: "", label: "Todos los roles" },
  { value: "admin", label: "Administrador" },
  { value: "employee", label: "Empleado" },
  { value: "patient", label: "Paciente" },
  { value: "interconnection", label: "Usuario Interconexiones" }
];

// Cargar usuarios desde el backend
const fetchUsers = async () => {
  try {
    loading.value = true;
    const response = await axios.get(getInsuranceApiUrl("/users"));
    users.value = response.data;
    applyFilters();
  } catch (err: any) {
    error.value = err.response?.data?.message || "Error al cargar usuarios";
    console.error(err);
  } finally {
    loading.value = false;
  }
};

// Aplicar filtros a los usuarios
const applyFilters = () => {
  filteredUsers.value = users.value.filter((user) => {
    // Filtro por email (coincidencia parcial)
    const matchesEmail = emailFilter.value 
      ? user.email.toLowerCase().includes(emailFilter.value.toLowerCase())
      : true;
    
    // Filtro por rol
    const matchesRole = roleFilter.value 
      ? user.role === roleFilter.value
      : true;
    
    // Filtro por fecha (si existiera createdAt en tu backend)
    let matchesDate = true;
    if (user.createdAt && (dateFromFilter.value || dateToFilter.value)) {
      const userDate = new Date(user.createdAt);
      
      if (dateFromFilter.value) {
        const fromDate = new Date(dateFromFilter.value);
        if (userDate < fromDate) {
          matchesDate = false;
        }
      }
      
      if (dateToFilter.value) {
        const toDate = new Date(dateToFilter.value);
        toDate.setHours(23, 59, 59);
        if (userDate > toDate) {
          matchesDate = false;
        }
      }
    }
    
    return matchesEmail && matchesRole && matchesDate;
  });
};

// Usuarios paginados
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredUsers.value.slice(start, end);
});

// Total de páginas
const totalPages = computed(() => {
  return Math.ceil(filteredUsers.value.length / itemsPerPage.value);
});

// Navegar a la página anterior
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

// Navegar a la página siguiente
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};

// Seleccionar un usuario para ver/editar sus detalles
const selectUser = (user: User) => {
  selectedUser.value = { ...user };
  showUserModal.value = true;
};

// Guardar cambios en el usuario
const saveUser = async () => {
  if (!selectedUser.value) return;
  
  try {
    loading.value = true;
    
    // Guardar el estado anterior para comparar después
    const originalUserEnabled = users.value.find(u => u.idUser === selectedUser.value?.idUser)?.enabled || 0;
    
    const response = await axios.put(
      getInsuranceApiUrl(`/users/${selectedUser.value.idUser}`),
      selectedUser.value
    );
    
    // Actualizar lista de usuarios
    if (response.status === 200) {
      const index = users.value.findIndex(u => u.idUser === selectedUser.value?.idUser);
      if (index !== -1) {
        users.value[index] = response.data;
        
        // Comprobar si cambió de inactivo a activo
        if (originalUserEnabled === 0 && selectedUser.value.enabled === 1) {
          await sendActivationEmail(selectedUser.value);
        }
        
        applyFilters();
      }
      showUserModal.value = false;
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || "Error al actualizar el usuario";
    console.error(err);
  } finally {
    loading.value = false;
  }
};

// Enviar email de notificación al activar un usuario
const sendActivationEmail = async (user: User) => {
  try {
    await axios.post(getInsuranceApiUrl("/notifications/email"), {
      to: user.email,
      subject: "Cuenta activada en Ensurance",
      body: `Hola ${user.name}, tu cuenta ha sido activada. Ya puedes iniciar sesión en el sistema.`
    });
  } catch (error) {
    console.error("Error al enviar correo de activación:", error);
  }
};

// Cancelar la edición
const cancelEdit = () => {
  showUserModal.value = false;
  selectedUser.value = null;
};

// Inicializar la página
onMounted(() => {
  fetchUsers();
});
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold mb-6">Administración de Usuarios</h1>
    
    <!-- Filtros -->
    <div class="bg-white shadow-md rounded-lg p-6 mb-6">
      <h2 class="text-lg font-semibold mb-4">Filtros</h2>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div>
          <label for="emailFilter" class="block text-sm font-medium text-gray-700">Email</label>
          <input
            id="emailFilter"
            v-model="emailFilter"
            type="text"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"
            placeholder="Filtrar por email"
            @input="applyFilters"
          />
        </div>
        
        <div>
          <label for="roleFilter" class="block text-sm font-medium text-gray-700">Rol</label>
          <select
            id="roleFilter"
            v-model="roleFilter"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"
            @change="applyFilters"
          >
            <option v-for="role in availableRoles" :key="role.value" :value="role.value">
              {{ role.label }}
            </option>
          </select>
        </div>
        
        <div>
          <label for="dateFromFilter" class="block text-sm font-medium text-gray-700">Fecha desde</label>
          <input
            id="dateFromFilter"
            v-model="dateFromFilter"
            type="date"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"
            @change="applyFilters"
          />
        </div>
        
        <div>
          <label for="dateToFilter" class="block text-sm font-medium text-gray-700">Fecha hasta</label>
          <input
            id="dateToFilter"
            v-model="dateToFilter"
            type="date"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"
            @change="applyFilters"
          />
        </div>
      </div>
    </div>
    
    <!-- Tabla de usuarios -->
    <div class="bg-white shadow-md rounded-lg overflow-hidden">
      <div v-if="loading" class="p-4 text-center">
        <p>Cargando usuarios...</p>
      </div>
      
      <div v-else-if="error" class="p-4 text-center text-red-500">
        <p>{{ error }}</p>
      </div>
      
      <div v-else-if="filteredUsers.length === 0" class="p-4 text-center">
        <p>No se encontraron usuarios con los filtros aplicados</p>
      </div>
      
      <table v-else class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Rol</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="user in paginatedUsers" :key="user.idUser" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">{{ user.idUser }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ user.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ user.email }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span v-if="user.role" class="px-2 py-1 text-xs font-semibold rounded-full" 
                    :class="{
                      'bg-purple-100 text-purple-800': user.role === 'admin',
                      'bg-blue-100 text-blue-800': user.role === 'employee',
                      'bg-green-100 text-green-800': user.role === 'patient',
                      'bg-yellow-100 text-yellow-800': user.role === 'interconnection'
                    }">
                {{ user.role }}
              </span>
              <span v-else class="px-2 py-1 text-xs font-semibold bg-gray-100 text-gray-800 rounded-full">
                Sin rol
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="px-2 py-1 text-xs font-semibold rounded-full"
                    :class="user.enabled === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                {{ user.enabled === 1 ? 'Activo' : 'Inactivo' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <button 
                @click="selectUser(user)"
                class="text-indigo-600 hover:text-indigo-900 mr-2">
                Administrar
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- Paginación -->
      <div class="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
        <div class="flex-1 flex justify-between sm:hidden">
          <button @click="prevPage" :disabled="currentPage === 1" 
                  class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                  :class="{ 'opacity-50 cursor-not-allowed': currentPage === 1 }">
            Anterior
          </button>
          <button @click="nextPage" :disabled="currentPage === totalPages" 
                  class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                  :class="{ 'opacity-50 cursor-not-allowed': currentPage === totalPages }">
            Siguiente
          </button>
        </div>
        <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
          <div>
            <p class="text-sm text-gray-700">
              Mostrando
              <span class="font-medium">{{ (currentPage - 1) * itemsPerPage + 1 }}</span>
              a
              <span class="font-medium">{{ Math.min(currentPage * itemsPerPage, filteredUsers.length) }}</span>
              de
              <span class="font-medium">{{ filteredUsers.length }}</span>
              resultados
            </p>
          </div>
          <div>
            <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Paginación">
              <button
                @click="prevPage"
                :disabled="currentPage === 1"
                class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                :class="{ 'opacity-50 cursor-not-allowed': currentPage === 1 }">
                <span class="sr-only">Anterior</span>
                &laquo;
              </button>
              <span class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700">
                {{ currentPage }} de {{ totalPages }}
              </span>
              <button
                @click="nextPage"
                :disabled="currentPage === totalPages"
                class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                :class="{ 'opacity-50 cursor-not-allowed': currentPage === totalPages }">
                <span class="sr-only">Siguiente</span>
                &raquo;
              </button>
            </nav>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Modal para editar usuario -->
    <div v-if="showUserModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-lg p-6 w-full max-w-2xl">
        <h2 class="text-xl font-semibold mb-4">Administrar Usuario</h2>
        
        <div v-if="selectedUser" class="space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label for="userName" class="block text-sm font-medium text-gray-700">Nombre</label>
              <input
                id="userName"
                v-model="selectedUser.name"
                type="text"
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>
            
            <div>
              <label for="userEmail" class="block text-sm font-medium text-gray-700">Email</label>
              <input
                id="userEmail"
                v-model="selectedUser.email"
                type="email"
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>
            
            <div>
              <label for="userCui" class="block text-sm font-medium text-gray-700">CUI/DPI</label>
              <input
                id="userCui"
                v-model="selectedUser.cui"
                type="number"
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>
            
            <div>
              <label for="userPhone" class="block text-sm font-medium text-gray-700">Teléfono</label>
              <input
                id="userPhone"
                v-model="selectedUser.phone"
                type="text"
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>
            
            <div>
              <label for="userAddress" class="block text-sm font-medium text-gray-700">Dirección</label>
              <input
                id="userAddress"
                v-model="selectedUser.address"
                type="text"
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>
            
            <div>
              <label for="userBirthDate" class="block text-sm font-medium text-gray-700">Fecha de Nacimiento</label>
              <input
                id="userBirthDate"
                v-model="selectedUser.birthDate"
                type="date"
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
              />
            </div>
            
            <div>
              <label for="userRole" class="block text-sm font-medium text-gray-700">Rol</label>
              <select
                id="userRole"
                v-model="selectedUser.role"
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"
              >
                <option value="">Sin rol</option>
                <option value="admin">Administrador</option>
                <option value="employee">Empleado</option>
                <option value="patient">Paciente</option>
                <option value="interconnection">Usuario Interconexiones</option>
              </select>
            </div>
            
            <div>
              <label for="userEnabled" class="block text-sm font-medium text-gray-700">Estado</label>
              <select
                id="userEnabled"
                v-model="selectedUser.enabled"
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"
              >
                <option :value="1">Activo</option>
                <option :value="0">Inactivo</option>
              </select>
            </div>
          </div>
          
          <div class="flex justify-end space-x-3 mt-6">
            <button
              @click="cancelEdit"
              class="px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              @click="saveUser"
              class="px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700"
            >
              Guardar Cambios
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template> 