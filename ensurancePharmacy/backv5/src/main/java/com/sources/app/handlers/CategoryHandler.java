package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Category;
import com.sources.app.dao.CategoryDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Manejador HTTP para gestionar las operaciones CRUD de las Categorías.
 * Responde a solicitudes en el endpoint "/api2/categories".
 * Soporta los métodos GET, POST, PUT y OPTIONS.
 */
public class CategoryHandler implements HttpHandler {
    private final CategoryDAO categoryDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/categories";

    /**
     * Constructor para CategoryHandler.
     *
     * @param categoryDAO El DAO para acceder a los datos de las categorías.
     */
    public CategoryHandler(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de categorías.
     * Configura las cabeceras CORS y delega a los métodos de manejo apropiados
     * según el método HTTP (handlePost, handleGet, handlePut).
     *
     * @param exchange El objeto HttpExchange que representa la solicitud y respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        // Handle CORS preflight requests
        if("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())){
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        String method = exchange.getRequestMethod();
        try {
            if("POST".equalsIgnoreCase(method)){
                handlePost(exchange);
            } else if("GET".equalsIgnoreCase(method)){
                handleGet(exchange);
            } else if("PUT".equalsIgnoreCase(method)){
                handlePut(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Maneja las solicitudes POST para crear una nueva categoría.
     * Espera un cuerpo de solicitud JSON con el nombre de la categoría.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Category createCat = objectMapper.readValue(requestBody, Category.class);
        
        if (createCat.getName() == null || createCat.getName().trim().isEmpty()) {
            sendResponse(exchange, 400, "{\"error\": \"Category name is required\"}");
            return;
        }
        
        Category category = categoryDAO.create(createCat.getName());
        if(category != null){
            sendResponse(exchange, 201, objectMapper.writeValueAsString(category));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create category\"}");
        }
    }

    /**
     * Maneja las solicitudes GET para obtener categorías.
     * Si se proporciona un parámetro de consulta 'id', devuelve la categoría específica.
     * De lo contrario, devuelve todas las categorías.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if(query != null && query.startsWith("id=")){
            handleGetById(exchange, query);
        } else {
            handleGetAll(exchange);
        }
    }

    /**
     * Maneja la obtención de una categoría específica por su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta que contiene el ID (formato: id=categoryId).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        try {
            Long id = Long.parseLong(query.substring(3));
            Category category = categoryDAO.getById(id);
            if(category != null){
                sendResponse(exchange, 200, objectMapper.writeValueAsString(category));
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID format\"}");
        }
    }

    /**
     * Maneja la obtención de todas las categorías.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Category> list = categoryDAO.getAll();
        sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
    }

    /**
     * Maneja las solicitudes PUT para actualizar una categoría existente.
     * Espera un cuerpo de solicitud JSON con los datos completos de la categoría, incluyendo su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Category updateCat = objectMapper.readValue(requestBody, Category.class);
        
        if (updateCat.getIdCategory() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Category ID is required for update\"}");
             return;
        }
        if (updateCat.getName() == null || updateCat.getName().trim().isEmpty()) {
            sendResponse(exchange, 400, "{\"error\": \"Category name is required for update\"}");
            return;
        }
        
        Category category = categoryDAO.update(updateCat);
        if(category != null){
            sendResponse(exchange, 200, objectMapper.writeValueAsString(category));
        } else {
             sendResponse(exchange, 400, "{\"error\": \"Failed to update category or category not found\"}");
        }
    }
    
    /**
     * Envía una respuesta HTTP con un código de estado y cuerpo específicos.
     *
     * @param exchange El objeto HttpExchange.
     * @param statusCode El código de estado HTTP.
     * @param responseBody El cuerpo de la respuesta como String.
     * @throws IOException Si ocurre un error de entrada/salida.
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
