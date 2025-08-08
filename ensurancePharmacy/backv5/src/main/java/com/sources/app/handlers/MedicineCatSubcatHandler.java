package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.MedicineCatSubcatDAO;
import com.sources.app.entities.MedicineCatSubcat;
import com.sources.app.entities.MedicineCatSubcatId;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.sources.app.entities.Medicine;
import com.sources.app.entities.Category;
import com.sources.app.entities.Subcategory;

/**
 * HTTP handler for managing CRUD operations on the relationship between
 * Medicines, Categories, and Subcategories ({@link MedicineCatSubcat}).
 * Responds to requests at the "/api2/medicine_catsubcats" endpoint.
 * Supports GET, POST, PUT, and OPTIONS methods.
 * Uses a composite key ({@link MedicineCatSubcatId}) to identify entities.
 */
public class MedicineCatSubcatHandler implements HttpHandler {
    private final MedicineCatSubcatDAO mcsDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/medicine_catsubcats";

    /**
     * Constructor for MedicineCatSubcatHandler.
     *
     * @param mcsDAO The DAO for accessing medicine-category-subcategory relationship data.
     */
    public MedicineCatSubcatHandler(MedicineCatSubcatDAO mcsDAO) {
        this.mcsDAO = mcsDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Handles incoming HTTP requests for the medicine-category-subcategory relationship endpoint.
     * Sets CORS headers and delegates to appropriate handler methods based on the HTTP method
     * (handlePost, handleGet, handlePut).
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
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Handles POST requests to create a new medicine-category-subcategory association.
     * Expects a JSON body containing the IDs of the related {@link Medicine}, {@link Category}, and {@link Subcategory}.
     *
     * @param exchange The {@link HttpExchange} object.
     * @throws IOException If an I/O error occurs or JSON processing fails.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        MedicineCatSubcat createObj = objectMapper.readValue(requestBody, MedicineCatSubcat.class);
        
        // Validar IDs
        Medicine medicine = createObj.getMedicine();
        Category category = createObj.getCategory();
        Subcategory subcategory = createObj.getSubcategory();
        if (medicine == null || medicine.getIdMedicine() == null ||
            category == null || category.getIdCategory() == null ||
            subcategory == null || subcategory.getIdSubcategory() == null) {
            sendResponse(exchange, 400, "{\"error\": \"Medicine ID, Category ID, and Subcategory ID are required\"}");
            return;
        }
        
        MedicineCatSubcat mcs = mcsDAO.create(medicine, category, subcategory);
        if (mcs != null) {
            sendResponse(exchange, 201, objectMapper.writeValueAsString(mcs));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create association\"}");
        }
    }

    /**
     * Handles GET requests to retrieve associations.
     * If an 'id' query parameter is provided (format: "id=medId,catId,subcatId"),
     * it retrieves the specific association by its composite ID.
     * Otherwise, it retrieves all associations.
     *
     * @param exchange The {@link HttpExchange} object.
     * @throws IOException If an I/O error occurs.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if(query != null && query.startsWith("id=")) {
            handleGetById(exchange, query);
        } else {
            handleGetAll(exchange);
        }
    }

    /**
     * Handles retrieving a specific association by its composite ID provided in the query string.
     *
     * @param exchange The {@link HttpExchange} object.
     * @param query    The query string containing the composite ID (format: "id=medId,catId,subcatId").
     * @throws IOException If an I/O error occurs or JSON processing fails.
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        String[] parts = query.substring(3).split(",");
        if(parts.length == 3) {
            try {
                Long medId = Long.parseLong(parts[0]);
                Long catId = Long.parseLong(parts[1]);
                Long subcatId = Long.parseLong(parts[2]);
                MedicineCatSubcatId id = new MedicineCatSubcatId(medId, catId, subcatId);
                MedicineCatSubcat mcs = mcsDAO.getById(id);
                if(mcs != null) {
                    sendResponse(exchange, 200, objectMapper.writeValueAsString(mcs));
                } else {
                    exchange.sendResponseHeaders(404, -1); // Not Found
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid composite ID format (expected: medId,catId,subcatId)\"}");
            }
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID parameter format (expected: id=medId,catId,subcatId)\"}");
        }
    }

    /**
     * Handles retrieving all medicine-category-subcategory associations.
     *
     * @param exchange The {@link HttpExchange} object.
     * @throws IOException If an I/O error occurs or JSON processing fails.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<MedicineCatSubcat> list = mcsDAO.getAll();
        sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
    }

    /**
     * Handles PUT requests to update an existing association.
     * Expects a JSON body with the complete data, including the composite key.
     * Note: The practical use of updating a link entity might be limited, as the key defines the relationship.
     *
     * @param exchange The {@link HttpExchange} object.
     * @throws IOException If an I/O error occurs or JSON processing fails.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        MedicineCatSubcat updateObj = objectMapper.readValue(requestBody, MedicineCatSubcat.class);
        
        // Validar clave compuesta
        if (updateObj.getId() == null || updateObj.getId().getMedicineId() == null || 
            updateObj.getId().getCategoryId() == null || updateObj.getId().getSubcategoryId() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Composite ID (medId, catId, subcatId) is required for update\"}");
             return;
        }
        
        MedicineCatSubcat mcs = mcsDAO.update(updateObj);
        if(mcs != null) {
            sendResponse(exchange, 200, objectMapper.writeValueAsString(mcs));
        } else {
             sendResponse(exchange, 400, "{\"error\": \"Failed to update association or association not found\"}");
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
