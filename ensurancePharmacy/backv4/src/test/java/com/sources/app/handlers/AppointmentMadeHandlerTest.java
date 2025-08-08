package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.AppointmentMadeDAO;
import com.sources.app.entities.AppointmentMade;
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
class AppointmentMadeHandlerTest {

    @Mock
    private AppointmentMadeDAO mockAppointmentMadeDAO;
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
    private AppointmentMadeHandler appointmentMadeHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<AppointmentMade> appointmentMadeCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/appointmentmade";

    @BeforeEach
    void setUp() {
        lenient().when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT));
        lenient().when(mockHttpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockHttpExchange.getResponseBody()).thenReturn(mockResponseBody);
        lenient().when(mockHttpExchange.getRequestHeaders()).thenReturn(mockRequestHeaders);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Verify close only if write was expected
        // verify(mockResponseBody, atLeastOnce()).close(); // Be careful with this
    }
    
     @Test
    void handle_OptionsRequest_SendsNoContent() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockAppointmentMadeDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/wrong/path"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockAppointmentMadeDAO);
    }
    
    @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE"); // Unsupported

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
        verifyNoInteractions(mockAppointmentMadeDAO);
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT)); // No query
        List<AppointmentMade> appointments = Arrays.asList(new AppointmentMade(), new AppointmentMade());
        when(mockAppointmentMadeDAO.findAll()).thenReturn(appointments);

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockAppointmentMadeDAO).findAll();
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), anyLong());
        verify(mockResponseBody).write(any(byte[].class));
        verify(mockResponseBody).close();
    }
    
    @Test
    void handleGet_FindAll_EmptyList() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT)); 
        List<AppointmentMade> emptyList = Collections.emptyList();
        when(mockAppointmentMadeDAO.findAll()).thenReturn(emptyList);
        String expectedJsonResponse = objectMapper.writeValueAsString(emptyList);
        byte[] expectedBytes = expectedJsonResponse.getBytes(StandardCharsets.UTF_8);

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockAppointmentMadeDAO).findAll();
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), eq((long)expectedBytes.length));
        verify(mockResponseBody).write(expectedBytes);
        verify(mockResponseBody).close();
    }

    @Test
    void handleGet_FindById_Success() throws IOException {
        Long testId = 5L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        AppointmentMade appointment = new AppointmentMade(); 
        appointment.setIdCita(testId); // Assuming ID matches idCita for this find
        when(mockAppointmentMadeDAO.findById(testId)).thenReturn(appointment);
        String expectedJsonResponse = objectMapper.writeValueAsString(appointment);
        byte[] expectedBytes = expectedJsonResponse.getBytes(StandardCharsets.UTF_8);

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockAppointmentMadeDAO).findById(testId);
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), eq((long)expectedBytes.length));
        verify(mockResponseBody).write(expectedBytes);
        verify(mockResponseBody).close();
    }
    
    @Test
    void handleGet_FindById_NotFound() throws IOException {
        Long testId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        when(mockAppointmentMadeDAO.findById(testId)).thenReturn(null);

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockAppointmentMadeDAO).findById(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verify(mockResponseBody, never()).write(any(byte[].class));
    }
    
    @Test
    void handleGet_FindById_InvalidIdFormat() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=abc"));

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockAppointmentMadeDAO, never()).findById(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        AppointmentMade requestApp = new AppointmentMade();
        requestApp.setIdCita(10L);
        requestApp.setIdUser(20L);
        requestApp.setAppointmentMadeDate(new Date());
        String requestJson = objectMapper.writeValueAsString(requestApp);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        AppointmentMade createdApp = new AppointmentMade(); // DAO returns created object
        when(mockAppointmentMadeDAO.create(eq(10L), eq(20L), any(Date.class))).thenReturn(createdApp);
        String expectedJsonResponse = objectMapper.writeValueAsString(createdApp);
        byte[] expectedBytes = expectedJsonResponse.getBytes(StandardCharsets.UTF_8);

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockAppointmentMadeDAO).create(eq(10L), eq(20L), any(Date.class));
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(201), eq((long)expectedBytes.length));
        verify(mockResponseBody).write(expectedBytes);
        verify(mockResponseBody).close();
    }
    
    @Test
    void handlePost_DaoCreateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        AppointmentMade requestApp = new AppointmentMade();
        requestApp.setIdCita(10L); requestApp.setIdUser(20L); requestApp.setAppointmentMadeDate(new Date());
        String requestJson = objectMapper.writeValueAsString(requestApp);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockAppointmentMadeDAO.create(anyLong(), anyLong(), any())).thenReturn(null);

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockAppointmentMadeDAO).create(anyLong(), anyLong(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
     @Test
    void handlePost_InvalidJson() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"idCita\":1, \"invalid\": } "; // Malformed - FIXED Closing quote
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockAppointmentMadeDAO, never()).create(anyLong(), anyLong(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }

    // --- PUT Tests ---

    @Test
    void handlePut_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        AppointmentMade appToUpdate = new AppointmentMade();
        appToUpdate.setIdCita(1L); // ID must be present for update usually
        appToUpdate.setIdUser(2L);
        appToUpdate.setAppointmentMadeDate(new Date());
        String requestJson = objectMapper.writeValueAsString(appToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockAppointmentMadeDAO.update(any(AppointmentMade.class))).thenReturn(appToUpdate);
        String expectedJsonResponse = objectMapper.writeValueAsString(appToUpdate);
        byte[] expectedBytes = expectedJsonResponse.getBytes(StandardCharsets.UTF_8);

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockAppointmentMadeDAO).update(appointmentMadeCaptor.capture());
        AppointmentMade capturedApp = appointmentMadeCaptor.getValue();
        assertEquals(appToUpdate.getIdCita(), capturedApp.getIdCita());
        assertEquals(appToUpdate.getIdUser(), capturedApp.getIdUser());
        // Compare dates formatted as strings due to ObjectMapper configuration
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(sdf.format(appToUpdate.getAppointmentMadeDate()), sdf.format(capturedApp.getAppointmentMadeDate()));
        
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), eq((long)expectedBytes.length));
        verify(mockResponseBody).write(expectedBytes);
        verify(mockResponseBody).close();
    }
    
     @Test
    void handlePut_DaoUpdateFails() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        AppointmentMade appToUpdate = new AppointmentMade();
        String requestJson = objectMapper.writeValueAsString(appToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockAppointmentMadeDAO.update(any(AppointmentMade.class))).thenReturn(null);

        appointmentMadeHandler.handle(mockHttpExchange);

        verify(mockAppointmentMadeDAO).update(any(AppointmentMade.class));
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
} 