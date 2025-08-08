<script setup lang="ts">
import axios from 'axios';
import {toJSON} from "flatted";
import { getInsuranceApiUrl } from "../../utils/api"; 
interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: number;
  email: string;
  enabled: number;
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
  appointmentDate: string; // "2024-01-12"
  enabled: number;
}

const users: Ref<User[]> = ref([]);
const appointments: Ref<Appointment[]> = ref([]);
const config = useRuntimeConfig();
const ip = config.public.ip;

let appointmentChanges: Appointment[] = [];
const fetchAppointments = async (userId: number) => {
  try {
    notify({
      type: "loading",
      title: "Loading policies",
      description: "Please wait...",
    });
    const response = await axios.get(getInsuranceApiUrl(`/appointment?user_id=${userId}`));
    appointments.value = response.data;
    appointmentChanges = response.data.map((appointment: Appointment) => ({ ...appointments }));
    notify({
      type: "success",
      title: "Appointments loaded",
      description: "Appointments loaded successfully",
    });
  } catch (error) {
    console.error("Error al obtener appointments:", error);
    notify({
      type: "error",
      title: "Error loading policies",
      description: "Error loading policies",
    });
  }
};

let userChanges: User[] = [];
const fetchUsers = async () => {
  try {
    notify({
      type: "loading",
      title: "Loading policies",
      description: "Please wait...",
    });
    const response = await axios.get(getInsuranceApiUrl("/users"));
    users.value = response.data;
    userChanges = response.data.map((user: User) => ({ ...user }));
    console.log(users.value);
    notify({
      type: "success",
      title: "Policies loaded",
      description: "Policies loaded successfully",
    });
  } catch (error) {
    console.error("Error al obtener hospitals:", error);
    notify({
      type: "error",
      title: "Error loading policies",
      description: "Error loading policies",
    });
  }
};
fetchUsers();

interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: number;
  email: string;
  enabled: number;
}

export interface Pharmacy {
  idPharmacy: number;
  name: string;
  address: string;
  phone: number;
  email: string;
  enabled: number;
}

export interface Medicine {
  idMedicine: number;
  name: string;
  description: string;
  price: number;
  pharmacy: Pharmacy;
  enabled: number;
  activePrinciple: string;
  presentation: string;
  stock: number;
  brand: string;
  coverage: number | null;
}

export interface Prescription {
  idPrescription: number;
  hospital: Hospital;
  user: User;
  medicine: Medicine;
  pharmacy: Pharmacy;
  prescriptionDate: string;
  total: number;
  copay: number;
  prescriptionComment: string;
  secured: number;
  auth: string;
}

const prescriptions: Ref<Prescription[]> = ref([]);
const fetchPrescription = async (userId : number) => {
  try {
    notify({
      type: "loading",
      title: "Loading services",
      description: "Please wait...",
    });
    const response = await axios.get(getInsuranceApiUrl(`/prescription?user_id=${userId}`));
    console.log("Prescriptions obtenidos:", response.data);
    prescriptions.value = response.data;
    notify({
      type: "success",
      title: "Services loaded",
      description: "Services loaded successfully",
    });
  } catch (error) {
    console.error("Error al obtener hospitals:", error);
    notify({
      type: "error",
      title: "Error loading services",
      description: "Error loading services",
    });
  }
};


const viewMode = ref('basic'); // 'basic' or 'detail'
const selectedUser: Ref<typeof users.value[0] | null> = ref(null);
const edit = useEdit();

function toggleViewMode() {
  viewMode.value = viewMode.value === 'basic' ? 'detail' : 'basic';
}

function selectUser(user: typeof users.value[0]) {
  selectedUser.value = user;
  fetchAppointments(user.idUser);
  fetchPrescription(user.idUser)
}
const search = useSearch();
</script>

<template>
  <div class="from-s-background to-sh-background h-full w-full bg-gradient-to-br py-8">
    <Search v-if="search" :fieldNames="['Name', 'CUI', 'Phone', 'Email', 'Address', 'Birthdate', 'Role', 'Policy']"
            :searchFields="['name', 'cui', 'phone', 'email', 'address', 'birthdate', 'role', 'policy']"
            v-model:output="users" />
    <div v-if="!edit" class="card mb-6">
      <div class="flex justify-between items-center mb-6">
        <h1 class="title">Users Management</h1>
        <div class="flex gap-4">
          <button @click="toggleViewMode" class="btn py-2 px-4">
            {{ viewMode === 'basic' ? 'Show Details' : 'Show Basic' }}
          </button>
        </div>
      </div>

      <!-- Users List -->
      <div v-if="!edit" class="responsive-grid">
        <div v-for="user in users" :key="user.cui"
             class="bg-s-background hover:bg-h-secondary mb-6 rounded-lg px-4 py-4 cursor-pointer"
             @click="selectUser(user)">
          <div class="flex items-center mb-4">
            <div
                class="h-16 w-16 bg-accent rounded-full flex items-center justify-center text-white text-xl font-bold mr-4">
              {{ user.name.charAt(0) }}
            </div>
            <div>
              <h2 class="text-primary text-lg font-semibold">{{ user.name }}</h2>
              <p class="text-secondary">{{ user.role }}</p>
            </div>
          </div>

          <!-- Basic Information (Always visible) -->
          <div class="text-primary mb-4 flex">
            <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
                 fill="currentColor">
              <path
                  d="M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H160Zm320-280L160-640v400h640v-400L480-440Zm0-80 320-200H160l320 200ZM160-640v-80 480-400Z" />
            </svg>
            <p class="me-2 font-semibold">E-Mail:</p>
            {{ user.email }}
          </div>
          <div class="text-primary mb-4 flex">
            <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
                 fill="currentColor">
              <path
                  d="M798-120q-125 0-247-54.5T329-329Q229-429 174.5-551T120-798q0-18 12-30t30-12h162q14 0 25 9.5t13 22.5l26 140q2 16-1 27t-11 19l-97 98q20 37 47.5 71.5T387-386q31 31 65 57.5t72 48.5l94-94q9-9 23.5-13.5T670-390l138 28q14 4 23 14.5t9 23.5v162q0 18-12 30t-30 12ZM241-600l66-66-17-94h-89q5 41 14 81t26 79Zm358 358q39 17 79.5 27t81.5 13v-88l-94-19-67 67ZM241-600Zm358 358Z" />
            </svg>
            <p class="me-2 font-semibold">Phone:</p>
            {{ user.phone }}
          </div>

          <!-- Detailed Information (Only visible in detail mode) -->
          <div v-if="viewMode === 'detail'">
            <div class="text-secondary mb-4 flex text-sm">
              <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
                   fill="currentColor">
                <path
                    d="M240-200h120v-240h240v240h120v-360L480-740 240-560v360Zm-80 80v-480l320-240 320 240v480H520v-240h-80v240H160Zm320-350Z" />
              </svg>
              <p class="me-2 font-semibold">Address:</p>
              {{ user.address }}
            </div>
            <div class="text-secondary mb-4 flex text-sm">
              <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
                   fill="currentColor">
                <path
                    d="M160-80q-17 0-28.5-11.5T120-120v-200q0-33 23.5-56.5T200-400v-160q0-33 23.5-56.5T280-640h160v-58q-18-12-29-29t-11-41q0-15 6-29.5t18-26.5l56-56 56 56q12 12 18 26.5t6 29.5q0 24-11 41t-29 29v58h160q33 0 56.5 23.5T760-560v160q33 0 56.5 23.5T840-320v200q0 17-11.5 28.5T800-80H160Zm120-320h400v-160H280v160Zm-80 240h560v-160H200v160Zm80-240h400-400Zm-80 240h560-560Z" />
              </svg>
              <p class="me-2 font-semibold">Birthday:</p>
              {{ user.birthDate }}
            </div>
            <div class="text-secondary mb-4 flex text-sm">
              <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
                   fill="currentColor">
                <path
                    d="M160-80q-33 0-56.5-23.5T80-160v-440q0-33 23.5-56.5T160-680h200v-120q0-33 23.5-56.5T440-880h80q33 0 56.5 23.5T600-800v120h200q33 0 56.5 23.5T880-600v440q0 33-23.5 56.5T800-80H160Zm0-80h640v-440H600q0 33-23.5 56.5T520-520h-80q-33 0-56.5-23.5T360-600H160v440Zm80-80h240v-18q0-17-9.5-31.5T444-312q-20-9-40.5-13.5T360-330q-23 0-43.5 4.5T276-312q-17 8-26.5 22.5T240-258v18Zm320-60h160v-60H560v60Zm-200-60q25 0 42.5-17.5T420-420q0-25-17.5-42.5T360-480q-25 0-42.5 17.5T300-420q0 25 17.5 42.5T360-360Zm200-60h160v-60H560v60ZM440-600h80v-200h-80v200Zm40 220Z" />
              </svg>
              <p class="me-2 font-semibold">CUI:</p>
              {{ user.cui }}
            </div>
            <div class="text-success mb-4 flex">
              <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
                   fill="currentColor">
                <path
                    d="M280-280h280v-80H280v80Zm0-160h400v-160H280v160Zm0-160h400v-80H280v80Zm-80 480q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160Zm-80 34L646-760H200v560h560v-446ZM480-240q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35ZM240-560h360v-160H240v160Zm-40-86v446-560 114Z" />
              </svg>
              <p class="me-2 font-semibold">Policy:</p>
              {{ user.policy.percentage }}
            </div>

            <!-- Prescriptions Summary -->
            <div class="mt-6 border-t pt-4">
              <div class="flex justify-between mb-2">
                <h3 class="text-primary font-semibold">Prescriptions</h3>
                <span class="bg-accent/20 text-primary px-2 py-1 rounded-full text-xs">
                  {{ prescriptions.length }} total
                </span>
              </div>

              <!-- Prescription List (condensed) -->
              <div v-for="prescription in prescriptions" :key="prescription.idPrescription"
                   class="text-secondary text-sm mb-2">
                <div class="flex justify-between">
                  <span>#{{ prescription.idPrescription }} - {{ prescription.hospital.name }}</span>
                  <span>${{ prescription.total.toFixed(2) }}</span>
                </div>
              </div>
            </div>

            <!-- Appointments Summary -->
            <div class="mt-4 border-t pt-4">
              <div class="flex justify-between mb-2">
                <h3 class="text-primary font-semibold">Appointments</h3>
                <span class="bg-accent/20 text-primary px-2 py-1 rounded-full text-xs">
                  {{ appointments.length }} total
                </span>
              </div>

              <!-- Appointment List (condensed) -->
              <div v-for="appointment in appointments" :key="appointment.idAppointment"
                   class="text-secondary text-sm mb-2">
                <div class="flex justify-between">
                  <span>#{{ appointment.idAppointment }} - {{ appointment.hospital }}</span>
                  <span>{{ appointment.appointmentDate }}</span>
                </div>
              </div>
            </div>

            <!-- Edit Buttons (Only when edit is enabled) -->
            <div v-if="edit" class="mt-6 flex justify-end gap-2">
              <button class="btn-secondary py-1 px-3 rounded-md text-sm">Edit</button>
              <button class="bg-error hover:bg-h-error text-white py-1 px-3 rounded-md text-sm">
                <svg class="inline-block" xmlns="http://www.w3.org/2000/svg" height="18px" viewBox="0 -960 960 960"
                     width="18px" fill="currentColor">
                  <path
                      d="M280-120q-33 0-56.5-23.5T200-200v-520h-40v-80h200v-40h240v40h200v80h-40v520q0 33-23.5 56.5T680-120H280Zm400-600H280v520h400v-520ZM360-280h80v-360h-80v360Zm160 0h80v-360h-80v360ZM280-720v520-520Z" />
                </svg>
                Delete
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- User Detail Modal (when a user is selected) -->
    <div v-if="selectedUser" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-s-background rounded-lg shadow-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto p-6">
        <div class="flex justify-between items-center mb-6">
          <h2 class="title text-xl">{{ selectedUser.name }}'s Details</h2>
          <button @click="selectedUser = null" class="text-primary hover:text-accent">
            <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
                 fill="currentColor">
              <path
                  d="M480-424 284-228q-11 11-28 11t-28-11q-11-11-11-28t11-28l196-196-196-196q-11-11-11-28t11-28q11-11 28-11t28 11l196 196 196-196q11-11 28-11t28 11q11 11 11 28t-11 28L536-480l196 196q11 11 11 28t-11 28q-11 11-28 11t-28-11L480-424Z" />
            </svg>
          </button>
        </div>

        <!-- User Profile Information -->
        <div class="mb-6">
          <h3 class="text-primary font-semibold mb-4">Profile Information</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="text-primary flex">
              <p class="me-2 font-semibold">Name:</p>
              {{ selectedUser.name }}
            </div>
            <div class="text-primary flex">
              <p class="me-2 font-semibold">Email:</p>
              {{ selectedUser.email }}
            </div>
            <div class="text-primary flex">
              <p class="me-2 font-semibold">Phone:</p>
              {{ selectedUser.phone }}
            </div>
            <div class="text-primary flex">
              <p class="me-2 font-semibold">Address:</p>
              {{ selectedUser.address }}
            </div>
            <div class="text-primary flex">
              <p class="me-2 font-semibold">Birthday:</p>
              {{ selectedUser.birthDate }}
            </div>
            <div class="text-primary flex">
              <p class="me-2 font-semibold">CUI:</p>
              {{ selectedUser.cui }}
            </div>
            <div class="text-primary flex">
              <p class="me-2 font-semibold">Role:</p>
              {{ selectedUser.role }}
            </div>
            <div class="text-primary flex">
              <p class="me-2 font-semibold">Policy:</p>
              {{ selectedUser.policy?.percentage  }}
            </div>
          </div>
        </div>

        <!-- User Prescriptions -->
        <div class="mb-6 border-t pt-4">
          <h3 class="text-primary font-semibold mb-4">Prescriptions</h3>
          <div v-for="prescription in prescriptions" :key="prescription.idPrescription"
               class="bg-s-background hover:bg-h-secondary mb-4 rounded-lg px-4 py-2">
            <div class="text-primary mb-4 flex">
              <div class="font-semibold me-2">ID: {{ prescription.idPrescription }}</div>
              - {{ prescription.hospital.name }} ({{ prescription.prescriptionDate }})
            </div>
            <div class="text-secondary mb-4 flex text-sm">
              <p class="me-2 font-semibold">Pharmacy:</p>
              {{ prescription.pharmacy.name }}
            </div>
            <div class="text-h-terciary mb-4 flex">
              <p class="me-2 font-semibold">Comments:</p>
              <span class="italic text-terciary">{{ prescription.prescriptionComment }}</span>
            </div>
            <div class="flex justify-between mt-4">
              <span class="text-error font-bold" v-if="!prescription.secured">
                <svg class="inline-block me-1" xmlns="http://www.w3.org/2000/svg" height="18px" viewBox="0 -960 960 960"
                     width="18px" fill="currentColor">
                  <path
                      d="M480-280q17 0 28.5-11.5T520-320q0-17-11.5-28.5T480-360q-17 0-28.5 11.5T440-320q0 17 11.5 28.5T480-280Zm-40-160h80v-240h-80v240Zm40 360q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z" />
                </svg>
                Not secured
              </span>
              <span class="text-success font-bold" v-else>
                <svg class="inline-block me-1" xmlns="http://www.w3.org/2000/svg" height="18px" viewBox="0 -960 960 960"
                     width="18px" fill="currentColor">
                  <path
                      d="m438-240 226-226-58-58-168 168-84-84-58 58 142 142ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Z" />
                </svg>
                Secured
              </span>
              <div class="text-accent font-bold">
                Total: ${{ prescription.total.toFixed(2) }}
              </div>
            </div>
          </div>
        </div>

        <!-- User Appointments -->
        <div class="mb-6 border-t pt-4">
          <h3 class="text-primary font-semibold mb-4">Appointments</h3>
          <div v-for="appointment in appointments" :key="appointment.idAppointment"
               class="bg-s-background hover:bg-h-secondary mb-4 rounded-lg px-4 py-2">
            <div class="text-primary mb-4 flex">
              <div class="font-semibold me-2">Number: {{ appointment.idAppointment }}</div>
            </div>
            <div class="text-primary mb-4 flex">
              <p class="me-2 font-semibold">Date:</p>
              {{ appointment.appointmentDate }}
            </div>
            <div class="text-secondary mb-4 flex text-sm">
              <p class="me-2 font-semibold">Hospital:</p>
              {{ appointment.hospital.name }}
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="flex justify-end gap-3 mt-6">
          <button @click="selectedUser = null" class="btn-secondary py-2 px-4 rounded-md">Close</button>
          <button v-if="edit" class="btn py-2 px-4 rounded-md">Edit User</button>
        </div>
      </div>
    </div>
    <div v-if="edit" v-for="user in users" :key="user.cui">
      <div class="card flex flex-col gap-2">
        <span class="text-primary">Name</span>
        <input type="text" class="text-primary field mb-4" :defaultValue="user.name" />
        <span class="text-primary">CUI</span>
        <input type="text" class="text-primary field mb-4" :defaultValue="user.cui.toString()" />
        <span class="text-primary">Phone</span>
        <input type="text" class="text-primary field mb-4" :defaultValue="user.phone" />
        <span class="text-primary">Email</span>
        <input type="text" class="text-primary field mb-4" :defaultValue="user.email" />
        <span class="text-primary">Address</span>
        <input type="text" class="text-primary field mb-4" :defaultValue="user.address" />
        <span class="text-primary">Birthdate</span>
        <input type="text" class="text-primary field mb-4" :defaultValue="user.birthDate?.toString()" />
        <span class="text-primary">Role</span>
        <input type="text" class="text-primary field mb-4" :defaultValue="user.role" />
        <span class="text-primary">Policy</span>
        <input type="text" class="text-primary field mb-4" :defaultValue="user.policy?.percentage?.toString()" />
        <button class="btn">Save</button>
      </div>
    </div>
    <button v-if="edit" class="btn mx-auto flex justify-center mb-6" @click="() => {
      users.push({
        name: '',
        cui: 0,
        phone: '',
        email: '',
        address: '',
        birthDate: 0,
        role: '',
        policy: {
            idPolicy: 0,
  percentage: 0,
  creationDate: 0,
  expDate: 0,
  cost: 0,
  enabled: 0
        },
        prescriptions: [],
        appointments: []
      });
    }">
      Add User
    </button>
  </div>

</template>

<style scoped>
.responsive-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.btn-secondary {
  background-color: var(--color-s-background);
  color: var(--color-primary);
  border: 1px solid var(--color-terciary);
}

.btn-secondary:hover {
  background-color: var(--color-sh-background);
}
</style>