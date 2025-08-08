package com.sources.app.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.*;
import com.sources.app.entities.*;
import com.sources.app.util.HibernateUtil;
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
import org.hibernate.SessionFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardHandlerTest {

    @Mock
    private ServiceApprovalDAO mockServiceApprovalDAO;
    @Mock
    private HospitalDAO mockHospitalDAO;
    @Mock
    private InsuranceServiceDAO mockInsuranceServiceDAO;
    @Mock
    private HospitalInsuranceServiceDAO mockHospitalInsuranceServiceDAO; // Injected but not used in GET /dashboard
    @Mock
    private HttpExchange mockHttpExchange;
    @Mock
    private Headers mockRequestHeaders;
    @Mock
    private Headers mockResponseHeaders;
    @Mock
    private OutputStream mockResponseBody;
    @Mock
    private SessionFactory mockSessionFactory;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

    @InjectMocks
    private DashboardHandler dashboardHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String ENDPOINT_DASHBOARD = "/api/dashboard";
    private static final String ENDPOINT_STATUS = "/api/dashboard/status";

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        mockedHibernateUtil.when(() -> HibernateUtil.setCorsHeaders(any(HttpExchange.class))).thenCallRealMethod();
        
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
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_DASHBOARD)); // Path needed for CORS
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        dashboardHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockServiceApprovalDAO, mockHospitalDAO, mockInsuranceServiceDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/other"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        dashboardHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockServiceApprovalDAO, mockHospitalDAO, mockInsuranceServiceDAO);
    }
    
    @Test
    void handle_UnsupportedMethodForDashboard_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_DASHBOARD));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST"); 
        dashboardHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }
    
     @Test
    void handle_UnsupportedMethodForStatus_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_STATUS));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST"); 
        dashboardHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET /dashboard Tests ---
    
    @Test
    void handleDashboardGet_Success_AggregatesData() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_DASHBOARD));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");

        // Mock DAO responses
        ServiceApproval app1 = new ServiceApproval(); app1.setId(1L); app1.setStatus("APPROVED"); app1.setApprovalCode("AP1"); app1.setServiceName("S1"); app1.setServiceCost(100.0); app1.setCoveredAmount(80.0); app1.setPatientAmount(20.0); app1.setApprovalDate(new Date());
        ServiceApproval app2 = new ServiceApproval(); app2.setId(2L); app2.setStatus("PENDING"); app2.setApprovalCode("AP2"); app2.setServiceName("S2"); app2.setServiceCost(50.0); app2.setCoveredAmount(40.0); app2.setPatientAmount(10.0); app2.setPrescriptionId(101L); 
        ServiceApproval app3 = new ServiceApproval(); app3.setId(3L); app3.setStatus("COMPLETED"); app3.setApprovalCode("AP3"); app3.setServiceName("S3"); app3.setServiceCost(200.0); app3.setCoveredAmount(160.0); app3.setPatientAmount(40.0);
        List<ServiceApproval> approvals = Arrays.asList(app1, app2, app3);
        when(mockServiceApprovalDAO.findAll()).thenReturn(approvals);
        when(mockHospitalDAO.findAll()).thenReturn(Collections.singletonList(new Hospital()));
        when(mockInsuranceServiceDAO.findAll()).thenReturn(Arrays.asList(new InsuranceService(), new InsuranceService()));

        dashboardHandler.handle(mockHttpExchange);

        verify(mockServiceApprovalDAO).findAll();
        verify(mockHospitalDAO).findAll();
        verify(mockInsuranceServiceDAO).findAll();
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        verify(mockResponseBody).close();

        // Deserialize and verify dashboard structure/content
        String jsonResponse = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        Map<String, Object> dashboardData = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});

        assertNotNull(dashboardData);
        assertTrue(dashboardData.containsKey("approvalStats"));
        assertTrue(dashboardData.containsKey("hospitalCount"));
        assertTrue(dashboardData.containsKey("serviceCount"));
        assertTrue(dashboardData.containsKey("pharmacyStats")); // Simulated
        assertTrue(dashboardData.containsKey("recentTransactions"));
        assertTrue(dashboardData.containsKey("connections"));
        assertTrue(dashboardData.containsKey("lastUpdated"));

        // Verify specific stats
        Map<String, Object> approvalStats = (Map<String, Object>) dashboardData.get("approvalStats");
        assertEquals(3, approvalStats.get("total"));
        assertEquals(1, approvalStats.get("approved"));
        assertEquals(1, approvalStats.get("pending"));
        assertEquals(0, approvalStats.get("rejected"));
        assertEquals(1, approvalStats.get("completed"));
        assertEquals(1, approvalStats.get("withPrescription"));
        assertEquals(100.0 + 50.0 + 200.0, approvalStats.get("totalAmount"));

        assertEquals(1, dashboardData.get("hospitalCount"));
        assertEquals(2, dashboardData.get("serviceCount"));

        List<Map<String, Object>> recentTransactions = (List<Map<String, Object>>) dashboardData.get("recentTransactions");
        assertEquals(3, recentTransactions.size()); // Should show up to 10, we provided 3
        assertEquals(app1.getId(), recentTransactions.get(0).get("id"));
        assertEquals(app1.getStatus(), recentTransactions.get(0).get("status"));
    }
    
    @Test
    void handleDashboardGet_DaoException_SendsError() throws IOException {
         when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_DASHBOARD));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        // Simulate exception when calling DAO
        when(mockServiceApprovalDAO.findAll()).thenThrow(new RuntimeException("Database connection failed"));

        dashboardHandler.handle(mockHttpExchange);

        verify(mockServiceApprovalDAO).findAll();
        verify(mockHospitalDAO, never()).findAll(); // Should fail before next DAO call
        verify(mockHttpExchange).sendResponseHeaders(eq(500), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("Error al generar el dashboard"));
        assertTrue(errorJson.contains("Database connection failed"));
        verify(mockResponseBody).close();
    }

    // --- GET /status Tests ---
    
    @Test
    void handleStatusGet_Success() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(ENDPOINT_STATUS));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");

        dashboardHandler.handle(mockHttpExchange);

        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        verify(mockResponseBody).close();

        String jsonResponse = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        Map<String, Object> statusData = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});

        assertNotNull(statusData);
        assertEquals(true, statusData.get("hospitalConnection")); // Currently hardcoded true
        assertEquals(true, statusData.get("pharmacyConnection")); // Currently hardcoded true
        assertEquals(true, statusData.get("databaseConnection"));
        assertTrue(statusData.containsKey("timestamp"));
    }
} 