<template>
  <div class="container mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold text-center mb-6">Agendar Citas Médicas</h1>
    
    <!-- Mostrar información del hospital por defecto -->
    <div v-if="defaultHospital" class="bg-blue-50 p-3 rounded mb-4 border border-blue-200">
      <div class="flex items-center justify-between">
        <span class="text-blue-700">{{ usingDefaultHospital }}</span>
      </div>
    </div>
    
  <div class="calendar-container">
    <h1 class="text-2xl font-bold mb-6">Calendario de Citas</h1>
    
    <!-- Calendario -->
    <div class="calendar">
      <div class="calendar-header">
        <div class="time-column"></div>
        <div 
          v-for="day in weekDays" 
          :key="day.toISOString()" 
          class="day-column"
        >
          {{ formatDay(day) }}
        </div>
      </div>
      
      <div class="calendar-body">
        <div 
          v-for="time in timeSlots" 
          :key="time" 
          class="time-slot"
        >
          <div class="time-label">{{ time }}</div>
          <div 
            v-for="day in weekDays" 
            :key="day.toISOString()" 
            class="slot"
            :class="{
              'taken': isSlotTaken(day, time),
              'selected': isSlotSelected(day, time)
            }"
            @click="handleSlotClick(day, time)"
          >
            <span v-if="isSlotTaken(day, time)">Ocupado</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Formulario para nueva cita -->
    <div v-if="selectedSlot" class="appointment-form">
      <h3 class="text-xl font-semibold mb-4">
        Nueva Cita para {{ formatDate(selectedSlot.date) }} - {{ selectedSlot.time }}
      </h3>
      <p><strong>Paciente:</strong> {{ user?.name }}</p>
      
      <form @submit.prevent="submitAppointment">
        <div class="form-group">
          <label for="doctor" class="block mb-2">Doctor</label>
          <select 
            id="doctor" 
            v-model="appointment.doctor" 
            class="form-select"
            required
          >
            <option value="">Seleccione un doctor</option>
            <option v-for="doctor in doctors" :key="doctor._id" :value="doctor._id">
              {{ doctor.name || doctor.username }}
            </option>
          </select>
        </div>
        
        <div class="form-group">
          <label for="reason" class="block mb-2">Motivo de la consulta</label>
          <textarea 
            id="reason" 
            v-model="appointment.reason" 
            class="form-textarea" 
            rows="4"
            required
          ></textarea>
        </div>
        
        <div class="button-group">
          <button type="submit" class="btn-primary">Confirmar Cita</button>
          <button type="button" class="btn-danger" @click="resetForm">Cancelar</button>
        </div>
      </form>
    </div>
    
    <!-- Lista de citas agendadas -->
    <div class="my-appointments mt-8" v-if="userAppointments.length > 0">
      <h2 class="text-xl font-semibold mb-4">Mis Citas</h2>
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Hora</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Doctor</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Motivo</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="appointment in userAppointments" :key="appointment._id">
              <td class="px-6 py-4 whitespace-nowrap">{{ formatDate(new Date(appointment.start)) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">{{ formatTime(new Date(appointment.start)) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">{{ getDoctorName(appointment.doctor) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">{{ appointment.reason }}</td>
              <td class="px-6 py-4 whitespace-nowrap space-x-2">
                <button 
                  @click="deleteAppointment(appointment._id)" 
                  class="btn-danger-sm"
                >
                  Cancelar
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { getInsuranceApiUrl } from "../utils/api";  
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

// Variables globales
const defaultHospital = getDefaultHospital();
const ip = import.meta.env.VITE_IP || 'localhost';
// Usar el puerto del hospital predeterminado o 5050 como fallback
const DEFAULT_PORT = defaultHospital?.port || '5050';
// Usamos el puerto del hospital seleccionado o el valor por defecto
const HOSPITAL_API_URL = `http://${ip}:${DEFAULT_PORT}`;
// Usamos getip.py para determinar la IP del backend
const INSURANCE_API_URL = getInsuranceApiUrl("/");

// Agregar información sobre el hospital predeterminado para debug
const usingDefaultHospital = computed(() => {
  return defaultHospital ? `Usando hospital: ${defaultHospital.name} (Puerto: ${defaultHospital.port || '5050'})` : 'No hay hospital predeterminado';
});

// Estado
const weekDays = ref<Date[]>([]);
const appointments = ref<any[]>([]);
const doctors = ref<any[]>([]);
const selectedSlot = ref<{ date: Date, time: string } | null>(null);
const userAppointments = ref<any[]>([]);

// Obtener usuario actual
const user = JSON.parse(localStorage.getItem('user') || 'null');

// Modelo de formulario
const appointment = ref({
  doctor: '',
  date: '',
  time: '',
  reason: ''
});

// Horarios disponibles
const timeSlots = [
  '08:00 AM', '08:30 AM', '09:00 AM', '09:30 AM', 
  '10:00 AM', '10:30 AM', '11:00 AM', '11:30 AM',
  '12:00 PM', '12:30 PM', '01:00 PM', '01:30 PM', 
  '02:00 PM', '02:30 PM', '03:00 PM', '03:30 PM',
  '04:00 PM', '04:30 PM'
];

// Inicialización
onMounted(() => {
  console.log('API URL del hospital:', HOSPITAL_API_URL);
  console.log(usingDefaultHospital.value);
  initializeWeekDays();
  loadDoctors();
  loadAppointments();
});

// Funciones
function initializeWeekDays() {
  const today = new Date();
  weekDays.value = [];
  for (let i = 0; i < 7; i++) {
    const day = new Date(today);
    day.setDate(today.getDate() + i);
    weekDays.value.push(day);
  }
}

async function loadDoctors() {
  try {
    // Cargar doctores desde la API del hospital
    const response = await axios.get(`${HOSPITAL_API_URL}/doctors`);
    doctors.value = response.data.doctors || [];
    
    // También cargar servicios como doctores
    const servicesResponse = await axios.get(`${HOSPITAL_API_URL}/api/services/`);
    const servicesAsDoctors = (servicesResponse.data.services || []).map((s: any) => ({
      _id: s._id,
      username: s.name,
      name: s.name
    }));
    
    doctors.value = [...doctors.value, ...servicesAsDoctors];
  } catch (error) {
    console.error('Error al cargar doctores:', error);
  }
}

async function loadAppointments() {
  try {
    console.log('Cargando citas...');
    // Cargar todas las citas desde el hospital
    const response = await axios.get(`${HOSPITAL_API_URL}/api/appointments/`);
    console.log('Citas obtenidas:', response.data);
    appointments.value = response.data.appointments || [];
    
    // Filtrar citas del usuario actual
    if (user) {
      // Buscar si el usuario tiene un registro en el sistema del hospital por email
      const userEmail = user.email;
      try {
        // Intentar obtener la lista de usuarios
        const usersResponse = await axios.get(`${HOSPITAL_API_URL}/users`);
        console.log('Usuarios del hospital:', usersResponse.data);
        
        // Buscar usuario en hospital por email
        let hospitalUser = null;
        
        // Verificar la estructura de la respuesta y buscar el usuario por email
        if (usersResponse.data && Array.isArray(usersResponse.data.appointments)) {
          hospitalUser = usersResponse.data.appointments.find((u: any) => u.email === userEmail);
        } else if (usersResponse.data && Array.isArray(usersResponse.data)) {
          hospitalUser = usersResponse.data.find((u: any) => u.email === userEmail);
        }
        
        console.log('Usuario encontrado en hospital:', hospitalUser);
        
        if (hospitalUser) {
          // Buscar citas con el ID del usuario en el hospital
          const userAppts = appointments.value.filter(appt => {
            // El campo patient puede ser un string o un objeto
            if (typeof appt.patient === 'object' && appt.patient && appt.patient._id) {
              return appt.patient._id === hospitalUser._id;
            } else if (typeof appt.patient === 'string') {
              return appt.patient === hospitalUser._id;
            }
            return false;
          });
          
          console.log('Citas del usuario:', userAppts);
          userAppointments.value = userAppts;
        } else {
          console.log('Usuario no encontrado en el sistema de hospital');
          userAppointments.value = [];
        }
      } catch (error: any) {
        console.error('Error al buscar usuario por email:', error);
        userAppointments.value = [];
      }
    }
  } catch (error: any) {
    console.error('Error al cargar citas:', error);
    appointments.value = [];
    userAppointments.value = [];
  }
}

function isSlotTaken(day: Date, time: string): boolean {
  const time24 = convertTo24(time);
  
  return appointments.value.some((appt) => {
    if (!appt.start) return false;
    const date = new Date(appt.start);
    return (
      date.getFullYear() === day.getFullYear() &&
      date.getMonth() === day.getMonth() &&
      date.getDate() === day.getDate() &&
      date.toTimeString().slice(0, 5) === time24
    );
  });
}

function isSlotSelected(day: Date, time: string): boolean {
  if (!selectedSlot.value) return false;
  return (
    selectedSlot.value.date.toDateString() === day.toDateString() &&
    selectedSlot.value.time === time
  );
}

function handleSlotClick(day: Date, time: string) {
  if (isSlotTaken(day, time)) {
    return;
  }
  
  selectedSlot.value = { date: day, time };
  const year = day.getFullYear();
  const month = (day.getMonth() + 1).toString().padStart(2, '0');
  const date = day.getDate().toString().padStart(2, '0');
  appointment.value.date = `${year}-${month}-${date}`;
  appointment.value.time = time;
}

function convertTo24(time: string): string {
  const [timePart, period] = time.split(' ');
  let [hour, minute] = timePart.split(':').map(Number);
  if (period === 'PM' && hour < 12) hour += 12;
  if (period === 'AM' && hour === 12) hour = 0;
  return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
}

async function submitAppointment() {
  if (!appointment.value.doctor) {
    alert('Debe seleccionar un doctor');
    return;
  }
  
  if (!user) {
    alert('Debe iniciar sesión para agendar una cita');
    return;
  }
  
  try {
    // Buscar usuario en hospital por email o crearlo si no existe
    let hospitalUserId;
    let doctorName = '';
    try {
      const userByEmailResponse = await axios.get(`${HOSPITAL_API_URL}/users?email=${user.email}`);
      const foundUser = userByEmailResponse.data && 
                        userByEmailResponse.data.appointments && 
                        userByEmailResponse.data.appointments.find((u: any) => u.email === user.email);
      
      // Obtener el nombre del doctor seleccionado
      const selectedDoctor = doctors.value.find(d => d._id === appointment.value.doctor);
      if (selectedDoctor) {
        doctorName = selectedDoctor.name || selectedDoctor.username;
      }
      
      if (foundUser) {
        hospitalUserId = foundUser._id;
      } else {
        // Crear usuario en hospital si no existe
        const registerResponse = await axios.post(`${HOSPITAL_API_URL}/register/`, {
          username: user.name.replace(/\s+/g, '_').toLowerCase(),
          email: user.email,
          password: "TemporalPassword123", // Contraseña temporal
          name: user.name,
          rol: "paciente",
          identification: user.cui ? user.cui.toString() : '000000000'
        });
        
        if (registerResponse.data && registerResponse.data._id) {
          hospitalUserId = registerResponse.data._id;
        } else {
          throw new Error('No se pudo crear el usuario en el hospital');
        }
      }
    } catch (error: any) {
      console.error('Error al buscar/crear usuario en hospital:', error);
      alert('Error al verificar usuario en el sistema del hospital');
      return;
    }
    
    // Preparar datos para la cita en formato del hospital
    const [timePart, period] = appointment.value.time.split(' ');
    let [hour, minute] = timePart.split(':').map(Number);
    if (period === 'PM' && hour < 12) hour += 12;
    if (period === 'AM' && hour === 12) hour = 0;
    const time24 = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
    
    const startDateTime = `${appointment.value.date}T${time24}:00`;
    
    // Construir el payload exactamente como se espera
    const payload = {
      doctor: appointment.value.doctor,
      start: startDateTime,
      reason: appointment.value.reason,
      patient: hospitalUserId
    };
    
    console.log('Enviando payload de cita:', payload);
    
    // Crear cita en el sistema del hospital
    const hospitalResponse = await axios.post(
      `${HOSPITAL_API_URL}/api/appointments/create/`, 
      payload
    );
    
    console.log('Respuesta del hospital:', hospitalResponse.data);
    
    if (!hospitalResponse.data || !hospitalResponse.data._id) {
      throw new Error('No se pudo crear la cita en el hospital');
    }
    
    const hospitalAppointmentId = hospitalResponse.data._id;
    
    // Guardar la cita en el sistema de seguros con la nueva entidad
    try {
      // Preparar datos para la nueva API de citas del seguro
      const ensuranceAppointmentData = {
        hospitalAppointmentId: hospitalAppointmentId,
        idUser: user.idUser,
        appointmentDate: appointment.value.date,
        doctorName: doctorName,
        reason: appointment.value.reason
      };
      
      console.log('Guardando cita en ensurance con datos:', ensuranceAppointmentData);
      
      // Llamar a la nueva API
      const response = await axios.post(
        `${INSURANCE_API_URL}/ensurance-appointments`, 
        ensuranceAppointmentData,
        {
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );
      
      console.log('Cita guardada en seguros:', response.data);
      
    } catch (insuranceError: any) {
      console.error('Error al guardar en seguros:', insuranceError);
      console.log('Detalles del error:', insuranceError.response?.data || 'No hay detalles');
      // Continuamos incluso si hay error, ya que lo principal es la cita en hospital
    }
    
    // Recargar datos
    await loadAppointments();
    resetForm();
    
    alert('Cita agendada correctamente');
  } catch (error: any) {
    console.error('Error al crear cita:', error);
    
    // Extraer mensaje de error de la respuesta, similar al código Angular
    const errorMessage = error.response?.data?.error || 
                        error.response?.data?.detail || 
                        error.message || 
                        'Error desconocido';
    
    alert('Error al crear la cita: ' + errorMessage);
  }
}

async function deleteAppointment(appointmentId: string) {
  if (confirm('¿Está seguro de cancelar esta cita?')) {
    try {
      console.log('Cancelando cita con ID:', appointmentId);
      
      // Eliminar del sistema del hospital
      await axios.delete(`${HOSPITAL_API_URL}/api/appointments/${appointmentId}/delete/`);
      console.log('Cita eliminada del sistema de hospital');
      
      // Buscar y eliminar del sistema de seguros
      try {
        console.log('Eliminando cita de seguros con ID hospital:', appointmentId);
        
        // Usar la nueva API para eliminar por ID de hospital
        await axios.delete(`${INSURANCE_API_URL}/ensurance-appointments?hospitalId=${appointmentId}`);
        console.log('Cita eliminada del sistema de seguros');
      } catch (insuranceError: any) {
        console.error('Error al eliminar cita del sistema de seguros:', insuranceError);
        // Continuamos incluso si hay error aquí, ya que lo importante es que se eliminó del hospital
      }
      
      // Recargar los datos
      await loadAppointments();
      alert('Cita cancelada correctamente');
    } catch (error: any) {
      console.error('Error al cancelar cita:', error);
      
      // Extraer mensaje de error de la respuesta
      const errorMessage = error.response?.data?.error || 
                          error.response?.data?.detail || 
                          error.message || 
                          'Error desconocido';
      
      alert('Error al cancelar la cita: ' + errorMessage);
    }
  }
}

function resetForm() {
  selectedSlot.value = null;
  appointment.value = { doctor: '', date: '', time: '', reason: '' };
}

function formatDay(date: Date): string {
  return date.toLocaleDateString('es-ES', { weekday: 'short', day: '2-digit', month: '2-digit' });
}

function formatDate(date: Date): string {
  return date.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function formatTime(date: Date): string {
  return date.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
}

function getDoctorName(doctorId: string): string {
  const doctor = doctors.value.find(d => d._id === doctorId);
  return doctor ? (doctor.name || doctor.username) : doctorId;
}
</script>

<style scoped>
.calendar-container {
  padding: 2rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.calendar {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: auto;
}

.calendar-header {
  display: grid;
  grid-template-columns: 80px repeat(7, 1fr);
  background-color: #f8f9fa;
  border-bottom: 1px solid #ddd;
}

.day-column,
.time-label {
  padding: 1rem;
  text-align: center;
  font-weight: bold;
}

.calendar-body {
  display: flex;
  flex-direction: column;
}

.time-slot {
  display: grid;
  grid-template-columns: 80px repeat(7, 1fr);
  border-bottom: 1px solid #eee;
}

.slot {
  border-left: 1px solid #eee;
  padding: 0.5rem;
  min-height: 40px;
  cursor: pointer;
}

.slot:hover:not(.taken) {
  background-color: #e3f2fd;
}

.slot.taken {
  background-color: #a52019;
  color: white;
  cursor: not-allowed;
}

.slot.selected {
  background-color: #bbdefb;
}

.appointment-form {
  margin-top: 2rem;
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-group {
  margin-bottom: 1rem;
}

.form-select, .form-textarea {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0.5rem;
}

.button-group {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1rem;
}

.btn-primary {
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0.5rem 1rem;
  cursor: pointer;
}

.btn-danger {
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0.5rem 1rem;
  cursor: pointer;
}

.btn-danger-sm {
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
  cursor: pointer;
}

.btn-primary:hover {
  background-color: #217dbb;
}

.btn-danger:hover, .btn-danger-sm:hover {
  background-color: #c0392b;
}
</style> 