package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.AppointmentDAO;
import com.sources.app.entities.Appointment;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.User;
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
 * sobre la entidad {@link Appointment}, que representa citas médicas programadas.
 * Gestiona las solicitudes para el endpoint "/api/appointment".
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/appointment}: Obtiene la lista de todas las citas.</li>
 *   <li>{@code GET /api/appointment?id={id}}: Obtiene una cita específica por su ID.</li>
 *   <li>{@code GET /api/appointment?user_id={userId}}: Obtiene todas las citas para un usuario específico.</li>
 *   <li>{@code POST /api/appointment}: Crea una nueva cita. Requiere `hospital` (con `idHospital`), `user` (con `idUser`) y `appointmentDate` en el cuerpo JSON.</li>
 *   <li>{@code PUT /api/appointment}: Actualiza una cita existente. Requiere `idAppointment` en el cuerpo JSON.</li>
 *   <li>(DELETE no está implementado en este manejador).</li>
 * </ul>
 */
public class AppointmentHandler implements HttpHandler {

    /** DAO para acceder a los datos de la entidad Appointment. */
    private final AppointmentDAO appointmentDAO;
    /** ObjectMapper para la serialización/deserialización JSON, configurado con formato de fecha yyyy-MM-dd. */
    private final ObjectMapper objectMapper;
    /** Ruta base para las solicitudes gestionadas por este manejador. */
    private static final String ENDPOINT = "/api/appointment";

    /**
     * Constructor del manejador de citas.
     * Inicializa el DAO de citas y el ObjectMapper, configurando un formato específico
     * para la serialización/deserialización de fechas ("yyyy-MM-dd").
     *
     * @param appointmentDAO El DAO para interactuar con la tabla de citas {@link Appointment}.
     */
    public AppointmentHandler(AppointmentDAO appointmentDAO) {
        this.appointmentDAO = appointmentDAO;
        this.objectMapper = new ObjectMapper();
        // Define el formato de fecha esperado/utilizado globalmente para las citas
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas al endpoint de citas.
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
        // Configuración CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Manejo de preflight OPTIONS
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

        // Enrutamiento según método HTTP
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
             System.err.println("Error inesperado en AppointmentHandler: " + e.getMessage());
             e.printStackTrace();
             sendErrorResponse(exchange, 500, "Error interno del servidor.");
        }
    }

    /**
     * Maneja las solicitudes GET a {@code /api/appointment}.
     * Permite filtrar por ID de cita (`?id=...`) o por ID de usuario (`?user_id=...`).
     * Si se proporciona `id`, busca y devuelve esa cita específica.
     * Si se proporciona `user_id`, busca y devuelve todas las citas para ese usuario.
     * Si no se proporcionan filtros válidos o el filtro no arroja resultados (excepto 404 para ID específico),
     * devuelve la lista completa de todas las citas.
     * Responde con 404 si se busca por un ID específico y no se encuentra.
     * Responde con 400 si el formato del ID (cita o usuario) es inválido.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        List<Appointment> appointments = null;
        Appointment singleAppointment = null;

        if (query != null) {
            Map<String, String> params = parseQuery(query);
            if (params.containsKey("id")) {
                try {
                    Long id = Long.parseLong(params.get("id"));
                    singleAppointment = appointmentDAO.findById(id);
                    if (singleAppointment == null) {
                        sendErrorResponse(exchange, 404, "Cita no encontrada con ID: " + id);
                        return;
                    }
                } catch (NumberFormatException e) {
                     sendErrorResponse(exchange, 400, "ID de cita inválido.");
                     return;
                }
            } else if (params.containsKey("user_id")) {
                try {
                    Long userId = Long.parseLong(params.get("user_id"));
                    appointments = appointmentDAO.findByUserId(userId);
                } catch (NumberFormatException e) {
                     sendErrorResponse(exchange, 400, "ID de usuario inválido.");
                     return;
                }
            }
        }
        
        // Si no se encontró por ID ni por user_id (o no había query), buscar todas
        if (singleAppointment == null && appointments == null) {
            appointments = appointmentDAO.findAll();
        }

        // Enviar la respuesta
        if (singleAppointment != null) {
             sendJsonResponse(exchange, 200, singleAppointment);
        } else if (appointments != null) { // Puede ser la lista por user_id o todas
             sendJsonResponse(exchange, 200, appointments);
        } else {
            // Este caso no debería ocurrir si la lógica anterior es correcta, pero por si acaso:
            sendErrorResponse(exchange, 500, "Error inesperado al obtener citas.");
        }
    }

    /**
     * Maneja las solicitudes POST a {@code /api/appointment} para crear una nueva cita.
     * Lee el cuerpo JSON, esperando una estructura que incluya objetos anidados para `hospital` y `user`,
     * cada uno conteniendo su respectivo ID (`idHospital`, `idUser`), además de `appointmentDate`.
     * Valida que estos campos requeridos estén presentes.
     * Llama a {@link AppointmentDAO#create(Long, Long, java.util.Date, Integer)} para guardar la nueva cita.
     * El campo `enabled` es opcional en el request; se pasará el valor recibido o null al DAO.
     * Responde con 201 (Created) y el objeto de la cita creada si tiene éxito.
     * Responde con 400 si faltan campos requeridos, si los IDs no están presentes en los objetos anidados, o si el JSON es inválido.
     * Responde con 500 si ocurre un error interno durante la creación en la base de datos.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Appointment appointment = objectMapper.readValue(requestBody, Appointment.class);

            // Validar datos de entrada
            Hospital hospital = appointment.getHospital();
            User user = appointment.getUser();
            if (hospital == null || hospital.getIdHospital() == null ||
                user == null || user.getIdUser() == null ||
                appointment.getAppointmentDate() == null) {
                sendErrorResponse(exchange, 400, "Datos incompletos para crear la cita (hospitalId, userId, appointmentDate requeridos).");
                return;
            }

            // Crear la cita usando los IDs
            Appointment createdAppointment = appointmentDAO.create(
                    hospital.getIdHospital(),
                    user.getIdUser(),
                    appointment.getAppointmentDate(),
                    appointment.getEnabled() // Usar el valor de enabled del request, o default si es null
            );
            
            if (createdAppointment != null) {
                sendJsonResponse(exchange, 201, createdAppointment);
            } else {
                 System.err.println("Error en DAO al crear cita.");
                sendErrorResponse(exchange, 500, "Error interno al crear la cita.");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
             System.err.println("Error al parsear JSON en POST: " + e.getMessage());
             sendErrorResponse(exchange, 400, "Formato JSON inválido.");
        } catch (Exception e) {
            System.err.println("Error inesperado en handlePost: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor al procesar la solicitud.");
        }
    }

    /**
     * Maneja las solicitudes PUT a {@code /api/appointment} para actualizar una cita existente.
     * Lee el cuerpo JSON de la solicitud, que *debe* contener el ID primario (`idAppointment`) de la cita a actualizar.
     * Valida la presencia del ID y otros campos opcionales que se puedan actualizar (hospital, user, date, enabled).
     * Si se incluyen `hospital` o `user` en el JSON, valida que contengan sus respectivos IDs.
     * Llama a {@link AppointmentDAO#update(Appointment)} para aplicar los cambios.
     * Responde con 200 (OK) y el objeto de la cita actualizada si tiene éxito.
     * Responde con 400 si falta `idAppointment`, si faltan los IDs en `hospital` o `user` (si se incluyen), si los datos son inválidos o si el JSON es inválido.
     * Responde con 404 si no se encuentra una cita con el `idAppointment` proporcionado.
     * Responde con 500 si ocurre un error interno durante la actualización.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
         try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Appointment appointment = objectMapper.readValue(requestBody, Appointment.class);

            // Validar que el ID de la cita esté presente para la actualización
            if (appointment.getIdAppointment() == null) {
                sendErrorResponse(exchange, 400, "Falta el campo 'idAppointment' para actualizar.");
                return;
            }
             // Validar IDs anidados si los objetos hospital/user están presentes
            if (appointment.getHospital() != null && appointment.getHospital().getIdHospital() == null) {
                 sendErrorResponse(exchange, 400, "Falta 'idHospital' dentro del objeto 'hospital'.");
                 return;
            }
            if (appointment.getUser() != null && appointment.getUser().getIdUser() == null) {
                 sendErrorResponse(exchange, 400, "Falta 'idUser' dentro del objeto 'user'.");
                 return;
            }

            // Actualizar la cita en la base de datos
            Appointment updatedAppointment = appointmentDAO.update(appointment);

            if (updatedAppointment != null) {
                sendJsonResponse(exchange, 200, updatedAppointment);
            } else {
                 // Podría ser 404 si el ID no existía
                 System.err.println("Error en DAO al actualizar cita o no encontrada (ID: " + appointment.getIdAppointment() + ").");
                 sendErrorResponse(exchange, 404, "Cita no encontrada con ID: " + appointment.getIdAppointment() + " o error al actualizar.");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("Error al parsear JSON en PUT: " + e.getMessage());
            sendErrorResponse(exchange, 400, "Formato JSON inválido.");
        } catch (Exception e) {
            System.err.println("Error inesperado en handlePut: " + e.getMessage());
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
        // Asegurarse de que no se intente escribir en la respuesta si ya se enviaron headers
        // (Podría pasar si ocurre un error después de una escritura parcial, aunque es menos común aquí)
        try {
            sendJsonResponse(exchange, statusCode, errorResponse);
        } catch (IOException e) {
            System.err.println("Error al enviar respuesta de error: " + e.getMessage());
            // No se puede hacer mucho más aquí si falla la escritura de la respuesta de error
        }
    }

    /**
     * Parsea los parámetros de una cadena de consulta (query string) de una URL en un mapa de clave-valor.
     * Maneja claves sin valor y parámetros mal formados. Ignora claves duplicadas, manteniendo la primera aparición.
     *
     * @param query La cadena de consulta (ej: "id=1&sort=name"). Puede ser {@code null} o vacía.
     * @return Un {@link Map} que contiene los parámetros de consulta. Devuelve un mapa vacío si la consulta es nula, vacía o no contiene parámetros válidos.
     */
    private Map<String, String> parseQuery(String query) {
        if (query == null || query.isEmpty()) {
            return Map.of(); // Devuelve un mapa vacío inmutable
        }
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2)) // Limita a 2 para manejar valores con '='
                .filter(pair -> pair.length == 2 && !pair[0].isEmpty()) // Asegura clave y valor no vacíos
                .collect(Collectors.toMap(
                        pair -> pair[0],
                        pair -> pair[1],
                        (v1, v2) -> v1 // En caso de claves duplicadas, toma la primera
                ));
    }
}
