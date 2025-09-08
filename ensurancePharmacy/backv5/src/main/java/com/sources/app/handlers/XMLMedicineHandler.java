package com.sources.app.handlers;

import com.sources.app.dao.MedicineDAO;
import com.sources.app.entities.Medicine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manejador HTTP para obtener una lista de medicamentos en formato XML.
 * Responde a solicitudes GET en el endpoint "/api2/medicines-xml".
 */
public class XMLMedicineHandler implements HttpHandler {
    private final MedicineDAO medicineDAO;
    private static final String ENDPOINT = "/api2/medicines-xml";
    private static final Logger LOGGER = Logger.getLogger(XMLMedicineHandler.class.getName());

    /**
     * Constructor para XMLMedicineHandler.
     *
     * @param medicineDAO El DAO para acceder a los datos de los medicamentos.
     */
    public XMLMedicineHandler(MedicineDAO medicineDAO) {
        this.medicineDAO = medicineDAO;
    }

    /**
     * Maneja las solicitudes HTTP entrantes.
     * Configura las cabeceras CORS y delega el manejo según el método HTTP (solo GET soportado).
     *
     * @param exchange El objeto HttpExchange que representa la solicitud y respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (!path.startsWith(ENDPOINT)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        try {
            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing XMLMedicineHandler request", e);
            exchange.sendResponseHeaders(500, -1);
        }
    }

    /**
     * Maneja las solicitudes GET.
     * Obtiene todos los medicamentos de la base de datos usando MedicineDAO,
     * los convierte a formato XML y envía la respuesta XML al cliente.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida al enviar la respuesta.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        // Get all medicines from the database
        List<Medicine> medicines = medicineDAO.getAll();
        
        // Convert the list of medicines to XML format
        String xmlResponse = convertToXml(medicines);
        
        // Set response headers
        exchange.getResponseHeaders().set("Content-Type", "application/xml");
        byte[] responseBytes = xmlResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        
        // Write the response
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Convierte una lista de objetos Medicine a una cadena de texto en formato XML.
     *
     * @param medicines La lista de medicamentos a convertir.
     * @return Una cadena String que representa la lista de medicamentos en formato XML.
     */
    private String convertToXml(List<Medicine> medicines) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<medicines>\n");
        
        for (Medicine medicine : medicines) {
            xml.append("  <medicine>\n");
            xml.append("    <idMedicine>").append(medicine.getIdMedicine()).append("</idMedicine>\n");
            xml.append("    <name>").append(escapeXml(medicine.getName())).append("</name>\n");
            xml.append("    <activeMedicament>").append(escapeXml(medicine.getActiveMedicament())).append("</activeMedicament>\n");
            xml.append("    <description>").append(escapeXml(medicine.getDescription())).append("</description>\n");
            xml.append("    <image>").append(escapeXml(medicine.getImage())).append("</image>\n");
            xml.append("    <concentration>").append(escapeXml(medicine.getConcentration())).append("</concentration>\n");
            xml.append("    <presentacion>").append(medicine.getPresentacion()).append("</presentacion>\n");
            xml.append("    <stock>").append(medicine.getStock()).append("</stock>\n");
            xml.append("    <brand>").append(escapeXml(medicine.getBrand())).append("</brand>\n");
            xml.append("    <prescription>").append(medicine.getPrescription()).append("</prescription>\n");
            xml.append("    <price>").append(medicine.getPrice()).append("</price>\n");
            xml.append("    <soldUnits>").append(medicine.getSoldUnits()).append("</soldUnits>\n");
            xml.append("  </medicine>\n");
        }
        
        xml.append("</medicines>");
        return xml.toString();
    }
    
    /**
     * Escapa caracteres especiales XML en una cadena de texto.
     * Reemplaza &, <, >, ", ' con sus entidades XML correspondientes.
     *
     * @param input La cadena de texto a escapar.
     * @return La cadena de texto con los caracteres especiales escapados, o una cadena vacía si la entrada es nula.
     */
    private String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
} 