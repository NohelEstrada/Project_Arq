<script setup lang="ts">
import { ref } from "vue";
import axios from "axios";
import { getInsuranceApiUrl } from "../../utils/api";
const search = useSearch();
interface Policy {
  idPolicy: number;
  percentage: number;
  creationDate: string;
  expDate: string;
  cost: number;
  enabled: number;
}
const policies = ref<Policy[]>([]);
const config = useRuntimeConfig();
const ip = config.public.ip;
const fetchPolicy = async () => {
  try {
    notify({
      type: "loading",
      title: "Loading policies",
      description: "Please wait...",
    });
    const response = await axios.get(getInsuranceApiUrl("/policy"));
    policies.value = response.data;
    console.log("Hospitals obtenidos:", policies.value);
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
const updatePolicy = async (policy: Policy) => {
  try {
    await axios.put(getInsuranceApiUrl("/policy"), policy);
    notify({ type: 'success', title: 'Policy updated', description: 'Successfully updated policy.' });
  } catch (err) {
    console.error('Error updating policy:', err);
    notify({ type: 'error', title: 'Update failed', description: 'Failed to update policy.' });
  }
};
const edit = useEdit();
fetchPolicy();
</script>

<template>
  <main
    class="bg-image-[url(https://cdn.pixabay.com/photo/2020/11/03/15/32/man-5710164_1280.jpg)]"
  >
    <Search v-if="search" :fieldNames="['Policy Code', 'Percentage', 'Anual Price', 'Creation Date', 'Expire Date']" :searchFields="['idPolicy', 'percentage', 'cost', 'creationDate', 'expDate']" v-model:output="policies" />
    <div class="responsive-grid">
      <div v-if="!edit" class="card m-4" v-for="policy in policies">
        <div class="title mb-6">
          {{ policy.percentage }}<span class="font-semibold">%</span>
        </div>

        <div class="text-md text-accent hover:text-h-accent mb-2 flex">
          <div class="flex pe-2 font-semibold">
            <svg
              class="me-2"
              xmlns="http://www.w3.org/2000/svg"
              height="24px"
              viewBox="0 -960 960 960"
              width="24px"
              fill="currentColor"
            >
              <path
                d="M40-120v-200h80v120h120v80H40Zm680 0v-80h120v-120h80v200H720ZM160-240v-480h80v480h-80Zm120 0v-480h40v480h-40Zm120 0v-480h80v480h-80Zm120 0v-480h120v480H520Zm160 0v-480h40v480h-40Zm80 0v-480h40v480h-40ZM40-640v-200h200v80H120v120H40Zm800 0v-120H720v-80h200v200h-80Z"
              />
            </svg>
            Policy Code:
          </div>
          {{ policy.idPolicy }}
        </div>
        <div class="text-md text-success hover:text-h-success mb-2 flex">
          <div class="flex pe-2 font-semibold">
            <svg
              class="me-2"
              xmlns="http://www.w3.org/2000/svg"
              height="24px"
              viewBox="0 -960 960 960"
              width="24px"
              fill="currentColor"
            >
              <path
                d="M300-520q-58 0-99-41t-41-99q0-58 41-99t99-41q58 0 99 41t41 99q0 58-41 99t-99 41Zm0-80q25 0 42.5-17.5T360-660q0-25-17.5-42.5T300-720q-25 0-42.5 17.5T240-660q0 25 17.5 42.5T300-600Zm360 440q-58 0-99-41t-41-99q0-58 41-99t99-41q58 0 99 41t41 99q0 58-41 99t-99 41Zm0-80q25 0 42.5-17.5T720-300q0-25-17.5-42.5T660-360q-25 0-42.5 17.5T600-300q0 25 17.5 42.5T660-240Zm-444 80-56-56 584-584 56 56-584 584Z"
              />
            </svg>
            Discount Percentage:
          </div>
          {{ policy.percentage }}
        </div>
        <div class="text-md text-error hover:text-h-error mb-6 flex">
          <div class="flex pe-2 font-semibold">
            <svg
              class="me-2"
              xmlns="http://www.w3.org/2000/svg"
              height="24px"
              viewBox="0 -960 960 960"
              width="24px"
              fill="currentColor"
            >
              <path
                d="M560-440q-50 0-85-35t-35-85q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35ZM280-320q-33 0-56.5-23.5T200-400v-320q0-33 23.5-56.5T280-800h560q33 0 56.5 23.5T920-720v320q0 33-23.5 56.5T840-320H280Zm80-80h400q0-33 23.5-56.5T840-480v-160q-33 0-56.5-23.5T760-720H360q0 33-23.5 56.5T280-640v160q33 0 56.5 23.5T360-400Zm440 240H120q-33 0-56.5-23.5T40-240v-440h80v440h680v80ZM280-400v-320 320Z"
              />
            </svg>
            Anual Price:
          </div>
          Q.{{ policy.cost }}
        </div>
        <div class="text-md text-primary hover:text-h-primary mb-2 flex">
          <div class="flex pe-2 font-semibold">
            <svg
              class="me-2"
              xmlns="http://www.w3.org/2000/svg"
              height="24px"
              viewBox="0 -960 960 960"
              width="24px"
              fill="currentColor"
            >
              <path
                d="M360-300q-42 0-71-29t-29-71q0-42 29-71t71-29q42 0 71 29t29 71q0 42-29 71t-71 29ZM200-80q-33 0-56.5-23.5T120-160v-560q0-33 23.5-56.5T200-800h40v-80h80v80h320v-80h80v80h40q33 0 56.5 23.5T840-720v560q0 33-23.5 56.5T760-80H200Zm0-80h560v-400H200v400Zm0-480h560v-80H200v80Zm0 0v-80 80Z"
              />
            </svg>
            Creation Date:
          </div>
          {{ policy.creationDate }}
        </div>
        <div class="text-md text-primary hover:text-h-primary mb-6 flex">
          <div class="flex pe-2 font-semibold">
            <svg
              class="me-2"
              xmlns="http://www.w3.org/2000/svg"
              height="24px"
              viewBox="0 -960 960 960"
              width="24px"
              fill="currentColor"
            >
              <path
                d="M580-240q-42 0-71-29t-29-71q0-42 29-71t71-29q42 0 71 29t29 71q0 42-29 71t-71 29ZM200-80q-33 0-56.5-23.5T120-160v-560q0-33 23.5-56.5T200-800h40v-80h80v80h320v-80h80v80h40q33 0 56.5 23.5T840-720v560q0 33-23.5 56.5T760-80H200Zm0-80h560v-400H200v400Zm0-480h560v-80H200v80Zm0 0v-80 80Z"
              />
            </svg>
            Expire Date:
          </div>
          {{ policy.expDate }}
        </div>
        <div class="text-secondary mb-4 text-justify text-sm">
          {{ policy.percentage }}
        </div>
      </div>
      <div v-if="edit" class="card m-4" v-for="(policy, index) in policies">
        <span class="text-primary font-semibold">Discount Percentage</span>
        <input type="number" class="field mb-8" v-model.number="policy.percentage" />

        <span class="text-primary font-semibold">Policy Code</span>
        <input type="number" class="field mb-8" v-model.number="policy.idPolicy" />

        <span class="text-primary font-semibold">Anual Price</span>
        <input type="number" class="field mb-8" v-model.number="policy.cost" />

        <span class="text-primary font-semibold">Creation Date</span>
        <input type="date" class="field mb-8" v-model="policy.creationDate" />

        <span class="text-primary font-semibold">Expire Date</span>
        <input type="date" class="field mb-8" v-model="policy.expDate" />

        <button class="btn mx-auto flex justify-center" @click="updatePolicy(policy)">
          Save
        </button>
      </div>
      <button v-if="edit" class="btn mx-auto flex justify-center mb-6"
        @click="()=>{
          policies.push({
            idPolicy: 0,
            percentage: 0,
            cost: 0,
            creationDate: '',
            expDate: '',
            enabled: 1,
          });
        }"
      >
        Add Policy
      </button>
    </div>
  </main>
</template>
