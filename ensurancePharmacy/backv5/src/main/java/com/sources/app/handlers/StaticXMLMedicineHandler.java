package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Medicine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Manejador HTTP para servir una lista estática de medicamentos en formato XML.
 * Los datos de los medicamentos están codificados directamente en la clase como una cadena JSON.
 * Responde a solicitudes GET en el endpoint "/api2/static-medicines-xml".
 */
public class StaticXMLMedicineHandler implements HttpHandler {
    private static final String ENDPOINT = "/api2/static-medicines-xml";
    private final List<Medicine> medicines;
    private final ObjectMapper objectMapper;

    /**
     * Constructor para StaticXMLMedicineHandler.
     * Inicializa el ObjectMapper y parsea la cadena JSON estática para crear la lista de medicamentos.
     * Lanza una RuntimeException si ocurre un error durante el parseo del JSON.
     */
    public StaticXMLMedicineHandler() {
        this.objectMapper = new ObjectMapper();
        
        // Parse the static JSON data provided by the user
        String jsonData = "[{\"idMedicine\":1,\"name\":\"Paracetamol\",\"activeMedicament\":\"Paracetamol\",\"description\":\"Para dolor de cabeza\",\"image\":\"https://i.guim.co.uk/img/media/20491572b80293361199ca2fc95e49dfd85e1f42/0_236_5157_3094/master/5157.jpg?width=700&quality=85&auto=format&fit=max&s=75467b37e566112a23646eeb8cbc9a26\",\"concentration\":\"500 MG\",\"presentacion\":30.0,\"stock\":85,\"brand\":\"MK\",\"prescription\":false,\"price\":60.0,\"soldUnits\":0},{\"idMedicine\":7,\"name\":\"Paracetamol\",\"activeMedicament\":\"Acetaminophen\",\"description\":\"Pain reliever\",\"image\":\"http://image-url.com/med.png\",\"concentration\":\"500mg\",\"presentacion\":30.0,\"stock\":97,\"brand\":\"MarcaX\",\"prescription\":false,\"price\":3.5,\"soldUnits\":0},{\"idMedicine\":8,\"name\":\"Prueba\",\"activeMedicament\":\"Acetaminophen\",\"description\":\"Pain reliever\",\"image\":\"http://image-url.com/med.png\",\"concentration\":\"500mg\",\"presentacion\":30.0,\"stock\":96,\"brand\":\"MarcaX\",\"prescription\":false,\"price\":3.5,\"soldUnits\":0},{\"idMedicine\":9,\"name\":\"Prueba\",\"activeMedicament\":\"qej\",\"description\":\"osdvos\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":200,\"brand\":\"condos\",\"prescription\":true,\"price\":900.0,\"soldUnits\":500},{\"idMedicine\":10,\"name\":\"Prueba\",\"activeMedicament\":\"qej\",\"description\":\"osdvos\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":200,\"brand\":\"condos\",\"prescription\":true,\"price\":900.0,\"soldUnits\":500},{\"idMedicine\":11,\"name\":\"Prueba\",\"activeMedicament\":\"qej\",\"description\":\"osdvos\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":200,\"brand\":\"condos\",\"prescription\":true,\"price\":900.0,\"soldUnits\":500},{\"idMedicine\":12,\"name\":\"Prueba\",\"activeMedicament\":\"qej\",\"description\":\"osdvos\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":200,\"brand\":\"condos\",\"prescription\":true,\"price\":900.0,\"soldUnits\":500},{\"idMedicine\":13,\"name\":\"Prueba\",\"activeMedicament\":\"qej\",\"description\":\"osdvos\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":200,\"brand\":\"condos\",\"prescription\":true,\"price\":900.0,\"soldUnits\":500},{\"idMedicine\":14,\"name\":\"Prueba\",\"activeMedicament\":\"qej\",\"description\":\"osdvos\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":200,\"brand\":\"condos\",\"prescription\":true,\"price\":900.0,\"soldUnits\":500},{\"idMedicine\":15,\"name\":\"Prueba\",\"activeMedicament\":\"qej\",\"description\":\"osdvos\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":200,\"brand\":\"condos\",\"prescription\":true,\"price\":900.0,\"soldUnits\":500},{\"idMedicine\":16,\"name\":\"Prueba\",\"activeMedicament\":\"qej\",\"description\":\"osdvos\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":200,\"brand\":\"condos\",\"prescription\":true,\"price\":900.0,\"soldUnits\":500},{\"idMedicine\":17,\"name\":\"Prueba\",\"activeMedicament\":\"qej\",\"description\":\"osdvos\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":200,\"brand\":\"condos\",\"prescription\":true,\"price\":900.0,\"soldUnits\":500},{\"idMedicine\":18,\"name\":\"Prueba\",\"activeMedicament\":\"qej\",\"description\":\"osdvos\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":200,\"brand\":\"condos\",\"prescription\":true,\"price\":900.0,\"soldUnits\":500},{\"idMedicine\":19,\"name\":\"Prueba\",\"activeMedicament\":\"qej\",\"description\":\"osdvos\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":200,\"brand\":\"condos\",\"prescription\":true,\"price\":900.0,\"soldUnits\":500},{\"idMedicine\":20,\"name\":\"Prueba\",\"activeMedicament\":\"qej\",\"description\":\"osdvos\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":200,\"brand\":\"condos\",\"prescription\":true,\"price\":900.0,\"soldUnits\":500},{\"idMedicine\":21,\"name\":\"Prueba\",\"activeMedicament\":\"PRUEBA\",\"description\":\"PRUEBA\",\"image\":\".\",\"concentration\":\"50\",\"presentacion\":30.0,\"stock\":900,\"brand\":\"PRUEBA\",\"prescription\":false,\"price\":199.0,\"soldUnits\":1},{\"idMedicine\":22,\"name\":\"Magic\",\"activeMedicament\":\"Aspirina\",\"description\":\"Magic magic cura todo\",\"image\":\".\",\"concentration\":\"99\",\"presentacion\":30.0,\"stock\":9950,\"brand\":\"Ravenclaw\",\"prescription\":false,\"price\":10000.0,\"soldUnits\":2},{\"idMedicine\":92,\"name\":null,\"activeMedicament\":null,\"description\":null,\"image\":null,\"concentration\":null,\"presentacion\":null,\"stock\":null,\"brand\":null,\"prescription\":null,\"price\":null,\"soldUnits\":null},{\"idMedicine\":93,\"name\":\"Test Medicine\",\"activeMedicament\":null,\"description\":null,\"image\":null,\"concentration\":null,\"presentacion\":null,\"stock\":null,\"brand\":null,\"prescription\":null,\"price\":null,\"soldUnits\":null},{\"idMedicine\":94,\"name\":null,\"activeMedicament\":null,\"description\":null,\"image\":null,\"concentration\":null,\"presentacion\":null,\"stock\":null,\"brand\":null,\"prescription\":null,\"price\":null,\"soldUnits\":null},{\"idMedicine\":95,\"name\":\"Test Medicine\",\"activeMedicament\":\"Active Ingredient\",\"description\":\"Test description\",\"image\":\"image.png\",\"concentration\":\"500mg\",\"presentacion\":10.0,\"stock\":100,\"brand\":\"Test Brand\",\"prescription\":false,\"price\":9.99,\"soldUnits\":0},{\"idMedicine\":96,\"name\":\"Test Medicine\",\"activeMedicament\":\"ActiveMed\",\"description\":\"Test Desc\",\"image\":\"image.png\",\"concentration\":\"500mg\",\"presentacion\":10.0,\"stock\":97,\"brand\":\"TestBrand\",\"prescription\":false,\"price\":9.99,\"soldUnits\":0},{\"idMedicine\":97,\"name\":\"Test Medicine\",\"activeMedicament\":\"Active Ingredient\",\"description\":\"Test description\",\"image\":\"image.png\",\"concentration\":\"500mg\",\"presentacion\":10.0,\"stock\":100,\"brand\":\"Test Brand\",\"prescription\":false,\"price\":9.99,\"soldUnits\":0},{\"idMedicine\":41,\"name\":\"Randyflux\",\"activeMedicament\":\"Fres\",\"description\":\"123\",\"image\":\".\",\"concentration\":\"99\",\"presentacion\":30.0,\"stock\":10000,\"brand\":\"GSI\",\"prescription\":true,\"price\":9898.0,\"soldUnits\":500},{\"idMedicine\":87,\"name\":null,\"activeMedicament\":null,\"description\":null,\"image\":null,\"concentration\":null,\"presentacion\":null,\"stock\":null,\"brand\":null,\"prescription\":null,\"price\":null,\"soldUnits\":null},{\"idMedicine\":88,\"name\":\"Test Medicine\",\"activeMedicament\":null,\"description\":null,\"image\":null,\"concentration\":null,\"presentacion\":null,\"stock\":null,\"brand\":null,\"prescription\":null,\"price\":null,\"soldUnits\":null},{\"idMedicine\":89,\"name\":null,\"activeMedicament\":null,\"description\":null,\"image\":null,\"concentration\":null,\"presentacion\":null,\"stock\":null,\"brand\":null,\"prescription\":null,\"price\":null,\"soldUnits\":null},{\"idMedicine\":90,\"name\":\"Test Medicine\",\"activeMedicament\":\"Active Ingredient\",\"description\":\"Test description\",\"image\":\"image.png\",\"concentration\":\"500mg\",\"presentacion\":10.0,\"stock\":100,\"brand\":\"Test Brand\",\"prescription\":false,\"price\":9.99,\"soldUnits\":0},{\"idMedicine\":91,\"name\":null,\"activeMedicament\":null,\"description\":null,\"image\":null,\"concentration\":null,\"presentacion\":null,\"stock\":null,\"brand\":null,\"prescription\":null,\"price\":null,\"soldUnits\":null},{\"idMedicine\":61,\"name\":\"Randy\",\"activeMedicament\":\"Prueba2\",\"description\":\"Dolor de pestaña\",\"image\":null,\"concentration\":\"100mg\",\"presentacion\":1.0,\"stock\":9000,\"brand\":\"MarcaY\",\"prescription\":false,\"price\":99999.0,\"soldUnits\":192033},{\"idMedicine\":81,\"name\":null,\"activeMedicament\":null,\"description\":null,\"image\":null,\"concentration\":null,\"presentacion\":null,\"stock\":null,\"brand\":null,\"prescription\":null,\"price\":null,\"soldUnits\":null},{\"idMedicine\":82,\"name\":null,\"activeMedicament\":null,\"description\":null,\"image\":null,\"concentration\":null,\"presentacion\":null,\"stock\":null,\"brand\":null,\"prescription\":null,\"price\":null,\"soldUnits\":null},{\"idMedicine\":83,\"name\":\"Test Medicine\",\"activeMedicament\":null,\"description\":null,\"image\":null,\"concentration\":null,\"presentacion\":null,\"stock\":null,\"brand\":null,\"prescription\":null,\"price\":null,\"soldUnits\":null},{\"idMedicine\":84,\"name\":null,\"activeMedicament\":null,\"description\":null,\"image\":null,\"concentration\":null,\"presentacion\":null,\"stock\":null,\"brand\":null,\"prescription\":null,\"price\":null,\"soldUnits\":null},{\"idMedicine\":85,\"name\":\"Test Medicine\",\"activeMedicament\":\"Active Ingredient\",\"description\":\"Test description\",\"image\":\"image.png\",\"concentration\":\"500mg\",\"presentacion\":10.0,\"stock\":100,\"brand\":\"Test Brand\",\"prescription\":false,\"price\":9.99,\"soldUnits\":0},{\"idMedicine\":86,\"name\":null,\"activeMedicament\":null,\"description\":null,\"image\":null,\"concentration\":null,\"presentacion\":null,\"stock\":null,\"brand\":null,\"prescription\":null,\"price\":null,\"soldUnits\":null}]";
        
        try {
            this.medicines = Arrays.asList(objectMapper.readValue(jsonData, Medicine[].class));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing static medicine data", e);
        }
    }

    /**
     * Maneja las solicitudes HTTP entrantes.
     * Configura las cabeceras CORS y delega el manejo a handleGet si el método es GET.
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
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }
    }

    /**
     * Maneja las solicitudes GET.
     * Convierte la lista estática de medicamentos a formato XML y la envía como respuesta.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida al enviar la respuesta.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        // Convert the static list of medicines to XML format
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