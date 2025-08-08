package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.OrderMedicineDAO;
import com.sources.app.entities.OrderMedicine;
import com.sources.app.entities.OrderMedicineId;
import com.sources.app.entities.Orders;
import com.sources.app.entities.Medicine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Manejador HTTP para gestionar las operaciones CRUD de la relación entre Pedidos y Medicamentos (OrderMedicine).
 * Responde a solicitudes en el endpoint "/api2/order_medicines".
 * Soporta los métodos GET, POST, PUT, DELETE y OPTIONS.
 * Utiliza una clave compuesta (OrderMedicineId) para identificar las entidades.
 */
public class OrderMedicineHandler implements HttpHandler {
    private final OrderMedicineDAO orderMedicineDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/order_medicines";

    /**
     * Constructor para OrderMedicineHandler.
     *
     * @param orderMedicineDAO El DAO para acceder a los datos de la relación pedido-medicamento.
     */
    public OrderMedicineHandler(OrderMedicineDAO orderMedicineDAO) {
        this.orderMedicineDAO = orderMedicineDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de la relación pedido-medicamento.
     * Configura las cabeceras CORS y delega a los métodos de manejo apropiados
     * según el método HTTP (handlePost, handleGet, handlePut, handleDelete).
     *
     * @param exchange El objeto HttpExchange que representa la solicitud y respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
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
            } else if("DELETE".equalsIgnoreCase(method)) {
                handleDelete(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Maneja las solicitudes POST para crear una nueva asociación pedido-medicamento.
     * Espera un cuerpo JSON con los IDs de Orders y Medicine, quantity, cost, total.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        OrderMedicine createOM = objectMapper.readValue(requestBody, OrderMedicine.class);
        
        // Validar IDs y datos requeridos
        Orders order = createOM.getOrders();
        Medicine medicine = createOM.getMedicine();
        if (order == null || order.getIdOrder() == null || medicine == null || medicine.getIdMedicine() == null) {
            sendResponse(exchange, 400, "{\"error\": \"Order ID and Medicine ID are required\"}");
            return;
        }
        // Podrían añadirse más validaciones (quantity > 0, etc.)
        
        OrderMedicine om = orderMedicineDAO.create(
                order,
                medicine,
                createOM.getQuantity(),
                createOM.getCost(),
                createOM.getTotal()
        );
        if(om != null){
            sendResponse(exchange, 201, objectMapper.writeValueAsString(om));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create order-medicine association\"}");
        }
    }

    /**
     * Maneja las solicitudes GET para obtener asociaciones pedido-medicamento.
     * Si se proporciona 'id=orderId,medicineId', devuelve la específica.
     * De lo contrario, devuelve todas.
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
     * Maneja la obtención de una asociación específica por su ID compuesto.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta con el ID (formato: id=orderId,medicineId).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        String[] parts = query.substring(3).split(",");
        if(parts.length == 2){
            try {
                Long orderId = Long.parseLong(parts[0]);
                Long medicineId = Long.parseLong(parts[1]);
                OrderMedicineId id = new OrderMedicineId(orderId, medicineId);
                OrderMedicine om = orderMedicineDAO.getById(id);
                if(om != null){
                    sendResponse(exchange, 200, objectMapper.writeValueAsString(om));
                } else {
                    exchange.sendResponseHeaders(404, -1); // Not Found
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid composite ID format (expected: orderId,medicineId)\"}");
            }
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID parameter format (expected: id=orderId,medicineId)\"}");
        }
    }

    /**
     * Maneja la obtención de todas las asociaciones pedido-medicamento.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<OrderMedicine> list = orderMedicineDAO.getAll();
        sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
    }

    /**
     * Maneja las solicitudes PUT para actualizar una asociación existente.
     * Espera un cuerpo JSON con los datos completos, incluyendo la clave compuesta.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        OrderMedicine updateOM = objectMapper.readValue(requestBody, OrderMedicine.class);
        
        // Validar clave compuesta
        if (updateOM.getId() == null || updateOM.getId().getOrderId() == null || updateOM.getId().getMedicineId() == null) {
            sendResponse(exchange, 400, "{\"error\": \"Composite ID (orderId, medicineId) is required for update\"}");
            return;
        }
        // Podrían añadirse más validaciones
        
        OrderMedicine om = orderMedicineDAO.update(updateOM);
        if(om != null){
            sendResponse(exchange, 200, objectMapper.writeValueAsString(om));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to update order-medicine association or association not found\"}");
        }
    }
    
     /**
     * Maneja las solicitudes DELETE para eliminar una asociación pedido-medicamento.
     * Espera el ID compuesto en la query string (formato: id=orderId,medicineId).
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleDelete(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if(query != null && query.startsWith("id=")) {
            String[] parts = query.substring(3).split(",");
            if(parts.length == 2) {
                 try {
                    Long orderId = Long.parseLong(parts[0]);
                    Long medicineId = Long.parseLong(parts[1]);
                    OrderMedicineId id = new OrderMedicineId(orderId, medicineId);
                    
                    // Asumiendo que el DAO tiene un método deleteById que retorna boolean
                    boolean deleted = orderMedicineDAO.deleteById(id); 
                    if(deleted) {
                        exchange.sendResponseHeaders(204, -1); // No Content
                    } else {
                        exchange.sendResponseHeaders(404, -1); // Not Found
                    }
                 } catch (NumberFormatException e) {
                     sendResponse(exchange, 400, "{\"error\": \"Invalid composite ID format (expected: orderId,medicineId)\"}");
                 }
            } else {
                sendResponse(exchange, 400, "{\"error\": \"Invalid ID parameter format (expected: id=orderId,medicineId)\"}");
            }
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Composite ID (id=orderId,medicineId) is required for delete\"}");
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
