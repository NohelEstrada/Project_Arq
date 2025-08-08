package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.HospitalInsuranceServiceDAO;
import com.sources.app.dao.HospitalDAO;
import com.sources.app.dao.InsuranceServiceDAO;
import com.sources.app.entities.HospitalInsuranceService;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.InsuranceService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Manejador HTTP para gestionar las relaciones de aprobación entre Hospitales y Servicios de Seguro.
 * Esta entidad representa qué servicios de seguro ({@link InsuranceService}) han sido aprobados
 * o están ofrecidos por un hospital específico ({@link Hospital}), almacenado como {@link HospitalInsuranceService}.
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/hospital-services?hospital={hospitalId}}: Lista todas las relaciones (servicios aprobados) para un hospital específico.</li>
 *   <li>{@code GET /api/hospital-services?service={serviceId}}: Lista todos los hospitales que tienen aprobado un servicio específico.</li>
 *   <li>{@code GET /api/hospital-services/{id}}: Obtiene una relación específica {@code HospitalInsuranceService} por su ID único.</li>
 *   <li>{@code POST /api/hospital-services/approve}: Crea una nueva relación de aprobación entre un hospital y un servicio. Requiere `hospital_id` y `service_id` en el cuerpo JSON.</li>
 *   <li>{@code POST /api/hospital-services/revoke}: Marca una relación existente como no aprobada (cambia estado a `isApproved=false`). Requiere `hospital_id` y `service_id` en el cuerpo JSON. Nota: No elimina la relación, solo cambia su estado.</li>
 *   <li>{@code DELETE /api/hospital-services/{id}}: Elimina físicamente una relación {@code HospitalInsuranceService} específica por su ID.</li>
 * </ul>
 */
public class HospitalInsuranceServiceHandler implements HttpHandler {
    /** DAO para acceder a los datos de la relación Hospital-Servicio de Seguro. */
    private final HospitalInsuranceServiceDAO hospitalInsuranceServiceDAO;
    /** DAO para acceder a los datos de Hospitales. */
    private final HospitalDAO hospitalDAO;
    /** DAO para acceder a los datos de Servicios de Seguro. */
    private final InsuranceServiceDAO insuranceServiceDAO;
    /** ObjectMapper para la serialización/deserialización JSON. */
    private final ObjectMapper objectMapper;
    /** Ruta base para los endpoints de este manejador. */
    private static final String ENDPOINT = "/api/hospital-services";
    /** Subruta específica para la acción de aprobar un servicio para un hospital. */
    private static final String APPROVE_PATH = ENDPOINT + "/approve";
    /** Subruta específica para la acción de revocar la aprobación de un servicio para un hospital. */
    private static final String REVOKE_PATH = ENDPOINT + "/revoke";

    /**
     * Constructor del manejador de relaciones Hospital-Servicio.
     * Inicializa los DAOs necesarios para interactuar con las entidades relacionadas
     * (HospitalInsuranceService, Hospital, InsuranceService) y el ObjectMapper.
     *
     * @param hospitalInsuranceServiceDAO DAO para la entidad de relación {@link HospitalInsuranceService}.
     * @param hospitalDAO DAO para la entidad {@link Hospital}.
     * @param insuranceServiceDAO DAO para la entidad {@link InsuranceService}.
     */
    public HospitalInsuranceServiceHandler(
            HospitalInsuranceServiceDAO hospitalInsuranceServiceDAO,
            HospitalDAO hospitalDAO,
            InsuranceServiceDAO insuranceServiceDAO) {
        this.hospitalInsuranceServiceDAO = hospitalInsuranceServiceDAO;
        this.hospitalDAO = hospitalDAO;
        this.insuranceServiceDAO = insuranceServiceDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes.
     * Configura las cabeceras CORS, maneja solicitudes OPTIONS (preflight),
     * valida la ruta base de la solicitud ({@link #ENDPOINT}), y enruta la solicitud
     * al método de manejo apropiado (GET, POST, DELETE) basado en el método HTTP
     * y la ruta específica (incluyendo subrutas como {@link #APPROVE_PATH}, {@link #REVOKE_PATH}
     * y la presencia de un ID en la ruta para GET por ID y DELETE).
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida durante el manejo de la solicitud.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración de CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Manejo de solicitudes preflight OPTIONS
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No Content
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        String method = exchange.getRequestMethod().toUpperCase();

        try {
             // Verifica que la ruta base coincida
            if (!path.startsWith(ENDPOINT)) {
                sendErrorResponse(exchange, 404, "Endpoint no encontrado.");
                return;
            }
            
            // Extraer ID de la ruta si existe (para GET /{id} y DELETE /{id})
            Long idFromPath = extractIdFromPath(path, ENDPOINT);

            // Enrutamiento principal
            if (method.equals("GET")) {
                if (idFromPath != null) {
                    handleGetById(exchange, idFromPath);
                } else if (path.equals(ENDPOINT)) {
                    handleGetByFilters(exchange, query);
                } else {
                    sendErrorResponse(exchange, 404, "Ruta GET no válida.");
                }
            } else if (method.equals("POST")) {
                if (path.equals(APPROVE_PATH)) {
                    handleApprove(exchange);
                } else if (path.equals(REVOKE_PATH)) {
                    handleRevoke(exchange);
                } else {
                    sendErrorResponse(exchange, 404, "Ruta POST no válida.");
                }
            } else if (method.equals("DELETE")) {
                 if (idFromPath != null) {
                    handleDelete(exchange, idFromPath);
                } else {
                     sendErrorResponse(exchange, 400, "Falta el ID en la ruta para DELETE (ej: /api/hospital-services/{id})");
                }
            } else {
                // Método no permitido
                 sendErrorResponse(exchange, 405, "Método no permitido.");
            }
        } catch (Exception e) {
             System.err.println("Error inesperado en HospitalInsuranceServiceHandler: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor.");
        }
    }
    
     /**
     * Extrae un ID numérico (Long) del final de una ruta URI, asumiendo que sigue
     * inmediatamente después de la ruta base especificada, separado por '/'.
     *
     * <p>Ejemplo: {@code extractIdFromPath("/api/items/123", "/api/items")} retorna {@code 123L}.</p>
     *
     * @param path La ruta completa de la solicitud (ej: "/api/hospital-services/5").
     * @param basePath La parte base de la ruta que precede al ID (ej: "/api/hospital-services").
     * @return El ID extraído como un {@link Long}, o {@code null} si no se encuentra un ID,
     *         la ruta no tiene el formato esperado, o la parte del ID no es un número válido.
     */
    private Long extractIdFromPath(String path, String basePath) {
        if (path.length() > basePath.length() + 1 && path.charAt(basePath.length()) == '/') {
            String idStr = path.substring(basePath.length() + 1);
            try {
                return Long.parseLong(idStr);
            } catch (NumberFormatException e) {
                System.err.println("ID inválido en la ruta: " + idStr);
                return null; // No es un número válido
            }
        } 
        return null; // No hay ID en la ruta
    }

    /**
     * Maneja las solicitudes GET a {@code /api/hospital-services} que incluyen filtros en la query string.
     * Espera un parámetro {@code hospital={id}} o {@code service={id}}.
     * - Si se proporciona {@code hospital={id}}, busca el hospital y devuelve todas las relaciones
     *   {@link HospitalInsuranceService} aprobadas para ese hospital usando {@link HospitalInsuranceServiceDAO#findApprovedByHospital(Hospital)}.
     * - Si se proporciona {@code service={id}}, busca el servicio y devuelve todas las relaciones
     *   (esencialmente, los hospitales que lo tienen aprobado) usando {@link HospitalInsuranceServiceDAO#findHospitalsByService(InsuranceService)}.
     * Responde con 400 si no se proporciona un filtro válido o si el ID es inválido.
     * Responde con 404 si el hospital o servicio especificado en el filtro no existe.
     * Responde con 200 y la lista de relaciones (puede estar vacía) si el filtro es válido y la entidad existe.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param query La cadena de consulta de la URL (ej: "hospital=123").
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGetByFilters(HttpExchange exchange, String query) throws IOException {
        Map<String, String> params = parseQueryParams(query);
        List<HospitalInsuranceService> relations = Collections.emptyList();
        boolean foundEntity = false; // Para saber si el hospital/servicio del filtro existe

        if (params.containsKey("hospital")) {
            try {
                Long hospitalId = Long.parseLong(params.get("hospital"));
                Hospital hospital = hospitalDAO.findById(hospitalId);
                if (hospital != null) {
                    foundEntity = true;
                    // Encuentra relaciones aprobadas para este hospital
                    relations = hospitalInsuranceServiceDAO.findApprovedByHospital(hospital);
                } else {
                     System.err.println("Hospital no encontrado para filtro: " + hospitalId);
                     // foundEntity sigue false
                }
            } catch (NumberFormatException e) {
                System.err.println("ID de hospital inválido en filtro: " + params.get("hospital"));
                sendErrorResponse(exchange, 400, "ID de hospital inválido.");
                return;
            }
        } else if (params.containsKey("service")) {
            try {
                Long serviceId = Long.parseLong(params.get("service"));
                InsuranceService service = insuranceServiceDAO.findById(serviceId);
                if (service != null) {
                    foundEntity = true;
                    // Encuentra hospitales que tienen aprobado este servicio
                    relations = hospitalInsuranceServiceDAO.findHospitalsByService(service);
                } else {
                     System.err.println("Servicio no encontrado para filtro: " + serviceId);
                    // foundEntity sigue false
                }
            } catch (NumberFormatException e) {
                 System.err.println("ID de servicio inválido en filtro: " + params.get("service"));
                 sendErrorResponse(exchange, 400, "ID de servicio inválido.");
                return;
            }
        } else {
             // No se proporcionó ningún filtro o filtro no soportado
             System.err.println("Solicitud GET a /api/hospital-services sin filtro válido (?hospital= o ?service=).");
             sendErrorResponse(exchange, 400, "Filtro requerido (?hospital= o ?service=).");
             return;
        }
        
        // Decidir respuesta basada en si se encontró la entidad del filtro y si hay relaciones
        if (!foundEntity) {
             // Si la entidad base (hospital/servicio) no se encontró, es un 404
             sendErrorResponse(exchange, 404, "Hospital o Servicio no encontrado para el filtro especificado.");
        } else {
             // Si la entidad se encontró, devolver la lista de relaciones (puede ser vacía)
             sendJsonResponse(exchange, 200, relations);
        }
    }

    /**
     * Maneja las solicitudes GET a {@code /api/hospital-services/{id}}.
     * Busca una relación {@link HospitalInsuranceService} específica por su ID único.
     * Responde con 200 y el objeto de la relación si se encuentra.
     * Responde con 404 si no se encuentra ninguna relación con ese ID.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param id El ID de la relación {@code HospitalInsuranceService} extraído de la ruta.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGetById(HttpExchange exchange, Long id) throws IOException {
        if (id == null) { // Validación extra por si acaso
             sendErrorResponse(exchange, 400, "ID inválido proporcionado.");
             return;
        }
        HospitalInsuranceService relation = hospitalInsuranceServiceDAO.findById(id);
        
        if (relation != null) {
            sendJsonResponse(exchange, 200, relation);
        } else {
            System.err.println("Relación no encontrada por ID: " + id);
            sendErrorResponse(exchange, 404, "Relación Hospital-Servicio no encontrada.");
        }
    }

    /**
     * Maneja las solicitudes POST a {@code /api/hospital-services/approve}.
     * Espera un cuerpo JSON con {@code hospital_id} y {@code service_id}.
     * Verifica que tanto el hospital como el servicio existan.
     * Busca si ya existe una relación entre ellos.
     * - Si no existe, crea una nueva relación {@link HospitalInsuranceService} con {@code isApproved=true}.
     * - Si existe y está marcada como no aprobada ({@code isApproved=false}), la actualiza a {@code isApproved=true}.
     * - Si ya existe y está aprobada, responde con 409 (Conflict).
     * Responde con 201 (Created) si se crea una nueva relación, o 200 (OK) si se actualiza una existente a aprobada.
     * Responde con 404 si el hospital o el servicio no existen.
     * Responde con 400 si falta algún ID en el cuerpo JSON o si el JSON es inválido.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handleApprove(HttpExchange exchange) throws IOException {
        try {
            String requestBody = readRequestBody(exchange);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(requestBody, Map.class);
            
            // Validar y obtener IDs
            Long hospitalId = parseLongFromMap(data, "hospitalId");
            Long serviceId = parseLongFromMap(data, "serviceId");
            String notes = data.containsKey("notes") ? (String) data.get("notes") : null;
            
            if (hospitalId == null || serviceId == null) {
                sendErrorResponse(exchange, 400, "Faltan 'hospitalId' o 'serviceId' (deben ser números).");
                return;
            }

            // Validar existencia de Hospital y Servicio
            Hospital hospital = hospitalDAO.findById(hospitalId);
            InsuranceService service = insuranceServiceDAO.findById(serviceId);
            
            if (hospital == null || service == null) {
                 System.err.println("Hospital o Servicio no encontrado para aprobar: H=" + hospitalId + ", S=" + serviceId);
                sendErrorResponse(exchange, 404, "Hospital o Servicio especificado no encontrado.");
                return;
            }
            
            // Intentar aprobar el servicio
            HospitalInsuranceService relation = hospitalInsuranceServiceDAO.approveService(hospital, service, notes);
            
            if (relation != null) {
                sendJsonResponse(exchange, 201, relation);
            } else {
                 System.err.println("Error en DAO al aprobar servicio H=" + hospitalId + ", S=" + serviceId);
                 // Podría ser un conflicto (409) si ya existía?
                sendErrorResponse(exchange, 500, "Error interno al aprobar el servicio.");
            }
         } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
             System.err.println("Error al parsear JSON en POST /approve: " + e.getMessage());
             sendErrorResponse(exchange, 400, "Formato JSON inválido.");
         } catch (Exception e) {
            System.err.println("Excepción al procesar POST /approve: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor.");
        }
    }

    /**
     * Maneja las solicitudes POST a {@code /api/hospital-services/revoke}.
     * Espera un cuerpo JSON con {@code hospital_id} y {@code service_id}.
     * Busca la relación existente entre el hospital y el servicio especificados.
     * - Si la relación existe y está actualmente aprobada ({@code isApproved=true}), la actualiza a {@code isApproved=false}.
     * - Si la relación existe pero ya no está aprobada, responde con 200 (OK) indicando que no hubo cambios.
     * - Si la relación no existe, responde con 404.
     * Responde con 404 si el hospital o el servicio especificados no existen.
     * Responde con 400 si falta algún ID en el cuerpo JSON o si el JSON es inválido.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handleRevoke(HttpExchange exchange) throws IOException {
         try {
            String requestBody = readRequestBody(exchange);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(requestBody, Map.class);
            
            // Validar y obtener IDs
            Long hospitalId = parseLongFromMap(data, "hospitalId");
            Long serviceId = parseLongFromMap(data, "serviceId");
            
             if (hospitalId == null || serviceId == null) {
                sendErrorResponse(exchange, 400, "Faltan 'hospitalId' o 'serviceId' (deben ser números).");
                return;
            }
            
            // Validar existencia (opcional, el DAO podría manejarlo)
            Hospital hospital = hospitalDAO.findById(hospitalId);
            InsuranceService service = insuranceServiceDAO.findById(serviceId);
            
            if (hospital == null || service == null) {
                 System.err.println("Hospital o Servicio no encontrado para revocar: H=" + hospitalId + ", S=" + serviceId);
                sendErrorResponse(exchange, 404, "Hospital o Servicio especificado no encontrado.");
                return;
            }
            
            // Intentar revocar
            boolean success = hospitalInsuranceServiceDAO.revokeApproval(hospital, service);
            
            if (success) {
                System.out.println("Aprobación revocada para H=" + hospitalId + ", S=" + serviceId);
                sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Aprobación revocada."));
            } else {
                // Puede que la relación no existiera o no estuviera aprobada
                 System.err.println("No se encontró relación para revocar o ya estaba revocada para H=" + hospitalId + ", S=" + serviceId);
                sendErrorResponse(exchange, 404, "No se encontró una aprobación activa para revocar.");
            }
         } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
             System.err.println("Error al parsear JSON en POST /revoke: " + e.getMessage());
             sendErrorResponse(exchange, 400, "Formato JSON inválido.");
         } catch (Exception e) {
            System.err.println("Excepción al procesar POST /revoke: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor.");
        }
    }

    /**
     * Maneja las solicitudes DELETE a {@code /api/hospital-services/{id}}.
     * Elimina físicamente la relación {@link HospitalInsuranceService} con el ID especificado.
     * Responde con 204 (No Content) si la eliminación es exitosa.
     * Responde con 404 si no se encuentra ninguna relación con ese ID para eliminar.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param id El ID de la relación {@code HospitalInsuranceService} a eliminar, extraído de la ruta.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleDelete(HttpExchange exchange, Long id) throws IOException {
         if (id == null) {
            sendErrorResponse(exchange, 400, "Falta ID para eliminar la relación.");
            return;
         }
        boolean success = hospitalInsuranceServiceDAO.delete(id);
        
        if (success) {
             System.out.println("Relación Hospital-Servicio eliminada con ID: " + id);
             exchange.sendResponseHeaders(204, -1); // No Content
        } else {
            System.err.println("No se encontró relación para eliminar con ID: " + id);
            sendErrorResponse(exchange, 404, "Relación Hospital-Servicio no encontrada.");
        }
    }
    
    /**
     * Lee el cuerpo completo de una solicitud HTTP y lo devuelve como una cadena UTF-8.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @return El cuerpo de la solicitud como String.
     * @throws IOException Si ocurre un error durante la lectura del InputStream.
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (java.io.InputStream requestBody = exchange.getRequestBody()) {
            return new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
     /**
     * Intenta parsear de forma segura un valor de un mapa como un Long.
     * Maneja valores que son instancias de {@link Number} o {@link String} que representan un número.
     *
     * @param map El mapa del cual extraer el valor (generalmente obtenido de un JSON deserializado).
     * @param key La clave asociada al valor que se espera sea un Long.
     * @return El valor como {@link Long}, o {@code null} si la clave no existe, el valor es nulo,
     *         o no se puede parsear como Long (registra una advertencia en caso de error de parseo o tipo inesperado).
     */
    private Long parseLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            System.err.println("Valor inválido para la clave '" + key + "': " + value + ". Se esperaba un número.");
            return null;
        }
    }

    /**
     * Parsea la cadena de consulta (query string) de una URL en un mapa de clave-valor.
     * Maneja claves sin valor y decodifica los componentes si es necesario (aunque aquí no se aplica decodificación).
     * Ignora parámetros mal formados o sin clave. En caso de claves duplicadas, conserva la primera aparición.
     *
     * @param query La cadena de consulta (ej: "hospital=123&sort=name"). Puede ser {@code null} o vacía.
     * @return Un {@link Map} que contiene los parámetros de consulta. Devuelve un mapa vacío si la consulta es nula, vacía o no contiene parámetros válidos.
     */
    private Map<String, String> parseQueryParams(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
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

    /**
     * Envía una respuesta JSON al cliente.
     * Serializa el objeto de datos proporcionado a JSON y lo escribe en el cuerpo de la respuesta
     * con el código de estado HTTP especificado y el tipo de contenido "application/json; charset=UTF-8".
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP para la respuesta (e.g., 200, 201, 400).
     * @param data El objeto a serializar como JSON (puede ser una lista, mapa, entidad, etc.). Puede ser {@code null} para respuestas sin cuerpo.
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
} 