<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { getInsuranceApiUrl } from "../../utils/api"; 
interface Approval {
  idApproval: number;
  authorizationNumber: string;
  idUser: number;
  prescriptionIdHospital?: string;
  prescriptionDetails?: string;
  prescriptionCost: number;
  approvalDate: string;
  status: string; // APPROVED, REJECTED, PENDING
  rejectionReason?: string;
  // Podríamos añadir datos del usuario si el backend los enriquece
  userName?: string;
}

interface Medicine {
  _id: string;
  principioActivo?: string;
  active_principle?: string;
  concentracion?: string;
  concentration?: string;
  presentacion?: string;
  presentation?: string;
  formaFarmaceutica?: string;
  pharmaceutical_form?: string;
  pharmaceutic_form?: string;
  dosis?: string;
  dosage?: string;
  frecuencia?: string;
  frequency?: string;
  duracion?: string;
  duration?: string;
  diagnostico?: string;
  diagnosis?: string;
  diagnostic?: string;
  quantity?: number;
  name?: string;
  calculatedPrice?: number;
  calculatedQuantity?: number;
  calculatedTotal?: number;
}

interface HospitalPrescription {
  _id: string;
  code: string;
  date?: string;
  created_at?: string;
  patient: {
    _id: string;
    username: string;
    email: string;
  };
  doctor: string;
  medicines: Medicine[];
  has_insurance?: boolean;
  insurance_code?: string;
  special_notes?: string;
  // UI state
  isProcessing?: boolean;
  estimatedCost?: number;
}

interface ConfigurableAmount {
  idConfigurableAmount: number;
  prescriptionAmount: number;
}

// Función para obtener el hospital predeterminado
const getDefaultHospital = () => {
  try {
    const storedHospital = localStorage.getItem('defaultHospital');
    if (storedHospital) {
      return JSON.parse(storedHospital);
    }
    return null;
  } catch (error) {
    console.error('Error al obtener el hospital predeterminado:', error);
    return null;
  }
};

// Obtener la configuración del hospital predeterminado
const defaultHospital = getDefaultHospital();
const ip = import.meta.env.VITE_IP || "localhost";
// Usar el puerto del hospital predeterminado o 5050 como fallback
const DEFAULT_PORT = defaultHospital?.port || '5050';

// Variables globales
const possibleIPs = [ip];
const HOSPITAL_API = `http://${ip}:${DEFAULT_PORT}`;
const PHARMACY_API_BASE = getInsuranceApiUrl("/");

// Información sobre el hospital predeterminado para mostrar
const usingDefaultHospital = computed(() => {
  return defaultHospital 
    ? `Hospital seleccionado: ${defaultHospital.name} (Puerto: ${defaultHospital.port || '5050'})` 
    : 'No hay hospital predeterminado seleccionado';
});

// Estado
const approvals = ref<Approval[]>([]);
const hospitalPrescriptions = ref<HospitalPrescription[]>([]);
const loading = ref(true);
const loadingHospital = ref(true);
const error = ref("");
const hospitalError = ref("");
const success = ref("");
const filterUserId = ref<number | null>(null);
const showAll = ref(true); // Por defecto mostrar todo
const minAmount = ref<number | null>(null);
const processingId = ref<number | null>(null);
const activeTab = ref('hospital'); // 'hospital' o 'approved'

// Función para probar múltiples IPs (simplificada para GET)
async function tryMultipleIPs(endpoint: string, method: string = 'GET', data: any = null) {
  const serverIP = import.meta.env.VITE_IP || "localhost";
  try {
    const url = getInsuranceApiUrl(endpoint);
    console.log(`Intentando ${method} a ${url}`);
    const response = await axios({ method, url, data, timeout: 3000 });
    return response;
  } catch (error: any) {
    console.error(`Error con IP ${serverIP}:`, error.message);
    throw new Error("No se pudo conectar con el servidor");
  }
}

// Obtener precio de medicamento desde la API de farmacia
const getMedicinePrice = async (medicineName: string) => {
  try {
    const response = await tryMultipleIPs(`/medicines/price?name=${encodeURIComponent(medicineName)}`);
    if (response && response.data && typeof response.data.price === 'number') {
      return response.data.price;
    }
    return null;
  } catch (err) {
    console.error(`Error al obtener precio para ${medicineName}:`, err);
    return null;
  }
};

// Cargar recetas del hospital
const fetchHospitalPrescriptions = async () => {
  try {
    loadingHospital.value = true;
    hospitalError.value = "";
    
    const response = await axios.get(`${HOSPITAL_API}/recipes`);
    
    if (response.data && response.data.recipes) {
      // Procesar las recetas
      const prescriptions = response.data.recipes;
      
      // Para cada receta, obtener precios reales y calcular el costo total
      const prescriptionsWithPrices = await Promise.all(prescriptions.map(async (prescription: HospitalPrescription) => {
        let totalCost = 0;
        
        // Mapear medicamentos y obtener precios
        const medicinesWithPrices = await Promise.all(prescription.medicines.map(async (medicine) => {
          const medicineName = getMedicineName(medicine);
          let price = null;
          
          // Intentar obtener el precio real desde la API
          try {
            price = await getMedicinePrice(medicineName);
          } catch (error) {
            console.error(`Error al obtener precio para ${medicineName}:`, error);
          }
          
          // Si no se pudo obtener el precio, usar un valor fijo (para que sea consistente)
          if (price === null) {
            // Usar hash del nombre del medicamento para generar un precio consistente
            const hashCode = function(str: string) {
              let hash = 0;
              for (let i = 0; i < str.length; i++) {
                const char = str.charCodeAt(i);
                hash = ((hash << 5) - hash) + char;
                hash = hash & hash; // Convert to 32bit integer
              }
              return Math.abs(hash);
            }
            
            // Generar un precio entre 50 y 500 basado en el nombre
            price = (hashCode(medicineName) % 450) + 50;
          }
          
          // Calcular cantidad basada en dosis y duración (si están disponibles)
          let quantity = 1;
          
          // Intentar extraer números de los campos de dosis y duración
          const dosis = extractNumber(medicine.dosis || medicine.dosage || "1");
          const duracion = extractNumber(medicine.duracion || medicine.duration || "1");
          const presentacion = extractNumber(medicine.presentacion || medicine.presentation || "1");
          
          if (dosis && duracion && presentacion && presentacion > 0) {
            quantity = Math.ceil((dosis * duracion) / presentacion);
          } else {
            // Si no se pueden extraer los valores, usar una cantidad fija
            quantity = 1;
          }
          
          // Asegurar que la cantidad sea al menos 1
          quantity = Math.max(1, quantity);
          
          // Calcular costo total para este medicamento
          const medicineTotal = price * quantity;
          
          // Sumar al costo total de la receta
          totalCost += medicineTotal;
          
          return {
            ...medicine,
            calculatedPrice: price,
            calculatedQuantity: quantity,
            calculatedTotal: medicineTotal
          };
        }));
        
        return {
          ...prescription,
          medicines: medicinesWithPrices,
          estimatedCost: totalCost
        };
      }));
      
      hospitalPrescriptions.value = prescriptionsWithPrices;
    } else {
      hospitalPrescriptions.value = [];
      hospitalError.value = "No se pudieron cargar las recetas del hospital.";
    }
  } catch (err: any) {
    hospitalError.value = "Error al conectar con el API del hospital.";
    console.error("Error al cargar recetas del hospital:", err);
    hospitalPrescriptions.value = [];
  } finally {
    loadingHospital.value = false;
  }
};

// Función auxiliar para extraer números de strings
const extractNumber = (str: string): number => {
  if (!str) return 1;
  const match = str.match(/\d+(\.\d+)?/);
  return match ? parseFloat(match[0]) : 1;
}

// Cargar el monto mínimo configurado
const fetchMinAmount = async () => {
  try {
    const response = await tryMultipleIPs('/configurable-amount/current');
    if (response.data) {
      minAmount.value = response.data.prescriptionAmount;
    }
  } catch (err: any) {
    console.error("Error al cargar el monto mínimo:", err);
    minAmount.value = 0; // Default fallback
  }
};

// Cargar aprobaciones
const fetchApprovals = async () => {
  try {
    loading.value = true;
    error.value = "";
    let endpoint = '/prescriptions/approvals';
    if (filterUserId.value) {
        endpoint += `?userId=${filterUserId.value}`;
    }
    if (showAll.value) {
        endpoint += (filterUserId.value ? '&' : '?') + 'showAll=true';
    }
    const response = await tryMultipleIPs(endpoint);
    approvals.value = Array.isArray(response.data) ? response.data : [];
  } catch (err: any) {
    error.value = "Error al cargar el historial de aprobaciones.";
    console.error(err);
    approvals.value = [];
  } finally {
    loading.value = false;
  }
};

// Aprobar receta del sistema actual
const approvePrescription = async (approval: Approval) => {
  if (!approval || processingId.value !== null) return;
  
  try {
    processingId.value = approval.idApproval;
    success.value = "";
    error.value = "";
    
    const response = await tryMultipleIPs(
      `/prescriptions/approve/${approval.idApproval}`, 
      'PUT'
    );
    
    if (response && response.data) {
      success.value = `Receta aprobada correctamente. Número de autorización: ${response.data.authorizationNumber}`;
      // Actualizar la lista
      await fetchApprovals();
    }
  } catch (err: any) {
    error.value = "Error al aprobar la receta. Intente nuevamente.";
    console.error(err);
  } finally {
    processingId.value = null;
  }
};

// Aprobar receta del hospital
const approveHospitalPrescription = async (prescription: HospitalPrescription) => {
  if (!prescription || prescription.isProcessing) return;
  
  try {
    // Marcar como procesando
    prescription.isProcessing = true;
    success.value = "";
    error.value = "";
    
    // Preparar datos para la API de seguros
    const newPrescriptionData = {
      prescriptionIdHospital: prescription.code,
      prescriptionDetails: JSON.stringify({
        patient: prescription.patient.username,
        medicines: prescription.medicines.map(m => ({
          name: m.principioActivo || m.active_principle,
          diagnosis: m.diagnostico || m.diagnosis || m.diagnostic
        })),
        special_notes: prescription.special_notes
      }),
      idUser: 1, // Usuario por defecto o extraído del código de seguro
      prescriptionCost: prescription.estimatedCost
    };
    
    // Enviar a la API de seguros
    const response = await tryMultipleIPs(
      '/prescriptions/create', 
      'POST',
      newPrescriptionData
    );
    
    if (response && response.data) {
      success.value = `Receta registrada correctamente. ID: ${response.data.idApproval}`;
      
      // Si está pendiente, aprobarla automáticamente si cumple con el monto
      if (meetsMinAmount(prescription.estimatedCost || 0)) {
        const approveResponse = await tryMultipleIPs(
          `/prescriptions/approve/${response.data.idApproval}`, 
          'PUT'
        );
        
        if (approveResponse && approveResponse.data) {
          success.value = `Receta aprobada correctamente. Número de autorización: ${approveResponse.data.authorizationNumber}`;
        }
      }
      
      // Actualizar ambas listas
      await Promise.all([fetchApprovals(), fetchHospitalPrescriptions()]);
    }
  } catch (err: any) {
    error.value = "Error al procesar la receta del hospital. Intente nuevamente.";
    console.error(err);
  } finally {
    if (prescription) {
      prescription.isProcessing = false;
    }
  }
};

// Rechazar receta
const rejectPrescription = async (approval: Approval, reason: string) => {
  if (!approval || processingId.value !== null) return;
  
  try {
    processingId.value = approval.idApproval;
    success.value = "";
    error.value = "";
    
    const response = await tryMultipleIPs(
      `/prescriptions/reject/${approval.idApproval}`, 
      'PUT',
      { rejectionReason: reason }
    );
    
    if (response) {
      success.value = "Receta rechazada correctamente.";
      // Actualizar la lista
      await fetchApprovals();
    }
  } catch (err: any) {
    error.value = "Error al rechazar la receta. Intente nuevamente.";
    console.error(err);
  } finally {
    processingId.value = null;
  }
};

// Formatear fecha y hora
const formatDateTime = (dateString: string) => {
  if (!dateString) return "N/A";
  return new Date(dateString).toLocaleString();
};

// Formatear costo
const formatCost = (cost: number) => {
    return `Q${cost.toFixed(2)}`;
};

// Verificar si la receta cumple con el monto mínimo
const meetsMinAmount = (cost: number) => {
    return minAmount.value !== null && cost >= minAmount.value;
};

// Obtener nombre de medicamento
const getMedicineName = (medicine: Medicine) => {
  return medicine.principioActivo || 
         medicine.active_principle || 
         medicine.name || 
         'Sin nombre';
};

// Obtener diagnóstico
const getDiagnosis = (medicine: Medicine) => {
  return medicine.diagnostico || 
         medicine.diagnosis || 
         medicine.diagnostic || 
         'No especificado';
};

// Cargar datos al iniciar
onMounted(async () => {
  await fetchMinAmount();
  await Promise.all([fetchApprovals(), fetchHospitalPrescriptions()]);
});

// Filtrar por usuario
const applyFilter = () => {
    fetchApprovals();
}

// Cambiar pestaña
const changeTab = (tab: string) => {
  activeTab.value = tab;
}

</script>

<template>
  <div class="container mx-auto p-6">
    <h1 class="text-2xl font-bold mb-6">Aprobación de Recetas</h1>
    
    <!-- Información del hospital por defecto -->
    <div v-if="defaultHospital" class="bg-blue-50 p-3 rounded mb-4 border border-blue-200">
      <div class="flex items-center">
        <span class="text-blue-700">{{ usingDefaultHospital }}</span>
      </div>
    </div>

    <!-- Mensajes de éxito o error -->
    <div v-if="success" class="success-message">{{ success }}</div>
    <div v-if="error" class="error-message">{{ error }}</div>
    <div v-if="hospitalError" class="error-message">{{ hospitalError }}</div>

    <!-- Información del monto mínimo -->
    <div v-if="minAmount !== null" class="info-message">
        Monto mínimo actual para aprobar recetas: <strong>{{ formatCost(minAmount) }}</strong>
    </div>

    <!-- Pestañas -->
    <div class="tabs">
      <button 
        @click="changeTab('hospital')" 
        :class="{ 'active-tab': activeTab === 'hospital' }"
        class="tab-button"
      >
        Recetas del Hospital
      </button>
      <button 
        @click="changeTab('approved')" 
        :class="{ 'active-tab': activeTab === 'approved' }"
        class="tab-button"
      >
        Historial de Aprobaciones
      </button>
    </div>

    <!-- Tab: Recetas del Hospital -->
    <div v-if="activeTab === 'hospital'" class="tab-content">
      <button @click="fetchHospitalPrescriptions" class="btn-refresh">
        Actualizar Recetas
      </button>

      <div class="approvals-table-container">
        <div v-if="loadingHospital" class="loading-container">
          <div class="loading-spinner"></div>
          <p class="loading-text">Cargando recetas del hospital...</p>
        </div>
        
        <div v-else-if="!hospitalPrescriptions || hospitalPrescriptions.length === 0" class="empty-message">
          <p>No hay recetas disponibles del hospital.</p>
        </div>
        
        <div v-else class="table-scroll-container">
          <table class="approvals-table">
            <thead>
              <tr>
                <th>Código</th>
                <th>Paciente</th>
                <th>Fecha</th>
                <th>Medicamentos</th>
                <th>Costo Total</th>
                <th>Seguro</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="prescription in hospitalPrescriptions" :key="prescription._id" class="table-row">
                <td class="prescription-code">{{ prescription.code }}</td>
                <td>{{ prescription.patient?.username || 'Desconocido' }}</td>
                <td>{{ formatDateTime(prescription.created_at || prescription.date || '') }}</td>
                <td class="medicines-cell">
                  <ul class="medicines-list">
                    <li v-for="(medicine, idx) in prescription.medicines" :key="idx" class="medicine-item">
                      <div class="medicine-name">
                        {{ getMedicineName(medicine) }}
                        <span class="medicine-diagnosis">({{ getDiagnosis(medicine) }})</span>
                      </div>
                      <div class="medicine-price">
                        <span class="quantity">{{ medicine.calculatedQuantity || 1 }} x </span>
                        {{ formatCost(medicine.calculatedPrice || 0) }} = 
                        <span class="medicine-total">{{ formatCost(medicine.calculatedTotal || 0) }}</span>
                      </div>
                    </li>
                  </ul>
                </td>
                <td class="cost">
                  <div class="total-cost">{{ formatCost(prescription.estimatedCost || 0) }}</div>
                </td>
                <td>
                  <span v-if="prescription.has_insurance" class="insurance-badge">
                    {{ prescription.insurance_code || 'Sí' }}
                  </span>
                  <span v-else>No</span>
                </td>
                <td class="actions">
                  <button 
                    @click="approveHospitalPrescription(prescription)"
                    :disabled="!meetsMinAmount(prescription.estimatedCost || 0) || prescription.isProcessing"
                    :class="{ 
                      'btn-approve': meetsMinAmount(prescription.estimatedCost || 0), 
                      'btn-disabled': !meetsMinAmount(prescription.estimatedCost || 0) || prescription.isProcessing 
                    }"
                    :title="!meetsMinAmount(prescription.estimatedCost || 0) ? 'No cumple con el monto mínimo' : ''"
                  >
                    {{ prescription.isProcessing ? 'Procesando...' : 'Aprobar' }}
                  </button>
                  
                  <div v-if="!meetsMinAmount(prescription.estimatedCost || 0)" class="warning-message">
                    No cumple con el monto mínimo requerido.
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Tab: Historial de Aprobaciones -->
    <div v-if="activeTab === 'approved'" class="tab-content">
      <!-- Filtros -->
      <div class="filter-container">
          <label for="userFilter">Filtrar por ID de Usuario:</label>
          <input 
              type="number" 
              id="userFilter"
              v-model.number="filterUserId"
              placeholder="Dejar vacío para todos"
              class="filter-input"
          />
          <button @click="applyFilter" class="btn-primary">Filtrar</button>
          <button @click="filterUserId = null; applyFilter();" class="btn-secondary">Limpiar</button>
          
          <div class="checkbox-container">
              <input type="checkbox" id="showAll" v-model="showAll" @change="applyFilter">
              <label for="showAll">Mostrar todas (incluyendo pendientes)</label>
          </div>
      </div>

      <div class="approvals-table-container">
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p class="loading-text">Cargando historial...</p>
        </div>
        
        <div v-else-if="!approvals || approvals.length === 0" class="empty-message">
          <p>No hay registros de recetas.</p>
        </div>
        
        <div v-else class="table-scroll-container">
          <table class="approvals-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>No. Autorización</th>
                <th>ID Usuario</th>
                <th>Costo Receta</th>
                <th>Fecha</th>
                <th>Estado</th>
                <th>ID Receta Hosp.</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="approval in approvals" :key="approval.idApproval" class="table-row">
                <td>{{ approval.idApproval }}</td>
                <td class="auth-number">{{ approval.authorizationNumber || '-' }}</td>
                <td>{{ approval.idUser }}</td>
                <td class="cost">{{ formatCost(approval.prescriptionCost) }}</td>
                <td>{{ formatDateTime(approval.approvalDate) }}</td>
                <td>
                  <span :class="{
                    'status-approved': approval.status === 'APPROVED',
                    'status-rejected': approval.status === 'REJECTED',
                    'status-pending': approval.status === 'PENDING'
                  }">
                    {{ approval.status }}
                  </span>
                </td>
                <td class="hospital-id">{{ approval.prescriptionIdHospital || '-' }}</td>
                <td class="actions">
                  <template v-if="approval.status === 'PENDING'">
                    <button 
                      @click="approvePrescription(approval)"
                      :disabled="!meetsMinAmount(approval.prescriptionCost) || processingId === approval.idApproval"
                      :class="{ 
                        'btn-approve': meetsMinAmount(approval.prescriptionCost), 
                        'btn-disabled': !meetsMinAmount(approval.prescriptionCost) || processingId === approval.idApproval 
                      }"
                      :title="!meetsMinAmount(approval.prescriptionCost) ? 'No cumple con el monto mínimo' : ''"
                    >
                      {{ processingId === approval.idApproval ? 'Procesando...' : 'Aprobar' }}
                    </button>
                    
                    <div v-if="!meetsMinAmount(approval.prescriptionCost)" class="warning-message">
                      No cumple con el monto mínimo requerido.
                    </div>
                    
                    <button 
                      @click="rejectPrescription(approval, 'Rechazado por el administrador')"
                      :disabled="processingId === approval.idApproval"
                      class="btn-reject"
                    >
                      Rechazar
                    </button>
                  </template>
                  <template v-else>
                    <span class="action-text">{{ approval.status === 'APPROVED' ? 'Aprobada' : 'Rechazada' }}</span>
                  </template>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 90%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 24px;
}

.success-message {
  background-color: #dcfce7;
  color: #166534;
  padding: 12px;
  margin-bottom: 16px;
  border-radius: 4px;
}

.error-message {
  background-color: #fee2e2;
  color: #b91c1c;
  padding: 12px;
  margin-bottom: 16px;
  border-radius: 4px;
}

.info-message {
  background-color: #eff6ff;
  color: #1e40af;
  padding: 12px;
  margin-bottom: 16px;
  border-radius: 4px;
}

.tabs {
  display: flex;
  margin-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.tab-button {
  padding: 8px 16px;
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
}

.active-tab {
  border-bottom: 2px solid #2563eb;
  color: #2563eb;
  font-weight: 500;
}

.tab-content {
  margin-top: 16px;
}

.btn-refresh {
  margin-bottom: 16px;
  padding: 8px 16px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-refresh:hover {
  background-color: #2563eb;
}

.filter-container {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.checkbox-container {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
}

.filter-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

.btn-primary {
  padding: 4px 12px;
  background-color: #2563eb;
  color: white;
  font-size: 14px;
  border-radius: 4px;
}

.btn-primary:hover {
  background-color: #1d4ed8;
}

.btn-secondary {
  padding: 4px 12px;
  background-color: #d1d5db;
  color: #1f2937;
  font-size: 14px;
  border-radius: 4px;
}

.btn-secondary:hover {
  background-color: #9ca3af;
}

.approvals-table-container {
  background-color: white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

.table-scroll-container {
  width: 100%;
  overflow-x: auto;
  max-height: 70vh; /* Límite de altura para el scroll vertical */
  overflow-y: auto;
}

.approvals-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 900px; /* Ancho mínimo para asegurar que aparezca scroll */
}

.loading-container {
  padding: 24px;
  text-align: center;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2px solid #e5e7eb;
  border-top-color: #3b82f6;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 16px;
  color: #4b5563;
}

.empty-message {
  padding: 40px;
  text-align: center;
}

.empty-message p {
  font-size: 18px;
  color: #4b5563;
}

.approvals-table th {
  padding: 8px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 500;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background-color: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.approvals-table td {
  padding: 8px 16px;
  white-space: nowrap;
  font-size: 14px;
  border-bottom: 1px solid #e5e7eb;
}

.table-row:hover {
  background-color: #f9fafb;
}

.auth-number {
  font-family: monospace;
  font-size: 12px;
}

.cost {
  text-align: right;
}

.status-approved {
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 500;
  background-color: #d1fae5;
  color: #065f46;
}

.status-rejected {
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 500;
  background-color: #fee2e2;
  color: #b91c1c;
}

.status-pending {
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 500;
  background-color: #fef3c7;
  color: #92400e;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.btn-approve {
  padding: 4px 12px;
  background-color: #10b981;
  color: white;
  font-size: 14px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-approve:hover {
  background-color: #059669;
}

.btn-reject {
  padding: 4px 12px;
  background-color: #ef4444;
  color: white;
  font-size: 14px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-reject:hover {
  background-color: #dc2626;
}

.btn-disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: #9ca3af;
}

.warning-message {
  font-size: 11px;
  color: #dc2626;
  margin: 4px 0;
}

.action-text {
  font-size: 14px;
  color: #6b7280;
}

.hospital-id {
  font-size: 12px;
}

.prescription-code {
  font-family: monospace;
  font-size: 12px;
}

.medicines-cell {
  max-width: 250px;
  min-width: 200px;
  white-space: normal;
}

.medicines-list {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 13px;
}

.medicines-list li {
  margin-bottom: 4px;
}

.medicine-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.medicine-name {
  flex: 1;
}

.medicine-price {
  color: #1d4ed8;
  font-weight: 500;
  font-size: 13px;
  white-space: nowrap;
}

.quantity {
  color: #6b7280;
  font-weight: normal;
}

.medicine-total {
  font-weight: 600;
}

.total-cost {
  font-weight: bold;
  color: #1d4ed8;
  font-size: 15px;
}

.medicine-diagnosis {
  font-style: italic;
  color: #6b7280;
  font-size: 11px;
}

.insurance-badge {
  background-color: #dbeafe;
  color: #1e40af;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}
</style> 