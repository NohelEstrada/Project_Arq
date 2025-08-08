package com.sources.app.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.EnsuranceAppointmentDAO;
import com.sources.app.entities.EnsuranceAppointment;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnsuranceAppointmentHandlerTest {

    @Mock
    private EnsuranceAppointmentDAO mockAppointmentDAO;
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
    private EnsuranceAppointmentHandler ensuranceAppointmentHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<EnsuranceAppointment> appointmentCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;
    @Captor
    ArgumentCaptor<Date> dateCaptor;

    private static final String API_ENDPOINT = "/api/ensurance-appointments";

    @BeforeEach
    void setUp() {
        lenient().when(mockHttpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockHttpExchange.getResponseBody()).thenReturn(mockResponseBody);
        lenient().when(mockHttpExchange.getRequestHeaders()).thenReturn(mockRequestHeaders);
        // Default URI for most tests, specific tests can override
        lenient().when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT)); 
    }

    @AfterEach
    void tearDown() throws IOException {
       // verify(mockResponseBody, atLeastOnce()).close(); // Add where applicable
    }

    // --- General Handling Tests ---

    @Test
    void handle_OptionsRequest_SendsNoContent() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        ensuranceAppointmentHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockAppointmentDAO);
    }

    @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PATCH"); // Unsupported
        ensuranceAppointmentHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<EnsuranceAppointment> appointments = Arrays.asList(new EnsuranceAppointment(), new EnsuranceAppointment());
        when(mockAppointmentDAO.findAll()).thenReturn(appointments);
        String expectedJson = objectMapper.writeValueAsString(appointments);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).findAll();
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindByHospitalId_Success() throws IOException {
        String hospitalId = "HOSP123";
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?hospitalId=" + hospitalId));
        EnsuranceAppointment appointment = new EnsuranceAppointment(); appointment.setHospitalAppointmentId(hospitalId);
        when(mockAppointmentDAO.findByHospitalAppointmentId(hospitalId)).thenReturn(appointment);
        String expectedJson = objectMapper.writeValueAsString(appointment);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).findByHospitalAppointmentId(hospitalId);
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindByHospitalId_NotFound() throws IOException {
        String hospitalId = "HOSP_NOT_FOUND";
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?hospitalId=" + hospitalId));
        when(mockAppointmentDAO.findByHospitalAppointmentId(hospitalId)).thenReturn(null);

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).findByHospitalAppointmentId(hospitalId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
    @Test
    void handleGet_FindByUserId_Success() throws IOException {
        Long userId = 50L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?userId=" + userId));
        List<EnsuranceAppointment> appointments = Collections.singletonList(new EnsuranceAppointment());
        when(mockAppointmentDAO.findByUserId(userId)).thenReturn(appointments);
        String expectedJson = objectMapper.writeValueAsString(appointments);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).findByUserId(userId);
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handleGet_FindByUserId_InvalidFormat() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?userId=abc"));

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO, never()).findByUserId(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    @Test
    void handleGet_FindToday_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?today=true"));
        List<EnsuranceAppointment> appointments = Collections.singletonList(new EnsuranceAppointment());
        when(mockAppointmentDAO.findTodayAppointments()).thenReturn(appointments);
        String expectedJson = objectMapper.writeValueAsString(appointments);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).findTodayAppointments();
        verifyResponseSent(200, expectedBytes);
    }
    
     @Test
    void handleGet_FindByDate_Success() throws IOException, java.text.ParseException {
        String dateStr = "2024-05-20";
        Date expectedDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?date=" + dateStr));
        List<EnsuranceAppointment> appointments = Collections.singletonList(new EnsuranceAppointment());
        when(mockAppointmentDAO.findByDate(any(Date.class))).thenReturn(appointments);
        String expectedJson = objectMapper.writeValueAsString(appointments);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).findByDate(dateCaptor.capture());
        // Assert dates are equal ignoring time part if format is just yyyy-MM-dd
        assertEquals(expectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(), 
                     dateCaptor.getValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handleGet_FindByDate_InvalidFormat() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?date=20-05-2024")); // Wrong format

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO, never()).findByDate(any());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L)); 
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String requestJson = "{\"hospitalAppointmentId\":\"H1\", \"idUser\":10, \"appointmentDate\":\"2024-06-01\", \"doctorName\":\"Dr.X\", \"reason\":\"R1\"}";
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        EnsuranceAppointment createdAppointment = new EnsuranceAppointment(); 
        createdAppointment.setIdAppointment(55L);
        when(mockAppointmentDAO.create(eq("H1"), eq(10L), any(Date.class), eq("Dr.X"), eq("R1"))).thenReturn(createdAppointment);
        String expectedJson = objectMapper.writeValueAsString(createdAppointment);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).create(eq("H1"), eq(10L), any(Date.class), eq("Dr.X"), eq("R1"));
        verifyResponseSent(201, expectedBytes);
    }
    
    @Test
    void handlePost_DaoCreateFails() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String requestJson = "{\"hospitalAppointmentId\":\"H1\", \"idUser\":10, \"appointmentDate\":\"2024-06-01\"}";
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockAppointmentDAO.create(anyString(), anyLong(), any(), anyString(), anyString())).thenReturn(null);

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).create(anyString(), anyLong(), any(), anyString(), anyString());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
     @Test
    void handlePost_InvalidJson() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"hospitalAppointmentId\": \"H1\", \"idUser\": BAD }";
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO, never()).create(anyString(), anyLong(), any(), anyString(), anyString());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- DELETE Tests ---

    @Test
    void handleDeleteByHospitalId_Success() throws IOException {
        String hospitalId = "HOSP_DEL_1";
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?hospitalId=" + hospitalId));
        when(mockAppointmentDAO.deleteByHospitalAppointmentId(hospitalId)).thenReturn(true);

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).deleteByHospitalAppointmentId(hospitalId);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
    }

    @Test
    void handleDeleteByHospitalId_NotFound() throws IOException {
        String hospitalId = "HOSP_DEL_NF";
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?hospitalId=" + hospitalId));
        when(mockAppointmentDAO.deleteByHospitalAppointmentId(hospitalId)).thenReturn(false);

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).deleteByHospitalAppointmentId(hospitalId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
    @Test
    void handleDeleteByHospitalId_MissingId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?otherParam=value")); // Missing hospitalId

        ensuranceAppointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO, never()).deleteByHospitalAppointmentId(anyString());
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