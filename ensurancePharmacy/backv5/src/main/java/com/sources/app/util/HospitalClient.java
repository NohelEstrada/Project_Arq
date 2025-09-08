package com.sources.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Cliente para realizar llamadas a la API del Hospital
 */
public class HospitalClient {
    // Base URL configurable y sobreescribible en pruebas
    private static volatile String HOSPITAL_API_BASE_URL;
    private static final int TIMEOUT = 10000; // 10 segundos
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        String env = System.getenv("HOSPITAL_API_URL");
        HOSPITAL_API_BASE_URL = (env != null && !env.isBlank()) ? env : "http://localhost:8000";
    }
    // Método de soporte para pruebas para inyectar base URL con puertos efímeros
    public static void setBaseUrlForTests(String baseUrl) {
        HOSPITAL_API_BASE_URL = baseUrl;
    }
    
    /**
     * Realiza una petición GET a la API del hospital
     * 
     * @param endpoint Endpoint de la API (comenzando con /)
     * @return Respuesta en formato JSON
     * @throws IOException Si ocurre un error en la comunicación
     */
    public static String get(String endpoint) throws IOException {
        URI uri = URI.create(HOSPITAL_API_BASE_URL + endpoint);
        URL url = uri.toURL();
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        
        return handleResponse(connection);
    }
    
    /**
     * Realiza una petición POST a la API del hospital
     * 
     * @param endpoint Endpoint de la API (comenzando con /)
     * @param data Datos a enviar en el cuerpo de la petición
     * @return Respuesta en formato JSON
     * @throws IOException Si ocurre un error en la comunicación
     */
    public static String post(String endpoint, Object data) throws IOException {
        URI uri = URI.create(HOSPITAL_API_BASE_URL + endpoint);
        URL url = uri.toURL();
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setDoOutput(true);
        
        String jsonData = objectMapper.writeValueAsString(data);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        return handleResponse(connection);
    }
    
    /**
     * Realiza una petición PUT a la API del hospital
     * 
     * @param endpoint Endpoint de la API (comenzando con /)
     * @param data Datos a enviar en el cuerpo de la petición
     * @return Respuesta en formato JSON
     * @throws IOException Si ocurre un error en la comunicación
     */
    public static String put(String endpoint, Object data) throws IOException {
        URI uri = URI.create(HOSPITAL_API_BASE_URL + endpoint);
        URL url = uri.toURL();
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setDoOutput(true);
        
        String jsonData = objectMapper.writeValueAsString(data);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        return handleResponse(connection);
    }
    
    /**
     * Procesa la respuesta HTTP y la convierte a String
     */
    private static String handleResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        
        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader br = new BufferedReader(
                     new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            try (BufferedReader br = new BufferedReader(
                     new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder error = new StringBuilder();
                String errorLine;
                while ((errorLine = br.readLine()) != null) {
                    error.append(errorLine.trim());
                }
                return "{\"error\":true,\"message\":\"" + error.toString() + "\",\"statusCode\":" + responseCode + "}";
            }
        }
    }
}
 