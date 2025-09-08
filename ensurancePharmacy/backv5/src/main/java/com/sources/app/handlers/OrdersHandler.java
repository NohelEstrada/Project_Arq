package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.OrdersDAO;
import com.sources.app.entities.Orders;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.sources.app.entities.User; // Import para validación
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manejador HTTP para gestionar las operaciones CRUD de los Pedidos (Orders).
 * Responde a solicitudes en el endpoint "/api2/orders".
 * Soporta los métodos GET, POST, PUT y OPTIONS.
 */
public class OrdersHandler implements HttpHandler {
    private final OrdersDAO ordersDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/orders";
    private static final Logger LOGGER = Logger.getLogger(OrdersHandler.class.getName());

    /**
     * Constructor para OrdersHandler.
     *
     * @param ordersDAO El DAO para acceder a los datos de los pedidos.
     */
    public OrdersHandler(OrdersDAO ordersDAO) {
        this.ordersDAO = ordersDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de pedidos.
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
            LOGGER.log(Level.SEVERE, "Error processing OrdersHandler request", e);
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Maneja las solicitudes POST para crear un nuevo pedido.
     * Espera un cuerpo JSON con el estado del pedido y el ID del usuario asociado.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Orders createOrder = objectMapper.readValue(requestBody, Orders.class);
        
        // Validar datos requeridos
        User user = createOrder.getUser();
        if (user == null || user.getIdUser() == null) {
            sendResponse(exchange, 400, "{\"error\": \"User ID is required\"}");
            return;
        }
        if (createOrder.getStatus() == null || createOrder.getStatus().trim().isEmpty()) {
            // Asignar un estado por defecto si no se provee? O requerirlo?
            // Por ahora, lo requerimos:
            sendResponse(exchange, 400, "{\"error\": \"Order status is required\"}");
            return;
        }
        
        // Llamar al DAO con status y userId
        Orders order = ordersDAO.create(createOrder.getStatus(), user.getIdUser()); 
        if(order != null) {
            sendResponse(exchange, 201, objectMapper.writeValueAsString(order));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create order\"}");
        }
    }

    /**
     * Maneja las solicitudes GET para obtener pedidos.
     * Si se proporciona un parámetro de consulta 'id', devuelve el pedido específico.
     * De lo contrario, devuelve todos los pedidos.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
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
     * Maneja la obtención de un pedido específico por su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta que contiene el ID (formato: id=orderId).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        try {
            Long id = Long.parseLong(query.substring(3));
            Orders order = ordersDAO.getById(id);
            if(order != null) {
                sendResponse(exchange, 200, objectMapper.writeValueAsString(order));
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID format\"}");
        }
    }

    /**
     * Maneja la obtención de todos los pedidos.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Orders> list = ordersDAO.getAll();
        sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
    }

    /**
     * Maneja las solicitudes PUT para actualizar un pedido existente.
     * Espera un cuerpo JSON con los datos completos del pedido, incluyendo su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Orders updateOrder = objectMapper.readValue(requestBody, Orders.class);
        
        if (updateOrder.getIdOrder() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Order ID is required for update\"}");
             return;
        }
        // Podrían ir más validaciones (e.g., status no vacío)
        
        Orders order = ordersDAO.update(updateOrder);
        if(order != null){
            sendResponse(exchange, 200, objectMapper.writeValueAsString(order));
        } else {
             sendResponse(exchange, 400, "{\"error\": \"Failed to update order or order not found\"}");
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
