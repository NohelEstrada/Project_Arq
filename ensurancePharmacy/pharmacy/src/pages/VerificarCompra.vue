<template>
  <div class="purchase-container">
    <div class="purchase-header">
      <h1 class="purchase-title">Verificación de Compra</h1>
      <p class="purchase-subtitle">Revisando disponibilidad y requisitos</p>
    </div>

    <!-- Datos del medicamento -->
    <div v-if="isLoading" class="loading-section">
      <div class="loading-spinner"></div>
      <p>Verificando información...</p>
    </div>

    <div v-else-if="medicine" class="medicine-details">
      <div class="medicine-header">
        <h2>{{ medicine.name }}</h2>
        <span class="medicine-badge" v-if="medicine.prescription">Requiere receta médica</span>
      </div>

      <div class="medicine-data">
        <div class="data-grid">
          <div class="data-item">
            <span class="label">Principio Activo:</span>
            <span class="value">{{ medicine.activeMedicament }}</span>
          </div>
          <div class="data-item">
            <span class="label">Presentación:</span>
            <span class="value">{{ medicine.presentacion }}</span>
          </div>
          <div class="data-item">
            <span class="label">Precio Unitario:</span>
            <span class="value price">Q{{ medicine.price.toFixed(2) }}</span>
          </div>
          <div class="data-item">
            <span class="label">Cantidad (paquetes):</span>
            <span class="value">{{ quantity }}</span>
          </div>
          <div class="data-item">
            <span class="label">Total:</span>
            <span class="value total">Q{{ (medicine.price * quantity).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- Verificación de stock -->
      <div class="verification-card stock-card" :class="{ 'status-success': hasStock, 'status-error': !hasStock }">
        <div class="card-icon">{{ hasStock ? '✓' : '✗' }}</div>
        <div class="card-content">
          <h3>Verificación de Stock</h3>
          <p v-if="hasStock">
            Hay suficiente stock disponible ({{ medicine.stock }} unidades)
          </p>
          <p v-else>
            No hay suficiente stock disponible. Solo quedan {{ medicine.stock }} unidades.
          </p>
        </div>
      </div>

      <!-- Verificación de receta médica -->
      <div v-if="medicine.prescription" class="prescription-section">
        <h3>Recetas Médicas Disponibles</h3>
         <div class="prescription-container">
            <!-- Lista de recetas a la izquierda -->
            <div class="prescription-list">
              <div v-if="isPrescriptionPending" class="loading-recipes">
                <div class="loading-spinner-small"></div>
                <p>Cargando recetas...</p>
              </div>
              
              <div v-else-if="!userRecipes || userRecipes.length === 0" class="no-recipes">
                <p>No se encontraron recetas disponibles</p>
                <button @click="recargarRecetas" class="reload-button">
                  <span class="reload-icon">⟳</span> Recargar recetas
                </button>
                <div class="prescription-input">
                  <label for="prescription">Adjuntar nueva receta:</label>
                  <input type="file" id="prescription" @change="handlePrescriptionUpload" />
                  <button @click="verifyPrescription" class="verify-button">Verificar receta</button>
                </div>
              </div>
              
              <div v-else>
                <div class="recipe-actions top">
                  <button @click="recargarRecetas" class="reload-button small">
                    <span class="reload-icon">⟳</span> Actualizar
                  </button>
                </div>
                <div 
                  v-for="recipe in userRecipes" 
                  :key="recipe._id" 
                  class="recipe-item"
                  :class="{ 'active': selectedRecipe && selectedRecipe._id === recipe._id }"
                  @click="forceSelectRecipe(recipe)"
                >
                  <div class="recipe-header">
                    <span class="recipe-code">{{ recipe.formatted_code }}</span>
                    <span class="recipe-date">{{ recipe.formatted_date }}</span>
                  </div>
                  <div class="recipe-doctor">Dr. {{ recipe.doctor }}</div>
                </div>
              </div>
            </div>
            
            <!-- Detalle de la receta a la derecha -->
            <div class="prescription-detail">
              <div v-if="selectedRecipe" class="recipe-detail-content">
                <h4>Detalle de la Receta</h4>
                <div class="recipe-info">
                  <p><strong>Código:</strong> {{ selectedRecipe.formatted_code }}</p>
                  <p><strong>Fecha:</strong> {{ selectedRecipe.formatted_date }}</p>
                  <p><strong>Doctor:</strong> {{ selectedRecipe.doctor }}</p>
                  <p v-if="selectedRecipe.special_notes"><strong>Notas:</strong> {{ selectedRecipe.special_notes }}</p>
                </div>
                
                <h4>Medicamentos recetados:</h4>
                <div class="medicine-list">
                  <div v-for="(med, index) in selectedRecipe.medicines" :key="index" class="medicine-item"
                       :class="{'matching-medicine': medicine && med.principioActivo === medicine.activeMedicament}">
                    <p><strong>{{ med.principioActivo }}</strong> {{ med.concentracion }}</p>
                    <p v-if="med.presentacion">Presentación: {{ med.presentacion }}</p>
                    <p>Dosis: {{ med.dosis }} unidades | Frecuencia: {{ med.frecuencia }} veces al día | Duración: {{ med.duracion }} días</p>
                    <p v-if="med.diagnostico">Diagnóstico: {{ med.diagnostico }}</p>
                    <div v-if="medicine && med.principioActivo === medicine.activeMedicament" class="calculation-details">
                      <p class="calculation-text">
                        <strong>Cálculo:</strong> 
                        {{ med.dosis }} unidades × 
                        {{ med.frecuencia }} veces/día × 
                        {{ med.duracion }} días = 
                        <strong>{{ parseInt(med.dosis) * parseInt(med.frecuencia) * parseInt(med.duracion) }} unidades totales</strong>
                      </p>
                      <p class="calculation-text">
                        <strong>Paquetes necesarios:</strong> 
                        ⌈{{ parseInt(med.dosis) * parseInt(med.frecuencia) * parseInt(med.duracion) }} 
                        ÷ {{ medicine.presentacion }}⌉ = 
                        <strong>{{ Math.ceil((parseInt(med.dosis) * parseInt(med.frecuencia) * parseInt(med.duracion)) / medicine.presentacion) }} paquete(s)</strong>
                      </p>
                    </div>
                  </div>
                </div>
                
                <div class="recipe-actions">
                  <button 
                    @click="useSelectedRecipe" 
                    class="use-recipe-button"
                    :disabled="!canUseSelectedRecipe"
                  >
                    Usar esta receta
                  </button>
                </div>
              </div>
              
              <div v-else class="no-recipe-selected">
                <p>Seleccione una receta para ver detalles</p>
              </div>
            </div>
          </div>
      </div>

      <!-- Verificación de seguro -->
      <div v-if="userStore.user" class="verification-card insurance-card" :class="{ 'status-success': hasInsurance, 'status-neutral': !hasInsurance }">
        <div class="card-icon">{{ hasInsurance ? '✓' : 'ⓘ' }}</div>
        <div class="card-content">
          <h3>Seguro Médico</h3>
          <p v-if="hasInsurance">
            Su seguro médico cubrirá {{ coveragePercentage }}% del costo
          </p>
          <p v-else>
            No se detectó seguro médico. Pagará el precio completo.
          </p>
        </div>
      </div>
      
      <!-- Formulario de Tarjeta de Crédito -->
      <div class="payment-section">
        <h3>Información de Pago</h3>
        <div class="card-form">
          <div class="form-group">
            <label for="card-name">Titular de la Tarjeta</label>
            <input 
              type="text" 
              id="card-name" 
              v-model="cardName" 
              placeholder="Nombre como aparece en la tarjeta"
              @input="validateAllCardFields"
            />
          </div>
          
          <div class="form-group">
            <label for="card-number">Número de Tarjeta</label>
            <div class="card-number-wrapper">
              <input 
                type="text" 
                id="card-number" 
                v-model="cardNumber" 
                placeholder="1234 5678 9012 3456"
                @input="formatCardNumber"
                maxlength="19"
              />
              <div class="card-type">
                <i class="fas fa-credit-card"></i>
              </div>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group col">
              <label for="card-expiry">Fecha de Expiración</label>
              <input 
                type="text" 
                id="card-expiry" 
                v-model="cardExpiry" 
                placeholder="MM/AA" 
                @input="formatCardExpiry"
                maxlength="5"
              />
            </div>
            
            <div class="form-group col">
              <label for="card-cvc">CVC</label>
              <input 
                type="text" 
                id="card-cvc" 
                v-model="cardCVC" 
                placeholder="123" 
                @input="validateAllCardFields"
                maxlength="4"
              />
            </div>
          </div>
          
          <div class="card-validation" v-if="cardNumber">
            <div class="validation-indicator" :class="{'valid': validateCardNumber()}">
              <i :class="validateCardNumber() ? 'fas fa-check' : 'fas fa-times'"></i>
              <span>Número de tarjeta {{ validateCardNumber() ? 'válido' : 'inválido' }}</span>
            </div>
            <div v-if="cardExpiry" class="validation-indicator" :class="{'valid': validateCardExpiry()}">
              <i :class="validateCardExpiry() ? 'fas fa-check' : 'fas fa-times'"></i>
              <span>Fecha de expiración {{ validateCardExpiry() ? 'válida' : 'inválida' }}</span>
            </div>
            <div v-if="cardCVC" class="validation-indicator" :class="{'valid': validateCardCVC()}">
              <i :class="validateCardCVC() ? 'fas fa-check' : 'fas fa-times'"></i>
              <span>CVC {{ validateCardCVC() ? 'válido' : 'inválido' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Resumen de pago -->
      <div class="payment-summary">
        <h3>Resumen de Pago</h3>
        <div class="summary-item">
          <span>Subtotal:</span>
          <span>Q{{ (medicine.price * quantity).toFixed(2) }}</span>
        </div>
        <div v-if="hasInsurance" class="summary-item discount">
          <span>Cobertura del seguro ({{ coveragePercentage }}%):</span>
          <span>-Q{{ ((medicine.price * quantity) * (coveragePercentage / 100)).toFixed(2) }}</span>
        </div>
        <div class="summary-item total">
          <span>Total a pagar:</span>
          <span>Q{{ calculateTotalToPay().toFixed(2) }}</span>
        </div>
      </div>

      <!-- Botones de acción -->
      <div class="action-buttons">
        <button @click="goBack" class="cancel-button">Cancelar</button>
        <button @click="viewDetail" class="detail-button">Ver Detalle</button>
        <button 
          @click="confirmPurchase" 
          class="confirm-button"
          :disabled="!canConfirmPurchase" 
        >
          Confirmar Compra
        </button>
      </div>
    </div>

    <div v-else class="error-container">
      <h2>Producto no encontrado</h2>
      <p>No se pudo encontrar el medicamento solicitado.</p>
      <button @click="goBack" class="back-button">Volver al catálogo</button>
    </div>

    <!-- Modal de detalle de compra -->
    <div v-if="showDetailModal" class="modal-overlay">
      <div class="modal-content detail-modal">
          <div class="modal-header">
            <h3>Detalle de la Compra</h3>
            <button @click="closeDetailModal" class="close-icon">&times;</button>
          </div>
          
          <div class="detail-content" v-if="medicine"> 
            <div class="detail-section">
              <h4>Medicamento</h4>
              <div class="detail-info">
                <p><strong>Nombre:</strong> {{ medicine.name }}</p>
                <p><strong>Principio Activo:</strong> {{ medicine.activeMedicament }}</p>
                <p><strong>Presentación:</strong> {{ medicine.presentacion }}</p>
                <p><strong>Precio Unitario:</strong> Q{{ medicine.price.toFixed(2) }}</p>
              </div>
            </div>
            
            <div class="detail-section">
              <h4>Detalles de la Compra</h4>
              <div class="detail-info">
                <p><strong>Cantidad (paquetes):</strong> {{ quantity }}</p>
                <p><strong>Subtotal:</strong> Q{{ (medicine.price * quantity).toFixed(2) }}</p>
                <p v-if="hasInsurance"><strong>Descuento Seguro:</strong> Q{{ ((medicine.price * quantity) * (coveragePercentage / 100)).toFixed(2) }}</p>
                <p><strong>Total a Pagar:</strong> Q{{ calculateTotalToPay().toFixed(2) }}</p>
              </div>
            </div>
            
            <div v-if="selectedRecipe" class="detail-section">
              <h4>Receta Médica Utilizada</h4>
              <div class="detail-info">
                <p><strong>Código:</strong> {{ selectedRecipe.formatted_code }}</p>
                <p><strong>Fecha:</strong> {{ selectedRecipe.formatted_date }}</p>
                <p><strong>Doctor:</strong> {{ selectedRecipe.doctor }}</p>
                <div v-if="medicineMatch" class="calculation-details">
                  <p class="calculation-text">
                    <strong>Cálculo:</strong> 
                    {{ medicineMatch.dosis }} unidades × 
                    {{ medicineMatch.frecuencia }} veces/día × 
                    {{ medicineMatch.duracion }} días = 
                    <strong>{{ parseInt(medicineMatch.dosis) * parseInt(medicineMatch.frecuencia) * parseInt(medicineMatch.duracion) }} unidades totales</strong>
                  </p>
                  <p class="calculation-text">
                    <strong>Paquetes necesarios:</strong> 
                    ⌈{{ parseInt(medicineMatch.dosis) * parseInt(medicineMatch.frecuencia) * parseInt(medicineMatch.duracion) }} 
                    ÷ {{ medicine.presentacion }}⌉ = 
                    <strong>{{ Math.ceil((parseInt(medicineMatch.dosis) * parseInt(medicineMatch.frecuencia) * parseInt(medicineMatch.duracion)) / medicine.presentacion) }} paquete(s)</strong>
                  </p>
                </div>
              </div>
            </div>
             <div v-else-if="medicine.prescription" class="detail-section">
                <h4>Receta Médica</h4>
                 <div class="detail-info">
                    <p class="status-warning">Se requiere receta, pero no se ha seleccionado ninguna.</p>
                 </div>
             </div>
            
            <div class="detail-section">
              <h4>Estado de la Compra</h4>
              <div class="detail-info">
                <p><strong>Stock disponible:</strong> <span :class="hasStock ? 'status-ok' : 'status-error'">{{ hasStock ? 'Sí' : 'No' }}</span></p>
                <p v-if="medicine.prescription"><strong>Receta médica:</strong> <span :class="hasPrescription ? 'status-ok' : 'status-pending'">{{ hasPrescription ? 'Validada' : 'Pendiente/No seleccionada' }}</span></p>
                <p><strong>Seguro médico:</strong> <span :class="hasInsurance ? 'status-ok' : 'status-info'">{{ hasInsurance ? `Cobertura del ${coveragePercentage}%` : 'No disponible' }}</span></p>
              </div>
            </div>
          </div>
           <div v-else class="detail-content">
                <p>Cargando detalles...</p>
           </div>
          
          <div class="modal-actions">
            <button @click="closeDetailModal" class="cancel-button">Cerrar</button>
            <button 
              @click="confirmPurchase" 
              class="confirm-button"
              :disabled="!canConfirmPurchase"
            >
              Confirmar Compra
            </button>
          </div>
        </div>
    </div>

    <!-- Modal de confirmación -->
    <div v-if="showConfirmationModal" class="modal-overlay">
      <div class="modal-content">
        <h3>¡Compra Exitosa!</h3>
        <p>Su compra ha sido procesada correctamente.</p>
        <div class="confirmation-details">
          <p><strong>Medicamento:</strong> {{ medicine?.name }}</p>
          <p><strong>Cantidad:</strong> {{ quantity }}</p>
          <p><strong>Total pagado:</strong> Q{{ calculateTotalToPay().toFixed(2) }}</p>
        </div>
        <button @click="finishPurchase" class="finish-button">Continuar</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/userStore';
import axios from 'axios';
import ApiService from '../services/ApiService';  
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// Estados
const medicine = ref(null);
const quantity = ref(1);
const isLoading = ref(true);
const hasStock = ref(false);
const hasPrescription = ref(false);
const isPrescriptionPending = ref(false);
const hasInsurance = ref(false);
const coveragePercentage = ref(0);
const showConfirmationModal = ref(false);
const userRecipes = ref([]);
const selectedRecipe = ref(null);
const showDetailModal = ref(false);
const medicineMatch = ref(null);

// Nuevos estados para tarjeta y seguro
const cardNumber = ref('');
const cardName = ref('');
const cardExpiry = ref('');
const cardCVC = ref('');
const isCardValid = ref(false);
const isCheckingInsurance = ref(false);
const insuranceDetails = ref(null);

// Verificar si el usuario puede realizar la compra
const canConfirmPurchase = computed(() => {
  return hasStock.value && isCardValid.value;
});

// Verificar si se puede usar la receta seleccionada
const canUseSelectedRecipe = computed(() => {
  if (!selectedRecipe.value || !medicine.value) return false;
  
  // Verificar si la receta contiene el medicamento actual
  const matchFound = selectedRecipe.value.medicines.find(med => 
    med.principioActivo === medicine.value.activeMedicament
  );
  
  return !!matchFound;
});

// Verificar stock disponible
function verificarStock() {
  if (!medicine.value) {
    console.error('No hay un medicamento para verificar stock');
    hasStock.value = false;
    return false;
  }
  
  // Verificar si hay suficiente stock (siempre para cantidad 1)
  hasStock.value = medicine.value.stock >= 1;
  
  console.log(`Verificación de stock: ${medicine.value.stock} disponibles, resultado: ${hasStock.value ? 'Disponible' : 'No disponible'}`);
  
  return hasStock.value;
}

// Forzar la selección de receta con cálculo visible
function forceSelectRecipe(recipe) {
  console.log('Forzando selección de receta:', recipe);
  selectedRecipe.value = recipe;
  
  if (!medicine.value) {
    console.warn('No hay medicamento cargado');
    return;
  }
  
  // Encuentra el medicamento que coincide
  const matchingMedicine = recipe.medicines.find(med => 
    med.principioActivo === medicine.value.activeMedicament
  );
  
  if (matchingMedicine) {
    medicineMatch.value = matchingMedicine;
    hasPrescription.value = true;
    
    // Calcular explícitamente
    const dosis = parseInt(matchingMedicine.dosis) || 0;
    const frecuencia = parseInt(matchingMedicine.frecuencia) || 0;
    const duracion = parseInt(matchingMedicine.duracion) || 0;
    const unidadesTotales = dosis * frecuencia * duracion;
    const paquetesNecesarios = Math.ceil(unidadesTotales / medicine.value.presentacion);
    
    // Actualizar cantidad
    quantity.value = paquetesNecesarios || 1;
    
    console.log('Resultado del cálculo:', {
      dosis,
      frecuencia, 
      duracion,
      unidadesTotales,
      unidadesPorPresentacion: medicine.value.presentacion,
      paquetesNecesarios
    });
    
    // Aplicar seguro si existe
    if (recipe.has_insurance && userStore.user && userStore.user.policy) {
      hasInsurance.value = true;
      coveragePercentage.value = 70;
    } else {
      hasInsurance.value = false;
      coveragePercentage.value = 0;
    }
  } else {
    medicineMatch.value = null;
    hasPrescription.value = false;
    quantity.value = 1;
    alert('Esta receta no contiene el medicamento seleccionado');
  }
}

// Seleccionar una receta
function selectRecipe(recipe) {
  forceSelectRecipe(recipe);
}

// Cargar recetas
async function loadRecipes() {
  isPrescriptionPending.value = true;
  try {
    if (userStore.user && userStore.user.idUser) {
      // En un entorno real, deberías obtener las recetas del usuario desde el backend
      console.log('Cargando recetas para el usuario:', userStore.user.idUser);
      
      // Simulamos una llamada a la API
      // En producción, aquí iría: const response = await axios.get(`http://${ip}:${apiPort}/api2/prescriptions/user/${userStore.user.idUser}`);
      
      // Por ahora, usamos datos de ejemplo
      setTimeout(() => {
        const recetasEjemplo = [
          {
            _id: "680f14e94d748efc0c04188b",
            patient: userStore.user.idUser || "user123",
            doctor: "67dcd3224d8c7c0ed8f0c01fe",
            formatted_code: "00256-20250427-3478",
            formatted_date: "27/04/2025 23:40:57",
            has_insurance: userStore.user.policy ? true : false,
            insurance_code: userStore.user.policy || null,
            special_notes: "2",
            created_at: "2025-04-27T23:40:57.300+00:00",
            medicines: [
              {
                _id: "med-paracetamol-500",
                principioActivo: "Paracetamol2",
                concentracion: "500 MG",
                presentacion: 30,
                dosis: "2",
                frecuencia: "1",
                duracion: "4",
                diagnostico: "2"
              }
            ]
          }
        ];
        
        userRecipes.value = recetasEjemplo;
        console.log('Recetas cargadas:', userRecipes.value);
        isPrescriptionPending.value = false;
      }, 500);
    } else {
      console.warn('No hay usuario logueado para cargar recetas');
      isPrescriptionPending.value = false;
    }
  } catch (error) {
    console.error('Error al cargar recetas:', error);
    isPrescriptionPending.value = false;
  }
}

// Manejar carga de archivo
function handlePrescriptionUpload(event) {
  const file = event.target.files[0];
  if (file) {
    console.log('Archivo de receta seleccionado:', file.name);
  } else {
    console.log('No se seleccionó ningún archivo');
  }
}

// Calcular total a pagar
function calculateTotalToPay() {
  if (!medicine.value) return 0;
  
  const subtotal = medicine.value.price * quantity.value;
  if (hasInsurance.value) {
    const discount = subtotal * (coveragePercentage.value / 100);
    return subtotal - discount;
  }
  return subtotal;
}

// Ver detalle de la compra (abre el modal)
function viewDetail() {
  showDetailModal.value = true;
}

// Cerrar modal de detalle
function closeDetailModal() {
  showDetailModal.value = false;
}

// Validar formato de tarjeta
function validateCardNumber() {
  // Implementación básica de validación (Luhn algorithm)
  const value = cardNumber.value.replace(/\s/g, '');
  
  // Verificar longitud
  if (value.length < 13 || value.length > 19) {
    return false;
  }
  
  // Verificar que sean solo dígitos
  if (!/^\d+$/.test(value)) {
    return false;
  }
  
  // Implementación del algoritmo de Luhn
  let sum = 0;
  let double = false;
  
  // Recorrer de derecha a izquierda
  for (let i = value.length - 1; i >= 0; i--) {
    let digit = parseInt(value.charAt(i));
    
    if (double) {
      digit *= 2;
      if (digit > 9) {
        digit -= 9;
      }
    }
    
    sum += digit;
    double = !double;
  }
  
  // Valid si es divisible por 10
  return sum % 10 === 0;
}

// Validar fecha de expiración
function validateCardExpiry() {
  const value = cardExpiry.value;
  if (!value || !/^\d{2}\/\d{2}$/.test(value)) {
    return false;
  }
  
  const [month, year] = value.split('/').map(num => parseInt(num, 10));
  
  // Verificar que el mes sea válido (1-12)
  if (month < 1 || month > 12) {
    return false;
  }
  
  // Obtener año actual y convertir el año de 2 dígitos a 4 dígitos
  const currentDate = new Date();
  const currentYear = currentDate.getFullYear() % 100; // últimos 2 dígitos
  const currentMonth = currentDate.getMonth() + 1; // 1-12
  
  // Convertir año de tarjeta a número de 2 dígitos para comparación
  const expYear = year;
  
  // Verificar si la tarjeta ya está vencida
  if (expYear < currentYear || (expYear === currentYear && month < currentMonth)) {
    return false;
  }
  
  return true;
}

// Validar CVC
function validateCardCVC() {
  const value = cardCVC.value;
  return /^\d{3,4}$/.test(value);
}

// Formatear número de tarjeta mientras se escribe
function formatCardNumber(e) {
  let value = e.target.value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
  
  // Limitar a 16 dígitos
  if (value.length > 16) {
    value = value.substr(0, 16);
  }
  
  // Formato 4 dígitos - 4 dígitos - 4 dígitos - 4 dígitos
  const parts = [];
  for (let i = 0; i < value.length; i += 4) {
    parts.push(value.substr(i, 4));
  }
  
  cardNumber.value = parts.join(' ');
}

// Formatear fecha de expiración mientras se escribe
function formatCardExpiry(e) {
  let value = e.target.value.replace(/[^0-9]/g, '');
  
  // Limitar a 4 dígitos
  if (value.length > 4) {
    value = value.substr(0, 4);
  }
  
  // Formato MM/YY
  if (value.length > 2) {
    cardExpiry.value = `${value.substr(0, 2)}/${value.substr(2)}`;
  } else {
    cardExpiry.value = value;
  }
}

// Verificar todos los campos de tarjeta
function validateAllCardFields() {
  isCardValid.value = 
    cardNumber.value.trim() !== '' && validateCardNumber() &&
    cardName.value.trim() !== '' &&
    cardExpiry.value.trim() !== '' && validateCardExpiry() &&
    cardCVC.value.trim() !== '' && validateCardCVC();
  
  return isCardValid.value;
}

// Verificar seguro médico del usuario
async function checkInsurance() {
  if (!userStore.user || !userStore.user.email) {
    console.warn('No hay email de usuario para verificar seguro');
    hasInsurance.value = false;
    coveragePercentage.value = 0;
    return;
  }
  
  isCheckingInsurance.value = true;
  
  try {
    const emailPaciente = userStore.user.email;
    console.log(`Verificando seguro para: ${emailPaciente}`);
    
    const insuranceResponse = await axios.get(ApiService.getInsuranceApiUrl(`/users/by-email/${encodeURIComponent(emailPaciente)}`));
    console.log('Respuesta de API de seguros:', insuranceResponse.data);
    
    if (insuranceResponse.data && insuranceResponse.data.policy) {
      // Usuario tiene seguro
      hasInsurance.value = true;
      insuranceDetails.value = insuranceResponse.data;
      
      // Verificar si cubre el medicamento actual
      if (medicine.value && medicine.value.activeMedicament) {
        // Simulación: validar que el medicamento está cubierto
        const isCovered = true; // En producción, esto vendría desde la API
        
        if (isCovered) {
          coveragePercentage.value = 70; // Porcentaje de cobertura (podría venir de la API)
          console.log(`Medicamento cubierto al ${coveragePercentage.value}%`);
        } else {
          coveragePercentage.value = 0;
          console.log('Medicamento no cubierto por el seguro');
        }
      }
    } else {
      hasInsurance.value = false;
      coveragePercentage.value = 0;
      insuranceDetails.value = null;
      console.log('Usuario no tiene seguro médico');
    }
  } catch (error) {
    console.error('Error al verificar seguro:', error);
    hasInsurance.value = false;
    coveragePercentage.value = 0;
  } finally {
    isCheckingInsurance.value = false;
  }
}

// Función para confirmar la compra
async function confirmPurchase() {
  console.log('[VerificarCompra] Iniciando confirmPurchase...');
  
  // Validar tarjeta de nuevo antes de procesar
  if (!validateAllCardFields()) {
    alert('Por favor, ingrese una tarjeta válida para continuar con la compra.');
    return;
  }
  
  try {
    isLoading.value = true;
    
    if (!medicine.value || !medicine.value.idMedicine) {
      console.error('No hay un medicamento válido para la compra');
      alert('No se pudo completar la compra: datos de medicamento incompletos');
      isLoading.value = false;
      return;
    }
    
    if (!userStore.user || !userStore.user.idUser) {
      console.error('No hay información del usuario para la compra');
      alert('Debe iniciar sesión para completar la compra');
      isLoading.value = false;
      return;
    }
    
    // Verificar stock nuevamente antes de procesar
    if (!verificarStock()) {
      alert('No hay suficiente stock para completar la compra');
      isLoading.value = false;
      return;
    }
    
    // Verificar que la cantidad solicitada no sea mayor al stock disponible
    if (quantity.value > medicine.value.stock) {
      alert(`Solo hay ${medicine.value.stock} unidades disponibles. Ajuste la cantidad.`);
      quantity.value = medicine.value.stock;
      isLoading.value = false;
      return;
    }
    
    console.log('Iniciando proceso de compra:', {
      medicineId: medicine.value.idMedicine,
      userId: userStore.user.idUser,
      quantity: quantity.value,
      recipeId: selectedRecipe.value?._id, // Incluir ID de receta si está seleccionada
      stockActual: medicine.value.stock
    });
    
    // Simulación de procesamiento de pago
    console.log('Procesando pago con tarjeta:', cardNumber.value.slice(-4));
    // En un entorno real, aquí iría la llamada a un servicio de pago
    
    // PROCESO DE COMPRA
    
    // 1. Actualizar primero el stock del medicamento para reservar el inventario
    try {
      console.log('Paso 1: Actualizando stock del medicamento...');
      const updatedMedicine = { ...medicine.value };
      const nuevoStock = updatedMedicine.stock - quantity.value;
      
      if (nuevoStock < 0) {
        throw new Error('Stock insuficiente');
      }
      
      updatedMedicine.stock = nuevoStock;
      
      await axios.put(
        ApiService.getPharmacyApiUrl(`/medicines/${medicine.value.idMedicine}`), 
        updatedMedicine
      );
      
      console.log(`Stock actualizado correctamente. Nuevo stock: ${nuevoStock}`);
      
      // 2. Crear orden de compra
      console.log('Paso 2: Creando orden de compra...');
      const orderData = {
        user: { idUser: userStore.user.idUser },
        status: 'Completado'
      };
      
      const orderResponse = await axios.post(ApiService.getPharmacyApiUrl("/orders"), orderData);
      const order = orderResponse.data;
      console.log('Orden creada:', order);
      
      // 3. Añadir medicamento a la orden
      console.log('Paso 3: Añadiendo medicamento a la orden...');
      const orderMedicineData = {
        orders: order,
        medicine: { idMedicine: medicine.value.idMedicine },
        quantity: quantity.value,
        cost: medicine.value.price,
        total: medicine.value.price * quantity.value
      };
      
      await axios.post(ApiService.getPharmacyApiUrl("/order_medicines"), orderMedicineData);
      console.log('Medicamento añadido a la orden con éxito');
      
      // 4. Si hay seguro o receta, generar registro de factura
      if (hasInsurance.value || selectedRecipe.value) {
        console.log('Paso 4: Generando factura...');
        const insuranceAmount = hasInsurance.value ? 
          (medicine.value.price * quantity.value) * (coveragePercentage.value / 100) : 0;
          
        const patientAmount = (medicine.value.price * quantity.value) - insuranceAmount;
        
        const billData = {
          prescription: selectedRecipe.value ? selectedRecipe.value._id : null,
          total: medicine.value.price * quantity.value,
          subtotal: medicine.value.price * quantity.value,
          taxes: 0,
          coveredAmount: insuranceAmount,
          patientAmount: patientAmount,
          copay: hasInsurance.value ? coveragePercentage.value : 0,
          status: 'Pagado',
          insuranceApprovalCode: hasInsurance.value ? 'AP' + Math.floor(Math.random() * 100000) : null
        };
        
        console.log('Datos de factura:', billData);
        await axios.post(ApiService.getPharmacyApiUrl("/bills"), billData);
        console.log('Factura creada con éxito');
      }
      
      console.log('Compra completada exitosamente');
      
      // Actualizamos la referencia local del medicamento para reflejar el nuevo stock
      medicine.value.stock = nuevoStock;
      
      // Cerrar cualquier modal abierto
      closeDetailModal();
      
      // Mostrar confirmación
      showConfirmationModal.value = true;
      
    } catch (stockError) {
      console.error('Error al actualizar el stock:', stockError);
      
      // Si falló la actualización del stock, intentamos revertir si ya se había actualizado
      if (stockError.message !== 'Stock insuficiente') {
        try {
          console.warn('Intentando verificar estado actual del stock...');
          const checkResponse = await axios.get(ApiService.getPharmacyApiUrl(`/medicines/${medicine.value.idMedicine}`));
          
          // Si el stock ya fue actualizado erróneamente, intentamos restaurarlo
          if (checkResponse.data.stock !== medicine.value.stock) {
            console.warn('Detectado cambio de stock, intentando restaurar...');
            await axios.put(ApiService.getPharmacyApiUrl(`/medicines/${medicine.value.idMedicine}`), medicine.value);
            console.log('Stock restaurado al valor original');
          }
        } catch (restoreError) {
          console.error('Error al restaurar stock:', restoreError);
        }
      }
      
      if (stockError.message === 'Stock insuficiente') {
        alert('No hay suficiente stock para completar la compra.');
      } else {
        alert('Error al actualizar el inventario. Inténtelo nuevamente.');
      }
    }
    
  } catch (error) {
    console.error('Error general procesando la compra:', error);
    alert('Ocurrió un error al procesar su compra. Por favor, inténtelo de nuevo.');
  } finally {
    isLoading.value = false;
  }
}

// Finalizar proceso y redirigir
function finishPurchase() {
  // Cerrar todos los modales
  showConfirmationModal.value = false;
  showDetailModal.value = false;
  
  // Redireccionar al catálogo
  router.push('/catalogo');
}

// Volver atrás
function goBack() {
  router.back();
}

// Usar la receta seleccionada (marcarla como usada)
function useSelectedRecipe() {
  if (selectedRecipe.value && canUseSelectedRecipe.value) {
    hasPrescription.value = true; // Ya debería ser true por selectRecipe
    console.log('Usando receta:', selectedRecipe.value._id);
    
    // Mostrar confirmación visual (si es necesario)
    // ... (código para cambiar estilo del botón si se desea)
  } else {
    alert('Esta receta no es válida para el medicamento actual.');
  }
}

// Simular verificación de receta subida
function verifyPrescription() {
  const fileInput = document.getElementById('prescription');
  if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
    alert('Por favor, adjunte una receta médica.');
    return;
  }
  
  isPrescriptionPending.value = true;
  console.log('Simulando verificación de receta subida...');
  
  setTimeout(() => {
    // Generar cantidades aleatorias pero realistas para la receta
    const dosis = Math.floor(Math.random() * 2) + 1; // 1 o 2 unidades por dosis
    const frecuencia = Math.floor(Math.random() * 3) + 1; // 1 a 3 veces al día
    const duracion = Math.floor(Math.random() * 7) + 3; // 3 a 10 días
    
    const nuevaReceta = {
      _id: "receta-" + Math.floor(Math.random() * 10000),
      patient: userStore.user?.idUser || "usuario-actual",
      doctor: "Doctor Subido",
      formatted_code: "SUBIDA-" + new Date().toISOString().substring(0, 10),
      formatted_date: new Date().toLocaleDateString(),
      has_insurance: false,
      special_notes: "Receta subida manualmente",
      medicines: [
        {
          _id: "med-" + Math.floor(Math.random() * 10000),
          principioActivo: medicine.value?.activeMedicament || 'Desconocido',
          concentracion: medicine.value?.concentration || "N/A",
          presentacion: medicine.value?.presentacion || 'N/A',
          dosis: dosis.toString(),
          frecuencia: frecuencia.toString(),
          duracion: duracion.toString(),
          diagnostico: "Subido manualmente"
        }
      ]
    };
    
    userRecipes.value.unshift(nuevaReceta);
    forceSelectRecipe(nuevaReceta); // Seleccionar y validar la nueva receta
    isPrescriptionPending.value = false;
    console.log('Receta subida y verificada (simulado) con cantidades:', {
      dosis,
      frecuencia,
      duracion,
      totalUnidades: dosis * frecuencia * duracion
    });
  }, 1500);
}

// Recargar recetas (simulación)
function recargarRecetas() {
  isPrescriptionPending.value = true;
  console.log('Recargando recetas...');
  setTimeout(() => {
    loadRecipes();
    isPrescriptionPending.value = false;
  }, 1000);
}

// Cargar datos del medicamento
onMounted(async () => {
  isLoading.value = true;
  try {
    const medicineId = route.params.id;
    console.log('Buscando medicamento con ID:', medicineId);
    
    // Modificamos la URL para buscar directamente por ID en lugar de principio activo
    const searchUrl = ApiService.getPharmacyApiUrl(`/medicines/${medicineId}`);
    console.log('URL de búsqueda directa:', searchUrl);
    
    const response = await axios.get(searchUrl);
    console.log('Respuesta de búsqueda:', response.data);
    
    // La respuesta debería ser el objeto medicina directamente, no un array
    if (response.data && response.data.idMedicine) {
      medicine.value = response.data;
      console.log('Medicamento encontrado:', medicine.value);
      
      // Verificar stock
      verificarStock();
      
      // Verificar seguro médico
      await checkInsurance();
      
      // Cargar recetas
      await loadRecipes();
      
      // Si hay receta, seleccionar
      if (route.query.recipeId) {
        const recipeToSelect = userRecipes.value.find(r => r._id === route.query.recipeId);
        if (recipeToSelect) {
          console.log('Seleccionando receta inicial:', recipeToSelect);
          selectRecipe(recipeToSelect);
        }
      }
    } else {
      // Si no encuentra por ID directo, intentamos buscar por principio activo como fallback
      try {
        const fallbackUrl = ApiService.getPharmacyApiUrl(`/medicines/search?activeMedicament=${medicineId}`);
        console.log('Intentando URL alternativa:', fallbackUrl);
        
        const fallbackResponse = await axios.get(fallbackUrl);
        
        if (fallbackResponse.data && fallbackResponse.data.length > 0) {
          medicine.value = fallbackResponse.data[0];
          console.log('Medicamento encontrado con búsqueda alternativa:', medicine.value);
          verificarStock();
          await checkInsurance();
          await loadRecipes();
        } else {
          console.warn('No se encontró el medicamento con ningún método');
          medicine.value = null;
        }
      } catch (fallbackError) {
        console.error('Error en búsqueda alternativa:', fallbackError);
        medicine.value = null;
      }
    }
  } catch (error) {
    console.error('Error al buscar el medicamento:', error);
    // Intentar con otro método si el primero falla
    try {
      const fallbackUrl = ApiService.getPharmacyApiUrl(`/medicines/search?query=${route.params.id}`);
      console.log('Intentando URL de respaldo general:', fallbackUrl);
      
      const generalSearchResponse = await axios.get(fallbackUrl);
      
      if (generalSearchResponse.data && generalSearchResponse.data.length > 0) {
        medicine.value = generalSearchResponse.data[0];
        console.log('Medicamento encontrado con búsqueda general:', medicine.value);
        verificarStock();
        await checkInsurance();
        await loadRecipes();
      } else {
        medicine.value = null;
      }
    } catch (backupError) {
      console.error('Error en la búsqueda de respaldo:', backupError);
      medicine.value = null;
    }
  } finally {
    isLoading.value = false;
  }
});

</script>

<style scoped>
.purchase-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #f8fafc;
}

.purchase-header {
  text-align: center;
  margin-bottom: 2rem;
}

.purchase-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1e40af;
  margin-bottom: 0.5rem;
}

.purchase-subtitle {
  font-size: 1.1rem;
  color: #64748b;
}

.loading-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #1e40af;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.medicine-details {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  margin-bottom: 2rem;
}

.medicine-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.medicine-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.medicine-badge {
  background-color: #ef4444;
  color: white;
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
}

.medicine-data {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.data-item {
  display: flex;
  flex-direction: column;
}

.data-item .label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 0.3rem;
}

.data-item .value {
  font-size: 1.1rem;
  color: #1e293b;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.quantity-btn {
  width: 25px;
  height: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e2e8f0;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
}

.quantity-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.data-item .price,
.data-item .total {
  font-weight: 700;
  color: #16a34a;
}

.verification-card {
  display: flex;
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  gap: 1rem;
}

.card-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  font-weight: bold;
  flex-shrink: 0;
}

.status-success .card-icon {
  background-color: #dcfce7;
  color: #16a34a;
}

.status-error .card-icon {
  background-color: #fee2e2;
  color: #dc2626;
}

.status-pending .card-icon {
  background-color: #fef3c7;
  color: #d97706;
}

.status-neutral .card-icon {
  background-color: #e0f2fe;
  color: #0284c7;
}

.card-content {
  flex: 1;
}

.card-content h3 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
}

/* Sección de recetas */
.prescription-section {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.prescription-section h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
}

.prescription-container {
  display: flex;
  gap: 1.5rem;
  min-height: 300px;
}

.prescription-list {
  flex: 1;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f8fafc;
}

.loading-recipes {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 150px;
}

.loading-spinner-small {
  width: 20px;
  height: 20px;
  border: 3px solid #e2e8f0;
  border-top: 3px solid #1e40af;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 0.5rem;
}

.no-recipes {
  padding: 1.5rem;
  text-align: center;
}

.reload-button {
  background-color: #3b82f6;
  color: white;
  border: none;
  padding: 0.6rem 1rem;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.3rem;
  margin: 0.75rem auto;
}

.reload-button.small {
  padding: 0.3rem 0.6rem;
  font-size: 0.8rem;
}

.reload-icon {
  display: inline-block;
  font-size: 1.2rem;
}

.reload-button:hover {
  background-color: #2563eb;
}

.recipe-item {
  padding: 1rem;
  border-bottom: 1px solid #e2e8f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.recipe-item:hover {
  background-color: #e0f2fe;
}

.recipe-item.active {
  background-color: #bfdbfe;
}

.recipe-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.recipe-code {
  font-weight: 600;
  font-size: 0.9rem;
}

.recipe-date {
  font-size: 0.8rem;
  color: #64748b;
}

.recipe-doctor {
  font-size: 0.9rem;
  color: #0f172a;
}

.prescription-detail {
  flex: 1.5;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background-color: white;
  padding: 1.5rem;
}

.no-recipe-selected {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #64748b;
  font-style: italic;
}

.recipe-detail-content h4 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
  color: #1e40af;
}

.recipe-info {
  background-color: #f8fafc;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.recipe-info p {
  margin: 0.5rem 0;
}

.medicine-list {
  margin-top: 1rem;
}

.medicine-item {
  padding: 1rem;
  background-color: #f8fafc;
  border-radius: 8px;
  margin-bottom: 0.75rem;
}

.medicine-item.matching-medicine {
  background-color: #e0f2fe;
  border: 1px solid #60a5fa;
}

.medicine-item p {
  margin: 0.3rem 0;
}

.calculation-details {
  margin-top: 0.8rem;
  padding: 0.8rem;
  background-color: #f0f9ff;
  border-radius: 6px;
  border-left: 3px solid #3b82f6;
}

.calculation-text {
  font-size: 0.9rem;
  margin: 0.5rem 0;
  color: #334155;
  line-height: 1.5;
}

.recipe-actions {
  margin-top: 1.5rem;
  display: flex;
  justify-content: flex-end;
}

.recipe-actions.top {
  margin-bottom: 0.5rem;
  padding: 0.5rem;
  display: flex;
  justify-content: flex-end;
}

.use-recipe-button {
  background-color: #1e40af;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}

.use-recipe-button:disabled {
  background-color: #94a3b8;
  cursor: not-allowed;
}

.prescription-input {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.verify-button {
  background-color: #0284c7;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  align-self: flex-start;
}

.payment-summary {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.payment-summary h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
}

.summary-item.discount {
  color: #16a34a;
}

.summary-item.total {
  font-weight: 700;
  font-size: 1.2rem;
  padding-top: 1rem;
  margin-top: 0.5rem;
  border-top: 1px solid #e2e8f0;
}

.action-buttons {
  padding: 1.5rem;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.cancel-button,
.confirm-button,
.back-button,
.finish-button,
.detail-button {
  padding: 0.8rem 1.5rem;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.cancel-button,
.back-button {
  background-color: #f1f5f9;
  color: #334155;
}

.confirm-button,
.finish-button {
  background-color: #1e40af;
  color: white;
}

.detail-button {
  background-color: #0284c7;
  color: white;
}

.confirm-button:disabled {
  background-color: #94a3b8;
  cursor: not-allowed;
}

.error-container {
  text-align: center;
  padding: 3rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background-color: white;
  border-radius: 12px;
  padding: 0; /* Ajustado para header/content/actions */
  width: 90%;
  max-width: 600px; /* Aumentado para más espacio */
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden; /* Para border-radius */
}

.detail-modal .modal-header {
  padding: 1rem 1.5rem;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-modal h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
}

.close-icon {
  background: none;
  border: none;
  font-size: 1.75rem;
  cursor: pointer;
  color: #64748b;
  line-height: 1;
}

.detail-modal .detail-content {
  padding: 1.5rem;
  text-align: left;
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 1.5rem;
}

.detail-section h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #1e40af;
  margin: 0 0 0.75rem 0;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 0.5rem;
}

.detail-info {
  background-color: #f8fafc;
  padding: 0.75rem 1rem;
  border-radius: 8px;
}

.detail-info p {
  margin: 0.4rem 0;
  font-size: 0.9rem;
}

.detail-info p strong {
  color: #334155;
}

.status-ok {
  color: #16a34a;
  font-weight: 600;
}

.status-error {
  color: #dc2626;
  font-weight: 600;
}

.status-pending,
.status-warning { /* Unificamos estilos */
  color: #d97706;
  font-weight: 600;
}

.status-info {
  color: #0284c7;
  font-weight: 600;
}

.detail-modal .modal-actions {
  padding: 1rem 1.5rem;
  background-color: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.modal-content h3 {
  /* Estilo para modal de confirmación (asegurar especificidad si es necesario) */
  &.confirmation-title { /* Ejemplo de clase para especificidad */
    font-size: 1.5rem;
    font-weight: 700;
    color: #16a34a;
    margin-bottom: 1rem;
  }
}

.confirmation-details {
  background-color: #f8fafc;
  border-radius: 8px;
  padding: 1rem;
  text-align: left;
  margin: 1.5rem 0;
}

@media (max-width: 768px) {
  .prescription-container {
    flex-direction: column;
  }

  .prescription-list,
  .prescription-detail {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .data-grid {
    grid-template-columns: 1fr;
  }

  .verification-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .action-buttons {
    flex-direction: column;
  }

  .cancel-button,
  .confirm-button,
  .detail-button {
    width: 100%;
  }
}

.quantity-controls.disabled {
  opacity: 0.7;
}

.quantity-locked {
  font-size: 0.8rem;
  color: #64748b;
  margin-left: 0.5rem;
  font-style: italic;
}

/* Estilos para el formulario de tarjeta de crédito */
.payment-section {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.payment-section h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
  color: #1e293b;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 0.5rem;
}

.card-form {
  background-color: #f8fafc;
  padding: 1.5rem;
  border-radius: 10px;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  font-weight: 500;
  color: #334155;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 1rem;
  transition: all 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.form-row {
  display: flex;
  gap: 1rem;
}

.form-row .col {
  flex: 1;
}

.card-number-wrapper {
  position: relative;
}

.card-type {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
}

.card-validation {
  margin-top: 0.5rem;
}

.validation-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
  font-size: 0.85rem;
  color: #ef4444;
}

.validation-indicator.valid {
  color: #22c55e;
}

.validation-indicator i {
  font-size: 1rem;
}

/* Pequeño loader para estados de verificación */
.verification-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #e2e8f0;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  display: inline-block;
  margin-right: 0.5rem;
}
</style> 