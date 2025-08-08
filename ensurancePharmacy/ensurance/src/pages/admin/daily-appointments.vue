<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { getInsuranceApiUrl } from "../../utils/api";
interface Appointment {
  idAppointment: number;
  hospitalAppointmentId: string;
  idUser: number;
  appointmentDate: string;
  doctorName: string;
  reason: string;
  // Datos adicionales que pueden ser enriquecidos desde el backend
  userName?: string;
  userEmail?: string;
  status?: string;
}

// Función para obtener el hospital predeterminado
const getDefaultHospital = () => {
  try {
    const storedHospital = localStorage.getItem('defaultHospital');
    if (storedHospital) {
      return JSON.parse(storedHospital);
    }
    return null;
  } catch (error) {
    console.error('Error al obtener el hospital predeterminado:', error);
    return null;
  }
};

// Obtener información del hospital predeterminado
const defaultHospital = getDefaultHospital();
const ip = import.meta.env.VITE_IP || "localhost";
// Usar el puerto del hospital predeterminado o 5050 como fallback
const DEFAULT_PORT = defaultHospital?.port || '5050';

// Variables globales
const HOSPITAL_API_URL = `http://${ip}:${DEFAULT_PORT}`;
const currentDate = new Date();
const formattedDate = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}-${String(currentDate.getDate()).padStart(2, '0')}`;

// Estado
const appointments = ref<Appointment[]>([]);
const loading = ref(true);
const error = ref("");
const success = ref("");
const selectedDate = ref(formattedDate);

// Función para probar múltiples IPs
async function tryMultipleIPs(endpoint: string, method: string = 'GET', data: any = null) {
  try {
    const url = getInsuranceApiUrl(endpoint);
    console.log(`Intentando ${method} a ${url}`);
    const response = await axios({ method, url, data, timeout: 3000 });
    return response;
  } catch (error: any) {
    console.error(`Error con IP ${ip}:`, error.message);
    throw new Error("No se pudo conectar con el servidor");
  }
}

// Cargar citas para la fecha seleccionada
const fetchAppointments = async () => {
  try {
    loading.value = true;
    error.value = "";
    
    // Si es la fecha de hoy, usar el endpoint especial para hoy
    const today = new Date().toISOString().split('T')[0];
    let endpoint = '';
    
    if (selectedDate.value === today) {
      endpoint = '/ensurance-appointments?today=true';
    } else {
      endpoint = `/ensurance-appointments?date=${selectedDate.value}`;
    }
    
    const response = await tryMultipleIPs(endpoint, 'GET');
    appointments.value = response.data;
    
    // Si no hay citas para la fecha, intentamos sincronizar con el hospital
    if (appointments.value.length === 0) {
      await syncAppointmentsFromHospital();
    }
  } catch (err: any) {
    error.value = "Error al cargar las citas";
    console.error(err);
  } finally {
    loading.value = false;
  }
};

// Sincronizar citas desde el hospital
const syncAppointmentsFromHospital = async () => {
  try {
    // Obtener citas desde el hospital
    const dateFormatted = new Date(selectedDate.value).toISOString().split('T')[0];
    const response = await axios.get(`${HOSPITAL_API_URL}/api/appointments/?date=${dateFormatted}`);
    
    if (response.data && response.data.appointments && response.data.appointments.length > 0) {
      success.value = "Se encontraron citas en el hospital. Sincronizando...";
      
      const hospitalAppointments = response.data.appointments;
      for (const appt of hospitalAppointments) {
        // Buscar usuario en el sistema de seguros por email
        try {
          // Buscar el usuario asociado a la cita en el hospital
          const patientId = typeof appt.patient === 'object' ? appt.patient._id : appt.patient;
          if (!patientId) continue;
          
          const patientResponse = await axios.get(`${HOSPITAL_API_URL}/api/patients/${patientId}`);
          if (!patientResponse.data || !patientResponse.data.email) continue;
          
          // Buscar usuario en sistema de seguros por email
          const userResponse = await tryMultipleIPs(`/users/by-email?email=${patientResponse.data.email}`, 'GET');
          if (!userResponse.data || !userResponse.data.idUser) continue;
          
          // Crear la cita en el sistema de seguros
          const doctorName = appt.doctor?.name || 'Médico';
          const ensuranceAppointmentData = {
            hospitalAppointmentId: appt._id,
            idUser: userResponse.data.idUser,
            appointmentDate: new Date(appt.start).toISOString().split('T')[0],
            doctorName: doctorName,
            reason: appt.reason || 'Consulta médica'
          };
          
          // Guardar la cita en el sistema de seguros
          await tryMultipleIPs('/ensurance-appointments', 'POST', ensuranceAppointmentData);
        } catch (err) {
          console.error("Error al sincronizar cita:", err);
        }
      }
      
      // Recargar las citas después de la sincronización
      await fetchAppointments();
      success.value = "Sincronización completada";
      setTimeout(() => {
        success.value = "";
      }, 3000);
    } else {
      success.value = "No se encontraron citas en el hospital para la fecha seleccionada";
      setTimeout(() => {
        success.value = "";
      }, 3000);
    }
  } catch (err) {
    error.value = "Error al sincronizar citas desde el hospital";
    console.error(err);
  }
};

// Formatear fecha
const formatDate = (dateString: string) => {
  if (!dateString) return "N/A";
  return new Date(dateString).toLocaleDateString();
};

// Cambiar fecha y recargar citas
const changeDate = () => {
  fetchAppointments();
};

// Cargar datos al iniciar
onMounted(() => {
  fetchAppointments();
});

// Información sobre el hospital predeterminado
const usingDefaultHospital = computed(() => {
  return defaultHospital 
    ? `Hospital seleccionado: ${defaultHospital.name} (Puerto: ${defaultHospital.port || '5050'})` 
    : 'No hay hospital predeterminado seleccionado';
});
</script>

<template>
  <div class="container mx-auto p-6">
    <h1 class="text-2xl font-bold mb-6">Citas Diarias</h1>
    
    <!-- Información del hospital por defecto -->
    <div v-if="defaultHospital" class="bg-blue-50 p-3 rounded mb-4 border border-blue-200">
      <div class="flex items-center">
        <span class="text-blue-700">{{ usingDefaultHospital }}</span>
      </div>
    </div>
    
    <!-- Mensajes de éxito o error -->
    <div v-if="success" class="bg-green-100 text-green-700 p-3 mb-4 rounded">{{ success }}</div>
    <div v-if="error" class="bg-red-100 text-red-700 p-3 mb-4 rounded">{{ error }}</div>
    
    <!-- Selector de fecha -->
    <div class="mb-6 flex space-x-4 items-center">
      <div class="flex-1">
        <input
          v-model="selectedDate"
          type="date"
          @change="changeDate"
          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
      <button
        @click="syncAppointmentsFromHospital"
        class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center"
      >
        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
        </svg>
        Sincronizar con Hospital
      </button>
    </div>
    
    <!-- Lista de citas -->
    <div class="bg-white shadow-md rounded-lg overflow-hidden">
      <div v-if="loading" class="p-6 text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mx-auto"></div>
        <p class="mt-4 text-gray-600">Cargando citas...</p>
      </div>
      
      <div v-else-if="appointments.length === 0" class="p-10 text-center">
        <svg class="w-16 h-16 mx-auto text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
        </svg>
        <p class="mt-4 text-lg text-gray-600">No hay citas programadas para esta fecha</p>
        <button 
          @click="syncAppointmentsFromHospital" 
          class="mt-4 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg"
        >
          Sincronizar con Hospital
        </button>
      </div>
      
      <table v-else class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Doctor</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Motivo</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID Usuario</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="appointment in appointments" :key="appointment.idAppointment" class="hover:bg-gray-100">
            <td class="px-6 py-4 whitespace-nowrap">{{ appointment.idAppointment }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ formatDate(appointment.appointmentDate) }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ appointment.doctorName || 'No especificado' }}</td>
            <td class="px-6 py-4">{{ appointment.reason || 'No especificado' }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ appointment.idUser }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
/* Estilos adicionales si son necesarios */
</style> 