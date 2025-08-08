package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.AppointmentDAO;
import com.sources.app.entities.Appointment;
import com.sources.app.entities.Hospital;
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
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
class AppointmentHandlerTest {

    @Mock
    private AppointmentDAO mockAppointmentDAO;
    @Mock
    private HttpExchange mockHttpExchange;
    @Mock
    private Headers mockRequestHeaders;
    @Mock
    private Headers mockResponseHeaders;
    @Mock
    private OutputStream mockResponseBody;

    // Use a real ObjectMapper configured similarly to the handler
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

    @InjectMocks
    private AppointmentHandler appointmentHandler;

    // Argument captors
    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<Appointment> appointmentCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/appointment";

    @BeforeEach
    void setUp() {
        // Reset mocks if necessary (though typically handled by MockitoExtension)
        // Mock basic exchange methods used in almost all handle() calls
        lenient().when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT));
        lenient().when(mockHttpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockHttpExchange.getResponseBody()).thenReturn(mockResponseBody);
        lenient().when(mockHttpExchange.getRequestHeaders()).thenReturn(mockRequestHeaders);
    }

    @AfterEach
    void tearDown() {
        // Verify response body stream was closed if opened
        // In tests where it's mocked and used, verify(mockResponseBody).close();
    }

    @Test
    void handle_OptionsRequest_SendsNoContent() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");

        appointmentHandler.handle(mockHttpExchange);

        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verify(mockResponseHeaders).add(eq("Access-Control-Allow-Origin"), eq("*"));
        verify(mockResponseHeaders).add(eq("Access-Control-Allow-Methods"), anyString());
        verify(mockResponseHeaders).add(eq("Access-Control-Allow-Headers"), anyString());
        verifyNoInteractions(mockAppointmentDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/wrong/path"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");

        appointmentHandler.handle(mockHttpExchange);

        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockAppointmentDAO);
        // CORS headers are added before path check
        verify(mockResponseHeaders).add(eq("Access-Control-Allow-Origin"), eq("*"));
    }

    @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PATCH"); // Unsupported

        appointmentHandler.handle(mockHttpExchange);

        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
        verifyNoInteractions(mockAppointmentDAO);
        verify(mockResponseHeaders).add(eq("Access-Control-Allow-Origin"), eq("*"));
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT)); // No query params
        List<Appointment> appointments = Arrays.asList(new Appointment(), new Appointment());
        when(mockAppointmentDAO.findAll()).thenReturn(appointments);

        appointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).findAll();
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(statusCodeCaptor.capture(), responseLengthCaptor.capture());
        verify(mockResponseBody).write(responseBodyCaptor.capture());

        assertEquals(200, statusCodeCaptor.getValue());
        assertTrue(responseLengthCaptor.getValue() > 0);
        String jsonResponse = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        // Basic check, could use JSONAssert for detailed comparison
        assertTrue(jsonResponse.startsWith("[")); 
        assertTrue(jsonResponse.endsWith("]"));
        verify(mockResponseBody).close();
    }

    @Test
    void handleGet_FindById_Success() throws IOException {
        Long testId = 5L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        Appointment appointment = new Appointment();
        appointment.setIdAppointment(testId);
        when(mockAppointmentDAO.findById(testId)).thenReturn(appointment);

        appointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).findById(testId);
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String jsonResponse = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(jsonResponse.contains("\"idAppointment\":" + testId));
         verify(mockResponseBody).close();
    }
    
     @Test
    void handleGet_FindById_NotFound() throws IOException {
        Long testId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        when(mockAppointmentDAO.findById(testId)).thenReturn(null); // Not found

        appointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).findById(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verify(mockResponseBody, never()).write(any(byte[].class));
    }
    
     @Test
    void handleGet_FindById_InvalidIdFormat() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=abc"));

        appointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO, never()).findById(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
         verify(mockResponseBody, never()).write(any(byte[].class));
    }
    
     @Test
    void handleGet_FindByUserId_Success() throws IOException {
        Long userId = 10L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?user_id=" + userId));
        List<Appointment> appointments = Collections.singletonList(new Appointment());
        when(mockAppointmentDAO.findByUserId(userId)).thenReturn(appointments);

        appointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).findByUserId(userId);
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), anyLong());
        verify(mockResponseBody).write(any(byte[].class));
        verify(mockResponseBody).close();
    }
    
    @Test
    void handleGet_FindByUserId_InvalidIdFormat() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?user_id=xyz"));

        appointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO, never()).findByUserId(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
         verify(mockResponseBody, never()).write(any(byte[].class));
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        
        // Prepare request body
        Long hospitalId = 1L;
        Long userId = 2L;
        Hospital hospital = new Hospital(); hospital.setIdHospital(hospitalId);
        User user = new User(); user.setIdUser(userId);
        Appointment requestAppointment = new Appointment();
        requestAppointment.setHospital(hospital);
        requestAppointment.setUser(user);
        requestAppointment.setAppointmentDate(new Date());
        requestAppointment.setEnabled(1);
        String requestJson = objectMapper.writeValueAsString(requestAppointment);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        // Mock DAO response
        Appointment createdAppointment = new Appointment();
        createdAppointment.setIdAppointment(99L);
        createdAppointment.setHospital(hospital);
        createdAppointment.setUser(user);
        createdAppointment.setAppointmentDate(requestAppointment.getAppointmentDate());
        createdAppointment.setEnabled(requestAppointment.getEnabled());
        when(mockAppointmentDAO.create(eq(hospitalId), eq(userId), any(Date.class), eq(1))).thenReturn(createdAppointment);

        appointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).create(eq(hospitalId), eq(userId), any(Date.class), eq(requestAppointment.getEnabled()));
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(201), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String responseJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(responseJson.contains("\"idAppointment\":99"));
        verify(mockResponseBody).close();
    }
    
     @Test
    void handlePost_MissingHospitalInBody() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Appointment requestAppointment = new Appointment();
        requestAppointment.setUser(new User()); // Missing hospital
        requestAppointment.setAppointmentDate(new Date());
        requestAppointment.setEnabled(1);
        String requestJson = objectMapper.writeValueAsString(requestAppointment);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        
        appointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO, never()).create(anyLong(), anyLong(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }
    
     @Test
    void handlePost_DaoCreateFails() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Hospital hospital = new Hospital(); hospital.setIdHospital(1L);
        User user = new User(); user.setIdUser(2L);
        Appointment requestAppointment = new Appointment();
        requestAppointment.setHospital(hospital);
        requestAppointment.setUser(user);
        requestAppointment.setAppointmentDate(new Date());
        requestAppointment.setEnabled(1);
        String requestJson = objectMapper.writeValueAsString(requestAppointment);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        
        // Simulate DAO returning null
        when(mockAppointmentDAO.create(anyLong(), anyLong(), any(), any())).thenReturn(null);
        
        appointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).create(anyLong(), anyLong(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
    @Test
    void handlePost_JsonParseException() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"hospital\":{\"idHospital\":1}, \"user\":{\"idUser\":2}, \"invalidField\":\"abc\" }"; // Malformed JSON - FIXED Closing quote
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        appointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO, never()).create(anyLong(), anyLong(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Or 400 depending on desired handling
    }

    // --- PUT Tests ---

    @Test
    void handlePut_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        
        Appointment appointmentToUpdate = new Appointment();
        appointmentToUpdate.setIdAppointment(1L);
        appointmentToUpdate.setEnabled(0); // Example update
        // Assume other fields are set as needed for update
        String requestJson = objectMapper.writeValueAsString(appointmentToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockAppointmentDAO.update(any(Appointment.class))).thenReturn(appointmentToUpdate);

        appointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).update(appointmentCaptor.capture());
        Appointment capturedAppointment = appointmentCaptor.getValue();
        assertEquals(appointmentToUpdate.getIdAppointment(), capturedAppointment.getIdAppointment());
        assertEquals(appointmentToUpdate.getEnabled(), capturedAppointment.getEnabled());
        
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), anyLong());
        verify(mockResponseBody).write(any(byte[].class));
        verify(mockResponseBody).close();
    }
    
     @Test
    void handlePut_DaoUpdateFails() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Appointment appointmentToUpdate = new Appointment();
         String requestJson = objectMapper.writeValueAsString(appointmentToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockAppointmentDAO.update(any(Appointment.class))).thenReturn(null); // Simulate failure

        appointmentHandler.handle(mockHttpExchange);

        verify(mockAppointmentDAO).update(any(Appointment.class));
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }

} 