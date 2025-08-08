package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.BillDAO;
import com.sources.app.entities.Bill;
import com.sources.app.entities.Prescription;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Manejador HTTP para gestionar las operaciones CRUD de las facturas (Bills).
 * Responde a solicitudes en el endpoint "/api2/bills".
 * Soporta los métodos GET, POST, PUT y OPTIONS.
 */
public class BillHandler implements HttpHandler {
    private final BillDAO billDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/bills";

    /**
     * Constructor para BillHandler.
     *
     * @param billDAO El DAO para acceder a los datos de las facturas.
     */
    public BillHandler(BillDAO billDAO) {
        this.billDAO = billDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de facturas.
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
     * Maneja las solicitudes POST para crear una nueva factura.
     * Espera un cuerpo de solicitud JSON con los datos de la factura (incluyendo el ID de la prescripción asociada).
     * 
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Bill createBill = objectMapper.readValue(requestBody, Bill.class);
        Prescription prescription = createBill.getPrescription(); // Asegurarse que el JSON incluye al menos prescription.id
        
        // Validar que la prescripción no sea nula y tenga ID
        if (prescription == null || prescription.getIdPrescription() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Prescription ID is required\"}");
             return;
        }
        
        Bill bill = billDAO.create(
                prescription,
                createBill.getTaxes(),
                createBill.getSubtotal(),
                createBill.getCopay(),
                createBill.getTotal()
        );
        if (bill != null) {
            sendResponse(exchange, 201, objectMapper.writeValueAsString(bill));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create bill\"}");
        }
    }

    /**
     * Maneja las solicitudes GET para obtener facturas.
     * Si se proporciona un parámetro de consulta 'id', devuelve la factura específica.
     * De lo contrario, devuelve todas las facturas.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.startsWith("id=")) {
            try {
                Long id = Long.parseLong(query.substring(3));
                Bill bill = billDAO.getById(id);
                if (bill != null) {
                    sendResponse(exchange, 200, objectMapper.writeValueAsString(bill));
                } else {
                    exchange.sendResponseHeaders(404, -1); // Not Found
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid ID format\"}");
            }
        } else {
            List<Bill> bills = billDAO.getAll();
            sendResponse(exchange, 200, objectMapper.writeValueAsString(bills));
        }
    }

    /**
     * Maneja las solicitudes PUT para actualizar una factura existente.
     * Espera un cuerpo de solicitud JSON con los datos completos de la factura, incluyendo su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Bill updateBill = objectMapper.readValue(requestBody, Bill.class);
         // Validar que el ID no sea nulo
        if (updateBill.getIdBill() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Bill ID is required for update\"}");
             return;
        }
        Bill bill = billDAO.update(updateBill);
        if (bill != null) {
            sendResponse(exchange, 200, objectMapper.writeValueAsString(bill));
        } else {
             // Podría ser 404 si el ID no existe, o 400 si la actualización falló por otra razón
            sendResponse(exchange, 400, "{\"error\": \"Failed to update bill or bill not found\"}");
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
