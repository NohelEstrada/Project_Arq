package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Policy;
import com.sources.app.dao.PolicyDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manejador HTTP para gestionar las operaciones CRUD de las Pólizas (Policy).
 * Responde a solicitudes en el endpoint "/api2/policies".
 * Soporta los métodos GET, POST, PUT y OPTIONS.
 */
public class PolicyHandler implements HttpHandler {
    private final PolicyDAO policyDAO;
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = Logger.getLogger(PolicyHandler.class.getName());

    /**
     * Constructor para PolicyHandler.
     *
     * @param policyDAO El DAO para acceder a los datos de las pólizas.
     */
    public PolicyHandler(PolicyDAO policyDAO) {
        this.policyDAO = policyDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de pólizas.
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
            LOGGER.log(Level.SEVERE, "Error processing PolicyHandler request", e);
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Maneja las solicitudes POST para crear una nueva póliza.
     * Espera un cuerpo JSON con 'percentage' y 'enabled'.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Policy createPolicy = objectMapper.readValue(requestBody, Policy.class);
        
        // Validaciones
        if (createPolicy.getPercentage() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Percentage is required\"}");
             return;
        }
         if (createPolicy.getEnabled() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Enabled status is required\"}");
             return;
        }
        // Podrían añadirse más validaciones para el rango de percentage
        
        Policy policy = policyDAO.create(createPolicy.getPercentage(), createPolicy.getEnabled());
        if(policy != null){
            sendResponse(exchange, 201, objectMapper.writeValueAsString(policy));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create policy\"}");
        }
    }

    /**
     * Maneja las solicitudes GET para obtener pólizas.
     * Si se proporciona un parámetro de consulta 'id', devuelve la póliza específica.
     * De lo contrario, devuelve todas las pólizas.
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
     * Maneja la obtención de una póliza específica por su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta que contiene el ID (formato: id=policyId).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        try {
            Long id = Long.parseLong(query.substring(3));
            Policy policy = policyDAO.getById(id);
            if(policy != null){
                sendResponse(exchange, 200, objectMapper.writeValueAsString(policy));
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID format\"}");
        }
    }

    /**
     * Maneja la obtención de todas las pólizas.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Policy> list = policyDAO.getAll();
        sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
    }

    /**
     * Maneja las solicitudes PUT para actualizar una póliza existente.
     * Espera un cuerpo JSON con los datos completos de la póliza, incluyendo su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Policy updatePolicy = objectMapper.readValue(requestBody, Policy.class);
        
        if (updatePolicy.getIdPolicy() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Policy ID is required for update\"}");
             return;
        }
        // Podrían ir más validaciones
        
        Policy policy = policyDAO.update(updatePolicy);
        if(policy != null){
            sendResponse(exchange, 200, objectMapper.writeValueAsString(policy));
        } else {
             sendResponse(exchange, 400, "{\"error\": \"Failed to update policy or policy not found\"}");
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
