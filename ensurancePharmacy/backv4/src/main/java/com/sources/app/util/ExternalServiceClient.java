package com.sources.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cliente para comunicación con servicios externos (hospitales y farmacias)
 */
public class ExternalServiceClient {
    
    private static final String HOSPITAL_BASE_URL = "http://localhost:5051/api";
    private static final String PHARMACY_BASE_URL = "http://localhost:8080/api";
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Constructor por defecto para ExternalServiceClient.
     */
    public ExternalServiceClient() {}

    /**
     * Realiza una petición GET síncrona a un endpoint específico.
     *
     * @param endpoint El path específico del recurso a solicitar (ej. "/citas").
     * @param isHospital Booleano que indica si la petición es para el servicio de hospital (true) o farmacia (false).
     * @return La respuesta del servidor como una cadena de texto (generalmente JSON).
     * @throws Exception Si ocurre un error durante la conexión o si el servidor responde con un código de error.
     */
    public String get(String endpoint, boolean isHospital) throws Exception {
        String baseUrl = isHospital ? HOSPITAL_BASE_URL : PHARMACY_BASE_URL;
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            return response.toString();
        } else {
            throw new Exception("Error en la petición GET: " + responseCode);
        }
    }
    
    /**
     * Realiza una petición POST síncrona a un endpoint específico, enviando datos en el cuerpo.
     *
     * @param endpoint El path específico del recurso donde se enviarán los datos.
     * @param requestBody El objeto a ser serializado como JSON y enviado en el cuerpo de la petición.
     * @param isHospital Booleano que indica si la petición es para el servicio de hospital (true) o farmacia (false).
     * @return La respuesta del servidor como una cadena de texto (generalmente JSON).
     * @throws Exception Si ocurre un error durante la conexión, serialización, o si el servidor responde con un código de error.
     */
    public String post(String endpoint, Object requestBody, boolean isHospital) throws Exception {
        String baseUrl = isHospital ? HOSPITAL_BASE_URL : PHARMACY_BASE_URL;
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        String jsonInputString = objectMapper.writeValueAsString(requestBody);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            return response.toString();
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            throw new Exception("Error en la petición POST: " + responseCode + ", Respuesta: " + response.toString());
        }
    }
    
    /**
     * Realiza una petición PUT síncrona a un endpoint específico, enviando datos en el cuerpo para actualizar un recurso.
     *
     * @param endpoint El path específico del recurso a actualizar.
     * @param requestBody El objeto con los datos actualizados a ser serializado como JSON.
     * @param isHospital Booleano que indica si la petición es para el servicio de hospital (true) o farmacia (false).
     * @return La respuesta del servidor como una cadena de texto (generalmente JSON).
     * @throws Exception Si ocurre un error durante la conexión, serialización, o si el servidor responde con un código de error.
     */
    public String put(String endpoint, Object requestBody, boolean isHospital) throws Exception {
        String baseUrl = isHospital ? HOSPITAL_BASE_URL : PHARMACY_BASE_URL;
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        String jsonInputString = objectMapper.writeValueAsString(requestBody);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            return response.toString();
        } else {
            throw new Exception("Error en la petición PUT: " + responseCode);
        }
    }
    
    /**
     * Realiza una petición GET asíncrona a un endpoint específico.
     *
     * @param endpoint El path específico del recurso a solicitar.
     * @param isHospital Booleano que indica si la petición es para el servicio de hospital (true) o farmacia (false).
     * @return Un CompletableFuture que contendrá la respuesta del servidor como String.
     */
    public CompletableFuture<String> getAsync(String endpoint, boolean isHospital) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return get(endpoint, isHospital);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    /**
     * Realiza una petición POST asíncrona a un endpoint específico.
     *
     * @param endpoint El path específico del recurso.
     * @param requestBody El objeto a enviar en el cuerpo de la petición.
     * @param isHospital Booleano que indica si la petición es para el servicio de hospital (true) o farmacia (false).
     * @return Un CompletableFuture que contendrá la respuesta del servidor como String.
     */
    public CompletableFuture<String> postAsync(String endpoint, Object requestBody, boolean isHospital) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return post(endpoint, requestBody, isHospital);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    /**
     * Realiza una petición PUT asíncrona a un endpoint específico.
     *
     * @param endpoint El path específico del recurso a actualizar.
     * @param requestBody El objeto con los datos actualizados a enviar.
     * @param isHospital Booleano que indica si la petición es para el servicio de hospital (true) o farmacia (false).
     * @return Un CompletableFuture que contendrá la respuesta del servidor como String.
     */
    public CompletableFuture<String> putAsync(String endpoint, Object requestBody, boolean isHospital) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return put(endpoint, requestBody, isHospital);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
} 