package com.sources.app.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.ServiceApprovalDAO;
import com.sources.app.entities.ServiceApproval;
import com.sources.app.util.HibernateUtil; // Used for CORS headers
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PharmacyInsuranceHandlerTest {

    @Mock
    private ServiceApprovalDAO mockServiceApprovalDAO;
    @Mock
    private HttpExchange mockHttpExchange;
    @Mock
    private Headers mockRequestHeaders; // Although not used by this handler logic
    @Mock
    private Headers mockResponseHeaders;
    @Mock
    private OutputStream mockResponseBody;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PharmacyInsuranceHandler pharmacyInsuranceHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;
    @Captor
    ArgumentCaptor<ServiceApproval> serviceApprovalCaptor;

    private static final String VALIDATE_ENDPOINT = "/api/pharmacy-insurance/validate-prescription";
    private static final String CHECK_COVERAGE_ENDPOINT_BASE = "/api/pharmacy-insurance/check-coverage/";

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @BeforeEach
    void setUp() {
        // Mock static HibernateUtil.setCorsHeaders if needed, or assume it works
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(() -> HibernateUtil.setCorsHeaders(any(HttpExchange.class)))
                           .thenAnswer(invocation -> null); 
        
        lenient().when(mockHttpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockHttpExchange.getResponseBody()).thenReturn(mockResponseBody);
        lenient().when(mockHttpExchange.getRequestHeaders()).thenReturn(mockRequestHeaders);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockedHibernateUtil.close();
        // verify(mockResponseBody, atLeastOnce()).close(); // Add if needed
    }
    
     @Test
    void handle_OptionsRequest_SendsNoContent() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(VALIDATE_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        pharmacyInsuranceHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockServiceApprovalDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/wrong"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        pharmacyInsuranceHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handle_WrongMethodForValidate_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(VALIDATE_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET"); 
        pharmacyInsuranceHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }
    
     @Test
    void handle_WrongMethodForCheckCoverage_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(CHECK_COVERAGE_ENDPOINT_BASE + "123"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST"); 
        pharmacyInsuranceHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- POST /validate-prescription Tests ---

    @Test
    void handleValidatePrescription_Success() throws IOException {
        String approvalCode = "AP123";
        Long prescriptionId = 50L;
        Double totalAmount = 100.0;
        Map<String, Object> requestMap = Map.of("approvalCode", approvalCode, "prescriptionId", prescriptionId, "totalAmount", totalAmount);
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(VALIDATE_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        ServiceApproval existingApproval = new ServiceApproval();
        existingApproval.setApprovalCode(approvalCode);
        existingApproval.setStatus("APPROVED"); // Must be approved
        existingApproval.setServiceCost(0.0); // Initialize amounts
        existingApproval.setCoveredAmount(0.0);
        existingApproval.setPatientAmount(0.0);
        when(mockServiceApprovalDAO.findByApprovalCode(approvalCode)).thenReturn(existingApproval);
        // Mock the update call to return the modified approval
        when(mockServiceApprovalDAO.update(any(ServiceApproval.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pharmacyInsuranceHandler.handle(mockHttpExchange);

        verify(mockServiceApprovalDAO).findByApprovalCode(approvalCode);
        verify(mockServiceApprovalDAO).update(serviceApprovalCaptor.capture());
        ServiceApproval capturedApproval = serviceApprovalCaptor.getValue();
        assertEquals(prescriptionId, capturedApproval.getPrescriptionId());
        assertEquals(totalAmount, capturedApproval.getPrescriptionTotal());
        // Assuming 70% coverage
        assertEquals(totalAmount * 0.7, capturedApproval.getCoveredAmount());
        assertEquals(totalAmount * 0.3, capturedApproval.getPatientAmount(), 0.001); // Allow for double precision issues

        verify(mockHttpExchange).sendResponseHeaders(eq(200), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String responseJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        Map<String, Object> responseMap = objectMapper.readValue(responseJson, new TypeReference<Map<String, Object>>() {});
        assertEquals(true, responseMap.get("success"));
        assertEquals(approvalCode, responseMap.get("approvalCode"));
        assertEquals(prescriptionId.intValue(), responseMap.get("prescriptionId")); // Jackson might deserialize Long as Integer
        assertEquals(totalAmount * 0.7, responseMap.get("coveredAmount"));
        assertEquals(totalAmount * 0.3, (Double)responseMap.get("patientAmount"), 0.001);
        verify(mockResponseBody).close();
    }
    
    @Test
    void handleValidatePrescription_MissingData_SendsBadRequest() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(VALIDATE_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String requestJson = "{\"approvalCode\":\"AP123\", \"totalAmount\":100.0}"; // Missing prescriptionId
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        pharmacyInsuranceHandler.handle(mockHttpExchange);

        verify(mockHttpExchange).sendResponseHeaders(eq(400), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("Faltan campos requeridos"));
        verifyNoInteractions(mockServiceApprovalDAO);
    }
    
     @Test
    void handleValidatePrescription_ApprovalNotFound() throws IOException {
        String approvalCode = "AP_NOTFOUND";
        Map<String, Object> requestMap = Map.of("approvalCode", approvalCode, "prescriptionId", 1L, "totalAmount", 100.0);
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(VALIDATE_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockServiceApprovalDAO.findByApprovalCode(approvalCode)).thenReturn(null);

        pharmacyInsuranceHandler.handle(mockHttpExchange);

        verify(mockServiceApprovalDAO).findByApprovalCode(approvalCode);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("Código de aprobación no encontrado"));
    }
    
    @Test
    void handleValidatePrescription_ApprovalNotApprovedStatus() throws IOException {
        String approvalCode = "AP_PENDING";
        Map<String, Object> requestMap = Map.of("approvalCode", approvalCode, "prescriptionId", 1L, "totalAmount", 100.0);
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        ServiceApproval pendingApproval = new ServiceApproval(); pendingApproval.setStatus("PENDING");
        
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(VALIDATE_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockServiceApprovalDAO.findByApprovalCode(approvalCode)).thenReturn(pendingApproval);

        pharmacyInsuranceHandler.handle(mockHttpExchange);

        verify(mockServiceApprovalDAO).findByApprovalCode(approvalCode);
        verify(mockHttpExchange).sendResponseHeaders(eq(400), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("El servicio no está en estado aprobado"));
    }
    
    @Test
    void handleValidatePrescription_AlreadyHasPrescription() throws IOException {
        String approvalCode = "AP_USED";
        Map<String, Object> requestMap = Map.of("approvalCode", approvalCode, "prescriptionId", 1L, "totalAmount", 100.0);
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        ServiceApproval usedApproval = new ServiceApproval(); 
        usedApproval.setStatus("APPROVED");
        usedApproval.setPrescriptionId(99L); // Already has a prescription ID
        
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(VALIDATE_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockServiceApprovalDAO.findByApprovalCode(approvalCode)).thenReturn(usedApproval);

        pharmacyInsuranceHandler.handle(mockHttpExchange);

        verify(mockServiceApprovalDAO).findByApprovalCode(approvalCode);
        verify(mockHttpExchange).sendResponseHeaders(eq(400), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("Este servicio ya tiene una receta asociada"));
    }
    
     @Test
    void handleValidatePrescription_DaoUpdateFails() throws IOException {
        String approvalCode = "AP_UPDATE_FAIL";
        Map<String, Object> requestMap = Map.of("approvalCode", approvalCode, "prescriptionId", 1L, "totalAmount", 100.0);
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        ServiceApproval approval = new ServiceApproval(); approval.setStatus("APPROVED");
        
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(VALIDATE_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockServiceApprovalDAO.findByApprovalCode(approvalCode)).thenReturn(approval);
        when(mockServiceApprovalDAO.update(any(ServiceApproval.class))).thenReturn(null); // Simulate update failure

        pharmacyInsuranceHandler.handle(mockHttpExchange);

        verify(mockServiceApprovalDAO).findByApprovalCode(approvalCode);
        verify(mockServiceApprovalDAO).update(any(ServiceApproval.class));
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }

    // --- GET /check-coverage/{id} Tests ---
    
    @Test
    void handleCheckCoverage_Success() throws IOException {
        Long prescriptionId = 77L;
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(CHECK_COVERAGE_ENDPOINT_BASE + prescriptionId));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");

        ServiceApproval approval = new ServiceApproval();
        approval.setApprovalCode("AP777");
        approval.setPrescriptionId(prescriptionId);
        approval.setCoveredAmount(70.0);
        approval.setPatientAmount(30.0);
        approval.setPrescriptionTotal(100.0);
        approval.setStatus("COMPLETED");
        when(mockServiceApprovalDAO.findByPrescriptionId(prescriptionId)).thenReturn(approval);
        String expectedJson = objectMapper.writeValueAsString(Map.of(
            "success", true,
            "approvalCode", "AP777",
            "prescriptionId", prescriptionId,
            "coveredAmount", 70.0,
            "patientAmount", 30.0,
            "totalAmount", 100.0,
            "status", "COMPLETED"
        ));
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        pharmacyInsuranceHandler.handle(mockHttpExchange);

        verify(mockServiceApprovalDAO).findByPrescriptionId(prescriptionId);
        verifyResponseSent(200, expectedBytes);
    }
    
     @Test
    void handleCheckCoverage_NotFound() throws IOException {
        Long prescriptionId = 88L;
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(CHECK_COVERAGE_ENDPOINT_BASE + prescriptionId));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockServiceApprovalDAO.findByPrescriptionId(prescriptionId)).thenReturn(null);

        pharmacyInsuranceHandler.handle(mockHttpExchange);

        verify(mockServiceApprovalDAO).findByPrescriptionId(prescriptionId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), anyLong());
         verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("No se encontró aprobación para esta receta"));
    }
    
     @Test
    void handleCheckCoverage_InvalidIdFormat() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(CHECK_COVERAGE_ENDPOINT_BASE + "abc"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");

        // The exception happens during path parsing before the handler method is called
        // The main handle method catches it and sends 500
        pharmacyInsuranceHandler.handle(mockHttpExchange);
        
        verify(mockServiceApprovalDAO, never()).findByPrescriptionId(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }

    // Helper to verify response sending
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