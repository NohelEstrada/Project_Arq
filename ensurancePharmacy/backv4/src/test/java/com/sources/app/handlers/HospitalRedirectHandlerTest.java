package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.util.HttpClientUtil;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalRedirectHandlerTest {

    @Mock
    private HttpExchange mockHttpExchange;
    @Mock
    private Headers mockRequestHeaders;
    @Mock
    private Headers mockResponseHeaders;
    @Mock
    private OutputStream mockResponseBody;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper(); // Used internally if response needs parsing

    @InjectMocks
    private HospitalRedirectHandler hospitalRedirectHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String BASE_ENDPOINT = "/api/hospital-integration";
    private static final String HOSPITAL_API_BASE_URL = "http://localhost:8000/api";

    private MockedStatic<HttpClientUtil> mockedHttpClientUtil;

    @BeforeEach
    void setUp() {
        lenient().when(mockHttpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockHttpExchange.getResponseBody()).thenReturn(mockResponseBody);
        lenient().when(mockHttpExchange.getRequestHeaders()).thenReturn(mockRequestHeaders);
        
        // Mock static HttpClientUtil methods
        mockedHttpClientUtil = Mockito.mockStatic(HttpClientUtil.class);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockedHttpClientUtil.close(); // Close the static mock
        // verify(mockResponseBody, atLeastOnce()).close(); // Add if needed
    }

    @Test
    void handle_OptionsRequest_SendsNoContent() throws IOException {
        // when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT)); // Unnecessary stubbing reported at line 80
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        hospitalRedirectHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        mockedHttpClientUtil.verifyNoInteractions();
    }

    @Test
    void handle_WrongBasePath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/wrong"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        hospitalRedirectHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        mockedHttpClientUtil.verifyNoInteractions();
    }
    
    @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        // This handler forwards based on method, so 405 happens in the switch
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/somepath"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("PATCH"); 
        hospitalRedirectHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
        mockedHttpClientUtil.verifyNoInteractions(); // No HTTP client call for unsupported method
    }

    // --- Forwarding Tests ---
    
    @Test
    void handleGet_ForwardsRequestCorrectly() throws IOException {
        String subPath = "/hospitals";
        String query = "enabled=1";
        String expectedForwardUrl = HOSPITAL_API_BASE_URL + subPath + "?" + query;
        String mockHospitalResponse = "[{\"id\": 1, \"name\": \"Hospital A\"}]";
        byte[] expectedBytes = mockHospitalResponse.getBytes(StandardCharsets.UTF_8);

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + subPath + "?" + query));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        mockedHttpClientUtil.when(() -> HttpClientUtil.get(eq(expectedForwardUrl))).thenReturn(mockHospitalResponse);

        hospitalRedirectHandler.handle(mockHttpExchange);

        mockedHttpClientUtil.verify(() -> HttpClientUtil.get(eq(expectedForwardUrl)));
        verifyResponseSent(200, expectedBytes);
    }
    
     @Test
    void handleGet_RootPath_ForwardsToApiRoot() throws IOException {
        String expectedForwardUrl = HOSPITAL_API_BASE_URL + "/"; // Root of the target API
        String mockHospitalResponse = "{\"message\": \"Hospital API Root\"}";
        byte[] expectedBytes = mockHospitalResponse.getBytes(StandardCharsets.UTF_8);

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/")); // Request to root of integration endpoint
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        mockedHttpClientUtil.when(() -> HttpClientUtil.get(eq(expectedForwardUrl))).thenReturn(mockHospitalResponse);

        hospitalRedirectHandler.handle(mockHttpExchange);

        mockedHttpClientUtil.verify(() -> HttpClientUtil.get(eq(expectedForwardUrl)));
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handlePost_ForwardsRequestCorrectly() throws IOException {
        String subPath = "/patients";
        String expectedForwardUrl = HOSPITAL_API_BASE_URL + subPath;
        String requestBody = "{\"name\": \"John Doe\"}";
        String mockHospitalResponse = "{\"id\": 123, \"name\": \"John Doe\"}";
        byte[] expectedBytes = mockHospitalResponse.getBytes(StandardCharsets.UTF_8);
        InputStream requestBodyStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + subPath));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        mockedHttpClientUtil.when(() -> HttpClientUtil.post(eq(expectedForwardUrl), eq(requestBody))).thenReturn(mockHospitalResponse);

        hospitalRedirectHandler.handle(mockHttpExchange);

        mockedHttpClientUtil.verify(() -> HttpClientUtil.post(eq(expectedForwardUrl), eq(requestBody)));
        verifyResponseSent(200, expectedBytes); // Handler sends 200, not necessarily matching upstream status
    }

    @Test
    void handlePut_ForwardsRequestCorrectly() throws IOException {
        String subPath = "/patients/123";
        String expectedForwardUrl = HOSPITAL_API_BASE_URL + subPath;
        String requestBody = "{\"name\": \"Jane Doe\"}";
        String mockHospitalResponse = "{\"id\": 123, \"name\": \"Jane Doe\"}";
        byte[] expectedBytes = mockHospitalResponse.getBytes(StandardCharsets.UTF_8);
        InputStream requestBodyStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + subPath));
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        mockedHttpClientUtil.when(() -> HttpClientUtil.put(eq(expectedForwardUrl), eq(requestBody))).thenReturn(mockHospitalResponse);

        hospitalRedirectHandler.handle(mockHttpExchange);

        mockedHttpClientUtil.verify(() -> HttpClientUtil.put(eq(expectedForwardUrl), eq(requestBody)));
        verifyResponseSent(200, expectedBytes); 
    }

    @Test
    void handleDelete_ForwardsRequestCorrectly() throws IOException {
        String subPath = "/appointments/456";
        String expectedForwardUrl = HOSPITAL_API_BASE_URL + subPath;
        String mockHospitalResponse = "{\"deleted\": true}"; // Example response
        byte[] expectedBytes = mockHospitalResponse.getBytes(StandardCharsets.UTF_8);

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + subPath));
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        mockedHttpClientUtil.when(() -> HttpClientUtil.delete(eq(expectedForwardUrl))).thenReturn(mockHospitalResponse);

        hospitalRedirectHandler.handle(mockHttpExchange);

        mockedHttpClientUtil.verify(() -> HttpClientUtil.delete(eq(expectedForwardUrl)));
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handle_HttpClientReturnsNull_SendsInternalError() throws IOException {
        String subPath = "/status";
        String expectedForwardUrl = HOSPITAL_API_BASE_URL + subPath;
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + subPath));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        mockedHttpClientUtil.when(() -> HttpClientUtil.get(eq(expectedForwardUrl))).thenReturn(null); // Simulate client error

        hospitalRedirectHandler.handle(mockHttpExchange);

        mockedHttpClientUtil.verify(() -> HttpClientUtil.get(eq(expectedForwardUrl)));
        verify(mockHttpExchange).sendResponseHeaders(eq(500), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("Error al conectar con el servicio del hospital"));
        verify(mockResponseBody).close();
    }
    
     @Test
    void handle_HttpClientThrowsException_SendsInternalError() throws IOException {
        String subPath = "/data";
        String expectedForwardUrl = HOSPITAL_API_BASE_URL + subPath;
        String exceptionMessage = "Connection refused";
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + subPath));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        mockedHttpClientUtil.when(() -> HttpClientUtil.get(eq(expectedForwardUrl)))
                           .thenThrow(new RuntimeException(exceptionMessage)); // Simulate client throwing exception

        hospitalRedirectHandler.handle(mockHttpExchange);

        mockedHttpClientUtil.verify(() -> HttpClientUtil.get(eq(expectedForwardUrl)));
        verify(mockHttpExchange).sendResponseHeaders(eq(500), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains(exceptionMessage.replace("\"", "'"))); // Check if exception message is in response
        verify(mockResponseBody).close();
    }

    // Helper to verify response sending
    private void verifyResponseSent(int expectedStatusCode, byte[] expectedBodyBytes) throws IOException {
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(statusCodeCaptor.capture(), responseLengthCaptor.capture());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        verify(mockResponseBody).close();

        assertEquals(expectedStatusCode, statusCodeCaptor.getValue());
        assertArrayEquals(expectedBodyBytes, responseBodyCaptor.getValue());
        assertEquals((long)expectedBodyBytes.length, responseLengthCaptor.getValue());
    }
} 