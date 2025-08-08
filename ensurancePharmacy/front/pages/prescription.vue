<script setup lang="ts">
import Hospital from "~/pages/hospital.vue";
import Pharmacy from "~/pages/pharmacy.vue";
import axios from "axios";
import { getInsuranceApiUrl } from "../../utils/api";
export interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: number;
  email: string;
  enabled: number;
}

export interface Policy {
  idPolicy: number;
  percentage: number;
  creationDate: string;
  expDate: string;
  cost: number;
  enabled: number;
}

export interface User {
  idUser: number;
  name: string;
  cui: number;
  phone: string;
  email: string;
  address: string;
  birthDate: string;
  role: string;
  policy: Policy;
  enabled: number;
  password: string;
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
const config = useRuntimeConfig();
const ip = config.public.ip;
const fetchTransactions = async () => {
  try {
    notify({
      type: "loading",
      title: "Loading services",
      description: "Please wait...",
    });
    const response = await axios.get(getInsuranceApiUrl("/prescription"));
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

const addPrescription = async () => {
  notify({
    type: "loading",
    title: "Adding prescription",
    description: "Please wait...",
  });
  await axios.post(getInsuranceApiUrl("/prescription"), {
    hospital: {
      idHospital: 1,
      name: "Hospital La Pez",
      address: "10A Calle 2-31 Ciudad de Ciudad de Guatemala, 01014",
      phone: 2217,
      email: "info@gruposermesa.com",
      enabled: 1
    },
    user: {
      idUser: 2,
      name: "Pablo",
      cui: 123121,
      phone: "123121",
      email: "pablo@gmail.com",
      address: "asdas",
      birthDate: "2024-01-12",
      role: "admin",
      policy: {
        idPolicy: 1,
        percentage: 70.0,
        creationDate: "2023-04-01",
        expDate: "2023-12-31",
        cost: 300.5,
        enabled: 1,
      },
      enabled: 1,
      password: "123",
    },
      medicine: {
      idMedicine: 1,
      name: "Paracetamol",
      description: "A medicine used to treat mild to moderate pain",
      price: 120,
      pharmacy: {
        idPharmacy: 1,
        name: "CRUZ VE",
        address: "AVENIDA PRINCIPAL",
        phone: 63517151,
        email: "Cruzverde@gmail.com",
        enabled: 1
      },
      enabled: 1,
      activePrinciple: "Paracetamol",
      presentation: "500MG",
      stock: 25,
      brand: "M",
      coverage: null
    },
    pharmacy: {
      idPharmacy: 1,
      name: "CRUZ VE",
      address: "AVENIDA PRINCIPAL",
      phone: 63517151,
      email: "Cruzverde@gmail.com",
      enabled: 1
    },
    prescriptionDate: "2024-01-12",
    total: 120,
    copay: 12,
    prescriptionComment: "succesful",
    secured: 1,
    auth: "AAAA"

  }).then((response) => {
    console.log("Farmacia agregada:", response.data);
    notify({
      type: "success",
      title: "Prescription added",
      description: "Prescription added successfully",
    });
  }).catch((error) => {
    console.error("Error al agregar farmacia:", error);
    notify({
      type: "error",
      title: "Error adding prescription",
      description: "Error adding prescription",
    });
  });
}
fetchTransactions();

let prescriptionChanges = prescriptions.value.map((prescription: Prescription) => ({ ...prescription }));
const edit = useEdit();
const search = useSearch();
</script>

<template>
  <main
    class="bg-image-[url('https://cdn.prod.website-files.com/6466101d017ab9d60c8d0137/668ce90cd1d21a2f4e8a8536_Repeat%20Prescriptions.jpg')]">
    <Search v-if="search" :fieldNames="['Id', 'Hospital Name', 'Doctor Name', 'Patient Name']"
      v-model:output="prescriptions" :searchFields="['id', 'hospital', 'doctor', 'patient']" />
    <div class="responsive-grid">
      <div v-if="!edit" v-for="prescription in prescriptions"
        class="card lg:align-center gap-4 transition duration-300 hover:scale-105 max-sm:mx-2 max-sm:flex-col md:flex-row lg:align-middle">
        <p class="text-title title mb-6">Number: {{ prescription.idPrescription }}</p>
        <p class="text-primary mb-3 flex text-lg">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M420-280h120v-140h140v-120H540v-140H420v140H280v120h140v140ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z" />
          </svg>
          <strong class="me-2">Hospital:</strong> {{ prescription.hospital.name }}
        </p>
        <div class="text-md text-primary mb-4 flex">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M480-480q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47ZM160-160v-112q0-34 17.5-62.5T224-378q62-31 126-46.5T480-440q66 0 130 15.5T736-378q29 15 46.5 43.5T800-272v112H160Zm80-80h480v-32q0-11-5.5-20T700-306q-54-27-109-40.5T480-360q-56 0-111 13.5T260-306q-9 5-14.5 14t-5.5 20v32Zm240-320q33 0 56.5-23.5T560-640q0-33-23.5-56.5T480-720q-33 0-56.5 23.5T400-640q0 33 23.5 56.5T480-560Zm0-80Zm0 400Z" />
          </svg>
          <p class="me-2 font-semibold">Usuario:</p>

          {{ prescription.user.name }}
        </div>
        <div class="text-secondary mb-6 flex text-sm">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
            fill="currentColor">
            <path
              d="M680-320q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35Zm0-80q17 0 28.5-11.5T720-440q0-17-11.5-28.5T680-480q-17 0-28.5 11.5T640-440q0 17 11.5 28.5T680-400ZM440-40v-116q0-21 10-39.5t28-29.5q32-19 67.5-31.5T618-275l62 75 62-75q37 6 72 18.5t67 31.5q18 11 28.5 29.5T920-156v116H440Zm79-80h123l-54-66q-18 5-35 13t-34 17v36Zm199 0h122v-36q-16-10-33-17.5T772-186l-54 66Zm-76 0Zm76 0Zm-518 0q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v200q-16-20-35-38t-45-24v-138H200v560h166q-3 11-4.5 22t-1.5 22v36H200Zm80-480h280q26-20 57-30t63-10v-40H280v80Zm0 160h200q0-21 4.5-41t12.5-39H280v80Zm0 160h138q11-9 23.5-16t25.5-13v-51H280v80Zm-80 80v-560 137-17 440Zm480-240Z" />
          </svg>
          <p class="text-tertiary me-2 font-semibold">Comment:</p>
          {{ prescription.prescriptionComment}}
        </div>
        <div class="text-secondary mb-6 flex text-sm">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
               fill="currentColor">
            <path
                d="M680-320q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35Zm0-80q17 0 28.5-11.5T720-440q0-17-11.5-28.5T680-480q-17 0-28.5 11.5T640-440q0 17 11.5 28.5T680-400ZM440-40v-116q0-21 10-39.5t28-29.5q32-19 67.5-31.5T618-275l62 75 62-75q37 6 72 18.5t67 31.5q18 11 28.5 29.5T920-156v116H440Zm79-80h123l-54-66q-18 5-35 13t-34 17v36Zm199 0h122v-36q-16-10-33-17.5T772-186l-54 66Zm-76 0Zm76 0Zm-518 0q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v200q-16-20-35-38t-45-24v-138H200v560h166q-3 11-4.5 22t-1.5 22v36H200Zm80-480h280q26-20 57-30t63-10v-40H280v80Zm0 160h200q0-21 4.5-41t12.5-39H280v80Zm0 160h138q11-9 23.5-16t25.5-13v-51H280v80Zm-80 80v-560 137-17 440Zm480-240Z" />
          </svg>
          <p class="text-tertiary me-2 font-semibold">Date:</p>
          {{ prescription.prescriptionDate}}
        </div>
        <div class="text-secondary mb-6 flex text-sm">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
               fill="currentColor">
            <path
                d="M680-320q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35Zm0-80q17 0 28.5-11.5T720-440q0-17-11.5-28.5T680-480q-17 0-28.5 11.5T640-440q0 17 11.5 28.5T680-400ZM440-40v-116q0-21 10-39.5t28-29.5q32-19 67.5-31.5T618-275l62 75 62-75q37 6 72 18.5t67 31.5q18 11 28.5 29.5T920-156v116H440Zm79-80h123l-54-66q-18 5-35 13t-34 17v36Zm199 0h122v-36q-16-10-33-17.5T772-186l-54 66Zm-76 0Zm76 0Zm-518 0q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v200q-16-20-35-38t-45-24v-138H200v560h166q-3 11-4.5 22t-1.5 22v36H200Zm80-480h280q26-20 57-30t63-10v-40H280v80Zm0 160h200q0-21 4.5-41t12.5-39H280v80Zm0 160h138q11-9 23.5-16t25.5-13v-51H280v80Zm-80 80v-560 137-17 440Zm480-240Z" />
          </svg>
          <p class="text-tertiary me-2 font-semibold">Copay:</p>
          {{ prescription.copay}}
        </div>
        <div class="text-secondary mb-6 flex text-sm">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
               fill="currentColor">
            <path
                d="M680-320q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35Zm0-80q17 0 28.5-11.5T720-440q0-17-11.5-28.5T680-480q-17 0-28.5 11.5T640-440q0 17 11.5 28.5T680-400ZM440-40v-116q0-21 10-39.5t28-29.5q32-19 67.5-31.5T618-275l62 75 62-75q37 6 72 18.5t67 31.5q18 11 28.5 29.5T920-156v116H440Zm79-80h123l-54-66q-18 5-35 13t-34 17v36Zm199 0h122v-36q-16-10-33-17.5T772-186l-54 66Zm-76 0Zm76 0Zm-518 0q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v200q-16-20-35-38t-45-24v-138H200v560h166q-3 11-4.5 22t-1.5 22v36H200Zm80-480h280q26-20 57-30t63-10v-40H280v80Zm0 160h200q0-21 4.5-41t12.5-39H280v80Zm0 160h138q11-9 23.5-16t25.5-13v-51H280v80Zm-80 80v-560 137-17 440Zm480-240Z" />
          </svg>
          <p class="text-tertiary me-2 font-semibold">Total:</p>
          {{ prescription.total}}
        </div>
        <button @click="() => (prescription.show = !prescription.show)" class="btn flex justify-center align-middle">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M440-280h80v-240h-80v240Zm40-320q17 0 28.5-11.5T520-640q0-17-11.5-28.5T480-680q-17 0-28.5 11.5T440-640q0 17 11.5 28.5T480-600Zm0 520q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z" />
          </svg>
          See More Details
        </button>
      </div>
      <Modal v-if="!edit" v-for="prescription in prescriptions" v-model:show="prescription.show">
        <p class="title mb-6">Number: {{ prescription.idPrescription }}</p>
        <p class="text-terciary mb-2 font-semibold">People</p>
        <div class="text-md text-primary mb-4 flex">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M480-480q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47ZM160-160v-112q0-34 17.5-62.5T224-378q62-31 126-46.5T480-440q66 0 130 15.5T736-378q29 15 46.5 43.5T800-272v112H160Zm80-80h480v-32q0-11-5.5-20T700-306q-54-27-109-40.5T480-360q-56 0-111 13.5T260-306q-9 5-14.5 14t-5.5 20v32Zm240-320q33 0 56.5-23.5T560-640q0-33-23.5-56.5T480-720q-33 0-56.5 23.5T400-640q0 33 23.5 56.5T480-560Zm0-80Zm0 400Z" />
          </svg>
          <p class="me-2 font-semibold">Usuario:</p>

          {{ prescription.user.name }}
        </div>
        <div class="text-terciary text-md mb-3 font-semibold">Institutions</div>
        <div class="text-secondary text-md mb-4 flex">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M420-280h120v-140h140v-120H540v-140H420v140H280v120h140v140ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z" />
          </svg>
          <p class="me-2 font-semibold">Hospital:</p>
          {{ prescription.hospital.name }}
        </div>
        <div class="text-secondary text-md mb-6 flex">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M420-260h120v-100h100v-120H540v-100H420v100H320v120h100v100ZM280-120q-33 0-56.5-23.5T200-200v-440q0-33 23.5-56.5T280-720h400q33 0 56.5 23.5T760-640v440q0 33-23.5 56.5T680-120H280Zm0-80h400v-440H280v440Zm-40-560v-80h480v80H240Zm40 120v440-440Z" />
          </svg>
          <p class="me-2 font-semibold">Pharmacy:</p>
          {{ prescription.pharmacy.name }}
        </div>
        <div class="text-terciary text-md mb-3 font-semibold">Pay</div>
        <div class="text-success text-md mb-4 flex">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M560-440q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35ZM280-320q-33 0-56.5-23.5T200-400v-320q0-33 23.5-56.5T280-800h560q33 0 56.5 23.5T920-720v320q0 33-23.5 56.5T840-320H280Zm80-80h400q0-33 23.5-56.5T840-480v-160q-33 0-56.5-23.5T760-720H360q0 33-23.5 56.5T280-640v160q33 0 56.5 23.5T360-400Zm440 240H120q-33 0-56.5-23.5T40-240v-440h80v440h680v80ZM280-400v-320 320Z" />
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
          {{ prescription.auth }}
        </div>
        <Switch class="mb-6" label="Secured" disabled checked></Switch>

        <p class="text-terciary text-md mt-6 mb-3 font-semibold">Comment</p>
        <textarea class="text-secondary">{{ prescription.prescriptionComment }}</textarea>
      </Modal>
      <div v-if="edit" v-for="(prescription, index) in prescriptions"
        class="card lg:align-center gap-4 transition duration-300 hover:scale-105 max-sm:mx-2 max-sm:flex-col md:flex-row lg:align-middle">
        <span class="text-primary font-semibold">Number</span>
        <input type="text" class="field mb-8" :defaultValue="prescription.id.toString()" @input="
          (event) => {
            const target = event.target as HTMLInputElement;
            prescriptionChanges[index].id = parseInt(target.value);
          }
        " />
        <span class="text-primary font-semibold">Hospital</span>
        <input type="text" class="field mb-8" :defaultValue="prescription.hospital" @input="
          (event) => {
            const target = event.target as HTMLInputElement;
            prescriptionChanges[index].hospital = target.value;
          }
        " />
        <span class="text-primary font-semibold">User</span>
        <input type="text" class="field mb-8" :defaultValue="prescription.patient" @input="
          (event) => {
            const target = event.target as HTMLInputElement;
            prescriptionChanges[index].patient = target.value;
          }
        " />
        <span class="text-primary font-semibold">Date</span>
        <input type="text" class="field mb-8" :defaultValue="prescription.date" @input="
          (event) => {
            const target = event.target as HTMLInputElement;
            prescriptionChanges[index].date = target.value;
          }
        " />
        <button @click="() => (prescription.show = !prescription.show)"
          class="btn mb-4 flex justify-center align-middle">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M440-280h80v-240h-80v240Zm40-320q17 0 28.5-11.5T520-640q0-17-11.5-28.5T480-680q-17 0-28.5 11.5T440-640q0 17 11.5 28.5T480-600Zm0 520q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z" />
          </svg>
          See More Details
        </button>
        <button v-if="edit" class="btn mx-auto flex justify-center" @click="
          () => {
            addChange(
              ['Prescription', 'Number', 'Hospital', 'User', 'Date', 'Comment'],
              prescription,
              prescriptionChanges[index],
              'prescription'
            );
          }
        ">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M840-680v480q0 33-23.5 56.5T760-120H200q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160Zm-80 34L646-760H200v560h560v-446ZM480-240q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35ZM240-560h360v-160H240v160Zm-40-86v446-560 114Z" />
          </svg>Save
        </button>
      </div>
      <button class="btn mx-auto flex justify-center mb-6" @click="addPrescription()">
        Add Prescription
      </button>
    </div>
    <Modal v-if="edit" v-for="prescription in prescriptions" v-model:show="prescription.show">
      <div class="my-8 max-h-[80vh] overflow-y-auto">
        <div class="mt-8">
          <span class="text-primary font-semibold">Number</span>
          <input type="text" class="field mb-8" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">User</span>
          <input type="text" class="field mb-8" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Doctor</span>
          <input type="text" class="field mb-8" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Hospital</span>
          <input type="text" class="field mb-8" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Pharmacy</span>
          <input type="text" class="field mb-8" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Total to Pay</span>
          <input type="text" class="field mb-8" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Copay</span>
          <input type="text" class="field mb-8" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Auth</span>
          <input type="text" class="field mb-8" />
        </div>
        <Switch class="mb-6" label="Secured" checked></Switch>
        <div class="mt-8" v-for="medicine in prescription.medicines">
          <span class="text-primary font-semibold">Medicine</span>
          <input type="text" class="field mb-8" />
        </div>
        <div class="mt-8">
          <span class="text-primary font-semibold">Comment</span>
          <textarea type="text" class="field mb-8" />
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
