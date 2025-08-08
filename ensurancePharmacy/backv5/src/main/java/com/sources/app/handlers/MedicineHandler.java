package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Medicine;
import com.sources.app.dao.MedicineDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Manejador HTTP para gestionar las operaciones CRUD de los Medicamentos.
 * Responde a solicitudes en el endpoint "/api2/medicines".
 * Soporta los métodos GET, POST, PUT y OPTIONS.
 * Para GET y PUT, espera el ID del medicamento como parte de la ruta (e.g., /api2/medicines/{id}).
 */
public class MedicineHandler implements HttpHandler {
    private final MedicineDAO medicineDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/medicines";

    /**
     * Constructor para MedicineHandler.
     *
     * @param medicineDAO El DAO para acceder a los datos de los medicamentos.
     */
    public MedicineHandler(MedicineDAO medicineDAO) {
        this.medicineDAO = medicineDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de medicamentos.
     * Configura las cabeceras CORS y delega a los métodos de manejo apropiados
     * según el método HTTP (handleGet, handlePost, handlePut).
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
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        // Basic check if path starts with the endpoint
        if (!path.startsWith(ENDPOINT)) {
            exchange.sendResponseHeaders(404, -1); // Not Found
            return;
        }

        String method = exchange.getRequestMethod();
        try {
            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange, path);
            } else if ("POST".equalsIgnoreCase(method)) {
                // POST should be to the base endpoint
                if (path.equalsIgnoreCase(ENDPOINT)) {
                     handlePost(exchange);
                } else {
                    exchange.sendResponseHeaders(400, -1); // Bad Request for POST to /medicines/{id}
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                handlePut(exchange, path);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Maneja las solicitudes GET para obtener medicamentos.
     * Si la ruta es el endpoint base, devuelve todos los medicamentos.
     * Si la ruta incluye un ID (e.g., /api2/medicines/{id}), devuelve ese medicamento.
     *
     * @param exchange El objeto HttpExchange.
     * @param path La ruta de la solicitud.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equalsIgnoreCase(ENDPOINT)) {
            // Get all medicines
            List<Medicine> medicines = medicineDAO.getAll();
            sendResponse(exchange, 200, objectMapper.writeValueAsString(medicines));
        } else {
            // Get medicine by ID from path
            try {
                Long id = extractIdFromPath(path);
                if (id == null) {
                    exchange.sendResponseHeaders(400, -1); // Invalid path format
                    return;
                }
                Medicine medicine = medicineDAO.getById(id);
                if (medicine != null) {
                    sendResponse(exchange, 200, objectMapper.writeValueAsString(medicine));
                } else {
                    exchange.sendResponseHeaders(404, -1); // Not Found
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid ID format in path\"}");
            }
        }
    }

    /**
     * Maneja las solicitudes POST para crear un nuevo medicamento.
     * Espera un cuerpo de solicitud JSON con los datos del medicamento.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Medicine createMed = objectMapper.readValue(requestBody, Medicine.class);
        // Validations could be added here (e.g., required fields)
        Medicine medicine = medicineDAO.create(
                createMed.getName(),
                createMed.getActiveMedicament(),
                createMed.getDescription(),
                createMed.getImage(),
                createMed.getConcentration(),
                createMed.getPresentacion(),
                createMed.getStock(),
                createMed.getBrand(),
                createMed.getPrescription(),
                createMed.getPrice(),
                createMed.getSoldUnits()
        );
        if (medicine != null) {
            sendResponse(exchange, 201, objectMapper.writeValueAsString(medicine));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create medicine\"}");
        }
    }

    /**
     * Maneja las solicitudes PUT para actualizar un medicamento existente.
     * Espera el ID del medicamento en la ruta (e.g., /api2/medicines/{id})
     * y los datos a actualizar en el cuerpo JSON.
     *
     * @param exchange El objeto HttpExchange.
     * @param path La ruta de la solicitud.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        try {
            Long id = extractIdFromPath(path);
            if (id == null) {
                exchange.sendResponseHeaders(400, -1); // Invalid path format
                return;
            }
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Medicine updateMed = objectMapper.readValue(requestBody, Medicine.class);
            updateMed.setIdMedicine(id); // Set the ID from the path
            
            // Validations could be added here
            
            Medicine updatedMedicine = medicineDAO.update(updateMed);
            if (updatedMedicine != null) {
                sendResponse(exchange, 200, objectMapper.writeValueAsString(updatedMedicine));
            } else {
                // Could be 404 if ID not found, or 400 if update failed for other reasons
                sendResponse(exchange, 404, "{\"error\": \"Failed to update medicine or medicine not found\"}"); 
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID format in path\"}");
        }
    }

    /**
     * Extrae el ID numérico del final de una ruta como /api/endpoint/{id}.
     *
     * @param path La ruta de la solicitud.
     * @return El ID como Long, o null si la ruta no tiene el formato esperado o el ID no es numérico.
     */
    private Long extractIdFromPath(String path) {
        String[] parts = path.split("/");
        if (parts.length == 4) { // Expecting /api2/medicines/{id}
            try {
                return Long.parseLong(parts[3]);
            } catch (NumberFormatException e) {
                return null; // ID part is not a valid number
            }
        } 
        return null; // Path doesn't match expected format
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
