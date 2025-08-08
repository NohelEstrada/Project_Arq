package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.TotalPharmacyDAO;
import com.sources.app.entities.Pharmacy;
import com.sources.app.entities.TotalPharmacy;
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
class TotalPharmacyHandlerTest {

    @Mock
    private TotalPharmacyDAO mockTotalPharmacyDAO;
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
    private TotalPharmacyHandler totalPharmacyHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<TotalPharmacy> totalPharmacyCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/totalpharmacy";

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
        totalPharmacyHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockTotalPharmacyDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/wrong"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        totalPharmacyHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockTotalPharmacyDAO);
    }
    
     @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE"); // Unsupported
        totalPharmacyHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<TotalPharmacy> totals = Arrays.asList(new TotalPharmacy(), new TotalPharmacy());
        when(mockTotalPharmacyDAO.findAll()).thenReturn(totals);
        String expectedJson = objectMapper.writeValueAsString(totals);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        totalPharmacyHandler.handle(mockHttpExchange);

        verify(mockTotalPharmacyDAO).findAll();
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindById_Success() throws IOException {
        Long testId = 1L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        TotalPharmacy totalPharmacy = new TotalPharmacy(); totalPharmacy.setIdTotalPharmacy(testId);
        when(mockTotalPharmacyDAO.findById(testId)).thenReturn(totalPharmacy);
        String expectedJson = objectMapper.writeValueAsString(totalPharmacy);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        totalPharmacyHandler.handle(mockHttpExchange);

        verify(mockTotalPharmacyDAO).findById(testId);
        verifyResponseSent(200, expectedBytes);
    }

     @Test
    void handleGet_FindById_NotFound() throws IOException {
        Long testId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        when(mockTotalPharmacyDAO.findById(testId)).thenReturn(null);

        totalPharmacyHandler.handle(mockHttpExchange);

        verify(mockTotalPharmacyDAO).findById(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handleGet_FindById_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=xyz"));
        
        totalPharmacyHandler.handle(mockHttpExchange);

        verify(mockTotalPharmacyDAO, never()).findById(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Long pharmacyId = 1L;
        Pharmacy pharmacy = new Pharmacy(); pharmacy.setIdPharmacy(pharmacyId);
        TotalPharmacy requestTotal = new TotalPharmacy();
        requestTotal.setPharmacy(pharmacy);
        requestTotal.setTotalDate(new Date());
        requestTotal.setTotal(new BigDecimal("500.00"));
        String requestJson = objectMapper.writeValueAsString(requestTotal);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        TotalPharmacy createdTotal = new TotalPharmacy(); createdTotal.setIdTotalPharmacy(50L);
        when(mockTotalPharmacyDAO.create(anyLong(), any(Date.class), any(BigDecimal.class)))
               .thenReturn(createdTotal);
        String expectedJson = objectMapper.writeValueAsString(createdTotal);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        totalPharmacyHandler.handle(mockHttpExchange);

        verify(mockTotalPharmacyDAO).create(eq(pharmacyId), any(Date.class), eq(requestTotal.getTotal()));
        verifyResponseSent(201, expectedBytes);
    }
    
    @Test
    void handlePost_MissingPharmacyId_SendsBadRequest() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        TotalPharmacy requestTotal = new TotalPharmacy(); // Missing pharmacy
        requestTotal.setTotalDate(new Date());
        requestTotal.setTotal(BigDecimal.TEN);
        String requestJson = objectMapper.writeValueAsString(requestTotal);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        totalPharmacyHandler.handle(mockHttpExchange);

        verify(mockTotalPharmacyDAO, never()).create(anyLong(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }
    
     @Test
    void handlePost_DaoCreateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Pharmacy pharmacy = new Pharmacy(); pharmacy.setIdPharmacy(1L);
        TotalPharmacy requestTotal = new TotalPharmacy(); 
        requestTotal.setPharmacy(pharmacy); requestTotal.setTotalDate(new Date()); requestTotal.setTotal(BigDecimal.ONE);
        String requestJson = objectMapper.writeValueAsString(requestTotal);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockTotalPharmacyDAO.create(anyLong(), any(), any())).thenReturn(null);

        totalPharmacyHandler.handle(mockHttpExchange);

        verify(mockTotalPharmacyDAO).create(anyLong(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
     @Test
    void handlePost_InvalidJson() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"total\": invalid}";
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        totalPharmacyHandler.handle(mockHttpExchange);

        verify(mockTotalPharmacyDAO, never()).create(anyLong(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Catches Exception
    }

    // --- PUT Tests ---

    @Test
    void handlePut_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        TotalPharmacy totalToUpdate = new TotalPharmacy(); totalToUpdate.setIdTotalPharmacy(1L);
        totalToUpdate.setTotal(new BigDecimal("750.00"));
        String requestJson = objectMapper.writeValueAsString(totalToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockTotalPharmacyDAO.update(any(TotalPharmacy.class))).thenReturn(totalToUpdate);
        String expectedJson = objectMapper.writeValueAsString(totalToUpdate);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        totalPharmacyHandler.handle(mockHttpExchange);

        verify(mockTotalPharmacyDAO).update(totalPharmacyCaptor.capture());
        assertEquals(totalToUpdate.getIdTotalPharmacy(), totalPharmacyCaptor.getValue().getIdTotalPharmacy());
        assertEquals(totalToUpdate.getTotal(), totalPharmacyCaptor.getValue().getTotal());
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handlePut_DaoUpdateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        TotalPharmacy totalToUpdate = new TotalPharmacy();
        String requestJson = objectMapper.writeValueAsString(totalToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockTotalPharmacyDAO.update(any(TotalPharmacy.class))).thenReturn(null);

        totalPharmacyHandler.handle(mockHttpExchange);

        verify(mockTotalPharmacyDAO).update(any(TotalPharmacy.class));
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