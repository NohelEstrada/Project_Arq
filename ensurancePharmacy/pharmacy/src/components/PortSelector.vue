<template>
  <div v-if="showDialog" class="modal-overlay">
    <div class="modal-container">
      <div class="modal-header">
        <h2>Configuración de Puertos</h2>
      </div>
      
      <div class="modal-body">
        <p class="modal-description">
          Configure los puertos para conectarse a los servidores de backend.
        </p>
        
        <div class="form-group">
          <label for="pharmacyPort">Puerto de Pharmacy:</label>
          <div class="input-group">
            <span class="input-prefix">http://{{ ip }}:</span>
            <input
              id="pharmacyPort"
              v-model="pharmacyPort"
              type="text"
              class="form-control"
              placeholder="8081"
            />
          </div>
          <p class="help-text">Puerto predeterminado: 8081</p>
        </div>
        
        <div class="form-group">
          <label for="ensurancePort">Puerto de Ensurance:</label>
          <div class="input-group">
            <span class="input-prefix">http://{{ ip }}:</span>
            <input
              id="ensurancePort"
              v-model="ensurancePort"
              type="text"
              class="form-control"
              placeholder="8080"
            />
          </div>
          <p class="help-text">Puerto predeterminado: 8080</p>
        </div>
        
        <div class="checkbox-group">
          <input
            id="savePreference"
            v-model="savePreference"
            type="checkbox"
          />
          <label for="savePreference">
            Recordar mi elección (no mostrar de nuevo)
          </label>
        </div>
      </div>
      
      <div class="modal-footer">
        <button
          @click="saveConfiguration"
          class="btn btn-primary"
        >
          Guardar configuración
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import ApiService from '../services/ApiService';

export default {
  name: 'PortSelector',
  data() {
    return {
      pharmacyPort: ApiService.defaultPortConfig.pharmacy,
      ensurancePort: ApiService.defaultPortConfig.ensurance,
      showDialog: true,
      savePreference: true,
      ip: process.env.VUE_APP_API_HOST || process.env.VUE_APP_IP || 'localhost'
    };
  },
  mounted() {
    // Cargar configuración guardada
    const savedConfig = localStorage.getItem("apiPortConfig");
    if (savedConfig) {
      try {
        const config = JSON.parse(savedConfig);
        if (config.pharmacy) this.pharmacyPort = config.pharmacy;
        if (config.ensurance) this.ensurancePort = config.ensurance;
      } catch (error) {
        console.warn("Error al cargar configuración de puertos:", error);
      }
    }
    
    // Cargar puertos en el sistema
    ApiService.loadPortConfiguration();
  },
  methods: {
    saveConfiguration() {
      // Validar puertos
      if (!this.isValidPort(this.pharmacyPort) || !this.isValidPort(this.ensurancePort)) {
        alert('Por favor ingrese puertos válidos (1024-65535)');
        return;
      }
      
      // Configurar puertos
      ApiService.configureApiPorts({
        pharmacy: this.pharmacyPort,
        ensurance: this.ensurancePort
      });
      
      // Guardar preferencia para no mostrar de nuevo si está marcado
      if (this.savePreference) {
        localStorage.setItem("skipPortSelector", "true");
      } else {
        localStorage.removeItem("skipPortSelector");
      }
      
      this.showDialog = false;
      this.$emit('close');
    },
    isValidPort(port) {
      const num = parseInt(port);
      return !isNaN(num) && num >= 1024 && num <= 65535;
    }
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-container {
  background-color: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.modal-body {
  padding: 20px;
}

.modal-description {
  margin-bottom: 20px;
  color: #666;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
}

.input-group {
  display: flex;
  align-items: center;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.input-prefix {
  background-color: #f8f8f8;
  padding: 8px 12px;
  border-right: 1px solid #ccc;
  color: #666;
}

.form-control {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 0 4px 4px 0;
  outline: none;
}

.help-text {
  margin-top: 4px;
  font-size: 0.85em;
  color: #666;
}

.checkbox-group {
  display: flex;
  align-items: center;
  margin-top: 16px;
}

.checkbox-group input {
  margin-right: 8px;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  text-align: right;
}

.btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background-color: #4a5af8;
  color: white;
}

.btn-primary:hover {
  background-color: #3949e6;
}
</style> 