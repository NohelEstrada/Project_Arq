package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.PolicyDAO;
import com.sources.app.entities.Policy;
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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyHandlerTest {

    @Mock
    private PolicyDAO mockPolicyDAO;
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
    private PolicyHandler policyHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<Policy> policyCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/policy";

    @BeforeEach
    void setUp() {
        lenient().when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT));
        lenient().when(mockHttpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockHttpExchange.getResponseBody()).thenReturn(mockResponseBody);
        lenient().when(mockHttpExchange.getRequestHeaders()).thenReturn(mockRequestHeaders);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        // verify(mockResponseBody, atLeastOnce()).close(); // Add if needed
    }

    @Test
    void handle_OptionsRequest_SendsNoContent() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        policyHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockPolicyDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/policies")); // Wrong path
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        policyHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockPolicyDAO);
    }
    
     @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PATCH"); // Unsupported
        policyHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<Policy> policies = Arrays.asList(new Policy(), new Policy());
        when(mockPolicyDAO.findAll()).thenReturn(policies);
        String expectedJson = objectMapper.writeValueAsString(policies);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO).findAll();
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindById_Success() throws IOException {
        Long testId = 1L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        Policy policy = new Policy(); policy.setIdPolicy(testId);
        when(mockPolicyDAO.find(testId)).thenReturn(policy); // DAO uses find, not findById
        String expectedJson = objectMapper.writeValueAsString(policy);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO).find(testId);
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handleGet_FindById_NotFound() throws IOException {
        Long testId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        when(mockPolicyDAO.find(testId)).thenReturn(null);

        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO).find(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handleGet_FindById_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=abc"));
        
        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO, never()).find(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Policy requestPolicy = new Policy(0.8f, new Date(), new Date(), 1000f, 1);
        String requestJson = objectMapper.writeValueAsString(requestPolicy);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        Policy createdPolicy = new Policy(); createdPolicy.setIdPolicy(50L);
        when(mockPolicyDAO.create(any(), any(), any(), any(), any())).thenReturn(createdPolicy);
        String expectedJson = objectMapper.writeValueAsString(createdPolicy);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO).create(
            eq(requestPolicy.getPercentage()), any(Date.class), 
            any(Date.class), eq(requestPolicy.getCost()), eq(requestPolicy.getEnabled())
        );
        verifyResponseSent(201, expectedBytes);
    }
    
    @Test
    void handlePost_DaoCreateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Policy requestPolicy = new Policy(0.8f, new Date(), new Date(), 1000f, 1);
        String requestJson = objectMapper.writeValueAsString(requestPolicy);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockPolicyDAO.create(any(), any(), any(), any(), any())).thenReturn(null);

        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO).create(any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
     @Test
    void handlePost_InvalidJson() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"percentage\": 0.8, \"enabled\": }";
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO, never()).create(any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Caught by generic Exception handler
    }

    // --- PUT Tests ---

    @Test
    void handlePut_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Policy policyToUpdate = new Policy(); policyToUpdate.setIdPolicy(1L);
        policyToUpdate.setCost(1500f);
        String requestJson = objectMapper.writeValueAsString(policyToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockPolicyDAO.update(any(Policy.class))).thenReturn(policyToUpdate);
        String expectedJson = objectMapper.writeValueAsString(policyToUpdate);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO).update(policyCaptor.capture());
        assertEquals(policyToUpdate.getIdPolicy(), policyCaptor.getValue().getIdPolicy());
        assertEquals(policyToUpdate.getCost(), policyCaptor.getValue().getCost());
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handlePut_DaoUpdateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Policy policyToUpdate = new Policy();
        String requestJson = objectMapper.writeValueAsString(policyToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockPolicyDAO.update(any(Policy.class))).thenReturn(null);

        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO).update(any(Policy.class));
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }

    // --- DELETE Tests ---

    @Test
    void handleDelete_Success() throws IOException {
        Long testId = 1L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        when(mockPolicyDAO.delete(testId)).thenReturn(true);

        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO).delete(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(200), eq(-1L)); // Success, No Content for delete often used (204), but handler uses 200
    }

    @Test
    void handleDelete_NotFound() throws IOException {
        Long testId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        when(mockPolicyDAO.delete(testId)).thenReturn(false);

        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO).delete(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
    @Test
    void handleDelete_MissingId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT)); // No ID
        
        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO, never()).delete(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }
    
    @Test
    void handleDelete_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=abc"));
        
        policyHandler.handle(mockHttpExchange);

        verify(mockPolicyDAO, never()).delete(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }
    
     // Helper method to verify JSON response
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