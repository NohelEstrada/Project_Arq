/**
 * Servicio para manejar las URLs de las APIs con puertos configurables
 */

// Importar variables de entorno
const ip = process.env.VUE_APP_API_HOST || process.env.VUE_APP_IP || 'localhost';

// Configuración de puertos por defecto
const defaultPortConfig = {
  pharmacy: '8081',    // Puerto por defecto para pharmacy
  ensurance: '8080'    // Puerto por defecto para ensurance
};

// Almacenar la configuración de puertos
let portConfig = { ...defaultPortConfig };

/**
 * Configura los puertos para las APIs
 * @param {Object} ports Configuración de puertos
 */
export const configureApiPorts = (ports) => {
  if (ports.pharmacy) portConfig.pharmacy = ports.pharmacy;
  if (ports.ensurance) portConfig.ensurance = ports.ensurance;
  
  // Guardar la configuración en localStorage para persistencia
  localStorage.setItem("apiPortConfig", JSON.stringify(portConfig));
  
  console.log(`Puertos configurados: Pharmacy=${portConfig.pharmacy}, Ensurance=${portConfig.ensurance}`);
};

/**
 * Carga la configuración de puertos desde localStorage
 */
export const loadPortConfiguration = () => {
  const savedConfig = localStorage.getItem("apiPortConfig");
  if (savedConfig) {
    try {
      const config = JSON.parse(savedConfig);
      portConfig = { ...portConfig, ...config };
      console.log(`Configuración de puertos cargada: Pharmacy=${portConfig.pharmacy}, Ensurance=${portConfig.ensurance}`);
    } catch (error) {
      console.warn("Error al cargar configuración de puertos:", error);
    }
  }
};

/**
 * Obtiene la URL de la API de farmacia con el puerto configurado
 * 
 * @param {string} endpoint - El endpoint de la API sin la barra inicial
 * @returns {string} URL completa con el puerto correcto
 */
export const getPharmacyApiUrl = (endpoint) => {
  // Eliminar la barra inicial del endpoint si existe
  const cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
  
  // Construir la URL completa
  return `http://${ip}:${portConfig.pharmacy}/api2/${cleanEndpoint}`;
};

/**
 * Obtiene la URL de la API de seguros con el puerto configurado
 * 
 * @param {string} endpoint - El endpoint de la API sin la barra inicial
 * @returns {string} URL completa con el puerto correcto
 */
export const getEnsuranceApiUrl = (endpoint) => {
  // Eliminar la barra inicial del endpoint si existe
  const cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
  
  // Construir la URL completa
  return `http://${ip}:${portConfig.ensurance}/api2/${cleanEndpoint}`;
};

// Exportar funciones y configuración por defecto
export default {
  configureApiPorts,
  loadPortConfiguration,
  getPharmacyApiUrl,
  getEnsuranceApiUrl,
  defaultPortConfig
}; 