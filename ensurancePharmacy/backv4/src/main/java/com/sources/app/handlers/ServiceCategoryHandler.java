package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.ServiceCategoryDAO;
import com.sources.app.entities.ServiceCategory;
import com.sources.app.entities.Service;
import com.sources.app.entities.Category;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceCategoryHandler implements HttpHandler {

    private final ServiceCategoryDAO serviceCategoryDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api/servicecategory";

    public ServiceCategoryHandler(ServiceCategoryDAO serviceCategoryDAO) {
        this.serviceCategoryDAO = serviceCategoryDAO;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Manejo de preflight
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Verificar endpoint
        String path = exchange.getRequestURI().getPath();
        if (!path.equalsIgnoreCase(ENDPOINT)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        // Enrutamiento según método HTTP
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
            default:
                exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        // Para obtener un registro único se requieren ambos parámetros: idService e idCategory
        if (query != null && query.contains("idService=") && query.contains("idCategory=")) {
            Map<String, String> params = parseQuery(query);
            try {
                Long idService = Long.parseLong(params.get("idService"));
                Long idCategory = Long.parseLong(params.get("idCategory"));
                ServiceCategory sc = serviceCategoryDAO.findById(idService, idCategory);
                if (sc != null) {
                    String jsonResponse = objectMapper.writeValueAsString(sc);
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
            // Si no se especifican ambos IDs, devolvemos todos los registros
            List<ServiceCategory> list = serviceCategoryDAO.findAll();
            String jsonResponse = objectMapper.writeValueAsString(list);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            ServiceCategory sc = objectMapper.readValue(requestBody, ServiceCategory.class);

            // Se asume que el JSON incluye objetos anidados "service" y "category" o, al menos, sus IDs
            Service service = sc.getService();
            Category category = sc.getCategory();
            if (service == null || category == null ||
                    service.getIdService() == null || category.getIdCategory() == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            ServiceCategory created = serviceCategoryDAO.create(
                    service.getIdService(),
                    category.getIdCategory()
            );

            if (created != null) {
                String jsonResponse = objectMapper.writeValueAsString(created);
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
            exchange.sendResponseHeaders(500, -1);
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        // El PUT se usaría para actualizar la relación. En una tabla de relación pura
        // (con clave compuesta y sin atributos extra), no suele ser común actualizar,
        // pero se incluye por consistencia.
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        ServiceCategory sc = objectMapper.readValue(requestBody, ServiceCategory.class);
        ServiceCategory updated = serviceCategoryDAO.update(sc);
        if (updated != null) {
            String jsonResponse = objectMapper.writeValueAsString(updated);
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

    private Map<String, String> parseQuery(String query) {
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(
                        kv -> kv[0],
                        kv -> kv.length > 1 ? kv[1] : ""
                ));
    }
}
