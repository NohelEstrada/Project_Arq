package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.SubcategoryDAO;
import com.sources.app.entities.Subcategory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Manejador HTTP para gestionar las operaciones CRUD de las Subcategorías (Subcategory).
 * Responde a solicitudes en el endpoint "/api2/subcategories".
 * Soporta los métodos GET, POST, PUT y OPTIONS.
 */
public class SubcategoryHandler implements HttpHandler {
    private final SubcategoryDAO subcategoryDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/subcategories";

    /**
     * Constructor para SubcategoryHandler.
     *
     * @param subcategoryDAO El DAO para acceder a los datos de las subcategorías.
     */
    public SubcategoryHandler(SubcategoryDAO subcategoryDAO) {
        this.subcategoryDAO = subcategoryDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de subcategorías.
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
     * Maneja las solicitudes POST para crear una nueva subcategoría.
     * Espera un cuerpo JSON con el 'name'.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Subcategory createSubcat = objectMapper.readValue(requestBody, Subcategory.class);
        
        if (createSubcat.getName() == null || createSubcat.getName().trim().isEmpty()) {
             sendResponse(exchange, 400, "{\"error\": \"Subcategory name is required\"}");
             return;
        }
                
        Subcategory subcategory = subcategoryDAO.create(createSubcat.getName());
        if(subcategory != null){
            sendResponse(exchange, 201, objectMapper.writeValueAsString(subcategory));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create subcategory\"}");
        }
    }

    /**
     * Maneja las solicitudes GET para obtener subcategorías.
     * Si se proporciona un parámetro de consulta 'id', devuelve la subcategoría específica.
     * De lo contrario, devuelve todas las subcategorías.
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
     * Maneja la obtención de una subcategoría específica por su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta que contiene el ID (formato: id=subcategoryId).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        try {
            Long id = Long.parseLong(query.substring(3));
            Subcategory subcategory = subcategoryDAO.getById(id);
            if(subcategory != null){
                sendResponse(exchange, 200, objectMapper.writeValueAsString(subcategory));
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID format\"}");
        }
    }

    /**
     * Maneja la obtención de todas las subcategorías.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Subcategory> list = subcategoryDAO.getAll();
        sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
    }

    /**
     * Maneja las solicitudes PUT para actualizar una subcategoría existente.
     * Espera un cuerpo JSON con los datos completos de la subcategoría, incluyendo su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Subcategory updateSubcat = objectMapper.readValue(requestBody, Subcategory.class);
        
        if (updateSubcat.getIdSubcategory() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Subcategory ID is required for update\"}");
             return;
        }
         if (updateSubcat.getName() == null || updateSubcat.getName().trim().isEmpty()) {
             sendResponse(exchange, 400, "{\"error\": \"Subcategory name is required for update\"}");
             return;
        }
                
        Subcategory subcategory = subcategoryDAO.update(updateSubcat);
        if(subcategory != null){
            sendResponse(exchange, 200, objectMapper.writeValueAsString(subcategory));
        } else {
             sendResponse(exchange, 400, "{\"error\": \"Failed to update subcategory or subcategory not found\"}");
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
