<!-- eslint-disable vue/multi-word-component-names -->
<!-- RecetaForm.vue -->

<template>
    <div class="recipe-container"> 
      <!-- Sección derecha con el formulario de receta -->
      <div class="recipe-box">
        <h1 class="text-2xl font-bold text-center text-blue-800 mb-4">
          Sus Recetas
        </h1>
        <img src="@/assets/logo.png" alt="Pharmacy Logo" class="logo" />
  
        <form @submit.prevent="createRecipe">
          <!-- ID de la receta (si lo defines manualmente o lo obtienes de un sistema) -->
          <div class="form-group">
            <label for="idRecipe">ID Receta</label>
            <div class="input-group">
              <input type="text" id="idRecipe" v-model="idRecipe" required />
              <span class="icon">🆔</span>
            </div>
          </div>
  
          <!-- ID del usuario al que pertenece la receta -->
          <div class="form-group">
            <label for="userId">ID Usuario</label>1
            <div class="input-group">
              <input type="text" id="userId" v-model="userId" required />
              <span class="icon">👤</span>
            </div>
          </div>
  
          <!-- ID de la póliza (si aplica en tu lógica) -->
          <div class="form-group">
            <label for="policyId">ID Póliza</label>
            <div class="input-group">
              <input type="text" id="policyId" v-model="policyId" />
              <span class="icon">📑</span>
            </div>
          </div>
  
          <!-- Médico que emite la receta -->
          <div class="form-group">
            <label for="doctor">Médico</label>
            <div class="input-group">
              <input type="text" id="doctor" v-model="doctor" required />
              <span class="icon">🏥</span>
            </div>
          </div>
  
          <!-- Diagnóstico -->
          <div class="form-group">
            <label for="diagnosis">Diagnóstico</label>
            <div class="input-group">
              <input type="text" id="diagnosis" v-model="diagnosis" required />
              <span class="icon">🔬</span>
            </div>
          </div>
  
          <!-- Fecha de emisión de la receta -->
          <div class="form-group">
            <label for="date">Fecha</label>
            <div class="input-group">
              <input type="date" id="date" v-model="date" required />
              <span class="icon">📅</span>
            </div>
          </div>
  
          <!-- Medicamentos indicados -->
          <div class="form-group">
            <label for="medicines">Medicamentos</label>
            <div class="input-group">
              <textarea
                id="medicines"
                v-model="medicines"
                placeholder="Lista de medicamentos, dosis, etc."
                rows="3"
                required
              ></textarea>
              <span class="icon">💊</span>
            </div>
          </div>
  
          <!-- Botón para guardar la receta -->
          <button type="submit" class="recipe-button">
            Guardar Receta
          </button>
        </form>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
//   import { useRouter } from 'vue-router';
  
//   const router = useRouter();
  
  // Campos de la receta
  const idRecipe = ref('');
  const userId = ref('');
  const policyId = ref('');
  const doctor = ref('');
  const diagnosis = ref('');
  const date = ref('');
  const medicines = ref('');
  
  // Acción al crear la receta
  const createRecipe = async () => {
    // Lógica para guardar la receta en tu base de datos:
    // Por ejemplo, hacer un fetch o axios.post a tu API
    const nuevaReceta = {
      idRecipe: idRecipe.value,
      userId: userId.value,
      policyId: policyId.value,
      doctor: doctor.value,
      diagnosis: diagnosis.value,
      date: date.value,
      medicines: medicines.value
    };
  
    console.log('Creando Receta:', nuevaReceta);
  
    // Después de guardar, podrías redirigir a otra vista
    // router.push('/otraRuta');
  };
  </script>
  
  <style scoped>
  /* Contenedor principal */
  .recipe-container {
    display: flex;
    height: 100vh;
    background-color: #f8f9fa;
  }
  
  /* Sección izquierda con imagen */
  .recipe-image {
    flex: 1;
    background: url('@/assets/farmaciareg.jpeg') no-repeat center center;
    background-size: cover;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  /* Sección derecha con el formulario */
  .recipe-box {
    flex: 1;
    background: white;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 50px;
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
    border-radius: 15px;
  }
  
  /* Logo */
  .logo {
    max-width: 120px;
    margin-bottom: 20px;
  }
  
  /* Formulario */
  form {
    display: flex;
    flex-direction: column;
    width: 100%;
    max-width: 400px;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  }
  
  /* Campos de entrada */
  .form-group {
    margin-bottom: 15px;
  }
  
  label {
    font-weight: bold;
    color: #495057;
  }
  
  /* Input con iconos */
  .input-group {
    position: relative;
    width: 100%;
  }
  
  input,
  textarea {
    width: 100%;
    padding: 12px 40px 12px 12px;
    border: 1px solid #ced4da;
    border-radius: 8px;
    font-size: 16px;
    box-sizing: border-box;
  }
  
  .icon {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    color: #6c757d;
  }
  
  /* Botón para guardar la receta */
  .recipe-button {
    width: 100%;
    background: #1e40af;
    color: white;
    padding: 12px;
    border-radius: 8px;
    font-size: 18px;
    font-weight: bold;
    cursor: pointer;
    transition: 0.3s;
    text-align: center;
    margin-top: 10px;
  }
  
  .recipe-button:hover {
    background: #1e3a8a;
  }
  
  /* Responsivo */
  @media (max-width: 768px) {
    .recipe-container {
      flex-direction: column;
    }
    .recipe-image {
      display: none;
    }
  }
  </style>