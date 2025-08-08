<script setup lang="ts">
import {setProfile, useProfile, initializeProfile} from "~/composables/useProfile";
import axios from "axios";
import {onMounted} from "vue";
import { toRaw } from 'vue';
import { getInsuranceApiUrl } from "../../utils/api";   
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

const profile = useProfile();
console.log(JSON.stringify(profile.value))
const user = ref<User | null>(null);
const isLoading = ref(false);
const hasError = ref(false);
const config = useRuntimeConfig();
const ip = config.public.ip;

const fetchUser = async () => {
  try {
    isLoading.value = true;
    hasError.value = false;

    const storedUser = profile.value;
    const id = storedUser?.idUser;

    if (!id) {
      throw new Error("No se encontró idUser en localStorage");
    }

    const response = await axios.get(getInsuranceApiUrl(`/users/${id}`));
    user.value = response.data;
    setProfile(user.value!);
  } catch (error) {
    console.error("Error al cargar el perfil:", error);
    hasError.value = true;
  } finally {
    isLoading.value = false;
  }
};
onMounted(() => {
  initializeProfile();
  fetchUser();
  fetchAppointment();
});
const prescriptions: Ref<
  {
    id: number;
    hospital: string;
    doctor: string;
    patient: string;
    pharmacy: string;
    date: string;
    total: number;
    copay: number;
    comments: string;
    medicines: string[];
    date_created: string;
    secured: boolean;
    minimun: number;
    auth_no: string;
    show: boolean;
  }[]
> = ref([
  {
    id: 1,
    hospital: "City Hospital",
    doctor: "Dr. Smith",
    patient: "John Doe",
    pharmacy: "Pharmacy One",
    date: "2023-04-01",
    total: 150.0,
    copay: 15.0,
    comments: "Take twice daily",
    medicines: ["Aspirin", "Ibuprofen"],
    date_created: "2023-04-01T09:00:00Z",
    secured: true,
    minimun: 1,
    auth_no: "123456789",
    show: false,
  },
  {
    id: 2,
    hospital: "General Hospital",
    doctor: "Dr. Brown",
    patient: "John Doe",
    pharmacy: "Pharmacy Two",
    date: "2023-04-15",
    total: 200.0,
    copay: 20.0,
    comments: "Take with food",
    medicines: ["Paracetamol"],
    date_created: "2023-04-15T10:00:00Z",
    secured: false,
    minimun: 1,
    auth_no: "987654321",
    show: false,
  },
  {
    id: 3,
    hospital: "Mercy Hospital",
    doctor: "Dr. Lee",
    patient: "John Doe",
    pharmacy: "Pharmacy Three",
    date: "2023-05-03",
    total: 175.0,
    copay: 17.5,
    comments: "Apply cream to affected area",
    medicines: ["Hydrocortisone"],
    date_created: "2023-05-03T11:00:00Z",
    secured: true,
    minimun: 1,
    auth_no: "456789123",
    show: false,
  }
]);

interface Policy {
  idPolicy: number;
  percentage: number;
  creationDate: number; // Representado en milisegundos
  expDate: number;      // Representado en milisegundos
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
  birthDate: number;    // Representado en milisegundos
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

interface Appointment {
  idAppointment: number;
  hospital: Hospital;
  user: User;
  appointmentDate: number;  // Representado en milisegundos
  enabled: number;
}
const appointments = ref<Appointment[]>([]);

const fetchAppointment = async () => {
  try {
    isLoading.value = true;
    hasError.value = false;

    const storedUser = profile.value;
    const id = storedUser?.idUser;

    if (!id) {
      throw new Error("No se encontró idUser en localStorage");
    }

    const response = await axios.get(getInsuranceApiUrl(`/appointment?user_id=${id}`));

    appointments.value = response.data;
    console.log(id);
    console.log(toRaw(appointments.value));
  } catch (error) {
    console.error("Error al cargar el perfil:", error);
    hasError.value = true;
  } finally {
    isLoading.value = false;
  }
};

const edit = useEdit();
console.log()
</script>

<template>
  <div
    class="from-s-background to-sh-background h-full w-full bg-gradient-to-br py-8"
  >
    <div class="align-middle md:flex">
      <div class="h-full align-middle md:my-auto md:ms-8 md:me-6 md:flex-col">
        <img
          src="https://images.pexels.com/photos/1704488/pexels-photo-1704488.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500"
          alt="Profile Picture"
          class="mx-auto mb-6 flex h-30 w-30 items-center justify-center rounded-full object-cover"
        />
        <h1 v-if="!edit" class="title mb-6">Hi, {{ user?.name }}</h1>
      </div>
      <div v-if="!edit" class="card mb-6">
        <h2 class="title mb-6 text-lg">General Info</h2>
        <div class="text-primary mb-4 flex">
          <svg
            class="me-2"
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill="currentColor"
          >
            <path
              d="M160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H160Zm320-280L160-640v400h640v-400L480-440Zm0-80 320-200H160l320 200ZM160-640v-80 480-400Z"
            />
          </svg>
          <p class="me-2 font-semibold">E-Mail:</p>
          {{ user?.email }}
        </div>
        <div class="text-primary mb-6 flex">
          <svg
            class="me-2"
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill="currentColor"
          >
            <path
              d="M798-120q-125 0-247-54.5T329-329Q229-429 174.5-551T120-798q0-18 12-30t30-12h162q14 0 25 9.5t13 22.5l26 140q2 16-1 27t-11 19l-97 98q20 37 47.5 71.5T387-386q31 31 65 57.5t72 48.5l94-94q9-9 23.5-13.5T670-390l138 28q14 4 23 14.5t9 23.5v162q0 18-12 30t-30 12ZM241-600l66-66-17-94h-89q5 41 14 81t26 79Zm358 358q39 17 79.5 27t81.5 13v-88l-94-19-67 67ZM241-600Zm358 358Z"
            />
          </svg>
          <p class="me-2 font-semibold">Phone:</p>
          {{ user?.phone }}
        </div>
        <div class="text-secondary mb-4 flex text-sm">
          <svg
            class="me-2"
            xmlns="http://www.w3.org/2000/svg"
            height="20px"
            viewBox="0 -960 960 960"
            width="20px"
            fill="currentColor"
          >
            <path
              d="M240-200h120v-240h240v240h120v-360L480-740 240-560v360Zm-80 80v-480l320-240 320 240v480H520v-240h-80v240H160Zm320-350Z"
            />
          </svg>
          <p class="me-2 font-semibold">Address:</p>
          {{ user?.address }}
        </div>
        <div class="text-secondary mb-4 flex text-sm">
          <svg
            class="me-2"
            xmlns="http://www.w3.org/2000/svg"
            height="20px"
            viewBox="0 -960 960 960"
            width="20px"
            fill="currentColor"
          >
            <path
              d="M160-80q-17 0-28.5-11.5T120-120v-200q0-33 23.5-56.5T200-400v-160q0-33 23.5-56.5T280-640h160v-58q-18-12-29-29t-11-41q0-15 6-29.5t18-26.5l56-56 56 56q12 12 18 26.5t6 29.5q0 24-11 41t-29 29v58h160q33 0 56.5 23.5T760-560v160q33 0 56.5 23.5T840-320v200q0 17-11.5 28.5T800-80H160Zm120-320h400v-160H280v160Zm-80 240h560v-160H200v160Zm80-240h400-400Zm-80 240h560-560Zm560-240H200h560Z"
            />
          </svg>
          <p class="me-2 font-semibold">Birthday:</p>
          {{ user?.birthDate }}
        </div>
        <div class="text-secondary mb-6 flex text-sm">
          <svg
            class="me-2"
            xmlns="http://www.w3.org/2000/svg"
            height="20px"
            viewBox="0 -960 960 960"
            width="20px"
            fill="currentColor"
          >
            <path
              d="M160-80q-33 0-56.5-23.5T80-160v-440q0-33 23.5-56.5T160-680h200v-120q0-33 23.5-56.5T440-880h80q33 0 56.5 23.5T600-800v120h200q33 0 56.5 23.5T880-600v440q0 33-23.5 56.5T800-80H160Zm0-80h640v-440H600q0 33-23.5 56.5T520-520h-80q-33 0-56.5-23.5T360-600H160v440Zm80-80h240v-18q0-17-9.5-31.5T444-312q-20-9-40.5-13.5T360-330q-23 0-43.5 4.5T276-312q-17 8-26.5 22.5T240-258v18Zm320-60h160v-60H560v60Zm-200-60q25 0 42.5-17.5T420-420q0-25-17.5-42.5T360-480q-25 0-42.5 17.5T300-420q0 25 17.5 42.5T360-360Zm200-60h160v-60H560v60ZM440-600h80v-200h-80v200Zm40 220Z"
            />
          </svg>
          <p class="me-2 font-semibold">CUI:</p>
          {{ user?.cui }}
        </div>
        <div class="text-success mb-6 flex">
          <svg
            class="me-2"
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill="currentColor"
          >
            <path
              d="M280-280h280v-80H280v80Zm0-160h400v-160H280v160Zm0-160h400v-80H280v80Zm-80 480q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160Zm-80 34L646-760H200v560h560v-446ZM480-240q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35ZM240-560h360v-160H240v160Zm-40-86v446-560 114Z"
            />
          </svg>

          <p class="me-2 font-semibold">Policy:</p>
          {{ user?.policy.percentage }}
        </div>
      </div>
      <div></div>
    </div>
    <div v-if="!edit" class="card">
      <div class="title mb-6">Appointments</div>
      <div class="responsive-grid">
        <div v-for="appointment in appointments">
          <div
            class="bg-s-background hover:bg-h-secondary mb-6 rounded-lg px-4 py-2"
          >
            <div class="text-primary mb-6 flex">
              <svg
                class="text-accent me-2"
                xmlns="http://www.w3.org/2000/svg"
                height="24px"
                viewBox="0 -960 960 960"
                width="24px"
                fill="currentColor"
              >
                <path
                  d="m240-160 40-160H120l20-80h160l40-160H180l20-80h160l40-160h80l-40 160h160l40-160h80l-40 160h160l-20 80H660l-40 160h160l-20 80H600l-40 160h-80l40-160H360l-40 160h-80v160H160v-160Zm140-240h160l40-160H420l-40 160Z"
                />
              </svg>
              <div class="title me-2 text-lg font-semibold">
                Number: {{ appointment.idAppointment }}
              </div>
            </div>
            <div class="text-primary mb-4 flex">
              <svg
                class="me-2"
                xmlns="http://www.w3.org/2000/svg"
                height="22px"
                viewBox="0 -960 960 960"
                width="22px"
                fill="currentColor"
              >
                <path
                  d="M580-240q-42 0-71-29t-29-71q0-42 29-71t71-29q42 0 71 29t29 71q0 42-29 71t-71 29ZM200-80q-33 0-56.5-23.5T120-160v-560q0-33 23.5-56.5T200-800h40v-80h80v80h320v-80h80v80h40q33 0 56.5 23.5T840-720v560q0 33-23.5 56.5T760-80H200Zm0-80h560v-400H200v400Zm0-480h560v-80H200v80Zm0 0v-80 80Z"
                />
              </svg>
              <div class="me-2 font-semibold">Date:</div>
              {{ appointment.appointmentDate }}
            </div>
            <div class="text-secondary mb-4 flex text-sm">
              <svg
                class="me-2"
                xmlns="http://www.w3.org/2000/svg"
                height="20px"
                viewBox="0 -960 960 960"
                width="20px"
                fill="currentColor"
              >
                <path
                  d="M694-120 552-262l57-56 85 85 170-170 56 57-226 226ZM540-80q-108 0-184-76t-76-184v-23q-86-14-143-80.5T80-600v-240h120v-40h80v160h-80v-40h-40v160q0 66 47 113t113 47q66 0 113-47t47-113v-160h-40v40h-80v-160h80v40h120v240q0 90-57 156.5T360-363v23q0 75 52.5 127.5T540-160v80Z"
                />
              </svg>
              <div class="me-2 font-semibold">Pacient:</div>
              {{ appointment.user.name }}
            </div>
            <div class="text-secondary flex text-sm">
              <svg
                class="me-2"
                xmlns="http://www.w3.org/2000/svg"
                height="20px"
                viewBox="0 -960 960 960"
                width="20px"
                fill="currentColor"
              >
                <path
                  d="M420-280h120v-140h140v-120H540v-140H420v140H280v120h140v140ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z"
                />
              </svg>
              <div class="me-2 font-semibold">hospital:</div>
              {{ appointment.hospital.name }}
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!edit" class="card">
      <div class="title mb-6">Prescriptions</div>
      <div class="responsive-grid">
        <div v-for="prescription in prescriptions">
          <div
            class="bg-s-background hover:bg-h-secondary mb-6 rounded-lg px-4 py-2"
          >
            <div class="text-primary mb-6 flex">
              <svg
                class="text-accent me-2"
                xmlns="http://www.w3.org/2000/svg"
                height="24px"
                viewBox="0 -960 960 960"
                width="24px"
                fill="currentColor"
              >
                <path
                  d="m240-160 40-160H120l20-80h160l40-160H180l20-80h160l40-160h80l-40 160h160l40-160h80l-40 160h160l-20 80H660l-40 160h160l-20 80H600l-40 160h-80l40-160H360l-40 160h-80v160H160v-160Zm140-240h160l40-160H420l-40 160Z"
                />
              </svg>
              <div class="title me-2 text-lg font-semibold">
                ID: {{ prescription.id }}
              </div>
            </div>
            <div class="text-primary mb-4 flex">
              <svg
                class="me-2"
                xmlns="http://www.w3.org/2000/svg"
                height="22px"
                viewBox="0 -960 960 960"
                width="22px"
                fill="currentColor"
              >
                <path
                  d="M580-240q-42 0-71-29t-29-71q0-42 29-71t71-29q42 0 71 29t29 71q0 42-29 71t-71 29ZM200-80q-33 0-56.5-23.5T120-160v-560q0-33 23.5-56.5T200-800h40v-80h80v80h320v-80h80v80h40q33 0 56.5 23.5T840-720v560q0 33-23.5 56.5T760-80H200Zm0-80h560v-400H200v400Zm0-480h560v-80H200v80Zm0 0v-80 80Z"
                />
              </svg>
              <div class="me-2 font-semibold">Date:</div>
              {{ prescription.date }}
            </div>
            <div class="text-secondary mb-4 flex text-sm">
              <svg
                class="me-2"
                xmlns="http://www.w3.org/2000/svg"
                height="20px"
                viewBox="0 -960 960 960"
                width="20px"
                fill="currentColor"
              >
                <path
                  d="M694-120 552-262l57-56 85 85 170-170 56 57-226 226ZM540-80q-108 0-184-76t-76-184v-23q-86-14-143-80.5T80-600v-240h120v-40h80v160h-80v-40h-40v160q0 66 47 113t113 47q66 0 113-47t47-113v-160h-40v40h-80v-160h80v40h120v240q0 90-57 156.5T360-363v23q0 75 52.5 127.5T540-160v80Z"
                />
              </svg>
              <div class="me-2 font-semibold">Doctor:</div>
              {{ prescription.doctor }}
            </div>
            <div class="text-secondary mb-4 flex text-sm">
              <svg
                class="me-2"
                xmlns="http://www.w3.org/2000/svg"
                height="20px"
                viewBox="0 -960 960 960"
                width="20px"
                fill="currentColor"
              >
                <path
                  d="M420-280h120v-140h140v-120H540v-140H420v140H280v120h140v140ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm0-560v560-560Z"
                />
              </svg>
              <div class="me-2 font-semibold">Hospital:</div>
              {{ prescription.hospital }}
            </div>
            <div class="text-secondary mb-4 flex text-sm">
              <svg
                class="me-2"
                xmlns="http://www.w3.org/2000/svg"
                height="20px"
                viewBox="0 -960 960 960"
                width="20px"
                fill="currentColor"
              >
                <path
                  d="M440-183v-274L296-600l51-51 133 133 133-133 51 51-144 143v274h-80Zm40-457Zm0 40ZM234-80l-94-94 94-94 51 51-43 43 43 43-51 51Zm246 0l-51-51 43-43-43-43 51-51 94 94-94 94Zm-40-720v-80h80v80h-80Z"
                />
              </svg>
              <div class="me-2 font-semibold">Pharmacy:</div>
              {{ prescription.pharmacy }}
            </div>
            <div class="text-success mb-4 flex">
              <svg
                class="me-2"
                xmlns="http://www.w3.org/2000/svg"
                height="20px"
                viewBox="0 -960 960 960"
                width="20px"
                fill="currentColor"
              >
                <path
                  d="M640-520q17 0 28.5-11.5T680-560q0-17-11.5-28.5T640-600q-17 0-28.5 11.5T600-560q0 17 11.5 28.5T640-520Zm-320 0q17 0 28.5-11.5T360-560q0-17-11.5-28.5T320-600q-17 0-28.5 11.5T280-560q0 17 11.5 28.5T320-520ZM480-400q-50 0-97.5-15.5T298-462q-17 14-37 22.5T216-428q29 37 70.5 63.5T378-326q9 35 33 63.5T480-226q41 0 73.5-25t42.5-66q120-19 197-85t77-158q0-26-6-51t-16-49q-12 17-29 26t-39 9q-50 0-85-35t-35-85q0-15 4-29.5t10-27.5H424q6 13 10 27.5t4 29.5q0 50-35 85t-85 35q-22 0-39-9t-29-26q-10 24-16 49t-6 51q0 92 77 158t197 85q10 41 42.5 66t73.5 25q48 0 84-33t36-81q0-27-13.5-50.5T680-420q-29 16-62.5 26.5T520-380q-9 0-18.5-.5T482-384q4 34 13 56.5t25 38.5q-11 14-24 21.5T480-260q-16 0-29-7.5T427-290q12-16 21-38.5t12-56.5q-11 3-20.5 3.5T420-380q-36 0-70-10.5T290-417q-24 5-45 8.5t-35 3.5q5 0 0 0-30 0-55-20t-25-50q0-19 11-36t29-27q-5 13-8 27t-3 29q0 16 6 30t18 24q11-2 27.5-6.5T248-444q-38-38-54-82.5T178-620q0-29 6-56.5t15-50.5q14 21 37 33.5t49 12.5q42 0 71-29t29-71q0-20-7.5-37.5T355-846q29-8 60-12t65-4q34 0 64.5 4t59.5 12q-15 13-22.5 30.5T574-778q0 42 29 71t71 29q26 0 49-12.5t37-33.5q9 23 15 50.5t6 56.5q0 50-16 93.5T711-441q17 3 33.5 7.5T772-426q12-10 18-24t6-30q0-15-3-29t-8-27q18 10 29 27t11 36q0 30-25 50t-55 20q-9 0-19.5-1t-20.5-3q-34 28-78 45t-96 17h-30Zm-.355-180Q497-580 508.5-591.75T520-620q0-17-11.855-28.5Q496.29-660 480-660t-28.5 11.5T440-620q0 17 11.5 28.5T479.645-580Z"
                />
              </svg>
              <div class="me-2 font-semibold">Medicines:</div>
              <div class="flex flex-wrap gap-2">
                <span 
                  v-for="medicine in prescription.medicines"
                  class="bg-accent/20 text-primary px-2 py-1 rounded-full text-xs"
                >
                  {{ medicine }}
                </span>
              </div>
            </div>
            <div class="text-accent mb-4 flex">
              <svg
                class="me-2"
                xmlns="http://www.w3.org/2000/svg"
                height="20px"
                viewBox="0 -960 960 960"
                width="20px"
                fill="currentColor"
              >
                <path
                  d="M556-80v-80h144v-404L480-206 260-564v404h144v80H160v-560l320 404 320-404v560h-80v-80h-164Zm40-457Zm0 40ZM234-80l-94-94 94-94 51 51-43 43 43 43-51 51Zm246 0l-51-51 43-43-43-43 51-51 94 94-94 94Zm-40-720v-80h80v80h-80Z"
                />
              </svg>
              <div class="me-2 font-semibold">Copay:</div>
              ${{ prescription.copay.toFixed(2) }}
            </div>
            <div class="text-h-terciary mb-4 flex">
              <svg
                class="me-2"
                xmlns="http://www.w3.org/2000/svg"
                height="20px"
                viewBox="0 -960 960 960"
                width="20px"
                fill="currentColor"
              >
                <path
                  d="M320-240h320v-80H320v80Zm0-160h320v-80H320v80Zm0-160h320v-80H320v80ZM880-80 720-240H160q-33 0-56.5-23.5T80-320v-480q0-33 23.5-56.5T160-880h640q33 0 56.5 23.5T880-800v720ZM800-280l-10-10v-510H170v480h550l80 80v-40Z"
                />
              </svg>
              <div class="me-2 font-semibold">Comments:</div>
              <span class="italic text-terciary">{{ prescription.comments }}</span>
            </div>
            <div class="flex justify-between mt-6">
              <span class="text-error font-bold" v-if="!prescription.secured">
                <svg
                  class="inline-block me-1"
                  xmlns="http://www.w3.org/2000/svg"
                  height="18px"
                  viewBox="0 -960 960 960"
                  width="18px"
                  fill="currentColor"
                >
                  <path
                    d="M480-280q17 0 28.5-11.5T520-320q0-17-11.5-28.5T480-360q-17 0-28.5 11.5T440-320q0 17 11.5 28.5T480-280Zm-40-160h80v-240h-80v240Zm40 360q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z"
                  />
                </svg>
                Not secured
              </span>
              <span class="text-success font-bold" v-else>
                <svg
                  class="inline-block me-1"
                  xmlns="http://www.w3.org/2000/svg"
                  height="18px"
                  viewBox="0 -960 960 960"
                  width="18px"
                  fill="currentColor"
                >
                  <path
                    d="m438-240 226-226-58-58-168 168-84-84-58 58 142 142ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Z"
                  />
                </svg>
                Secured
              </span>
              <div class="text-accent font-bold">
                Total: ${{ prescription.total.toFixed(2) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="edit" class="card mb-6">
      <p class="title mb-6">Edit Profile</p>
      <div class="mb-8">
        <span class="text-primary font-semibold">Name</span>
        <input type="text" class="field" :defaultValue="user?.name" />
      </div>
      <div class="mb-8">
        <span class="text-primary font-semibold">Email</span>
        <input type="email" class="field" :defaultValue="user?.email" />
      </div>
      <div class="mb-8">
        <span class="text-primary font-semibold">Phone</span>
        <input type="text" class="field" :defaultValue="user?.phone" />
      </div>
      <div class="mb-8">
        <span class="text-primary font-semibold">Address</span>
        <input type="text" class="field" :defaultValue="user?.address" />
      </div>
      <div class="mb-8">
        <span class="text-primary font-semibold">BirthDay</span>
        <input type="date" class="field" :defaultValue="user?.birthDate?.toString()" />
      </div>
      <div class="mb-8">
        <span class="text-primary font-semibold">CUI</span>
        <input type="number" class="field" :defaultValue="user?.cui.toString()" />
      </div>
      <div class="mb-8">
        <span class="text-primary font-semibold">Policy</span>
        <input type="text" class="field" :defaultValue="user?.policy.percentage.toString()" />
      </div>
      <div class="mb-8">
        <span class="text-primary font-semibold">Role</span>
        <input type="text" class="field" :defaultValue="profiles.role" />
      </div>
      <button class="btn mx-auto flex">
        <svg
          class="me-2"
          xmlns="http://www.w3.org/2000/svg"
          height="24px"
          viewBox="0 -960 960 960"
          width="24px"
          fill="currentColor"
        >
          <path
            d="M840-680v480q0 33-23.5 56.5T760-120H200q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160Zm-80 34L646-760H200v560h560v-446ZM480-240q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35ZM240-560h360v-160H240v160Zm-40-86v446-560 114Z"
          />
        </svg>
        Save
      </button>
    </div>
    
    <!-- Edit Appointments Section -->
    <div v-if="edit" class="card mb-6">
      <p class="title mb-6">Edit Appointments</p>
      <div v-for="(appointment, index) in appointments" :key="appointment.number" class="mb-8 border-b border-terciary pb-6">
        <div class="flex justify-between mb-4">
          <span class="text-accent text-lg font-bold">Appointment #{{ appointment.number }}</span>
          <button class="text-error hover:text-h-error transition-colors">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              height="22px"
              viewBox="0 -960 960 960"
              width="22px"
              fill="currentColor"
            >
              <path
                d="M280-120q-33 0-56.5-23.5T200-200v-520h-40v-80h200v-40h240v40h200v80h-40v520q0 33-23.5 56.5T680-120H280Zm400-600H280v520h400v-520ZM360-280h80v-360h-80v360Zm160 0h80v-360h-80v360ZM280-720v520-520Z"
              />
            </svg>
          </button>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="mb-4">
            <span class="text-primary font-semibold">Date</span>
            <input type="date" class="field" v-model="appointment.date" />
          </div>
          <div class="mb-4">
            <span class="text-primary font-semibold">Doctor</span>
            <input type="text" class="field" v-model="appointment.doctor" />
          </div>
          <div class="mb-4">
            <span class="text-primary font-semibold">Hospital</span>
            <input type="text" class="field" v-model="appointment.hospital" />
          </div>
        </div>
      </div>
      
      <!-- Add New Appointment Button -->
      <button class="btn flex mx-auto mt-4">
        <svg
          class="me-2"
          xmlns="http://www.w3.org/2000/svg"
          height="24px"
          viewBox="0 -960 960 960"
          width="24px"
          fill="currentColor"
        >
          <path
            d="M440-440H200q-17 0-28.5-11.5T160-480q0-17 11.5-28.5T200-520h240v-240q0-17 11.5-28.5T480-800q17 0 28.5 11.5T520-760v240h240q17 0 28.5 11.5T800-480q0 17-11.5 28.5T760-440H520v240q0 17-11.5 28.5T480-160q-17 0-28.5-11.5T440-200v-240Z"
          />
        </svg>
        Add New Appointment
      </button>
      
      <!-- Save Button -->
      <button class="btn mx-auto flex mt-8">
        <svg
          class="me-2"
          xmlns="http://www.w3.org/2000/svg"
          height="24px"
          viewBox="0 -960 960 960"
          width="24px"
          fill="currentColor"
        >
          <path
            d="M840-680v480q0 33-23.5 56.5T760-120H200q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160Zm-80 34L646-760H200v560h560v-446ZM480-240q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35ZM240-560h360v-160H240v160Zm-40-86v446-560 114Z"
          />
        </svg>
        Save All Appointments
      </button>
    </div>
    
    <!-- Edit Prescriptions Section -->
    <div v-if="edit" class="card mb-6">
      <p class="title mb-6">Edit Prescriptions</p>
      <div v-for="(prescription, index) in prescriptions" :key="prescription.id" class="mb-8 border-b border-terciary pb-6">
        <div class="flex justify-between mb-4">
          <span class="text-accent text-lg font-bold">Prescription #{{ prescription.id }}</span>
          <button class="text-error hover:text-h-error transition-colors">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              height="22px"
              viewBox="0 -960 960 960"
              width="22px"
              fill="currentColor"
            >
              <path
                d="M280-120q-33 0-56.5-23.5T200-200v-520h-40v-80h200v-40h240v40h200v80h-40v520q0 33 23.5 56.5T480-120H280Zm400-600H280v520h400v-520ZM360-280h80v-360h-80v360Zm160 0h80v-360h-80v360ZM280-720v520-520Z"
              />
            </svg>
          </button>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="mb-4">
            <span class="text-primary font-semibold">Hospital</span>
            <input type="text" class="field" v-model="prescription.hospital" />
          </div>
          <div class="mb-4">
            <span class="text-primary font-semibold">Doctor</span>
            <input type="text" class="field" v-model="prescription.doctor" />
          </div>
          <div class="mb-4">
            <span class="text-primary font-semibold">Pharmacy</span>
            <input type="text" class="field" v-model="prescription.pharmacy" />
          </div>
          <div class="mb-4">
            <span class="text-primary font-semibold">Date</span>
            <input type="date" class="field" v-model="prescription.date" />
          </div>
          <div class="mb-4">
            <span class="text-primary font-semibold">Total</span>
            <input type="number" step="0.01" class="field" v-model="prescription.total" />
          </div>
          <div class="mb-4">
            <span class="text-primary font-semibold">Copay</span>
            <input type="number" step="0.01" class="field" v-model="prescription.copay" />
          </div>
          <div class="mb-4 md:col-span-2">
            <span class="text-primary font-semibold">Comments</span>
            <textarea class="field" v-model="prescription.comments"></textarea>
          </div>
          <div class="mb-4 md:col-span-2">
            <span class="text-primary font-semibold">Medicines</span>
            <div class="flex flex-wrap gap-2 mt-2">
              <div v-for="(medicine, medIndex) in prescription.medicines" :key="medIndex" class="flex items-center bg-accent/20 text-primary px-3 py-1 rounded-full">
                <span>{{ medicine }}</span>
                <button class="ml-2 text-primary hover:text-error" @click="prescription.medicines.splice(medIndex, 1)">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    height="16px"
                    viewBox="0 -960 960 960"
                    width="16px"
                    fill="currentColor"
                  >
                    <path
                      d="M480-424 284-228q-11 11-28 11t-28-11q-11-11-11-28t11-28l196-196-196-196q-11-11-11-28t11-28q11-11 28-11t28 11l196 196 196-196q11-11 28-11t28 11q11 11 11 28t-11 28L536-480l196 196q11 11 11 28t-11 28q-11 11-28 11t-28-11L480-424Z"
                    />
                  </svg>
                </button>
              </div>
              <div class="flex items-center">
                <input type="text" placeholder="Add medicine..." class="px-3 py-1 rounded-full border border-accent text-primary text-sm" />
                <button class="ml-2 bg-accent text-light dark:text-dark rounded-full p-1">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    height="16px"
                    viewBox="0 -960 960 960"
                    width="16px"
                    fill="currentColor"
                  >
                    <path
                      d="M440-440H200q-17 0-28.5-11.5T160-480q0-17 11.5-28.5T200-520h240v-240q0-17 11.5-28.5T480-800q17 0 28.5 11.5T520-760v240h240q17 0 28.5 11.5T800-480q0 17-11.5 28.5T760-440H520v240q0 17-11.5 28.5T480-160q-17 0-28.5-11.5T440-200v-240Z"
                    />
                  </svg>
                </button>
              </div>
            </div>
          </div>
          <div class="mb-4">
            <span class="text-primary font-semibold">Secured</span>
            <div class="mt-2">
              <label class="inline-flex items-center cursor-pointer">
                <input type="checkbox" class="sr-only peer" v-model="prescription.secured">
                <div class="relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-accent/50 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-accent"></div>
                <span class="ms-3 text-sm font-medium text-primary">{{ prescription.secured ? 'Yes' : 'No' }}</span>
              </label>
            </div>
          </div>
          <div class="mb-4">
            <span class="text-primary font-semibold">Auth Number</span>
            <input type="text" class="field" v-model="prescription.auth_no" />
          </div>
        </div>
      </div>
      
      <!-- Add New Prescription Button -->
      <button class="btn flex mx-auto mt-4">
        <svg
          class="me-2"
          xmlns="http://www.w3.org/2000/svg"
          height="24px"
          viewBox="0 -960 960 960"
          width="24px"
          fill="currentColor"
        >
          <path
            d="M440-440H200q-17 0-28.5-11.5T160-480q0-17 11.5-28.5T200-520h240v-240q0-17 11.5-28.5T480-800q17 0 28.5 11.5T520-760v240h240q17 0 28.5 11.5T800-480q0 17-11.5 28.5T760-440H520v240q0 17-11.5 28.5T480-160q-17 0-28.5-11.5T440-200v-240Z"
          />
        </svg>
        Add New Prescription
      </button>
      
      <!-- Save Button -->
      <button class="btn mx-auto flex mt-8">
        <svg
          class="me-2"
          xmlns="http://www.w3.org/2000/svg"
          height="24px"
          viewBox="0 -960 960 960"
          width="24px"
          fill="currentColor"
        >
          <path
            d="M840-680v480q0 33-23.5 56.5T760-120H200q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h480l160 160Zm-80 34L646-760H200v560h560v-446ZM480-240q50 0 85-35t35-85q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 50 35 85t85 35ZM240-560h360v-160H240v160Zm-40-86v446-560 114Z"
          />
        </svg>
        Save All Prescriptions
      </button>
    </div>
  </div>
</template>
