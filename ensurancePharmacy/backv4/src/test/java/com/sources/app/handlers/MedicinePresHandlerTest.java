package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.MedicinePresDAO;
import com.sources.app.entities.Medicine;
import com.sources.app.entities.MedicinePres;
import com.sources.app.entities.Prescription;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicinePresHandlerTest {

    @Mock
    private MedicinePresDAO mockMedicinePresDAO;
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
    private MedicinePresHandler medicinePresHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<MedicinePres> medicinePresCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/medicinepres";

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
        medicinePresHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockMedicinePresDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/wrong"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        medicinePresHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockMedicinePresDAO);
    }
    
     @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE"); // Unsupported
        medicinePresHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<MedicinePres> relations = Arrays.asList(new MedicinePres(), new MedicinePres());
        when(mockMedicinePresDAO.findAll()).thenReturn(relations);
        String expectedJson = objectMapper.writeValueAsString(relations);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        medicinePresHandler.handle(mockHttpExchange);

        verify(mockMedicinePresDAO).findAll();
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindById_Success() throws IOException {
        Long presId = 10L;
        Long medId = 20L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?idPrescription=" + presId + "&idMedicine=" + medId));
        MedicinePres relation = new MedicinePres(); // Assume IDs are set implicitly via composite key
        when(mockMedicinePresDAO.findById(presId, medId)).thenReturn(relation);
        String expectedJson = objectMapper.writeValueAsString(relation);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        medicinePresHandler.handle(mockHttpExchange);

        verify(mockMedicinePresDAO).findById(presId, medId);
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handleGet_FindById_MissingParam() throws IOException {
        Long presId = 10L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        // Missing idMedicine param
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?idPrescription=" + presId)); 
        // Since findById isn't called, it falls through to findAll
        List<MedicinePres> relations = Collections.singletonList(new MedicinePres());
        when(mockMedicinePresDAO.findAll()).thenReturn(relations);
        String expectedJson = objectMapper.writeValueAsString(relations);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        medicinePresHandler.handle(mockHttpExchange);

        verify(mockMedicinePresDAO, never()).findById(anyLong(), anyLong());
        verify(mockMedicinePresDAO).findAll();
        verifyResponseSent(200, expectedBytes); 
    }

     @Test
    void handleGet_FindById_NotFound() throws IOException {
        Long presId = 99L;
        Long medId = 98L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?idPrescription=" + presId + "&idMedicine=" + medId));
        when(mockMedicinePresDAO.findById(presId, medId)).thenReturn(null);

        medicinePresHandler.handle(mockHttpExchange);

        verify(mockMedicinePresDAO).findById(presId, medId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handleGet_FindById_InvalidIdFormat() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?idPrescription=abc&idMedicine=123"));
        
        medicinePresHandler.handle(mockHttpExchange);

        verify(mockMedicinePresDAO, never()).findById(anyLong(), anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Long presId = 1L; Long medId = 2L;
        // Request body needs nested objects with IDs
        String requestJson = String.format(
            "{\"prescription\":{\"idPrescription\":%d}, \"medicine\":{\"idMedicine\":%d}}", 
            presId, medId
        );
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        MedicinePres createdRelation = new MedicinePres(); // DAO returns created object
        when(mockMedicinePresDAO.create(eq(presId), eq(medId))).thenReturn(createdRelation);
        String expectedJson = objectMapper.writeValueAsString(createdRelation);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        medicinePresHandler.handle(mockHttpExchange);

        verify(mockMedicinePresDAO).create(eq(presId), eq(medId));
        verifyResponseSent(201, expectedBytes);
    }
    
     @Test
    void handlePost_MissingPrescriptionId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
         String requestJson = "{\"medicine\":{\"idMedicine\":2}}"; // Missing prescription
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        
        medicinePresHandler.handle(mockHttpExchange);

        verify(mockMedicinePresDAO, never()).create(anyLong(), anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L)); 
    }
    
     @Test
    void handlePost_DaoCreateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String requestJson = "{\"prescription\":{\"idPrescription\":1}, \"medicine\":{\"idMedicine\":2}}";
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockMedicinePresDAO.create(eq(1L), eq(2L))).thenReturn(null); // Simulate failure

        medicinePresHandler.handle(mockHttpExchange);

        verify(mockMedicinePresDAO).create(eq(1L), eq(2L));
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
    @Test
    void handlePost_InvalidJson() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"prescription\":{\"idPrescription\":1}, invalid}";
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        medicinePresHandler.handle(mockHttpExchange);

        verify(mockMedicinePresDAO, never()).create(anyLong(), anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Generic exception catch
    }

    // --- PUT Tests ---

    @Test
    void handlePut_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        // PUT usually updates based on existing composite key implicitly present in the object
        MedicinePres relationToUpdate = new MedicinePres(); 
        Prescription p = new Prescription(); p.setIdPrescription(1L);
        Medicine m = new Medicine(); m.setIdMedicine(2L);
        relationToUpdate.setPrescription(p); 
        relationToUpdate.setMedicine(m);
        // Add any other fields that might be updatable if the entity had more
        
        String requestJson = objectMapper.writeValueAsString(relationToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockMedicinePresDAO.update(any(MedicinePres.class))).thenReturn(relationToUpdate);
        String expectedJson = objectMapper.writeValueAsString(relationToUpdate);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        medicinePresHandler.handle(mockHttpExchange);

        verify(mockMedicinePresDAO).update(medicinePresCaptor.capture());
        // Verify the captured object has the expected state (IDs from the JSON)
        assertEquals(p.getIdPrescription(), medicinePresCaptor.getValue().getPrescription().getIdPrescription());
        assertEquals(m.getIdMedicine(), medicinePresCaptor.getValue().getMedicine().getIdMedicine());
        
        verifyResponseSent(200, expectedBytes);
    }
    
     @Test
    void handlePut_DaoUpdateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        MedicinePres relationToUpdate = new MedicinePres();
        String requestJson = objectMapper.writeValueAsString(relationToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockMedicinePresDAO.update(any(MedicinePres.class))).thenReturn(null);

        medicinePresHandler.handle(mockHttpExchange);

        verify(mockMedicinePresDAO).update(any(MedicinePres.class));
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }

     // Helper method to verify JSON response sending
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