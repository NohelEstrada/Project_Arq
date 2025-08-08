package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.UserDAO;
import com.sources.app.entities.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

/**
 * Manejador HTTP para las operaciones CRUD (Crear, Leer, Actualizar)
 * sobre la entidad {@link User}.
 * Gestiona las solicitudes para el endpoint "/api/users".
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/users}: Obtiene la lista de todos los usuarios.</li>
 *   <li>{@code GET /api/users/{id}}: Obtiene un usuario específico por su ID.</li>
 *   <li>{@code POST /api/users}: Crea un nuevo usuario. Realiza validaciones para evitar CUI/email duplicados.</li>
 *   <li>{@code PUT /api/users/{id}}: Actualiza un usuario existente (identificado por el ID en la ruta).</li>
 *   <li>(DELETE no está implementado en este manejador).</li>
 * </ul>
 */
public class UserHandler implements HttpHandler {
    /** DAO para acceder a los datos de la entidad User. */
    private final UserDAO userDAO;
    /** ObjectMapper para la serialización/deserialización JSON. */
    private final ObjectMapper objectMapper;
    /** Ruta base para las solicitudes gestionadas por este manejador. */
    private static final String ENDPOINT = "/api/users";
    /** Patrón Regex para extraer el ID numérico de la ruta (e.g., /api/users/123). */
    private static final Pattern ID_PATTERN = Pattern.compile(ENDPOINT + "/([0-9]+)");

    /**
     * Constructor del manejador de usuarios.
     * Inicializa el DAO de usuarios y el ObjectMapper.
     *
     * @param userDAO El DAO para interactuar con la tabla de usuarios.
     */
    public UserHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas al endpoint de usuarios.
     * Configura las cabeceras CORS, maneja solicitudes OPTIONS (preflight),
     * valida la ruta base ({@link #ENDPOINT}) y extrae un posible ID de usuario de la ruta usando {@link #ID_PATTERN}.
     * Enruta las solicitudes GET, POST y PUT a sus respectivos métodos de manejo.
     * Cualquier otro método o ruta no coincidente resulta en un error 404 o 405.
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Agregar encabezados CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Manejar solicitud preflight (OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // Sin contenido
            return;
        }

        // Obtenemos la ruta solicitada
        String path = exchange.getRequestURI().getPath();

        // Si la ruta no comienza con el endpoint, devolvemos 404
        if (!path.startsWith(ENDPOINT)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        // Verificar si es una solicitud de ID específico
        Matcher matcher = ID_PATTERN.matcher(path);
        boolean isIdRequest = matcher.matches();
        Long userId = null;
        
        if (isIdRequest) {
            try {
                userId = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
        }

        // Manejo de GET
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleGet(exchange, path, userId, isIdRequest);
        }
        // Manejo de POST (crear usuario)
        else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            handlePost(exchange);
        }
        // Manejo de PUT (actualizar usuario)
        else if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            handlePut(exchange, userId);
        }
        // Si se utiliza otro método, se rechaza con 405 (Method Not Allowed)
        else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    /**
     * Maneja las solicitudes GET a {@code /api/users} y {@code /api/users/{id}}.
     * Si se detectó un ID válido en la ruta (`isIdRequest` es true), intenta obtener
     * el usuario específico usando {@link UserDAO#findById(Long)}.
     * Si no hay ID en la ruta, obtiene la lista completa de usuarios usando {@link UserDAO#findAll()}.
     * Envía el usuario encontrado (o la lista) como respuesta JSON.
     * Responde con 404 si se busca por un ID específico y no se encuentra.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param path La ruta completa de la solicitud (usada para logging o debugging si fuera necesario).
     * @param userId El ID del usuario extraído de la ruta (puede ser {@code null} si no es una solicitud por ID).
     * @param isIdRequest Booleano que indica si la ruta coincidió con el patrón de ID.
     * @throws IOException Si ocurre un error al obtener datos o al enviar la respuesta.
     */
    private void handleGet(HttpExchange exchange, String path, Long userId, boolean isIdRequest) throws IOException {
        // Si la URL es del tipo /api/users/{id} intentamos leer un usuario por id
        if (isIdRequest) {
            User user = userDAO.findById(userId);
            if (user != null) {
                String jsonResponse = objectMapper.writeValueAsString(user);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        } else {
            // Si no se especifica un id, devolvemos todos los usuarios
            List<User> users = userDAO.findAll();
            String jsonResponse = objectMapper.writeValueAsString(users);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    /**
     * Maneja las solicitudes POST a {@code /api/users} para crear un nuevo usuario.
     * Lee el cuerpo JSON de la solicitud, esperando la estructura de un objeto {@link User}
     * (incluyendo potencialmente un objeto anidado {@link com.sources.app.entities.Policy}).
     * Realiza validaciones previas a la creación:
     * - Verifica si ya existe un usuario con el mismo email usando {@link UserDAO#existsUserWithEmail(String)}.
     * - Verifica si ya existe un usuario con el mismo CUI usando {@link UserDAO#existsUserWithCUI(String)}.
     * Si alguna de estas validaciones falla, responde con 400 (Bad Request) y un mensaje de error específico.
     * Si las validaciones pasan, llama a {@link UserDAO#create(String, String, String, String, java.util.Date, String, String, com.sources.app.entities.Policy)}
     * para guardar el nuevo usuario.
     * Responde con 201 (Created) y el objeto del usuario creado si tiene éxito.
     * Responde con 400 si la creación falla en el DAO o si el JSON es inválido.
     * Responde con 500 si ocurre un error interno inesperado.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo, interactuar con el DAO o enviar la respuesta.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            // Leemos el cuerpo de la petición y lo convertimos a un objeto User
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            User createUser = objectMapper.readValue(requestBody, User.class);

            // Verificar si el correo ya existe
            if (userDAO.existsUserWithEmail(createUser.getEmail())) {
                String errorMessage = "El correo electrónico ya está registrado";
                Map<String, String> errorResponse = Map.of(
                    "error", "email_already_exists",
                    "message", errorMessage
                );
                
                String jsonResponse = objectMapper.writeValueAsString(errorResponse);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            
            // Verificar si el CUI ya existe
            if (userDAO.existsUserWithCUI(createUser.getCui())) {
                String errorMessage = "El CUI/DPI ya está registrado";
                Map<String, String> errorResponse = Map.of(
                    "error", "cui_already_exists",
                    "message", errorMessage
                );
                
                String jsonResponse = objectMapper.writeValueAsString(errorResponse);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            // Llamamos al método create del DAO, ahora incluyendo el objeto Policy
            User user = userDAO.create(
                    createUser.getName(),
                    createUser.getCui(),
                    createUser.getPhone(),
                    createUser.getEmail(),
                    createUser.getBirthDate(),
                    createUser.getAddress(),
                    createUser.getPassword(),
                    createUser.getPolicy() // Se espera que en el JSON se incluya la información de la Policy
            );

            if (user != null) {
                // Si se creó el usuario correctamente, devolvemos el objeto en formato JSON
                String jsonResponse = objectMapper.writeValueAsString(user);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                // Se usa 201 (Created) para indicar que se creó un recurso
                exchange.sendResponseHeaders(201, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else {
                // Si no se pudo crear, devolvemos 400 Bad Request con un mensaje de error
                Map<String, String> errorResponse = Map.of(
                    "error", "user_creation_failed",
                    "message", "No se pudo crear el usuario. Verifique los datos e intente de nuevo."
                );
                
                String jsonResponse = objectMapper.writeValueAsString(errorResponse);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Devolver mensaje de error detallado
            Map<String, String> errorResponse = Map.of(
                "error", "server_error",
                "message", "Error en el servidor: " + e.getMessage()
            );
            
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    /**
     * Maneja las solicitudes PUT a {@code /api/users/{id}} para actualizar un usuario existente.
     * Requiere que se haya extraído un `userId` válido de la ruta.
     * Lee el cuerpo JSON de la solicitud, esperando la estructura de un objeto {@link User}.
     * Establece el ID del usuario en el objeto deserializado con el `userId` de la ruta para asegurar
     * que se actualiza el usuario correcto.
     * Llama a {@link UserDAO#update(User)} para aplicar los cambios.
     * Responde con 200 (OK) y el objeto del usuario actualizado si tiene éxito.
     * Responde con 400 si el JSON es inválido o si ocurre un error durante la validación previa a la actualización (potencialmente CUI/email duplicado si se implementara aquí).
     * Responde con 404 si el `userId` no corresponde a un usuario existente.
     * Responde con 500 si ocurre un error interno inesperado.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param userId El ID del usuario a actualizar, extraído de la ruta.
     * @throws IOException Si ocurre un error al leer el cuerpo, interactuar con el DAO o enviar la respuesta.
     */
    private void handlePut(HttpExchange exchange, Long userId) throws IOException {
        if (userId == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        try {
            // Verificar si el usuario existe
            User existingUser = userDAO.findById(userId);
            if (existingUser == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            // Leer el cuerpo de la petición
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            User updatedUser = objectMapper.readValue(requestBody, User.class);
            
            // Asegurarse de que el ID sea el mismo
            updatedUser.setIdUser(userId);
            
            // Reglas de negocio para la actualización
            // 1. Si paidService es false, policy debe ser null
            if (updatedUser.getPaidService() != null && !updatedUser.getPaidService()) {
                updatedUser.setPolicy(null);
                updatedUser.setExpirationDate(null);
            }
            
            // 2. Si paidService es true y expirationDate es null, se podría establecer una fecha predeterminada
            // (esta lógica se maneja en UserDAO)
            
            // Actualizar el usuario
            User result = userDAO.update(updatedUser);
            
            if (result != null) {
                // Si se actualizó correctamente, devolvemos el objeto actualizado
                String jsonResponse = objectMapper.writeValueAsString(result);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else {
                // Si no se pudo actualizar
                exchange.sendResponseHeaders(500, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }
    }
}
