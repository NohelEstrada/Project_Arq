/**
 * Utilidades para manejar las URLs de las APIs con puertos configurables
 */

// Importar las variables de entorno
const ip = import.meta.env.VITE_IP || "localhost";

// Interfaces
interface PortConfig {
  ensurance: string;  // Puerto para el backend de seguros
  pharmacy: string;   // Puerto para el backend de farmacia
}

// Almacenar la configuración de puertos
let portConfig: PortConfig = {
  ensurance: "8080",  // Puerto por defecto para ensurance
  pharmacy: "8081"    // Puerto por defecto para pharmacy
};

/**
 * Configura los puertos para las APIs
 * @param ports Configuración de puertos
 */
export const configureApiPorts = (ports: Partial<PortConfig>): void => {
  if (ports.ensurance) portConfig.ensurance = ports.ensurance;
  if (ports.pharmacy) portConfig.pharmacy = ports.pharmacy;
  
  // Guardar la configuración en localStorage para persistencia
  localStorage.setItem("apiPortConfig", JSON.stringify(portConfig));
  
  console.log(`Puertos configurados: Ensurance=${portConfig.ensurance}, Pharmacy=${portConfig.pharmacy}`);
};

/**
 * Carga la configuración de puertos desde localStorage
 */
export const loadPortConfiguration = (): void => {
  const savedConfig = localStorage.getItem("apiPortConfig");
  if (savedConfig) {
    try {
      const config = JSON.parse(savedConfig);
      portConfig = { ...portConfig, ...config };
      console.log(`Configuración de puertos cargada: Ensurance=${portConfig.ensurance}, Pharmacy=${portConfig.pharmacy}`);
    } catch (error) {
      console.warn("Error al cargar configuración de puertos:", error);
    }
  }
};

/**
 * Obtiene la URL de la API de seguros con el puerto configurado
 * 
 * @param endpoint - El endpoint de la API sin la barra inicial, ej: "users"
 * @returns URL completa con el puerto correcto
 */
export const getInsuranceApiUrl = (endpoint: string): string => {
  // Eliminar la barra inicial del endpoint si existe
  const cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
  
  // Construir la URL completa
  return `http://${ip}:${portConfig.ensurance}/api/${cleanEndpoint}`;
};

/**
 * Obtiene la URL de la API de farmacia con el puerto configurado
 * 
 * @param endpoint - El endpoint de la API sin la barra inicial
 * @returns URL completa de la API de farmacia
 */
export const getPharmacyApiUrl = (endpoint: string): string => {
  // Eliminar la barra inicial del endpoint si existe
  const cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
  
  // Construir la URL completa
  return `http://${ip}:${portConfig.pharmacy}/api/${cleanEndpoint}`;
}; 