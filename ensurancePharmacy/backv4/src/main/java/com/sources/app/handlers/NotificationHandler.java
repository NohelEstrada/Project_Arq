package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Manejador HTTP diseñado específicamente para enviar notificaciones por correo electrónico.
 * Expone un único endpoint para recibir solicitudes de envío de correo.
 * La configuración del servidor SMTP (host, puerto, autenticación) y las credenciales
 * del remitente se cargan desde un archivo de propiedades (`mail.properties`) ubicado en el classpath.
 *
 * <p>Endpoint manejado:</p>
 * <ul>
 *   <li>{@code POST /api/notifications/email}: Envía un correo electrónico. Requiere un cuerpo JSON
 *       con los campos "to" (destinatario), "subject" (asunto) y "body" (cuerpo del mensaje).</li>
 * </ul>
 */
public class NotificationHandler implements HttpHandler {
    /** ObjectMapper para la serialización/deserialización JSON. */
    private final ObjectMapper objectMapper;
    /** Ruta base para las solicitudes gestionadas por este manejador. */
    private static final String ENDPOINT = "/api/notifications/email";
    /** Logger para registrar eventos y errores de este manejador. */
    private static final Logger LOGGER = Logger.getLogger(NotificationHandler.class.getName());

    /** Propiedades de configuración de correo cargadas desde `mail.properties`. */
    private final Properties mailProperties;
    /** Dirección de correo electrónico del remitente, obtenida de las propiedades. */
    private final String senderEmail;
    /** Contraseña de la cuenta de correo del remitente, obtenida de las propiedades. */
    private final String senderPassword;

    /**
     * Constructor del manejador de notificaciones.
     * Inicializa el ObjectMapper y carga las propiedades de configuración de correo
     * (servidor SMTP, credenciales del remitente) desde el archivo `mail.properties`
     * usando el método {@link #loadMailProperties()}. Registra un error grave si faltan
     * las propiedades esenciales `mail.sender.email` o `mail.sender.password`.
     */
    public NotificationHandler() {
        this.objectMapper = new ObjectMapper();
        this.mailProperties = loadMailProperties();
        this.senderEmail = mailProperties.getProperty("mail.sender.email");
        this.senderPassword = mailProperties.getProperty("mail.sender.password");
        
        if (this.senderEmail == null || this.senderPassword == null) {
            LOGGER.severe("Faltan propiedades esenciales (mail.sender.email o mail.sender.password) en mail.properties");
            // Considerar lanzar una excepción aquí si es crítico
        }
        LOGGER.info("NotificationHandler inicializado."); // No mostrar email en logs públicos
    }

    /**
     * Carga las propiedades de configuración de correo electrónico desde el archivo `mail.properties`
     * que debe encontrarse en el classpath del proyecto.
     * Utiliza el ClassLoader para localizar el archivo.
     *
     * @return Un objeto {@link Properties} conteniendo las propiedades cargadas del archivo.
     * @throws RuntimeException Si el archivo `mail.properties` no se encuentra en el classpath
     *                          o si ocurre un error de {@link IOException} durante la carga.
     */
    private Properties loadMailProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("mail.properties")) {
            if (input == null) {
                LOGGER.severe("No se pudo encontrar el archivo mail.properties en el classpath.");
                throw new RuntimeException("No se pudo encontrar el archivo mail.properties.");
            }
            props.load(input);
            LOGGER.info("Propiedades de correo cargadas correctamente desde mail.properties");
        } catch (IOException e) {
            LOGGER.severe("Error crítico al cargar las propiedades de correo desde mail.properties: " + e.getMessage());
            throw new RuntimeException("Error al cargar las propiedades de correo.", e);
        }
        return props;
    }

    /**
     * Punto de entrada principal para manejar las solicitudes HTTP entrantes dirigidas al endpoint de notificaciones por email.
     * Configura las cabeceras CORS (permitiendo solo POST y OPTIONS), maneja solicitudes OPTIONS (preflight),
     * valida que la ruta coincida exactamente con {@link #ENDPOINT} y que el método sea POST.
     * Si la solicitud es válida, lee el cuerpo JSON, extrae el destinatario, asunto y cuerpo del mensaje,
     * valida que no estén vacíos, e invoca a {@link #sendEmail(String, String, String)} para realizar el envío.
     * Responde con 200 (OK) si el envío es exitoso, 400 si faltan datos o el JSON es inválido, 500 si ocurre
     * un error interno durante el envío, 404 si la ruta es incorrecta, o 405 si el método no es POST.
     *
     * @param exchange El objeto {@link HttpExchange} que encapsula la solicitud y la respuesta HTTP.
     * @throws IOException Si ocurre un error de entrada/salida (generalmente manejado internamente).
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración de CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS"); // Solo POST y OPTIONS
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Manejo de preflight OPTIONS
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Verificar que la ruta sea la correcta
        String path = exchange.getRequestURI().getPath();
        if (!path.equalsIgnoreCase(ENDPOINT)) {
             LOGGER.warning("Solicitud recibida para ruta incorrecta: " + path);
            exchange.sendResponseHeaders(404, -1); // Not Found
            return;
        }

        // Solo permitimos el método POST
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            try {
                // Leer el cuerpo de la petición (JSON)
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                @SuppressWarnings("unchecked") // Usamos Map por simplicidad, DTO sería más robusto
                Map<String, String> emailRequest = objectMapper.readValue(requestBody, Map.class);

                // Extraer datos del email
                String to = emailRequest.get("to");
                String subject = emailRequest.get("subject");
                String body = emailRequest.get("body");

                // Validar datos mínimos requeridos
                if (to == null || to.trim().isEmpty() || 
                    subject == null || subject.trim().isEmpty() || 
                    body == null || body.trim().isEmpty()) {
                    LOGGER.warning("Solicitud POST a /api/notifications/email con datos incompletos.");
                    sendErrorResponse(exchange, 400, "Faltan datos requeridos (to, subject, body).");
                    return;
                }

                // Enviar el email usando el método auxiliar
                boolean emailSent = sendEmail(to, subject, body);

                if (emailSent) {
                    // Email enviado con éxito
                    LOGGER.info("Email enviado exitosamente a: " + to);
                    Map<String, Object> response = Map.of(
                        "success", true,
                        "message", "Email enviado con éxito"
                    );
                    sendJsonResponse(exchange, 200, response);
                } else {
                    // Error interno al enviar el email (ya logueado en sendEmail)
                     LOGGER.severe("Fallo al enviar email a: " + to + " (ver logs anteriores para detalles).");
                    sendErrorResponse(exchange, 500, "Error interno al enviar el email.");
                }
            } catch (Exception e) {
                 LOGGER.severe("Error crítico al procesar la solicitud POST en NotificationHandler: " + e.getMessage());
                e.printStackTrace(); // Loguear stack trace para depuración
                sendErrorResponse(exchange, 500, "Error interno del servidor.");
            }
        } else {
            // Método no permitido
            LOGGER.warning("Método no permitido (" + exchange.getRequestMethod() + ") para " + ENDPOINT);
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    /**
     * Envía un correo electrónico utilizando la API Jakarta Mail y la configuración
     * (servidor SMTP, credenciales) cargada desde `mail.properties`.
     * Configura la sesión de correo con autenticación SMTP y TLS (basado en las propiedades).
     * Crea un {@link MimeMessage} con el remitente, destinatario(s), asunto y cuerpo.
     * Maneja posibles errores durante la configuración o el envío (e.g., credenciales inválidas,
     * problemas de conexión con el servidor SMTP, dirección de destinatario inválida) y los registra.
     *
     * @param to La dirección de correo electrónico del destinatario (o múltiples direcciones separadas por coma).
     * @param subject El asunto del correo electrónico.
     * @param body El cuerpo del correo electrónico (como texto plano).
     * @return {@code true} si {@link Transport#send(Message)} se completa sin lanzar excepciones, {@code false} si
     *         faltan credenciales en la configuración o si ocurre cualquier excepción durante la preparación o envío del correo.
     */
    private boolean sendEmail(String to, String subject, String body) {
        // Validar que las propiedades esenciales estén cargadas
        if (senderEmail == null || senderPassword == null) {
            LOGGER.severe("Intento de enviar email sin credenciales configuradas.");
            return false;
        }
        
        try {
            LOGGER.info("Intentando enviar email a: " + to + " con asunto: " + subject);
            
            // Configurar propiedades para el servidor SMTP desde las propiedades cargadas
            Properties props = new Properties();
            props.put("mail.smtp.auth", mailProperties.getProperty("mail.smtp.auth", "true"));
            props.put("mail.smtp.starttls.enable", mailProperties.getProperty("mail.smtp.starttls.enable", "true"));
            props.put("mail.smtp.host", mailProperties.getProperty("mail.smtp.host"));
            props.put("mail.smtp.port", mailProperties.getProperty("mail.smtp.port"));

            // Validar que host y port no sean nulos
            if (props.getProperty("mail.smtp.host") == null || props.getProperty("mail.smtp.port") == null) {
                LOGGER.severe("Faltan propiedades SMTP host o port en mail.properties.");
                return false;
            }
            
            // Crear sesión con autenticación
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    // Usa las credenciales cargadas en el constructor
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });
            
            // Habilitar debug de JavaMail si es necesario (útil en desarrollo)
            // session.setDebug(true);
            
            // Crear el mensaje MimeMessage
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            // Manejar múltiples destinatarios si están separados por coma
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false)); 
            message.setSubject(subject, "UTF-8"); // Especificar UTF-8 para el asunto
            // Considerar usar MimeMultipart para contenido HTML o mixto
            message.setText(body, "UTF-8"); // Especificar UTF-8 para el cuerpo
            message.setHeader("Content-Type", "text/plain; charset=UTF-8"); // Asegurar Content-Type
            
            // Enviar el mensaje
            Transport.send(message);
            
            LOGGER.info("Email enviado correctamente a: " + to);
            return true;
        } catch (AuthenticationFailedException e) {
            LOGGER.severe("Error de autenticación al enviar email desde " + senderEmail + ": " + e.getMessage());
            // No exponer detalles de contraseña en logs
            return false;
        } catch (MessagingException e) {
            LOGGER.severe("Error de MessagingException al enviar email a " + to + ": " + e.getMessage());
            // Podría ser dirección inválida, problema de conexión, etc.
             e.printStackTrace(); // Útil para ver la causa raíz
            return false;
        } catch (Exception e) { // Captura genérica para otros errores inesperados
            LOGGER.severe("Error inesperado al intentar enviar email a " + to + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envía una respuesta JSON al cliente.
     * Serializa el objeto de datos a JSON y lo escribe en el cuerpo de la respuesta con el código de estado adecuado.
     * Utilizado principalmente para respuestas de éxito.
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
     * @param statusCode El código de estado HTTP de error (e.g., 400, 500).
     * @param errorMessage El mensaje descriptivo del error.
     * @throws IOException Si ocurre un error al escribir la respuesta.
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        Map<String, Object> errorResponse = Map.of("success", false, "message", errorMessage);
        sendJsonResponse(exchange, statusCode, errorResponse);
    }
} 