package com.sources.app.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.ConfigurableAmountDAO;
import com.sources.app.dao.PrescriptionApprovalDAO;
import com.sources.app.dao.UserDAO;
import com.sources.app.entities.ConfigurableAmount;
import com.sources.app.entities.PrescriptionApproval;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionApprovalHandlerTest {

    @Mock
    private PrescriptionApprovalDAO mockApprovalDAO;
    @Mock
    private UserDAO mockUserDAO;
    @Mock
    private ConfigurableAmountDAO mockConfigDAO;
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
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    @InjectMocks
    private PrescriptionApprovalHandler handler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;
    @Captor
    ArgumentCaptor<PrescriptionApproval> approvalCaptor;

    private static final String APPROVE_ENDPOINT = "/api/prescriptions/approve";
    private static final String GET_APPROVALS_ENDPOINT = "/api/prescriptions/approvals";

    private User testUser;
    private ConfigurableAmount testConfig;

    @BeforeEach
    void setUp() {
        lenient().when(mockHttpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockHttpExchange.getResponseBody()).thenReturn(mockResponseBody);
        lenient().when(mockHttpExchange.getRequestHeaders()).thenReturn(mockRequestHeaders);
        
        // Setup default valid user and config for approval tests
        testUser = new User();
        testUser.setIdUser(1L);
        testUser.setPaidService(true);
        lenient().when(mockUserDAO.findById(1L)).thenReturn(testUser);

        testConfig = new ConfigurableAmount();
        testConfig.setPrescriptionAmount(new BigDecimal("250.00"));
        lenient().when(mockConfigDAO.findCurrentConfig()).thenReturn(testConfig);
        
        // Mock the save method to return the argument passed to it
        lenient().when(mockApprovalDAO.save(any(PrescriptionApproval.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() throws IOException {
       // verify(mockResponseBody, atLeastOnce()).close();
    }

    @Test
    void handle_OptionsRequest_SendsNoContent() throws IOException {
        // when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT)); // Unnecessary stubbing reported at line 110
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        handler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockApprovalDAO, mockUserDAO, mockConfigDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/wrong"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        handler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
    @Test
    void handle_WrongMethodForApprove_SendsNotFound() throws IOException {
        // This handler routes based on path and method, wrong method for known path = 404
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(APPROVE_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET"); 
        handler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handle_WrongMethodForGetApprovals_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(GET_APPROVALS_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST"); 
        handler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }

    // --- GET /approvals Tests ---
    
    @Test
    void handleGetApprovals_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(GET_APPROVALS_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<PrescriptionApproval> approvals = Arrays.asList(new PrescriptionApproval(), new PrescriptionApproval());
        when(mockApprovalDAO.findAll()).thenReturn(approvals);
        String expectedJson = objectMapper.writeValueAsString(approvals);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        handler.handle(mockHttpExchange);

        verify(mockApprovalDAO).findAll();
        verifyResponseSent(200, expectedBytes);
    }
    
     @Test
    void handleGetApprovals_FindByUserId_Success() throws IOException {
        Long userId = 1L;
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(GET_APPROVALS_ENDPOINT + "?userId=" + userId));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<PrescriptionApproval> approvals = Collections.singletonList(new PrescriptionApproval());
        when(mockApprovalDAO.findByUserId(userId)).thenReturn(approvals);
        String expectedJson = objectMapper.writeValueAsString(approvals);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        handler.handle(mockHttpExchange);

        verify(mockApprovalDAO).findByUserId(userId);
        verifyResponseSent(200, expectedBytes);
    }
    
     @Test
    void handleGetApprovals_FindByUserId_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(GET_APPROVALS_ENDPOINT + "?userId=abc"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        
        handler.handle(mockHttpExchange);

        verify(mockApprovalDAO, never()).findByUserId(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST /approve Tests ---

    @Test
    void handlePrescriptionApproval_Success() throws IOException {
        Long userId = 1L;
        Double cost = 300.0;
        String hospPresId = "HOSP123";
        String details = "{\"med\":\"X\"}";
        Map<String, Object> requestMap = Map.of("userId", userId, "totalCost", cost, "prescriptionIdHospital", hospPresId, "details", details);
        setupPostRequest(requestMap);
        
        // User and Config are mocked in setup

        handler.handle(mockHttpExchange);

        verify(mockUserDAO).findById(userId);
        verify(mockConfigDAO).findCurrentConfig();
        verify(mockApprovalDAO).save(approvalCaptor.capture());
        
        PrescriptionApproval savedApproval = approvalCaptor.getValue();
        assertEquals(userId, savedApproval.getIdUser());
        assertEquals(cost, savedApproval.getPrescriptionCost());
        assertEquals(hospPresId, savedApproval.getPrescriptionIdHospital());
        assertEquals(details, savedApproval.getPrescriptionDetails());
        assertEquals("APPROVED", savedApproval.getStatus());
        assertNotNull(savedApproval.getAuthorizationNumber());
        assertNull(savedApproval.getRejectionReason());

        verify(mockHttpExchange).sendResponseHeaders(eq(200), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String responseJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(responseJson.contains("\"status\": \"Approved\""));
        assertTrue(responseJson.contains("\"authorizationNumber\":"));
    }
    
     @Test
    void handlePrescriptionApproval_UserNotFound_Rejects() throws IOException {
        Long userId = 99L;
        Map<String, Object> requestMap = Map.of("userId", userId, "totalCost", 300.0, "prescriptionIdHospital", "H1", "details", "D");
        setupPostRequest(requestMap);
        when(mockUserDAO.findById(userId)).thenReturn(null); // User not found

        handler.handle(mockHttpExchange);

        verify(mockUserDAO).findById(userId);
        verify(mockConfigDAO).findCurrentConfig(); // Still checks config
        verify(mockApprovalDAO).save(approvalCaptor.capture()); // Saves rejection
        assertEquals("REJECTED", approvalCaptor.getValue().getStatus());
        assertEquals("User not found", approvalCaptor.getValue().getRejectionReason());

        verify(mockHttpExchange).sendResponseHeaders(eq(404), anyLong()); // User not found status
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("User not found"));
    }
    
    @Test
    void handlePrescriptionApproval_CoverageInactive_Rejects() throws IOException {
        Long userId = 1L;
        Map<String, Object> requestMap = Map.of("userId", userId, "totalCost", 300.0, "prescriptionIdHospital", "H1", "details", "D");
        setupPostRequest(requestMap);
        testUser.setPaidService(false); // Make user inactive
        when(mockUserDAO.findById(userId)).thenReturn(testUser);

        handler.handle(mockHttpExchange);

        verify(mockUserDAO).findById(userId);
        verify(mockConfigDAO).findCurrentConfig();
        verify(mockApprovalDAO).save(approvalCaptor.capture());
        assertEquals("REJECTED", approvalCaptor.getValue().getStatus());
        assertEquals("Client coverage inactive", approvalCaptor.getValue().getRejectionReason());

        verify(mockHttpExchange).sendResponseHeaders(eq(400), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("Client coverage inactive"));
    }
    
     @Test
    void handlePrescriptionApproval_CostBelowMinimum_Rejects() throws IOException {
         Long userId = 1L;
         Double cost = 100.0; // Below default 250.00
         Map<String, Object> requestMap = Map.of("userId", userId, "totalCost", cost, "prescriptionIdHospital", "H1", "details", "D");
         setupPostRequest(requestMap);

         handler.handle(mockHttpExchange);

         verify(mockUserDAO).findById(userId);
         verify(mockConfigDAO).findCurrentConfig();
         verify(mockApprovalDAO).save(approvalCaptor.capture());
         assertEquals("REJECTED", approvalCaptor.getValue().getStatus());
         assertTrue(approvalCaptor.getValue().getRejectionReason().contains("below minimum threshold"));

         verify(mockHttpExchange).sendResponseHeaders(eq(400), anyLong());
         verify(mockResponseBody).write(responseBodyCaptor.capture());
         String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
         assertTrue(errorJson.contains("below minimum threshold"));
    }
    
     @Test
    void handlePrescriptionApproval_DaoSaveFailsOnReject_SendsInternalError() throws IOException {
         Long userId = 99L; // User not found triggers rejection
         Map<String, Object> requestMap = Map.of("userId", userId, "totalCost", 300.0, "prescriptionIdHospital", "H1", "details", "D");
         setupPostRequest(requestMap);
         when(mockUserDAO.findById(userId)).thenReturn(null);
         // Simulate save failure during rejection
         when(mockApprovalDAO.save(any(PrescriptionApproval.class))).thenThrow(new RuntimeException("DB Error on Save"));

         // Exception should bubble up to the main catch block
         handler.handle(mockHttpExchange);

         verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
     @Test
    void handlePrescriptionApproval_DaoSaveFailsOnApprove_SendsInternalError() throws IOException {
         Long userId = 1L; 
         Map<String, Object> requestMap = Map.of("userId", userId, "totalCost", 300.0, "prescriptionIdHospital", "H1", "details", "D");
         setupPostRequest(requestMap);
          // Simulate save failure during approval
         when(mockApprovalDAO.save(any(PrescriptionApproval.class))).thenThrow(new RuntimeException("DB Error on Save"));

         handler.handle(mockHttpExchange);

         verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }

    // Helper method to setup POST request
    private void setupPostRequest(Map<String, Object> requestMap) throws IOException {
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(APPROVE_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
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