package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.AppointmentMade;
import com.sources.app.dao.AppointmentMadeDAO;
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
 * sobre la entidad {@link AppointmentMade}, que representa el registro de una cita médica
 * ya realizada o completada.
 * Gestiona las solicitudes para el endpoint "/api/appointmentmade".
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/appointmentmade}: Obtiene la lista de todos los registros de citas realizadas.</li>
 *   <li>{@code GET /api/appointmentmade?id={id}}: Obtiene un registro específico de cita realizada por su ID.</li>
 *   <li>{@code POST /api/appointmentmade}: Crea un nuevo registro de cita realizada.</li>
 *   <li>{@code PUT /api/appointmentmade}: Actualiza un registro existente (requiere 'idAppointmentMade' en el cuerpo).</li>
 *   <li>(DELETE no está implementado en este manejador).</li>
 * </ul>
 */
public class AppointmentMadeHandler implements HttpHandler {

    /** DAO para acceder a los datos de la entidad AppointmentMade. */
    private final AppointmentMadeDAO appointmentMadeDAO;
    /** ObjectMapper para la serialización/deserialización JSON, configurado con formato de fecha/hora. */
    private final ObjectMapper objectMapper;
    /** Ruta base para las solicitudes gestionadas por este manejador. */
    private static final String ENDPOINT = "/api/appointmentmade";

    /**
     * Constructor del manejador de citas realizadas.
     * Inicializa el DAO y el ObjectMapper, configurando un formato de fecha/hora específico
     * ("yyyy-MM-dd HH:mm:ss") para la serialización/deserialización.
     *
     * @param appointmentMadeDAO El DAO para interactuar con la tabla {@code AppointmentMade}.
     */
    public AppointmentMadeHandler(AppointmentMadeDAO appointmentMadeDAO) {
        this.appointmentMadeDAO = appointmentMadeDAO;
        this.objectMapper = new ObjectMapper();
        // Asegura consistencia en el formato de fecha
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")); // Incluir hora si es relevante
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas al endpoint de citas realizadas.
     * Configura las cabeceras CORS, maneja solicitudes OPTIONS (preflight),
     * valida que la ruta coincida con {@link #ENDPOINT}, y enruta las solicitudes
     * GET, POST y PUT a sus respectivos métodos de manejo. Cualquier otro método resulta
     * en un error 405 (Method Not Allowed). Captura excepciones generales para devolver un error 500.
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida (generalmente manejado internamente).
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración de CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Respuesta a solicitudes OPTIONS (preflight de CORS)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        // Verifica si la ruta coincide con el endpoint esperado
        if (!path.equalsIgnoreCase(ENDPOINT)) {
            sendErrorResponse(exchange, 404, "Endpoint no encontrado.");
            return;
        }

        // Delega según el método HTTP, con manejo de errores general
        try {
            switch (exchange.getRequestMethod().toUpperCase()){
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
            System.err.println("Error inesperado en AppointmentMadeHandler: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor.");
        }
    }

    /**
     * Maneja las solicitudes GET a {@code /api/appointmentmade}.
     * Si se proporciona un parámetro de consulta 'id', intenta obtener el registro específico por ese ID.
     * Si no se proporciona 'id' o el parámetro es inválido, obtiene y devuelve la lista de todos los registros de citas realizadas.
     * Responde con 404 si el registro solicitado por ID no se encuentra.
     * Responde con 400 si el parámetro 'id' tiene un formato inválido.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        // Verifica si se busca por ID
        if(query != null && query.contains("id=")){
            Map<String,String> params = parseQuery(query);
            try{
                Long id = Long.parseLong(params.get("id"));
                AppointmentMade am = appointmentMadeDAO.findById(id);
                if(am != null){
                    // Cita encontrada, enviar respuesta JSON
                    sendJsonResponse(exchange, 200, am);
                } else {
                    // Cita no encontrada
                    sendErrorResponse(exchange, 404, "Cita realizada no encontrada con ID: " + id);
                }
            } catch(NumberFormatException e){
                // ID inválido
                sendErrorResponse(exchange, 400, "ID de cita realizada inválido.");
            }
        } else {
            // Obtener todas las citas realizadas
            List<AppointmentMade> list = appointmentMadeDAO.findAll();
            sendJsonResponse(exchange, 200, list);
        }
    }

    /**
     * Maneja las solicitudes POST a {@code /api/appointmentmade}.
     * Crea un nuevo registro de cita realizada basado en el cuerpo JSON de la solicitud.
     * El cuerpo JSON debe contener los campos requeridos: `idCita`, `idUser`, y `appointmentMadeDate`.
     * Responde con 201 (Created) y el objeto {@link AppointmentMade} creado si tiene éxito.
     * Responde con 400 si faltan campos requeridos, si los datos son inválidos (e.g., formato de fecha) o si el JSON es inválido.
     * Responde con 500 si ocurre un error al interactuar con la base de datos.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            // Lee y parsea el cuerpo de la solicitud
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            AppointmentMade am = objectMapper.readValue(requestBody, AppointmentMade.class);

            // Validar datos requeridos
            if (am.getIdCita() == null || am.getIdUser() == null || am.getAppointmentMadeDate() == null) {
                sendErrorResponse(exchange, 400, "Datos incompletos para crear la cita realizada (idCita, idUser, appointmentMadeDate requeridos).");
                return;
            }

            // Crea la cita en la base de datos
            AppointmentMade created = appointmentMadeDAO.create(
                    am.getIdCita(),
                    am.getIdUser(),
                    am.getAppointmentMadeDate()
            );
            if(created != null){
                // Cita creada exitosamente, enviar respuesta JSON
                 sendJsonResponse(exchange, 201, created);
            } else {
                // Error al crear la cita
                 System.err.println("Error en DAO al crear AppointmentMade.");
                sendErrorResponse(exchange, 500,"Error interno al crear el registro de cita realizada.");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
             System.err.println("Error al parsear JSON en POST AppointmentMade: " + e.getMessage());
             sendErrorResponse(exchange, 400, "Formato JSON inválido.");
        } catch(Exception e){
            System.err.println("Error inesperado en handlePost AppointmentMade: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor al procesar la solicitud.");
        }
    }

    /**
     * Maneja las solicitudes PUT a {@code /api/appointmentmade}.
     * Actualiza un registro de cita realizada existente basado en el cuerpo JSON de la solicitud.
     * El cuerpo JSON *debe* contener 'idAppointmentMade' para identificar el registro a actualizar.
     * Otros campos presentes en el JSON (idCita, idUser, appointmentMadeDate) se usarán para la actualización.
     * Responde con 200 (OK) y el objeto {@link AppointmentMade} actualizado si tiene éxito.
     * Responde con 400 si falta 'idAppointmentMade' o si el JSON es inválido.
     * Responde con 404 si el 'idAppointmentMade' proporcionado no corresponde a un registro existente o si la actualización falla en el DAO por otra razón.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        try {
            // Lee y parsea el cuerpo de la solicitud
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            AppointmentMade am = objectMapper.readValue(requestBody, AppointmentMade.class);

            // Validar que el ID esté presente para la actualización
            if (am.getIdCita() == null) { 
                 sendErrorResponse(exchange, 400, "Falta el idCita para actualizar.");
                 return;
            }
            // Podrían añadirse más validaciones aquí (e.g., idUser, date no nulos si se actualizan)

            // Actualiza la cita en la base de datos
            AppointmentMade updated = appointmentMadeDAO.update(am);
            if(updated != null){
                // Cita actualizada exitosamente, enviar respuesta JSON
                sendJsonResponse(exchange, 200, updated);
            } else {
                // Error al actualizar (posiblemente no encontrada o error interno)
                System.err.println("Error en DAO al actualizar AppointmentMade o no encontrada (ID Cita: " + am.getIdCita() + ")."); 
                sendErrorResponse(exchange, 404, "Registro de cita realizada (ID Cita: " + am.getIdCita() + ") no encontrado o error al actualizar.");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("Error al parsear JSON en PUT AppointmentMade: " + e.getMessage());
            sendErrorResponse(exchange, 400, "Formato JSON inválido.");
        } catch (Exception e) {
            System.err.println("Error inesperado en handlePut AppointmentMade: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor al procesar la actualización.");
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
        String jsonResponse = objectMapper.writeValueAsString(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
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
     * @param errorMessage El mensaje descriptivo del error.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        Map<String, Object> errorResponse = Map.of("success", false, "message", errorMessage);
        try {
             // Verifica si los headers ya fueron enviados antes de intentar escribir
             if (!exchange.getResponseHeaders().containsKey("Content-Type")) { 
                 sendJsonResponse(exchange, statusCode, errorResponse);
             } else {
                 System.err.println("Intento de enviar respuesta de error cuando los headers ya fueron enviados. Status: " + statusCode + ", Msg: " + errorMessage);
             }
        } catch (IOException e) {
            System.err.println("Error al enviar respuesta de error: " + e.getMessage());
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
