<template>
  <div class="prescription-pay-container">
    <h2 class="text-2xl font-bold text-center text-blue-800 mb-4">Detalle de la Receta</h2>

    <!-- Mensaje de error si lo hay -->
    <div v-if="errorMessage" class="mb-4 text-red-600 text-center">
      {{ errorMessage }}
    </div>

    <!-- Mostrar la receta cuando se cargue -->
    <div v-if="prescription">
      <p><strong>ID Receta:</strong> {{ prescription.id.prescriptionId }}</p>

      <!-- Tabla de Medicinas si existen -->
      <div v-if="prescription.medicines && prescription.medicines.length > 0">
        <table class="medicine-table">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Concentración</th>
              <th>Presentación</th>
              <th>Dosis</th>
              <th>Frecuencia</th>
              <th>Duración</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="medicine in prescription.medicines" :key="medicine.idMedicine">
              <tr>
                <td>{{ medicine.name }}</td>
                <td>{{ medicine.concentration }}</td>
                <td>Conjunto de {{ medicine.presentacion }} unidades</td>
                <td>{{ medicine.dosis }}</td>
                <td>{{ medicine.frecuencia }} horas</td>
                <td>{{ medicine.duracion }} días</td>
              </tr>
              <tr v-if="medicine.details" :key="medicine.idMedicine + '-purchase'">
                <td colspan="6">
                  <div class="purchase-info"
                       style="display: flex;
                              align-items: flex-start;
                              gap: 20px;
                              margin-top: 1rem;">
                    <img :src="medicine.details.image"
                         :alt="'Imagen de ' + medicine.name"
                         style="width: 200px;
                                height: 200px;
                                object-fit: cover;">
                    <div style="display: flex;
                                flex-direction: column;
                                gap: 0.5rem;">
                      <p><strong>Nombre: {{ medicine.name }}</strong></p>
                      <p><strong>Principio activo: {{ medicine.activeMedicament }}</strong></p>
                      <p><strong>Presentación: {{ medicine.presentacion }}</strong></p>
                      <p>
                        <strong>Cantidad a comprar: </strong>
                        <span v-if="Math.ceil((medicine.dosis * medicine.duracion) / medicine.details.presentacion) <= medicine.details.stock">
                          {{ Math.ceil((medicine.dosis * medicine.duracion) / medicine.details.presentacion) }}
                        </span>
                        <span v-else>NO DISPONIBLE</span>
                      </p>
                    </div>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <div v-if="prescription && allMedicinesAvailable" class="complete-purchase-container" style="margin-top: 20px; text-align: center;">
        <button @click="openModal" class="complete-purchase-button" style="padding: 10px 20px; font-size: 16px;">Completar compra</button>
      </div>

      <!-- Modal for credit card details -->
      <div v-if="isModalOpen" class="modal-overlay" style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center;">
        <div class="modal-content" style="background: #fff; padding: 20px; border-radius: 5px; width: 300px; text-align: center;">
          <h3>Datos de Tarjeta</h3>
          <input v-model="cardNumber" placeholder="Número de tarjeta" style="width: 100%; padding: 8px; margin: 5px 0;" />
          <input v-model="cardExpiry" @input="handleExpiryInput" placeholder="MM/AA" style="width: 100%; padding: 8px; margin: 5px 0;" />
          <input v-model="cardCvv" placeholder="CVV" style="width: 100%; padding: 8px; margin: 5px 0;" />
          <button :disabled="!isCardValid" @click="completePurchase" style="padding: 8px 16px; margin-top: 10px;">Comprar</button>
          <button @click="closeModal" style="padding: 8px 16px; margin-top: 10px; margin-left: 10px;">Cancelar</button>
        </div>
      </div>
    </div>
    <div v-else>
      <p>Cargando receta...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import ApiService from '../services/ApiService';
const route = useRoute();
const prescription = ref(null);
const errorMessage = ref('');
const isModalOpen = ref(false);
const cardNumber = ref('');
const cardExpiry = ref('');
const cardCvv = ref('');
const handleExpiryInput = () => {
  // Remove non-digit characters
  let val = cardExpiry.value.replace(/[^0-9]/g, '');
  if (val.length > 2) {
    cardExpiry.value = val.slice(0, 2) + '/' + val.slice(2, 4);
  } else {
    cardExpiry.value = val;
  }
};

const isCardValid = computed(() => {
  return cardNumber.value && cardExpiry.value && cardCvv.value;
});

const allMedicinesAvailable = computed(() => {
  return prescription.value && prescription.value.medicines.every(med => med.details && Math.ceil((med.dosis * med.duracion) / med.details.presentacion) <= med.details.stock);
});

const fetchMedicineDetails = async (medicine) => {
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl(`/medicines/${medicine.idMedicine}`));
    // Extract only specific fields: name, presentacion, concentration, image, and stock
    const { name, presentacion, concentration, image, stock } = response.data;
    medicine.details = { name, presentacion, concentration, image, stock };
  } catch (error) {
    console.error(`Error fetching details for medicine id ${medicine.idMedicine}:`, error);
    medicine.details = null;
  }
};

const fetchPrescription = async () => {
  try {
    const prescriptionId = route.params.id;
    // Fetch all prescription medicines as in Prescriptions.vue
    const response = await axios.get(ApiService.getPharmacyApiUrl("/prescription_medicines"));
    const allData = response.data;
    // Filtrar solo las recetas que coinciden con el id de la receta
    const filtered = allData.filter(p => p.id.prescriptionId == prescriptionId);

    if (filtered.length > 0) {
      // Agrupar los datos por prescriptionId para consolidar las medicinas
      const grouped = {};
      filtered.forEach(p => {
        const prescId = p.id.prescriptionId;
        if (!grouped[prescId]) {
          grouped[prescId] = {
            ...p, // Copy all properties of p (including idPrescription, approved, hospital, user, etc.)
            medicines: []
          };
        }
        grouped[prescId].medicines.push({
          ...p.medicine,
          dosis: p.dosis,
          frecuencia: p.frecuencia,
          duracion: p.duracion,
          cantidad: 1,
          precio: p.medicine.price || 0
        });
      });
      prescription.value = grouped[prescriptionId];

      if (prescription.value && prescription.value.medicines) {
        prescription.value.medicines.forEach(med => {
          fetchMedicineDetails(med);
        });
      }
      console.log(prescription.value.prescription.user.idUser)
    } else {
      errorMessage.value = 'No se encontró la receta.';
    }
  } catch (error) {
    console.error('Error al obtener la receta:', error);
    errorMessage.value = 'Error al obtener la receta. Por favor, inténtelo de nuevo.';
  }
};

const openModal = () => {
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

const completePurchase = async () => {
  if (!isCardValid.value) return;
  try {
    // Create order with status 'Completado' and user id from prescription
    const orderResponse = await axios.post(ApiService.getPharmacyApiUrl("/orders"), {
      status: "Completado",
      user: { idUser: prescription.value.prescription.user.idUser }
    });
    const order = orderResponse.data;

    // For each medicine, calculate quantity and send to order_medicines API
    for (const med of prescription.value.medicines) {
      const quantity = Math.ceil((med.dosis * med.duracion) / med.details.presentacion);
      await axios.post(ApiService.getPharmacyApiUrl("/order_medicines"), {
        orders: order,
        medicine: { idMedicine: med.idMedicine },
        quantity: quantity,
        cost: med.precio || 0,
        total: (med.precio || 0) * quantity
      });
    }

    closeModal();
    alert("Gracias por tu compra");
    window.location.href = "/";
  } catch (error) {
    console.error("Error completing purchase:", error);
    alert("Error al completar la compra. Intente de nuevo.");
  }
};

onMounted(() => {
  fetchPrescription();
});
</script>

<style scoped>
.prescription-pay-container {
  padding: 50px;
  background-color: #f8f9fa;
}

.medicine-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.medicine-table th, .medicine-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: center;
}

.medicine-table th {
  background-color: #f4f4f4;
  font-weight: bold;
}
</style>
