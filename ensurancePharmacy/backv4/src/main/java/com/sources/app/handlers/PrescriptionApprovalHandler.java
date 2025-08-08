package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.ConfigurableAmountDAO;
import com.sources.app.dao.PrescriptionApprovalDAO;
import com.sources.app.dao.UserDAO;
import com.sources.app.entities.ConfigurableAmount;
import com.sources.app.entities.PrescriptionApproval;
import com.sources.app.entities.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manejador HTTP para procesar solicitudes de aprobación de prescripciones médicas.
 * Evalúa una prescripción basada en criterios como el estado de pago del usuario
 * y el costo de la prescripción en comparación con un monto mínimo configurable.
 * Guarda el resultado de la aprobación (Aprobado/Rechazado) con un número de autorización o motivo de rechazo.
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code POST /api/prescriptions/approve}: Procesa una solicitud de aprobación. Espera un cuerpo JSON con
 *       `userId`, `totalCost`, `prescriptionIdHospital`, y `details`.</li>
 *   <li>{@code GET /api/prescriptions/approvals}: Obtiene la lista de todas las aprobaciones procesadas.</li>
 *   <li>{@code GET /api/prescriptions/approvals?userId={id}}: Obtiene la lista de aprobaciones para un usuario específico.</li>
 * </ul>
 */
public class PrescriptionApprovalHandler implements HttpHandler {

    /** DAO para guardar/leer los registros de aprobación de prescripciones. */
    private final PrescriptionApprovalDAO approvalDAO;
    /** DAO para obtener información del usuario (e.g., estado de pago). */
    private final UserDAO userDAO;
    /** DAO para obtener el monto mínimo configurable para la aprobación. */
    private final ConfigurableAmountDAO configDAO;
    /** ObjectMapper para la serialización/deserialización JSON. */
    private final ObjectMapper objectMapper;

    /**
     * Constructor del manejador de aprobación de prescripciones.
     * Inicializa los DAOs necesarios (PrescriptionApproval, User, ConfigurableAmount)
     * y el ObjectMapper, configurando un formato de fecha/hora específico.
     *
     * @param approvalDAO DAO para {@link PrescriptionApproval}.
     * @param userDAO DAO para {@link User}.
     * @param configDAO DAO para {@link ConfigurableAmount}.
     */
    public PrescriptionApprovalHandler(PrescriptionApprovalDAO approvalDAO, UserDAO userDAO, ConfigurableAmountDAO configDAO) {
        this.approvalDAO = approvalDAO;
        this.userDAO = userDAO;
        this.configDAO = configDAO;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas a los endpoints de aprobación.
     * Configura las cabeceras CORS, maneja solicitudes OPTIONS (preflight), y enruta las solicitudes
     * GET y POST a los métodos de manejo apropiados basados en la ruta exacta.
     * Rechaza cualquier otra ruta o método no soportado.
     * Captura excepciones generales para devolver un error 500.
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida (generalmente manejado internamente).
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configurar CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No content for OPTIONS
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/prescriptions/approve") && method.equalsIgnoreCase("POST")) {
                handlePrescriptionApprovalRequest(exchange);
            } else if (path.equals("/api/prescriptions/approvals") && method.equalsIgnoreCase("GET")) {
                handleGetApprovals(exchange);
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Maneja las solicitudes GET a {@code /api/prescriptions/approvals}.
     * Si se proporciona el parámetro de consulta `userId`, filtra las aprobaciones para ese usuario
     * usando {@link PrescriptionApprovalDAO#findByUserId(Long)}.
     * Si no se proporciona `userId` o es inválido, obtiene todas las aprobaciones usando {@link PrescriptionApprovalDAO#findAll()}.
     * Envía la lista de aprobaciones resultante como JSON.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al obtener datos o al enviar la respuesta.
     */
    private void handleGetApprovals(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        List<PrescriptionApproval> approvals;
        
        if (query != null && query.contains("userId=")) {
             Map<String, String> params = parseQuery(query);
             try {
                 Long userId = Long.parseLong(params.get("userId"));
                 approvals = approvalDAO.findByUserId(userId);
             } catch (NumberFormatException e) {
                 exchange.sendResponseHeaders(400, -1); // Bad Request
                 return;
             }
        } else {
            approvals = approvalDAO.findAll();
        }

        String jsonResponse = objectMapper.writeValueAsString(approvals);
        sendJsonResponse(exchange, 200, jsonResponse);
    }

    /**
     * Maneja las solicitudes POST a {@code /api/prescriptions/approve}.
     * Lee los datos de la solicitud (userId, totalCost, prescriptionIdHospital, details) del cuerpo JSON.
     * Realiza una serie de validaciones:
     * 1. Verifica si el usuario existe.
     * 2. Verifica si el usuario tiene el servicio pagado activo.
     * 3. Compara el costo de la prescripción con el monto mínimo configurable.
     * Si alguna validación falla, llama a {@link #rejectApproval(PrescriptionApproval, String)} y envía una respuesta de error (404 o 400).
     * Si todas las validaciones pasan, llama a {@link #approvePrescription(PrescriptionApproval)} y envía una respuesta de éxito (200)
     * con el número de autorización generado.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo, interactuar con los DAOs o enviar la respuesta.
     */
    private void handlePrescriptionApprovalRequest(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        Map<String, Object> data = objectMapper.readValue(requestBody, Map.class);

        Long userId = Long.valueOf(data.get("userId").toString());
        Double prescriptionCost = Double.valueOf(data.get("totalCost").toString());
        String prescriptionIdHospital = (String) data.get("prescriptionIdHospital");
        String details = (String) data.get("details"); // Asumimos que los detalles vienen como JSON o texto

        User user = userDAO.findById(userId);
        ConfigurableAmount config = configDAO.findCurrentConfig();
        BigDecimal minimumAmount = config.getPrescriptionAmount();

        PrescriptionApproval approval = new PrescriptionApproval();
        approval.setIdUser(userId);
        approval.setPrescriptionIdHospital(prescriptionIdHospital);
        approval.setPrescriptionDetails(details);
        approval.setPrescriptionCost(prescriptionCost);

        // 1. Verificar si el usuario existe
        if (user == null) {
            rejectApproval(approval, "User not found");
            sendJsonResponse(exchange, 404, "{\"error\": \"User not found\", \"status\": \"Rejected\"}");
            return;
        }

        // 2. Verificar si el usuario tiene servicio pagado
        if (user.getPaidService() == null || !user.getPaidService()) {
            rejectApproval(approval, "Client coverage inactive");
            sendJsonResponse(exchange, 400, "{\"error\": \"Client coverage inactive\", \"status\": \"Rejected\"}");
            return;
        }

        // 3. Verificar si el costo es mayor o igual al mínimo configurado
        if (BigDecimal.valueOf(prescriptionCost).compareTo(minimumAmount) < 0) {
            String reason = String.format("Prescription cost (Q%.2f) below minimum threshold (Q%.2f)", 
                                        prescriptionCost, minimumAmount);
            rejectApproval(approval, reason);
            sendJsonResponse(exchange, 400, 
                String.format("{\"error\": \"%s\", \"status\": \"Rejected\"}", reason));
            return;
        }

        // 4. Aprobar la receta
        approvePrescription(approval);
        sendJsonResponse(exchange, 200, 
            String.format("{\"authorizationNumber\": \"%s\", \"status\": \"Approved\"}", 
                          approval.getAuthorizationNumber()));
    }

    /**
     * Marca una solicitud de aprobación como rechazada y la guarda en la base de datos.
     * Establece el estado a "REJECTED", asigna el motivo del rechazo y genera un número
     * de referencia único (prefijo "N/A-") para el registro.
     *
     * @param approval El objeto {@link PrescriptionApproval} a rechazar.
     * @param reason La razón textual por la cual la prescripción fue rechazada.
     */
    private void rejectApproval(PrescriptionApproval approval, String reason) {
        approval.setStatus("REJECTED");
        approval.setRejectionReason(reason);
        approval.setAuthorizationNumber("N/A-" + UUID.randomUUID().toString().substring(0, 8)); // Generar un ID único para rechazos
        approvalDAO.save(approval);
    }

    /**
     * Marca una solicitud de aprobación como aprobada y la guarda en la base de datos.
     * Establece el estado a "APPROVED", elimina cualquier motivo de rechazo previo y genera
     * un número de autorización único usando {@link #generateAuthorizationNumber()}.
     *
     * @param approval El objeto {@link PrescriptionApproval} a aprobar.
     */
    private void approvePrescription(PrescriptionApproval approval) {
        approval.setStatus("APPROVED");
        approval.setRejectionReason(null);
        approval.setAuthorizationNumber(generateAuthorizationNumber());
        approvalDAO.save(approval);
    }

    /**
     * Genera un número de autorización único para las prescripciones aprobadas.
     * Utiliza un prefijo "AUTH-" seguido de una porción de un UUID en mayúsculas.
     *
     * @return Una cadena que representa el número de autorización generado.
     */
    private String generateAuthorizationNumber() {
        // Genera un número de autorización único (ejemplo simple)
        return "AUTH-" + UUID.randomUUID().toString().toUpperCase().substring(0, 12);
    }

    /**
     * Lee el cuerpo completo de una solicitud HTTP y lo devuelve como una cadena UTF-8.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @return El cuerpo de la solicitud como String.
     * @throws IOException Si ocurre un error durante la lectura del InputStream.
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            return new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Envía una respuesta JSON al cliente.
     * Escribe la cadena JSON proporcionada en el cuerpo de la respuesta con el código de estado HTTP especificado.
     * Establece el tipo de contenido a "application/json".
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP para la respuesta (e.g., 200, 400, 404).
     * @param body La cadena JSON que representa el cuerpo de la respuesta.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    /**
     * Parsea los parámetros de una cadena de consulta (query string) de una URL en un mapa de clave-valor.
     * Maneja claves sin valor y parámetros mal formados. Ignora claves duplicadas, manteniendo la primera aparición.
     *
     * @param query La cadena de consulta (ej: "userId=123"). Puede ser {@code null} o vacía.
     * @return Un {@link Map} que contiene los parámetros de consulta. Devuelve un mapa vacío si la consulta es nula, vacía o no contiene parámetros válidos.
     */
     private Map<String, String> parseQuery(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .collect(Collectors.toMap(
                        param -> param[0],
                        param -> param.length > 1 ? param[1] : ""
                ));
    }
} 