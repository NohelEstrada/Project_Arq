package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.HospitalDAO;
import com.sources.app.entities.Hospital;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Manejador que actúa como proxy entre el frontend y los servicios del hospital.
 * Utiliza el puerto configurado para cada hospital para realizar las solicitudes.
 */
public class HospitalServiceProxyHandler implements HttpHandler {

    private final HospitalDAO hospitalDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT_PREFIX = "/api/hospital-proxy";

    /**
     * Constructor para el proxy de servicios del hospital.
     *
     * @param hospitalDAO DAO para acceder a los datos de los hospitales
     */
    public HospitalServiceProxyHandler(HospitalDAO hospitalDAO) {
        this.hospitalDAO = hospitalDAO;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Hospital-Port");

        // Preflight (solicitud OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            // Verificar si la URL comienza con el endpoint esperado
            String path = exchange.getRequestURI().getPath();
            if (!path.startsWith(ENDPOINT_PREFIX)) {
                sendErrorResponse(exchange, 404, "Endpoint no encontrado");
                return;
            }

            // Obtener el ID del hospital y el puerto de los headers o parámetros
            Long hospitalId = getHospitalIdFromPath(path);
            String hospitalPort = exchange.getRequestHeaders().getFirst("X-Hospital-Port");

            if (hospitalId == null) {
                sendErrorResponse(exchange, 400, "ID de hospital no especificado");
                return;
            }

            // Obtener el hospital de la base de datos
            Hospital hospital = hospitalDAO.findById(hospitalId);
            if (hospital == null) {
                sendErrorResponse(exchange, 404, "Hospital no encontrado");
                return;
            }

            // Usar el puerto proporcionado en el header o el puerto del hospital o el valor por defecto
            String port = hospitalPort != null && !hospitalPort.trim().isEmpty() ? 
                         hospitalPort : 
                         (hospital.getPort() != null && !hospital.getPort().trim().isEmpty() ? 
                         hospital.getPort() : "8000");

            // Construir la URL del servicio del hospital
            String targetPath = path.substring(ENDPOINT_PREFIX.length());
            String targetUrl = "http://localhost:" + port + targetPath;

            // Reenviar la solicitud al servicio del hospital
            forwardRequest(exchange, targetUrl);

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Reenvía la solicitud HTTP al servicio de destino.
     *
     * @param exchange Intercambio HTTP original
     * @param targetUrl URL del servicio de destino
     * @throws IOException Si ocurre un error al reenviar la solicitud
     */
    private void forwardRequest(HttpExchange exchange, String targetUrl) throws IOException {
        HttpURLConnection connection = null;
        try {
            // Crear la conexión
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(exchange.getRequestMethod());

            // Copiar headers, excepto los relacionados con la conexión
            for (Map.Entry<String, List<String>> header : exchange.getRequestHeaders().entrySet()) {
                String key = header.getKey();
                if (!key.equalsIgnoreCase("Host") && !key.equalsIgnoreCase("Connection")) {
                    for (String value : header.getValue()) {
                        connection.addRequestProperty(key, value);
                    }
                }
            }

            // Configurar para enviar datos si es necesario
            boolean hasOutput = "POST".equals(exchange.getRequestMethod()) || 
                              "PUT".equals(exchange.getRequestMethod());
            if (hasOutput) {
                connection.setDoOutput(true);
                try (InputStream is = exchange.getRequestBody();
                     OutputStream os = connection.getOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                }
            }

            // Obtener la respuesta
            int responseCode = connection.getResponseCode();
            
            // Copiar headers de respuesta
            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null) {
                    for (String value : header.getValue()) {
                        exchange.getResponseHeaders().add(header.getKey(), value);
                    }
                }
            }

            // Asegurar que el Content-Type está establecido
            if (!exchange.getResponseHeaders().containsKey("Content-Type")) {
                exchange.getResponseHeaders().add("Content-Type", "application/json");
            }

            // Leer y enviar el cuerpo de la respuesta
            try (InputStream is = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
                if (is != null) {
                    byte[] response = is.readAllBytes();
                    exchange.sendResponseHeaders(responseCode, response.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response);
                    }
                } else {
                    exchange.sendResponseHeaders(responseCode, -1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 502, "Error al comunicarse con el servicio del hospital: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Extrae el ID del hospital de la ruta de la solicitud.
     *
     * @param path Ruta de la solicitud
     * @return ID del hospital o null si no se puede extraer
     */
    private Long getHospitalIdFromPath(String path) {
        try {
            // Formato esperado: /api/hospital-proxy/{hospitalId}/...
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                return Long.parseLong(parts[3]);
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Envía una respuesta de error en formato JSON.
     *
     * @param exchange Intercambio HTTP
     * @param statusCode Código de estado HTTP
     * @param message Mensaje de error
     * @throws IOException Si ocurre un error al enviar la respuesta
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorResponse = Map.of("success", false, "message", message);
        String response = objectMapper.writeValueAsString(errorResponse);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
} 