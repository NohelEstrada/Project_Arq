package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.ConfigurableAmount;
import com.sources.app.dao.ConfigurableAmountDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Manejador HTTP para gestionar un valor de monto configurable, específicamente el "monto de prescripción"
 * ({@code prescriptionAmount}) almacenado en la entidad {@link ConfigurableAmount}.
 * Esta entidad probablemente almacena una única fila con este valor configurable.
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/configurable-amount/current}: Obtiene el valor actual del monto configurable.
 *       Si no existe un registro en la base de datos, devuelve un valor por defecto ({@link #DEFAULT_AMOUNT}).</li>
 *   <li>{@code PUT /api/configurable-amount/update}: Actualiza (o crea si no existe) el valor del monto configurable.
 *       Espera un cuerpo JSON con el campo "prescriptionAmount" conteniendo el nuevo valor numérico.</li>
 * </ul>
 */
public class ConfigurableAmountHandler implements HttpHandler {

    /** DAO para acceder a los datos de la entidad ConfigurableAmount. */
    private final ConfigurableAmountDAO configDAO;
    /** ObjectMapper para la serialización/deserialización JSON. */
    private final ObjectMapper objectMapper;
    /** Ruta específica para obtener el monto configurable actual. */
    private static final String ENDPOINT_CURRENT = "/api/configurable-amount/current";
    /** Ruta específica para actualizar el monto configurable. */
    private static final String ENDPOINT_UPDATE = "/api/configurable-amount/update";
    /** Valor por defecto para el monto configurable si no se encuentra ninguno en la base de datos. */
    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("250.00");

    /**
     * Constructor del manejador de monto configurable.
     * Inicializa el DAO y el ObjectMapper.
     *
     * @param configDAO El DAO para interactuar con la tabla {@code ConfigurableAmount}.
     */
    public ConfigurableAmountHandler(ConfigurableAmountDAO configDAO) {
        this.configDAO = configDAO;
        this.objectMapper = new ObjectMapper();
        // No se necesita formato de fecha específico si la entidad no tiene fechas.
        // this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas a los endpoints de monto configurable.
     * Configura las cabeceras CORS, maneja solicitudes OPTIONS (preflight), y enruta las solicitudes GET y PUT
     * a los métodos {@link #handleGetCurrentConfig(HttpExchange)} y {@link #handleUpdateConfig(HttpExchange)} respectivamente,
     * basándose en la ruta de la solicitud.
     * Rechaza cualquier otra ruta o método no soportado.
     * Captura excepciones generales para devolver un error 500.
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida (generalmente manejado internamente).
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración de CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Respuesta a solicitudes OPTIONS (preflight de CORS)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            // Delega según la ruta y el método
            if (path.equals(ENDPOINT_CURRENT) && method.equalsIgnoreCase("GET")) {
                handleGetCurrentConfig(exchange);
            } else if (path.equals(ENDPOINT_UPDATE) && method.equalsIgnoreCase("PUT")) {
                handleUpdateConfig(exchange);
            } else {
                // Ruta no encontrada o método incorrecto para la ruta
                sendErrorResponse(exchange, 404, "Endpoint no encontrado o método no válido para la ruta.");
            }
        } catch (Exception e) {
            System.err.println("Error inesperado en ConfigurableAmountHandler: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor.");
        }
    }

    /**
     * Maneja las solicitudes GET a {@code /api/configurable-amount/current}.
     * Busca la configuración actual del monto usando {@link ConfigurableAmountDAO#findCurrentConfig()}.
     * Si no se encuentra ninguna configuración (la tabla está vacía o el método devuelve null),
     * crea un objeto {@link ConfigurableAmount} temporal con el valor {@link #DEFAULT_AMOUNT}.
     * Envía la configuración encontrada (o la por defecto) como respuesta JSON con estado 200.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al obtener datos o al enviar la respuesta.
     */
    private void handleGetCurrentConfig(HttpExchange exchange) throws IOException {
        ConfigurableAmount config = configDAO.findCurrentConfig();
        // Si no existe configuración, crea un objeto temporal con el valor por defecto
        if (config == null) {
            config = new ConfigurableAmount(); 
            config.setPrescriptionAmount(DEFAULT_AMOUNT);
            System.out.println("No se encontró configuración de monto, usando valor por defecto: " + DEFAULT_AMOUNT);
        }
        sendJsonResponse(exchange, 200, config);
    }

    /**
     * Maneja las solicitudes PUT a {@code /api/configurable-amount/update}.
     * Lee el cuerpo JSON de la solicitud, esperando encontrar un campo "prescriptionAmount".
     * Valida que el valor proporcionado sea un número válido y no negativo.
     * Busca la configuración actual en la base de datos:
     * - Si no existe, llama a {@link ConfigurableAmountDAO#create(BigDecimal)} para crear un nuevo registro con el monto proporcionado.
     * - Si existe, actualiza el campo {@code prescriptionAmount} del objeto existente y llama a {@link ConfigurableAmountDAO#update(ConfigurableAmount)}.
     * Responde con 200 (OK) y el objeto {@link ConfigurableAmount} guardado (creado o actualizado) si tiene éxito.
     * Responde con 400 si falta el campo "prescriptionAmount", si el valor es inválido (no numérico, negativo) o si el JSON es inválido.
     * Responde con 500 si ocurre un error interno durante la operación de guardado en la base de datos.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handleUpdateConfig(HttpExchange exchange) throws IOException {
        try {
            String requestBody = readRequestBody(exchange);
            // Parsea el cuerpo JSON a un mapa para extraer el monto
            @SuppressWarnings("unchecked") // Usar un DTO sería más seguro
            Map<String, Object> data = objectMapper.readValue(requestBody, Map.class);

            Object amountObject = data.get("prescriptionAmount");
            if (amountObject == null) {
                sendErrorResponse(exchange, 400, "Falta el campo 'prescriptionAmount' en el cuerpo de la solicitud.");
                return;
            }

            // Extrae y convierte el nuevo monto, validando el formato
            BigDecimal newAmount;
            try {
                newAmount = new BigDecimal(amountObject.toString());
                // Validar que no sea negativo
                if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
                     sendErrorResponse(exchange, 400, "El monto 'prescriptionAmount' no puede ser negativo.");
                     return;
                }
            } catch (NumberFormatException e) {
                 sendErrorResponse(exchange, 400, "Valor inválido para 'prescriptionAmount'. Debe ser un número.");
                 return;
            }

            ConfigurableAmount currentConfig = configDAO.findCurrentConfig();
            ConfigurableAmount savedConfig;
            if (currentConfig == null) {
                // Si no existe, crea una nueva configuración
                System.out.println("Creando nueva configuración de monto: " + newAmount);
                savedConfig = configDAO.create(newAmount);
            } else {
                // Si existe, actualiza el monto
                System.out.println("Actualizando configuración de monto de " + currentConfig.getPrescriptionAmount() + " a " + newAmount);
                currentConfig.setPrescriptionAmount(newAmount);
                savedConfig = configDAO.update(currentConfig);
            }

            if (savedConfig != null) {
                // Envía la configuración actualizada como respuesta
                sendJsonResponse(exchange, 200, savedConfig);
            } else {
                // Error al crear o actualizar
                 System.err.println("Error en DAO al guardar ConfigurableAmount.");
                sendErrorResponse(exchange, 500, "Error interno al guardar la configuración del monto.");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
             System.err.println("Error al parsear JSON en PUT ConfigurableAmount: " + e.getMessage());
             sendErrorResponse(exchange, 400, "Formato JSON inválido.");
        } catch (Exception e) { // Captura más genérica por si acaso
            System.err.println("Error inesperado en handleUpdateConfig: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor al actualizar la configuración.");
        }
    }

    /**
     * Lee el cuerpo completo de una solicitud HTTP y lo devuelve como una cadena UTF-8.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @return El cuerpo de la solicitud como String.
     * @throws IOException Si ocurre un error durante la lectura del InputStream.
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            return new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Envía una respuesta JSON al cliente.
     * Serializa el objeto de datos a JSON y lo escribe en el cuerpo de la respuesta con el código de estado adecuado.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP (e.g., 200).
     * @param data El objeto a serializar.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = objectMapper.writeValueAsString(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    /**
     * Envía una respuesta de error JSON estandarizada al cliente.
     * Crea un cuerpo JSON con claves "success" (fijo a false) y "message".
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP de error (e.g., 400, 404, 500).
     * @param errorMessage El mensaje descriptivo del error.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        Map<String, Object> errorResponse = Map.of("success", false, "message", errorMessage);
         try {
             if (!exchange.getResponseHeaders().containsKey("Content-Type")) { 
                 sendJsonResponse(exchange, statusCode, errorResponse);
             } else {
                 System.err.println("Intento de enviar respuesta de error cuando los headers ya fueron enviados. Status: " + statusCode + ", Msg: " + errorMessage);
             }
        } catch (IOException e) {
            System.err.println("Error crítico al enviar respuesta de error: " + e.getMessage());
            // No relanzar aquí para evitar bucles si la escritura original falló
        }
    }
}
