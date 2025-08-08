package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.TransactionPolicyDAO;
import com.sources.app.entities.Policy;
import com.sources.app.entities.TransactionPolicy;
import com.sources.app.entities.User;
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
class TransactionPolicyHandlerTest {

    @Mock
    private TransactionPolicyDAO mockTransactionPolicyDAO;
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
    private TransactionPolicyHandler transactionPolicyHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<TransactionPolicy> transactionPolicyCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/transactionpolicy";

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
        transactionPolicyHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockTransactionPolicyDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/wrong"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        transactionPolicyHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockTransactionPolicyDAO);
    }
    
    @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE"); // Unsupported
        transactionPolicyHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<TransactionPolicy> transactions = Arrays.asList(new TransactionPolicy(), new TransactionPolicy());
        when(mockTransactionPolicyDAO.findAll()).thenReturn(transactions);
        String expectedJson = objectMapper.writeValueAsString(transactions);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        transactionPolicyHandler.handle(mockHttpExchange);

        verify(mockTransactionPolicyDAO).findAll();
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindById_Success() throws IOException {
        Long testId = 1L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        TransactionPolicy tp = new TransactionPolicy(); tp.setIdTransactionPolicy(testId);
        when(mockTransactionPolicyDAO.findById(testId)).thenReturn(tp);
        String expectedJson = objectMapper.writeValueAsString(tp);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        transactionPolicyHandler.handle(mockHttpExchange);

        verify(mockTransactionPolicyDAO).findById(testId);
        verifyResponseSent(200, expectedBytes);
    }

     @Test
    void handleGet_FindById_NotFound() throws IOException {
        Long testId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        when(mockTransactionPolicyDAO.findById(testId)).thenReturn(null);

        transactionPolicyHandler.handle(mockHttpExchange);

        verify(mockTransactionPolicyDAO).findById(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
    @Test
    void handleGet_FindById_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=abc"));
        
        transactionPolicyHandler.handle(mockHttpExchange);

        verify(mockTransactionPolicyDAO, never()).findById(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Long policyId = 1L; Long userId = 2L;
        Policy policy = new Policy(); policy.setIdPolicy(policyId);
        User user = new User(); user.setIdUser(userId);
        TransactionPolicy requestTp = new TransactionPolicy();
        requestTp.setPolicy(policy);
        requestTp.setUser(user);
        requestTp.setPayDate(new Date());
        requestTp.setTotal(new BigDecimal("99.99"));
        String requestJson = objectMapper.writeValueAsString(requestTp);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        TransactionPolicy createdTp = new TransactionPolicy(); createdTp.setIdTransactionPolicy(50L);
        when(mockTransactionPolicyDAO.create(anyLong(), anyLong(), any(Date.class), any(BigDecimal.class)))
               .thenReturn(createdTp);
        String expectedJson = objectMapper.writeValueAsString(createdTp);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        transactionPolicyHandler.handle(mockHttpExchange);

        verify(mockTransactionPolicyDAO).create(eq(policyId), eq(userId), any(Date.class), eq(requestTp.getTotal()));
        verifyResponseSent(201, expectedBytes);
    }
    
    @Test
    void handlePost_MissingPolicyId_SendsBadRequest() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        TransactionPolicy requestTp = new TransactionPolicy(); // Missing Policy
        requestTp.setUser(new User()); requestTp.getUser().setIdUser(1L);
        requestTp.setPayDate(new Date()); requestTp.setTotal(BigDecimal.TEN);
        String requestJson = objectMapper.writeValueAsString(requestTp);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        transactionPolicyHandler.handle(mockHttpExchange);

        verify(mockTransactionPolicyDAO, never()).create(anyLong(), anyLong(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L)); 
    }
    
     @Test
    void handlePost_DaoCreateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Policy policy = new Policy(); policy.setIdPolicy(1L);
        User user = new User(); user.setIdUser(2L);
        TransactionPolicy requestTp = new TransactionPolicy();
        requestTp.setPolicy(policy); requestTp.setUser(user); requestTp.setPayDate(new Date()); requestTp.setTotal(BigDecimal.ONE);
        String requestJson = objectMapper.writeValueAsString(requestTp);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockTransactionPolicyDAO.create(anyLong(), anyLong(), any(), any())).thenReturn(null);

        transactionPolicyHandler.handle(mockHttpExchange);

        verify(mockTransactionPolicyDAO).create(anyLong(), anyLong(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
     @Test
    void handlePost_InvalidJson() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"total\": invalid}";
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        transactionPolicyHandler.handle(mockHttpExchange);

        verify(mockTransactionPolicyDAO, never()).create(anyLong(), anyLong(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Catches Exception
    }

    // --- PUT Tests ---

    @Test
    void handlePut_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        TransactionPolicy tpToUpdate = new TransactionPolicy(); tpToUpdate.setIdTransactionPolicy(1L);
        tpToUpdate.setTotal(new BigDecimal("150.00"));
        String requestJson = objectMapper.writeValueAsString(tpToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockTransactionPolicyDAO.update(any(TransactionPolicy.class))).thenReturn(tpToUpdate);
        String expectedJson = objectMapper.writeValueAsString(tpToUpdate);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        transactionPolicyHandler.handle(mockHttpExchange);

        verify(mockTransactionPolicyDAO).update(transactionPolicyCaptor.capture());
        assertEquals(tpToUpdate.getIdTransactionPolicy(), transactionPolicyCaptor.getValue().getIdTransactionPolicy());
        assertEquals(tpToUpdate.getTotal(), transactionPolicyCaptor.getValue().getTotal());
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handlePut_DaoUpdateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        TransactionPolicy tpToUpdate = new TransactionPolicy();
        String requestJson = objectMapper.writeValueAsString(tpToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockTransactionPolicyDAO.update(any(TransactionPolicy.class))).thenReturn(null);

        transactionPolicyHandler.handle(mockHttpExchange);

        verify(mockTransactionPolicyDAO).update(any(TransactionPolicy.class));
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
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