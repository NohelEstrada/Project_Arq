package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Hospital;
import com.sources.app.dao.HospitalDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador HTTP para las operaciones CRUD (Crear, Leer, Actualizar)
 * sobre la entidad {@link Hospital}. Gestiona las solicitudes para el endpoint "/api/hospital".
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/hospital}: Obtiene la lista de todos los hospitales.</li>
 *   <li>{@code GET /api/hospital?id={id}}: Obtiene un hospital específico por su ID.</li>
 *   <li>{@code POST /api/hospital}: Crea un nuevo hospital.</li>
 *   <li>{@code PUT /api/hospital}: Actualiza un hospital existente (requiere 'idHospital' en el cuerpo).</li>
 *   <li>(DELETE no está implementado en este manejador).</li>
 * </ul>
 */
public class HospitalHandler implements HttpHandler {

    /** DAO para acceder a los datos de la entidad Hospital. */
    private final HospitalDAO hospitalDAO;
    /** ObjectMapper para la serialización/deserialización JSON. */
    private final ObjectMapper objectMapper;
    /** Ruta base para las solicitudes gestionadas por este manejador. */
    private static final String ENDPOINT = "/api/hospital";

    /**
     * Constructor del manejador de hospitales.
     * Inicializa el DAO de hospitales y el ObjectMapper.
     *
     * @param hospitalDAO El DAO para interactuar con la tabla de hospitales.
     */
    public HospitalHandler(HospitalDAO hospitalDAO) {
        this.hospitalDAO = hospitalDAO;
        this.objectMapper = new ObjectMapper();
        // No se necesita formato de fecha si la entidad no tiene fechas
        // this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas al endpoint de hospitales.
     * Configura las cabeceras CORS, maneja solicitudes OPTIONS (preflight),
     * valida que la ruta coincida con {@link #ENDPOINT}, y enruta las solicitudes
     * GET, POST y PUT a sus respectivos métodos de manejo. Cualquier otro método resulta
     * en un error 405 (Method Not Allowed). Captura excepciones generales para devolver un error 500.
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida (generalmente manejado internamente
     *                     y resultando en una respuesta de error 500, pero la firma del método lo requiere).
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

        // Verificar endpoint
        String path = exchange.getRequestURI().getPath();
        if (!path.equalsIgnoreCase(ENDPOINT)) {
            sendErrorResponse(exchange, 404, "Endpoint no encontrado.");
            return;
        }

        // Enrutamiento según método HTTP con manejo de errores general
        try {
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
                    sendErrorResponse(exchange, 405, "Método no permitido.");
            }
        } catch (Exception e) {
             System.err.println("Error inesperado en HospitalHandler: " + e.getMessage());
             e.printStackTrace();
             sendErrorResponse(exchange, 500, "Error interno del servidor.");
        }
    }

    /**
     * Maneja las solicitudes GET a {@code /api/hospital}.
     * Si se proporciona un parámetro de consulta 'id', intenta obtener el hospital específico por ese ID.
     * Si no se proporciona 'id' o el parámetro es inválido, obtiene y devuelve la lista de todos los hospitales.
     * Responde con 404 si el hospital solicitado por ID no se encuentra.
     * Responde con 400 si el parámetro 'id' tiene un formato inválido.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        // Busca por ID si el parámetro está presente
        if(query != null && query.contains("id=")) {
            Map<String,String> params = parseQuery(query);
            try {
                Long id = Long.parseLong(params.get("id"));
                Hospital hospital = hospitalDAO.findById(id);
                if(hospital != null) {
                    // Hospital encontrado, enviar respuesta JSON
                    sendJsonResponse(exchange, 200, hospital);
                } else {
                    // Hospital no encontrado
                    sendErrorResponse(exchange, 404, "Hospital no encontrado con ID: " + id);
                }
            } catch(NumberFormatException e) {
                // ID inválido
                sendErrorResponse(exchange, 400, "ID de hospital inválido.");
            }
        } else {
            // Obtener todos los hospitales
            List<Hospital> list = hospitalDAO.findAll();
            sendJsonResponse(exchange, 200, list);
        }
    }

    /**
     * Maneja las solicitudes POST a {@code /api/hospital}.
     * Crea un nuevo hospital basado en el cuerpo JSON de la solicitud.
     * El cuerpo JSON debe contener al menos 'name' y 'address'. Otros campos como
     * 'phone', 'email', 'enabled' y 'port' son opcionales.
     * Responde con 201 (Created) y el objeto del hospital creado si tiene éxito.
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
            Hospital hospital = objectMapper.readValue(requestBody, Hospital.class);
            
            // Validar datos requeridos
            if (hospital.getName() == null || hospital.getName().trim().isEmpty() ||
                hospital.getAddress() == null || hospital.getAddress().trim().isEmpty()) {
                 sendErrorResponse(exchange, 400, "Los campos 'name' y 'address' son requeridos.");
                 return;
            }

            // Crea el hospital en la base de datos
            Hospital created = hospitalDAO.create(
                    hospital.getName(),
                    hospital.getAddress(),
                    hospital.getPhone(),     // Puede ser null
                    hospital.getEmail(),     // Puede ser null
                    hospital.getEnabled(),    // Puede ser null (usará default)
                    hospital.getPort()       // Puede ser null
            );
            if(created != null) {
                // Hospital creado exitosamente, enviar respuesta JSON
                sendJsonResponse(exchange, 201, created);
            } else {
                // Error al crear el hospital
                 System.err.println("Error en DAO al crear hospital.");
                sendErrorResponse(exchange, 500, "Error interno al crear el hospital.");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
             System.err.println("Error al parsear JSON en POST Hospital: " + e.getMessage());
             sendErrorResponse(exchange, 400, "Formato JSON inválido.");
        } catch(Exception e) {
             System.err.println("Error inesperado en handlePost Hospital: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor al procesar la solicitud.");
        }
    }

    /**
     * Maneja las solicitudes PUT a {@code /api/hospital}.
     * Actualiza un hospital existente basado en el cuerpo JSON de la solicitud.
     * El cuerpo JSON *debe* contener 'idHospital' para identificar el hospital a actualizar.
     * Otros campos presentes en el JSON (name, address, phone, email, enabled) se usarán para la actualización.
     * Se realizan validaciones para asegurar que 'name' y 'address', si se actualizan, no queden vacíos.
     * Responde con 200 (OK) y el objeto del hospital actualizado si tiene éxito.
     * Responde con 400 si falta 'idHospital' o si algún campo actualizado viola las restricciones (e.g., nombre vacío) o si el JSON es inválido.
     * Responde con 404 si el 'idHospital' proporcionado no corresponde a un hospital existente o si la actualización falla en el DAO por otra razón.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        try {
            // Lee y parsea el cuerpo de la solicitud
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Hospital hospital = objectMapper.readValue(requestBody, Hospital.class);
            
            // Validar que el ID esté presente para la actualización
            if (hospital.getIdHospital() == null) {
                 sendErrorResponse(exchange, 400, "Falta el campo 'idHospital' para actualizar.");
                 return;
            }
             // Validaciones adicionales (ej: nombre no vacío si se actualiza)
             if (hospital.getName() != null && hospital.getName().trim().isEmpty()) {
                  sendErrorResponse(exchange, 400, "El campo 'name' no puede ser vacío.");
                  return;
             }
             if (hospital.getAddress() != null && hospital.getAddress().trim().isEmpty()) {
                  sendErrorResponse(exchange, 400, "El campo 'address' no puede ser vacío.");
                  return;
             }

            // Actualiza el hospital en la base de datos
            Hospital updated = hospitalDAO.update(hospital);
            if(updated != null) {
                // Hospital actualizado exitosamente, enviar respuesta JSON
                sendJsonResponse(exchange, 200, updated);
            } else {
                // Error al actualizar (posiblemente no encontrado o error interno)
                 System.err.println("Error en DAO al actualizar hospital o no encontrado (ID: " + hospital.getIdHospital() + ").");
                sendErrorResponse(exchange, 404, "Hospital no encontrado con ID: " + hospital.getIdHospital() + " o error al actualizar.");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("Error al parsear JSON en PUT Hospital: " + e.getMessage());
            sendErrorResponse(exchange, 400, "Formato JSON inválido.");
        } catch (Exception e) {
             System.err.println("Error inesperado en handlePut Hospital: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor al procesar la solicitud.");
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
        String body = objectMapper.writeValueAsString(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
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
            System.err.println("Error crítico al enviar respuesta de error: " + e.getMessage());
        }
    }

    /**
     * Parsea los parámetros de una cadena de consulta (query string) de una URL en un mapa de clave-valor.
     * Maneja claves sin valor y parámetros mal formados. Ignora claves duplicadas, manteniendo la primera aparición.
     *
     * @param query La cadena de consulta (ej: "id=1&sort=name"). Puede ser {@code null} o vacía.
     * @return Un {@link Map} que contiene los parámetros de consulta. Devuelve un mapa vacío si la consulta es nula, vacía o no contiene parámetros válidos.
     */
    private Map<String,String> parseQuery(String query){
        if (query == null || query.isEmpty()) {
            return Map.of();
        }
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .filter(pair -> pair.length == 2 && !pair[0].isEmpty())
                .collect(Collectors.toMap(
                        pair -> pair[0],
                        pair -> pair[1],
                        (v1, v2) -> v1
                ));
    }
}
