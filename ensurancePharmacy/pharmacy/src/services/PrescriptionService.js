import axios from 'axios';
import ApiService from './ApiService';

class PrescriptionService {
  /**
   * Verifica una receta médica usando el código de aprobación del seguro
   * @param {string} approvalCode - Código de aprobación del seguro
   */
  async verifyPrescription(approvalCode) {
    try {
      const response = await axios.get(ApiService.getEnsuranceApiUrl(`/service-approvals/check/${approvalCode}`));
      return response.data;
    } catch (error) {
      console.error('Error al verificar la receta:', error);
      throw error;
    }
  }

  /**
   * Procesa una receta para dispensar medicamentos
   * @param {Object} prescriptionData - Datos de la receta a procesar
   */
  async processPrescription(prescriptionData) {
    try {
      const response = await axios.post(ApiService.getPharmacyApiUrl('/prescriptions/process'), prescriptionData);
      return response.data;
    } catch (error) {
      console.error('Error al procesar la receta:', error);
      throw error;
    }
  }

  /**
   * Marca una receta como completada en el sistema de seguros
   * @param {string} approvalCode - Código de aprobación del seguro
   */
  async completePrescription(approvalCode) {
    try {
      const response = await axios.put(ApiService.getEnsuranceApiUrl(`/service-approvals/complete/${approvalCode}`));
      return response.data;
    } catch (error) {
      console.error('Error al completar la receta:', error);
      throw error;
    }
  }

  /**
   * Obtiene el historial de recetas procesadas
   * @param {Object} filters - Filtros para la búsqueda
   */
  async getPrescriptionHistory(filters = {}) {
    try {
      const response = await axios.get(ApiService.getPharmacyApiUrl('/prescriptions/history'), { 
        params: filters 
      });
      return response.data;
    } catch (error) {
      console.error('Error al obtener el historial de recetas:', error);
      throw error;
    }
  }

  /**
   * Obtiene el detalle de una receta específica
   * @param {string} prescriptionId - ID de la receta
   */
  async getPrescriptionDetails(prescriptionId) {
    try {
      const response = await axios.get(ApiService.getPharmacyApiUrl(`/prescriptions/${prescriptionId}`));
      return response.data;
    } catch (error) {
      console.error('Error al obtener detalles de la receta:', error);
      throw error;
    }
  }

  /**
   * Verifica si un medicamento está disponible en inventario
   * @param {string} medicationCode - Código del medicamento
   * @param {number} quantity - Cantidad requerida
   */
  async checkMedicationAvailability(medicationCode, quantity) {
    try {
      const response = await axios.get(ApiService.getPharmacyApiUrl('/inventory/check'), {
        params: {
          code: medicationCode,
          quantity: quantity
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error al verificar disponibilidad del medicamento:', error);
      throw error;
    }
  }

  /**
   * Actualiza el inventario después de dispensar medicamentos
   * @param {Array} medications - Lista de medicamentos dispensados
   */
  async updateInventoryAfterDispensing(medications) {
    try {
      const response = await axios.post(ApiService.getPharmacyApiUrl('/inventory/update-after-dispensing'), {
        medications: medications
      });
      return response.data;
    } catch (error) {
      console.error('Error al actualizar el inventario:', error);
      throw error;
    }
  }

  /**
   * Genera un recibo para la receta procesada
   * @param {Object} receiptData - Datos para el recibo
   */
  async generateReceipt(receiptData) {
    try {
      const response = await axios.post(ApiService.getPharmacyApiUrl('/receipts/generate'), receiptData);
      return response.data;
    } catch (error) {
      console.error('Error al generar el recibo:', error);
      throw error;
    }
  }

  /**
   * Verifica si un medicamento está cubierto por el seguro
   * @param {string} medicationCode - Código del medicamento
   * @param {string} approvalCode - Código de aprobación del seguro
   */
  async checkInsuranceCoverage(medicationCode, approvalCode) {
    try {
      const response = await axios.get(ApiService.getEnsuranceApiUrl('/medication-coverage/check'), {
        params: {
          medicationCode,
          approvalCode
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error al verificar cobertura del medicamento:', error);
      throw error;
    }
  }
}

export default new PrescriptionService(); 