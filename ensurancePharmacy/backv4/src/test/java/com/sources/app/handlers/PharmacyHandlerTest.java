package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.PharmacyDAO;
import com.sources.app.entities.Pharmacy;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PharmacyHandlerTest {

    @Mock
    private PharmacyDAO mockPharmacyDAO;
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
    private PharmacyHandler pharmacyHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<Pharmacy> pharmacyCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/pharmacy";

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
        pharmacyHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockPharmacyDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/pharmacies")); // Wrong path
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        pharmacyHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockPharmacyDAO);
    }
    
     @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE"); // Unsupported
        pharmacyHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<Pharmacy> pharmacies = Arrays.asList(new Pharmacy(), new Pharmacy());
        when(mockPharmacyDAO.findAll()).thenReturn(pharmacies);
        String expectedJson = objectMapper.writeValueAsString(pharmacies);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        pharmacyHandler.handle(mockHttpExchange);

        verify(mockPharmacyDAO).findAll();
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindById_Success() throws IOException {
        Long testId = 1L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        Pharmacy pharmacy = new Pharmacy(); pharmacy.setIdPharmacy(testId);
        when(mockPharmacyDAO.findById(testId)).thenReturn(pharmacy);
        String expectedJson = objectMapper.writeValueAsString(pharmacy);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        pharmacyHandler.handle(mockHttpExchange);

        verify(mockPharmacyDAO).findById(testId);
        verifyResponseSent(200, expectedBytes);
    }

     @Test
    void handleGet_FindById_NotFound() throws IOException {
        Long testId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        when(mockPharmacyDAO.findById(testId)).thenReturn(null);

        pharmacyHandler.handle(mockHttpExchange);

        verify(mockPharmacyDAO).findById(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handleGet_FindById_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=xyz"));
        
        pharmacyHandler.handle(mockHttpExchange);

        verify(mockPharmacyDAO, never()).findById(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Pharmacy requestPharmacy = new Pharmacy();
        requestPharmacy.setName("Corner Drug");
        requestPharmacy.setAddress("1 Main St");
        requestPharmacy.setPhone(12345L);
        requestPharmacy.setEmail("c@d.com");
        requestPharmacy.setEnabled(1);
        String requestJson = objectMapper.writeValueAsString(requestPharmacy);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        Pharmacy createdPharmacy = new Pharmacy(); createdPharmacy.setIdPharmacy(50L);
        when(mockPharmacyDAO.create(anyString(), anyString(), anyLong(), anyString(), anyInt()))
               .thenReturn(createdPharmacy);
        String expectedJson = objectMapper.writeValueAsString(createdPharmacy);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        pharmacyHandler.handle(mockHttpExchange);

        verify(mockPharmacyDAO).create(eq("Corner Drug"), eq("1 Main St"), eq(12345L), eq("c@d.com"), eq(1));
        verifyResponseSent(201, expectedBytes);
    }
    
    @Test
    void handlePost_DaoCreateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Pharmacy requestPharmacy = new Pharmacy(); // Assume valid data
        String requestJson = objectMapper.writeValueAsString(requestPharmacy);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockPharmacyDAO.create(any(), any(), any(), any(), any())).thenReturn(null);

        pharmacyHandler.handle(mockHttpExchange);

        verify(mockPharmacyDAO).create(any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
     @Test
    void handlePost_InvalidJson() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"name\": \"Test\", \"phone\": wrong}";
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        pharmacyHandler.handle(mockHttpExchange);

        verify(mockPharmacyDAO, never()).create(any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Catches Exception
    }

    // --- PUT Tests ---

    @Test
    void handlePut_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Pharmacy pharmacyToUpdate = new Pharmacy(); pharmacyToUpdate.setIdPharmacy(1L);
        pharmacyToUpdate.setName("Updated Name");
        String requestJson = objectMapper.writeValueAsString(pharmacyToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockPharmacyDAO.update(any(Pharmacy.class))).thenReturn(pharmacyToUpdate);
        String expectedJson = objectMapper.writeValueAsString(pharmacyToUpdate);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        pharmacyHandler.handle(mockHttpExchange);

        verify(mockPharmacyDAO).update(pharmacyCaptor.capture());
        assertEquals(pharmacyToUpdate.getIdPharmacy(), pharmacyCaptor.getValue().getIdPharmacy());
        assertEquals(pharmacyToUpdate.getName(), pharmacyCaptor.getValue().getName());
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handlePut_DaoUpdateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Pharmacy pharmacyToUpdate = new Pharmacy();
        String requestJson = objectMapper.writeValueAsString(pharmacyToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockPharmacyDAO.update(any(Pharmacy.class))).thenReturn(null);

        pharmacyHandler.handle(mockHttpExchange);

        verify(mockPharmacyDAO).update(any(Pharmacy.class));
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