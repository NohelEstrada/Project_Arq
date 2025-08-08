<script setup lang="ts">
import axios from 'axios';
const currentWeekStart = ref(getMonday(new Date()));
const horasDelDia = Array.from({ length: 24 }, (_, i) => i);
const showModal = ref(false);
const selectedDate = ref(null);
const selectedHour = ref(null);
const nombrePaciente = ref("");
const duracion = ref(30);
const notas = ref("");
const appointments = ref([]);

interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: string;
  email: string;
  enabled: boolean;
}

const hospitals = ref<Hospital[]>([]);

// Add loading state if not already present
const isLoading = ref(false);
const config = useRuntimeConfig();
const ip = config.public.ip;



const fetchHospitals = async () => {
  try {
    notify({
      type: "loading",
      title: "Loading hospitals",
      description: "Please wait...",
    });
    isLoading.value = true;
    const response = await axios.get(`http://${ip}:8080/api/hospital`);
    hospitals.value = response.data;
    console.log("Hospitals obtenidos:", hospitals.value);
    notify({
      type: "success",
      title: "Hospitals loaded",
      description: "Hospitals loaded successfully",
    });
  } catch (error) {
    console.error("Error al obtener hospitals:", error);
    notify({
      type: "error",
      title: "Error loading hospitals",
      description: "Error loading hospitals",
    });
  } finally {
    isLoading.value = false;
  }
};

fetchHospitals();

function getMonday(date) {
  const d = new Date(date);
  const day = d.getDay(); // domingo=0, lunes=1, etc.
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  d.setDate(diff);
  d.setHours(0, 0, 0, 0);
  return d;
}

const diasSemana = computed(() => {
  const dias = [];
  for (let i = 0; i < 7; i++) {
    const d = new Date(currentWeekStart.value);
    d.setDate(d.getDate() + i);
    dias.push(d);
  }
  return dias;
});

const rangoFechas = computed(() => {
  if (!diasSemana.value.length) return "";
  const inicio = diasSemana.value[0];
  const fin = diasSemana.value[6];
  return (
    `${inicio.getDate()}/${inicio.getMonth() + 1}/${inicio.getFullYear()} - ` +
    `${fin.getDate()}/${fin.getMonth() + 1}/${fin.getFullYear()}`
  );
});

function formatearFecha(date) {
  const dia = date.getDate();
  const mes = date.toLocaleString("es-ES", { month: "short" });
  return `${dia} ${mes}`;
}

function nombreCortoDia(date) {
  return date
    .toLocaleDateString("es-ES", { weekday: "short" })
    .replace(".", "")
    .toLowerCase();
}

function prevWeek() {
  currentWeekStart.value.setDate(currentWeekStart.value.getDate() - 7);
  currentWeekStart.value = new Date(currentWeekStart.value);
}

function nextWeek() {
  currentWeekStart.value.setDate(currentWeekStart.value.getDate() + 7);
  currentWeekStart.value = new Date(currentWeekStart.value);
}

// Función para abrir el modal - modificada con depuración
function openAppointmentModal(date, hour) {
  console.log("Abriendo modal para:", date, hour);
  selectedDate.value = new Date(date);
  selectedHour.value = hour;
  showModal.value = true;
  console.log("Estado del modal:", showModal.value);
}

// Función para guardar la cita
function saveAppointment() {
  if (
    !selectedDate.value ||
    selectedHour.value === null ||
    !nombrePaciente.value
  ) {
    return; // Validación básica
  }

  const newAppointment = {
    id: Date.now(), // ID único simple
    date: new Date(selectedDate.value),
    hour: selectedHour.value,
    duration: parseInt(duracion.value),
    patientName: nombrePaciente.value,
    notes: notas.value,
  };

  appointments.value.push(newAppointment);

  // Resetear el formulario
  nombrePaciente.value = "";
  duracion.value = 30;
  notas.value = "";

  // Cerrar el modal
  closeModal();
}

// Función para comprobar si hay una cita en una fecha y hora específicas
function getAppointmentsForDateAndHour(date, hour) {
  return appointments.value.filter((app) => {
    const appDate = new Date(app.date);
    return (
      appDate.getDate() === date.getDate() &&
      appDate.getMonth() === date.getMonth() &&
      appDate.getFullYear() === date.getFullYear() &&
      app.hour === hour
    );
  });
}

// Función para cerrar el modal - actualizada para limpiar los campos
function closeModal() {
  showModal.value = false;
  nombrePaciente.value = "";
  duracion.value = 30;
  notas.value = "";
}

// Función para editar una cita existente
function editAppointment(appointment) {
  selectedDate.value = new Date(appointment.date);
  selectedHour.value = appointment.hour;
  nombrePaciente.value = appointment.patientName;
  duracion.value = appointment.duration;
  notas.value = appointment.notes;

  // Almacenar el ID de la cita que estamos editando
  const editingAppointmentId = appointment.id;

  // Modificar la función saveAppointment para actualizar en lugar de crear nueva
  const saveOriginal = saveAppointment;
  saveAppointment = () => {
    // Eliminar la cita anterior
    appointments.value = appointments.value.filter(
      (a) => a.id !== editingAppointmentId,
    );

    // Llamar a la función original
    saveOriginal();

    // Restaurar la función original
    saveAppointment = saveOriginal;
  };

  showModal.value = true;
}

interface Policy {
  idPolicy: number;
  percentage: number;
  creationDate: number;
  expDate: number;
  cost: number;
  enabled: number;
}

interface User {
  idUser: number;
  name: string;
  cui: number;
  phone: string;
  email: string;
  address: string;
  birthDate: number;
  role: string;
  policy: Policy;
  enabled: number;
  password: string;
}

interface Appointment {
  idAppointment: number;
  hospital: Hospital;
  user: User;
  appointmentDate: string; // Por ejemplo, "2024-01-12"
  enabled: number;
}

const fetchAppointments = async () => {
  try {
    notify({
      type: "loading",
      title: "Loading Appointments",
      description: "Please wait...",
    });
    const response = await axios.get("http://localhost:8080/api/appointment");
    console.log("Appointments obtenidos:", response.data);
    notify({
      type: "success",
      title: "Appointments loaded",
      description: "Services loaded successfully",
    });
  } catch (error) {
    console.error("Error al obtener hospitals:", error);
    notify({
      type: "error",
      title: "Error loading appointments",
      description: "Error loading services",
    });
  }
};
fetchAppointments();
</script>

<template>
  <div
    class="bg-image-[url('https://cdn.pixabay.com/photo/2023/11/06/09/52/mountain-8369262_1280.jpg')] mx-auto flex w-full flex-col p-4"
  >
    <div class="mb-4 flex items-center justify-between">
      <button class="btn flex" @click="prevWeek">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          height="24px"
          viewBox="0 -960 960 960"
          width="24px"
          fill="currentColor"
        >
          <path d="M400-80 0-480l400-400 71 71-329 329 329 329-71 71Z" />
        </svg>
      </button>

      <div class="text-xl font-semibold">
        {{ rangoFechas }}
      </div>

      <button class="btn flex" @click="nextWeek">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          height="24px"
          viewBox="0 -960 960 960"
          width="24px"
          fill="currentColor"
        >
          <path d="m321-80-71-71 329-329-329-329 71-71 400 400L321-80Z" />
        </svg>
      </button>
    </div>
    <div
      class="border-primary overflow-auto rounded-3xl border-2 backdrop-blur-md"
    >
      <table class="w-full table-fixed border-collapse">
        <thead>
          <tr class="bg-background text-primary">
            <th class="w-16"></th>
            <th
              v-for="(dia, index) in diasSemana"
              :key="index"
              class="border-primary border-b text-center"
            >
              <div class="font-semibold">{{ nombreCortoDia(dia) }}</div>
              <div>{{ formatearFecha(dia) }}</div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(hora, idx) in horasDelDia"
            :key="idx"
            class="border-primary border-t"
          >
            <td
              class="border-primary bg-background hover:bg-h-background text-primary border-top border-e p-2 text-center font-semibold"
            >
              {{ hora }}:00
            </td>

            <td
              v-for="(dia, dayIndex) in diasSemana"
              :key="dayIndex"
              class="bg-background/80 border-primary hover:bg-h-background/80 border-top relative h-16 border-e p-0"
            >
              <button
                type="button"
                @click="openAppointmentModal(dia, hora)"
                class="hover:bg-primary/10 flex h-full w-full items-start justify-start p-2 text-left transition-colors duration-200"
              >
                <span
                  v-if="getAppointmentsForDateAndHour(dia, hora).length === 0"
                  class="text-xs"
                ></span>
              </button>
              <!-- Mostrar citas en esta celda -->
              <div
                v-for="appointment in getAppointmentsForDateAndHour(dia, hora)"
                :key="appointment.id"
                class="bg-accent text-primary dark:text-dark absolute inset-x-0 z-10 mx-1 cursor-pointer overflow-hidden rounded-md shadow-md"
                :style="{
                  top: '2px',
                  height: `calc(${(appointment.duration / 60) * 100}% - 4px)`,
                  maxHeight: 'calc(100% - 4px)',
                }"
                @click.stop="editAppointment(appointment)"
              >
                <div class="p-2">
                  <div class="truncate text-xs font-bold">
                    {{ appointment.patientName }}
                  </div>
                  <div class="text-xs opacity-90">
                    {{ hora }}:00 -
                    {{ hora + Math.floor(appointment.duration / 60) }}:{{
                      appointment.duration % 60 || "00"
                    }}
                  </div>
                  <div
                    v-if="appointment.notes"
                    class="mt-1 line-clamp-2 text-xs opacity-80"
                  >
                    {{ appointment.notes }}
                  </div>
                </div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal actualizado con v-model para los campos del formulario -->
    <div
      v-if="showModal"
      class="fixed inset-0 z-50 flex items-center justify-center"
    >
      <div class="fixed inset-0 bg-black/50" @click="closeModal"></div>
      <div class="card relative mx-4 w-full max-w-md rounded-lg shadow-xl">
        <div class="border-b p-4">
          <h2 class="title text-xl font-semibold">
            Configurar cita:
            {{
              selectedDate
                ? `${selectedDate.toLocaleDateString("es-ES")} a las ${selectedHour}:00`
                : ""
            }}
          </h2>
        </div>
        <div class="p-4">
          <div class="mb-4">
            <label
              class="text-primary mb-1 block text-sm font-medium"
              for="nombrePaciente"
              >Hospital</label
            >
            <input
              v-model="nombrePaciente"
              id="nombrePaciente"
              type="text"
              class="border-primary text-secondary w-full rounded-md border p-2"
            />
          </div>
          <div class="mb-4">
            <label
              class="text-primary mb-1 block text-sm font-medium"
              for="duracion"
              >Duración (minutos)</label
            >
            <select
              v-model="duracion"
              id="duracion"
              class="border-primary text-secondary w-full rounded-md border p-2"
            >
              <option value="15">15 minutos</option>
              <option value="30">30 minutos</option>
              <option value="45">45 minutos</option>
              <option value="60">1 hora</option>
            </select>
          </div>
          <div class="mb-4">
            <label
              class="text-primary mb-1 block text-sm font-medium"
              for="notas"
              >Notas</label
            >
            <textarea
              v-model="notas"
              id="notas"
              class="border-primary text-secondary w-full rounded-md border p-2"
              rows="3"
            ></textarea>
          </div>
        </div>
        <div class="border-primary flex justify-end space-x-2 border-t pt-4 px-4">
          <button class="btn btn-outline" @click="closeModal">Cancelar</button>
          <button class="btn btn-primary" @click="saveAppointment">
            Guardar cita
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
