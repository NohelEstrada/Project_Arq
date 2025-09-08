package com.sources.app.handlers;

import com.sources.app.dao.ExternalMedicineDAO;
import com.sources.app.entities.Medicine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manejador HTTP para obtener medicamentos desde una fuente externa.
 * Responde a solicitudes GET en el endpoint "/api2/external_medicines",
 * utilizando ExternalMedicineDAO para recuperar los datos y devolviéndolos como JSON.
 */
public class ExternalMedicineHandler implements HttpHandler {
    private ExternalMedicineDAO externalMedicineDAO;
    private ObjectMapper objectMapper;
    private static final Logger LOGGER = Logger.getLogger(ExternalMedicineHandler.class.getName());

    /**
     * Constructor para ExternalMedicineHandler.
     *
     * @param externalMedicineDAO El DAO para acceder a los datos de medicamentos externos.
     */
    public ExternalMedicineHandler(ExternalMedicineDAO externalMedicineDAO) {
        this.externalMedicineDAO = externalMedicineDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes.
     * Configura las cabeceras CORS.
     * Delega las solicitudes GET a handleGet. Responde con 405 Method Not Allowed
     * para otros métodos.
     *
     * @param exchange El objeto HttpExchange que representa la solicitud y respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        try {
            if (exchange.getRequestMethod().equals("GET")) {
                handleGet(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled exception in ExternalMedicineHandler", e);
            exchange.sendResponseHeaders(500, -1);
        }
    }
    
    /**
     * Maneja las solicitudes GET.
     * Obtiene la lista de todos los medicamentos externos usando ExternalMedicineDAO,
     * los convierte a formato JSON y los envía como respuesta.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida al enviar la respuesta.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        List<Medicine> medicines = externalMedicineDAO.getAll();
        String jsonResponse = objectMapper.writeValueAsString(medicines);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
