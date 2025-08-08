package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.PrescriptionDAO;
import com.sources.app.dao.BillDAO;
import com.sources.app.entities.Prescription;
import com.sources.app.entities.Bill;
import com.sources.app.util.ExternalServiceClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

/**
 * Manejador HTTP para la integración con el sistema de seguros.
 * Gestiona la validación de recetas y la verificación de cobertura
 * interactuando con un servicio externo de seguros.
 */
public class InsuranceIntegrationHandler implements HttpHandler {
    private final PrescriptionDAO prescriptionDAO;
    private final BillDAO billDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/insurance";
    private static final String INSURANCE_API_BASE_URL = "http://localhost:8080/api/pharmacy-insurance"; // URL base del servicio externo de seguros
    private static final ExternalServiceClient externalServiceClient = new ExternalServiceClient(); // Cliente para llamadas HTTP externas

    /**
     * Constructor para InsuranceIntegrationHandler.
     *
     * @param prescriptionDAO DAO para acceder a los datos de las prescripciones.
     * @param billDAO DAO para acceder a los datos de las facturas.
     */
    public InsuranceIntegrationHandler(PrescriptionDAO prescriptionDAO, BillDAO billDAO) {
        this.prescriptionDAO = prescriptionDAO;
        this.billDAO = billDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes.
     * Dirige las solicitudes POST a los endpoints /validate-prescription y /check-coverage
     * a sus respectivos métodos de manejo. Responde con 405 Method Not Allowed para otros métodos
     * y 404 Not Found para otras rutas.
     *
     * @param exchange El objeto HttpExchange que representa la solicitud y respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals(ENDPOINT + "/validate-prescription")) {
            if (method.equals("POST")) {
                handleValidatePrescription(exchange);
            } else {
                sendMethodNotAllowed(exchange);
            }
        } else if (path.equals(ENDPOINT + "/check-coverage")) {
            if (method.equals("POST")) {
                handleCheckCoverage(exchange);
            } else {
                sendMethodNotAllowed(exchange);
            }
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    /**
     * Maneja la solicitud POST para validar una prescripción con el seguro.
     * Extrae el ID de la prescripción y el código de aprobación del cuerpo de la solicitud.
     * Busca la prescripción, verifica si ya ha sido facturada, calcula el total,
     * llama al servicio externo del seguro para validar y, si es exitoso,
     * crea una factura en la base de datos.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida o durante la comunicación con el seguro.
     */
    private void handleValidatePrescription(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> data = objectMapper.readValue(requestBody, Map.class);
        
        // Validar datos requeridos
        if (!data.containsKey("prescriptionId") || !data.containsKey("approvalCode")) {
            sendBadRequest(exchange, "Campos requeridos: prescriptionId, approvalCode");
            return;
        }
        
        Long prescriptionId = Long.valueOf(data.get("prescriptionId").toString());
        String approvalCode = (String) data.get("approvalCode");
        
        // Buscar la receta
        Prescription prescription = prescriptionDAO.getById(prescriptionId);
        
        if (prescription == null) {
            sendNotFound(exchange, "Receta no encontrada");
            return;
        }
        
        // Si ya existe una factura para esta receta, retornar error
        Bill existingBill = billDAO.getByPrescriptionId(prescriptionId);
        if (existingBill != null) {
            sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of(
                "success", false,
                "message", "Esta receta ya fue procesada y facturada"
            )));
            return;
        }
        
        // Calcular el total de la receta
        double totalAmount = prescription.calculateTotal();
        
        // Datos para enviar al seguro
        Map<String, Object> insuranceData = new HashMap<>();
        insuranceData.put("approvalCode", approvalCode);
        insuranceData.put("prescriptionId", prescriptionId);
        insuranceData.put("totalAmount", totalAmount);
        
        try {
            // Llamar al servicio del seguro
            String serviceType = "INSURANCE";
            String endpoint = "/validate-prescription";
            String jsonResponse = externalServiceClient.post(serviceType, endpoint, insuranceData);
            
            // Parsear la respuesta
            Map<String, Object> insuranceResponse = objectMapper.readValue(jsonResponse, Map.class);
            
            if (Boolean.TRUE.equals(insuranceResponse.get("success"))) {
                // Crear la factura
                Bill bill = new Bill();
                bill.setPrescription(prescription);
                bill.setTotalAmount(totalAmount);
                bill.setCoveredAmount((Double) insuranceResponse.get("coveredAmount"));
                bill.setPatientAmount((Double) insuranceResponse.get("patientAmount"));
                bill.setInsuranceApprovalCode((String) insuranceResponse.get("approvalCode"));
                bill.setCreatedAt(new Date());
                bill.setStatus("PENDING");
                
                // Asignando los valores requeridos por el método create de billDAO
                Double taxes = totalAmount * 0.07; // Ejemplo: 7% de impuestos
                Double subtotal = totalAmount - taxes;
                Double copay = bill.getPatientAmount();
                String totalStr = String.valueOf(totalAmount);
                
                Bill savedBill = billDAO.create(prescription, taxes, subtotal, copay, totalStr);
                
                if (savedBill != null) {
                    Map<String, Object> response = new HashMap<>(insuranceResponse);
                    response.put("billId", savedBill.getId());
                    sendResponse(exchange, 200, objectMapper.writeValueAsString(response));
                } else {
                    sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of(
                        "success", false,
                        "message", "Error al guardar la factura"
                    )));
                }
            } else {
                // Enviar la respuesta de error del seguro
                sendResponse(exchange, 400, jsonResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of(
                "success", false,
                "message", "Error al comunicarse con el seguro: " + e.getMessage()
            )));
        }
    }
    
    /**
     * Maneja la solicitud POST para verificar la cobertura de una prescripción con el seguro.
     * Extrae el ID de la prescripción del cuerpo de la solicitud y llama al servicio externo
     * del seguro para obtener la información de cobertura.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida o durante la comunicación con el seguro.
     */
    private void handleCheckCoverage(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> data = objectMapper.readValue(requestBody, Map.class);
        
        // Validar datos requeridos
        if (!data.containsKey("prescriptionId")) {
            sendBadRequest(exchange, "Campo requerido: prescriptionId");
            return;
        }
        
        Long prescriptionId = Long.valueOf(data.get("prescriptionId").toString());
        
        try {
            // Llamar al servicio del seguro
            String serviceType = "INSURANCE";
            String endpoint = "/check-coverage/" + prescriptionId;
            String jsonResponse = externalServiceClient.get(serviceType, endpoint);
            
            // Enviar la respuesta del seguro
            sendResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, objectMapper.writeValueAsString(Map.of(
                "success", false,
                "message", "Error al comunicarse con el seguro: " + e.getMessage()
            )));
        }
    }
    
    /**
     * Envía una respuesta HTTP 405 Method Not Allowed.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, -1);
    }
    
    /**
     * Envía una respuesta HTTP 400 Bad Request con un mensaje de error.
     *
     * @param exchange El objeto HttpExchange.
     * @param message El mensaje de error a incluir en la respuesta JSON.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 400, objectMapper.writeValueAsString(Map.of(
            "success", false,
            "message", message
        )));
    }
    
    /**
     * Envía una respuesta HTTP 404 Not Found con un mensaje de error.
     *
     * @param exchange El objeto HttpExchange.
     * @param message El mensaje de error a incluir en la respuesta JSON.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 404, objectMapper.writeValueAsString(Map.of(
            "success", false,
            "message", message
        )));
    }
    
    /**
     * Envía una respuesta HTTP con el código de estado y cuerpo de respuesta especificados.
     * Establece la cabecera Content-Type a application/json.
     *
     * @param exchange El objeto HttpExchange.
     * @param statusCode El código de estado HTTP para la respuesta.
     * @param response El cuerpo de la respuesta como una cadena String (se asume JSON).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
} 