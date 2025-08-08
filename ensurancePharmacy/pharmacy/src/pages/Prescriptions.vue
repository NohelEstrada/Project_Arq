<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="prescriptions-container">
    <div class="header-section">
      <h2 class="page-title">Mis Recetas Médicas</h2>
      <p class="page-subtitle">Revisa y compra medicamentos de tus recetas médicas</p>
    </div>

    <!-- Mensaje de error si lo hay -->
    <div v-if="errorMessage" class="error-message">
      <i class="fas fa-exclamation-circle"></i>
      <span>{{ errorMessage }}</span>
    </div>

    <!-- Loader mientras carga las recetas -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Cargando tus recetas...</p>
    </div>

    <!-- Lista de recetas -->
    <div v-else-if="recipes && recipes.length > 0" class="prescriptions-list">
      <div v-for="recipe in recipes" :key="recipe._id" class="prescription-card">
        <div class="prescription-header">
          <div class="patient-info">
            <span class="patient-name">{{ getPatientName(recipe) }}</span>
            <span v-if="getPatientEmail(recipe)" class="patient-email">{{ getPatientEmail(recipe) }}</span>
          </div>
          <div class="recipe-date">
            <i class="fas fa-calendar-alt"></i>
            <span>{{ recipe.formatted_date || recipe.created_at || 'Fecha no disponible' }}</span>
          </div>
        </div>

        <div class="prescription-body">
          <div class="recipe-details">
            <div class="detail-item">
              <span class="detail-label">ID Receta:</span>
              <span class="detail-value">{{ recipe._id }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Diagnóstico:</span>
              <span class="detail-value diagnostic">{{ getDiagnostic(recipe) }}</span>
            </div>
          </div>

          <div v-if="recipe.has_insurance" class="insurance-badge">
            <i class="fas fa-shield-alt"></i>
            Con cobertura de seguro
          </div>
        </div>

        <!-- Tabla de Medicinas -->
        <div class="medicines-section" v-if="recipe.medicines && recipe.medicines.length > 0">
          <h3 class="medicines-title">Medicamentos Recetados</h3>
          
          <div class="medicines-list">
            <div v-for="medicine in recipe.medicines" :key="medicine._id" class="medicine-card">
              <div class="medicine-info">
                <h4 class="medicine-name">{{ medicine.principioActivo }}</h4>
                <div class="medicine-details">
                  <span class="medicine-concentration">{{ medicine.concentracion }}</span>
                  <span class="medicine-presentation">{{ medicine.presentacion }}</span>
                </div>
              </div>
              
              <div class="dosage-info">
                <div class="dosage-item">
                  <span class="dosage-label">Dosis:</span>
                  <span class="dosage-value">{{ medicine.dosis }}</span>
                </div>
                <div class="dosage-item">
                  <span class="dosage-label">Frecuencia:</span>
                  <span class="dosage-value">{{ medicine.frecuencia }}</span>
                </div>
                <div class="dosage-item">
                  <span class="dosage-label">Duración:</span>
                  <span class="dosage-value">{{ medicine.duracion }}</span>
                </div>
              </div>
              
              <button 
                class="buy-button" 
                @click="goToVerification(medicine.principioActivo, recipe._id)"
              >
                <i class="fas fa-shopping-cart"></i>
                Comprar
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Mensaje si no hay recetas -->
    <div v-else class="empty-state">
      <div class="empty-icon">
        <i class="fas fa-clipboard-list"></i>
      </div>
      <h3>No hay recetas disponibles</h3>
      <p>Cuando tengas recetas médicas recetadas, aparecerán aquí.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import ApiService from '../services/ApiService';
const router = useRouter();
const recipes = ref([]);
const errorMessage = ref('');
const patientInfo = ref(null);
const isLoading = ref(true);

// Funciones auxiliares para extraer información del paciente
const getPatientName = (recipe) => {
  // Si el paciente es un objeto completo en la receta
  if (recipe.patient && typeof recipe.patient === 'object' && recipe.patient.username) {
    return recipe.patient.username;
  }
  // Si el paciente está referenciado como ID y tenemos la información del paciente guardada
  if (patientInfo.value && recipe.patient === patientInfo.value._id) {
    return patientInfo.value.username;
  }
  // Si doctor_details está presente, usamos el formato doctor_details
  if (recipe.doctor_details) {
    return recipe.patient_details ? recipe.patient_details.username : 'Paciente';
  }
  return 'Paciente';
};

const getPatientEmail = (recipe) => {
  // Si el paciente es un objeto completo en la receta
  if (recipe.patient && typeof recipe.patient === 'object' && recipe.patient.email) {
    return recipe.patient.email;
  }
  // Si el paciente está referenciado como ID y tenemos la información del paciente guardada
  if (patientInfo.value && recipe.patient === patientInfo.value._id) {
    return patientInfo.value.email;
  }
  // Si patient_details está presente
  if (recipe.patient_details) {
    return recipe.patient_details.email;
  }
  return '';
};

const getDiagnostic = (recipe) => {
  // Intentar obtener diagnóstico desde diferentes estructuras posibles
  if (recipe.diagnostic) {
    return recipe.diagnostic;
  }
  if (recipe.medicines && recipe.medicines.length > 0 && recipe.medicines[0].diagnostico) {
    return recipe.medicines[0].diagnostico;
  }
  return 'No especificado';
};

// Función para navegar a la página de verificación
function goToVerification(medicinePrincipioActivo, recipeId) {
  if (!medicinePrincipioActivo) {
    console.error('No se proporcionó Principio Activo para la verificación.');
    errorMessage.value = 'Error interno: No se pudo seleccionar el medicamento.';
    return;
  }
  
  console.log(`Navegando a VerificarCompra para medicamento: ${medicinePrincipioActivo}, receta ID: ${recipeId}`);
  
  // Primero debemos obtener el ID del medicamento basado en el principio activo
  const baseApiUrl = ApiService.getPharmacyApiUrl(`/medicines/search?activeMedicament=${encodeURIComponent(medicinePrincipioActivo)}`)
  
  axios.get(baseApiUrl)
    .then(response => {
      if (response.data && response.data.length > 0) {
        const medicamento = response.data[0];
        console.log('Medicamento encontrado para compra:', medicamento);
        
        // Ahora redirigimos usando el ID real del medicamento
        router.push({
          name: 'VerificarCompra',
          params: { id: medicamento.idMedicine.toString() },
          query: { recipeId: recipeId }
        });
      } else {
        console.error('No se encontró el medicamento en la base de datos:', medicinePrincipioActivo);
        errorMessage.value = `No se encontró el medicamento "${medicinePrincipioActivo}" en la base de datos.`;
      }
    })
    .catch(error => {
      console.error('Error al buscar el medicamento:', error);
      errorMessage.value = 'Error al conectar con el servidor. Inténtelo más tarde.';
    });
}

const fetchPrescriptions = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  
  try {
    // Obtener el email del usuario del localStorage
    let userEmail = ''; // Valor por defecto
    try {
      const userData = JSON.parse(localStorage.getItem('user'));
      if (userData && userData.email) {
        userEmail = userData.email;
        console.log("Email de usuario obtenido:", userEmail);
      } else {
        // Intentar obtener desde session
        const sessionData = JSON.parse(localStorage.getItem('session'));
        if (sessionData && sessionData.email) {
          userEmail = sessionData.email;
          console.log("Email de usuario obtenido de session:", userEmail);
        } else {
          console.warn("No se encontró el email de usuario en localStorage");
          // Fallback a username si no hay email
          if (userData && userData.name) {
            userEmail = userData.name;
            console.log("Usando nombre de usuario en lugar de email:", userEmail);
          } else if (sessionData && sessionData.name) {
            userEmail = sessionData.name;
            console.log("Usando nombre de usuario de session en lugar de email:", userEmail);
          } else {
            userEmail = 'rrrivera@unis.edu.gt'; // Valor por defecto si no hay información
          }
        }
      }
    } catch (err) {
      console.error("Error al obtener el usuario del localStorage:", err);
      userEmail = 'rrrivera@unis.edu.gt'; // Valor por defecto en caso de error
    }

    // Usando la URL específica proporcionada
    // Corrigiendo la URL (quitando un slash)
    const baseUrl = ApiService.getPharmacyApiUrl("/recipes/email/");
    const url = `${baseUrl}${userEmail}`;
    console.log(`Consultando recetas con URL dinámica: ${url}`);
    
    const response = await axios.get(url, {
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    });
    
    console.log("Respuesta recibida:", response);
    
    // Verificar si la respuesta es HTML (texto que comienza con <!DOCTYPE o <html)
    if (typeof response.data === 'string' && 
        (response.data.trim().startsWith('<!DOCTYPE') || 
         response.data.trim().startsWith('<html'))) {
      console.error("Se recibió HTML en lugar de JSON:", response.data.substring(0, 200) + '...');
      recipes.value = [];
      errorMessage.value = 'El servidor respondió con HTML en lugar de datos JSON. Verifique la conexión.';
      return;
    }
    
    // Manejo flexible de diferentes formatos de respuesta
    if (response.data) {
      let recetasEncontradas = false;
      
      // Verificamos si existe el objeto patient (formato nuevo)
      if (response.data.patient && Array.isArray(response.data.recipes)) {
        recipes.value = response.data.recipes;
        patientInfo.value = response.data.patient;
        recetasEncontradas = true;
        console.log("Formato 1: Objeto con patient y recipes array");
      } 
      // Verificamos si recipes existe directamente en la respuesta como array
      else if (Array.isArray(response.data.recipes)) {
        recipes.value = response.data.recipes;
        recetasEncontradas = true;
        console.log("Formato 2: Objeto con recipes array");
      } 
      // En caso de que la API devuelva un array directamente
      else if (Array.isArray(response.data)) {
        recipes.value = response.data;
        recetasEncontradas = true;
        console.log("Formato 3: Array directo");
      } 
      // Si la API devuelve un objeto que es la receta directamente
      else if (response.data._id && response.data.medicines) {
        recipes.value = [response.data];
        recetasEncontradas = true;
        console.log("Formato 4: Objeto de receta única");
      }
      // Si no reconocemos el formato, pero hay un objeto receta
      else if (typeof response.data === 'object') {
        // Intentar extraer cualquier array que parezca contener recetas
        const possibleRecipesArrays = Object.values(response.data).filter(val => 
          Array.isArray(val) && val.length > 0 && val[0]._id && val[0].medicines
        );
        
        if (possibleRecipesArrays.length > 0) {
          recipes.value = possibleRecipesArrays[0];
          recetasEncontradas = true;
          console.log("Formato 5: Extraído array de recetas de objeto", possibleRecipesArrays[0]);
        } else {
          // Último intento: buscar propiedades que parezcan recetas individuales
          const possibleRecipes = Object.values(response.data).filter(val => 
            val && typeof val === 'object' && val._id && val.medicines
          );
          
          if (possibleRecipes.length > 0) {
            recipes.value = possibleRecipes;
            recetasEncontradas = true;
            console.log("Formato 6: Extraídas recetas individuales", possibleRecipes);
          }
        }
      }
      
      console.log("RECETAS PROCESADAS:", recipes.value);
      
      if (!recetasEncontradas) {
        console.warn("Formato de respuesta no reconocido:", response.data);
        recipes.value = [];
        errorMessage.value = 'Formato de respuesta no reconocido. Consulte la consola para más detalles.';
      } else if (recipes.value.length === 0) {
        errorMessage.value = 'No se encontraron recetas para este usuario.';
      }
    } else {
      console.warn("La respuesta del API está vacía");
      recipes.value = [];
      errorMessage.value = 'No se encontraron recetas para este usuario.';
    }
  } catch (error) {
    console.error("Error al obtener las recetas:", error);
    recipes.value = [];
    errorMessage.value = `Error al obtener las recetas: ${error.message}. Por favor, inténtelo de nuevo.`;
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchPrescriptions();
});
</script>

<style scoped>
.prescriptions-container {
  padding: 2rem;
  background-color: #f8f9fa;
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.header-section {
  margin-bottom: 2rem;
  text-align: center;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  color: #6c757d;
  font-size: 1.1rem;
}

.error-message {
  background-color: #fff3f3;
  border-left: 4px solid #f44336;
  padding: 1rem 1.5rem;
  margin-bottom: 1.5rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 0.8rem;
  color: #d32f2f;
}

.error-message i {
  font-size: 1.5rem;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
}

.loading-spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-left-color: #3498db;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.prescriptions-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.prescription-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.prescription-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}

.prescription-header {
  background-color: #3498db;
  color: white;
  padding: 1.2rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.patient-info {
  display: flex;
  flex-direction: column;
}

.patient-name {
  font-size: 1.2rem;
  font-weight: 600;
}

.patient-email {
  font-size: 0.9rem;
  opacity: 0.9;
}

.recipe-date {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.prescription-body {
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid #eee;
}

.recipe-details {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.detail-label {
  font-weight: 600;
  color: #555;
  min-width: 100px;
}

.detail-value {
  color: #333;
}

.detail-value.diagnostic {
  background-color: #f0f7ff;
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
  font-weight: 500;
}

.insurance-badge {
  background-color: #e8f5e9;
  color: #388e3c;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.medicines-section {
  padding: 1.5rem;
}

.medicines-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.5rem;
}

.medicines-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.medicine-card {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 1.2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-left: 4px solid #3498db;
}

.medicine-info {
  flex: 1;
}

.medicine-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 0.3rem 0;
}

.medicine-details {
  display: flex;
  gap: 1rem;
  font-size: 0.9rem;
  color: #6c757d;
}

.medicine-concentration, .medicine-presentation {
  background-color: #e9ecef;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
}

.dosage-info {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.dosage-item {
  display: flex;
  flex-direction: column;
  min-width: 120px;
}

.dosage-label {
  font-size: 0.85rem;
  color: #6c757d;
}

.dosage-value {
  font-weight: 600;
  color: #333;
}

.buy-button {
  background-color: #4caf50;
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  border: none;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: background-color 0.2s;
}

.buy-button:hover {
  background-color: #3d8b40;
}

.empty-state {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  padding: 3rem;
  text-align: center;
  margin-top: 2rem;
}

.empty-icon {
  font-size: 4rem;
  color: #d1d8e0;
  margin-bottom: 1rem;
}

.empty-state h3 {
  font-size: 1.4rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #6c757d;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .prescriptions-container {
    padding: 1rem;
  }
  
  .medicine-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .dosage-info {
    flex-direction: column;
    gap: 0.5rem;
    margin: 0.5rem 0;
  }
  
  .prescription-header, .prescription-body {
    flex-direction: column;
    gap: 0.8rem;
  }
  
  .buy-button {
    width: 100%;
    justify-content: center;
  }
}
</style>