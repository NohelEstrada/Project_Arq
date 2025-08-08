package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.PrescriptionDAO;
import com.sources.app.entities.Prescription;
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

public class PrescriptionHandler implements HttpHandler {

    private final PrescriptionDAO prescriptionDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api/prescription";

    public PrescriptionHandler(PrescriptionDAO prescriptionDAO) {
        this.prescriptionDAO = prescriptionDAO;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración CORS
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

        // Enrutamiento según método HTTP
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
                exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        // Si hay query, podemos buscar por "id" o por "user_id"
        if (query != null) {
            Map<String, String> params = parseQuery(query);
            if (params.containsKey("user_id")) {
                // Buscar recetas por el ID del usuario
                try {
                    Long userId = Long.parseLong(params.get("user_id"));
                    List<Prescription> list = prescriptionDAO.findByUserId(userId);
                    String jsonResponse = objectMapper.writeValueAsString(list);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try(OutputStream os = exchange.getResponseBody()){
                        os.write(responseBytes);
                    }
                } catch(NumberFormatException e){
                    exchange.sendResponseHeaders(400, -1);
                }
            } else if (params.containsKey("id")) {
                // Buscar una receta específica por ID
                try {
                    Long id = Long.parseLong(params.get("id"));
                    Prescription prescription = prescriptionDAO.findById(id);
                    if (prescription != null) {
                        String jsonResponse = objectMapper.writeValueAsString(prescription);
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                        exchange.sendResponseHeaders(200, responseBytes.length);
                        try(OutputStream os = exchange.getResponseBody()){
                            os.write(responseBytes);
                        }
                    } else {
                        exchange.sendResponseHeaders(404, -1);
                    }
                } catch(NumberFormatException e){
                    exchange.sendResponseHeaders(400, -1);
                }
            } else {
                // Si no se pasan parámetros específicos, se retornan todas las recetas
                List<Prescription> list = prescriptionDAO.findAll();
                String jsonResponse = objectMapper.writeValueAsString(list);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                try(OutputStream os = exchange.getResponseBody()){
                    os.write(responseBytes);
                }
            }
        } else {
            // Sin query, retorna todas las recetas
            List<Prescription> list = prescriptionDAO.findAll();
            String jsonResponse = objectMapper.writeValueAsString(list);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try(OutputStream os = exchange.getResponseBody()){
                os.write(responseBytes);
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try{
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Prescription prescription = objectMapper.readValue(requestBody, Prescription.class);
            // Se asume que el JSON incluye los objetos anidados para hospital, user, medicine y pharmacy.
            // Extraemos los IDs correspondientes
            Long idHospital = prescription.getHospital().getIdHospital();
            Long idUser = prescription.getUser().getIdUser();
            Long idMedicine = prescription.getMedicine().getIdMedicine();
            Long idPharmacy = prescription.getPharmacy().getIdPharmacy();

            Prescription created = prescriptionDAO.create(
                    idHospital,
                    idUser,
                    idMedicine,
                    idPharmacy,
                    prescription.getPrescriptionDate(),
                    prescription.getTotal(),
                    prescription.getCopay(),
                    prescription.getPrescriptionComment(),
                    prescription.getSecured(),
                    prescription.getAuth()
            );
            if (created != null) {
                String jsonResponse = objectMapper.writeValueAsString(created);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(201, responseBytes.length);
                try(OutputStream os = exchange.getResponseBody()){
                    os.write(responseBytes);
                }
            } else {
                exchange.sendResponseHeaders(500, -1);
            }
        } catch(Exception e){
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Prescription prescription = objectMapper.readValue(requestBody, Prescription.class);
        Prescription updated = prescriptionDAO.update(prescription);
        if (updated != null) {
            String jsonResponse = objectMapper.writeValueAsString(updated);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try(OutputStream os = exchange.getResponseBody()){
                os.write(responseBytes);
            }
        } else {
            exchange.sendResponseHeaders(500, -1);
        }
    }

    private Map<String, String> parseQuery(String query) {
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(
                        kv -> kv[0],
                        kv -> kv.length > 1 ? kv[1] : ""
                ));
    }
}
