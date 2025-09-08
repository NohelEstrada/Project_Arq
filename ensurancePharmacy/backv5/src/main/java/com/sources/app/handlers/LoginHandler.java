package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.User;
import com.sources.app.dao.UserDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
 import java.util.logging.Level;
 import java.util.logging.Logger;

/**
 * Manejador HTTP para gestionar las solicitudes de inicio de sesión de usuarios.
 * Responde a solicitudes POST en el endpoint "/api2/login".
 * Espera un cuerpo JSON con "email" y "password".
 * Utiliza UserDAO para verificar las credenciales.
 */
public class LoginHandler implements HttpHandler {
    private final UserDAO userDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/login";

    private static final Logger LOGGER = Logger.getLogger(LoginHandler.class.getName());

    /**
     * Constructor para LoginHandler.
     *
     * @param userDAO El DAO para acceder a los datos de los usuarios y verificar credenciales.
     */
    public LoginHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de login.
     * Configura las cabeceras CORS.
     * Solo procesa solicitudes POST. Verifica la ruta y el método.
     * Extrae email y contraseña del cuerpo JSON, llama a userDAO.login().
     * Si la autenticación es exitosa, responde con 200 OK y los datos del usuario en JSON.
     * Si la autenticación falla, responde con 401 Unauthorized.
     * Responde con 404 Not Found si la ruta no es correcta.
     * Responde con 405 Method Not Allowed para métodos diferentes de POST y OPTIONS.
     * Responde con 500 Internal Server Error si ocurre una excepción.
     *
     * @param exchange El objeto HttpExchange que representa la solicitud y respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Encabezados CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Manejo de OPTIONS (preflight)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Verificar que la ruta sea la correcta
        String path = exchange.getRequestURI().getPath();
        if (!path.equalsIgnoreCase(ENDPOINT)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        // Solo permitimos el método POST para login
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                // Se espera un JSON con email y password
                User loginUser = objectMapper.readValue(requestBody, User.class);

                // Intentamos autenticar al usuario
                User user = userDAO.login(loginUser.getEmail(), loginUser.getPassword());

                if (user != null) {
                    // Login exitoso: devolvemos el usuario en JSON (o se podría retornar un token)
                    String jsonResponse = objectMapper.writeValueAsString(user);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                } else {
                    // Login fallido: credenciales incorrectas
                    exchange.sendResponseHeaders(401, -1);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error handling login request", e);
                exchange.sendResponseHeaders(500, -1);
            }
        } else {
            // Si se utiliza otro método, se rechaza con 405 (Method Not Allowed)
            exchange.sendResponseHeaders(405, -1);
        }
    }
}
