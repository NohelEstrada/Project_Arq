package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.BillMedicineDAO;
import com.sources.app.entities.BillMedicine;
import com.sources.app.entities.Bill;
import com.sources.app.entities.Medicine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.sources.app.entities.BillMedicineId;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manejador HTTP para gestionar las operaciones CRUD de la relación entre facturas y medicamentos (BillMedicine).
 * Responde a solicitudes en el endpoint "/api2/bill_medicines".
 * Soporta los métodos GET, POST, PUT y OPTIONS.
 * Utiliza una clave compuesta (BillMedicineId) para identificar las entidades.
 */
public class BillMedicineHandler implements HttpHandler {
    private final BillMedicineDAO billMedicineDAO;
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = Logger.getLogger(BillMedicineHandler.class.getName());

    /**
     * Constructor para BillMedicineHandler.
     *
     * @param billMedicineDAO El DAO para acceder a los datos de la relación factura-medicamento.
     */
    public BillMedicineHandler(BillMedicineDAO billMedicineDAO) {
        this.billMedicineDAO = billMedicineDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de la relación factura-medicamento.
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
            LOGGER.log(Level.SEVERE, "Unhandled error in BillMedicineHandler", e);
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Maneja las solicitudes POST para crear una nueva asociación factura-medicamento.
     * Espera un cuerpo de solicitud JSON con los datos (incluyendo IDs de factura y medicamento).
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        BillMedicine createBM = objectMapper.readValue(requestBody, BillMedicine.class);
        Bill bill = createBM.getBill(); // Asegurar que el JSON incluye al menos bill.id
        Medicine medicine = createBM.getMedicine(); // Asegurar que el JSON incluye al menos medicine.id
        
        // Validar IDs
        if (bill == null || bill.getIdBill() == null || medicine == null || medicine.getIdMedicine() == null) {
            sendResponse(exchange, 400, "{\"error\": \"Bill ID and Medicine ID are required\"}");
            return;
        }
        
        BillMedicine bm = billMedicineDAO.create(
                bill,
                medicine,
                createBM.getQuantity(),
                createBM.getCost(),
                createBM.getCopay(),
                createBM.getTotal()
        );
        if(bm != null){
            sendResponse(exchange, 201, objectMapper.writeValueAsString(bm));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create bill-medicine association\"}");
        }
    }

    /**
     * Maneja las solicitudes GET para obtener asociaciones factura-medicamento.
     * Si se proporciona un parámetro de consulta 'id' en formato 'billId,medicineId',
     * devuelve la asociación específica. De lo contrario, devuelve todas.
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
     * Maneja la obtención de una asociación factura-medicamento específica por su ID compuesto.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta que contiene el ID compuesto (formato: id=billId,medicineId).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        String[] parts = query.substring(3).split(",");
        if(parts.length == 2){
            try {
                Long billId = Long.parseLong(parts[0]);
                Long medicineId = Long.parseLong(parts[1]);
                BillMedicineId id = new BillMedicineId(billId, medicineId);
                BillMedicine bm = billMedicineDAO.getById(id);
                if(bm != null){
                    sendResponse(exchange, 200, objectMapper.writeValueAsString(bm));
                } else {
                    exchange.sendResponseHeaders(404, -1); // Not Found
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid composite ID format (expected: billId,medicineId)\"}");
            }
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID parameter format (expected: id=billId,medicineId)\"}");
        }
    }

    /**
     * Maneja la obtención de todas las asociaciones factura-medicamento.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<BillMedicine> list = billMedicineDAO.getAll();
        sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
    }

    /**
     * Maneja las solicitudes PUT para actualizar una asociación factura-medicamento existente.
     * Espera un cuerpo de solicitud JSON con los datos completos, incluyendo la clave compuesta.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        BillMedicine updateBM = objectMapper.readValue(requestBody, BillMedicine.class);
        
        // Validar clave compuesta
        if (updateBM.getId() == null || updateBM.getId().getBillId() == null || updateBM.getId().getMedicineId() == null) {
            sendResponse(exchange, 400, "{\"error\": \"Composite ID (billId, medicineId) is required for update\"}");
            return;
        }
        
        BillMedicine bm = billMedicineDAO.update(updateBM);
        if(bm != null){
            sendResponse(exchange, 200, objectMapper.writeValueAsString(bm));
        } else {
            // Podría ser 404 si el ID no existe, o 400 si la actualización falló
            sendResponse(exchange, 400, "{\"error\": \"Failed to update bill-medicine association or association not found\"}");
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
