package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.User;
import com.sources.app.dao.UserDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Manejador HTTP para la autenticación de usuarios (login).
 * Gestiona las solicitudes POST al endpoint "/api/login".
 */
public class LoginHandler implements HttpHandler {
    private final UserDAO userDAO;
    private final ObjectMapper objectMapper;
    // Definimos el endpoint que maneja este handler
    private static final String ENDPOINT = "/api/login";

    /**
     * Constructor del manejador de login.
     * @param userDAO El DAO para acceder a los datos de los usuarios.
     */
    public LoginHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.objectMapper = new ObjectMapper();
        // No se necesita formato de fecha específico para Login
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el login.
     * Espera una solicitud POST con un cuerpo JSON conteniendo `email` y `password`.
     * Verifica las credenciales contra la base de datos.
     * @param exchange El objeto HttpExchange que representa la solicitud y respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración de CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        // Permitir solo POST y OPTIONS para el login
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS"); 
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Manejo de solicitudes preflight OPTIONS
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // Sin contenido
            return;
        }

        // Verificamos que la ruta solicitada sea la correcta
        String path = exchange.getRequestURI().getPath();
        if (!path.equalsIgnoreCase(ENDPOINT)) {
             sendErrorResponse(exchange, 404, "Endpoint no encontrado.");
            return;
        }

        // Solo permitimos el método POST para login
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            try {
                // Leemos el cuerpo de la petición (JSON con email y password)
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                // Usamos un mapa para flexibilidad, aunque podríamos usar una clase LoginRequestDTO
                @SuppressWarnings("unchecked")
                Map<String, String> loginCredentials = objectMapper.readValue(requestBody, Map.class);
                String email = loginCredentials.get("email");
                String password = loginCredentials.get("password");

                // Validar que email y password no sean nulos o vacíos
                if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
                    System.err.println("Login: faltan credenciales o están vacías.");
                     sendErrorResponse(exchange, 400, "Email y contraseña son requeridos.");
                    return;
                }

                // Intentamos autenticar al usuario usando el DAO
                 System.out.println("Intentando login para: " + email);
                User user = userDAO.login(email, password);

                if (user != null) {
                    // Login exitoso
                    System.out.println("Login exitoso para: " + user.getEmail() + " (ID: " + user.getIdUser() + ")");
                    // Devolvemos el objeto User completo (excluyendo password)
                    // En una app real, aquí generarías y devolverías un token JWT
                    user.setPassword(null); // Omitir password en la respuesta por seguridad
                    sendJsonResponse(exchange, 200, user);
                } else {
                    // Login fallido: credenciales incorrectas
                    System.out.println("Login fallido (credenciales incorrectas) para: " + email);
                     sendErrorResponse(exchange, 401, "Credenciales inválidas."); // 401 Unauthorized
                }
             } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                 System.err.println("Error al parsear JSON en Login: " + e.getMessage());
                 sendErrorResponse(exchange, 400, "Formato JSON inválido.");
             } catch (Exception e) {
                 System.err.println("Error inesperado procesando login para " + exchange.getRemoteAddress() + ": " + e.getMessage());
                 e.printStackTrace();
                 sendErrorResponse(exchange, 500, "Error interno del servidor.");
             }
        } else {
            // Si se utiliza otro método HTTP, se rechaza
             sendErrorResponse(exchange, 405, "Método no permitido. Use POST para login.");
        }
    }
    
     /**
     * Envía una respuesta JSON al cliente.
     * @param exchange El objeto HttpExchange.
     * @param statusCode El código de estado HTTP.
     * @param data El objeto a serializar como JSON.
     * @throws IOException Si ocurre un error de escritura.
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
     * Envía una respuesta de error simple con un mensaje JSON.
     * @param exchange El objeto HttpExchange.
     * @param statusCode El código de estado HTTP de error (e.g., 400, 401, 404, 500).
     * @param errorMessage El mensaje de error.
     * @throws IOException Si ocurre un error de escritura.
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
}
