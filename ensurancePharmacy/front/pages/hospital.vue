<script setup lang="ts">
import axios from "axios";
import LoadingSpinner from "@/components/LoadingSpinner.vue";

interface Hospital {
  idHospital: number;
  name: string;
  address: string;
  phone: number;
  email: string;
  enabled: number;
}

// Declaramos la variable reactiva usando ref (solo minúscula)
const hospitals = ref<Hospital[]>([]);
let hospitalsChanges: Hospital[] = [];

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
    hospitalsChanges = response.data.map((hospital: Hospital) => ({ ...hospital }));
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

const addHospital = async () => {
  notify({
    type: "loading",
    title: "Adding hospital",
    description: "Please wait...",
  });
  await axios.post(`http://${ip}:8080/api/hospital`, {
    name: 'hgfghjk',
    address: 'hgfghjk',
    phone: 5555,
    email: 'hgfghjk',
    enabled: 1,
  }).then((response) => {
    console.log("Hospital agregado:", response.data);
    notify({
      type: "success",
      title: "Hospital added",
      description: "Hospital added successfully",
    });
  }).catch((error) => {
    console.error("Error al agregar hospital:", error);
    notify({
      type: "error",
      title: "Error adding hospital",
      description: "Error adding hospital",
    });
  });
}
interface Service {
  id_serv: number;
  id_hos: number;
  name: string;
  descrip: string;
  id_cat: number;
  enabled: boolean;
  cost: number;
}

const services: Ref<Service[]> = ref([]);

const fetchService = async () => {
  try {
    notify({
      type: "loading",
      title: "Loading services",
      description: "Please wait...",
    });
    const response = await axios.get(`http://${ip}:8080/api/service`);
    services.value = response.data;
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
}


const edit = useEdit();
const search = useSearch();
const showServices: Ref<boolean> = ref(false);

fetchService();
fetchHospitals();

const isHospitalService = (service: Service) => {
  let hospitalService = false;
  hospitals.value.forEach((hospital) => {
    if (service.id_hos === hospital.idHospital) {
      hospitalService = true;
    }
  });
  return hospitalService;
};
</script>

<template>
  <div class="bg-image-[url('/medicine.jpg')] h-full w-full grid-flow-row items-center justify-center p-4">
    <Search v-if="search" :fieldNames="[
      'Id',
      'Name',
      'Address',
      'Phone Number',
      'E-Mail',
      'Enabled',
    ]" v-model:output="hospitals" :searchFields="[
      'idHospital',
      'name',
      'address',
      'phone',
      'email',
      'enabled',
    ]" />
    <div class="responsive-grid">
      <div v-if="!edit" v-for="(hospital, index) in hospitals" class="card">
        <h2 class="title mb-6">Hospital #{{ hospital.idHospital }}</h2>
        <p class="text-primary mb-4 flex">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M160-80q-33 0-56.5-23.5T80-160v-440q0-33 23.5-56.5T160-680h200v-120q0-33 23.5-56.5T440-880h80q33 0 56.5 23.5T600-800v120h200q33 0 56.5 23.5T880-600v440q0 33-23.5 56.5T800-80H160Zm0-80h640v-440H600q0 33-23.5 56.5T520-520h-80q-33 0-56.5-23.5T360-600H160v440Zm80-80h240v-18q0-17-9.5-31.5T444-312q-20-9-40.5-13.5T360-330q-23 0-43.5 4.5T276-312q-17 8-26.5 22.5T240-258v18Zm320-60h160v-60H560v60Zm-200-60q25 0 42.5-17.5T420-420q0-25-17.5-42.5T360-480q-25 0-42.5 17.5T300-420q0 25 17.5 42.5T360-360Zm200-60h160v-60H560v60ZM440-600h80v-200h-80v200Zm40 220Z" />
          </svg><span class="me-2 font-semibold">Nombre:</span> {{ hospital.name }}
        </p>
        <p class="text-secondary mb-2 flex text-sm">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
            fill="currentColor">
            <path
              d="M480-301q99-80 149.5-154T680-594q0-90-56-148t-144-58q-88 0-144 58t-56 148q0 65 50.5 139T480-301Zm0 101Q339-304 269.5-402T200-594q0-125 78-205.5T480-880q124 0 202 80.5T760-594q0 94-69.5 192T480-200Zm0-320q33 0 56.5-23.5T560-600q0-33-23.5-56.5T480-680q-33 0-56.5 23.5T400-600q0 33 23.5 56.5T480-520ZM200-80v-80h560v80H200Zm280-520Z" />
          </svg><span class="font-semibold">Dirección:</span> {{ hospital.address }}
        </p>
        <p class="text-secondary mb-2 flex text-sm">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
            fill="currentColor">
            <path
              d="M798-120q-125 0-247-54.5T329-329Q229-429 174.5-551T120-798q0-18 12-30t30-12h162q14 0 25 9.5t13 22.5l26 140q2 16-1 27t-11 19l-97 98q20 37 47.5 71.5T387-386q31 31 65 57.5t72 48.5l94-94q9-9 23.5-13.5T670-390l138 28q14 4 23 14.5t9 23.5v162q0 18-12 30t-30 12ZM241-600l66-66-17-94h-89q5 41 14 81t26 79Zm358 358q39 17 79.5 27t81.5 13v-88l-94-19-67 67ZM241-600Zm358 358Z" />
          </svg><span class="font-semibold">Teléfono:</span> {{ hospital.phone }}
        </p>
        <p class="text-secondary mb-4 flex text-sm">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 -960 960 960" width="20px"
            fill="currentColor">
            <path
              d="M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H160Zm320-280L160-640v400h640v-400Zm0-80 320-200H160l320 200ZM160-640v-80 480-400Z" />
          </svg>
          <span class="font-semibold">Email:</span> {{ hospital.email }}
        </p>
        <Switch class="mb-4 flex w-full" v-model="hospital.enabled" label="Enabled"></Switch>
        <button class="btn mb-4 flex" @click="
          () => {
            showServices = true;
          }
        ">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="#e8eaed">
            <path
              d="M440-280h80v-240h-80v240Zm40-320q17 0 28.5-11.5T520-640q0-17-11.5-28.5T480-680q-17 0-28.5 11.5T440-640q0 17 11.5 28.5T480-600Zm0 520q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z" />
          </svg>See Details
        </button>
      </div>
      <div v-if="edit" v-for="(hospital, index) in hospitals" class="card">
        <span class="text-primary font-semibold">Hospital</span>
        <input type="text" class="field mb-8" :defaultValue="hospital.idHospital.toString()" @input="
          (event) => {
            const target = event.target as HTMLInputElement;
            hospitalsChanges[index].idHospital = parseInt(target.value);
          }
        " />
        <span class="text-primary font-semibold">Name</span>
        <input type="text" class="field mb-8" :defaultValue="hospital.name" @input="
          (event) => {
            const target = event.target as HTMLInputElement;
            hospitalsChanges[index].name = target.value;
          }
        " />
        <span class="text-primary font-semibold">Address</span>
        <input type="text" class="field mb-8" :defaultValue="hospital.address" @input="
          (event) => {
            const target = event.target as HTMLInputElement;
            hospitalsChanges[index].address = target.value;
          }
        " />
        <span class="text-primary font-semibold">Phone Number</span>
        <input type="text" class="field mb-8" :defaultValue="hospital.phone" @input="
          (event) => {
            const target = event.target as HTMLInputElement;
            hospitalsChanges[index].phone = parseInt(target.value);
          }
        " />
        <span class="text-primary font-semibold">E-Mail</span>
        <input type="text" class="field mb-8" :defaultValue="hospital.email" @input="
          (event) => {
            const target = event.target as HTMLInputElement;
            hospitalsChanges[index].email = target.value;
          }
        " />
        <div class="mb-8">
          <Switch label="Enabled" />
        </div>
        <button class="btn mx-auto flex" @click="() => {
          addChange(
            ['Hospital', 'Name', 'Address', 'Phone Number', 'E-Mail'],
            hospital,
            hospitalsChanges[index],
            'hospital'
          );
        }">
          <svg class="me-2" xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px"
            fill="currentColor">
            <path
              d="M840-680v480q0 33-23.5 56.5T760-120H200q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160Zm-80 34L646-760H200v560h560v-446ZM480-240q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35ZM240-560h360v-160H240v160Zm-40-86v446-560 114Z" />
          </svg>
          Save
        </button>
      </div>
      <button v-if="edit" class="btn mx-auto flex justify-center mb-6" @click="addHospital()">
        Add Policy
      </button>
    </div>
  </div>
  <div v-if="isLoading" class="flex justify-center my-8">
    <LoadingSpinner size="lg" message="Cargando hospitales..." />
  </div>
  <Modal v-for="service in services" v-model:show="showServices">
    <div v-if="isHospitalService(service)" class="title text-accent mx-auto mb-6 flex w-full justify-center text-lg">
      Number {{ service.id_serv }}
    </div>
    <div class="mb-4 flex">
      <div class="me-2 font-bold">Name:</div>
      {{ service.name }}
    </div>
    <div class="mb-4 flex">
      <div class="me-2 font-bold">Description:</div>
      {{ service.descrip }}
    </div>
    <div class="mb-4 flex">
      <div class="me-2 font-bold">Enabled:</div>
      {{ service.enabled }}
    </div>
    <div class="mb-4 flex">
      <div class="me-2 font-bold">Cost:</div>
      {{ service.cost }}
    </div>
  </Modal>

</template>
