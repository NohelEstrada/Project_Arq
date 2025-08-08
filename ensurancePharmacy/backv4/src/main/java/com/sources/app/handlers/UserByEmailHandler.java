package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.User;
import com.sources.app.dao.UserDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UserByEmailHandler implements HttpHandler {
    private final UserDAO userDAO;
    private final ObjectMapper objectMapper;
    private static final String BASE_PATH = "/api/users/by-email/";

    public UserByEmailHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración de CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // Sin contenido
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path.startsWith(BASE_PATH) && "GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            // Extraer el email de la URL
            String email = path.substring(BASE_PATH.length());
            
            // Se puede decodificar la URL si es necesario
            email = java.net.URLDecoder.decode(email, StandardCharsets.UTF_8);
            
            // Buscar el usuario por email
            User user = userDAO.findByEmail(email);
            
            if (user != null) {
                // Usuario encontrado, devolver JSON
                String jsonResponse = objectMapper.writeValueAsString(user);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else {
                // Usuario no encontrado
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No se encontró un usuario con el correo: " + email);
                
                String jsonError = objectMapper.writeValueAsString(errorResponse);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonError.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }
        } else {
            // Método no permitido o ruta incorrecta
            exchange.sendResponseHeaders(404, -1);
        }
    }
} 