package com.sources.app.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.HospitalDAO;
import com.sources.app.dao.HospitalInsuranceServiceDAO;
import com.sources.app.dao.InsuranceServiceDAO;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.HospitalInsuranceService;
import com.sources.app.entities.InsuranceService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalInsuranceServiceHandlerTest {

    @Mock
    private HospitalInsuranceServiceDAO mockHospInsSvcDAO;
    @Mock
    private HospitalDAO mockHospitalDAO;
    @Mock
    private InsuranceServiceDAO mockInsuranceServiceDAO;
    @Mock
    private HttpExchange mockHttpExchange;
    @Mock
    private Headers mockRequestHeaders;
    @Mock
    private Headers mockResponseHeaders;
    @Mock
    private OutputStream mockResponseBody;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @InjectMocks
    private HospitalInsuranceServiceHandler handler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;
    @Captor
    ArgumentCaptor<Hospital> hospitalCaptor;
    @Captor
    ArgumentCaptor<InsuranceService> serviceCaptor;
    @Captor
    ArgumentCaptor<HospitalInsuranceService> relationCaptor;

    private static final String BASE_ENDPOINT = "/api/hospital-services";

    @BeforeEach
    void setUp() {
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
        // when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT)); // Unnecessary stubbing reported at line 95
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        handler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockHospInsSvcDAO, mockHospitalDAO, mockInsuranceServiceDAO);
    }

    @Test
    void handle_WrongBasePath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/wrong"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        handler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handle_UnsupportedMethodForBase_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("PATCH");
        handler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET /api/hospital-services Tests ---

    @Test
    void handleGet_ByHospitalFilter_Success() throws IOException {
        Long hospitalId = 1L;
        Hospital hospital = new Hospital(); hospital.setIdHospital(hospitalId);
        List<HospitalInsuranceService> relations = Arrays.asList(new HospitalInsuranceService(), new HospitalInsuranceService());
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "?hospital=" + hospitalId));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHospitalDAO.findById(hospitalId)).thenReturn(hospital);
        when(mockHospInsSvcDAO.findApprovedByHospital(hospital)).thenReturn(relations);
        String expectedJson = objectMapper.writeValueAsString(relations);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        handler.handle(mockHttpExchange);

        verify(mockHospitalDAO).findById(hospitalId);
        verify(mockHospInsSvcDAO).findApprovedByHospital(hospital);
        verifyResponseSent(200, expectedBytes);
    }
    
     @Test
    void handleGet_ByHospitalFilter_HospitalNotFound() throws IOException {
        Long hospitalId = 99L;
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "?hospital=" + hospitalId));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHospitalDAO.findById(hospitalId)).thenReturn(null);

        handler.handle(mockHttpExchange);

        verify(mockHospitalDAO).findById(hospitalId);
        verify(mockHospInsSvcDAO, never()).findApprovedByHospital(any());
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
    @Test
    void handleGet_ByServiceFilter_Success() throws IOException {
        Long serviceId = 2L;
        InsuranceService service = new InsuranceService(); service.setIdInsuranceService(serviceId);
        List<HospitalInsuranceService> relations = Collections.singletonList(new HospitalInsuranceService());
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "?service=" + serviceId));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockInsuranceServiceDAO.findById(serviceId)).thenReturn(service);
        when(mockHospInsSvcDAO.findHospitalsByService(service)).thenReturn(relations);
        String expectedJson = objectMapper.writeValueAsString(relations);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        handler.handle(mockHttpExchange);

        verify(mockInsuranceServiceDAO).findById(serviceId);
        verify(mockHospInsSvcDAO).findHospitalsByService(service);
        verifyResponseSent(200, expectedBytes);
    }
    
     @Test
    void handleGet_ByServiceFilter_ServiceNotFound() throws IOException {
        Long serviceId = 98L;
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "?service=" + serviceId));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockInsuranceServiceDAO.findById(serviceId)).thenReturn(null);

        handler.handle(mockHttpExchange);

        verify(mockInsuranceServiceDAO).findById(serviceId);
        verify(mockHospInsSvcDAO, never()).findHospitalsByService(any());
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handleGet_ById_Success() throws IOException {
        Long relationId = 5L;
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/" + relationId));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        HospitalInsuranceService relation = new HospitalInsuranceService(); relation.setIdHospitalService(relationId);
        when(mockHospInsSvcDAO.findById(relationId)).thenReturn(relation);
        String expectedJson = objectMapper.writeValueAsString(relation);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);
        
        handler.handle(mockHttpExchange);
        
        verify(mockHospInsSvcDAO).findById(relationId);
        verifyResponseSent(200, expectedBytes);
    }
    
     @Test
    void handleGet_ById_NotFound() throws IOException {
         Long relationId = 95L;
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/" + relationId));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHospInsSvcDAO.findById(relationId)).thenReturn(null);

        handler.handle(mockHttpExchange);

        verify(mockHospInsSvcDAO).findById(relationId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handleGet_ById_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/abc"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");

        handler.handle(mockHttpExchange);

        verify(mockHospInsSvcDAO, never()).findById(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST /approve Tests ---
    
    @Test
    void handleApprove_Success() throws IOException {
        Long hospitalId = 1L;
        Long serviceId = 2L;
        String notes = "Approved via API";
        Hospital hospital = new Hospital(); hospital.setIdHospital(hospitalId);
        InsuranceService service = new InsuranceService(); service.setIdInsuranceService(serviceId);
        Map<String, Object> requestMap = Map.of("hospitalId", hospitalId, "serviceId", serviceId, "notes", notes);
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/approve"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockHospitalDAO.findById(hospitalId)).thenReturn(hospital);
        when(mockInsuranceServiceDAO.findById(serviceId)).thenReturn(service);
        
        HospitalInsuranceService createdRelation = new HospitalInsuranceService(); 
        createdRelation.setIdHospitalService(10L);
        when(mockHospInsSvcDAO.approveService(eq(hospital), eq(service), eq(notes))).thenReturn(createdRelation);
        String expectedJson = objectMapper.writeValueAsString(createdRelation);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        handler.handle(mockHttpExchange);

        verify(mockHospitalDAO).findById(hospitalId);
        verify(mockInsuranceServiceDAO).findById(serviceId);
        verify(mockHospInsSvcDAO).approveService(hospital, service, notes);
        verifyResponseSent(201, expectedBytes);
    }
    
    @Test
    void handleApprove_HospitalNotFound() throws IOException {
         Long hospitalId = 1L; Long serviceId = 2L; String notes = "N";
         Map<String, Object> requestMap = Map.of("hospitalId", hospitalId, "serviceId", serviceId, "notes", notes);
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/approve"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockHospitalDAO.findById(hospitalId)).thenReturn(null); // Hospital not found
        when(mockInsuranceServiceDAO.findById(serviceId)).thenReturn(new InsuranceService()); // Service found

        handler.handle(mockHttpExchange);

        verify(mockHospitalDAO).findById(hospitalId);
        verify(mockInsuranceServiceDAO).findById(serviceId);
        verify(mockHospInsSvcDAO, never()).approveService(any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }
    
    // --- POST /revoke Tests ---
    
     @Test
    void handleRevoke_Success() throws IOException {
        Long hospitalId = 1L;
        Long serviceId = 2L;
        Hospital hospital = new Hospital(); hospital.setIdHospital(hospitalId);
        InsuranceService service = new InsuranceService(); service.setIdInsuranceService(serviceId);
        Map<String, Object> requestMap = Map.of("hospitalId", hospitalId, "serviceId", serviceId);
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/revoke"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockHospitalDAO.findById(hospitalId)).thenReturn(hospital);
        when(mockInsuranceServiceDAO.findById(serviceId)).thenReturn(service);
        when(mockHospInsSvcDAO.revokeApproval(hospital, service)).thenReturn(true);
        String expectedJson = "{\"success\":true}";
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        handler.handle(mockHttpExchange);

        verify(mockHospitalDAO).findById(hospitalId);
        verify(mockInsuranceServiceDAO).findById(serviceId);
        verify(mockHospInsSvcDAO).revokeApproval(hospital, service);
        verifyResponseSent(200, expectedBytes);
    }
    
     @Test
    void handleRevoke_NotFoundOrFailed() throws IOException {
         Long hospitalId = 1L; Long serviceId = 2L;
         Hospital hospital = new Hospital(); InsuranceService service = new InsuranceService();
         Map<String, Object> requestMap = Map.of("hospitalId", hospitalId, "serviceId", serviceId);
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/revoke"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockHospitalDAO.findById(hospitalId)).thenReturn(hospital);
        when(mockInsuranceServiceDAO.findById(serviceId)).thenReturn(service);
        when(mockHospInsSvcDAO.revokeApproval(hospital, service)).thenReturn(false); // Simulate revoke failed

        handler.handle(mockHttpExchange);

        verify(mockHospInsSvcDAO).revokeApproval(hospital, service);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }

    // --- DELETE Tests ---

    @Test
    void handleDelete_Success() throws IOException {
        Long relationId = 15L;
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/" + relationId));
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        when(mockHospInsSvcDAO.delete(relationId)).thenReturn(true);
        String expectedJson = "{\"success\":true}";
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        handler.handle(mockHttpExchange);

        verify(mockHospInsSvcDAO).delete(relationId);
        verifyResponseSent(200, expectedBytes);
    }
    
     @Test
    void handleDelete_NotFound() throws IOException {
        Long relationId = 95L;
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/" + relationId));
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        when(mockHospInsSvcDAO.delete(relationId)).thenReturn(false);

        handler.handle(mockHttpExchange);

        verify(mockHospInsSvcDAO).delete(relationId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handleDelete_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/abc"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");

        handler.handle(mockHttpExchange);

        verify(mockHospInsSvcDAO, never()).delete(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // Helper to reduce repetition
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