package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.MedicineDAO;
import com.sources.app.entities.Medicine;
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
import java.math.BigDecimal;
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
class MedicineHandlerTest {

    @Mock
    private MedicineDAO mockMedicineDAO;
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
    private MedicineHandler medicineHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<Medicine> medicineCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/medicine";

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
        medicineHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockMedicineDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/meds")); // Wrong path
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        medicineHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockMedicineDAO);
    }
    
    @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE"); // Unsupported
        medicineHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<Medicine> medicines = Arrays.asList(new Medicine(), new Medicine());
        when(mockMedicineDAO.findAll()).thenReturn(medicines);
        String expectedJson = objectMapper.writeValueAsString(medicines);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        medicineHandler.handle(mockHttpExchange);

        verify(mockMedicineDAO).findAll();
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindById_Success() throws IOException {
        Long testId = 10L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        Medicine medicine = new Medicine(); medicine.setIdMedicine(testId);
        when(mockMedicineDAO.findById(testId)).thenReturn(medicine);
        String expectedJson = objectMapper.writeValueAsString(medicine);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        medicineHandler.handle(mockHttpExchange);

        verify(mockMedicineDAO).findById(testId);
        verifyResponseSent(200, expectedBytes);
    }

     @Test
    void handleGet_FindById_NotFound() throws IOException {
        Long testId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=" + testId));
        when(mockMedicineDAO.findById(testId)).thenReturn(null);

        medicineHandler.handle(mockHttpExchange);

        verify(mockMedicineDAO).findById(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handleGet_FindById_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=abc"));
        
        medicineHandler.handle(mockHttpExchange);

        verify(mockMedicineDAO, never()).findById(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Pharmacy pharmacy = new Pharmacy(); pharmacy.setIdPharmacy(1L); // Need Pharmacy object in request
        Medicine requestMedicine = new Medicine();
        requestMedicine.setName("Test Med");
        requestMedicine.setDescription("Desc");
        requestMedicine.setPrice(BigDecimal.TEN);
        requestMedicine.setPharmacy(pharmacy);
        requestMedicine.setEnabled(1);
        requestMedicine.setCoverage(1);
        String requestJson = objectMapper.writeValueAsString(requestMedicine);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        Medicine createdMedicine = new Medicine(); createdMedicine.setIdMedicine(50L);
        // Mock the create call with specific primitive/wrapper args and the Pharmacy object
        when(mockMedicineDAO.create(
            eq(requestMedicine.getName()), eq(requestMedicine.getDescription()), eq(requestMedicine.getPrice()), 
            eq(pharmacy), // Match the mocked Pharmacy object
            eq(requestMedicine.getEnabled()), eq(requestMedicine.getActivePrinciple()), 
            eq(requestMedicine.getPresentation()), eq(requestMedicine.getStock()), 
            eq(requestMedicine.getBrand()), eq(requestMedicine.getCoverage())
        )).thenReturn(createdMedicine);
        
        String expectedJson = objectMapper.writeValueAsString(createdMedicine);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        medicineHandler.handle(mockHttpExchange);

        verify(mockMedicineDAO).create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verifyResponseSent(201, expectedBytes);
    }
    
    @Test
    void handlePost_DaoCreateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Medicine requestMedicine = new Medicine(); // Assume valid data
        requestMedicine.setPharmacy(new Pharmacy()); // Need a pharmacy object
        String requestJson = objectMapper.writeValueAsString(requestMedicine);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockMedicineDAO.create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(null);

        medicineHandler.handle(mockHttpExchange);

        verify(mockMedicineDAO).create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
     @Test
    void handlePost_InvalidJson() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"name\": \"Test Med\", \"price\": wrong}";
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        medicineHandler.handle(mockHttpExchange);

        verify(mockMedicineDAO, never()).create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Catches Exception
    }

    // --- PUT Tests ---

    @Test
    void handlePut_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Medicine medicineToUpdate = new Medicine(); medicineToUpdate.setIdMedicine(1L);
        medicineToUpdate.setName("Updated Med Name");
        String requestJson = objectMapper.writeValueAsString(medicineToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockMedicineDAO.update(any(Medicine.class))).thenReturn(medicineToUpdate);
        String expectedJson = objectMapper.writeValueAsString(medicineToUpdate);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        medicineHandler.handle(mockHttpExchange);

        verify(mockMedicineDAO).update(medicineCaptor.capture());
        assertEquals(medicineToUpdate.getIdMedicine(), medicineCaptor.getValue().getIdMedicine());
        assertEquals(medicineToUpdate.getName(), medicineCaptor.getValue().getName());
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handlePut_DaoUpdateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Medicine medicineToUpdate = new Medicine();
        String requestJson = objectMapper.writeValueAsString(medicineToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockMedicineDAO.update(any(Medicine.class))).thenReturn(null);

        medicineHandler.handle(mockHttpExchange);

        verify(mockMedicineDAO).update(any(Medicine.class));
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