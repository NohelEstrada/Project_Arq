package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.ConfigurableAmountDAO;
import com.sources.app.entities.ConfigurableAmount;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurableAmountHandlerTest {

    @Mock
    private ConfigurableAmountDAO mockConfigDAO;
    @Mock
    private HttpExchange mockHttpExchange;
    @Mock
    private Headers mockRequestHeaders;
    @Mock
    private Headers mockResponseHeaders;
    @Mock
    private OutputStream mockResponseBody;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

    @InjectMocks
    private ConfigurableAmountHandler configurableAmountHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;
    @Captor
    ArgumentCaptor<ConfigurableAmount> configAmountCaptor;

    private static final String ENDPOINT_CURRENT = "/api/configurable-amount/current";
    private static final String ENDPOINT_UPDATE = "/api/configurable-amount/update";

    @BeforeEach
    void setUp() {
        // Common mocks for exchange
        lenient().when(mockHttpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockHttpExchange.getResponseBody()).thenReturn(mockResponseBody);
        lenient().when(mockHttpExchange.getRequestHeaders()).thenReturn(mockRequestHeaders);
    }

    @AfterEach
    void tearDown() throws IOException {
       // verify(mockResponseBody, atLeastOnce()).close(); // Add where applicable
    }
    
    @Test
    void handle_OptionsRequest_SendsNoContent() throws IOException {
        // when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_CURRENT)); // Likely unnecessary for OPTIONS check
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        configurableAmountHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockConfigDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/wrong-path"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        configurableAmountHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockConfigDAO);
    }
    
    @Test
    void handle_UnsupportedMethodForCurrent_SendsNotFound() throws IOException {
        // This handler routes based on path first, then method within the private handlers.
        // An unsupported method for a valid path will hit the final else in handle()
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_CURRENT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST"); // Unsupported for /current
        configurableAmountHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L)); // Because path matched, but method didn't in specific handlers
        verifyNoInteractions(mockConfigDAO);
    }
    
    @Test
    void handle_UnsupportedMethodForUpdate_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_UPDATE));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST"); // Unsupported for /update
        configurableAmountHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockConfigDAO);
    }

    // --- GET /current Tests ---

    @Test
    void handleGetCurrentConfig_Found_Success() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_CURRENT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        
        ConfigurableAmount config = new ConfigurableAmount();
        config.setIdConfigurableAmount(1L);
        config.setPrescriptionAmount(new BigDecimal("300.00"));
        when(mockConfigDAO.findCurrentConfig()).thenReturn(config);
        String expectedJson = objectMapper.writeValueAsString(config);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        configurableAmountHandler.handle(mockHttpExchange);

        verify(mockConfigDAO).findCurrentConfig();
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), eq((long)expectedBytes.length));
        verify(mockResponseBody).write(expectedBytes);
        verify(mockResponseBody).close();
    }
    
    @Test
    void handleGetCurrentConfig_NotFound_ReturnsDefault() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_CURRENT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockConfigDAO.findCurrentConfig()).thenReturn(null); // Simulate not found

        // Expected default object
        ConfigurableAmount defaultConfig = new ConfigurableAmount();
        defaultConfig.setPrescriptionAmount(new BigDecimal("250.00"));
        String expectedJson = objectMapper.writeValueAsString(defaultConfig);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        configurableAmountHandler.handle(mockHttpExchange);

        verify(mockConfigDAO).findCurrentConfig();
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), eq((long)expectedBytes.length));
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        // Verify the response body matches the default object JSON
        String actualJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertEquals(expectedJson, actualJson); 
        verify(mockResponseBody).close();
    }

    // --- PUT /update Tests ---

    @Test
    void handleUpdateConfig_ExistingConfig_Success() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_UPDATE));
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");

        BigDecimal newAmount = new BigDecimal("400.00");
        Map<String, Object> requestMap = Map.of("prescriptionAmount", newAmount.toString());
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        ConfigurableAmount existingConfig = new ConfigurableAmount();
        existingConfig.setIdConfigurableAmount(1L);
        existingConfig.setPrescriptionAmount(new BigDecimal("250.00"));
        when(mockConfigDAO.findCurrentConfig()).thenReturn(existingConfig);
        
        // Mock the update call - it should return the updated object
        when(mockConfigDAO.update(any(ConfigurableAmount.class))).thenAnswer(invocation -> {
            ConfigurableAmount updatedConfig = invocation.getArgument(0);
            assertEquals(newAmount, updatedConfig.getPrescriptionAmount()); // Check if amount was set before update call
            return updatedConfig;
        });

        configurableAmountHandler.handle(mockHttpExchange);

        verify(mockConfigDAO).findCurrentConfig();
        verify(mockConfigDAO).update(configAmountCaptor.capture());
        ConfigurableAmount capturedConfig = configAmountCaptor.getValue();
        assertEquals(existingConfig.getIdConfigurableAmount(), capturedConfig.getIdConfigurableAmount());
        assertEquals(newAmount, capturedConfig.getPrescriptionAmount());

        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String responseJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(responseJson.contains("\"prescriptionAmount\":400.00")); // Check response body
        verify(mockResponseBody).close();
    }

    @Test
    void handleUpdateConfig_NoExistingConfig_CreatesNew() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_UPDATE));
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");

        BigDecimal newAmount = new BigDecimal("350.00");
        Map<String, Object> requestMap = Map.of("prescriptionAmount", newAmount.toString());
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockConfigDAO.findCurrentConfig()).thenReturn(null); // No existing config

        ConfigurableAmount createdConfig = new ConfigurableAmount();
        createdConfig.setIdConfigurableAmount(5L); // Simulate ID generated on create
        createdConfig.setPrescriptionAmount(newAmount);
        when(mockConfigDAO.create(eq(newAmount))).thenReturn(createdConfig);
        String expectedJson = objectMapper.writeValueAsString(createdConfig);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        configurableAmountHandler.handle(mockHttpExchange);

        verify(mockConfigDAO).findCurrentConfig();
        verify(mockConfigDAO).create(eq(newAmount));
        verify(mockConfigDAO, never()).update(any());
        
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), eq((long)expectedBytes.length)); // Should still be 200 OK as per code
        verify(mockResponseBody).write(expectedBytes);
        verify(mockResponseBody).close();
    }
    
     @Test
    void handleUpdateConfig_DaoUpdateFails() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_UPDATE));
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");

        Map<String, Object> requestMap = Map.of("prescriptionAmount", "500.00");
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        ConfigurableAmount existingConfig = new ConfigurableAmount();
        when(mockConfigDAO.findCurrentConfig()).thenReturn(existingConfig);
        when(mockConfigDAO.update(any(ConfigurableAmount.class))).thenReturn(null); // Simulate update failure

        configurableAmountHandler.handle(mockHttpExchange);

        verify(mockConfigDAO).findCurrentConfig();
        verify(mockConfigDAO).update(any(ConfigurableAmount.class));
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
     @Test
    void handleUpdateConfig_DaoCreateFailsWhenNoExisting() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_UPDATE));
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");

        Map<String, Object> requestMap = Map.of("prescriptionAmount", "500.00");
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockConfigDAO.findCurrentConfig()).thenReturn(null); // No existing config
        when(mockConfigDAO.create(any(BigDecimal.class))).thenReturn(null); // Simulate create failure

        configurableAmountHandler.handle(mockHttpExchange);

        verify(mockConfigDAO).findCurrentConfig();
        verify(mockConfigDAO).create(any(BigDecimal.class));
        verify(mockConfigDAO, never()).update(any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
     @Test
    void handleUpdateConfig_InvalidJsonBody() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_UPDATE));
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        String invalidJson = "{\"prescriptionAmount\": invalid}";
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        // Expect the handler to catch the Jackson parsing exception
        configurableAmountHandler.handle(mockHttpExchange);

        verify(mockConfigDAO, never()).findCurrentConfig();
        verify(mockConfigDAO, never()).create(any());
        verify(mockConfigDAO, never()).update(any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Exception leads to 500
    }
} 