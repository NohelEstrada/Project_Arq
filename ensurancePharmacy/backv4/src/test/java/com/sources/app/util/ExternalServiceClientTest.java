package com.sources.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalServiceClientTest {

    @Mock
    private HttpURLConnection mockConnection;
    @Mock
    private OutputStream mockOutputStream;
    
    // Spy the ObjectMapper to use its real methods but allow verification
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper(); 

    @InjectMocks
    private ExternalServiceClient externalServiceClient; // Instance under test
    
    @Captor
    ArgumentCaptor<byte[]> outputStreamCaptor;
    @Captor
    ArgumentCaptor<URL> urlCaptor;

    private static final String HOSPITAL_BASE_URL = "http://localhost:5051/api";
    private static final String PHARMACY_BASE_URL = "http://localhost:8080/api";
    private final String TEST_ENDPOINT = "/test-data";
    private final String SUCCESS_RESPONSE = "{\"data\":\"success\"}";
    private final String ERROR_RESPONSE = "{\"error\":\"failed\"}";
    private final Map<String, String> TEST_BODY = Map.of("key", "value");
    private final String TEST_BODY_JSON = "{\"key\":\"value\"}"; // Manually match expected JSON

    // Mock construction of URL and HttpURLConnection
    // This is the standard Mockito way but might not work for final URL class
    // PowerMock or refactoring might be needed for robust tests.
    private MockedConstruction<URL> mockedUrlConstruction;
    private MockedConstruction<HttpURLConnection> mockedConnectionConstruction;

    @Mock
    private URL mockUrlInstance; // Mock URL instance

    private MockedStatic<URL> mockedUrlStatic; // Class-level static mock

    @BeforeEach
    void setUp() throws IOException {
        // Setup static URL mock BEFORE other setup
        mockedUrlStatic = Mockito.mockStatic(URL.class);
        
        // Mock specific URL constructions needed by tests
        // No MalformedURLException expected with anyString()
        mockedUrlStatic.when(() -> new URL(anyString())).thenReturn(mockUrlInstance);
        when(mockUrlInstance.openConnection()).thenReturn(mockConnection);

        // Existing setup
        when(mockConnection.getOutputStream()).thenReturn(mockOutputStream);
        // Default successful response setup (can be overridden in tests)
        setupMockResponse(HttpURLConnection.HTTP_OK, SUCCESS_RESPONSE);
    }

    @AfterEach
    void tearDown() {
        // Close static mock AFTER tests
        mockedUrlStatic.close();
    }
    
     // Helper to mock responses
    private void setupMockResponse(int statusCode, String responseBody) throws IOException {
        when(mockConnection.getResponseCode()).thenReturn(statusCode);
        InputStream responseStream = new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Error Stream Content".getBytes(StandardCharsets.UTF_8)); // Generic error content

        if (statusCode >= 200 && statusCode < 300) {
            when(mockConnection.getInputStream()).thenReturn(responseStream);
            lenient().when(mockConnection.getErrorStream()).thenReturn(null);
        } else {
            lenient().when(mockConnection.getInputStream()).thenThrow(new IOException("Cannot get input stream on error"));
            when(mockConnection.getErrorStream()).thenReturn(errorStream);
        }
    }

    // --- Synchronous Tests ---

    @Test
    void get_Hospital_Success() throws Exception {
        // Setup specific URL expectation for this test
        mockedUrlStatic.when(() -> new URL(HOSPITAL_BASE_URL + TEST_ENDPOINT)).thenReturn(mockUrlInstance);
        setupMockResponse(HttpURLConnection.HTTP_OK, SUCCESS_RESPONSE); // Ensure correct response for this test

        String result = externalServiceClient.get(TEST_ENDPOINT, true);
        assertEquals(SUCCESS_RESPONSE, result);

        verify(mockConnection).setRequestMethod("GET");
        verify(mockConnection).setRequestProperty(eq("Content-Type"), eq("application/json"));
        verify(mockConnection).getResponseCode();
        verify(mockConnection).getInputStream();
        verify(mockConnection).disconnect();
    }
    
     @Test
    void get_Pharmacy_Success() throws Exception {
        // Setup specific URL expectation
        mockedUrlStatic.when(() -> new URL(PHARMACY_BASE_URL + TEST_ENDPOINT)).thenReturn(mockUrlInstance);
        setupMockResponse(HttpURLConnection.HTTP_OK, SUCCESS_RESPONSE);

        String result = externalServiceClient.get(TEST_ENDPOINT, false); // isHospital = false
        assertEquals(SUCCESS_RESPONSE, result);
        verify(mockConnection).setRequestMethod("GET");
    }

    @Test
    void get_HttpError_ThrowsException() throws Exception {
        // Setup specific URL expectation
        mockedUrlStatic.when(() -> new URL(HOSPITAL_BASE_URL + TEST_ENDPOINT)).thenReturn(mockUrlInstance);
        setupMockResponse(HttpURLConnection.HTTP_NOT_FOUND, ERROR_RESPONSE);

        Exception exception = assertThrows(Exception.class, () -> {
            externalServiceClient.get(TEST_ENDPOINT, true);
        });
        assertTrue(exception.getMessage().contains("Error en la petición GET: 404"));
        verify(mockConnection).disconnect();
    }
    
    @Test
    void post_Hospital_Success() throws Exception {
        // Setup specific URL expectation
         mockedUrlStatic.when(() -> new URL(HOSPITAL_BASE_URL + TEST_ENDPOINT)).thenReturn(mockUrlInstance);
         setupMockResponse(HttpURLConnection.HTTP_CREATED, SUCCESS_RESPONSE);

        String result = externalServiceClient.post(TEST_ENDPOINT, TEST_BODY, true);
        assertEquals(SUCCESS_RESPONSE, result);

        verify(mockConnection).setRequestMethod("POST");
        verify(mockConnection).setDoOutput(true);
        verify(mockConnection).setRequestProperty(eq("Content-Type"), eq("application/json"));
        verify(mockOutputStream).write(outputStreamCaptor.capture(), eq(0), anyInt());
        assertEquals(TEST_BODY_JSON, new String(outputStreamCaptor.getValue(), StandardCharsets.UTF_8));
        verify(mockConnection).getResponseCode();
        verify(mockConnection).getInputStream();
        verify(mockConnection).disconnect();
    }
    
    @Test
    void post_HttpError_ThrowsExceptionWithErrorBody() throws Exception {
        // Setup specific URL expectation
         mockedUrlStatic.when(() -> new URL(HOSPITAL_BASE_URL + TEST_ENDPOINT)).thenReturn(mockUrlInstance);
         setupMockResponse(HttpURLConnection.HTTP_BAD_REQUEST, ERROR_RESPONSE);

        Exception exception = assertThrows(Exception.class, () -> {
            externalServiceClient.post(TEST_ENDPOINT, TEST_BODY, true);
        });
        assertTrue(exception.getMessage().contains("Error en la petición POST: 400"));
        assertTrue(exception.getMessage().contains(ERROR_RESPONSE)); 
        verify(mockConnection).getErrorStream();
        verify(mockConnection).disconnect();
    }
    
    // --- Async Tests (verify they delegate correctly) ---
    
    @Test
    void getAsync_DelegatesToGet() throws Exception {
        // We don't need full HTTP mocking here, just mock the sync method
        ExternalServiceClient spiedClient = spy(externalServiceClient);
        doReturn(SUCCESS_RESPONSE).when(spiedClient).get(eq(TEST_ENDPOINT), eq(true));
        
        CompletableFuture<String> future = spiedClient.getAsync(TEST_ENDPOINT, true);
        String result = future.get(); // Wait for completion
        
        assertEquals(SUCCESS_RESPONSE, result);
        verify(spiedClient).get(eq(TEST_ENDPOINT), eq(true)); // Verify delegation
    }
    
    @Test
    void getAsync_HandlesException() throws Exception {
        ExternalServiceClient spiedClient = spy(externalServiceClient);
        Exception testException = new Exception("GET failed");
        doThrow(testException).when(spiedClient).get(anyString(), anyBoolean());
        
        CompletableFuture<String> future = spiedClient.getAsync(TEST_ENDPOINT, true);
        
        ExecutionException thrown = assertThrows(ExecutionException.class, () -> {
            try {
                future.get(); // This might throw InterruptedException as well, caught by outer assertThrows
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                fail("Test interrupted unexpectedly");
            }
        });
        
        assertTrue(thrown.getCause() instanceof RuntimeException);
        assertEquals(testException, thrown.getCause().getCause());
        verify(spiedClient).get(anyString(), anyBoolean());
    }
    
     @Test
    void postAsync_DelegatesToPost() throws Exception {
        ExternalServiceClient spiedClient = spy(externalServiceClient);
        doReturn(SUCCESS_RESPONSE).when(spiedClient).post(eq(TEST_ENDPOINT), eq(TEST_BODY), eq(false));
        
        CompletableFuture<String> future = spiedClient.postAsync(TEST_ENDPOINT, TEST_BODY, false);
        String result = future.get(); 
        
        assertEquals(SUCCESS_RESPONSE, result);
        verify(spiedClient).post(eq(TEST_ENDPOINT), eq(TEST_BODY), eq(false)); 
    }
    
    @Test
    void putAsync_DelegatesToPut() throws Exception {
        ExternalServiceClient spiedClient = spy(externalServiceClient);
        doReturn(SUCCESS_RESPONSE).when(spiedClient).put(eq(TEST_ENDPOINT), eq(TEST_BODY), eq(true));
        
        CompletableFuture<String> future = spiedClient.putAsync(TEST_ENDPOINT, TEST_BODY, true);
        String result = future.get(); 
        
        assertEquals(SUCCESS_RESPONSE, result);
        verify(spiedClient).put(eq(TEST_ENDPOINT), eq(TEST_BODY), eq(true)); 
    }

} 