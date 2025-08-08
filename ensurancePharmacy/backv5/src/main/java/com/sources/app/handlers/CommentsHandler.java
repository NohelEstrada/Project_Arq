package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.CommentsDAO;
import com.sources.app.entities.Comments;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Manejador HTTP para gestionar las operaciones CRUD de los Comentarios.
 * Responde a solicitudes en el endpoint "/api2/comments".
 * Soporta los métodos GET, POST, PUT y OPTIONS.
 * Los comentarios están asociados a un usuario, opcionalmente a un comentario previo y a un medicamento.
 */
public class CommentsHandler implements HttpHandler {
    private final CommentsDAO commentsDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/comments";

    /**
     * Constructor para CommentsHandler.
     *
     * @param commentsDAO El DAO para acceder a los datos de los comentarios.
     */
    public CommentsHandler(CommentsDAO commentsDAO) {
        this.commentsDAO = commentsDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de comentarios.
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
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Maneja las solicitudes POST para crear un nuevo comentario.
     * Espera un cuerpo de solicitud JSON con los datos del comentario 
     * (ID de usuario, ID de medicamento, texto y opcionalmente ID de comentario previo).
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Comments createComment = objectMapper.readValue(requestBody, Comments.class);
        
        // Validaciones básicas
        if (createComment.getUser() == null || createComment.getUser().getIdUser() == null) {
            sendResponse(exchange, 400, "{\"error\": \"User ID is required\"}");
            return;
        }
        if (createComment.getMedicine() == null || createComment.getMedicine().getIdMedicine() == null) {
            sendResponse(exchange, 400, "{\"error\": \"Medicine ID is required\"}");
            return;
        }
        if (createComment.getCommentText() == null || createComment.getCommentText().trim().isEmpty()) {
            sendResponse(exchange, 400, "{\"error\": \"Comment text is required\"}");
            return;
        }
        
        Comments comment = commentsDAO.create(
                createComment.getUser(),
                createComment.getPrevComment(), // Puede ser null
                createComment.getCommentText(),
                createComment.getMedicine()
        );
        if(comment != null){
            sendResponse(exchange, 201, objectMapper.writeValueAsString(comment));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create comment\"}");
        }
    }

    /**
     * Maneja las solicitudes GET para obtener comentarios.
     * Si se proporciona un parámetro de consulta 'id', devuelve el comentario específico.
     * De lo contrario, devuelve todos los comentarios.
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
     * Maneja la obtención de un comentario específico por su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta que contiene el ID (formato: id=commentId).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        try {
            Long id = Long.parseLong(query.substring(3));
            Comments comment = commentsDAO.getById(id);
            if(comment != null){
                sendResponse(exchange, 200, objectMapper.writeValueAsString(comment));
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID format\"}");
        }
    }

    /**
     * Maneja la obtención de todos los comentarios.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Comments> list = commentsDAO.getAll();
        sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
    }

    /**
     * Maneja las solicitudes PUT para actualizar un comentario existente.
     * Espera un cuerpo de solicitud JSON con los datos completos del comentario, incluyendo su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Comments updateComment = objectMapper.readValue(requestBody, Comments.class);
        
        // Validaciones básicas
        if (updateComment.getIdComments() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Comment ID is required for update\"}");
             return;
        }
         if (updateComment.getCommentText() == null || updateComment.getCommentText().trim().isEmpty()) {
            sendResponse(exchange, 400, "{\"error\": \"Comment text is required for update\"}");
            return;
        }
       // Aquí podrían ir más validaciones si se permiten cambiar el usuario, medicamento, etc.
        
        Comments comment = commentsDAO.update(updateComment);
        if(comment != null){
            sendResponse(exchange, 200, objectMapper.writeValueAsString(comment));
        } else {
             sendResponse(exchange, 400, "{\"error\": \"Failed to update comment or comment not found\"}");
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
