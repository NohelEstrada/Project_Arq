package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.User;
import com.sources.app.dao.UserDAO;
import com.sources.app.dto.UserCreateRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP handler for managing CRUD operations for {@link User} entities.
 * Responds to requests at the "/api2/users" endpoint.
 * Supports GET, POST, PUT, and OPTIONS methods.
 */
public class UserHandler implements HttpHandler {
    private final UserDAO userDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/users";
    private static final Logger LOGGER = Logger.getLogger(UserHandler.class.getName());

    /**
     * Constructor for UserHandler.
     *
     * @param userDAO The DAO for accessing user data.
     */
    public UserHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Handles incoming HTTP requests for the users endpoint.
     * Sets CORS headers and delegates to appropriate handler methods
     * based on the HTTP method (handlePost, handleGet, handlePut).
     *
     * @param exchange The {@link HttpExchange} object representing the request and response.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Handle CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        try {
             if ("POST".equalsIgnoreCase(method)) {
                handlePost(exchange);
            } else if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange);
            } else if ("PUT".equalsIgnoreCase(method)) {
                handlePut(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing UserHandler request", e);
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Handles POST requests to create a new user.
     * Expects a JSON body with user details (name, CUI, phone, email, birthDate, address, password).
     * <p>
     * **Security Note:** This handler passes the received password directly to the DAO.
     * Ensure that the {@link UserDAO#create} method (or logic before calling it) handles password hashing.
     * </p>
     *
     * @param exchange The {@link HttpExchange} object.
     * @throws IOException If an I/O error occurs or JSON processing fails.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        User createUser = objectMapper.readValue(requestBody, User.class);
        
        // Aquí se podrían añadir validaciones para los campos requeridos y formatos
        if (createUser.getEmail() == null || createUser.getPassword() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Email and password are required\"}");
             return;
        }

        UserCreateRequest request = new UserCreateRequest(
                createUser.getName(),
                createUser.getCui(),
                createUser.getPhone(),
                createUser.getEmail(),
                createUser.getBirthDate(),
                createUser.getAddress(),
                createUser.getPassword() // Ensure DAO handles hashing
        );
        User user = userDAO.create(request);
        if (user != null) {
            // Omit password in the response
            user.setPassword(null); 
            sendResponse(exchange, 201, objectMapper.writeValueAsString(user));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create user (e.g., email already exists)\"}");
        }
    }

    /**
     * Handles GET requests to retrieve users.
     * If an 'id' query parameter is provided, retrieves the specific user.
     * Otherwise, retrieves all users.
     * Passwords are omitted from the response.
     *
     * @param exchange The {@link HttpExchange} object.
     * @throws IOException If an I/O error occurs.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.startsWith("id=")) {
            handleGetById(exchange, query);
        } else {
            handleGetAll(exchange);
        }
    }

    /**
     * Handles retrieving a specific user by their ID provided in the query string.
     * The user's password is omitted from the response.
     *
     * @param exchange The {@link HttpExchange} object.
     * @param query    The query string containing the user ID (format: id=userId).
     * @throws IOException If an I/O error occurs or JSON processing fails.
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        try {
            Long id = Long.parseLong(query.substring(3));
            User user = userDAO.getById(id);
            if (user != null) {
                // Omit password in the response
                user.setPassword(null); 
                sendResponse(exchange, 200, objectMapper.writeValueAsString(user));
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID format\"}");
        }
    }

    /**
     * Handles retrieving all users.
     * Passwords are omitted from the response list.
     *
     * @param exchange The {@link HttpExchange} object.
     * @throws IOException If an I/O error occurs or JSON processing fails.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<User> users = userDAO.getAll();
        // Omit passwords from the list
        users.forEach(u -> u.setPassword(null)); 
        sendResponse(exchange, 200, objectMapper.writeValueAsString(users));
    }

    /**
     * Handles PUT requests to update an existing user.
     * Expects a JSON body with the complete user data, including their ID.
     * Note: The password is explicitly set to null before updating, so this endpoint
     * is not intended for password changes. A separate, secure endpoint should handle that.
     *
     * @param exchange The {@link HttpExchange} object.
     * @throws IOException If an I/O error occurs or JSON processing fails.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        User updateUser = objectMapper.readValue(requestBody, User.class);
        
        if (updateUser.getIdUser() == null) {
             sendResponse(exchange, 400, "{\"error\": \"User ID is required for update\"}");
             return;
        }
        // Ensure password is not included or is ignored in the update
        updateUser.setPassword(null); 
        
        User user = userDAO.update(updateUser);
        if (user != null) {
            // Omit password in the response
            user.setPassword(null);
            sendResponse(exchange, 200, objectMapper.writeValueAsString(user));
        } else {
             sendResponse(exchange, 400, "{\"error\": \"Failed to update user or user not found\"}");
        }
    }
    
    /**
     * Sends an HTTP response with a specific status code and JSON body.
     *
     * @param exchange     The {@link HttpExchange} object.
     * @param statusCode   The HTTP status code.
     * @param responseBody The response body as a JSON String.
     * @throws IOException If an I/O error occurs writing the response.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
