package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.TotalHospitalDAO;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.TotalHospital;
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
class TotalHospitalHandlerTest {

    @Mock
    private TotalHospitalDAO mockTotalHospitalDAO;
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
    private TotalHospitalHandler totalHospitalHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<TotalHospital> totalHospitalCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/totalhospital";

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
        totalHospitalHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockTotalHospitalDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/wrong"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        totalHospitalHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockTotalHospitalDAO);
    }
    
     @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE"); // Unsupported
        totalHospitalHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<TotalHospital> totals = Arrays.asList(new TotalHospital(), new TotalHospital());
        when(mockTotalHospitalDAO.findAll()).thenReturn(totals);
        String expectedJson = objectMapper.writeValueAsString(totals);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        totalHospitalHandler.handle(mockHttpExchange);

        verify(mockTotalHospitalDAO).findAll();
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindById_Success() throws IOException {
        Long testId = 1L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        TotalHospital totalHospital = new TotalHospital(); totalHospital.setIdTotalHospital(testId);
        when(mockTotalHospitalDAO.findById(testId)).thenReturn(totalHospital);
        String expectedJson = objectMapper.writeValueAsString(totalHospital);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        totalHospitalHandler.handle(mockHttpExchange);

        verify(mockTotalHospitalDAO).findById(testId);
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindById_NotFound() throws IOException {
        Long testId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        when(mockTotalHospitalDAO.findById(testId)).thenReturn(null);

        totalHospitalHandler.handle(mockHttpExchange);

        verify(mockTotalHospitalDAO).findById(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
    @Test
    void handleGet_FindById_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=abc"));
        
        totalHospitalHandler.handle(mockHttpExchange);

        verify(mockTotalHospitalDAO, never()).findById(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Long hospitalId = 1L;
        Hospital hospital = new Hospital(); hospital.setIdHospital(hospitalId);
        TotalHospital requestTotal = new TotalHospital();
        requestTotal.setHospital(hospital);
        requestTotal.setTotalDate(new Date());
        requestTotal.setTotal(new BigDecimal("1000.00"));
        String requestJson = objectMapper.writeValueAsString(requestTotal);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        TotalHospital createdTotal = new TotalHospital(); createdTotal.setIdTotalHospital(50L);
        when(mockTotalHospitalDAO.create(anyLong(), any(Date.class), any(BigDecimal.class)))
               .thenReturn(createdTotal);
        String expectedJson = objectMapper.writeValueAsString(createdTotal);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        totalHospitalHandler.handle(mockHttpExchange);

        verify(mockTotalHospitalDAO).create(eq(hospitalId), any(Date.class), eq(requestTotal.getTotal()));
        verifyResponseSent(201, expectedBytes);
    }
    
    @Test
    void handlePost_MissingHospitalId_SendsBadRequest() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        TotalHospital requestTotal = new TotalHospital(); // Missing hospital
        requestTotal.setTotalDate(new Date());
        requestTotal.setTotal(BigDecimal.TEN);
        String requestJson = objectMapper.writeValueAsString(requestTotal);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        totalHospitalHandler.handle(mockHttpExchange);

        verify(mockTotalHospitalDAO, never()).create(anyLong(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }
    
     @Test
    void handlePost_DaoCreateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Hospital hospital = new Hospital(); hospital.setIdHospital(1L);
        TotalHospital requestTotal = new TotalHospital(); 
        requestTotal.setHospital(hospital); requestTotal.setTotalDate(new Date()); requestTotal.setTotal(BigDecimal.ONE);
        String requestJson = objectMapper.writeValueAsString(requestTotal);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockTotalHospitalDAO.create(anyLong(), any(), any())).thenReturn(null);

        totalHospitalHandler.handle(mockHttpExchange);

        verify(mockTotalHospitalDAO).create(anyLong(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
     @Test
    void handlePost_InvalidJson() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"total\": invalid}";
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        totalHospitalHandler.handle(mockHttpExchange);

        verify(mockTotalHospitalDAO, never()).create(anyLong(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Catches Exception
    }

    // --- PUT Tests ---

    @Test
    void handlePut_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        TotalHospital totalToUpdate = new TotalHospital(); totalToUpdate.setIdTotalHospital(1L);
        totalToUpdate.setTotal(new BigDecimal("2000.00"));
        String requestJson = objectMapper.writeValueAsString(totalToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockTotalHospitalDAO.update(any(TotalHospital.class))).thenReturn(totalToUpdate);
        String expectedJson = objectMapper.writeValueAsString(totalToUpdate);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        totalHospitalHandler.handle(mockHttpExchange);

        verify(mockTotalHospitalDAO).update(totalHospitalCaptor.capture());
        assertEquals(totalToUpdate.getIdTotalHospital(), totalHospitalCaptor.getValue().getIdTotalHospital());
        assertEquals(totalToUpdate.getTotal(), totalHospitalCaptor.getValue().getTotal());
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handlePut_DaoUpdateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        TotalHospital totalToUpdate = new TotalHospital();
        String requestJson = objectMapper.writeValueAsString(totalToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockTotalHospitalDAO.update(any(TotalHospital.class))).thenReturn(null);

        totalHospitalHandler.handle(mockHttpExchange);

        verify(mockTotalHospitalDAO).update(any(TotalHospital.class));
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