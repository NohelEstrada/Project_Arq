package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.ServiceApprovalDAO;
import com.sources.app.entities.ServiceApproval;
import com.sources.app.util.PharmacyClient;
import com.sources.app.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador HTTP para gestionar las interacciones entre el sistema de seguros y el sistema de farmacia (simulado).
 * Se encarga principalmente de validar las prescripciones contra una aprobación de servicio existente
 * y de consultar la información de cobertura asociada a una prescripción ya validada.
 * Utiliza un {@link ServiceApprovalDAO} para buscar y actualizar las aprobaciones de servicio.
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code POST /api/pharmacy-insurance/validate-prescription}: Valida una prescripción contra un código de aprobación,
 *       asocia la prescripción a la aprobación y calcula/guarda los montos cubiertos y a pagar por el paciente.</li>
 *   <li>{@code GET /api/pharmacy-insurance/check-coverage/{prescriptionId}}: Consulta la información de cobertura
 *       (montos, estado) para una prescripción específica que ya ha sido validada previamente.</li>
 * </ul>
 */
public class PharmacyInsuranceHandler implements HttpHandler {
    /** DAO para acceder a los datos de las aprobaciones de servicio. */
    private final ServiceApprovalDAO serviceApprovalDAO;
    /** ObjectMapper para la serialización/deserialización de objetos JSON. */
    private final ObjectMapper objectMapper;
    /** Ruta base para los endpoints gestionados por este manejador. */
    private static final String ENDPOINT = "/api/pharmacy-insurance";

    /**
     * Constructor para el manejador de integración farmacia-seguro.
     * Inicializa el DAO necesario para interactuar con las aprobaciones de servicio
     * y el ObjectMapper para el manejo de JSON.
     *
     * @param serviceApprovalDAO DAO para acceder a la entidad {@link ServiceApproval}.
     */
    public PharmacyInsuranceHandler(ServiceApprovalDAO serviceApprovalDAO) {
        this.serviceApprovalDAO = serviceApprovalDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes.
     * Configura las cabeceras CORS, maneja las solicitudes OPTIONS (preflight),
     * y enruta las solicitudes POST a `/validate-prescription` y GET a `/check-coverage/{id}`
     * a sus métodos de manejo correspondientes ({@link #handleValidatePrescription(HttpExchange)}
     * y {@link #handleCheckCoverage(HttpExchange, long)}).
     * Responde con 404 para rutas no reconocidas o 405 para métodos no permitidos en las rutas válidas.
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida durante el manejo.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers for all requests
        HibernateUtil.setCorsHeaders(exchange);
        
        // Handle OPTIONS requests (CORS preflight)
        if (exchange.getRequestMethod().equals("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        
        if (path.equals(ENDPOINT + "/validate-prescription")) {
            if (method.equals("POST")) {
                handleValidatePrescription(exchange);
            } else {
                sendMethodNotAllowed(exchange);
            }
        } else if (path.matches(ENDPOINT + "/check-coverage/\\d+")) {
            if (method.equals("GET")) {
                long prescriptionId = Long.parseLong(path.substring((ENDPOINT + "/check-coverage/").length()));
                handleCheckCoverage(exchange, prescriptionId);
            } else {
                sendMethodNotAllowed(exchange);
            }
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }
    
    /**
     * Maneja las solicitudes POST a {@code /api/pharmacy-insurance/validate-prescription}.
     * Lee el cuerpo JSON de la solicitud, esperando `approvalCode`, `prescriptionId`, y `totalAmount`.
     * Busca la {@link ServiceApproval} correspondiente por el `approvalCode`.
     * Realiza validaciones:
     * - Verifica que la aprobación exista.
     * - Verifica que la aprobación esté en estado "APPROVED".
     * - Verifica que la aprobación no tenga ya un `prescriptionId` asociado.
     * Si las validaciones son exitosas, actualiza la aprobación con el `prescriptionId` y el `totalAmount`,
     * calcula el monto cubierto por el seguro (ej. 70%) y el monto a pagar por el paciente,
     * actualiza estos montos en la aprobación y la guarda usando el DAO.
     * Responde con 200 y los detalles de la cobertura si la validación y actualización son exitosas,
     * o con 400/404/500 en caso de error.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo, interactuar con el DAO o enviar la respuesta.
     */
    private void handleValidatePrescription(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> data = objectMapper.readValue(requestBody, Map.class);
        
        // Validar datos requeridos
        if (!data.containsKey("approvalCode") || !data.containsKey("prescriptionId") || !data.containsKey("totalAmount")) {
            sendBadRequest(exchange, "Se requieren los campos: approvalCode, prescriptionId, totalAmount");
            return;
        }
        
        String approvalCode = (String) data.get("approvalCode");
        Long prescriptionId = Long.valueOf(data.get("prescriptionId").toString());
        Double totalAmount = Double.valueOf(data.get("totalAmount").toString());
        
        // Buscar la aprobación de servicio por su código
        ServiceApproval approval = serviceApprovalDAO.findByApprovalCode(approvalCode);
        
        if (approval == null) {
            sendNotFound(exchange, "Código de aprobación no encontrado");
            return;
        }
        
        // Verificar el estado de la aprobación
        if (!"APPROVED".equals(approval.getStatus())) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of(
                "success", false,
                "message", "El servicio no está en estado aprobado"
            )));
            return;
        }
        
        // Verificar si ya tiene una receta asociada
        if (approval.getPrescriptionId() != null) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of(
                "success", false,
                "message", "Este servicio ya tiene una receta asociada"
            )));
            return;
        }
        
        // Actualizar la aprobación con la información de la receta
        approval.setPrescriptionId(prescriptionId);
        approval.setPrescriptionTotal(totalAmount);
        
        // Calcular cuánto cubre el seguro (por ejemplo, 70% del total)
        double coveragePercentage = 0.7; // 70% de cobertura
        double coveredAmount = totalAmount * coveragePercentage;
        double patientAmount = totalAmount - coveredAmount;
        
        approval.setCoveredAmount(approval.getCoveredAmount() + coveredAmount);
        approval.setPatientAmount(approval.getPatientAmount() + patientAmount);
        
        // Guardar los cambios
        ServiceApproval updated = serviceApprovalDAO.update(approval);
        
        if (updated != null) {
            // Preparar respuesta para la farmacia
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("approvalCode", updated.getApprovalCode());
            response.put("prescriptionId", updated.getPrescriptionId());
            response.put("coveredAmount", coveredAmount);
            response.put("patientAmount", patientAmount);
            response.put("totalAmount", totalAmount);
            
            sendResponse(exchange, 200, objectMapper.writeValueAsString(response));
        } else {
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of(
                "success", false,
                "message", "Error al actualizar la aprobación del servicio"
            )));
        }
    }
    
    /**
     * Maneja las solicitudes GET a {@code /api/pharmacy-insurance/check-coverage/{prescriptionId}}.
     * Extrae el `prescriptionId` de la ruta.
     * Busca la {@link ServiceApproval} asociada a ese `prescriptionId` usando {@link ServiceApprovalDAO#findByPrescriptionId(long)}.
     * Si se encuentra la aprobación, responde con 200 y un JSON que contiene los detalles relevantes
     * de la cobertura (código de aprobación, ID de prescripción, montos cubierto/paciente/total, estado).
     * Si no se encuentra la aprobación, responde con 404.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param prescriptionId El ID de la prescripción extraído de la ruta.
     * @throws IOException Si ocurre un error al buscar en el DAO o al enviar la respuesta.
     */
    private void handleCheckCoverage(HttpExchange exchange, long prescriptionId) throws IOException {
        // Buscar la aprobación de servicio por el ID de receta
        ServiceApproval approval = serviceApprovalDAO.findByPrescriptionId(prescriptionId);
        
        if (approval == null) {
            sendNotFound(exchange, "No se encontró aprobación para esta receta");
            return;
        }
        
        // Preparar respuesta con la información de cobertura
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("approvalCode", approval.getApprovalCode());
        response.put("prescriptionId", approval.getPrescriptionId());
        response.put("coveredAmount", approval.getCoveredAmount());
        response.put("patientAmount", approval.getPatientAmount());
        response.put("totalAmount", approval.getPrescriptionTotal());
        response.put("status", approval.getStatus());
        
        sendResponse(exchange, 200, objectMapper.writeValueAsString(response));
    }
    
    /**
     * Envía una respuesta HTTP con código 405 (Method Not Allowed).
     * Utilizado cuando se recibe un método HTTP no soportado para un endpoint específico.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al enviar la cabecera de respuesta.
     */
    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, -1);
    }
    
    /**
     * Envía una respuesta HTTP con código 400 (Bad Request) y un mensaje de error JSON.
     * Utilizado para indicar que la solicitud del cliente es inválida (e.g., faltan campos requeridos).
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param message El mensaje de error descriptivo a incluir en la respuesta JSON.
     * @throws IOException Si ocurre un error al serializar o enviar la respuesta.
     */
    private void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of(
            "success", false,
            "message", message
        )));
    }
    
    /**
     * Envía una respuesta HTTP con código 404 (Not Found) y un mensaje de error JSON.
     * Utilizado para indicar que el recurso solicitado no pudo ser encontrado (e.g., aprobación no encontrada).
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param message El mensaje de error descriptivo a incluir en la respuesta JSON.
     * @throws IOException Si ocurre un error al serializar o enviar la respuesta.
     */
    private void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of(
            "success", false,
            "message", message
        )));
    }
    
    /**
     * Envía una respuesta HTTP genérica con un código de estado y un cuerpo de respuesta JSON.
     * Establece la cabecera "Content-Type" a "application/json".
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP para la respuesta (e.g., 200, 400, 404, 500).
     * @param response El cuerpo de la respuesta como una cadena JSON.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        // CORS headers already set in handle method
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
} 