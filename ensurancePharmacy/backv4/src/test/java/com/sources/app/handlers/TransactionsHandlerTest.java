package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.TransactionsDAO;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.Transactions;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionsHandlerTest {

    @Mock
    private TransactionsDAO mockTransactionsDAO;
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
    private TransactionsHandler transactionsHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<Transactions> transactionCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/transactions";

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
        transactionsHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockTransactionsDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/transaction")); // Wrong path
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        transactionsHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockTransactionsDAO);
    }
    
     @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE"); // Unsupported
        transactionsHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<Transactions> transactions = Arrays.asList(new Transactions(), new Transactions());
        when(mockTransactionsDAO.findAll()).thenReturn(transactions);
        String expectedJson = objectMapper.writeValueAsString(transactions);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        transactionsHandler.handle(mockHttpExchange);

        verify(mockTransactionsDAO).findAll();
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindById_Success() throws IOException {
        Long testId = 1L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        Transactions transaction = new Transactions(); transaction.setIdTransaction(testId);
        when(mockTransactionsDAO.findById(testId)).thenReturn(transaction);
        String expectedJson = objectMapper.writeValueAsString(transaction);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        transactionsHandler.handle(mockHttpExchange);

        verify(mockTransactionsDAO).findById(testId);
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindByUserId_Success() throws IOException {
        Long userId = 5L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?userId=" + userId)); // Using userId
        List<Transactions> transactions = Collections.singletonList(new Transactions());
        when(mockTransactionsDAO.findByUserId(userId)).thenReturn(transactions);
        String expectedJson = objectMapper.writeValueAsString(transactions);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        transactionsHandler.handle(mockHttpExchange);

        verify(mockTransactionsDAO).findByUserId(userId);
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handleGet_FindByUserId_WithUnderscore_Success() throws IOException {
        Long userId = 6L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?user_id=" + userId)); // Using user_id
        List<Transactions> transactions = Collections.singletonList(new Transactions());
        when(mockTransactionsDAO.findByUserId(userId)).thenReturn(transactions);
        String expectedJson = objectMapper.writeValueAsString(transactions);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        transactionsHandler.handle(mockHttpExchange);

        verify(mockTransactionsDAO).findByUserId(userId);
        verifyResponseSent(200, expectedBytes);
    }

     @Test
    void handleGet_FindById_NotFound() throws IOException {
        Long testId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        when(mockTransactionsDAO.findById(testId)).thenReturn(null);

        transactionsHandler.handle(mockHttpExchange);

        verify(mockTransactionsDAO).findById(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
    @Test
    void handleGet_FindById_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=invalid"));
        
        transactionsHandler.handle(mockHttpExchange);

        verify(mockTransactionsDAO, never()).findById(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Long userId = 1L; Long hospitalId = 2L;
        User user = new User(); user.setIdUser(userId);
        Hospital hospital = new Hospital(); hospital.setIdHospital(hospitalId);
        Transactions requestTx = new Transactions();
        requestTx.setUser(user);
        requestTx.setHospital(hospital);
        requestTx.setTransDate(new Date());
        requestTx.setTotal(100.0);
        requestTx.setCopay(10.0);
        requestTx.setTransactionComment("Comment");
        requestTx.setResult("OK");
        requestTx.setCovered(1);
        requestTx.setAuth("A1");
        
        String requestJson = objectMapper.writeValueAsString(requestTx);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        Transactions createdTx = new Transactions(); createdTx.setIdTransaction(50L);
        when(mockTransactionsDAO.create(anyLong(), anyLong(), any(Date.class), any(Double.class), any(Double.class), anyString(), anyString(), anyInt(), anyString()))
               .thenReturn(createdTx);
        String expectedJson = objectMapper.writeValueAsString(createdTx);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        transactionsHandler.handle(mockHttpExchange);

        verify(mockTransactionsDAO).create(eq(userId), eq(hospitalId), any(Date.class), eq(requestTx.getTotal()), eq(requestTx.getCopay()), eq(requestTx.getTransactionComment()), eq(requestTx.getResult()), eq(requestTx.getCovered()), eq(requestTx.getAuth()));
        verifyResponseSent(201, expectedBytes);
    }
    
    @Test
    void handlePost_MissingUserId_SendsBadRequest() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Transactions requestTx = new Transactions(); // Missing User
        requestTx.setTransDate(new Date()); requestTx.setTotal(10.0);
        String requestJson = objectMapper.writeValueAsString(requestTx);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        transactionsHandler.handle(mockHttpExchange);

        verify(mockTransactionsDAO, never()).create(anyLong(), anyLong(), any(), any(), any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L)); 
    }
    
     @Test
    void handlePost_DaoCreateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        User user = new User(); user.setIdUser(1L);
        Hospital hospital = new Hospital(); hospital.setIdHospital(2L);
        Transactions requestTx = new Transactions();
        requestTx.setUser(user); requestTx.setHospital(hospital); requestTx.setTransDate(new Date()); requestTx.setTotal(1.0); requestTx.setCopay(0.0);
        String requestJson = objectMapper.writeValueAsString(requestTx);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockTransactionsDAO.create(anyLong(), anyLong(), any(), any(), any(), any(), any(), any(), any())).thenReturn(null);

        transactionsHandler.handle(mockHttpExchange);

        verify(mockTransactionsDAO).create(anyLong(), anyLong(), any(), any(), any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
     @Test
    void handlePost_InvalidJson() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"user\": invalid}";
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        transactionsHandler.handle(mockHttpExchange);

        verify(mockTransactionsDAO, never()).create(anyLong(), anyLong(), any(), any(), any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Catches Exception
    }

    // --- PUT Tests ---

    @Test
    void handlePut_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Transactions transactionToUpdate = new Transactions(); transactionToUpdate.setIdTransaction(1L);
        transactionToUpdate.setResult("Updated Result");
        String requestJson = objectMapper.writeValueAsString(transactionToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockTransactionsDAO.update(any(Transactions.class))).thenReturn(transactionToUpdate);
        String expectedJson = objectMapper.writeValueAsString(transactionToUpdate);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        transactionsHandler.handle(mockHttpExchange);

        verify(mockTransactionsDAO).update(transactionCaptor.capture());
        assertEquals(transactionToUpdate.getIdTransaction(), transactionCaptor.getValue().getIdTransaction());
        assertEquals(transactionToUpdate.getResult(), transactionCaptor.getValue().getResult());
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handlePut_DaoUpdateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Transactions transactionToUpdate = new Transactions();
        String requestJson = objectMapper.writeValueAsString(transactionToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockTransactionsDAO.update(any(Transactions.class))).thenReturn(null);

        transactionsHandler.handle(mockHttpExchange);

        verify(mockTransactionsDAO).update(any(Transactions.class));
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