package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Prescription;
import com.sources.app.dao.PrescriptionDAO;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manejador HTTP para gestionar las operaciones CRUD de las Prescripciones (Prescription).
 * Responde a solicitudes en el endpoint "/api2/prescriptions".
 * Soporta los métodos GET, POST, PUT y OPTIONS.
 */
public class PrescriptionHandler implements HttpHandler {
    private final PrescriptionDAO prescriptionDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/prescriptions";
    private static final Logger LOGGER = Logger.getLogger(PrescriptionHandler.class.getName());

    /**
     * Constructor para PrescriptionHandler.
     *
     * @param prescriptionDAO El DAO para acceder a los datos de las prescripciones.
     */
    public PrescriptionHandler(PrescriptionDAO prescriptionDAO) {
        this.prescriptionDAO = prescriptionDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de prescripciones.
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
            LOGGER.log(Level.SEVERE, "Error processing PrescriptionHandler request", e);
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Maneja las solicitudes POST para crear una nueva prescripción.
     * Espera un cuerpo JSON con los IDs de hospital y usuario, y el estado 'approved'.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Prescription createPres = objectMapper.readValue(requestBody, Prescription.class);
        
        // Validaciones
        Hospital hospital = createPres.getHospital();
        User user = createPres.getUser();
        if (hospital == null || hospital.getIdHospital() == null || user == null || user.getIdUser() == null) {
            sendResponse(exchange, 400, "{\"error\": \"Hospital ID and User ID are required\"}");
            return;
        }
         if (createPres.getApproved() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Approved status is required\"}");
             return;
        }
                
        Prescription prescription = prescriptionDAO.create(hospital, user, createPres.getApproved());
        if(prescription != null){
            sendResponse(exchange, 201, objectMapper.writeValueAsString(prescription));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create prescription\"}");
        }
    }

    /**
     * Maneja las solicitudes GET para obtener prescripciones.
     * Si se proporciona un parámetro de consulta 'id', devuelve la prescripción específica.
     * De lo contrario, devuelve todas las prescripciones.
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
     * Maneja la obtención de una prescripción específica por su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta que contiene el ID (formato: id=prescriptionId).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        try {
            Long id = Long.parseLong(query.substring(3));
            Prescription prescription = prescriptionDAO.getById(id);
            if(prescription != null){
                sendResponse(exchange, 200, objectMapper.writeValueAsString(prescription));
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID format\"}");
        }
    }

    /**
     * Maneja la obtención de todas las prescripciones.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Prescription> list = prescriptionDAO.getAll();
        sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
    }

    /**
     * Maneja las solicitudes PUT para actualizar una prescripción existente.
     * Espera un cuerpo JSON con los datos completos de la prescripción, incluyendo su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Prescription updatePres = objectMapper.readValue(requestBody, Prescription.class);
        
        if (updatePres.getIdPrescription() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Prescription ID is required for update\"}");
             return;
        }
        // Podrían ir más validaciones
        
        Prescription prescription = prescriptionDAO.update(updatePres);
        if(prescription != null){
            sendResponse(exchange, 200, objectMapper.writeValueAsString(prescription));
        } else {
             sendResponse(exchange, 400, "{\"error\": \"Failed to update prescription or prescription not found\"}");
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
