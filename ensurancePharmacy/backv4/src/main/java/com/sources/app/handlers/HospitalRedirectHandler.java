package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.util.HttpClientUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Manejador HTTP que actúa como un proxy o redirector para las solicitudes
 * destinadas a una API externa de hospital (simulada).
 * Reenvía las solicitudes recibidas en {@code /api/hospital-integration/*}
 * a la URL base {@code HOSPITAL_API_BASE_URL} manteniendo el método, subruta, query params y cuerpo.
 * 
 * <p>Este manejador es útil para desacoplar el frontend de la URL directa del servicio externo,
 * permitiendo centralizar la configuración de la API externa y potencialmente añadir lógica
 * de autenticación o transformación en el futuro.</p>
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/hospital-integration/*}</li>
 *   <li>{@code POST /api/hospital-integration/*}</li>
 *   <li>{@code PUT /api/hospital-integration/*}</li>
 *   <li>{@code DELETE /api/hospital-integration/*}</li>
 * </ul>
 */
public class HospitalRedirectHandler implements HttpHandler {
    /** Ruta base del endpoint que activa este manejador. */
    private static final String ENDPOINT = "/api/hospital-integration";
    /** URL base de la API externa del hospital a la que se redirigirán las solicitudes. */
    // TODO: Externalizar esta URL a un archivo de configuración
    private static final String HOSPITAL_API_BASE_URL = "http://localhost:8000/api"; 
    /** ObjectMapper para serializar respuestas de error en formato JSON. */
    private final ObjectMapper objectMapper;

     /**
     * Constructor del manejador de redirección de hospital.
     * Inicializa el ObjectMapper necesario para formatear respuestas de error.
     */
    public HospitalRedirectHandler() {
        this.objectMapper = new ObjectMapper(); // Usado para respuestas de error
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes.
     * Configura las cabeceras CORS, maneja solicitudes OPTIONS (preflight),
     * valida que la ruta de la solicitud comience con {@link #ENDPOINT},
     * construye la URL de destino completa (incluyendo subruta y query params)
     * para la API externa del hospital, y reenvía la solicitud original (método, cuerpo)
     * usando {@link HttpClientUtil}. Finalmente, envía la respuesta recibida del servicio externo
     * de vuelta al cliente original.
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida durante la comunicación o el manejo.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS Headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Handle preflight requests
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        // Valida que la solicitud comience con el endpoint base esperado
        if (!path.startsWith(ENDPOINT)) {
            sendErrorResponse(exchange, 404, "Endpoint no encontrado.");
            return;
        }

        try {
            // Extrae la subruta después del endpoint base
            String subPath = path.substring(ENDPOINT.length());
            
            // Asegura que la subruta comience con / si no está vacía
            if (!subPath.isEmpty() && !subPath.startsWith("/")) {
                subPath = "/" + subPath;
            } else if (subPath.isEmpty()) {
                subPath = "/"; // Si es solo /api/hospital-integration, apunta a la raíz del hospital API
            }
            
            String method = exchange.getRequestMethod().toUpperCase();
            String query = exchange.getRequestURI().getQuery();
            // Construye la URL completa para la API del hospital
            String targetUrl = HOSPITAL_API_BASE_URL + subPath;
            
            // Añade query string si existe
            if (query != null && !query.isEmpty()) {
                targetUrl += "?" + query;
            }
            
            System.out.println("Redirigiendo " + method + " " + path + (query != null ? "?"+query : "") + " a " + targetUrl);

            // Lee el cuerpo de la solicitud si es POST o PUT
            String requestBody = null;
            if ("POST".equals(method) || "PUT".equals(method)) {
                requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                 System.out.println("  Con cuerpo: " + requestBody.substring(0, Math.min(requestBody.length(), 100)) + "..."); // Loguea inicio del cuerpo
            }
            
            // Realiza la llamada a la API externa usando HttpClientUtil
            String responseBody;
            int responseCode = 200; // Asume éxito por defecto
            
            try {
                switch (method) {
                    case "GET":
                        responseBody = HttpClientUtil.get(targetUrl);
                        break;
                    case "POST":
                        responseBody = HttpClientUtil.post(targetUrl, requestBody);
                        // Podríamos adaptar responseCode basado en la respuesta real si HttpClientUtil la proveyera
                        responseCode = 201; // Asume 201 para POST exitoso
                        break;
                    case "PUT":
                        responseBody = HttpClientUtil.put(targetUrl, requestBody);
                        break;
                    case "DELETE":
                        responseBody = HttpClientUtil.delete(targetUrl);
                         responseCode = 204; // Típicamente 204 para DELETE exitoso sin cuerpo
                         if (responseBody == null || responseBody.isEmpty()) {
                             sendResponse(exchange, responseCode, null); // Enviar sin cuerpo para 204
                             return;
                         }
                        break;
                    default:
                         sendErrorResponse(exchange, 405, "Método HTTP no soportado para redirección.");
                        return;
                }
             } catch (IOException e) {
                 // Error específico de la llamada HTTP
                 System.err.println("Error al conectar con la API del hospital en " + targetUrl + ": " + e.getMessage());
                 sendErrorResponse(exchange, 502, "Error de comunicación con el servicio externo (hospital)." ); // 502 Bad Gateway
                 return;
             }
            
            // Enviar la respuesta obtenida del servicio externo al cliente original
            if (responseBody != null) {
                 System.out.println("  Respuesta recibida (" + responseCode + "): " + responseBody.substring(0, Math.min(responseBody.length(), 100)) + "...");
                sendResponse(exchange, responseCode, responseBody);
            } else {
                // Si HttpClientUtil devuelve null en caso de error no capturado antes
                System.err.println("Respuesta nula recibida de la API del hospital en " + targetUrl);
                sendErrorResponse(exchange, 500, "Respuesta inesperada del servicio externo (hospital).");
            }
        } catch (Exception e) {
             System.err.println("Error inesperado en HospitalRedirectHandler: " + e.getMessage());
            e.printStackTrace();
            // Evita enviar headers si ya se hizo (por ejemplo, en error de HttpClientUtil)
            if (exchange.getResponseCode() == -1) { 
                 sendErrorResponse(exchange, 500, "Error interno del servidor al procesar la redirección.");
            }
        }
    }

    /**
     * Envía una respuesta HTTP al cliente original.
     * Establece el código de estado, las cabeceras (Content-Type a application/json si hay cuerpo)
     * y escribe el cuerpo de la respuesta si se proporciona.
     * Maneja correctamente las respuestas sin cuerpo (e.g., 204 No Content).
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP para la respuesta (e.g., 200, 201, 204).
     * @param responseBody El cuerpo de la respuesta como String. Puede ser {@code null} o vacío para respuestas sin cuerpo.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        // Asume JSON si hay cuerpo, pero podría necesitar ser más flexible
        byte[] responseBytes = new byte[0];
        if (responseBody != null) {
             exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
             responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        }
        
        exchange.sendResponseHeaders(statusCode, responseBytes.length > 0 ? responseBytes.length : -1); // Usa -1 para respuestas sin cuerpo
        
        if (responseBytes.length > 0) {
             try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
    
    /**
     * Envía una respuesta de error JSON estandarizada al cliente.
     * Utiliza el ObjectMapper para crear un cuerpo JSON con los campos "success" (fijo a false)
     * y "message" (con el mensaje de error proporcionado).
     * Luego llama a {@link #sendResponse(HttpExchange, int, String)} para enviar la respuesta.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP de error (e.g., 404, 500, 502).
     * @param errorMessage El mensaje descriptivo del error.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
     private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        Map<String, Object> errorResponse = Map.of("success", false, "message", errorMessage);
        String jsonError = objectMapper.writeValueAsString(errorResponse);
        sendResponse(exchange, statusCode, jsonError);
    }
} 