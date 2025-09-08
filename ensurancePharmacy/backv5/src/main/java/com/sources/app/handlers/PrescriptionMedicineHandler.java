package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.PrescriptionMedicineDAO;
import com.sources.app.entities.PrescriptionMedicine;
import com.sources.app.entities.PrescriptionMedicineId;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for managing HTTP requests related to prescription medicines.
 * This class implements HttpHandler and provides functionality for CRUD operations
 * on prescription medicine entities through RESTful endpoints.
 * 
 * The handler supports the following HTTP methods:
 * - POST: Creates a new prescription medicine
 * - GET: Retrieves either a specific prescription medicine by ID or all prescription medicines
 * - PUT: Updates an existing prescription medicine
 * - OPTIONS: Handles CORS preflight requests
 *
 * @author Pablo Flores Mollinedo
 * @version 1.0
 * @since 2025-04-11
 */
public class PrescriptionMedicineHandler implements HttpHandler {
    /** Data Access Object for prescription medicine operations */
    private final PrescriptionMedicineDAO prescriptionMedicineDAO;
    
    /** JSON serializer/deserializer for converting between Java objects and JSON */
    private final ObjectMapper objectMapper;
    
    /** The API endpoint path for prescription medicines */
    private static final String ENDPOINT = "/api2/prescription_medicines";
    private static final Logger LOGGER = Logger.getLogger(PrescriptionMedicineHandler.class.getName());

    /**
     * Constructs a new PrescriptionMedicineHandler with the specified DAO.
     * 
     * @param prescriptionMedicineDAO the data access object for prescription medicine operations
     */
    public PrescriptionMedicineHandler(PrescriptionMedicineDAO prescriptionMedicineDAO) {
        this.prescriptionMedicineDAO = prescriptionMedicineDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Handles HTTP requests for the prescription medicines endpoint.
     * This method processes different HTTP methods and delegates to appropriate
     * helper methods based on the request type.
     * 
     * <p>Supported operations:</p>
     * <ul>
     *   <li>OPTIONS: Handles CORS preflight requests</li>
     *   <li>POST: Creates a new prescription medicine association</li>
     *   <li>GET: Retrieves prescription medicine(s)</li>
     *   <li>PUT: Updates an existing prescription medicine</li>
     * </ul>
     *
     * <p>For POST requests: Expects a JSON with prescription (with ID), medicine (with ID), 
     * dosis, frecuencia, and duracion. Creates a new PrescriptionMedicine object and returns it in JSON format.</p>
     *
     * <p>For GET requests: If a query parameter "id=" is specified with format "prescriptionId,medicineId", 
     * it retrieves a specific prescription medicine. Otherwise, it returns all prescription medicines.</p>
     *
     * <p>For PUT requests: Expects a complete JSON representation of PrescriptionMedicine 
     * (including the composite key) to update the prescription medicine information.</p>
     *
     * @param exchange the HttpExchange object that contains the request and response data
     * @throws IOException if an I/O error occurs during the handling of the request
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers to allow cross-origin requests
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Handle CORS preflight requests
        if("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())){
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        if("POST".equalsIgnoreCase(method)){
            handlePostRequest(exchange);
        } else if("GET".equalsIgnoreCase(method)){
            handleGetRequest(exchange);
        } else if("PUT".equalsIgnoreCase(method)){
            handlePutRequest(exchange);
        } else {
            // Method not allowed
            exchange.sendResponseHeaders(405, -1);
        }
    }
    
    /**
     * Handles POST requests to create a new prescription medicine.
     * 
     * @param exchange the HttpExchange object containing the request and response data
     * @throws IOException if an I/O error occurs during request processing
     */
    private void handlePostRequest(HttpExchange exchange) throws IOException {
        try {
            // Parse request body into PrescriptionMedicine object
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            PrescriptionMedicine createPM = objectMapper.readValue(requestBody, PrescriptionMedicine.class);
            
            // Create new prescription medicine in the database
            PrescriptionMedicine pm = prescriptionMedicineDAO.create(
                    createPM.getPrescription(),
                    createPM.getMedicine(),
                    createPM.getDosis(),
                    createPM.getFrecuencia(),
                    createPM.getDuracion()
            );
            
            // Send success response with created object
            if(pm != null){
                String jsonResponse = objectMapper.writeValueAsString(pm);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(201, responseBytes.length);
                try(OutputStream os = exchange.getResponseBody()){
                    os.write(responseBytes);
                }
            } else {
                // Bad request if creation failed
                exchange.sendResponseHeaders(400, -1);
            }
        } catch(Exception e){
            // Log exception and send server error response
            LOGGER.log(Level.SEVERE, "Error creating PrescriptionMedicine", e);
            exchange.sendResponseHeaders(500, -1);
        }
    }
    
    /**
     * Handles GET requests to retrieve prescription medicine(s).
     * If query includes an ID parameter, returns a specific prescription medicine;
     * otherwise returns all prescription medicines.
     * 
     * @param exchange the HttpExchange object containing the request and response data
     * @throws IOException if an I/O error occurs during request processing
     */
    private void handleGetRequest(HttpExchange exchange) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            if(query != null && query.startsWith("id=")){
                // Get specific prescription medicine by composite ID
                handleGetById(exchange, query);
            } else {
                // Get all prescription medicines
                handleGetAll(exchange);
            }
        } catch(Exception e){
            // Log exception and send server error response
            LOGGER.log(Level.SEVERE, "Error retrieving PrescriptionMedicine(s)", e);
            exchange.sendResponseHeaders(500, -1);
        }
    }
    
    /**
     * Handles retrieval of a specific prescription medicine by its composite ID.
     * 
     * @param exchange the HttpExchange object containing the request and response data
     * @param query the query string containing the ID parameter
     * @throws IOException if an I/O error occurs during request processing
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        String[] parts = query.substring(3).split(",");
        if(parts.length == 2){
            try {
                // Parse composite ID parts
                Long presId = Long.parseLong(parts[0]);
                Long medId = Long.parseLong(parts[1]);
                PrescriptionMedicineId id = new PrescriptionMedicineId(presId, medId);
                
                // Retrieve prescription medicine
                PrescriptionMedicine pm = prescriptionMedicineDAO.getById(id);
                
                if(pm != null){
                    // Send success response with found object
                    String jsonResponse = objectMapper.writeValueAsString(pm);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try(OutputStream os = exchange.getResponseBody()){
                        os.write(responseBytes);
                    }
                } else {
                    // Not found response
                    exchange.sendResponseHeaders(404, -1);
                }
            } catch (NumberFormatException e) {
                // Invalid ID format
                exchange.sendResponseHeaders(400, -1);
            }
        } else {
            // Invalid ID parameter format
            exchange.sendResponseHeaders(400, -1);
        }
    }
    
    /**
     * Handles retrieval of all prescription medicines.
     * 
     * @param exchange the HttpExchange object containing the request and response data
     * @throws IOException if an I/O error occurs during request processing
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<PrescriptionMedicine> list = prescriptionMedicineDAO.getAll();
        String jsonResponse = objectMapper.writeValueAsString(list);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        try(OutputStream os = exchange.getResponseBody()){
            os.write(responseBytes);
        }
    }
    
    /**
     * Handles PUT requests to update an existing prescription medicine.
     * 
     * @param exchange the HttpExchange object containing the request and response data
     * @throws IOException if an I/O error occurs during request processing
     */
    private void handlePutRequest(HttpExchange exchange) throws IOException {
        try {
            // Parse request body into PrescriptionMedicine object
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            PrescriptionMedicine updatePM = objectMapper.readValue(requestBody, PrescriptionMedicine.class);
            
            // Update prescription medicine in the database
            PrescriptionMedicine pm = prescriptionMedicineDAO.update(updatePM);
            
            if(pm != null){
                // Send success response with updated object
                String jsonResponse = objectMapper.writeValueAsString(pm);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                try(OutputStream os = exchange.getResponseBody()){
                    os.write(responseBytes);
                }
            } else {
                // Bad request if update failed
                exchange.sendResponseHeaders(400, -1);
            }
        } catch(Exception e){
            // Log exception and send server error response
            LOGGER.log(Level.SEVERE, "Error updating PrescriptionMedicine", e);
            exchange.sendResponseHeaders(500, -1);
        }
    }
}
