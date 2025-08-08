package com.sources.app.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Cliente para realizar llamadas a servicios externos (hospital y farmacia)
 */
public class ExternalServiceClient {

    private static final String HOSPITAL_BASE_URL = "http://localhost:8000/api";
    private static final String PHARMACY_BASE_URL = "http://localhost:8082/api";
    private static final String INSURANCE_BASE_URL = "http://localhost:8080/api/pharmacy-insurance";
    private static final int TIMEOUT = 10000; // 10 segundos
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Realiza una petición GET a un servicio externo
     */
    public String get(String serviceType, String endpoint) throws IOException {
        String baseUrl = getBaseUrl(serviceType);
        URL url = new URL(baseUrl + endpoint);
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        
        return handleResponse(connection);
    }
    
    /**
     * Realiza una petición POST a un servicio externo
     */
    public String post(String serviceType, String endpoint, Object data) throws IOException {
        String baseUrl = getBaseUrl(serviceType);
        URL url = new URL(baseUrl + endpoint);
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setDoOutput(true);
        
        String jsonData = mapper.writeValueAsString(data);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        return handleResponse(connection);
    }
    
    /**
     * Realiza una petición PUT a un servicio externo
     */
    public String put(String serviceType, String endpoint, Object requestBody) throws IOException {
        String baseUrl = getBaseUrl(serviceType);
        URL url = new URL(baseUrl + endpoint);
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setDoOutput(true);
        
        // Convertir objeto a JSON si no es null
        if (requestBody != null) {
            String jsonBody = mapper.writeValueAsString(requestBody);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }
        
        return handleResponse(connection);
    }
    
    /**
     * Realiza una petición GET asíncrona
     */
    public CompletableFuture<String> getAsync(String serviceType, String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return get(serviceType, endpoint);
            } catch (IOException e) {
                throw new RuntimeException("Error en petición asíncrona GET", e);
            }
        }, executor);
    }
    
    /**
     * Realiza una petición POST asíncrona
     */
    public CompletableFuture<String> postAsync(String serviceType, String endpoint, Object requestBody) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return post(serviceType, endpoint, requestBody);
            } catch (IOException e) {
                throw new RuntimeException("Error en petición asíncrona POST", e);
            }
        }, executor);
    }
    
    /**
     * Realiza una petición PUT asíncrona
     */
    public CompletableFuture<String> putAsync(String serviceType, String endpoint, Object requestBody) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return put(serviceType, endpoint, requestBody);
            } catch (IOException e) {
                throw new RuntimeException("Error en petición asíncrona PUT", e);
            }
        }, executor);
    }
    
    /**
     * Obtiene la URL base según el tipo de servicio
     */
    private String getBaseUrl(String serviceType) {
        switch (serviceType.toUpperCase()) {
            case "HOSPITAL":
                return HOSPITAL_BASE_URL;
            case "PHARMACY":
                return PHARMACY_BASE_URL;
            case "INSURANCE":
                return INSURANCE_BASE_URL;
            default:
                throw new IllegalArgumentException("Tipo de servicio no válido: " + serviceType);
        }
    }
    
    /**
     * Maneja la respuesta HTTP
     */
    private String handleResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        
        try (BufferedReader br = new BufferedReader(
                 new InputStreamReader(responseCode >= 200 && responseCode < 300 ? 
                         connection.getInputStream() : connection.getErrorStream()))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
} 