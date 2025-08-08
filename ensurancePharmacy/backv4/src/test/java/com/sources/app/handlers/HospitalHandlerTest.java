package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.HospitalDAO;
import com.sources.app.entities.Hospital;
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
class HospitalHandlerTest {

    @Mock
    private HospitalDAO mockHospitalDAO;
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
    private HospitalHandler hospitalHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<Hospital> hospitalCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/hospital";

    @BeforeEach
    void setUp() {
        lenient().when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT));
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
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        hospitalHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockHospitalDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/hospitals")); // Incorrect path
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        hospitalHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockHospitalDAO);
    }
    
     @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE"); // Unsupported
        hospitalHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<Hospital> hospitals = Arrays.asList(new Hospital(), new Hospital());
        when(mockHospitalDAO.findAll()).thenReturn(hospitals);
        String expectedJson = objectMapper.writeValueAsString(hospitals);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        hospitalHandler.handle(mockHttpExchange);

        verify(mockHospitalDAO).findAll();
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindById_Success() throws IOException {
        Long testId = 10L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        Hospital hospital = new Hospital(); hospital.setIdHospital(testId);
        when(mockHospitalDAO.findById(testId)).thenReturn(hospital);
        String expectedJson = objectMapper.writeValueAsString(hospital);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        hospitalHandler.handle(mockHttpExchange);

        verify(mockHospitalDAO).findById(testId);
        verifyResponseSent(200, expectedBytes);
    }

     @Test
    void handleGet_FindById_NotFound() throws IOException {
        Long testId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        when(mockHospitalDAO.findById(testId)).thenReturn(null);

        hospitalHandler.handle(mockHttpExchange);

        verify(mockHospitalDAO).findById(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
    @Test
    void handleGet_FindById_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=invalid"));
        
        hospitalHandler.handle(mockHttpExchange);

        verify(mockHospitalDAO, never()).findById(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Hospital requestHospital = new Hospital();
        requestHospital.setName("New Hosp");
        requestHospital.setAddress("Addr");
        requestHospital.setPhone(123L);
        requestHospital.setEmail("e@mail.com");
        requestHospital.setEnabled(1);
        String requestJson = objectMapper.writeValueAsString(requestHospital);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        Hospital createdHospital = new Hospital(); createdHospital.setIdHospital(50L);
        when(mockHospitalDAO.create(anyString(), anyString(), anyLong(), anyString(), anyInt()))
                .thenReturn(createdHospital);
        String expectedJson = objectMapper.writeValueAsString(createdHospital);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        hospitalHandler.handle(mockHttpExchange);

        verify(mockHospitalDAO).create(eq("New Hosp"), eq("Addr"), eq(123L), eq("e@mail.com"), eq(1));
        verifyResponseSent(201, expectedBytes);
    }
    
    @Test
    void handlePost_DaoCreateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Hospital requestHospital = new Hospital(); 
        String requestJson = objectMapper.writeValueAsString(requestHospital);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockHospitalDAO.create(any(), any(), any(), any(), any())).thenReturn(null);

        hospitalHandler.handle(mockHttpExchange);

        verify(mockHospitalDAO).create(any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
    @Test
    void handlePost_InvalidJson() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"name\": \"Hosp\", \"phone\": wrong}";
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        hospitalHandler.handle(mockHttpExchange);

        verify(mockHospitalDAO, never()).create(any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Catches Exception
    }

    // --- PUT Tests ---

    @Test
    void handlePut_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Hospital hospitalToUpdate = new Hospital(); hospitalToUpdate.setIdHospital(1L);
        hospitalToUpdate.setName("Updated Hosp Name");
        String requestJson = objectMapper.writeValueAsString(hospitalToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockHospitalDAO.update(any(Hospital.class))).thenReturn(hospitalToUpdate);
        String expectedJson = objectMapper.writeValueAsString(hospitalToUpdate);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        hospitalHandler.handle(mockHttpExchange);

        verify(mockHospitalDAO).update(hospitalCaptor.capture());
        assertEquals(hospitalToUpdate.getIdHospital(), hospitalCaptor.getValue().getIdHospital());
        assertEquals(hospitalToUpdate.getName(), hospitalCaptor.getValue().getName());
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handlePut_DaoUpdateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Hospital hospitalToUpdate = new Hospital(); 
        String requestJson = objectMapper.writeValueAsString(hospitalToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockHospitalDAO.update(any(Hospital.class))).thenReturn(null);

        hospitalHandler.handle(mockHttpExchange);

        verify(mockHospitalDAO).update(any(Hospital.class));
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