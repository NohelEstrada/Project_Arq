<template>
  <div class="process-prescription-container">
    <h1>Procesar Receta Médica</h1>
    
    <!-- Formulario de verificación de receta -->
    <div class="verification-form" v-if="!prescriptionVerified">
      <div class="card">
        <div class="card-header">Verificar Receta</div>
        <div class="card-body">
          <div class="form-group">
            <label for="approvalCode">Código de Aprobación:</label>
            <input 
              type="text" 
              id="approvalCode"
              class="form-control" 
              placeholder="Ingrese el código de aprobación" 
              v-model="approvalCode"
              :disabled="loading"
            />
          </div>
          <div class="form-actions">
            <button 
              class="btn btn-primary"
              @click="verifyPrescription"
              :disabled="!approvalCode || loading"
            >
              <span v-if="loading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
              Verificar
            </button>
          </div>
          <div v-if="error" class="error-message alert alert-danger mt-3">
            {{ error }}
          </div>
        </div>
      </div>
    </div>
    
    <!-- Información de receta verificada -->
    <div class="prescription-info" v-if="prescriptionVerified && prescriptionData">
      <div class="card mb-4">
        <div class="card-header bg-success text-white">Receta Verificada</div>
        <div class="card-body">
          <div class="row">
            <div class="col-md-6">
              <h3>Información del Servicio</h3>
              <p><strong>Código de Aprobación:</strong> {{ prescriptionData.approvalCode }}</p>
              <p><strong>Servicio:</strong> {{ prescriptionData.serviceName }}</p>
              <p><strong>Descripción:</strong> {{ prescriptionData.serviceDescription }}</p>
              <p><strong>Costo del Servicio:</strong> Q{{ formatPrice(prescriptionData.serviceCost) }}</p>
            </div>
            <div class="col-md-6">
              <h3>Información de Cobertura</h3>
              <p><strong>Monto Cubierto:</strong> Q{{ formatPrice(prescriptionData.coveredAmount) }}</p>
              <p><strong>Monto del Paciente:</strong> Q{{ formatPrice(prescriptionData.patientAmount) }}</p>
              <p v-if="prescriptionData.prescriptionTotal"><strong>Monto de la Receta:</strong> Q{{ formatPrice(prescriptionData.prescriptionTotal) }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Formulario para dispensar medicamentos -->
      <div class="card">
        <div class="card-header">Dispensar Medicamentos</div>
        <div class="card-body">
          <div v-if="prescriptionData.status === 'REJECTED'" class="alert alert-danger">
            <strong>Receta Rechazada:</strong> {{ prescriptionData.rejectionReason }}
          </div>
          
          <div v-else-if="prescriptionData.status === 'COMPLETED'" class="alert alert-warning">
            <strong>Receta ya completada:</strong> Esta receta ya ha sido dispensada.
          </div>
          
          <div v-else>
            <div class="medications-list">
              <h3>Medicamentos a Dispensar</h3>
              <div class="form-group" v-for="(medication, index) in medications" :key="index">
                <div class="row mb-2">
                  <div class="col-md-4">
                    <label :for="'medicationCode-' + index">Código del Medicamento:</label>
                    <input 
                      type="text" 
                      class="form-control" 
                      :id="'medicationCode-' + index"
                      v-model="medication.code"
                      placeholder="Código"
                    />
                  </div>
                  <div class="col-md-4">
                    <label :for="'medicationName-' + index">Nombre del Medicamento:</label>
                    <input 
                      type="text" 
                      class="form-control" 
                      :id="'medicationName-' + index"
                      v-model="medication.name"
                      placeholder="Nombre"
                    />
                  </div>
                  <div class="col-md-2">
                    <label :for="'medicationQuantity-' + index">Cantidad:</label>
                    <input 
                      type="number" 
                      class="form-control" 
                      :id="'medicationQuantity-' + index"
                      v-model="medication.quantity"
                      min="1"
                    />
                  </div>
                  <div class="col-md-2">
                    <label :for="'medicationPrice-' + index">Precio Unitario:</label>
                    <input 
                      type="number" 
                      class="form-control" 
                      :id="'medicationPrice-' + index"
                      v-model="medication.price"
                      min="0"
                      step="0.01"
                    />
                  </div>
                </div>
                <div class="row mb-3">
                  <div class="col-md-10">
                    <label :for="'medicationInstructions-' + index">Instrucciones:</label>
                    <input 
                      type="text" 
                      class="form-control" 
                      :id="'medicationInstructions-' + index"
                      v-model="medication.instructions"
                      placeholder="Instrucciones de uso"
                    />
                  </div>
                  <div class="col-md-2 d-flex align-items-end">
                    <button 
                      type="button" 
                      class="btn btn-danger w-100"
                      @click="removeMedication(index)"
                    >
                      Eliminar
                    </button>
                  </div>
                </div>
                <hr v-if="index < medications.length - 1" />
              </div>
              
              <button 
                type="button" 
                class="btn btn-secondary mb-3"
                @click="addMedication"
              >
                + Agregar Medicamento
              </button>
              
              <div class="total-section text-right">
                <h4>Total: Q{{ calculateTotal() }}</h4>
              </div>
              
              <div v-if="processingError" class="alert alert-danger mt-3">
                {{ processingError }}
              </div>
              
              <div class="form-actions text-right mt-3">
                <button 
                  type="button" 
                  class="btn btn-secondary mr-2"
                  @click="resetForm"
                >
                  Cancelar
                </button>
                <button 
                  type="button" 
                  class="btn btn-primary"
                  @click="processPrescription"
                  :disabled="loading || medications.length === 0 || !isValidForm()"
                >
                  <span v-if="loading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                  Dispensar Medicamentos
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Mensaje de éxito -->
    <div v-if="success" class="success-message alert alert-success mt-3">
      <h3>¡Receta procesada con éxito!</h3>
      <p>Los medicamentos han sido dispensados correctamente.</p>
      <p><strong>Número de Recibo:</strong> {{ receiptNumber }}</p>
      <button class="btn btn-primary" @click="resetForm">
        Procesar Nueva Receta
      </button>
    </div>
  </div>
</template>

<script>
import PrescriptionService from '@/services/PrescriptionService';

export default {
  name: 'ProcessPrescription',
  data() {
    return {
      approvalCode: '',
      prescriptionVerified: false,
      prescriptionData: null,
      medications: [
        { code: '', name: '', quantity: 1, price: 0, instructions: '' }
      ],
      loading: false,
      error: null,
      processingError: null,
      success: false,
      receiptNumber: ''
    }
  },
  methods: {
    async verifyPrescription() {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await PrescriptionService.verifyPrescription(this.approvalCode);
        
        if (response.success) {
          this.prescriptionData = response;
          this.prescriptionVerified = true;
        } else {
          this.error = response.message || 'No se pudo verificar la receta. Por favor, verifique el código e intente nuevamente.';
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Error al verificar la receta. Por favor, intente nuevamente.';
      } finally {
        this.loading = false;
      }
    },
    
    addMedication() {
      this.medications.push({ code: '', name: '', quantity: 1, price: 0, instructions: '' });
    },
    
    removeMedication(index) {
      if (this.medications.length > 1) {
        this.medications.splice(index, 1);
      }
    },
    
    calculateTotal() {
      return this.medications.reduce((total, medication) => {
        return total + (medication.quantity * medication.price);
      }, 0).toFixed(2);
    },
    
    isValidForm() {
      return this.medications.every(medication => 
        medication.code.trim() !== '' && 
        medication.name.trim() !== '' && 
        medication.quantity > 0 && 
        medication.price > 0
      );
    },
    
    async processPrescription() {
      if (!this.isValidForm()) {
        this.processingError = 'Por favor, complete todos los campos de medicamentos.';
        return;
      }
      
      this.loading = true;
      this.processingError = null;
      
      try {
        // Validar que el total coincida con el monto de la receta del seguro
        const total = parseFloat(this.calculateTotal());
        if (this.prescriptionData.prescriptionTotal && 
            Math.abs(total - this.prescriptionData.prescriptionTotal) > 0.01) {
          this.processingError = `El total de los medicamentos (Q${total}) no coincide con el monto de la receta aprobada (Q${this.prescriptionData.prescriptionTotal}).`;
          this.loading = false;
          return;
        }
        
        // Procesar la receta
        const processData = {
          approvalCode: this.approvalCode,
          medications: this.medications,
          total: total
        };
        
        const response = await PrescriptionService.processPrescription(processData);
        
        if (response.success) {
          // Marcar como completada en el sistema de seguros
          await PrescriptionService.completePrescription(this.approvalCode);
          
          this.success = true;
          this.receiptNumber = response.receiptNumber;
        } else {
          this.processingError = response.message || 'Error al procesar la receta.';
        }
      } catch (error) {
        this.processingError = error.response?.data?.message || 'Error al procesar la receta. Por favor, intente nuevamente.';
      } finally {
        this.loading = false;
      }
    },
    
    resetForm() {
      this.approvalCode = '';
      this.prescriptionVerified = false;
      this.prescriptionData = null;
      this.medications = [{ code: '', name: '', quantity: 1, price: 0, instructions: '' }];
      this.error = null;
      this.processingError = null;
      this.success = false;
      this.receiptNumber = '';
    },
    
    formatPrice(value) {
      return parseFloat(value).toFixed(2);
    }
  }
}
</script>

<style scoped>
.process-prescription-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  margin-bottom: 30px;
  color: #2c3e50;
  text-align: center;
}

.card {
  margin-bottom: 20px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  font-weight: bold;
  font-size: 18px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  font-weight: 500;
  margin-bottom: 5px;
  display: block;
}

.form-actions {
  margin-top: 20px;
}

.error-message, .success-message {
  margin-top: 20px;
  padding: 15px;
  border-radius: 4px;
}

.total-section {
  margin-top: 20px;
  padding-top: 10px;
  border-top: 1px solid #e0e0e0;
}

.medications-list {
  margin-top: 20px;
}

h3 {
  margin-bottom: 15px;
  color: #2c3e50;
}

hr {
  margin-top: 20px;
  margin-bottom: 20px;
}
</style> 