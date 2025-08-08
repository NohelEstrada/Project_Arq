<script setup lang="ts">
import axios from 'axios';
import { getInsuranceApiUrl } from "../../utils/api";
interface Policy {
  idPolicy: number;
  percentage: number;
  creationDate: string; // Formato: "YYYY-MM-DD"
  expDate: string;      // Formato: "YYYY-MM-DD"
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
  birthDate: string; // Formato: "YYYY-MM-DD"
  role: string;
  policy: Policy;
  enabled: number;
  password: string;
}

interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: number;
  email: string;
  enabled: number;
}

interface Transaction {
  idTransaction: number;
  user: User;
  hospital: Hospital;
  transDate: string;         // Formato: "YYYY-MM-DD"
  total: number;
  copay: number;
  transactionComment: string;
  result: string;
  covered: number;
  auth: string;
}
const transaction: Ref<Transaction[]> = ref([]);
const config = useRuntimeConfig();
const ip = config.public.ip;
const fetchTransactions = async () => {
  try {
    notify({
      type: "loading",
      title: "Loading services",
      description: "Please wait...",
    });
    const response = await axios.get(getInsuranceApiUrl("/transactions"));
    console.log("Prescriptions obtenidos:", response.data);
    transaction.value = response.data;
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
fetchTransactions();
/*const prescriptions: Ref<
Prescription[]
> = ref([
  {
    id: 1,
    hospital: "Hospital San José",
    patient: "Juan Pérez",
    date: "2023-09-01",
    total: 250,
    copay: 50,
    comments: "Consulta general",
    secured: true,
    auth_no: "AUTH001",
    result: "Success",
    show: false,
  },
  {
    id: 2,
    hospital: "Clínica Santa María",
    patient: "María López",
    date: "2023-09-03",
    total: 300,
    copay: 60,
    comments: "Revisión de seguimiento",
    secured: false,
    auth_no: "AUTH002",
    result: "Pending",
    show: false,
  },
  {
    id: 3,
    hospital: "Hospital del Sol",
    patient: "Carlos García",
    date: "2023-09-05",
    total: 150,
    copay: 30,
    comments: "Consulta urgente",
    secured: true,
    auth_no: "AUTH003",
    result: "Success",
    show: false,
  },
  {
    id: 4,
    hospital: "Clínica del Valle",
    patient: "Ana Martínez",
    date: "2023-09-07",
    total: 400,
    copay: 80,
    comments: "Chequeo periódico",
    secured: false,
    auth_no: "AUTH004",
    result: "Failed",
    show: false,
  },
  {
    id: 5,
    hospital: "Hospital Central",
    patient: "Luis Rodríguez",
    date: "2023-09-09",
    total: 500,
    copay: 100,
    comments: "Procedimiento menor",
    secured: true,
    auth_no: "AUTH005",
    result: "Success",
    show: false,
  },
  {
    id: 6,
    hospital: "Centro Médico Vida",
    patient: "Laura Sánchez",
    date: "2023-09-11",
    total: 350,
    copay: 70,
    comments: "Consulta de nutrición",
    secured: true,
    auth_no: "AUTH006",
    result: "Success",
    show: false,
  },
  {
    id: 7,
    hospital: "Hospital La Esperanza",
    patient: "Miguel Torres",
    date: "2023-09-13",
    total: 200,
    copay: 40,
    comments: "Primer ingreso",
    secured: false,
    auth_no: "AUTH007",
    result: "Pending",
    show: false,
  },
  {
    id: 8,
    hospital: "Clínica Buena Salud",
    patient: "Sofía Ramírez",
    date: "2023-09-15",
    total: 275,
    copay: 55,
    comments: "Consulta dermato",
    secured: true,
    auth_no: "AUTH008",
    result: "Success",
    show: false,
  },
  {
    id: 9,
    hospital: "Hospital Santa Cruz",
    patient: "Diego Herrera",
    date: "2023-09-17",
    total: 320,
    copay: 64,
    comments: "Chequeo anual",
    secured: true,
    auth_no: "AUTH009",
    result: "Success",
    show: false,
  },
  {
    id: 10,
    hospital: "Clínica Nueva Vida",
    patient: "Isabel Navarro",
    date: "2023-09-19",
    total: 180,
    copay: 36,
    comments: "Consulta pediátrica",
    secured: false,
    auth_no: "AUTH010",
    result: "Pending",
    show: false,
  },
  {
    id: 11,
    hospital: "Hospital San Martín",
    patient: "Ricardo Mendoza",
    date: "2023-09-21",
    total: 410,
    copay: 82,
    comments: "Consulta cardiológica",
    secured: true,
    auth_no: "AUTH011",
    result: "Success",
    show: false,
  },
  {
    id: 12,
    hospital: "Clínica del Río",
    patient: "Patricia Vega",
    date: "2023-09-23",
    total: 290,
    copay: 58,
    comments: "Control post-operatorio",
    secured: false,
    auth_no: "AUTH012",
    result: "Failed",
    show: false,
  },
  {
    id: 13,
    hospital: "Hospital Nuestra Señora",
    patient: "Fernando Ruiz",
    date: "2023-09-25",
    total: 360,
    copay: 72,
    comments: "Consulta oftalmológica",
    secured: true,
    auth_no: "AUTH013",
    result: "Success",
    show: false,
  },
  {
    id: 14,
    hospital: "Clínica Horizonte",
    patient: "Gabriela Díaz",
    date: "2023-09-27",
    total: 220,
    copay: 44,
    comments: "Revisión dental",
    secured: true,
    auth_no: "AUTH014",
    result: "Success",
    show: false,
  },
  {
    id: 15,
    hospital: "Hospital Central de Emergencias",
    patient: "Esteban Castro",
    date: "2023-09-29",
    total: 480,
    copay: 96,
    comments: "Atención de emergencia",
    secured: false,
    auth_no: "AUTH015",
    result: "Pending",
    show: false,
  },
]);*/
const edit = useEdit();
const search = useSearch();
</script>

<template>
  <main
    class="bg-image-[url('https://cdn.prod.website-files.com/6466101d017ab9d60c8d0137/668ce90cd1d21a2f4e8a8536_Repeat%20Prescriptions.jpg')] max-sm:flex-col max-sm:items-center max-sm:justify-center max-sm:gap-4 max-sm:px-2 max-sm:py-8 md:grid md:grid-cols-2 md:gap-4 md:px-8 md:py-16 lg:grid-cols-4 lg:gap-2 xl:grid-cols-4">
    <Search v-if="search"
      :fieldNames="['Hospital', 'Patient', 'Date', 'Total', 'Copay', 'Comments', 'Secured', 'Auth No', 'Result']"
      :searchFields="['hospital', 'patient', 'date', 'total', 'copay', 'comments', 'secured', 'auth_no', 'result']"
      v-model:output="transaction" />
    <div v-if="!edit" v-for="transactions in transaction"
      class="card lg:align-center gap-4 transition duration-300 hover:scale-105 max-sm:mx-2 max-sm:flex-col md:flex-row lg:align-middle">
      <p class="text-title title mb-6">Number: {{ transactions.idTransaction }}</p>
      <p class="text-primary mb-3 flex text-lg">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
          fill="currentColor">
          <path
            d="M420-280h120v-140h140v-120H540v-140H420v140H280v120h140v140ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z" />
        </svg>
        <strong class="me-2">Hospital:</strong> {{ transactions.hospital.name }}
      </p>
      <div class="text-md text-primary mb-4 flex">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
          fill="currentColor">
          <path
            d="M480-480q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47ZM160-160v-112q0-34 17.5-62.5T224-378q62-31 126-46.5T480-440q66 0 130 15.5T736-378q29 15 46.5 43.5T800-272v112H160Zm80-80h480v-32q0-11-5.5-20T700-306q-54-27-109-40.5T480-360q-56 0-111 13.5T260-306q-9 5-14.5 14t-5.5 20v32Zm240-320q33 0 56.5-23.5T560-640q0-33-23.5-56.5T480-720q-33 0-56.5 23.5T400-640q0 33 23.5 56.5T480-560Zm0-80Zm0 400Z" />
        </svg>
        <p class="me-2 font-semibold">Usuario:</p>

        {{ transactions.user.name }}
      </div>
      <div class="text-secondary mb-6 flex text-sm">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
          fill="#e8eaed">
          <path
            d="m612-292 56-56-148-148v-184h-80v216l172.16.57.55 172.16.57.55ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-400Zm0 320q133 0 226.5-93.5T800-480q0-133-93.5-226.5T480-800q-133 0-226.5 93.5T160-480q0 133 93.5 226.5T480-160Z" />
        </svg>
        <p class="text-tertiary me-2 font-semibold">Date:</p>
        {{ transactions.transDate }}
      </div>
      <div class="text-secondary mb-6 flex text-sm">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
             fill="#e8eaed">
          <path
              d="m612-292 56-56-148-148v-184h-80v216l172.16.57.55 172.16.57.55ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-400Zm0 320q133 0 226.5-93.5T800-480q0-133-93.5-226.5T480-800q-133 0-226.5 93.5T160-480q0 133 93.5 226.5T480-160Z" />
        </svg>
        <p class="text-tertiary me-2 font-semibold">Comment:</p>
        {{ transactions.transactionComment }}
      </div>
      <div class="text-secondary mb-6 flex text-sm">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
             fill="#e8eaed">
          <path
              d="m612-292 56-56-148-148v-184h-80v216l172.16.57.55 172.16.57.55ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-400Zm0 320q133 0 226.5-93.5T800-480q0-133-93.5-226.5T480-800q-133 0-226.5 93.5T160-480q0 133 93.5 226.5T480-160Z" />
        </svg>
        <p class="text-tertiary me-2 font-semibold">Copay:</p>
        {{ transactions.copay }}
      </div>
      <div class="text-secondary mb-6 flex text-sm">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
             fill="#e8eaed">
          <path
              d="m612-292 56-56-148-148v-184h-80v216l172.16.57.55 172.16.57.55ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-400Zm0 320q133 0 226.5-93.5T800-480q0-133-93.5-226.5T480-800q-133 0-226.5 93.5T160-480q0 133 93.5 226.5T480-160Z" />
        </svg>
        <p class="text-tertiary me-2 font-semibold">Covered:</p>
        {{ transactions.covered }}
      </div>
      <div class="text-secondary mb-6 flex text-sm">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
             fill="#e8eaed">
          <path
              d="m612-292 56-56-148-148v-184h-80v216l172.16.57.55 172.16.57.55ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-400Zm0 320q133 0 226.5-93.5T800-480q0-133-93.5-226.5T480-800q-133 0-226.5 93.5T160-480q0 133 93.5 226.5T480-160Z" />
        </svg>
        <p class="text-tertiary me-2 font-semibold">Auth:</p>
        {{ transactions.auth }}
      </div>
      <div class="text-secondary mb-6 flex text-sm">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
             fill="#e8eaed">
          <path
              d="m612-292 56-56-148-148v-184h-80v216l172.16.57.55 172.16.57.55ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-400Zm0 320q133 0 226.5-93.5T800-480q0-133-93.5-226.5T480-800q-133 0-226.5 93.5T160-480q0 133 93.5 226.5T480-160Z" />
        </svg>
        <p class="text-tertiary me-2 font-semibold">Result:</p>
        {{ transactions.result }}
      </div>
      <div class="text-secondary mb-6 flex text-sm">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
             fill="#e8eaed">
          <path
              d="m612-292 56-56-148-148v-184h-80v216l172.16.57.55 172.16.57.55ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-400Zm0 320q133 0 226.5-93.5T800-480q0-133-93.5-226.5T480-800q-133 0-226.5 93.5T160-480q0 133 93.5 226.5T480-160Z" />
        </svg>
        <p class="text-tertiary me-2 font-semibold">Total:</p>
        {{ transactions.total }}
      </div>
    </div>
    <Modal v-for="transactions in transaction">
      <p class="title mb-6">Number: {{ prescription.id }}</p>
      <p class="text-terciary mb-2 font-semibold">People</p>
      <div class="text-md text-primary mb-4 flex">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
          fill="currentColor">
          <path
            d="M480-480q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47ZM160-160v-112q0-34 17.5-62.5T224-378q62-31 126-46.5T480-440q66 0 130 15.5T736-378q29 15 46.5 43.5T800-272v112H160Zm80-80h480v-32q0-11-5.5-20T700-306q-54-27-109-40.5T480-360q-56 0-111 13.5T260-306q-9 5-14.5 14t-5.5 20v32Zm240-320q33 0 56.5-23.5T560-640q0-33-23.5-56.5T480-720q-33 0-56.5 23.5T400-640q0 33 23.5 56.5T480-560Zm0-80Zm0 400Z" />
        </svg>
        <p class="me-2 font-semibold">Usuario:</p>

        {{ prescription.patient }}
      </div>
      <div class="text-secondary text-md mb-6 flex">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
          fill="currentColor">
          <path
            d="m612-292 56-56-148-148v-184h-80v216l172.16.57.55 172.16.57.55ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-400Zm0 320q133 0 226.5-93.5T800-480q0-133-93.5-226.5T480-800q-133 0-226.5 93.5T160-480q0 133 93.5 226.5T480-160Z" />
        </svg>
        <p class="me-2 font-semibold">Date:</p>
        {{ prescription.date }}
      </div>
      <div class="text-terciary text-md mb-3 font-semibold">Institutions</div>
      <div class="text-secondary text-md mb-4 flex">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
          fill="currentColor">
          <path
            d="M420-280h120v-140h140v-120H540v-140H420v140H280v120h140v140ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z" />
        </svg>
        <p class="me-2 font-semibold">Hospital:</p>
        {{ prescription.hospital }}
      </div>
      <div class="text-secondary text-md mb-6 flex">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
          fill="currentColor">
          <path
            d="M420-260h120v-100h100v-120H540v-100H420v100H320v120h100v100ZM280-120q-33 0-56.5-23.5T200-200v-440q0-33 23.5-56.5T280-720h400q33 0 56.5 23.5T760-640v440q0 33-23.5 56.5T680-120H280Zm0-80h400v-440H280v440Zm-40-560v-80h480v80H240Zm40 120v440-440Z" />
        </svg>
        <p class="me-2 font-semibold">Result:</p>
        {{ prescription.result }}
      </div>
      <div class="text-terciary text-md mb-3 font-semibold">Pay</div>
      <div class="text-success text-md mb-4 flex">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
          fill="currentColor">
          <path
            d="M320-280q17 0 28.5-11.5T360-320q0-17-11.5-28.5T320-360q-17 0-28.5 11.5T280-320q0 17 11.5 28.5T320-280Zm0-160q17 0 28.5-11.5T360-480q0-17-11.5-28.5T320-520q-17 0-28.5 11.5T280-480q0 17 11.5 28.5T320-440Zm0-160q17 0 28.5-11.5T360-640q0-17-11.5-28.5T320-680q-17 0-28.5 11.5T280-640q0 17 11.5 28.5T320-600Zm120 320h240v-80H440v80Zm0-160h240v-80H440v80Zm0-160h240v-80H440v80ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z" />
        </svg>
        <p class="me-2 font-semibold">Total to Pay:</p>
        {{ prescription.total }}
      </div>
      <div class="text-error text-md mb-4 flex">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
          fill="currentColor">
          <path
            d="M300-520q-58 0-99-41t-41-99q0-58 41-99t99-41q58 0 99 41t41 99q0 58-41 99t-99 41Zm0-80q25 0 42.5-17.5T360-660q0-25-17.5-42.5T300-720q-25 0-42.5 17.5T240-660q0 25 17.5 42.5T300-600Zm360 440q-58 0-99-41t-41-99q0-58 41-99t99-41q58 0 99 41t41 99q0 58-41 99t-99 41Zm0-80q25 0 42.5-17.5T720-300q0-25-17.5-42.5T660-360q-25 0-42.5 17.5T600-300q0 25 17.5 42.5T660-240Zm-444 80-56-56 584-584 56 56-584 584Z" />
        </svg>
        <p class="me-2 font-semibold">Copay:</p>
        {{ prescription.copay }}
      </div>
      <div class="text-accent text-md mb-4 flex">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
          fill="currentColor">
          <path
            d="M520-120v-80h80v80h-80Zm-80-80v-200h80v200h-80Zm320-120v-160h80v160h-80Zm-80-160v-80h80v80h-80Zm-480 80v-80h80v80h-80Zm-80-80v-80h80v80h-80Zm360-280v-80h80v80h-80ZM180-660h120v-120H180v120Zm-60 60v-240h240v240H120Zm60 420h120v-120H180v120Zm-60 60v-240h240v240H120Zm540-540h120v-120H660v120Zm-60 60v-240h240v240H600Zm80 480v-120h-80v-80h160v120h80v80H680ZM520-400v-80h160v80H520Zm-160 0v-80h-80v-80h240v80h-80v80h-80Zm40-200v-160h80v80h80v80H400Zm-190-90v-60h60v60h-60Zm0 480v-60h60v60h-60Zm480-480v-60h60v60h-60Z" />
        </svg>
        <p class="me-2 font-semibold">Auth:</p>
        {{ prescription.auth_no }}
      </div>
      <Switch class="mb-6" label="Secured" disabled checked></Switch>
      <p class="text-terciary text-md mb-3 font-semibold">Medicines</p>
      <p class="text-terciary text-md mt-6 mb-3 font-semibold">Comment</p>
      <textarea class="text-secondary">{{ prescription.comments }}</textarea>
    </Modal>
    <div v-if="edit" v-for="prescription in prescriptions"
      class="card lg:align-center gap-4 transition duration-300 hover:scale-105 max-sm:mx-2 max-sm:flex-col md:flex-row lg:align-middle">
      <span class="text-primary font-semibold">Number</span>
      <input type="text" class="field mb-8" :defaultValue="prescription.id.toString()" />
      <span class="text-primary font-semibold">Hospital</span>
      <input type="text" class="field mb-8" :defaultValue="prescription.hospital" />
      <span class="text-primary font-semibold">User</span>
      <input type="text" class="field mb-8" :defaultValue="prescription.patient" />
      <span class="text-primary font-semibold">Date</span>
      <input type="text" class="field mb-8" :defaultValue="prescription.date" />
      <button @click="() => (prescription.show = !prescription.show)" class="btn mb-4 flex justify-center align-middle">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
          fill="currentColor">
          <path
            d="M440-280h80v-240h-80v240Zm40-320q17 0 28.5-11.5T520-640q0-17-11.5-28.5T480-680q-17 0-28.5 11.5T440-640q0 17 11.5 28.5T480-600Zm0 520q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z" />
        </svg>
        See More Details
      </button>
      <button class="btn mx-auto flex justify-center">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
          fill="currentColor">
          <path
            d="M840-680v480q0 33-23.5 56.5T760-120H200q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160Zm-80 34L646-760H200v560h560v-446ZM480-240q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35ZM240-560h360v-160H240v160Zm-40-86v446-560 114Z" />
        </svg>Save
      </button>
    </div>
    <button v-if="edit" class="btn mx-auto flex justify-center mb-6" @click="() => {
      prescriptions.push({
        id: 0,
        hospital: '',
        patient: '',
        date: '',
        total: 0,
        copay: 0,
        comments: '',
        secured: false,
        auth_no: '',
        result: '',
        show: false,
        doctor: '',
        pharmacy: '',
      });
    }">
      Add Prescription
    </button>
    <Modal v-if="edit" v-for="prescription in prescriptions" v-model:show="prescription.show">
      <div class="my-8 max-h-[80vh] overflow-y-auto">
        <div class="mt-8">
          <span class="text-primary font-semibold">Number</span>
          <input type="text" class="field mb-8" :defaultValue="prescription.id.toString()" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">User</span>
          <input type="text" class="field mb-8" :defaultValue="prescription.patient" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Doctor</span>
          <input type="text" class="field mb-8" :defaultValue="prescription.doctor" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Hospital</span>
          <input type="text" class="field mb-8" :defaultValue="prescription.hospital" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Pharmacy</span>
          <input type="text" class="field mb-8" :defaultValue="prescription.pharmacy" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Total to Pay</span>
          <input type="text" class="field mb-8" :defaultValue="prescription.total.toString()" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Copay</span>
          <input type="text" class="field mb-8" :defaultValue="prescription.copay.toString()" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Auth</span>
          <input type="text" class="field mb-8" :defaultValue="prescription.auth_no" />
        </div>
        <Switch class="mb-6" label="Secured" checked></Switch>
        <div class="mt-8">
          <span class="text-primary font-semibold">Comment</span>
          <textarea type="text" class="field mb-8" :defaultValue="prescription.comments" />
        </div>
      </div>


      <button class="btn mx-auto mb-4 flex justify-center">
        <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
          fill="currentColor">
          <path
            d="M840-680v480q0 33-23.5 56.5T760-120H200q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160Zm-80 34L646-760H200v560h560v-446ZM480-240q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35ZM240-560h360v-160H240v160Zm-40-86v446-560 114Z" />
        </svg>Save
      </button>
    </Modal>
  </main>
</template>

<style scoped></style>
