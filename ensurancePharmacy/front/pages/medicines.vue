<script setup lang="ts">
import axios from "axios";
import { useEdit } from "@/composables/useEdit"; // Ajusta la ruta según tu proyecto

interface Pharmacy {
  idPharmacy: number;
  name: string;
  address: string;
  phone: number;
  email: string;
  enabled: number;
}

interface Medicine {
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
}

const medicines = ref<Medicine[]>([]);
const ip = useRuntimeConfig().public.ip;
const edit = useEdit();
const search = useSearch();
let medicineChanges: Medicine[] = [];

const fetchMedicines = async () => {
  try {
    notify({
      type: "loading",
      title: "Loading medicines",
      description: "Please wait...",
    });
    const response = await axios.get(`http://${ip}:8080/api/medicine`);
    medicines.value = response.data;
    medicineChanges = response.data.map((medicine: Medicine) => ({ ...medicine }));
    console.log("Medicines obtenidas:", medicines.value);
    notify({
      type: "success",
      title: "Medicines loaded",
      description: "Medicines loaded successfully",
    });
  } catch (error) {
    console.error("Error al obtener medicines:", error);
    notify({
      type: "error",
      title: "Error loading medicines",
      description: "Error loading medicines",
    });
  }
};

const updateMedicine = async (medicine: Medicine) => {
  try {
    const response = await axios.put(`http://${ip}:8080/api/medicine/${medicine.idMedicine}`, medicine);
    console.log("Medicine actualizada:", response.data);
    notify({
      type: "success",
      title: "Medicine updated",
      description: "Medicine updated successfully",
    });
  } catch (error) {
    console.error("Error al actualizar medicine:", error);
    notify({
      type: "error",
      title: "Error updating medicine",
      description: "Error updating medicine",
    });
  }
};

fetchMedicines();
</script>

<template>
  <div class="container bg-color p-4">
    <!-- Page header -->
    <Search v-if="search" :fieldNames="['Nombre', 'Descripción', 'Categoría', 'Proveedor']"
      :searchFields="['name', 'description', 'category', 'supplier']" v-model:output="medicines" />
    <!-- Medicines grid -->
    <div >
      <!-- Grid of medicines -->
      <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        <!-- Vista normal -->
        <div v-if="!edit" v-for="medicine in medicines" :key="medicine.idMedicine"
          class="card p-6 shadow-md transition-all hover:shadow-lg">
          <div class="flex justify-between items-start mb-4">
            <h2 class="text-xl font-bold text-primary">{{ medicine.name }}</h2>
            <span class="bg-success-light text-success text-xs font-medium px-2.5 py-0.5 rounded-full">
              {{ medicine.stock }} unidades
            </span>
          </div>

          <div class="space-y-2 text-sm">
            <p class="flex justify-between">
              <span class="font-medium text-secondary">Code:</span>
              <span class="text-primary">{{ medicine.idMedicine }}</span>
            </p>
            <p class="flex justify-between">
              <span class="font-medium text-secondary">Active Principle:</span>
              <span class="text-primary">{{ medicine.activePrinciple }}</span>
            </p>
            <p class="flex justify-between">
              <span class="font-medium text-secondary">Presentation:</span>
              <span class="text-primary">{{ medicine.presentation }}</span>
            </p>
            <p class="flex justify-between">
              <span class="font-medium text-secondary">Brand:</span>
              <span class="text-primary">{{ medicine.brand }}</span>
            </p>
            <p class="flex justify-between">
              <span class="font-medium text-secondary">Price:</span>
              <span class="font-bold text-primary">${{ medicine.price }}</span>
            </p>
          </div>

          <div class="mt-4 pt-4 border-t border-secondary">
            <p class="text-sm text-secondary mb-2">{{ medicine.description }}</p>
            <div class="flex items-center mt-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-secondary mr-1" fill="none"
                viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
              <span class="text-xs text-secondary">{{ medicine.pharmacy.name }}</span>
            </div>
          </div>

        </div>

        <!-- Vista edición (ejemplo, se puede personalizar) -->
        <div v-if="edit" v-for="(medicine, index) in medicines" :key="medicine.idMedicine"
          class="card p-6 shadow-md hover:shadow-lg transition-all">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-secondary">Name:</label>
              <input type="text" :defaultValue="medicine.name"
                class="mt-1 block w-full rounded-md border text-primary border-primary px-3 py-2 shadow-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                @input="
                  (event) => {
                    const target = event.target as HTMLInputElement;
                    medicineChanges[index].name = target.value;
                  }
                " />
            </div>

            <div>
              <label class="block text-sm font-medium text-secondary">Code:</label>
              <input type="text" :defaultValue="medicine.idMedicine.toString()" @input="
                (event) => {
                  const target = event.target as HTMLInputElement;
                  medicineChanges[index].idMedicine = parseInt(target.value);
                }
              " class="mt-1 block w-full rounded-md border text-primary border-primary px-3 py-2 shadow-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary" />
            </div>

            <div>
              <label class="block text-sm font-medium text-secondary">Active Principle:</label>
              <input type="text" :defaultValue="medicine.activePrinciple" @input="
                (event) => {
                  const target = event.target as HTMLInputElement;
                  medicineChanges[index].activePrinciple = target.value;
                }
              " class="mt-1 block w-full rounded-md border text-primary border-primary px-3 py-2 shadow-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary" />
            </div>

            <div>
              <label class="block text-sm font-medium text-secondary">Presentation:</label>
              <input type="text" :defaultValue="medicine.presentation"
                class="mt-1 block w-full text-primary rounded-md border border-primary px-3 py-2 shadow-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                @input="
                  (event) => {
                    const target = event.target as HTMLInputElement;
                    medicineChanges[index].presentation = target.value;
                  }
                " />
            </div>

            <div>
              <label class="block text-sm font-medium text-secondary">Units:</label>
              <input type="number" :defaultValue="medicine.stock.toString()"
                class="mt-1 block w-full text-primary rounded-md border border-primary px-3 py-2 shadow-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                @input="
                  (event) => {
                    const target = event.target as HTMLInputElement;
                    medicineChanges[index].stock = parseInt(target.value);
                  }
                " />
            </div>

            <div>
              <Switch class="mt-2" label="Recipe Required" />
            </div>

            <div>
              <label class="block text-sm font-medium text-secondary">Brand:</label>
              <input type="text" :defaultValue="medicine.brand" @input="
                (event) => {
                  const target = event.target as HTMLInputElement;
                  medicineChanges[index].brand = target.value;
                }
              " class="mt-1 block w-full rounded-md border text-primary border-primary px-3 py-2 shadow-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary" />
            </div>

            <Dropdown class="mt-2" />
            <button class="btn mx-auto flex" @click="
              () => {
                addChange(
                  ['Medicine', 'Name', 'Code', 'Active Principle', 'Presentation', 'Units', 'Brand'],
                  medicine,
                  medicineChanges[index],
                  'medicine'
                );
              }
            ">
              <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
                fill="currentColor">
                <path
                  d="M840-680v480q0 33-23.5 56.5T760-120H200q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160Zm-80 34L646-760H200v560h560v-446ZM480-240q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35ZM240-560h360v-160H240v160Zm-40-86v446-560 114Z" />
              </svg>

              Save
            </button>
          </div>
        </div>


        <button v-if="edit" class="btn mx-auto flex justify-center mb-6" @click="() => {
          medicines.push({
            idMedicine: 0,
            name: '',
            description: '',
            price: 0,
              pharmacy: { idPharmacy: 0, name: '', address: '', phone: 0, email: '', enabled: 1 },
            enabled: 1,
            activePrinciple: '',
            presentation: '',
            stock: 0,
            brand: '',
          });
        }"
        >
          Add Medicine
        </button>
      </div>
    </div>
  </div>
</template>
