<template>
  <div class="create-product-container">
    <h2 class="text-2xl font-bold text-center text-blue-800 mb-4">
      Crear Nuevo Producto
    </h2>

    <!-- Mensaje de error si lo hay -->
    <div v-if="errorMessage" class="mb-4 text-red-600 text-center">
      {{ errorMessage }}
    </div>

    <!-- Formulario -->
    <form @submit.prevent="createProduct">
      <!-- Nombre -->
      <div class="mb-4">
        <label class="block text-gray-700">Nombre</label>
        <input v-model="name" type="text" class="input-field" required />
      </div>

      <!-- Medicamento Activo -->
      <div class="mb-4">
        <label class="block text-gray-700">Medicamento Activo</label>
        <input v-model="activeMedicament" type="text" class="input-field" required />
      </div>

      <!-- Descripción -->
      <div class="mb-4">
        <label class="block text-gray-700">Descripción</label>
        <textarea v-model="description" class="input-field" required></textarea>
      </div>

      <!-- Imagen -->
      <div class="mb-4">
        <label class="block text-gray-700">Imagen</label>
        <input v-model="image" type="text" class="input-field" required />
      </div>

      <!-- Concentración -->
      <div class="mb-4">
        <label class="block text-gray-700">Concentración</label>
        <input v-model="concentration" type="text" class="input-field" required />
      </div>

      <!-- Presentación -->
      <div class="mb-4">
        <label class="block text-gray-700">Presentación</label>
        <input v-model="presentacion" type="number" class="input-field" required />
      </div>

      <!-- Stock -->
      <div class="mb-4">
        <label class="block text-gray-700">Stock</label>
        <input v-model="stock" type="number" class="input-field" required />
      </div>

      <!-- Marca -->
      <div class="mb-4">
        <label class="block text-gray-700">Marca</label>
        <input v-model="brand" type="text" class="input-field" required />
      </div>

      <!-- Prescripción -->
      <div class="mb-4">
        <label class="block text-gray-700">Prescripción</label>
        <input type="checkbox" v-model="prescription" class="checkbox-field" />
      </div>

      <!-- Precio -->
      <div class="mb-4">
        <label class="block text-gray-700">Precio</label>
        <input v-model="price" type="number" step="0.01" class="input-field" required />
      </div>

      <!-- Unidades Vendidas -->
      <div class="mb-4">
        <label class="block text-gray-700">Unidades Vendidas</label>
        <input v-model="soldUnits" type="number" class="input-field" required />
      </div>

      <!-- Botón de crear producto -->
      <button type="submit" class="create-button">Crear Producto</button>
    </form>

    <!-- Modal de éxito -->
    <div v-if="showModal" class="modal">
      <div class="modal-content">
        <span class="close" @click="showModal = false">&times;</span>
        <p>Producto creado exitosamente.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
import ApiService from '../services/ApiService';
// Campos reactivamente enlazados al formulario
const name = ref('');
const activeMedicament = ref('');
const description = ref('');
const image = ref('');
const concentration = ref('');
const presentacion = ref('');
const stock = ref(0);
const brand = ref('');
const prescription = ref(false);
const price = ref(0.0);
const soldUnits = ref(0);
const errorMessage = ref('');
const showModal = ref(false);


// Función principal de crear producto
const createProduct = async () => {
  errorMessage.value = '';

  try {
    // Petición POST al backend (ajusta la URL a la tuya)
    const response = await axios.post(ApiService.getPharmacyApiUrl("/medicines"), {
      name: name.value,
      activeMedicament: activeMedicament.value,
      description: description.value,
      image: image.value,
      concentration: concentration.value,
      presentacion: presentacion.value,
      stock: stock.value,
      brand: brand.value,
      prescription: prescription.value,
      price: price.value,
      soldUnits: soldUnits.value
    });

    console.log("Producto creado exitosamente:", response.data);
    // Mostrar modal de éxito
    showModal.value = true;
    // Limpiar campos del formulario
    name.value = '';
    activeMedicament.value = '';
    description.value = '';
    image.value = '';
    concentration.value = '';
    presentacion.value = '';
    stock.value = 0;
    brand.value = '';
    prescription.value = false;
    price.value = 0.0;
    soldUnits.value = 0;
  } catch (error) {
    console.error("Error al crear el producto:", error);
    errorMessage.value = 'Error al crear el producto. Por favor, inténtelo de nuevo.';
  }
};
</script>

<style scoped>
/* Ejemplo de estilos, ajusta a tu gusto. */
.create-product-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 50px;
  background-color: #f8f9fa;
}

.input-field {
  width: 100%;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 16px;
  margin-top: 8px;
}

.checkbox-field {
  margin-top: 8px;
}

.create-button {
  width: 100%;
  background: #1e40af;
  color: white;
  padding: 12px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 1rem;
}

.create-button:hover {
  background: #1e3a8a;
}

.modal {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  z-index: 1;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}

.close {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 24px;
  cursor: pointer;
}
</style>
