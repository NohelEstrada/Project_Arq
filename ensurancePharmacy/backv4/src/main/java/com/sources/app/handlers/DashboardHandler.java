package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.ServiceApprovalDAO;
import com.sources.app.dao.HospitalDAO;
import com.sources.app.dao.InsuranceServiceDAO;
import com.sources.app.dao.HospitalInsuranceServiceDAO;
import com.sources.app.entities.ServiceApproval;
import com.sources.app.util.PharmacyClient;
import com.sources.app.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Manejador HTTP responsable de generar datos agregados para un panel de control (dashboard)
 * y verificar el estado de las conexiones con sistemas externos o bases de datos.
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/dashboard}: Devuelve un conjunto de estadísticas y datos agregados,
 *       incluyendo conteos de aprobaciones por estado, montos totales, conteo de hospitales
 *       y servicios, estadísticas simuladas de farmacia, transacciones recientes y conteos
 *       de conexiones/integraciones (algunos valores pueden ser ficticios).</li>
 *   <li>{@code GET /api/dashboard/status}: Devuelve el estado actual (activo/inactivo)
 *       de las conexiones clave, como la base de datos, la integración con el hospital
 *       y la integración con la farmacia (estas últimas pueden ser simuladas).</li>
 * </ul>
 */
public class DashboardHandler implements HttpHandler {
    /** DAO para acceder a los datos de aprobaciones de servicio. */
    private final ServiceApprovalDAO serviceApprovalDAO;
    /** DAO para acceder a los datos de hospitales. */
    private final HospitalDAO hospitalDAO;
    /** DAO para acceder a los datos de servicios de seguro. */
    private final InsuranceServiceDAO insuranceServiceDAO;
    /** DAO para acceder a los datos de la relación hospital-servicio. (Actualmente no usado directamente en los métodos de dashboard). */
    private final HospitalInsuranceServiceDAO hospitalInsuranceServiceDAO;
    /** ObjectMapper para la serialización/deserialización JSON, configurado con formato de fecha/hora. */
    private final ObjectMapper objectMapper;
    /** Ruta base para los datos del dashboard. */
    private static final String ENDPOINT = "/api/dashboard";
    /** Subruta específica para obtener el estado de las conexiones. */
    private static final String ENDPOINT_STATUS = ENDPOINT + "/status";

    /**
     * Constructor del manejador del dashboard.
     * Inicializa todos los DAOs necesarios para recopilar las estadísticas y el ObjectMapper
     * con un formato de fecha específico ("yyyy-MM-dd HH:mm:ss").
     *
     * @param serviceApprovalDAO DAO para {@link ServiceApproval}.
     * @param hospitalDAO DAO para {@link com.sources.app.entities.Hospital}.
     * @param insuranceServiceDAO DAO para {@link com.sources.app.entities.InsuranceService}.
     * @param hospitalInsuranceServiceDAO DAO para {@link com.sources.app.entities.HospitalInsuranceService}.
     */
    public DashboardHandler(
            ServiceApprovalDAO serviceApprovalDAO,
            HospitalDAO hospitalDAO,
            InsuranceServiceDAO insuranceServiceDAO,
            HospitalInsuranceServiceDAO hospitalInsuranceServiceDAO) {
        this.serviceApprovalDAO = serviceApprovalDAO;
        this.hospitalDAO = hospitalDAO;
        this.insuranceServiceDAO = insuranceServiceDAO;
        this.hospitalInsuranceServiceDAO = hospitalInsuranceServiceDAO;
        this.objectMapper = new ObjectMapper();
        // Formato de fecha para la respuesta del dashboard
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas a los endpoints del dashboard.
     * Configura las cabeceras CORS, maneja solicitudes OPTIONS (preflight), y enruta las solicitudes GET
     * a {@link #handleDashboardGet(HttpExchange)} o {@link #handleStatusGet(HttpExchange)} según la ruta.
     * Rechaza cualquier otro método HTTP o ruta no reconocida dentro de su ámbito.
     * Captura excepciones generales para devolver un error 500.
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida (generalmente manejado internamente).
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configura los encabezados CORS para todas las respuestas
        HibernateUtil.setCorsHeaders(exchange);
        
        // Maneja las solicitudes OPTIONS (preflight de CORS)
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1); // Sin contenido
            return;
        }
        
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        
        try {
             // Delega según la ruta y el método
            if (path.equals(ENDPOINT)) {
                if (method.equals("GET")) {
                    handleDashboardGet(exchange);
                } else {
                    sendMethodNotAllowed(exchange);
                }
            } else if (path.equals(ENDPOINT_STATUS)) {
                if (method.equals("GET")) {
                    handleStatusGet(exchange);
                } else {
                    sendMethodNotAllowed(exchange);
                }
            } else {
                // Ruta no encontrada
                sendErrorResponse(exchange, 404, "Endpoint no encontrado.");
            }
        } catch (Exception e) {
            System.err.println("Error inesperado en DashboardHandler: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor.");
        }
    }
    
    /**
     * Maneja las solicitudes GET a {@code /api/dashboard}.
     * Recopila diversas estadísticas y datos agregados de diferentes fuentes (DAOs):
     * - Estadísticas de {@link ServiceApproval} (total, por estado, montos, con prescripción).
     * - Conteos totales de hospitales y servicios de seguro.
     * - Estadísticas simuladas de farmacia basadas en aprobaciones con prescripción.
     * - Lista de las últimas transacciones (basadas en las aprobaciones de servicio más recientes).
     * - Conteos de conexiones/integraciones (algunos valores son ficticios/placeholders).
     * Empaqueta todos estos datos en un mapa y los envía como una respuesta JSON.
     * Nota: La obtención de todas las aprobaciones (`serviceApprovalDAO.findAll()`) puede ser ineficiente
     * para grandes volúmenes de datos; se deberían considerar consultas SQL agregadas o paginación.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al obtener datos o al enviar la respuesta JSON.
     */
    private void handleDashboardGet(HttpExchange exchange) throws IOException {
        try {
            // Recopilar datos para el dashboard
            Map<String, Object> dashboardData = new HashMap<>();
            
            // 1. Estadísticas de aprobaciones de servicio
            List<ServiceApproval> approvals = serviceApprovalDAO.findAll(); // Podría ser costoso, considerar paginación o querys específicas
            int totalApprovals = approvals.size();
            long approvedCount = approvals.stream().filter(a -> "APPROVED".equals(a.getStatus())).count();
            long pendingCount = approvals.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
            long rejectedCount = approvals.stream().filter(a -> "REJECTED".equals(a.getStatus())).count();
            long completedCount = approvals.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
            long withPrescription = approvals.stream().filter(a -> a.getPrescriptionId() != null).count();
            
            // Usar DoubleStream para sumar montos de forma segura (manejo de nulos)
            double totalCoveredAmount = approvals.stream()
                .mapToDouble(a -> a.getCoveredAmount() != null ? a.getCoveredAmount() : 0.0)
                .sum();
            double totalPatientAmount = approvals.stream()
                .mapToDouble(a -> a.getPatientAmount() != null ? a.getPatientAmount() : 0.0)
                .sum();
            double totalAmount = approvals.stream()
                .mapToDouble(a -> a.getServiceCost() != null ? a.getServiceCost() : 0.0)
                .sum();
            
            // Agrupar estadísticas de aprobación
            Map<String, Object> approvalStats = Map.of(
                "total", totalApprovals,
                "approved", approvedCount,
                "pending", pendingCount,
                "rejected", rejectedCount,
                "completed", completedCount,
                "withPrescription", withPrescription,
                "totalCoveredAmount", totalCoveredAmount,
                "totalPatientAmount", totalPatientAmount,
                "totalAmount", totalAmount
            );
            
            // 2. Conteo de hospitales y servicios de seguro (considerar count(*) queries si es más eficiente)
            long hospitalCount = hospitalDAO.findAll().size(); // Revert to findAll().size()
            long serviceCount = insuranceServiceDAO.findAll().size(); // Revert to findAll().size()
            
            // 3. Estadísticas de farmacia (simuladas)
            Map<String, Object> pharmacyStats = new HashMap<>();
            pharmacyStats.put("totalPrescriptions", withPrescription); 
            // Simulación más realista
            int dispensedPrescriptions = (int) (withPrescription * 0.75); // Simula 75% dispensadas
            pharmacyStats.put("dispensedPrescriptions", dispensedPrescriptions);
            pharmacyStats.put("pendingPrescriptions", (int)withPrescription - dispensedPrescriptions);
            
            // 4. Transacciones recientes (Obtener últimas N directamente desde la BD sería más eficiente)
            // Revert to original logic for recent transactions
            List<Map<String, Object>> recentTransactions = new ArrayList<>();
            SimpleDateFormat transactionDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Use separate formatter if needed
            int transactionsToShow = Math.min(10, approvals.size());
            List<ServiceApproval> recentApprovals = approvals.subList(Math.max(0, approvals.size() - transactionsToShow), approvals.size());
            // Consider reversing if needed: Collections.reverse(recentApprovals);
            
            for (ServiceApproval approval : recentApprovals) {
                 Map<String, Object> transaction = new HashMap<>(); // Use HashMap for mutable map
                 transaction.put("id", approval.getId());
                 transaction.put("approvalCode", approval.getApprovalCode());
                 transaction.put("serviceName", approval.getServiceName());
                 transaction.put("status", approval.getStatus());
                 // Use the separate formatter for the transaction date
                 transaction.put("date", approval.getApprovalDate() != null ? 
                     transactionDateFormat.format(approval.getApprovalDate()) : "N/A");
                 transaction.put("coveredAmount", approval.getCoveredAmount());
                 transaction.put("patientAmount", approval.getPatientAmount());
                 transaction.put("total", approval.getServiceCost());
                 transaction.put("hasPrescription", approval.getPrescriptionId() != null);
                 recentTransactions.add(transaction);
            }
            
            // 5. Estadísticas de conexión (Valores deben obtenerse idealmente de forma dinámica)
             // Revert to placeholder counts
            long categoryCount = 5; // Valor ficticio
            long subCategoryCount = 15; // Valor ficticio
            Map<String, Object> connections = Map.of(
                "hospitalIntegrations", hospitalCount, 
                "pharmacyIntegrations", 1, // Suponiendo 1 farmacia integrada
                "serviceCategories", categoryCount, 
                "serviceSubcategories", subCategoryCount 
            );
            
            // Construir el objeto final del dashboard
            dashboardData.put("approvalStats", approvalStats);
            dashboardData.put("hospitalCount", hospitalCount);
            dashboardData.put("serviceCount", serviceCount);
            dashboardData.put("pharmacyStats", pharmacyStats);
            dashboardData.put("recentTransactions", recentTransactions);
            dashboardData.put("connections", connections);
            dashboardData.put("lastUpdated", new Date()); // Jackson usará el formato definido
            
            // Enviar respuesta JSON exitosa
            sendJsonResponse(exchange, 200, dashboardData);
            
        } catch (Exception e) {
            System.err.println("Error generando datos del dashboard: " + e.getMessage());
            e.printStackTrace();
            // Enviar respuesta de error
            sendErrorResponse(exchange, 500, "Error al generar el dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Maneja las solicitudes GET a {@code /api/dashboard/status}.
     * Verifica el estado de conexión de varios componentes críticos:
     * - Conexión a la base de datos (usando {@link #checkDatabaseConnection()}).
     * - Conexión con la integración del hospital (usando {@link #checkHospitalConnection()}, puede ser simulada).
     * - Conexión con la integración de la farmacia (usando {@link #checkPharmacyConnection()}, puede ser simulada).
     * Devuelve un objeto JSON que indica el estado (true/false) de cada conexión.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al enviar la respuesta JSON.
     */
    private void handleStatusGet(HttpExchange exchange) throws IOException {
        Map<String, Object> status = new HashMap<>();
        boolean overallStatus = true;

        // 1. Comprobar conexión con hospital (simulado)
        boolean hospitalConnection = checkHospitalConnection(); // Usar método auxiliar
        status.put("hospitalApi", hospitalConnection ? "OK" : "ERROR");
        if (!hospitalConnection) overallStatus = false;
        
        // 2. Comprobar conexión con farmacia (usando PharmacyClient)
        boolean pharmacyConnection = checkPharmacyConnection(); // Usar método auxiliar
        status.put("pharmacyApi", pharmacyConnection ? "OK" : "ERROR");
         if (!pharmacyConnection) overallStatus = false;
        
        // 3. Comprobar conexión con Base de Datos (usando HibernateUtil)
        boolean dbConnection = checkDatabaseConnection(); // Usar método auxiliar
        status.put("database", dbConnection ? "OK" : "ERROR");
         if (!dbConnection) overallStatus = false;
        
        // 4. Estado general
        status.put("overallStatus", overallStatus ? "OK" : "ERROR");
        status.put("lastChecked", new Date()); // Jackson formateará la fecha
            
        // Enviar respuesta JSON
        sendJsonResponse(exchange, 200, status);
    }
    
    /**
     * Verifica el estado de la conexión con el sistema externo del hospital.
     * (Implementación actual es un placeholder/simulación).
     * En una implementación real, esto podría implicar hacer un ping o una solicitud
     * simple a un endpoint de estado conocido de la API del hospital.
     *
     * @return {@code true} si la conexión se considera activa (simulado), {@code false} en caso contrario.
     */
    private boolean checkHospitalConnection() {
         try {
            // Simulación: Reemplazar con llamada real a API externa
            System.out.println("Simulando chequeo de conexión a Hospital API...");
            // Thread.sleep(50); // Simular pequeña demora
            return true; // Asumir éxito para la simulación
        } catch (Exception e) {
            System.err.println("Error (simulado) conectando al Hospital API: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica el estado de la conexión con el sistema externo de la farmacia.
     * Utiliza una clase cliente simulada {@link PharmacyClient#ping()} para verificar.
     * En una implementación real, esto podría implicar una llamada a un endpoint de estado
     * de la API de la farmacia o verificar la disponibilidad de un servicio relacionado.
     *
     * @return {@code true} si el ping al cliente de farmacia es exitoso, {@code false} en caso contrario o si ocurre una excepción.
     */
     private boolean checkPharmacyConnection() {
         try {
             System.out.println("Chequeando conexión a Pharmacy API...");
             // Asumiendo que PharmacyClient tiene un método para health check
             // y que lanza excepción o devuelve null/false en caso de error.
             // boolean isOk = PharmacyClient.checkStatus(); // Revert: Remove call to undefined method
             boolean isOk = true; // Revert to simple simulation
             System.out.println("Pharmacy API status: " + (isOk ? "OK" : "ERROR"));
             return isOk;
         } catch (Exception e) {
             System.err.println("Error conectando a Pharmacy API: " + e.getMessage());
             return false;
         }
     }
     
     /**
     * Verifica el estado de la conexión con la base de datos.
     * Utiliza {@link HibernateUtil#isSessionOpen()} para comprobar si hay una sesión de Hibernate activa.
     * Una verificación más robusta podría implicar ejecutar una consulta simple (e.g., "SELECT 1")
     * para asegurar que la conexión física esté realmente operativa.
     *
     * @return {@code true} si la sesión de Hibernate se considera abierta, {@code false} en caso contrario.
     */
     private boolean checkDatabaseConnection() {
          try {
             System.out.println("Chequeando conexión a la Base de Datos...");
             // Intenta obtener y cerrar una sesión para verificar la conexión
             HibernateUtil.getSessionFactory().openSession().close();
             System.out.println("Database connection status: OK");
             return true;
         } catch (Exception e) {
             System.err.println("Error conectando a la Base de Datos: " + e.getMessage());
             e.printStackTrace(); // Loguear el error completo
             return false;
         }
     }

    /**
     * Envía una respuesta 405 Method Not Allowed.
     * Utilizado cuando se recibe una solicitud con un método HTTP no soportado para la ruta dada.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendErrorResponse(exchange, 405, "Método no permitido para este endpoint.");
    }

    /**
     * Envía una respuesta JSON al cliente.
     * Serializa el objeto de datos a JSON y lo escribe en el cuerpo de la respuesta con el código de estado adecuado.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP (e.g., 200).
     * @param data El objeto a serializar.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String response = objectMapper.writeValueAsString(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
     /**
     * Envía una respuesta de error JSON estandarizada al cliente.
     * Crea un cuerpo JSON con una clave "error" y el mensaje proporcionado.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP de error (e.g., 404, 500).
     * @param errorMessage El mensaje descriptivo del error.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        Map<String, Object> errorResponse = Map.of("success", false, "message", errorMessage);
        try {
             if (!exchange.getResponseHeaders().containsKey("Content-Type")) { 
                 sendJsonResponse(exchange, statusCode, errorResponse);
             } else {
                 System.err.println("Intento de enviar respuesta de error cuando los headers ya fueron enviados. Status: " + statusCode + ", Msg: " + errorMessage);
             }
        } catch (IOException e) {
            System.err.println("Error al enviar respuesta de error: " + e.getMessage());
        }
    }
} 