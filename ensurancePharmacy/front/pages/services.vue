<script setup lang="ts">
import axios from "axios";

interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: number;
  email: string;
  enabled: number;
}

interface Category {
  idCategory: number;
  name: string;
  enabled: boolean;
}

interface Service {
  idService: number;
  hospital: Hospital;
  name: string;
  description: string;
  category: Category;
  subcategory: Category;
  cost: number;
  enabled: number;
}

interface SelectedCategory {
  id: number;
  name: string;
}

const edit = useEdit();
const search = useSearch();
const getCategory: Ref<SelectedCategory | null> = getCurrentCategory();
const services = ref<Service[]>([]);
let serviceChanges: Service[] = [];
const config = useRuntimeConfig();
const ip = config.public.ip;
let temp: Service[] = [];
import { getInsuranceApiUrl } from "../../utils/api";
const fetchService = async () => {
  try {
    notify({
      type: "loading",
      title: "Loading services",
      description: "Please wait...",
    });
    const response = await axios.get(getInsuranceApiUrl("/service"));
    console.log(response.data);
    services.value = response.data;
    serviceChanges = response.data.map((service: Service) => ({ ...service }));
    temp = response.data.map((service: Service) => ({ ...service }));
    console.log("Hospitals obtenidos:", services.value);
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
fetchService();

watch(getCategory, () => {
  if (getCategory.value === null) {
    services.value = temp;
    return;
  }
  services.value = temp;
  console.log(services.value);
  services.value = services.value.filter(
    (service) => (service.category.name.toLowerCase() === getCategory.value?.name.toLowerCase()
      || service.subcategory.name.toLowerCase() === getCategory.value?.name.toLowerCase()
    ));
  console.log(services.value);
});
</script>

<template>
  <div
    class=" bg-image-[url(https://images.unsplash.com/photo-1542744173-8e7e53415bb0?q=80&w=3270&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D)] py-8">
    <div class="responsive-grid">
      <Search v-if="search" :fieldNames="['Name', 'Hospital', 'Category', 'SubCategory', 'Cost', 'Description']"
        :searchFields="['name', 'hospital', 'category', 'subcategory', 'cost', 'description']"
        v-model:output="services" />
      <div v-if="!edit" v-for="service in services" class="card">
        <h2 class="title mb-6">{{ service.name }}</h2>
        <div class="text-primary mb-4 flex">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M420-280h120v-140h140v-120H540v-140H420v140H280v120h140v140ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z" />
          </svg>
          <p class="me-2 font-semibold">Hospital:</p>
          {{ service.hospital.name }}
        </div>
        <div class="text-error mb-4 flex">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M560-440q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35ZM280-320q-33 0-56.5-23.5T200-400v-320q0-33 23.5-56.5T280-800h560q33 0 56.5 23.5T920-720v320q0 33-23.5 56.5T840-320H280Zm80-80h400q0-33 23.5-56.5T840-480v-160q-33 0-56.5-23.5T760-720H360q0 33-23.5 56.5T280-640v160q33 0 56.5 23.5T360-400Zm440 240H120q-33 0-56.5-23.5T40-240v-440h80v440h680v80ZM280-400v-320 320Z" />
          </svg>
          <p class="me-2 font-semibold">Price:</p>
          {{ service.cost }}
        </div>
        <div class="grid grid-flow-row grid-cols-2">
          <p class="text-primary mb-2 font-semibold">Categories</p>
          <div class="text-background bg-accent mx-2 mb-4 rounded-md px-2 py-1 text-sm">
            {{ service.category.name }}
          </div>
        </div>
        <div class="grid grid-flow-row grid-cols-2">
          <p class="text-primary mb-2 font-semibold">SubCategories</p>
          <div class="text-background bg-accent mx-2 mb-4 rounded-md px-2 py-1 text-sm">
            {{ service.subcategory.name }}
          </div>
        </div>
        <p class="text-primary mb-2 font-semibold">Description</p>
        <textarea class="text-secondary flex w-full" disabled>{{
          service.description
        }}</textarea>
      </div>

      <div v-if="edit" v-for="(service, index) in services" class="card">
        <span class="text-primary font-semibold">{{ service.name }}</span>
        <input type="text" class="field mb-8" :defaultValue="service.name" 
        @input="
            (event) => {
              const target = event.target as HTMLInputElement;
              serviceChanges[index].name = target.value;
            }
          "
        />
        <span class="text-primary font-semibold">{{ service.hospital.name }}</span>
        <input type="text" class="field mb-8" :defaultValue="service.hospital.name" 
        @input="
            (event) => {
              const target = event.target as HTMLInputElement;
              serviceChanges[index].hospital.name = target.value;
            }
          "
        />
        <span class="text-primary font-semibold">Price</span>
        <input type="text" class="field mb-8" :defaultValue="service.cost.toString()" 
        @input="
            (event) => {
              const target = event.target as HTMLInputElement;
              serviceChanges[index].cost = parseInt(target.value);
            }
          "
        />
        <Dropdown class="me-2 mb-6" />
        <span class="text-primary font-semibold">{{ service.description }}</span>
        <textarea type="text" class="field mb-8" :defaultValue="service.description" 
        @input="
            (event) => {
              const target = event.target as HTMLInputElement;
              serviceChanges[index].description = target.value;
            }
          " />
      </div>
    </div>
    <button v-if="edit" class="btn flex mx-auto justify-center mb-6" @click="() => {
      services.push({
        idService: 0,
        name: '',
        description: '',
        category: { idCategory: 0, name: '', enabled: true },
        subcategory: { idCategory: 0, name: '', enabled: true },
        cost: 0,
        enabled: 1,
        hospital: { idHospital: 0, name: '', address: '', phone: 0, email: '', enabled: 1 },
      });
    }">
      Add Service
    </button>
  </div>

</template>

<style scoped></style>
