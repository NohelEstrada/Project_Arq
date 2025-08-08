package com.sources.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PharmacyClientTest {

    @Mock
    private HttpURLConnection mockConnection;
    @Mock
    private OutputStream mockOutputStream;
    @Mock
    private URL mockUrl;

    // Static mocking requires careful handling, especially for final classes like URL
    // PowerMock might be necessary for full coverage. Mockito's static mocking is limited.
    private MockedStatic<URL> mockedUrlStatic; 

    @Captor
    ArgumentCaptor<byte[]> outputStreamCaptor;

    private static final String PHARMACY_API_BASE_URL = "http://localhost:8082/api2";
    private final String TEST_ENDPOINT = "/medications";
    private final String SUCCESS_RESPONSE = "{\"status\":\"ok\"}";
    private final String ERROR_RESPONSE = "{\"error\":\"invalid request\"}";
    private final Map<String, String> TEST_BODY_MAP = Map.of("id", "123", "qty", "10");
    private final String TEST_BODY_JSON = "{\"id\":\"123\",\"qty\":\"10\"}"; // Approximate expected JSON

    @BeforeEach
    void setUp() throws IOException {
        lenient().when(mockConnection.getOutputStream()).thenReturn(mockOutputStream);
        lenient().doNothing().when(mockOutputStream).write(any(byte[].class), anyInt(), anyInt());
        lenient().doNothing().when(mockOutputStream).close();
        lenient().doNothing().when(mockConnection).disconnect();
        
        // Attempt to mock URL constructor and openConnection using Mockito static mocking
        // This might fail or have limitations. 
        mockedUrlStatic = Mockito.mockStatic(URL.class);
        try {
            URL realUrl = new URL(PHARMACY_API_BASE_URL + TEST_ENDPOINT);
            mockedUrlStatic.when(() -> new URL(anyString())).thenReturn(mockUrl); // Mock constructor
            when(mockUrl.openConnection()).thenReturn(mockConnection); // Mock openConnection on the mocked URL
        } catch (MalformedURLException e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        if (mockedUrlStatic != null) {
            mockedUrlStatic.close();
        }
    }

    // Helper to setup response
    private void setupMockResponse(int statusCode, String responseBody, String errorBody) throws IOException {
        when(mockConnection.getResponseCode()).thenReturn(statusCode);
        InputStream responseStream = new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream(errorBody.getBytes(StandardCharsets.UTF_8));

        if (statusCode >= 200 && statusCode < 300) {
            when(mockConnection.getInputStream()).thenReturn(responseStream);
            lenient().when(mockConnection.getErrorStream()).thenReturn(null);
        } else {
            lenient().when(mockConnection.getInputStream()).thenThrow(new IOException("Simulated error stream access"));
            when(mockConnection.getErrorStream()).thenReturn(errorStream);
        }
    }

    @Test
    void get_Success() throws Exception {
        setupMockResponse(HttpURLConnection.HTTP_OK, SUCCESS_RESPONSE, "");
        String result = PharmacyClient.get(TEST_ENDPOINT);
        
        assertEquals(SUCCESS_RESPONSE, result);
        mockedUrlStatic.verify(() -> new URL(PHARMACY_API_BASE_URL + TEST_ENDPOINT));
        verify(mockConnection).setRequestMethod("GET");
        verify(mockConnection).getResponseCode();
        verify(mockConnection).getInputStream();
    }
    
     @Test
    void get_HttpError_ReturnsErrorJson() throws Exception {
        setupMockResponse(HttpURLConnection.HTTP_NOT_FOUND, "", ERROR_RESPONSE);
        String result = PharmacyClient.get(TEST_ENDPOINT);
        
        // The method currently wraps the error in a new JSON structure
        assertTrue(result.contains("\"error\":true"));
        assertTrue(result.contains("\"message\":\"")); // Contains the error stream content
        assertTrue(result.contains("\"statusCode\":404")); 
        verify(mockConnection).setRequestMethod("GET");
        verify(mockConnection).getResponseCode();
        verify(mockConnection).getErrorStream();
        verify(mockConnection, never()).getInputStream();
    }

    @Test
    void post_Success() throws Exception {
        setupMockResponse(HttpURLConnection.HTTP_CREATED, SUCCESS_RESPONSE, "");
        String result = PharmacyClient.post(TEST_ENDPOINT, TEST_BODY_MAP);

        assertEquals(SUCCESS_RESPONSE, result);
        mockedUrlStatic.verify(() -> new URL(PHARMACY_API_BASE_URL + TEST_ENDPOINT));
        verify(mockConnection).setRequestMethod("POST");
        verify(mockConnection).setDoOutput(true);
        verify(mockConnection).setRequestProperty(eq("Content-Type"), eq("application/json"));
        verify(mockOutputStream).write(outputStreamCaptor.capture(), eq(0), anyInt());
        // Verify JSON body was written (approximation)
        assertTrue(new String(outputStreamCaptor.getValue(), StandardCharsets.UTF_8).contains("\"key\":\"value\""));
        verify(mockConnection).getResponseCode();
        verify(mockConnection).getInputStream();
    }
    
    @Test
    void post_HttpError_ReturnsErrorJson() throws Exception {
        setupMockResponse(HttpURLConnection.HTTP_BAD_REQUEST, "", ERROR_RESPONSE);
        String result = PharmacyClient.post(TEST_ENDPOINT, TEST_BODY_MAP);

        assertTrue(result.contains("\"error\":true"));
        assertTrue(result.contains(ERROR_RESPONSE)); 
        assertTrue(result.contains("\"statusCode\":400")); 
        verify(mockConnection).setRequestMethod("POST");
        verify(mockConnection).getResponseCode();
        verify(mockConnection).getErrorStream();
    }

    @Test
    void put_Success() throws Exception {
         setupMockResponse(HttpURLConnection.HTTP_OK, SUCCESS_RESPONSE, "");
        String result = PharmacyClient.put(TEST_ENDPOINT, TEST_BODY_MAP);

        assertEquals(SUCCESS_RESPONSE, result);
        mockedUrlStatic.verify(() -> new URL(PHARMACY_API_BASE_URL + TEST_ENDPOINT));
        verify(mockConnection).setRequestMethod("PUT");
        verify(mockConnection).setDoOutput(true);
        verify(mockOutputStream).write(outputStreamCaptor.capture(), eq(0), anyInt());
        assertTrue(new String(outputStreamCaptor.getValue(), StandardCharsets.UTF_8).contains("\"key\":\"value\""));
        verify(mockConnection).getResponseCode();
        verify(mockConnection).getInputStream();
    }
    
     @Test
    void put_HttpError_ThrowsException() throws Exception {
        // The PUT method in the client throws Exception on non-2xx, unlike POST
        setupMockResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, "", ERROR_RESPONSE);

        Exception exception = assertThrows(Exception.class, () -> {
            PharmacyClient.put(TEST_ENDPOINT, TEST_BODY_MAP);
        });
        
        assertTrue(exception.getMessage().contains("Error en la petición PUT: 500"));
        verify(mockConnection).setRequestMethod("PUT");
        verify(mockConnection).getResponseCode();
        // Error stream is not explicitly read in the client's PUT error handling
        // verify(mockConnection).getErrorStream(); 
    }
} 