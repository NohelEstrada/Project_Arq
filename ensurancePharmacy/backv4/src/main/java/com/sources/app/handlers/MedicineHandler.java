package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.MedicineDAO;
import com.sources.app.entities.Medicine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manejador HTTP para las operaciones CRUD (Crear, Leer, Actualizar, Eliminar - aunque DELETE no está implementado)
 * sobre la entidad {@link Medicine}.
 * Gestiona las solicitudes para el endpoint "/api/medicine".
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/medicine}: Obtiene todos los medicamentos.</li>
 *   <li>{@code GET /api/medicine?id={id}}: Obtiene un medicamento específico por su ID.</li>
 *   <li>{@code POST /api/medicine}: Crea un nuevo medicamento.</li>
 *   <li>{@code PUT /api/medicine}: Actualiza un medicamento existente (requiere 'idMedicine' en el cuerpo).</li>
 * </ul>
 */
public class MedicineHandler implements HttpHandler {

    /** DAO para acceder a los datos de la entidad Medicine. */
    private final MedicineDAO medicineDAO;
    /** ObjectMapper para la serialización/deserialización JSON. Configurado con formato de fecha yyyy-MM-dd. */
    private final ObjectMapper objectMapper;
    /** Ruta base para las solicitudes gestionadas por este manejador. */
    private static final String ENDPOINT = "/api/medicine";
    /** Logger para registrar eventos y errores de este manejador. */
    private static final Logger LOGGER = Logger.getLogger(MedicineHandler.class.getName());

    /**
     * Constructor del manejador de medicamentos.
     * Inicializa el DAO de medicamentos y el ObjectMapper con un formato de fecha específico.
     *
     * @param medicineDAO El DAO para interactuar con la tabla de medicamentos.
     */
    public MedicineHandler(MedicineDAO medicineDAO) {
        this.medicineDAO = medicineDAO;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas al endpoint de medicamentos.
     * Configura las cabeceras CORS para permitir solicitudes desde cualquier origen y los métodos HTTP comunes.
     * Maneja las solicitudes OPTIONS (preflight) y enruta las solicitudes GET, POST y PUT
     * a sus respectivos métodos de manejo. Cualquier otro método resulta en un error 405 (Method Not Allowed).
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida durante el manejo de la solicitud.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Preflight (solicitud OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            // Verificar endpoint
            String path = exchange.getRequestURI().getPath();
            if (!path.equalsIgnoreCase(ENDPOINT)) {
                sendErrorResponse(exchange, 404, "Not Found");
                return;
            }

            // Enrutamiento según método HTTP
            switch (exchange.getRequestMethod().toUpperCase()) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "PUT":
                    handlePut(exchange);
                    break;
                default:
                    sendErrorResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado en el manejador de Medicine: " + e.getMessage(), e);
            sendErrorResponse(exchange, 500, "Internal Server Error");
        } finally {
            exchange.close();
        }
    }

    /**
     * Maneja las solicitudes GET a {@code /api/medicine}.
     * Si se proporciona un parámetro de consulta 'id', intenta obtener el medicamento específico por ese ID.
     * Si no se proporciona 'id', obtiene y devuelve la lista de todos los medicamentos.
     * Responde con 404 si el medicamento solicitado por ID no se encuentra.
     * Responde con 400 si el parámetro 'id' es inválido.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        try {
            // Busca por ID si el parámetro está presente
            if (query != null && query.contains("id=")) {
                Map<String, String> params = parseQuery(query);
                String idParam = params.get("id");
                 if (idParam == null || idParam.isEmpty()) {
                    sendErrorResponse(exchange, 400, "Missing or invalid 'id' parameter");
                    return;
                }
                Long id = Long.parseLong(idParam);
                Medicine medicine = medicineDAO.findById(id);
                if (medicine != null) {
                    // Medicamento encontrado, enviar respuesta JSON
                    sendJsonResponse(exchange, 200, medicine);
                } else {
                    // Medicamento no encontrado
                    sendErrorResponse(exchange, 404, "Medicine not found");
                }
            } else {
                // Obtener todos los medicamentos
                List<Medicine> list = medicineDAO.findAll();
                sendJsonResponse(exchange, 200, list);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID de medicamento inválido: " + query, e);
            sendErrorResponse(exchange, 400, "Invalid 'id' parameter format");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al procesar GET de medicamento: " + e.getMessage(), e);
            sendErrorResponse(exchange, 500, "Internal Server Error");
        }
    }

    /**
     * Maneja las solicitudes POST a {@code /api/medicine}.
     * Crea un nuevo medicamento basado en el cuerpo JSON de la solicitud.
     * El cuerpo JSON debe contener los campos requeridos: 'name', 'price', 'pharmacy' (con 'idPharmacy'), 'coverage', y 'stock'.
     * Campos como 'description', 'enabled', 'activePrinciple', 'presentation', y 'brand' son opcionales.
     * Si 'enabled' no se proporciona, se asume 1 (activo).
     * Responde con 201 (Created) y el objeto del medicamento creado si tiene éxito.
     * Responde con 400 si faltan campos requeridos o el JSON es inválido.
     * Responde con 500 si ocurre un error al interactuar con la base de datos.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            // Lee y parsea el cuerpo de la solicitud
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Medicine medicine = objectMapper.readValue(requestBody, Medicine.class);

            // Validaciones básicas
            if (medicine.getName() == null || medicine.getName().trim().isEmpty() ||
                medicine.getPrice() == null ||
                medicine.getPharmacy() == null || medicine.getPharmacy().getIdPharmacy() == null ||
                medicine.getCoverage() == null || medicine.getStock() == null) {
                LOGGER.warning("Datos incompletos para crear medicamento: " + requestBody);
                sendErrorResponse(exchange, 400, "Missing required fields for medicine creation");
                return;
            }

            // Crea el medicamento en la base de datos
            Medicine created = medicineDAO.create(
                    medicine.getName(),
                    medicine.getDescription(),
                    medicine.getPrice(),
                    medicine.getPharmacy(), // Se pasa el objeto Pharmacy completo
                    medicine.getEnabled() != null ? medicine.getEnabled() : 1, // Default enabled to 1 (true) if null
                    medicine.getActivePrinciple(),
                    medicine.getPresentation(),
                    medicine.getStock(),
                    medicine.getBrand(),
                    medicine.getCoverage()
            );

            if (created != null) {
                LOGGER.info("Medicamento creado con ID: " + created.getIdMedicine());
                sendJsonResponse(exchange, 201, created); // 201 Created
            } else {
                LOGGER.severe("Error en DAO al crear medicamento con datos: " + requestBody);
                sendErrorResponse(exchange, 500, "Failed to create medicine");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
             LOGGER.log(Level.WARNING, "Error al parsear JSON en POST de medicamento: " + e.getMessage(), e);
             sendErrorResponse(exchange, 400, "Invalid JSON format");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Excepción al procesar POST de medicamento: " + e.getMessage(), e);
            sendErrorResponse(exchange, 500, "Internal Server Error");
        }
    }

    /**
     * Maneja las solicitudes PUT a {@code /api/medicine}.
     * Actualiza un medicamento existente basado en el cuerpo JSON de la solicitud.
     * El cuerpo JSON *debe* contener 'idMedicine' para identificar el medicamento a actualizar.
     * También debe incluir 'pharmacy' con 'idPharmacy'.
     * Todos los demás campos son opcionales y solo se actualizarán si están presentes en el JSON.
     * Si 'enabled' no se proporciona, se asume 1 (activo).
     * Responde con 200 (OK) y el objeto del medicamento actualizado si tiene éxito.
     * Responde con 400 si falta 'idMedicine' o 'pharmacy' o si el JSON es inválido.
     * Responde con 404 si el 'idMedicine' proporcionado no corresponde a un medicamento existente.
     * Responde con 500 si ocurre un error durante la actualización en la base de datos.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        try {
            // Lee y parsea el cuerpo de la solicitud
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Medicine medicine = objectMapper.readValue(requestBody, Medicine.class);

            // Validar que el ID esté presente para actualizar
            if (medicine.getIdMedicine() == null) {
                 LOGGER.warning("Falta ID para actualizar medicamento: " + requestBody);
                 sendErrorResponse(exchange, 400, "Missing 'idMedicine' for update");
                 return;
            }
            // Validación adicional (ej: pharmacy no nula)
             if (medicine.getPharmacy() == null || medicine.getPharmacy().getIdPharmacy() == null) {
                 LOGGER.warning("Falta información de farmacia para actualizar medicamento: " + requestBody);
                sendErrorResponse(exchange, 400, "Missing pharmacy information for update");
                return;
            }
             // Asegurar que el estado enabled tenga un valor por defecto si no se provee
            if (medicine.getEnabled() == null) {
                 medicine.setEnabled(1); // Default to 1 (true)
            }


            // Actualiza el medicamento en la base de datos
            Medicine updated = medicineDAO.update(medicine);
            if (updated != null) {
                LOGGER.info("Medicamento actualizado con ID: " + updated.getIdMedicine());
                sendJsonResponse(exchange, 200, updated);
            } else {
                 LOGGER.severe("Error en DAO al actualizar medicamento con ID: " + medicine.getIdMedicine());
                // Podríamos verificar si el medicamento existe primero para dar 404
                Medicine existing = medicineDAO.findById(medicine.getIdMedicine());
                if (existing == null) {
                   sendErrorResponse(exchange, 404, "Medicine not found for update");
                } else {
                   sendErrorResponse(exchange, 500, "Failed to update medicine");
                }
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
             LOGGER.log(Level.WARNING, "Error al parsear JSON en PUT de medicamento: " + e.getMessage(), e);
             sendErrorResponse(exchange, 400, "Invalid JSON format");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Excepción al procesar PUT de medicamento: " + e.getMessage(), e);
            sendErrorResponse(exchange, 500, "Internal Server Error");
        }
    }

    /**
     * Envía una respuesta JSON al cliente.
     * Serializa el objeto de datos proporcionado a JSON y lo escribe en el cuerpo de la respuesta
     * con el código de estado HTTP especificado y el tipo de contenido "application/json; charset=UTF-8".
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP para la respuesta (e.g., 200, 201, 400).
     * @param data El objeto a serializar como JSON (puede ser una lista, mapa, entidad, etc.).
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        String jsonResponse = objectMapper.writeValueAsString(data);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
    * Envía una respuesta de error JSON estandarizada al cliente.
    * Crea un objeto JSON con una clave "error" que contiene el mensaje proporcionado.
    *
    * @param exchange El objeto {@link HttpExchange}.
    * @param statusCode El código de estado HTTP de error (e.g., 400, 404, 500).
    * @param message El mensaje de error descriptivo a incluir en la respuesta.
    * @throws IOException Si ocurre un error al escribir la respuesta.
    */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
       exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
       Map<String, String> errorResponse = Map.of("error", message);
       String jsonResponse = objectMapper.writeValueAsString(errorResponse);
       byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
       exchange.sendResponseHeaders(statusCode, responseBytes.length);
       try (OutputStream os = exchange.getResponseBody()) {
           os.write(responseBytes);
       }
    }


    /**
     * Parsea los parámetros de una cadena de consulta (query string) de una URL en un mapa de clave-valor.
     * Maneja claves sin valor y parámetros mal formados. Ignora claves duplicadas, manteniendo la primera aparición.
     *
     * @param query La cadena de consulta (ej: "id=1&name=test"). Puede ser {@code null} o vacía.
     * @return Un {@link Map} que contiene los parámetros de consulta. Devuelve un mapa vacío si la consulta es nula, vacía o no contiene parámetros válidos.
     */
    private Map<String, String> parseQuery(String query) {
        if (query == null || query.isEmpty()) {
            return Map.of(); // Devolver mapa vacío si no hay query
        }
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2)) // Limitar a 2 para manejar valores con '='
                .filter(kv -> kv.length == 2 && !kv[0].isEmpty()) // Ignorar parámetros mal formados o sin clave
                .collect(Collectors.toMap(
                        kv -> kv[0],
                        kv -> kv[1],
                        (v1, v2) -> v1 // En caso de claves duplicadas, mantener la primera
                ));
    }
}
