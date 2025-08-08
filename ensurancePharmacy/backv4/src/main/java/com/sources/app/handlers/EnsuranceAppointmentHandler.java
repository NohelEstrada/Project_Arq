package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.EnsuranceAppointmentDAO;
import com.sources.app.entities.EnsuranceAppointment;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manejador HTTP para las operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * relacionadas con la entidad {@link EnsuranceAppointment}, que representa citas médicas
 * posiblemente vinculadas a un sistema de seguros o un sistema externo de hospital.
 * Gestiona las solicitudes para el endpoint "/api/ensurance-appointments".
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/ensurance-appointments}: Obtiene todas las citas.</li>
 *   <li>{@code GET /api/ensurance-appointments?id={id}}: Obtiene una cita por su ID primario (interno).</li>
 *   <li>{@code GET /api/ensurance-appointments?hospitalId={hospitalId}}: Obtiene una cita por el ID original del sistema de hospital.</li>
 *   <li>{@code GET /api/ensurance-appointments?userId={userId}}: Obtiene todas las citas para un usuario específico.</li>
 *   <li>{@code GET /api/ensurance-appointments?today=true}: Obtiene todas las citas programadas para el día actual.</li>
 *   <li>{@code GET /api/ensurance-appointments?date={yyyy-MM-dd}}: Obtiene todas las citas programadas para una fecha específica.</li>
 *   <li>{@code POST /api/ensurance-appointments}: Crea una nueva cita.</li>
 *   <li>{@code PUT /api/ensurance-appointments}: Actualiza una cita existente (requiere 'id' en el cuerpo).</li>
 *   <li>{@code DELETE /api/ensurance-appointments?hospitalId={hospitalId}}: Elimina una cita usando el ID original del sistema de hospital.</li>
 *   <li>(DELETE por ID interno podría implementarse como {@code DELETE /api/ensurance-appointments/{id}}).</li>
 * </ul>
 */
public class EnsuranceAppointmentHandler implements HttpHandler {

    /** DAO para acceder a los datos de la entidad EnsuranceAppointment. */
    private final EnsuranceAppointmentDAO appointmentDAO;
    /** ObjectMapper para la serialización/deserialización JSON, configurado con formato de fecha/hora. */
    private final ObjectMapper objectMapper;
    /** Ruta base para las solicitudes gestionadas por este manejador. */
    private static final String ENDPOINT = "/api/ensurance-appointments";

    /**
     * Constructor del manejador de citas de seguro.
     * Inicializa el DAO de citas y el ObjectMapper, configurando un formato específico
     * para la serialización/deserialización de fechas y horas ("yyyy-MM-dd HH:mm:ss").
     *
     * @param appointmentDAO El DAO para interactuar con la tabla de citas {@link EnsuranceAppointment}.
     */
    public EnsuranceAppointmentHandler(EnsuranceAppointmentDAO appointmentDAO) {
        this.appointmentDAO = appointmentDAO;
        this.objectMapper = new ObjectMapper();
        // Define el formato de fecha esperado/utilizado
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")); // Usar formato con hora si es relevante
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas al endpoint de citas.
     * Configura las cabeceras CORS, maneja solicitudes OPTIONS (preflight), y enruta las solicitudes
     * GET, POST, PUT y DELETE a sus respectivos métodos de manejo basados en el método HTTP,
     * la ruta y los parámetros de consulta.
     * Captura excepciones generales para devolver un error 500.
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida (generalmente manejado internamente
     *                     y resultando en una respuesta de error 500).
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configurar CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Respuesta a solicitudes OPTIONS (preflight de CORS)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // Sin contenido
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        try {
            // Verifica si la ruta base es correcta
            if (!path.startsWith(ENDPOINT)) {
                sendErrorResponse(exchange, 404, "Endpoint no encontrado.");
                return;
            }
            
            // Enrutamiento principal basado en el método HTTP
            switch (method.toUpperCase()) { // Usar toUpperCase para consistencia
                case "GET":
                    handleGetRequests(exchange, query);
                    break;
                case "POST":
                    // Asume que POST siempre es para crear en la ruta base
                    if(path.equals(ENDPOINT)){
                        handlePost(exchange);
                    } else {
                         sendErrorResponse(exchange, 404, "Ruta POST no válida.");
                    }
                    break;
                case "DELETE":
                     // Asume que DELETE requiere un identificador (aquí, hospitalId)
                     // GET /api/ensurance-appointments?hospitalId=... para obtener
                     // DELETE /api/ensurance-appointments?hospitalId=... para eliminar
                    if (query != null && query.contains("hospitalId=")) {
                        handleDeleteByHospitalId(exchange, query);
                    } else {
                        sendErrorResponse(exchange, 400, "Falta el parámetro 'hospitalId' para DELETE.");
                    }
                    break;
                case "PUT": // Añadir manejo de PUT si es necesario
                     // Por ejemplo, requerir ID en la ruta: /api/ensurance-appointments/{id}
                     // O ID en el cuerpo y llamar a handlePut(exchange);
                     handlePut(exchange); // Llama a handlePut existente (requiere ID en body)
                     break;
                default:
                    sendErrorResponse(exchange, 405, "Método no permitido.");
            }
        } catch (Exception e) {
            System.err.println("Error inesperado en EnsuranceAppointmentHandler: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor.");
        }
    }

    /**
     * Enruta las solicitudes GET a los manejadores específicos basados en los parámetros de consulta.
     * Determina qué tipo de búsqueda GET se solicita (por hospitalId, fecha, userId, ID interno, o todas)
     * y llama al método correspondiente. Si no se reconoce ningún parámetro válido o no hay parámetros,
     * se asume que se solicitan todas las citas.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param query La cadena de consulta de la URI (puede ser {@code null} o vacía).
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGetRequests(HttpExchange exchange, String query) throws IOException {
         Map<String, String> params = parseQueryParams(query); // Parsear una sola vez
         
        if (params.containsKey("hospitalId")) {
            handleGetByHospitalId(exchange, params.get("hospitalId"));
        } else if (params.containsKey("today") && "true".equalsIgnoreCase(params.get("today"))) {
            handleGetTodayAppointments(exchange);
        } else if (params.containsKey("date")) {
            handleGetByDate(exchange, params.get("date"));
        } else if (params.containsKey("userId")) {
             handleGetByUserId(exchange, params.get("userId"));
        } else if (params.containsKey("id")) { // Añadir búsqueda por ID primario si se necesita
            handleGetById(exchange, params.get("id"));
        } else if (query == null || query.isEmpty()) { 
            // Si no hay parámetros específicos, obtiene todas las citas
            handleGetAll(exchange);
        } else {
            // Parámetro no reconocido
            sendErrorResponse(exchange, 400, "Parámetro de búsqueda no válido.");
        }
    }
    
    /**
     * Maneja la solicitud GET para obtener todas las citas de {@link EnsuranceAppointment}.
     * Llama a {@link EnsuranceAppointmentDAO#findAll()} y envía la lista resultante como JSON.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al obtener los datos o al enviar la respuesta.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<EnsuranceAppointment> appointments = appointmentDAO.findAll();
        sendJsonResponse(exchange, 200, appointments);
    }

    /**
     * Maneja la solicitud GET para obtener una cita específica por su ID primario (interno).
     * Parsea el ID de la cadena, busca la cita usando {@link EnsuranceAppointmentDAO#findById(Long)}
     * y la envía como JSON si se encuentra. Devuelve 404 si no se encuentra o 400 si el ID es inválido.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param idString El ID de la cita (obtenido del parámetro de consulta 'id') como {@link String}.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGetById(HttpExchange exchange, String idString) throws IOException {
         try {
            Long id = Long.parseLong(idString);
            EnsuranceAppointment appointment = appointmentDAO.findById(id);
            if (appointment != null) {
                sendJsonResponse(exchange, 200, appointment);
            } else {
                sendErrorResponse(exchange, 404, "Cita no encontrada con ID: " + id);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "ID de cita inválido: " + idString);
        }
    }

    /**
     * Maneja la solicitud GET para obtener todas las citas asociadas a un ID de usuario específico.
     * Parsea el ID de usuario, busca las citas usando {@link EnsuranceAppointmentDAO#findByUserId(Long)}
     * y envía la lista resultante (puede estar vacía) como JSON. Devuelve 400 si el ID de usuario es inválido.
     * Nota: Actualmente no enriquece la respuesta con detalles del usuario.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param userIdString El ID del usuario (obtenido del parámetro de consulta 'userId') como {@link String}.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGetByUserId(HttpExchange exchange, String userIdString) throws IOException {
        try {
            Long userId = Long.parseLong(userIdString);
            List<EnsuranceAppointment> appointments = appointmentDAO.findByUserId(userId);
            // TODO: Considerar enriquecer con información del usuario si es necesario
            // List<EnsuranceAppointment> enrichedAppointments = enrichAppointmentsWithUserInfo(appointments);
            sendJsonResponse(exchange, 200, appointments); // Devuelve lista vacía si no hay citas
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear userId: " + userIdString);
            sendErrorResponse(exchange, 400, "ID de usuario inválido: " + userIdString);
        }
    }

    /**
     * Maneja la solicitud GET para obtener una cita específica usando el ID original del sistema de hospital.
     * Busca la cita usando {@link EnsuranceAppointmentDAO#findByHospitalAppointmentId(String)}
     * y la envía como JSON si se encuentra. Devuelve 404 si no se encuentra.
     * Requiere que el parámetro 'hospitalId' no sea nulo ni vacío.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param hospitalId El ID de la cita en el sistema original del hospital (obtenido del parámetro de consulta 'hospitalId').
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGetByHospitalId(HttpExchange exchange, String hospitalId) throws IOException {
        if(hospitalId == null || hospitalId.trim().isEmpty()){
             sendErrorResponse(exchange, 400, "Falta el parámetro 'hospitalId'.");
             return;
        }
        EnsuranceAppointment appointment = appointmentDAO.findByHospitalAppointmentId(hospitalId);
        if (appointment != null) {
            sendJsonResponse(exchange, 200, appointment);
        } else {
            // Es común que una cita específica no se encuentre, 404 es apropiado
            sendErrorResponse(exchange, 404, "Cita no encontrada para hospitalId: " + hospitalId);
        }
    }

    /**
     * Maneja la solicitud POST a {@code /api/ensurance-appointments} para crear una nueva cita.
     * Lee el cuerpo JSON de la solicitud, que debe contener los detalles de la cita
     * (incluyendo potencialmente {@code hospitalAppointmentId}, {@code userId}, {@code date}, {@code status}, etc.).
     * Valida los campos requeridos (ej: `userId`, `date`, `hospitalAppointmentId` si es necesario).
     * Llama a {@link EnsuranceAppointmentDAO#create(EnsuranceAppointment)} para guardar la nueva cita.
     * Responde con 201 (Created) y el objeto de la cita creada si tiene éxito.
     * Responde con 400 si faltan campos requeridos, si los datos son inválidos (e.g., formato de fecha incorrecto) o si el JSON es inválido.
     * Responde con 409 (Conflict) si ya existe una cita con el mismo `hospitalAppointmentId`.
     * Responde con 500 si ocurre un error interno durante la creación.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String requestBody = readRequestBody(exchange);
            System.out.println("Cuerpo recibido para POST EnsuranceAppointment: " + requestBody);

            // Parsear el cuerpo JSON a la entidad EnsuranceAppointment
            EnsuranceAppointment newAppointmentData = objectMapper.readValue(requestBody, EnsuranceAppointment.class);

            // Validar datos mínimos (ejemplo básico)
            if (newAppointmentData.getHospitalAppointmentId() == null || newAppointmentData.getHospitalAppointmentId().trim().isEmpty() ||
                newAppointmentData.getIdUser() == null || 
                newAppointmentData.getAppointmentDate() == null) {
                System.err.println("Datos incompletos para crear cita EnsuranceAppointment.");
                sendErrorResponse(exchange, 400, "Datos incompletos (hospitalAppointmentId, idUser, appointmentDate requeridos).");
                return;
            }

            // Crear la cita usando el DAO
            EnsuranceAppointment createdAppointment = appointmentDAO.create(
                    newAppointmentData.getHospitalAppointmentId(),
                    newAppointmentData.getIdUser(),
                    newAppointmentData.getAppointmentDate(),
                    newAppointmentData.getDoctorName(), // Puede ser null
                    newAppointmentData.getReason()      // Puede ser null
            );

            if (createdAppointment != null) {
                sendJsonResponse(exchange, 201, createdAppointment);
            } else {
                System.err.println("Error en DAO al crear EnsuranceAppointment.");
                sendErrorResponse(exchange, 500, "Error interno al crear la cita.");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
             System.err.println("Error al parsear JSON en POST EnsuranceAppointment: " + e.getMessage());
             sendErrorResponse(exchange, 400, "Formato JSON inválido.");
        } catch (Exception e) {
            System.err.println("Excepción al procesar POST EnsuranceAppointment: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor al procesar la solicitud.");
        }
    }

    /**
     * Maneja la solicitud PUT a {@code /api/ensurance-appointments} para actualizar una cita existente.
     * Lee el cuerpo JSON de la solicitud, que *debe* contener el ID primario (`id`) de la cita a actualizar.
     * Valida la presencia del ID y otros campos opcionales que se puedan actualizar.
     * Llama a {@link EnsuranceAppointmentDAO#update(EnsuranceAppointment)} para aplicar los cambios.
     * Responde con 200 (OK) y el objeto de la cita actualizada si tiene éxito.
     * Responde con 400 si falta el ID, si los datos son inválidos o si el JSON es inválido.
     * Responde con 404 si no se encuentra una cita con el ID proporcionado.
     * Responde con 500 si ocurre un error interno durante la actualización.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        try {
            String requestBody = readRequestBody(exchange);
            EnsuranceAppointment appointmentData = objectMapper.readValue(requestBody, EnsuranceAppointment.class);
            
            // Validar que el ID esté presente para la actualización
            if(appointmentData.getIdAppointment() == null){
                 System.err.println("Falta ID (idAppointment) para actualizar cita.");
                 sendErrorResponse(exchange, 400, "Falta el campo 'idAppointment' para actualizar.");
                 return;
            }
            // Añadir más validaciones si es necesario (e.g., fecha no nula)

            EnsuranceAppointment updated = appointmentDAO.update(appointmentData);
            
            if (updated != null) {
                sendJsonResponse(exchange, 200, updated);
            } else {
                // Podría ser 404 si el ID no existe, o 500 si falla la actualización
                 System.err.println("Error en DAO al actualizar EnsuranceAppointment o no encontrada (ID: " + appointmentData.getIdAppointment() + ").");
                 sendErrorResponse(exchange, 404, "Cita no encontrada o error al actualizar.");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
             System.err.println("Error al parsear JSON en PUT EnsuranceAppointment: " + e.getMessage());
             sendErrorResponse(exchange, 400, "Formato JSON inválido.");
        } catch (Exception e) {
            System.err.println("Excepción al procesar PUT EnsuranceAppointment: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor al procesar la actualización.");
        }
    }

    /**
     * Maneja la eliminación de una cita por su ID primario (interno).
     * (Actualmente no invocado directamente por el enrutador principal, pero podría ser usado
     * si se implementa DELETE /api/ensurance-appointments/{id}).
     * Llama a {@link EnsuranceAppointmentDAO#deleteById(Long)}.
     * Responde con 204 (No Content) si la eliminación es exitosa.
     * Responde con 404 si no se encuentra la cita.
     * Responde con 500 si ocurre un error interno.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param id El ID primario de la cita a eliminar.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleDeleteById(HttpExchange exchange, Long id) throws IOException {
         if (id == null) {
            sendErrorResponse(exchange, 400, "Falta ID para eliminar la cita.");
            return;
         }
        boolean deleted = appointmentDAO.delete(id);
        if (deleted) {
             System.out.println("Cita eliminada con ID: " + id);
             exchange.sendResponseHeaders(204, -1); // No Content
        } else {
            // Puede que no se encontrara el ID
            System.err.println("No se encontró cita para eliminar con ID: " + id);
            sendErrorResponse(exchange, 404, "Cita no encontrada para eliminar.");
        }
    }

    /**
     * Maneja la solicitud DELETE para eliminar una cita usando el ID original del sistema de hospital.
     * Extrae el 'hospitalId' de la query string.
     * Llama a {@link EnsuranceAppointmentDAO#deleteByHospitalAppointmentId(String)}.
     * Responde con 204 (No Content) si la eliminación es exitosa (se encontró y eliminó la cita).
     * Responde con 404 si no se encontró ninguna cita con ese 'hospitalId'.
     * Responde con 400 si falta el parámetro 'hospitalId' o es inválido.
     * Responde con 500 si ocurre un error interno durante la eliminación.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param query La cadena de consulta que debe contener 'hospitalId'.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleDeleteByHospitalId(HttpExchange exchange, String query) throws IOException {
        Map<String, String> params = parseQueryParams(query);
        String hospitalId = params.get("hospitalId");
        
        if (hospitalId == null || hospitalId.trim().isEmpty()) {
            System.err.println("Falta hospitalId para DELETE.");
            sendErrorResponse(exchange, 400, "Falta el parámetro 'hospitalId' para eliminar.");
            return;
        }
        
        boolean deleted = appointmentDAO.deleteByHospitalAppointmentId(hospitalId);
        
        if (deleted) {
             System.out.println("Cita eliminada para hospitalId: " + hospitalId);
            exchange.sendResponseHeaders(204, -1); // No Content
        } else {
            // Podría ser que no se encontró o hubo un error
             System.err.println("No se encontró cita para eliminar con hospitalId: " + hospitalId);
            sendErrorResponse(exchange, 404, "Cita no encontrada para eliminar por hospitalId.");
        }
    }

    /**
     * Envía una respuesta JSON al cliente.
     * Serializa el objeto de datos a JSON y lo escribe en el cuerpo de la respuesta con el código de estado adecuado.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP (e.g., 200, 201).
     * @param data El objeto a serializar (puede ser una lista, un objeto simple, etc.).
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
     * Crea un cuerpo JSON con una clave "error" y el mensaje proporcionado.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP de error (e.g., 400, 404, 500).
     * @param errorMessage El mensaje descriptivo del error.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        Map<String, Object> errorResponse = Map.of("success", false, "message", errorMessage);
         try {
             // Evita doble escritura de headers si ya ocurrió un error antes
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
     * Maneja la solicitud GET para obtener todas las citas programadas para la fecha actual (hoy).
     * Obtiene la fecha actual, la formatea como "yyyy-MM-dd" y llama a
     * {@link EnsuranceAppointmentDAO#findByDate(String)}.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGetTodayAppointments(HttpExchange exchange) throws IOException {
        List<EnsuranceAppointment> appointments = appointmentDAO.findTodayAppointments();
        sendJsonResponse(exchange, 200, appointments);
    }

    /**
     * Maneja la solicitud GET para obtener todas las citas programadas para una fecha específica.
     * Valida que la fecha proporcionada en la query string tenga el formato "yyyy-MM-dd".
     * Llama a {@link EnsuranceAppointmentDAO#findByDate(String)} con la fecha formateada.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param dateStr La fecha como String (obtenida del parámetro de consulta 'date'), debe estar en formato "yyyy-MM-dd".
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGetByDate(HttpExchange exchange, String dateStr) throws IOException {
        if (dateStr == null) {
            sendErrorResponse(exchange, 400, "Falta el parámetro 'date'.");
            return;
        }
        try {
            // Usa el mismo formato definido en el constructor para parsear
            Date date = objectMapper.getDateFormat().parse(dateStr); 
            List<EnsuranceAppointment> appointments = appointmentDAO.findByDate(date);
            sendJsonResponse(exchange, 200, appointments);
        } catch (ParseException e) {
            System.err.println("Error al parsear fecha: " + dateStr + ". Formato esperado: yyyy-MM-dd");
            sendErrorResponse(exchange, 400, "Formato de fecha inválido. Use yyyy-MM-dd.");
        }
    }
    
    /**
     * Método auxiliar (marcado como TODO) para potencialmente enriquecer la lista de citas
     * con información adicional del usuario antes de enviarla como respuesta.
     * Actualmente, solo devuelve la lista original.
     *
     * @param appointments La lista de citas a enriquecer.
     * @return La lista de citas (potencialmente enriquecida).
     */
    private List<EnsuranceAppointment> enrichAppointmentsWithUserInfo(List<EnsuranceAppointment> appointments) {
        // TODO: Implementar lógica para buscar y añadir información del usuario si es necesario.
        // Ejemplo: iterar, buscar usuario por ID y añadir nombre a un DTO.
        System.out.println("Nota: La información del usuario no se está enriqueciendo en las citas.");
        return appointments;
    }

    /**
     * Parsea los parámetros de una cadena de consulta (query string) de una URL en un mapa de clave-valor.
     * Maneja claves sin valor y parámetros mal formados. Ignora claves duplicadas, manteniendo la primera aparición.
     *
     * @param query La cadena de consulta (ej: "id=1&sort=name"). Puede ser {@code null} o vacía.
     * @return Un {@link Map} que contiene los parámetros de consulta. Devuelve un mapa vacío si la consulta es nula, vacía o no contiene parámetros válidos.
     */
    private Map<String, String> parseQueryParams(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2)) // Limita a 2 para manejar valores con '=' 
                .filter(pair -> pair.length == 2 && !pair[0].isEmpty()) // Asegura que haya clave y valor
                .collect(Collectors.toMap(
                        pair -> pair[0], // Clave
                        pair -> pair[1], // Valor
                        (v1, v2) -> v1 // En caso de claves duplicadas, mantiene la primera
                ));
    }
} 