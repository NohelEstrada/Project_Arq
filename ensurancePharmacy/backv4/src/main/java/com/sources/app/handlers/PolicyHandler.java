package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Policy;
import com.sources.app.dao.PolicyDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;


public class PolicyHandler implements HttpHandler {

    private final PolicyDAO policyDAO;
    private final ObjectMapper objectMapper;
    // Endpoint que maneja este handler
    private static final String ENDPOINT = "/api/policy";

    public PolicyHandler(PolicyDAO policyDAO) {
        this.policyDAO = policyDAO;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Configuración de CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Manejo de preflight
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Verificar que la ruta sea la correcta
        String path = exchange.getRequestURI().getPath();
        if (!path.equalsIgnoreCase(ENDPOINT)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        // Seleccionamos la operación en función del método HTTP
        switch (exchange.getRequestMethod().toUpperCase()) {
            case "GET":
                handleGet(exchange);
                break;
            case "POST":
                handlePost(exchange);
                break;
            case "PUT":
                handlePut(exchange);
                break;
            case "DELETE":
                handleDelete(exchange);
                break;
            default:
                exchange.sendResponseHeaders(405, -1);
        }
    }

    // GET: Si se recibe el parámetro id se retorna la Policy, de lo contrario se retornan todas
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.contains("id=")) {
            Map<String, String> params = parseQuery(query);
            try {
                Long id = Long.parseLong(params.get("id"));
                Policy policy = policyDAO.find(id);
                if (policy != null) {
                    String jsonResponse = objectMapper.writeValueAsString(policy);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, -1);
            }
        } else {
            List<Policy> policies = policyDAO.findAll();
            String jsonResponse = objectMapper.writeValueAsString(policies);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    // POST: Crea una nueva Policy. Se espera que el cuerpo de la petición contenga un JSON que se mapea a Policy.
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            // Opcional: imprimir requestBody para verificar que llega correctamente
            System.out.println("Request Body: " + requestBody);

            Policy policy = objectMapper.readValue(requestBody, Policy.class);

            Policy createdPolicy = policyDAO.create(
                    policy.getPercentage(),
                    policy.getCreationDate(),
                    policy.getExpDate(),
                    policy.getCost(),
                    policy.getEnabled()
            );

            if (createdPolicy != null) {
                String jsonResponse = objectMapper.writeValueAsString(createdPolicy);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(201, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } else {
                exchange.sendResponseHeaders(500, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Aquí se captura la excepción para evitar que se cierre abruptamente la conexión.
            exchange.sendResponseHeaders(500, -1);
        }
    }


    // PUT: Actualiza una Policy. Se espera que el cuerpo contenga el JSON de la Policy a actualizar.
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Policy policy = objectMapper.readValue(requestBody, Policy.class);

        Policy updatedPolicy = policyDAO.update(policy);
        if (updatedPolicy != null) {
            String jsonResponse = objectMapper.writeValueAsString(updatedPolicy);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        } else {
            exchange.sendResponseHeaders(500, -1);
        }
    }

    // DELETE: Elimina una Policy. Se espera que el parámetro "id" en la query defina la Policy a eliminar.
    private void handleDelete(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.contains("id=")) {
            Map<String, String> params = parseQuery(query);
            try {
                Long id = Long.parseLong(params.get("id"));
                boolean deleted = policyDAO.delete(id);
                if (deleted) {
                    exchange.sendResponseHeaders(200, -1);
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, -1);
            }
        } else {
            exchange.sendResponseHeaders(400, -1);
        }
    }

    // Método auxiliar para parsear parámetros de consulta
    private Map<String, String> parseQuery(String query) {
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(
                        kv -> kv[0],
                        kv -> kv.length > 1 ? kv[1] : ""
                ));
    }
}
