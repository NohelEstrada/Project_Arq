package com.sources.app.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.CategoryDAO;
import com.sources.app.dao.InsuranceServiceDAO;
import com.sources.app.entities.Category;
import com.sources.app.entities.InsuranceService;
import com.sources.app.util.HttpClientUtil;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsuranceServiceHandlerTest {

    @Mock
    private InsuranceServiceDAO mockInsuranceServiceDAO;
    @Mock
    private CategoryDAO mockCategoryDAO;
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
    private InsuranceServiceHandler insuranceServiceHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;
    @Captor
    ArgumentCaptor<InsuranceService> serviceCaptor;

    private static final String BASE_ENDPOINT = "/api/insurance-services";
    private static final String HOSPITAL_SVC_ENDPOINT = BASE_ENDPOINT + "/hospital-services";
    private static final String APPROVE_SVC_ENDPOINT = BASE_ENDPOINT + "/approve-hospital-service";

    private MockedStatic<HttpClientUtil> mockedHttpClientUtil;

    @BeforeEach
    void setUp() {
        lenient().when(mockHttpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockHttpExchange.getResponseBody()).thenReturn(mockResponseBody);
        lenient().when(mockHttpExchange.getRequestHeaders()).thenReturn(mockRequestHeaders);
        
        // Mock static HttpClientUtil methods
        mockedHttpClientUtil = Mockito.mockStatic(HttpClientUtil.class);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockedHttpClientUtil.close();
        // verify(mockResponseBody, atLeastOnce()).close(); // Add if needed
    }

    // --- General Handling Tests ---
    
     @Test
    void handle_OptionsRequest_SendsNoContent() throws IOException {
        // when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT)); // Unnecessary stubbing reported at line 105
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        insuranceServiceHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockInsuranceServiceDAO, mockCategoryDAO);
        mockedHttpClientUtil.verifyNoInteractions();
    }

    @Test
    void handle_WrongBasePath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/wrong"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        insuranceServiceHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
    @Test
    void handle_UnsupportedMethodForBase_SendsMethodNotAllowed() throws IOException {
         when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("PATCH"); 
        insuranceServiceHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- GET /hospital-services Tests ---

    @Test
    void handleGetHospitalServices_Success_ParsesResponse() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(HOSPITAL_SVC_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");

        String mockHospitalResponse = "[{\"_id\": \"HOSP1\", \"name\": \"Consultation\", \"total\": 150.0}, {\"id\": \"HOSP2\", \"nombre\": \"X-Ray\", \"price\": 200}]";
        // Mock HttpClient to succeed on the first try
        mockedHttpClientUtil.when(() -> HttpClientUtil.get(anyString())).thenReturn(mockHospitalResponse);
        
        // Mock DAO check for existing services
        when(mockInsuranceServiceDAO.findByExternalId("HOSP1")).thenReturn(Collections.emptyList());
        InsuranceService existingService = new InsuranceService(); existingService.setIdInsuranceService(5L);
        when(mockInsuranceServiceDAO.findByExternalId("HOSP2")).thenReturn(Collections.singletonList(existingService));

        insuranceServiceHandler.handle(mockHttpExchange);

        mockedHttpClientUtil.verify(() -> HttpClientUtil.get(anyString()), times(1)); // Verify it tried at least one URL
        verify(mockInsuranceServiceDAO).findByExternalId("HOSP1");
        verify(mockInsuranceServiceDAO).findByExternalId("HOSP2");
        verify(mockHttpExchange).sendResponseHeaders(eq(200), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        verify(mockResponseBody).close();

        String jsonResponse = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        List<Map<String, Object>> responseList = objectMapper.readValue(jsonResponse, new TypeReference<List<Map<String, Object>>>() {});
        
        assertEquals(2, responseList.size());
        // Check first service (new)
        assertEquals("HOSP1", responseList.get(0).get("hospitalServiceId"));
        assertEquals("Consultation", responseList.get(0).get("name"));
        assertEquals(150.0, responseList.get(0).get("total"));
        assertEquals(false, responseList.get(0).get("imported"));
        // Check second service (existing)
        assertEquals("HOSP2", responseList.get(1).get("hospitalServiceId"));
        assertEquals("X-Ray", responseList.get(1).get("name"));
        assertEquals(200.0, responseList.get(1).get("price")); // Check alternative field name
        assertEquals(true, responseList.get(1).get("imported"));
        assertEquals(5L, responseList.get(1).get("insuranceServiceId"));
    }
    
    @Test
    void handleGetHospitalServices_HttpClientFails_SendsServiceUnavailable() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(HOSPITAL_SVC_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        // Mock HttpClient to fail on all attempts
        mockedHttpClientUtil.when(() -> HttpClientUtil.get(anyString())).thenReturn(null);

        insuranceServiceHandler.handle(mockHttpExchange);

        mockedHttpClientUtil.verify(() -> HttpClientUtil.get(anyString()), atLeast(1)); // Verify it tried
        verify(mockHttpExchange).sendResponseHeaders(eq(503), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("No se pudieron obtener los servicios del hospital"));
        verify(mockResponseBody).close();
    }
    
    @Test
    void handleGetHospitalServices_InvalidJsonResponse_SendsError() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(HOSPITAL_SVC_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        String invalidJson = "this is not json";
        mockedHttpClientUtil.when(() -> HttpClientUtil.get(anyString())).thenReturn(invalidJson);

        insuranceServiceHandler.handle(mockHttpExchange);

        verify(mockHttpExchange).sendResponseHeaders(eq(500), anyLong());
         verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("Formato de respuesta del hospital inválido"));
        verify(mockResponseBody).close();
    }

    // --- POST /approve-hospital-service Tests ---
    
    @Test
    void handleApproveHospitalService_CreateNewService_Success() throws IOException {
        Long categoryId = 1L; Long subcategoryId = 2L; Integer coverage = 90;
        String hospitalServiceId = "HOSP_NEW"; String serviceName = "New Service"; Double price = 100.0;
        Map<String, Object> requestMap = Map.of(
            "hospitalServiceId", hospitalServiceId, 
            "categoryId", categoryId, 
            "subcategoryId", subcategoryId, 
            "coveragePercentage", coverage, 
            "name", serviceName, // Provide basic info
            "price", price
        );
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(APPROVE_SVC_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        
        // Simulate service not found in hospital API
        mockedHttpClientUtil.when(() -> HttpClientUtil.get(contains(hospitalServiceId))).thenReturn(null);
        // Simulate service doesn't exist locally yet
        when(mockInsuranceServiceDAO.findByExternalId(hospitalServiceId)).thenReturn(Collections.emptyList());
        
        Category category = new Category(); category.setIdCategory(categoryId);
        Category subcategory = new Category(); subcategory.setIdCategory(subcategoryId);
        when(mockCategoryDAO.findById(categoryId)).thenReturn(category);
        when(mockCategoryDAO.findById(subcategoryId)).thenReturn(subcategory);
        
        InsuranceService createdService = new InsuranceService(); createdService.setIdInsuranceService(1L);
        when(mockInsuranceServiceDAO.create(any(InsuranceService.class))).thenReturn(createdService);
        String expectedJson = objectMapper.writeValueAsString(createdService);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        insuranceServiceHandler.handle(mockHttpExchange);

        mockedHttpClientUtil.verify(() -> HttpClientUtil.get(anyString()), atLeastOnce());
        verify(mockInsuranceServiceDAO).findByExternalId(hospitalServiceId);
        verify(mockCategoryDAO).findById(categoryId);
        verify(mockCategoryDAO).findById(subcategoryId);
        verify(mockInsuranceServiceDAO).create(serviceCaptor.capture());
        // Verify captured service details
        assertEquals(hospitalServiceId, serviceCaptor.getValue().getExternalId());
        assertEquals(serviceName, serviceCaptor.getValue().getName());
        assertEquals(price, serviceCaptor.getValue().getPrice());
        assertEquals(category, serviceCaptor.getValue().getCategory());
        assertEquals(subcategory, serviceCaptor.getValue().getSubcategory());
        assertEquals(coverage, serviceCaptor.getValue().getCoveragePercentage());
        assertEquals(1, serviceCaptor.getValue().getEnabled());
        
        verifyResponseSent(200, expectedBytes); // Handler sends 200 on successful save
    }
    
     @Test
    void handleApproveHospitalService_UpdateExistingService_Success() throws IOException {
        Long categoryId = 1L; Long subcategoryId = 2L; Integer coverage = 70;
        String hospitalServiceId = "HOSP_EXIST"; String serviceNameFromHosp = "Detailed Name"; Double priceFromHosp = 150.0;
        Map<String, Object> requestMap = Map.of("hospitalServiceId", hospitalServiceId, "categoryId", categoryId, "subcategoryId", subcategoryId, "coveragePercentage", coverage);
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));

        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(APPROVE_SVC_ENDPOINT));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        
        // Simulate service found in hospital API
        String hospServiceJson = String.format("{\"id\": \"%s\", \"name\": \"%s\", \"price\": %.2f}", hospitalServiceId, serviceNameFromHosp, priceFromHosp);
        mockedHttpClientUtil.when(() -> HttpClientUtil.get(contains(hospitalServiceId))).thenReturn(hospServiceJson);
        
        // Simulate service already exists locally
        InsuranceService existingService = new InsuranceService(); existingService.setIdInsuranceService(5L); existingService.setExternalId(hospitalServiceId);
        when(mockInsuranceServiceDAO.findByExternalId(hospitalServiceId)).thenReturn(Collections.singletonList(existingService));
        
        Category category = new Category(); category.setIdCategory(categoryId);
        Category subcategory = new Category(); subcategory.setIdCategory(subcategoryId);
        when(mockCategoryDAO.findById(categoryId)).thenReturn(category);
        when(mockCategoryDAO.findById(subcategoryId)).thenReturn(subcategory);
        
        when(mockInsuranceServiceDAO.update(any(InsuranceService.class))).thenReturn(existingService);
        String expectedJson = objectMapper.writeValueAsString(existingService);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        insuranceServiceHandler.handle(mockHttpExchange);

        verify(mockInsuranceServiceDAO).findByExternalId(hospitalServiceId);
        verify(mockCategoryDAO).findById(categoryId);
        verify(mockCategoryDAO).findById(subcategoryId);
        verify(mockInsuranceServiceDAO).update(serviceCaptor.capture());
        assertEquals(serviceNameFromHosp, serviceCaptor.getValue().getName());
        assertEquals(priceFromHosp, serviceCaptor.getValue().getPrice());
        assertEquals(coverage, serviceCaptor.getValue().getCoveragePercentage());
        assertEquals(1, serviceCaptor.getValue().getEnabled()); // Should be enabled
        verify(mockInsuranceServiceDAO, never()).create(any());
        
        verifyResponseSent(200, expectedBytes);
    }
    
    // --- GET by Category/Subcategory/ID Tests (covered by DAO tests, but include basic handler wiring) ---

    @Test
    void handleGet_ByCategory_Success() throws IOException {
        Long catId = 1L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "?category=" + catId));
        Category category = new Category(); category.setIdCategory(catId);
        when(mockCategoryDAO.findById(catId)).thenReturn(category);
        List<InsuranceService> services = Collections.singletonList(new InsuranceService());
        when(mockInsuranceServiceDAO.findByCategory(category)).thenReturn(services);
        String expectedJson = objectMapper.writeValueAsString(services);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        insuranceServiceHandler.handle(mockHttpExchange);

        verify(mockCategoryDAO).findById(catId);
        verify(mockInsuranceServiceDAO).findByCategory(category);
        verifyResponseSent(200, expectedBytes);
    }
    
    // --- POST/PUT/DELETE for InsuranceService (standard CRUD) ---
    
    @Test
    void handleCreate_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT));
        
        Long categoryId = 1L; Long subcategoryId = 2L;
        Map<String, Object> requestMap = Map.of(
            "name", "New Service", "description", "Desc", "categoryId", categoryId,
            "subcategoryId", subcategoryId, "price", 50.0, "coveragePercentage", 100, "enabled", 1
        );
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        Category category = new Category(); Category subcategory = new Category();
        when(mockCategoryDAO.findById(categoryId)).thenReturn(category);
        when(mockCategoryDAO.findById(subcategoryId)).thenReturn(subcategory);
        
        InsuranceService createdService = new InsuranceService(); createdService.setIdInsuranceService(1L);
        when(mockInsuranceServiceDAO.create(any(InsuranceService.class))).thenReturn(createdService);
        String expectedJson = objectMapper.writeValueAsString(createdService);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        insuranceServiceHandler.handle(mockHttpExchange);

        verify(mockCategoryDAO).findById(categoryId);
        verify(mockCategoryDAO).findById(subcategoryId);
        verify(mockInsuranceServiceDAO).create(serviceCaptor.capture());
        assertEquals("New Service", serviceCaptor.getValue().getName());
        assertEquals(category, serviceCaptor.getValue().getCategory());
        assertEquals(subcategory, serviceCaptor.getValue().getSubcategory());
        verifyResponseSent(201, expectedBytes);
    }
    
    @Test
    void handleUpdate_Success() throws IOException {
        Long serviceId = 5L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/" + serviceId));

        Map<String, Object> requestMap = Map.of("name", "Updated Name", "enabled", 0);
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        InsuranceService existingService = new InsuranceService(); existingService.setIdInsuranceService(serviceId);
        when(mockInsuranceServiceDAO.findById(serviceId)).thenReturn(existingService);
        when(mockInsuranceServiceDAO.update(any(InsuranceService.class))).thenReturn(existingService);
        String expectedJson = objectMapper.writeValueAsString(existingService);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        insuranceServiceHandler.handle(mockHttpExchange);

        verify(mockInsuranceServiceDAO).findById(serviceId);
        verify(mockInsuranceServiceDAO).update(serviceCaptor.capture());
        assertEquals("Updated Name", serviceCaptor.getValue().getName());
        assertEquals(0, serviceCaptor.getValue().getEnabled());
        verifyResponseSent(200, expectedBytes);
    }

     @Test
    void handleUpdate_NotFound() throws IOException {
        Long serviceId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/" + serviceId));
        String requestJson = "{}"; // Empty body
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockInsuranceServiceDAO.findById(serviceId)).thenReturn(null);

        insuranceServiceHandler.handle(mockHttpExchange);

        verify(mockInsuranceServiceDAO).findById(serviceId);
        verify(mockInsuranceServiceDAO, never()).update(any());
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handleDelete_Success() throws IOException {
        Long serviceId = 25L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/" + serviceId));
        when(mockInsuranceServiceDAO.delete(serviceId)).thenReturn(true);
        String expectedJson = "{\"success\":true}";
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);
        
        insuranceServiceHandler.handle(mockHttpExchange);
        
        verify(mockInsuranceServiceDAO).delete(serviceId);
        verifyResponseSent(200, expectedBytes);
    }
    
     @Test
    void handleDelete_NotFound() throws IOException {
        Long serviceId = 96L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "/" + serviceId));
        when(mockInsuranceServiceDAO.delete(serviceId)).thenReturn(false);
        
        insuranceServiceHandler.handle(mockHttpExchange);
        
        verify(mockInsuranceServiceDAO).delete(serviceId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
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