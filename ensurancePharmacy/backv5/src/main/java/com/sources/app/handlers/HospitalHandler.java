package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Hospital;
import com.sources.app.dao.HospitalDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manejador HTTP para gestionar las operaciones CRUD de los Hospitales.
 * Responde a solicitudes en el endpoint "/api2/hospitals".
 * Soporta los métodos GET, POST, PUT y OPTIONS.
 */
public class HospitalHandler implements HttpHandler {
    private final HospitalDAO hospitalDAO;
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = Logger.getLogger(HospitalHandler.class.getName());

    /**
     * Constructor para HospitalHandler.
     *
     * @param hospitalDAO El DAO para acceder a los datos de los hospitales.
     */
    public HospitalHandler(HospitalDAO hospitalDAO) {
        this.hospitalDAO = hospitalDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de hospitales.
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
            LOGGER.log(Level.SEVERE, "Error handling request", e);
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Maneja las solicitudes POST para crear un nuevo hospital.
     * Espera un cuerpo de solicitud JSON con los datos del hospital (nombre, teléfono, etc.).
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Hospital createHospital = objectMapper.readValue(requestBody, Hospital.class);
        
        // Aquí podrían ir validaciones de los campos requeridos (name, etc.)
        if (createHospital.getName() == null || createHospital.getName().trim().isEmpty()) {
             sendResponse(exchange, 400, "{\"error\": \"Hospital name is required\"}");
             return;
        }
        
        Hospital hospital = hospitalDAO.create(
                createHospital.getName(),
                createHospital.getPhone(),
                createHospital.getEmail(),
                createHospital.getAddress(),
                createHospital.getEnabled() // Asume que getEnabled no es null o tiene valor por defecto
        );
        if(hospital != null){
            sendResponse(exchange, 201, objectMapper.writeValueAsString(hospital));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create hospital\"}");
        }
    }

    /**
     * Maneja las solicitudes GET para obtener hospitales.
     * Si se proporciona un parámetro de consulta 'id', devuelve el hospital específico.
     * De lo contrario, devuelve todos los hospitales.
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
     * Maneja la obtención de un hospital específico por su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta que contiene el ID (formato: id=hospitalId).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        try {
            Long id = Long.parseLong(query.substring(3));
            Hospital hospital = hospitalDAO.getById(id);
            if(hospital != null){
                sendResponse(exchange, 200, objectMapper.writeValueAsString(hospital));
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID format\"}");
        }
    }

    /**
     * Maneja la obtención de todos los hospitales.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Hospital> list = hospitalDAO.getAll();
        sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
    }

    /**
     * Maneja las solicitudes PUT para actualizar un hospital existente.
     * Espera un cuerpo de solicitud JSON con los datos completos del hospital, incluyendo su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Hospital updateHospital = objectMapper.readValue(requestBody, Hospital.class);
        
        if (updateHospital.getIdHospital() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Hospital ID is required for update\"}");
             return;
        }
        // Aquí podrían ir más validaciones
        
        Hospital hospital = hospitalDAO.update(updateHospital);
        if(hospital != null){
            sendResponse(exchange, 200, objectMapper.writeValueAsString(hospital));
        } else {
             sendResponse(exchange, 400, "{\"error\": \"Failed to update hospital or hospital not found\"}");
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
