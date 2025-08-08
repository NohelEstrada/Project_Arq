package com.sources.app.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.InsuranceServiceDAO;
import com.sources.app.dao.CategoryDAO;
import com.sources.app.entities.InsuranceService;
import com.sources.app.entities.Category;
import com.sources.app.util.HttpClientUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manejador HTTP para gestionar las solicitudes relacionadas con los servicios de seguro.
 * Proporciona operaciones CRUD para la entidad {@link InsuranceService} y maneja la
 * interacción con una API externa de servicios hospitalarios para importar datos.
 *
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>{@code GET /api/insurance-services}: Obtiene todos los servicios de seguro internos.
 *       Permite filtrar por ID de categoría usando el parámetro de consulta {@code ?category_id=}.</li>
 *   <li>{@code GET /api/insurance-services/{id}}: Obtiene un servicio de seguro interno específico por su ID.</li>
 *   <li>{@code POST /api/insurance-services}: Crea un nuevo servicio de seguro interno.</li>
 *   <li>{@code PUT /api/insurance-services/{id}}: Actualiza un servicio de seguro interno existente por su ID.</li>
 *   <li>{@code DELETE /api/insurance-services/{id}}: Elimina (marcado como deshabilitado) un servicio de seguro interno por su ID.</li>
 *   <li>{@code GET /api/insurance-services/hospital-services}: Obtiene la lista de servicios ofrecidos por la API externa del hospital.</li>
 *   <li>{@code POST /api/insurance-services/approve-hospital-service}: Importa/aprueba un servicio desde la API externa del hospital,
 *       creándolo en la base de datos interna si no existe.</li>
 * </ul>
 */
public class InsuranceServiceHandler implements HttpHandler {

    /** Logger para registrar eventos y errores de este manejador. */
    private static final Logger LOGGER = Logger.getLogger(InsuranceServiceHandler.class.getName());

    /** DAO para acceder a los datos de los servicios de seguro. */
    private final InsuranceServiceDAO insuranceServiceDAO;
    /** DAO para acceder a los datos de las categorías. */
    private final CategoryDAO categoryDAO;
    /** ObjectMapper para la serialización/deserialización JSON. */
    private final ObjectMapper objectMapper;

    /** Ruta base para los endpoints de servicios de seguro. */
    private static final String ENDPOINT = "/api/insurance-services";
    /** Subruta para obtener servicios de la API externa del hospital. */
    private static final String HOSPITAL_SERVICES_PATH = ENDPOINT + "/hospital-services";
    /** Subruta para aprobar/importar un servicio de la API externa del hospital. */
    private static final String APPROVE_HOSPITAL_SERVICE_PATH = ENDPOINT + "/approve-hospital-service";

    // TODO: Externalizar esta URL base a un archivo de configuración
    /** URL base de la API externa del hospital. */
    private static final String HOSPITAL_API_BASE_URL = "http://0.0.0.0:5050/api";
    // TODO: Externalizar lista de URLs a intentar o mejorar la lógica de descubrimiento
    /** Lista de URLs candidatas para obtener los servicios de la API externa del hospital. */
    private static final String[] HOSPITAL_SERVICE_URLS = {
            "http://0.0.0.0:5050/api/services/",
            "http://0.0.0.0:5050/api/services",
            "http://localhost:5050/api/services/",
            "http://localhost:5050/api/services",
            "http://127.0.0.1:5050/api/services/",
            "http://127.0.0.1:5050/api/services",
            "http://192.168.0.4:5052/api/services/", // Suponiendo posibles IPs locales
            "http://192.168.0.4:5052/api/services",
            "http://192.168.0.4:5050/api/services/",
            "http://192.168.0.4:5050/api/services"
        };

    /**
     * Constructor del manejador de servicios de seguro.
     * Inicializa los DAOs necesarios y el ObjectMapper.
     *
     * @param insuranceServiceDAO DAO para interactuar con la tabla de servicios de seguro.
     * @param categoryDAO DAO para interactuar con la tabla de categorías.
     */
    public InsuranceServiceHandler(InsuranceServiceDAO insuranceServiceDAO, CategoryDAO categoryDAO) {
        this.insuranceServiceDAO = insuranceServiceDAO;
        this.categoryDAO = categoryDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas a los endpoints
     * relacionados con los servicios de seguro. Configura CORS, maneja las solicitudes OPTIONS (preflight)
     * y enruta la solicitud al método de manejo apropiado (GET, POST, PUT, DELETE) basado en
     * el método HTTP y la ruta de la solicitud.
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida durante el manejo de la solicitud.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración de CORS Headers para permitir solicitudes desde cualquier origen
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Manejo de solicitudes OPTIONS (preflight)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No Content
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        String method = exchange.getRequestMethod().toUpperCase();

         try {
             // Verificar que la ruta pertenezca a este manejador
            if (!path.startsWith(ENDPOINT)) {
                LOGGER.log(Level.FINE, "Ruta no manejada por InsuranceServiceHandler: {0}", path);
                sendErrorResponse(exchange, 404, "Endpoint no encontrado.");
                return;
            }

             // Extraer ID numérico de la ruta si está presente (ej: /api/insurance-services/123)
            Long idFromPath = extractIdFromPath(path, ENDPOINT);

            // Enrutamiento basado en método y ruta
            switch (method) {
                case "GET":
                    if (path.equals(HOSPITAL_SERVICES_PATH)) {
                        handleGetHospitalServices(exchange);
                    } else if (idFromPath != null) {
                        handleGetById(exchange, idFromPath);
                    } else if (path.equals(ENDPOINT)) {
                        handleGetAll(exchange, query);
                    } else {
                         LOGGER.log(Level.WARNING, "Intento de acceso a ruta GET no válida: {0}", path);
                         sendErrorResponse(exchange, 404, "Ruta GET no válida: " + path);
                    }
                    break;
                case "POST":
                    if (path.equals(APPROVE_HOSPITAL_SERVICE_PATH)) {
                        handleApproveHospitalService(exchange);
                    } else if (path.equals(ENDPOINT)) {
                        handleCreate(exchange);
                    } else {
                         LOGGER.log(Level.WARNING, "Intento de acceso a ruta POST no válida: {0}", path);
                         sendErrorResponse(exchange, 404, "Ruta POST no válida: " + path);
                    }
                    break;
                case "PUT":
                     if (idFromPath != null) {
                        handleUpdate(exchange, idFromPath);
                    } else {
                        LOGGER.log(Level.WARNING, "Intento de PUT sin ID en la ruta: {0}", path);
                        sendErrorResponse(exchange, 400, "Falta el ID en la ruta para PUT. Formato esperado: " + ENDPOINT + "/{id}");
                    }
                    break;
                case "DELETE":
                     if (idFromPath != null) {
                        handleDelete(exchange, idFromPath);
                    } else {
                         LOGGER.log(Level.WARNING, "Intento de DELETE sin ID en la ruta: {0}", path);
                         sendErrorResponse(exchange, 400, "Falta el ID en la ruta para DELETE. Formato esperado: " + ENDPOINT + "/{id}");
                    }
                    break;
                default:
                    // Método HTTP no soportado
                    LOGGER.log(Level.WARNING, "Método HTTP no permitido: {0} en ruta {1}", new Object[]{method, path});
                    sendErrorResponse(exchange, 405, "Método no permitido: " + method);
                    break;
            }
         } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error al parsear ID de la ruta: " + path, e);
            sendErrorResponse(exchange, 400, "El ID proporcionado en la ruta no es un número válido.");
         } catch (Exception e) {
            // Captura general para errores inesperados
            LOGGER.log(Level.SEVERE, "Error inesperado en InsuranceServiceHandler: " + exchange.getRequestURI(), e);
            // Evitar enviar otra respuesta si las cabeceras ya fueron enviadas
             if (exchange.getResponseCode() == -1) {
                 sendErrorResponse(exchange, 500, "Error interno inesperado del servidor.");
             }
        } finally {
            exchange.close(); // Asegurar que el exchange se cierre siempre
        }
    }

    /**
     * Extrae un ID numérico (Long) del final de una ruta URI, asumiendo que sigue
     * inmediatamente después de la ruta base especificada.
     *
     * <p>Ejemplos:</p>
     * <ul>
     *   <li>{@code extractIdFromPath("/api/items/123", "/api/items")} retorna {@code 123L}.</li>
     *   <li>{@code extractIdFromPath("/api/items/abc", "/api/items")} retorna {@code null}.</li>
     *   <li>{@code extractIdFromPath("/api/items/", "/api/items")} retorna {@code null}.</li>
     *   <li>{@code extractIdFromPath("/api/items", "/api/items")} retorna {@code null}.</li>
     *   <li>{@code extractIdFromPath("/api/items/123/details", "/api/items")} retorna {@code 123L}.</li>
     * </ul>
     *
     * @param path La ruta completa de la solicitud (ej: "/api/insurance-services/5").
     * @param basePath La parte base de la ruta que precede al ID (ej: "/api/insurance-services").
     * @return El ID extraído como un {@link Long}, o {@code null} si no se encuentra un ID válido
     *         al final de la ruta o si la ruta no tiene el formato esperado.
     * @throws NumberFormatException Si la parte de la ruta después de {@code basePath + "/"} no es un número válido.
     */
    private Long extractIdFromPath(String path, String basePath) throws NumberFormatException {
        String prefix = basePath + "/";
        if (path.startsWith(prefix) && path.length() > prefix.length()) {
            String potentialIdPart = path.substring(prefix.length());
            // Manejar sub-rutas después del ID, ej: /services/123/details -> extrae 123
            String idStr = potentialIdPart.split("/")[0];
            // Validar si está vacío después de quitar posibles subrutas
            if (idStr.isEmpty()) {
                return null;
            }
            try {
                 return Long.parseLong(idStr);
            } catch (NumberFormatException e) {
                 LOGGER.log(Level.WARNING, "Intento de parsear ID inválido: '" + idStr + "' en la ruta: " + path);
                 throw e; // Relanzar para manejo centralizado en handle()
            }
        }
        return null; // No hay ID en la ruta o el formato es incorrecto
    }

    /**
     * Maneja la solicitud GET a {@code /api/insurance-services/hospital-services}.
     * Intenta obtener la lista de servicios desde una API externa de hospital, probando
     * una lista predefinida de URLs. Marca los servicios que ya existen en la base de datos local.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error de comunicación o al procesar la respuesta.
     */
    private void handleGetHospitalServices(HttpExchange exchange) throws IOException {
        LOGGER.log(Level.INFO, "Solicitud GET para obtener servicios de hospital externos.");
        String servicesJson = null;
        String successUrl = "";

        // Intenta obtener los servicios desde las URLs configuradas
        for (String url : HOSPITAL_SERVICE_URLS) {
            LOGGER.log(Level.FINE, "Intentando obtener servicios externos desde: {0}", url);
            try {
                servicesJson = HttpClientUtil.get(url);
                if (servicesJson != null && !servicesJson.trim().isEmpty()) {
                    successUrl = url;
                    LOGGER.log(Level.INFO, "Servicios externos obtenidos exitosamente desde: {0}", url);
                    break;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error inesperado al obtener servicios externos desde " + url, e);
                // Considerar continuar con la siguiente URL o manejar de otra forma
            }
        }

        if (servicesJson == null || servicesJson.trim().isEmpty()) {
            LOGGER.log(Level.SEVERE, "No se pudo obtener la lista de servicios externos de ninguna URL configurada.");
            sendErrorResponse(exchange, 503, "No se pudo conectar con la API externa de servicios hospitalarios.");
            return;
        }

        try {
            // Parsear la respuesta JSON de la API externa
            // Asume que la respuesta es una lista de objetos, cada uno con al menos 'id' y 'nombre'
            List<Map<String, Object>> externalServices = objectMapper.readValue(servicesJson, new TypeReference<List<Map<String, Object>>>() {});

            // Obtener los IDs de los servicios externos que ya están importados/registrados localmente
            List<String> existingExternalIds = insuranceServiceDAO.findAll().stream()
                    .map(InsuranceService::getExternalId)
                    .filter(id -> id != null && !id.isEmpty())
                    .collect(Collectors.toList());

            // Marcar cada servicio externo indicando si ya está registrado localmente
            List<Map<String, Object>> processedServices = new ArrayList<>();
            for (Map<String, Object> service : externalServices) {
                // Intentar obtener el ID externo (puede ser numérico o cadena)
                String externalId = null;
                Object idObj = service.get("id");
                if (idObj != null) {
                    externalId = String.valueOf(idObj);
                }

                // Asegurarse de que otros campos esperados (nombre, descripción, precio) existan
                // y asignar un valor por defecto o manejar si son null si es necesario.
                service.putIfAbsent("nombre", "Nombre no disponible");
                service.putIfAbsent("descripcion", "Descripción no disponible");
                service.putIfAbsent("precio", 0.0); // Asumir 0 si no está presente

                // Añadir el campo 'isRegisteredLocally'
                service.put("isRegisteredLocally", externalId != null && existingExternalIds.contains(externalId));
                processedServices.add(service);
            }

            LOGGER.log(Level.INFO, "Enviando lista de {0} servicios externos (URL: {1}).", new Object[]{processedServices.size(), successUrl});
            sendJsonResponse(exchange, 200, processedServices);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al parsear la respuesta JSON de la API externa desde " + successUrl, e);
            sendErrorResponse(exchange, 500, "Error al procesar la respuesta de la API externa.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al procesar servicios externos.", e);
            sendErrorResponse(exchange, 500, "Error interno al procesar servicios externos.");
        }
    }

    /**
     * Maneja la solicitud POST a {@code /api/insurance-services/approve-hospital-service}.
     * Importa un servicio desde la API externa del hospital, creando una entrada correspondiente
     * en la base de datos local si no existe ya un servicio con el mismo ID externo.
     * Espera un cuerpo JSON con los detalles del servicio a importar, incluyendo al menos
     * 'id', 'nombre', 'descripcion', 'precio' y 'categoria_id'.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al procesar el JSON.
     */
    private void handleApproveHospitalService(HttpExchange exchange) throws IOException {
        LOGGER.log(Level.INFO, "Solicitud POST para aprobar/importar servicio de hospital.");
        try {
            String requestBody = readRequestBody(exchange);
            if (requestBody.isEmpty()) {
                LOGGER.log(Level.WARNING, "Cuerpo de solicitud vacío para aprobación de servicio.");
                sendErrorResponse(exchange, 400, "Cuerpo de la solicitud vacío.");
                return;
            }

            // Parsear JSON a Map para extraer datos
            Map<String, Object> serviceData = objectMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {});

            // Extraer y validar datos requeridos
            Object externalIdObj = serviceData.get("id");
            String name = (String) serviceData.get("nombre"); // Asegurarse de que el nombre del campo coincida
            String description = (String) serviceData.get("descripcion");
            Double price = parseDoubleFromMap(serviceData, "precio");
            Long categoryId = parseLongFromMap(serviceData, "category_id");

            if (externalIdObj == null) {
                sendErrorResponse(exchange, 400, "Falta el campo 'id' del servicio externo.");
                return;
            }
            String externalId = String.valueOf(externalIdObj); // Aceptar ID externo como cadena

            if (name == null || name.trim().isEmpty()) {
                sendErrorResponse(exchange, 400, "Falta el campo 'nombre' o está vacío.");
                return;
            }
            if (price == null || price < 0) {
                sendErrorResponse(exchange, 400, "El campo 'precio' es inválido o negativo.");
                return;
            }
            if (categoryId == null) {
                sendErrorResponse(exchange, 400, "Falta el campo 'category_id'.");
                return;
            }
            // Descripción es opcional, normalizar a vacío si es null
            if (description == null) {
                description = "";
            }

            // Validar si la categoría existe
            Category category = categoryDAO.findById(categoryId);
            if (category == null) {
                LOGGER.log(Level.WARNING, "Intento de importar servicio a categoría inexistente ID: {0}", categoryId);
                sendErrorResponse(exchange, 404, "La categoría especificada con ID " + categoryId + " no existe.");
                return;
            }

            // Verificar si un servicio con este ID externo ya existe
            boolean exists = insuranceServiceDAO.findByExternalId(externalId) != null;

            if (exists) {
                LOGGER.log(Level.INFO, "Servicio externo con ID '{0}' ya está registrado localmente.", externalId);
                sendErrorResponse(exchange, 409, "El servicio externo con ID '" + externalId + "' ya está registrado.");
                return;
            }

            // Crear la nueva entidad de servicio
            InsuranceService newService = new InsuranceService();
            newService.setName(name.trim());
            newService.setDescription(description.trim());
            newService.setPrice(price);
            newService.setCategory(category);
            newService.setEnabled(1); // Habilitado por defecto al importar
            newService.setExternalId(externalId); // Guardar el ID externo

            // Guardar en la base de datos
            insuranceServiceDAO.create(newService);

            LOGGER.log(Level.INFO, "Servicio externo ID '{0}' importado exitosamente como servicio local (ID BD potencial: {1})",
                       new Object[]{externalId, newService.getIdInsuranceService()}); // ID puede ser null si create no lo actualiza

            // Devolver el servicio recién creado (o al menos los datos enviados)
            // Es mejor devolver el objeto creado si el DAO.create lo actualiza con el ID
            // Como alternativa segura, devolver un mensaje de éxito o los datos originales + ID BD si está disponible.
             Map<String, Object> responseData = new HashMap<>();
             responseData.put("message", "Servicio importado exitosamente.");
             // Incluir el ID de BD si está disponible (puede ser null)
             responseData.put("local_id", newService.getIdInsuranceService());
             responseData.put("external_id", newService.getExternalId());
             responseData.put("name", newService.getName());
             responseData.put("category_id", newService.getCategory().getIdCategory());

            sendJsonResponse(exchange, 201, responseData);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al leer/parsear JSON para importar servicio.", e);
            sendErrorResponse(exchange, 400, "Error al leer o parsear el cuerpo de la solicitud JSON.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al importar el servicio.", e);
            sendErrorResponse(exchange, 500, "Error interno del servidor al importar el servicio.");
        }
    }

    /**
     * Maneja la solicitud GET a {@code /api/insurance-services}.
     * Obtiene una lista de todos los servicios de seguro internos. Permite filtrar opcionalmente
     * por ID de categoría usando el parámetro de consulta {@code category_id}.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param query La cadena de consulta de la URL (puede contener {@code category_id}).
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGetAll(HttpExchange exchange, String query) throws IOException {
        LOGGER.log(Level.INFO, "Solicitud GET para obtener todos los servicios de seguro (query: {0})", query);
        List<InsuranceService> services;
        Map<String, String> queryParams = parseQueryParams(query);
        String categoryIdStr = queryParams.get("category_id");

        try {
            if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
                try {
                    Long categoryId = Long.parseLong(categoryIdStr);
                    LOGGER.log(Level.INFO, "Filtrando servicios por category_id: {0}", categoryId);
                    // Necesitaríamos un método DAO como `findByCategoryId` o filtrar aquí
                    // Filtrando en memoria como ejemplo si DAO no lo soporta:
                    services = insuranceServiceDAO.findAll().stream()
                        .filter(s -> s.getCategory() != null && s.getCategory().getIdCategory().equals(categoryId))
                        .collect(Collectors.toList());
                    if (services.isEmpty()) {
                         LOGGER.log(Level.INFO, "No se encontraron servicios para la categoría ID: {0}", categoryId);
                         // Devolver lista vacía es correcto, no es un error 404
                    }
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Parámetro category_id inválido: {0}", categoryIdStr);
                    sendErrorResponse(exchange, 400, "El parámetro 'category_id' debe ser un número válido.");
                    return;
                }
            } else {
                // Obtener todos los servicios si no hay filtro de categoría
                services = insuranceServiceDAO.findAll();
            }
            LOGGER.log(Level.INFO, "Enviando {0} servicios.", services.size());
            sendJsonResponse(exchange, 200, services);
        } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Error al obtener todos los servicios.", e);
             sendErrorResponse(exchange, 500, "Error interno del servidor al obtener los servicios.");
        }
    }

    /**
     * Maneja la solicitud GET a {@code /api/insurance-services/{id}}.
     * Obtiene un servicio de seguro específico por su ID.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param id El ID del servicio a obtener (extraído de la ruta).
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void handleGetById(HttpExchange exchange, Long id) throws IOException {
        LOGGER.log(Level.INFO, "Solicitud GET para servicio ID: {0}", id);
        try {
            InsuranceService service = insuranceServiceDAO.findById(id);
            if (service != null) {
                 LOGGER.log(Level.FINE, "Servicio encontrado ID: {0}", id);
                sendJsonResponse(exchange, 200, service);
            } else {
                LOGGER.log(Level.WARNING, "Servicio no encontrado ID: {0}", id);
                sendErrorResponse(exchange, 404, "Servicio de seguro no encontrado con ID: " + id);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener servicio ID: " + id, e);
            sendErrorResponse(exchange, 500, "Error interno del servidor al obtener el servicio.");
        }
    }

    /**
     * Maneja la solicitud POST a {@code /api/insurance-services}.
     * Crea un nuevo servicio de seguro con los datos proporcionados en el cuerpo JSON.
     * Requiere 'name', 'price', y 'category_id'. 'description' es opcional.
     * Valida que no exista otro servicio con el mismo nombre en la misma categoría.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @throws IOException Si ocurre un error al leer el cuerpo de la solicitud o al procesar el JSON.
     */
    private void handleCreate(HttpExchange exchange) throws IOException {
        LOGGER.log(Level.INFO, "Solicitud POST para crear un nuevo servicio de seguro.");
        try {
            String requestBody = readRequestBody(exchange);
            if (requestBody.isEmpty()) {
                LOGGER.log(Level.WARNING, "Cuerpo de solicitud vacío para crear servicio.");
                sendErrorResponse(exchange, 400, "Cuerpo de la solicitud vacío.");
                return;
            }

            // Parsear JSON a Map para extraer datos
            Map<String, Object> serviceData = objectMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {});

            // Extraer y validar datos requeridos
            String name = (String) serviceData.get("name");
            Double price = parseDoubleFromMap(serviceData, "price");
            Long categoryId = parseLongFromMap(serviceData, "category_id");
            String description = (String) serviceData.getOrDefault("description", ""); // Default a vacío
            // 'enabled' por defecto a 1 (activo), permitir especificarlo opcionalmente
            Object enabledObj = serviceData.get("enabled");
            Integer enabled = 1; // Default a 1
            if (enabledObj != null) {
                enabled = parseEnabledFlag(enabledObj);
                if (enabled == null) {
                    sendErrorResponse(exchange, 400, "El campo 'enabled' debe ser booleano (true/false), número (1/0) o cadena ('true'/'false'/'1'/'0').");
                    return;
                }
            }


            // Validaciones básicas
            if (name == null || name.trim().isEmpty()) {
                sendErrorResponse(exchange, 400, "El campo 'name' es obligatorio y no puede estar vacío.");
                return;
            }
            if (price == null || price < 0) {
                sendErrorResponse(exchange, 400, "El campo 'price' es obligatorio y debe ser un número no negativo.");
                return;
            }
            if (categoryId == null) {
                sendErrorResponse(exchange, 400, "El campo 'category_id' es obligatorio.");
                return;
            }
            // Normalizar descripción
            description = description.trim();
            name = name.trim();

            // Validar si la categoría existe
            Category category = categoryDAO.findById(categoryId);
            if (category == null) {
                LOGGER.log(Level.WARNING, "Intento de crear servicio con categoría inexistente ID: {0}", categoryId);
                sendErrorResponse(exchange, 404, "La categoría especificada con ID " + categoryId + " no existe.");
                return;
            }

            // Crear la entidad
            InsuranceService newService = new InsuranceService();
            newService.setName(name);
            newService.setDescription(description);
            newService.setPrice(price);
            newService.setCategory(category);
            newService.setEnabled(enabled); // Usar el valor parseado o el default
            newService.setExternalId(null); // No es un servicio externo importado

            // Validar si ya existe un servicio con el mismo nombre en la misma categoría
            final String finalName = name; // Necesario para lambda
            final Long finalCategoryId = categoryId; // Necesario para lambda
            boolean nameConflict = insuranceServiceDAO.findAll().stream()
                    .filter(s -> s.getCategory() != null && s.getCategory().getIdCategory().equals(finalCategoryId))
                    .anyMatch(s -> s.getName().equalsIgnoreCase(finalName));

            if (nameConflict) {
                LOGGER.log(Level.WARNING, "Conflicto: Ya existe un servicio con el nombre '{0}' en la categoría ID {1}.", new Object[]{finalName, finalCategoryId});
                sendErrorResponse(exchange, 409, "Ya existe un servicio con el nombre '" + finalName + "' en la categoría seleccionada.");
                return;
            }

            // Guardar en la base de datos
            insuranceServiceDAO.create(newService);
            LOGGER.log(Level.INFO, "Servicio creado (Nombre={0}, CategoriaID={1}) -> ID BD: {2}",
                       new Object[]{finalName, finalCategoryId, newService.getIdInsuranceService()});

            // Devolver el servicio creado (el objeto newService ahora debería tener el ID asignado por el DAO.create)
            sendJsonResponse(exchange, 201, newService);

        } catch (IOException e) {
             LOGGER.log(Level.SEVERE, "Error al leer/parsear JSON para crear servicio.", e);
            sendErrorResponse(exchange, 400, "Error al leer o parsear el cuerpo de la solicitud JSON.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al crear el servicio.", e);
            sendErrorResponse(exchange, 500, "Error interno del servidor al crear el servicio.");
        }
    }


    /**
     * Maneja la solicitud PUT a {@code /api/insurance-services/{id}}.
     * Actualiza un servicio de seguro existente con los datos proporcionados en el cuerpo JSON.
     * Solo actualiza los campos presentes en la solicitud.
     * Valida que el nombre actualizado no entre en conflicto con otro servicio en la misma categoría.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param id El ID del servicio a actualizar (extraído de la ruta).
     * @throws IOException Si ocurre un error de entrada/salida o al parsear el JSON.
     */
    private void handleUpdate(HttpExchange exchange, Long id) throws IOException {
         LOGGER.log(Level.INFO, "Solicitud PUT para actualizar servicio ID: {0}", id);
        try {
            // 1. Verificar si el servicio existe
            InsuranceService existingService = insuranceServiceDAO.findById(id);
            if (existingService == null) {
                LOGGER.log(Level.WARNING, "Intento de actualizar servicio inexistente ID: {0}", id);
                sendErrorResponse(exchange, 404, "Servicio de seguro no encontrado con ID: " + id);
                return;
            }

            // 2. Leer y parsear el cuerpo de la solicitud
            String requestBody = readRequestBody(exchange);
            if (requestBody.isEmpty()) {
                LOGGER.log(Level.WARNING, "Cuerpo de solicitud vacío para actualizar servicio ID: {0}", id);
                sendErrorResponse(exchange, 400, "Cuerpo de la solicitud vacío para la actualización.");
                return;
            }

            // Parsear JSON a Map para actualización parcial
            Map<String, Object> updateData = objectMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {});
            boolean updated = false;
            String originalName = existingService.getName();
            Long originalCategoryId = (existingService.getCategory() != null) ? existingService.getCategory().getIdCategory() : null;

            // 3. Validar y preparar actualizaciones
            String newName = null;
            if (updateData.containsKey("name")) {
                newName = (String) updateData.get("name");
                if (newName == null || newName.trim().isEmpty()) {
                    sendErrorResponse(exchange, 400, "El campo 'name' no puede estar vacío.");
                    return;
                }
                newName = newName.trim();
                if (!existingService.getName().equals(newName)) {
                    LOGGER.log(Level.FINE, "Actualizando nombre para servicio ID {0} de '{1}' a '{2}'", new Object[]{id, existingService.getName(), newName});
                    existingService.setName(newName);
                    updated = true;
                }
            }

            if (updateData.containsKey("description")) {
                String description = (String) updateData.get("description");
                description = (description == null) ? "" : description.trim(); // Normalizar null a vacío y trim
                if (!existingService.getDescription().equals(description)) {
                   existingService.setDescription(description);
                    LOGGER.log(Level.FINE, "Actualizando descripción para servicio ID {0}", id);
                   updated = true;
                }
            }

            if (updateData.containsKey("price")) {
                 Double price = parseDoubleFromMap(updateData, "price");
                 if (price == null || price < 0) {
                     sendErrorResponse(exchange, 400, "El campo 'price' debe ser un número no negativo.");
                     return;
                 }
                 // Usar Double.compare para manejar precisión flotante, permitir actualizar si antes era null
                 if (existingService.getPrice() == null || Double.compare(existingService.getPrice(), price) != 0) {
                     LOGGER.log(Level.FINE, "Actualizando precio para servicio ID {0} de {1} a {2}", new Object[]{id, existingService.getPrice(), price});
                     existingService.setPrice(price);
                     updated = true;
                 }
            }

            // 3.1. Validar y actualizar categoría (si se proporciona)
            Long newCategoryId = null;
            Category newCategory = null;
            if (updateData.containsKey("category_id")) {
                newCategoryId = parseLongFromMap(updateData, "category_id");
                if (newCategoryId == null) {
                    sendErrorResponse(exchange, 400, "El campo 'category_id' debe ser un ID numérico válido.");
                    return;
                }

                // Verificar si la categoría nueva existe
                newCategory = categoryDAO.findById(newCategoryId);
                if (newCategory == null) {
                    LOGGER.log(Level.WARNING, "Intento de actualizar servicio ID {0} con categoría inexistente ID: {1}", new Object[]{id, newCategoryId});
                    sendErrorResponse(exchange, 404, "La categoría especificada con ID " + newCategoryId + " no existe.");
                    return;
                }

                // Comprobar si la categoría realmente cambió
                Long currentCategoryId = (existingService.getCategory() != null) ? existingService.getCategory().getIdCategory() : null;
                if (currentCategoryId == null || !currentCategoryId.equals(newCategoryId)) {
                    LOGGER.log(Level.FINE, "Actualizando categoría para servicio ID {0} a ID {1} ('{2}')", new Object[]{id, newCategoryId, newCategory.getName()});
                    existingService.setCategory(newCategory);
                    updated = true;
                }
            } else {
                // Si no se proporciona category_id en la solicitud, usar la existente
                newCategory = existingService.getCategory();
                newCategoryId = originalCategoryId;
            }

             if (updateData.containsKey("enabled")) {
                 Object enabledObj = updateData.get("enabled");
                 Integer newEnabled = parseEnabledFlag(enabledObj);

                 if (newEnabled == null) {
                     sendErrorResponse(exchange, 400, "El campo 'enabled' debe ser booleano (true/false), número (1/0) o cadena ('true'/'false'/'1'/'0').");
                     return;
                 }

                 if(existingService.getEnabled() != newEnabled) {
                    LOGGER.log(Level.FINE, "Actualizando estado 'enabled' para servicio ID {0} de {1} a {2}", new Object[]{id, existingService.getEnabled(), newEnabled});
                    existingService.setEnabled(newEnabled);
                    updated = true;
                 }
             }

             // No permitir actualizar externalId directamente a través de este endpoint
             if (updateData.containsKey("externalId")) {
                  LOGGER.log(Level.WARNING, "Intento de actualizar 'externalId' para servicio ID {0} (ignorado).", id);
             }


            // 3.2 Validar unicidad de nombre *después* de potenciales cambios de nombre y categoría
            // Solo si el nombre o la categoría cambiaron
            String finalName = (newName != null) ? newName : originalName; // Usar el nombre potencialmente actualizado
            Long finalCategoryId = newCategoryId; // Usar la categoría potencialmente actualizada

            if (updated && (updateData.containsKey("name") || updateData.containsKey("category_id"))) {
                 LOGGER.log(Level.FINE, "Validando conflicto de nombre para ID {0}: Nombre='{1}', CatID={2}", new Object[]{id, finalName, finalCategoryId});
                 boolean nameConflict = insuranceServiceDAO.findAll().stream()
                     .filter(s -> !s.getIdInsuranceService().equals(id)) // Excluir el servicio actual
                     .filter(s -> { // Comparar categorías (manejar nulls)
                         Category otherCategory = s.getCategory();
                         Long otherCategoryId = (otherCategory != null) ? otherCategory.getIdCategory() : null;
                         if (finalCategoryId == null) {
                             return otherCategoryId == null; // Ambos sin categoría
                         } else {
                             return finalCategoryId.equals(otherCategoryId); // Comparar IDs si ambos tienen categoría
                         }
                     })
                     .anyMatch(s -> s.getName().equalsIgnoreCase(finalName)); // Comparar nombres (ignorando caso)

                 if (nameConflict) {
                     String categoryName = (newCategory != null) ? newCategory.getName() : "(sin categoría)";
                     LOGGER.log(Level.WARNING, "Conflicto de actualización: Ya existe otro servicio con el nombre '{0}' en la categoría '{1}' (ID:{2}).",
                                new Object[]{finalName, categoryName, finalCategoryId});
                     sendErrorResponse(exchange, 409, "Ya existe otro servicio con el nombre '" + finalName + "' en la categoría '" + categoryName + "'.");
                     return;
                 }
            }

            // 4. Actualizar la entidad en la BD solo si hubo cambios
            if (updated) {
                insuranceServiceDAO.update(existingService);
                LOGGER.log(Level.INFO, "Servicio ID: {0} actualizado exitosamente.", id);
                sendJsonResponse(exchange, 200, existingService); // Devolver el objeto actualizado
            } else {
                LOGGER.log(Level.INFO, "Solicitud PUT para servicio ID: {0} no resultó en cambios.", id);
                // Devolver 200 OK con el objeto sin cambios
                sendJsonResponse(exchange, 200, existingService);
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al leer/parsear JSON para actualizar servicio ID: " + id, e);
            sendErrorResponse(exchange, 400, "Error al leer o parsear el cuerpo de la solicitud JSON.");
        } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Error inesperado al actualizar el servicio ID: " + id, e);
            sendErrorResponse(exchange, 500, "Error interno del servidor al actualizar el servicio.");
        }
    }


     /**
      * Maneja la solicitud DELETE a {@code /api/insurance-services/{id}}.
      * Realiza una eliminación lógica marcando el servicio como deshabilitado (enabled = 0).
      * Si se implementara una eliminación física, se llamaría a {@code insuranceServiceDAO.delete(id)}.
      *
      * @param exchange El objeto {@link HttpExchange}.
      * @param id El ID del servicio a eliminar (lógicamente).
      * @throws IOException Si ocurre un error al enviar la respuesta.
      */
    private void handleDelete(HttpExchange exchange, Long id) throws IOException {
         LOGGER.log(Level.INFO, "Solicitud DELETE para servicio ID: {0}", id);
         try {
             InsuranceService service = insuranceServiceDAO.findById(id);
             if (service != null) {
                 // Realizar eliminación lógica (marcar como deshabilitado)
                 if (service.getEnabled() == 1) { // Solo actualizar si está habilitado
                     service.setEnabled(0);
                     insuranceServiceDAO.update(service); // Usar update para cambiar el estado
                     LOGGER.log(Level.INFO, "Servicio ID: {0} marcado como deshabilitado (eliminación lógica).", id);
                     sendJsonResponse(exchange, 200, Map.of("message", "Servicio ID " + id + " deshabilitado exitosamente."));
                 } else {
                     LOGGER.log(Level.INFO, "Servicio ID: {0} ya estaba deshabilitado.", id);
                     sendJsonResponse(exchange, 200, Map.of("message", "Servicio ID " + id + " ya estaba deshabilitado."));
                 }
                 // Si se quisiera eliminación física:
                 // insuranceServiceDAO.delete(id);
                 // LOGGER.log(Level.INFO, "Servicio ID: {0} eliminado físicamente.", id);
                 // sendJsonResponse(exchange, 204, null); // 204 No Content para DELETE exitoso
             } else {
                 LOGGER.log(Level.WARNING, "Intento de eliminar servicio inexistente ID: {0}", id);
                 sendErrorResponse(exchange, 404, "Servicio de seguro no encontrado con ID: " + id);
             }
         } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Error al deshabilitar (eliminar lógicamente) servicio ID: " + id, e);
             sendErrorResponse(exchange, 500, "Error interno del servidor al eliminar el servicio.");
         }
    }

    /**
     * Envía una respuesta JSON al cliente.
     * Serializa el objeto de datos proporcionado a JSON y lo escribe en el cuerpo de la respuesta
     * con el código de estado HTTP especificado y el tipo de contenido "application/json".
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP para la respuesta (e.g., 200, 201, 400).
     * @param data El objeto a serializar como JSON (puede ser una lista, mapa, entidad, etc.).
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        byte[] responseBytes;
        if (data != null) {
            String jsonResponse = objectMapper.writeValueAsString(data);
            responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        } else {
            responseBytes = new byte[0]; // Cuerpo vacío si data es null
        }
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length > 0 ? responseBytes.length : -1); // Usar -1 para cuerpo vacío
        if (responseBytes.length > 0) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
        LOGGER.log(Level.FINE, "Respuesta enviada: Código={0}, Tipo=JSON, Tamaño={1} bytes", new Object[]{statusCode, responseBytes.length});
    }

    /**
     * Envía una respuesta de error JSON al cliente.
     * Crea un objeto JSON estándar con un campo "error" que contiene el mensaje de error
     * y lo envía con el código de estado HTTP especificado.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @param statusCode El código de estado HTTP de error (e.g., 400, 404, 500).
     * @param errorMessage El mensaje de error a incluir en la respuesta JSON.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        Map<String, String> errorResponse = Collections.singletonMap("error", errorMessage);
        byte[] responseBytes = objectMapper.writeValueAsString(errorResponse).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
         LOGGER.log(Level.WARNING, "Respuesta de error enviada: Código={0}, Mensaje='{1}'", new Object[]{statusCode, errorMessage});
    }

    /**
     * Lee el cuerpo completo de una solicitud HTTP y lo devuelve como una cadena.
     *
     * @param exchange El objeto {@link HttpExchange}.
     * @return El cuerpo de la solicitud como una cadena UTF-8.
     * @throws IOException Si ocurre un error al leer el InputStream.
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = exchange.getRequestBody()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
        }
        String body = sb.toString();
        LOGGER.log(Level.FINEST, "Cuerpo de solicitud leído: {0}", body); // Loguear solo en FINEST por seguridad
        return body;
    }

    /**
     * Parsea la cadena de consulta (query string) de una URL en un mapa de clave-valor.
     * Maneja claves sin valor y decodifica los componentes si es necesario (aunque aquí no se aplica decodificación).
     *
     * @param query La cadena de consulta (ej: "category_id=5&sort=name"). Puede ser {@code null} o vacía.
     * @return Un {@link Map} que contiene los parámetros de consulta. Vacío si no hay parámetros.
     */
    private Map<String, String> parseQueryParams(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> params = new HashMap<>();
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2); // Divide en clave y valor (máximo 2 partes)
            if (pair.length > 0 && !pair[0].isEmpty()) {
                String key = pair[0];
                String value = (pair.length == 2) ? pair[1] : ""; // Valor vacío si no hay '=' o después del '='
                // Aquí se podría añadir URLDecoder.decode(value, StandardCharsets.UTF_8) si se esperan valores codificados
                params.put(key, value);
            }
        }
        LOGGER.log(Level.FINE, "Parámetros de consulta parseados: {0}", params);
        return params;
    }

    /**
     * Intenta parsear un valor de un mapa como un Long.
     * Útil para extraer IDs numéricos de datos JSON deserializados en un Map.
     * Maneja casos donde el valor es un Number (Integer, Long, etc.) o una String que representa un número.
     *
     * @param map El mapa del cual extraer el valor.
     * @param key La clave asociada al valor numérico.
     * @return El valor como {@link Long}, o {@code null} si la clave no existe, el valor es nulo,
     *         o no se puede parsear como Long.
     */
    private Long parseLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                 LOGGER.log(Level.WARNING, "No se pudo parsear Long desde String '" + value + "' para la clave '" + key + "'");
                return null; // No se pudo parsear
            }
        }
         LOGGER.log(Level.WARNING, "Tipo inesperado ('" + value.getClass().getName() + "') para Long en la clave '" + key + "'");
        return null; // Tipo inesperado
    }

    /**
     * Intenta parsear un valor de un mapa como un Double.
     * Maneja casos donde el valor es un Number (Integer, Long, Double, etc.) o una String que representa un número.
     *
     * @param map El mapa del cual extraer el valor.
     * @param key La clave asociada al valor numérico.
     * @return El valor como {@link Double}, o {@code null} si la clave no existe, el valor es nulo,
     *         o no se puede parsear como Double.
     */
    private Double parseDoubleFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                 LOGGER.log(Level.WARNING, "No se pudo parsear Double desde String '" + value + "' para la clave '" + key + "'");
                return null;
            }
        }
         LOGGER.log(Level.WARNING, "Tipo inesperado ('" + value.getClass().getName() + "') para Double en la clave '" + key + "'");
        return null;
    }

    /**
     * Intenta parsear un valor como un indicador booleano (representado por Integer 1 o 0).
     * Acepta Boolean (true/false), Number (1/0), o String ("true"/"false"/"1"/"0").
     *
     * @param value El objeto a parsear (puede ser Boolean, Number, String).
     * @return {@code 1} para verdadero, {@code 0} para falso, o {@code null} si el valor es nulo o no se puede interpretar.
     */
    private Integer parseEnabledFlag(Object value) {
        if (value == null) return null;

        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        } else if (value instanceof Number) {
            int intValue = ((Number) value).intValue();
            if (intValue == 1) return 1;
            if (intValue == 0) return 0;
        } else if (value instanceof String) {
            String strValue = ((String) value).trim().toLowerCase();
            if (strValue.equals("true") || strValue.equals("1")) return 1;
            if (strValue.equals("false") || strValue.equals("0")) return 0;
        }

        LOGGER.log(Level.WARNING, "Valor inválido para indicador booleano (enabled): '" + value + "' (Tipo: " + value.getClass().getName() + ")");
        return null; // Valor no reconocido
    }

    /**
     * Añade de forma segura un campo de un JsonNode a un Map<String, Object>.
     * Verifica si el campo existe en el JsonNode y no es nulo antes de añadirlo al mapa.
     * Útil para construir mapas a partir de JSON parcial o con campos potencialmente ausentes.
     *
     * @param map El mapa al que se añadirá el campo.
     * @param node El JsonNode del que se extraerá el campo.
     * @param jsonField El nombre del campo en el JsonNode.
     * @param mapKey La clave a usar en el mapa.
     */
    private void addSafeFieldToMap(Map<String, Object> map, JsonNode node, String jsonField, String mapKey) {
        if (node.has(jsonField) && !node.get(jsonField).isNull()) {
            // Extraer el valor apropiado basado en el tipo esperado o como texto genérico
            JsonNode fieldNode = node.get(jsonField);
            if (fieldNode.isTextual()) {
                map.put(mapKey, fieldNode.asText());
            } else if (fieldNode.isNumber()) {
                map.put(mapKey, fieldNode.numberValue());
            } else if (fieldNode.isBoolean()) {
                map.put(mapKey, fieldNode.asBoolean());
            } else {
                // Para otros tipos (arrays, objects), podrías necesitar manejo específico
                // o simplemente usar asText() como fallback
                map.put(mapKey, fieldNode.asText()); // O considera usar fieldNode directamente
            }
        }
    }
}
