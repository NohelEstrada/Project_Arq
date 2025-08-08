package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.MedicineDAO;
import com.sources.app.entities.Medicine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manejador para búsquedas avanzadas de medicamentos por diferentes criterios.
 * Maneja el endpoint /api2/medicines/search
 */
public class SearchMedicineHandler implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(SearchMedicineHandler.class.getName());
    private final MedicineDAO medicineDAO;
    private final ObjectMapper objectMapper;

    public SearchMedicineHandler(MedicineDAO medicineDAO) {
        this.medicineDAO = medicineDAO;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configurar CORS para permitir solicitudes desde cualquier origen
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equals(exchange.getRequestMethod())) {
            handleSearch(exchange);
        } else {
            sendErrorResponse(exchange, 405, "Método no permitido. Use GET para buscar medicamentos.");
        }
    }

    private void handleSearch(HttpExchange exchange) throws IOException {
        try {
            // Analizar parámetros de consulta
            String query = exchange.getRequestURI().getQuery();
            LOGGER.info("Query recibida: " + query);
            Map<String, String> params = parseQueryParams(query);

            // Log de todos los parámetros para depuración
            for (Map.Entry<String, String> entry : params.entrySet()) {
                LOGGER.info("Parámetro: " + entry.getKey() + " = " + entry.getValue());
            }

            // Obtener todos los medicamentos para filtrar
            List<Medicine> allMedicines = medicineDAO.getAll();
            LOGGER.info("Total de medicamentos en base de datos: " + allMedicines.size());
            List<Medicine> results = new ArrayList<>();

            // Buscar por principio activo
            if (params.containsKey("activeMedicament")) {
                String activeMedicament = params.get("activeMedicament");
                LOGGER.info("Buscando medicamentos con principio activo: " + activeMedicament);
                
                // Imprimir los primeros medicamentos para depuración
                LOGGER.info("Ejemplos de principios activos en la base de datos:");
                for (int i = 0; i < Math.min(10, allMedicines.size()); i++) {
                    LOGGER.info("Medicamento " + i + ": " + allMedicines.get(i).getName() + 
                               ", Principio activo: " + allMedicines.get(i).getActiveMedicament());
                }
                
                // Filtrar medicamentos por principio activo (insensible a mayúsculas/minúsculas)
                for (Medicine medicine : allMedicines) {
                    if (medicine.getActiveMedicament() != null) {
                        // Búsqueda más flexible: primero intentamos coincidencia exacta
                        if (medicine.getActiveMedicament().equalsIgnoreCase(activeMedicament)) {
                            LOGGER.info("¡Coincidencia exacta encontrada! Medicamento: " + medicine.getName());
                            results.add(medicine);
                        } 
                        // Si no hay coincidencia exacta, buscamos coincidencia parcial
                        else if (medicine.getActiveMedicament().toLowerCase().contains(activeMedicament.toLowerCase())) {
                            LOGGER.info("Coincidencia parcial encontrada. Medicamento: " + medicine.getName() + 
                                       ", Principio activo: " + medicine.getActiveMedicament());
                            results.add(medicine);
                        }
                    }
                }
            } 
            // Buscar por nombre si no se especificó principio activo o como búsqueda secundaria
            else if (params.containsKey("name")) {
                String name = params.get("name");
                LOGGER.info("Buscando medicamentos con nombre: " + name);
                
                // Filtrar medicamentos por nombre (insensible a mayúsculas/minúsculas)
                for (Medicine medicine : allMedicines) {
                    if (medicine.getName() != null && 
                        medicine.getName().toLowerCase().contains(name.toLowerCase())) {
                        results.add(medicine);
                    }
                }
            } 
            // Si no hay parámetros de búsqueda, devolver todos
            else {
                LOGGER.info("No se especificó criterio de búsqueda, devolviendo todos los medicamentos");
                results = allMedicines;
            }

            LOGGER.info("Total de resultados encontrados: " + results.size());
            
            // Enviar resultados
            sendJsonResponse(exchange, 200, results);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al procesar la búsqueda: " + e.getMessage(), e);
            sendErrorResponse(exchange, 500, "Error interno al buscar medicamentos: " + e.getMessage());
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                String key = pair[0];
                String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                params.put(key, value);
            } else {
                params.put(pair[0], "");
            }
        }
        return params;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = objectMapper.writeValueAsBytes(data);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        String jsonError = String.format("{\"error\": \"%s\"}", message);
        byte[] responseBytes = jsonError.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
} 