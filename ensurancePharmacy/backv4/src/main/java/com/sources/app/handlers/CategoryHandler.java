package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Category;
import com.sources.app.dao.CategoryDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador HTTP para las operaciones CRUD (Crear, Leer, Actualizar)
 * sobre la entidad {@link Category}, que representa categorías de servicios o productos.
 * Gestiona las solicitudes para el endpoint "/api/category".
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/category}: Obtiene la lista de todas las categorías.</li>
 *   <li>{@code GET /api/category?id={id}}: Obtiene una categoría específica por su ID.</li>
 *   <li>{@code POST /api/category}: Crea una nueva categoría.</li>
 *   <li>{@code PUT /api/category}: Actualiza una categoría existente (requiere 'idCategory' en el cuerpo).</li>
 *   <li>(DELETE no está implementado en este manejador).</li>
 * </ul>
 */
public class CategoryHandler implements HttpHandler {

    /** DAO para acceder a los datos de la entidad Category. */
    private final CategoryDAO categoryDAO;
    /** ObjectMapper para la serialización/deserialización JSON. */
    private final ObjectMapper objectMapper;
    /** Ruta base para las solicitudes gestionadas por este manejador. */
    private static final String ENDPOINT = "/api/category";

    /**
     * Constructor del manejador de categorías.
     * Inicializa el DAO de categorías y el ObjectMapper.
     *
     * @param categoryDAO El DAO para interactuar con la tabla de categorías.
     */
    public CategoryHandler(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
        this.objectMapper = new ObjectMapper();
        // Podría definirse un formato de fecha si Category tuviera campos de fecha
        // this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd")); 
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas al endpoint de categorías.
     * Configura las cabeceras CORS, maneja solicitudes OPTIONS (preflight),
     * valida que la ruta coincida con {@link #ENDPOINT}, y enruta las solicitudes
     * GET, POST y PUT a sus respectivos métodos de manejo. Cualquier otro método resulta
     * en un error 405 (Method Not Allowed). Captura excepciones generales para devolver un error 500.
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida (generalmente manejado internamente).
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración de CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Respuesta a solicitudes OPTIONS (preflight de CORS)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        // Verifica si la ruta coincide con el endpoint esperado
        if (!path.equalsIgnoreCase(ENDPOINT)) {
            sendErrorResponse(exchange, 404, "Endpoint no encontrado.");
            return;
        }

        // Delega según el método HTTP con manejo de errores general
        try {
            switch (exchange.getRequestMethod().toUpperCase()){
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
                    sendErrorResponse(exchange, 405, "Método no permitido.");
            }
        } catch (Exception e) {
             System.err.println("Error inesperado en CategoryHandler: " + e.getMessage());
             e.printStackTrace();
             sendErrorResponse(exchange, 500, "Error interno del servidor.");
        }
    }

    /**
     * Maneja las solicitudes GET a {@code /api/category}.
     * Si se proporciona un parámetro de consulta 'id', intenta obtener la categoría específica por ese ID.
     * Si no se proporciona 'id' o el parámetro es inválido, obtiene y devuelve la lista de todas las categorías.
     * Responde con 404 si la categoría solicitada por ID no se encuentra.
     * Responde con 400 si el parámetro 'id' tiene un formato inválido.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        // Verifica si se busca por ID
        if(query != null && query.contains("id=")){
            Map<String,String> params = parseQuery(query);
            try{
                Long id = Long.parseLong(params.get("id"));
                Category category = categoryDAO.findById(id);
                if(category != null){
                    // Categoría encontrada, enviar respuesta JSON
                    sendJsonResponse(exchange, 200, category);
                } else {
                    // Categoría no encontrada
                    sendErrorResponse(exchange, 404, "Categoría no encontrada con ID: " + id);
                }
            } catch(NumberFormatException e){
                // ID inválido
                sendErrorResponse(exchange, 400, "ID de categoría inválido.");
            }
        } else {
            // Obtener todas las categorías
            List<Category> list = categoryDAO.findAll();
            sendJsonResponse(exchange, 200, list);
        }
    }

    /**
     * Maneja las solicitudes POST a {@code /api/category}.
     * Crea una nueva categoría basada en el cuerpo JSON de la solicitud.
     * El cuerpo JSON debe contener al menos 'name'. El campo 'enabled' es opcional
     * (se utilizará el valor proporcionado o el valor por defecto definido en la entidad/DAO).
     * Responde con 201 (Created) y el objeto de la categoría creada si tiene éxito.
     * Responde con 400 si falta el campo 'name' o si el JSON es inválido.
     * Responde con 500 si ocurre un error al interactuar con la base de datos.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            // Lee y parsea el cuerpo de la solicitud
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Category category = objectMapper.readValue(requestBody, Category.class);
            
            // Validación: El nombre es requerido
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                sendErrorResponse(exchange, 400, "El nombre de la categoría es requerido.");
                return;
            }

            // Crea la categoría en la base de datos
            Category created = categoryDAO.create(
                    category.getName(),
                    category.getEnabled() // Usar el valor proporcionado o el default del DAO/Entity
            );
            if(created != null){
                // Categoría creada exitosamente, enviar respuesta JSON
                sendJsonResponse(exchange, 201, created);
            } else {
                // Error al crear la categoría
                System.err.println("Error en DAO al crear categoría.");
                sendErrorResponse(exchange, 500, "Error interno al crear la categoría.");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("Error al parsear JSON en POST Category: " + e.getMessage());
            sendErrorResponse(exchange, 400, "Formato JSON inválido.");
        } catch(Exception e){
            System.err.println("Error inesperado en handlePost Category: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor al procesar la solicitud.");
        }
    }

    /**
     * Maneja las solicitudes PUT a {@code /api/category}.
     * Actualiza una categoría existente basada en el cuerpo JSON de la solicitud.
     * El cuerpo JSON *debe* contener 'idCategory' para identificar la categoría a actualizar.
     * Otros campos presentes en el JSON (name, enabled) se usarán para la actualización.
     * Se valida que el campo 'name', si se actualiza, no quede vacío.
     * Responde con 200 (OK) y el objeto de la categoría actualizada si tiene éxito.
     * Responde con 400 si falta 'idCategory', si el nombre proporcionado es vacío o si el JSON es inválido.
     * Responde con 404 si la 'idCategory' proporcionada no corresponde a una categoría existente o si la actualización falla en el DAO por otra razón.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        try {
            // Lee y parsea el cuerpo de la solicitud
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Category category = objectMapper.readValue(requestBody, Category.class);

             // Validar que el ID esté presente para la actualización
            if (category.getIdCategory() == null) {
                 sendErrorResponse(exchange, 400, "Falta el idCategory para actualizar.");
                 return;
            }
             // Validar que el nombre no sea vacío si se proporciona
            if (category.getName() != null && category.getName().trim().isEmpty()) {
                 sendErrorResponse(exchange, 400, "El nombre de la categoría no puede estar vacío.");
                 return;
            }

            // Actualiza la categoría en la base de datos
            Category updated = categoryDAO.update(category);
            if(updated != null){
                // Categoría actualizada exitosamente, enviar respuesta JSON
                sendJsonResponse(exchange, 200, updated);
            } else {
                // Error al actualizar (posiblemente no encontrada o error interno)
                System.err.println("Error en DAO al actualizar categoría o no encontrada (ID: " + category.getIdCategory() + ").");
                sendErrorResponse(exchange, 404, "Categoría no encontrada o error al actualizar.");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("Error al parsear JSON en PUT Category: " + e.getMessage());
            sendErrorResponse(exchange, 400, "Formato JSON inválido.");
        } catch (Exception e) {
            System.err.println("Error inesperado en handlePut Category: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor al procesar la actualización.");
        }
    }
    
     /**
     * Envía una respuesta JSON al cliente.
     * Serializa el objeto de datos proporcionado a JSON y lo escribe en el cuerpo de la respuesta
     * con el código de estado HTTP especificado y el tipo de contenido "application/json; charset=UTF-8".
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP para la respuesta (e.g., 200, 201, 400).
     * @param data El objeto a serializar como JSON (puede ser una lista, mapa, entidad, etc.).
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
     * Crea un objeto JSON con una clave "error" que contiene el mensaje proporcionado.
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
            System.err.println("Error al enviar respuesta de error: " + e.getMessage());
        }
    }

    /**
     * Parsea los parámetros de una cadena de consulta (query string) de una URL en un mapa de clave-valor.
     * Maneja claves sin valor y parámetros mal formados. Ignora claves duplicadas, manteniendo la primera aparición.
     *
     * @param query La cadena de consulta (ej: "id=1&sort=name"). Puede ser {@code null} o vacía.
     * @return Un {@link Map} que contiene los parámetros de consulta. Devuelve un mapa vacío si la consulta es nula, vacía o no contiene parámetros válidos.
     */
    private Map<String,String> parseQuery(String query){
         if (query == null || query.isEmpty()) {
            return Map.of();
        }
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .filter(pair -> pair.length == 2 && !pair[0].isEmpty())
                .collect(Collectors.toMap(
                        pair -> pair[0],
                        pair -> pair[1],
                        (v1, v2) -> v1
                ));
    }
}
