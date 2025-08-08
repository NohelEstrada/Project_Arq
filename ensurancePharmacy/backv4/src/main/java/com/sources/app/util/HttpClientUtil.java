package com.sources.app.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Clase de utilidad para realizar peticiones HTTP (GET, POST, PUT, DELETE).
 * Proporciona métodos estáticos simples para interactuar con APIs REST.
 * Incluye manejo básico de timeouts y errores.
 */
public class HttpClientUtil {
    
    /**
     * Constructor privado para prevenir la instanciación de la clase de utilidad.
     */
    private HttpClientUtil() {}
    
    private static final int TIMEOUT = 5000; // 5 segundos
    
    /**
     * Realiza una petición GET a la URL especificada
     * @param urlString URL de destino
     * @return Respuesta como String, o null si hay error
     */
    public static String get(String urlString) {
        HttpURLConnection connection = null;
        try {
            System.out.println("Realizando GET a: " + urlString);
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestProperty("Accept", "application/json");
            
            int responseCode = connection.getResponseCode();
            System.out.println("Código de respuesta: " + responseCode);
            
            if (responseCode >= 200 && responseCode < 300) {
                String response = readResponse(connection);
                System.out.println("Respuesta recibida, longitud: " + (response != null ? response.length() : 0));
                return response;
            } else {
                String errorResponse = readErrorResponse(connection);
                System.err.println("Error en petición GET a " + urlString + 
                                  ": Código " + responseCode + 
                                  ", Respuesta: " + errorResponse);
                return null;
            }
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("Timeout al conectar con " + urlString + " - La conexión tardó más de " + TIMEOUT + "ms");
            return null;
        } catch (java.net.ConnectException e) {
            System.err.println("Error de conexión a " + urlString + " - " + e.getMessage());
            return null;
        } catch (java.net.UnknownHostException e) {
            System.err.println("Host desconocido: " + urlString + " - " + e.getMessage());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Excepción al realizar GET a " + urlString + ": " + e.getMessage() + " (" + e.getClass().getName() + ")");
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Realiza una petición POST a la URL especificada
     * @param urlString URL de destino
     * @param jsonBody Cuerpo de la petición en formato JSON
     * @return Respuesta como String, o null si hay error
     */
    public static String post(String urlString, String jsonBody) {
        return sendWithBody(urlString, "POST", jsonBody);
    }
    
    /**
     * Realiza una petición PUT a la URL especificada
     * @param urlString URL de destino
     * @param jsonBody Cuerpo de la petición en formato JSON
     * @return Respuesta como String, o null si hay error
     */
    public static String put(String urlString, String jsonBody) {
        return sendWithBody(urlString, "PUT", jsonBody);
    }
    
    /**
     * Realiza una petición DELETE a la URL especificada
     * @param urlString URL de destino
     * @return Respuesta como String, o null si hay error
     */
    public static String delete(String urlString) {
        HttpURLConnection connection = null;
        try {
            System.out.println("Realizando DELETE a: " + urlString);
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            
            int responseCode = connection.getResponseCode();
            System.out.println("Código de respuesta: " + responseCode);
            
            if (responseCode >= 200 && responseCode < 300) {
                String response = readResponse(connection);
                System.out.println("Respuesta recibida, longitud: " + (response != null ? response.length() : 0));
                return response;
            } else {
                String errorResponse = readErrorResponse(connection);
                System.err.println("Error en petición DELETE a " + urlString + 
                                  ": Código " + responseCode + 
                                  ", Respuesta: " + errorResponse);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Excepción al realizar DELETE a " + urlString + ": " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Método auxiliar para enviar peticiones con cuerpo (POST/PUT)
     */
    private static String sendWithBody(String urlString, String method, String jsonBody) {
        HttpURLConnection connection = null;
        try {
            System.out.println("Realizando " + method + " a: " + urlString);
            if (jsonBody != null) {
                System.out.println("Body: " + jsonBody);
            }
            
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            
            // Escribir el cuerpo de la petición
            if (jsonBody != null && !jsonBody.isEmpty()) {
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            
            int responseCode = connection.getResponseCode();
            System.out.println("Código de respuesta: " + responseCode);
            
            if (responseCode >= 200 && responseCode < 300) {
                String response = readResponse(connection);
                System.out.println("Respuesta recibida, longitud: " + (response != null ? response.length() : 0));
                return response;
            } else {
                String errorResponse = readErrorResponse(connection);
                System.err.println("Error en petición " + method + " a " + urlString + 
                                  ": Código " + responseCode + 
                                  ", Respuesta: " + errorResponse);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Excepción al realizar " + method + " a " + urlString + ": " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Lee la respuesta de una conexión HTTP
     */
    private static String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
    
    /**
     * Lee la respuesta de error de una conexión HTTP
     */
    private static String readErrorResponse(HttpURLConnection connection) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        } catch (Exception e) {
            return "No se pudo leer la respuesta de error: " + e.getMessage();
        }
    }
} 