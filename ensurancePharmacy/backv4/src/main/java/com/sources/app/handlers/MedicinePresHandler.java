package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.MedicinePresDAO;
import com.sources.app.entities.MedicinePres;
import com.sources.app.entities.Prescription;
import com.sources.app.entities.Medicine;
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
 * Manejador HTTP para las operaciones CRUD (Crear, Leer, Actualizar - aunque Actualizar puede ser no estándar para una tabla de unión)
 * sobre la entidad {@link MedicinePres}, que representa la relación muchos-a-muchos
 * entre una {@link Medicine} (medicamento) y una {@link Prescription} (prescripción).
 * Indica qué medicamentos específicos están incluidos en una prescripción dada.
 * Gestiona las solicitudes para el endpoint "/api/medicinepres".
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/medicinepres}: Obtiene todas las relaciones medicamento-prescripción existentes.</li>
 *   <li>{@code GET /api/medicinepres?idPrescription={pid}&idMedicine={mid}}: Obtiene una relación específica basada en la clave compuesta (ID de prescripción e ID de medicamento).</li>
 *   <li>{@code POST /api/medicinepres}: Crea una nueva relación entre un medicamento y una prescripción. Requiere `prescription` (con `idPrescription`) y `medicine` (con `idMedicine`) en el cuerpo JSON.</li>
 *   <li>{@code PUT /api/medicinepres}: Intenta actualizar una relación existente. Requiere `prescription` (con `idPrescription`) y `medicine` (con `idMedicine`) en el cuerpo JSON. Su utilidad es limitada si la tabla no tiene campos adicionales más allá de las claves foráneas.</li>
 *   <li>(DELETE no está implementado, pero podría añadirse para eliminar una relación específica por clave compuesta o ID si tuviera).</li>
 * </ul>
 */
public class MedicinePresHandler implements HttpHandler {

    /** DAO para acceder a los datos de la entidad de relación MedicinePres. */
    private final MedicinePresDAO medicinePresDAO;
    /** ObjectMapper para la serialización/deserialización JSON, configurado con formato de fecha yyyy-MM-dd. */
    private final ObjectMapper objectMapper;
    /** Ruta base para las solicitudes gestionadas por este manejador. */
    private static final String ENDPOINT = "/api/medicinepres";

    /**
     * Constructor del manejador de relaciones Medicina-Prescripción.
     * Inicializa el DAO específico y el ObjectMapper, configurando un formato de fecha
     * ("yyyy-MM-dd") aunque esta entidad particular no parece tener campos de fecha propios.
     *
     * @param medicinePresDAO El DAO para interactuar con la tabla de unión {@code MedicinePres}.
     */
    public MedicinePresHandler(MedicinePresDAO medicinePresDAO) {
        this.medicinePresDAO = medicinePresDAO;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas al endpoint de relaciones Medicina-Prescripción.
     * Configura las cabeceras CORS, maneja solicitudes OPTIONS (preflight),
     * valida que la ruta coincida con {@link #ENDPOINT}, y enruta las solicitudes
     * GET, POST y PUT a sus respectivos métodos de manejo. Cualquier otro método resulta
     * en un error 405 (Method Not Allowed).
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida (generalmente manejado internamente).
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Configuración CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Preflight (solicitud OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Verificar endpoint
        String path = exchange.getRequestURI().getPath();
        if (!path.equalsIgnoreCase(ENDPOINT)) {
            exchange.sendResponseHeaders(404, -1); // Not Found
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
            // DELETE podría añadirse si se necesita eliminar una relación específica
            default:
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    /**
     * Maneja las solicitudes GET a {@code /api/medicinepres}.
     * Si se proporcionan los parámetros de consulta 'idPrescription' y 'idMedicine',
     * intenta obtener la relación específica {@link MedicinePres} usando {@link MedicinePresDAO#findById(Long, Long)}.
     * Si no se proporcionan ambos IDs o alguno es inválido, obtiene y devuelve la lista completa
     * de todas las relaciones usando {@link MedicinePresDAO#findAll()}.
     * Responde con 404 si la relación específica buscada por IDs no se encuentra.
     * Responde con 400 si los IDs proporcionados no son números válidos.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        // Para buscar por clave compuesta, se requieren ambos IDs
        if (query != null && query.contains("idPrescription=") && query.contains("idMedicine=")) {
            Map<String, String> params = parseQuery(query);
            try {
                Long idPrescription = Long.parseLong(params.get("idPrescription"));
                Long idMedicine = Long.parseLong(params.get("idMedicine"));
                // Busca la relación específica en la BD
                MedicinePres mp = medicinePresDAO.findById(idPrescription, idMedicine);
                if (mp != null) {
                    // Relación encontrada, enviar respuesta JSON
                    String jsonResponse = objectMapper.writeValueAsString(mp);
                    sendJsonResponse(exchange, 200, jsonResponse);
                } else {
                    // Relación no encontrada
                    exchange.sendResponseHeaders(404, -1);
                }
            } catch (NumberFormatException e) {
                // IDs inválidos
                 System.err.println("IDs inválidos en GET: " + query);
                exchange.sendResponseHeaders(400, -1);
            }
        } else {
            // Si no se especifican IDs, obtiene todas las relaciones
            List<MedicinePres> list = medicinePresDAO.findAll();
            String jsonResponse = objectMapper.writeValueAsString(list);
            sendJsonResponse(exchange, 200, jsonResponse);
        }
    }

    /**
     * Maneja las solicitudes POST a {@code /api/medicinepres} para crear una nueva relación.
     * Lee el cuerpo JSON, esperando una estructura que contenga objetos anidados para `prescription` y `medicine`,
     * cada uno con su respectivo ID (`idPrescription`, `idMedicine`).
     * Valida que estos objetos y sus IDs no sean nulos.
     * Llama a {@link MedicinePresDAO#create(Long, Long)} para crear la entrada en la tabla de unión.
     * Responde con 201 (Created) y el objeto {@link MedicinePres} creado (generalmente solo con los IDs) si tiene éxito.
     * Responde con 400 si faltan datos requeridos o el JSON es inválido.
     * Responde con 500 si ocurre un error en el DAO (podría ser también 409 Conflict si la relación ya existe y hay una restricción UNIQUE).
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            // Lee y parsea el cuerpo de la solicitud
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            MedicinePres mp = objectMapper.readValue(requestBody, MedicinePres.class);
            
            // Extrae los objetos Prescription y Medicine del objeto principal
            Prescription pres = mp.getPrescription();
            Medicine med = mp.getMedicine();

            // Validación: Asegurarse de que los objetos y sus IDs no son nulos
            if (pres == null || med == null ||
                    pres.getIdPrescription() == null || med.getIdMedicine() == null) {
                System.err.println("Datos incompletos para crear relación MedicinePres.");
                exchange.sendResponseHeaders(400, -1); // Bad Request
                return;
            }

            // Crea la relación usando solo los IDs
            MedicinePres created = medicinePresDAO.create(
                    pres.getIdPrescription(),
                    med.getIdMedicine()
            );
            
            if (created != null) {
                // Relación creada exitosamente, enviar respuesta JSON
                String jsonResponse = objectMapper.writeValueAsString(created);
                sendJsonResponse(exchange, 201, jsonResponse); // 201 Created
            } else {
                // Error al crear la relación (posiblemente ya existe o error de BD)
                System.err.println("Error en DAO al crear relación MedicinePres.");
                exchange.sendResponseHeaders(500, -1); // Internal Server Error (o 409 Conflict si ya existe)
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Excepción al procesar POST de MedicinePres: " + e.getMessage());
            exchange.sendResponseHeaders(500, -1); // Internal Server Error (o 400 si es error de parseo)
        }
    }

    /**
     * Maneja las solicitudes PUT a {@code /api/medicinepres} para actualizar (o verificar la existencia de) una relación.
     * Lee el cuerpo JSON esperando la misma estructura que POST (`prescription` con `idPrescription`, `medicine` con `idMedicine`).
     * Valida que los IDs necesarios estén presentes.
     * Llama a {@link MedicinePresDAO#update(MedicinePres)}. La lógica exacta de 'update' en una tabla de unión simple
     * puede variar; podría simplemente verificar si la relación existe y devolverla, o fallar si no existe.
     * Asume que no hay otros campos en {@link MedicinePres} para actualizar.
     * Responde con 200 (OK) y el objeto {@link MedicinePres} si la operación del DAO tiene éxito (encontrado/actualizado).
     * Responde con 400 si faltan IDs o el JSON es inválido.
     * Responde con 404 o 500 si el DAO indica que la relación no se encontró o hubo un error.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al enviar la respuesta.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
         try {
            // Lee y parsea el cuerpo de la solicitud
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            MedicinePres mp = objectMapper.readValue(requestBody, MedicinePres.class);

            // Validar que los IDs necesarios estén presentes
            if (mp.getPrescription() == null || mp.getMedicine() == null ||
                mp.getPrescription().getIdPrescription() == null || mp.getMedicine().getIdMedicine() == null) {
                 System.err.println("IDs faltantes para actualizar MedicinePres.");
                 exchange.sendResponseHeaders(400, -1); // Bad Request
                 return;
            }

            // Intenta actualizar (la lógica exacta depende del DAO)
            MedicinePres updated = medicinePresDAO.update(mp);
            
            if (updated != null) {
                // Actualización (o verificación de existencia) exitosa
                String jsonResponse = objectMapper.writeValueAsString(updated);
                sendJsonResponse(exchange, 200, jsonResponse);
            } else {
                // Error al actualizar o no encontrado
                System.err.println("Error en DAO al actualizar MedicinePres o no encontrado.");
                exchange.sendResponseHeaders(500, -1); // Considerar 404 si el DAO indica no encontrado
            }
         } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Excepción al procesar PUT de MedicinePres: " + e.getMessage());
            exchange.sendResponseHeaders(500, -1); // O 400 si es error de parseo
        }
    }

    /**
     * Envía una respuesta JSON al cliente.
     * Escribe la cadena JSON proporcionada en el cuerpo de la respuesta con el código de estado HTTP especificado.
     * Establece el tipo de contenido a "application/json; charset=UTF-8".
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP para la respuesta (e.g., 200, 201).
     * @param body La cadena JSON que representa el cuerpo de la respuesta.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
     private void sendJsonResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Parsea los parámetros de una cadena de consulta (query string) de una URL en un mapa de clave-valor.
     * Maneja claves sin valor y parámetros mal formados. Ignora claves duplicadas, manteniendo la primera aparición.
     *
     * @param query La cadena de consulta (ej: "idPrescription=1&idMedicine=5"). Puede ser {@code null} o vacía.
     * @return Un {@link Map} que contiene los parámetros de consulta. Devuelve un mapa vacío si la consulta es nula, vacía o no contiene parámetros válidos.
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
