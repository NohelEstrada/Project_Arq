package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.TransactionsDAO;
import com.sources.app.entities.Transactions;
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
 * sobre la entidad {@link Transactions}, que representa transacciones asociadas a usuarios y hospitales.
 * Gestiona las solicitudes para el endpoint "/api/transactions".
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/transactions}: Obtiene la lista de todas las transacciones.</li>
 *   <li>{@code GET /api/transactions?id={id}}: Obtiene una transacción específica por su ID.</li>
 *   <li>{@code GET /api/transactions?userId={id}} o {@code GET /api/transactions?user_id={id}}: Obtiene todas las transacciones para un usuario específico.</li>
 *   <li>{@code POST /api/transactions}: Crea una nueva transacción. Requiere `user` (con `idUser`) y `hospital` (con `idHospital`) en el cuerpo JSON, además de otros campos de la transacción.</li>
 *   <li>{@code PUT /api/transactions}: Actualiza una transacción existente. Requiere el ID de la transacción (`idTransaction`) dentro del cuerpo JSON.</li>
 *   <li>(DELETE no está implementado en este manejador).</li>
 * </ul>
 */
public class TransactionsHandler implements HttpHandler {

    /** DAO para acceder a los datos de la entidad Transactions. */
    private final TransactionsDAO transactionsDAO;
    /** ObjectMapper para la serialización/deserialización JSON, configurado con formato de fecha yyyy-MM-dd. */
    private final ObjectMapper objectMapper;
    /** Ruta base para las solicitudes gestionadas por este manejador. */
    private static final String ENDPOINT = "/api/transactions";

    /**
     * Constructor del manejador de transacciones.
     * Inicializa el DAO de transacciones y el ObjectMapper, configurando un formato específico
     * para la serialización/deserialización de fechas ("yyyy-MM-dd").
     *
     * @param transactionsDAO El DAO para interactuar con la tabla de transacciones.
     */
    public TransactionsHandler(TransactionsDAO transactionsDAO) {
        this.transactionsDAO = transactionsDAO;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas al endpoint de transacciones.
     * Configura las cabeceras CORS, maneja solicitudes OPTIONS (preflight),
     * valida que la ruta coincida con {@link #ENDPOINT}, y enruta las solicitudes
     * GET, POST y PUT a sus respectivos métodos de manejo. Cualquier otro método resulta
     * en un error 405 (Method Not Allowed).
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración de CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Preflight
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

    /**
     * Maneja las solicitudes GET a {@code /api/transactions}.
     * Permite filtrar por ID de transacción (`?id=...`) o por ID de usuario (`?userId=...` o `?user_id=...`).
     * Si se proporciona `id`, busca y devuelve esa transacción específica.
     * Si se proporciona `userId` o `user_id`, busca y devuelve todas las transacciones para ese usuario.
     * Si no se proporcionan filtros válidos, devuelve la lista completa de todas las transacciones.
     * Responde con 404 si se busca por un ID específico y no se encuentra.
     * Responde con 400 si el formato del ID (transacción o usuario) es inválido.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            Map<String, String> params = parseQuery(query);
            if (params.containsKey("userId") || params.containsKey("user_id")) {
                // Buscar transacciones por ID de usuario (cliente)
                try {
                    // Intentar con ambos formatos de parámetro (userId y user_id)
                    String userIdParam = params.containsKey("userId") ? params.get("userId") : params.get("user_id");
                    Long userId = Long.parseLong(userIdParam);
                    
                    System.out.println("Buscando transacciones para usuario con ID: " + userId);
                    List<Transactions> list = transactionsDAO.findByUserId(userId);
                    System.out.println("Encontradas " + list.size() + " transacciones");
                    
                    String jsonResponse = objectMapper.writeValueAsString(list);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error al convertir ID de usuario: " + e.getMessage());
                    exchange.sendResponseHeaders(400, -1);
                }
            } else if (params.containsKey("id")) {
                // Buscar transacción por su ID
                try {
                    Long id = Long.parseLong(params.get("id"));
                    Transactions t = transactionsDAO.findById(id);
                    if (t != null) {
                        String jsonResponse = objectMapper.writeValueAsString(t);
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
                // Si no se especifica parámetro, retorna todas las transacciones
                List<Transactions> list = transactionsDAO.findAll();
                String jsonResponse = objectMapper.writeValueAsString(list);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }
        } else {
            // Sin query, retornar todas las transacciones
            List<Transactions> list = transactionsDAO.findAll();
            String jsonResponse = objectMapper.writeValueAsString(list);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    /**
     * Maneja las solicitudes POST a {@code /api/transactions} para crear una nueva transacción.
     * Lee el cuerpo JSON, esperando una estructura que incluya objetos anidados para `user` y `hospital`,
     * cada uno conteniendo su respectivo ID (`idUser`, `idHospital`), además de otros campos como
     * `transDate`, `total`, `copay`, etc.
     * Valida que los IDs de usuario y hospital estén presentes.
     * Llama a {@link TransactionsDAO#create(Long, Long, java.util.Date, Double, Double, String, String, String, String)}
     * para guardar la nueva transacción.
     * Responde con 201 (Created) y el objeto de la transacción creada si tiene éxito.
     * Responde con 400 si faltan IDs requeridos o si el JSON es inválido.
     * Responde con 500 si ocurre un error interno durante la creación en la base de datos.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Transactions t = objectMapper.readValue(requestBody, Transactions.class);
            if (t.getUser() == null || t.getUser().getIdUser() == null ||
                    t.getHospital() == null || t.getHospital().getIdHospital() == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
            Transactions created = transactionsDAO.create(
                    t.getUser().getIdUser(),        // Extraer el ID de User
                    t.getHospital().getIdHospital(),  // Extraer el ID de Hospital
                    t.getTransDate(),
                    t.getTotal(),
                    t.getCopay(),
                    t.getTransactionComment(),
                    t.getResult(),
                    t.getCovered(),
                    t.getAuth()
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

    /**
     * Maneja las solicitudes PUT a {@code /api/transactions} para actualizar una transacción existente.
     * Lee el cuerpo JSON de la solicitud, que *debe* contener el ID primario (`idTransaction`) de la transacción a actualizar.
     * Llama a {@link TransactionsDAO#update(Transactions)} para aplicar los cambios.
     * Responde con 200 (OK) y el objeto de la transacción actualizada si tiene éxito.
     * Responde con 400 si el JSON es inválido (no valida explícitamente la presencia del ID aquí).
     * Responde con 500 si la actualización falla en el DAO (podría ser porque el ID no existe o por un error interno).
     * Nota: Sería más robusto validar la presencia del ID y devolver 400/404 apropiadamente.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Transactions t = objectMapper.readValue(requestBody, Transactions.class);
        Transactions updated = transactionsDAO.update(t);
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

    /**
     * Parsea los parámetros de una cadena de consulta (query string) de una URL en un mapa de clave-valor.
     * Maneja claves sin valor y parámetros mal formados. Ignora claves duplicadas, manteniendo la primera aparición.
     * Nota: A diferencia de otros parseQuery, este usa split("=") directamente, lo que podría fallar si un valor contiene "=".
     * Un enfoque más seguro sería usar split("=", 2).
     *
     * @param query La cadena de consulta (ej: "userId=1&limit=10"). Puede ser {@code null} o vacía.
     * @return Un {@link Map} que contiene los parámetros de consulta. Devuelve un mapa vacío si la consulta es nula o vacía.
     */
    private Map<String, String> parseQuery(String query) {
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(
                        kv -> kv[0],
                        kv -> kv.length > 1 ? kv[1] : ""
                ));
    }
}
