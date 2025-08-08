package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.CategoryDAO;
import com.sources.app.entities.Category;
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
class CategoryHandlerTest {

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
    private CategoryHandler categoryHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<Category> categoryCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/category";

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
        categoryHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockCategoryDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/wrong/path"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        categoryHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockCategoryDAO);
    }
    
    @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE"); // Unsupported
        categoryHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
        verifyNoInteractions(mockCategoryDAO);
    }

    // --- GET Tests ---

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT));
        List<Category> categories = Arrays.asList(new Category(), new Category());
        when(mockCategoryDAO.findAll()).thenReturn(categories);
        String expectedJson = objectMapper.writeValueAsString(categories);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        categoryHandler.handle(mockHttpExchange);

        verify(mockCategoryDAO).findAll();
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
        Category category = new Category(); category.setIdCategory(testId);
        when(mockCategoryDAO.findById(testId)).thenReturn(category);
        String expectedJson = objectMapper.writeValueAsString(category);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        categoryHandler.handle(mockHttpExchange);

        verify(mockCategoryDAO).findById(testId);
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
        when(mockCategoryDAO.findById(testId)).thenReturn(null);

        categoryHandler.handle(mockHttpExchange);

        verify(mockCategoryDAO).findById(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
     @Test
    void handleGet_FindById_InvalidIdFormat() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "?id=abc"));

        categoryHandler.handle(mockHttpExchange);

        verify(mockCategoryDAO, never()).findById(anyLong());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
    }

    // --- POST Tests ---

    @Test
    void handlePost_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Category requestCategory = new Category();
        requestCategory.setName("New Category");
        requestCategory.setEnabled(1);
        String requestJson = objectMapper.writeValueAsString(requestCategory);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        Category createdCategory = new Category(); // DAO returns created object
        createdCategory.setIdCategory(100L);
        createdCategory.setName(requestCategory.getName());
        createdCategory.setEnabled(requestCategory.getEnabled());
        when(mockCategoryDAO.create(eq("New Category"), eq(1))).thenReturn(createdCategory);
        String expectedJsonResponse = objectMapper.writeValueAsString(createdCategory);
        byte[] expectedBytes = expectedJsonResponse.getBytes(StandardCharsets.UTF_8);

        categoryHandler.handle(mockHttpExchange);

        verify(mockCategoryDAO).create(eq("New Category"), eq(1));
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(201), eq((long)expectedBytes.length));
        verify(mockResponseBody).write(expectedBytes);
        verify(mockResponseBody).close();
    }
    
     @Test
    void handlePost_DaoCreateFails() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Category requestCategory = new Category(); requestCategory.setName("Fail Cat"); requestCategory.setEnabled(1);
        String requestJson = objectMapper.writeValueAsString(requestCategory);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockCategoryDAO.create(anyString(), anyInt())).thenReturn(null); // Simulate failure

        categoryHandler.handle(mockHttpExchange);

        verify(mockCategoryDAO).create(anyString(), anyInt());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
    
    @Test
    void handlePost_JsonParseException() throws IOException {
         when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"name\": \"Test Cat\", \"enabled\": "; // Malformed
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        categoryHandler.handle(mockHttpExchange);

        verify(mockCategoryDAO, never()).create(anyString(), anyInt());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }

    // --- PUT Tests ---

    @Test
    void handlePut_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Category categoryToUpdate = new Category(); 
        categoryToUpdate.setIdCategory(1L); // Must have ID for update
        categoryToUpdate.setName("Updated Category");
        categoryToUpdate.setEnabled(0);
        String requestJson = objectMapper.writeValueAsString(categoryToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockCategoryDAO.update(any(Category.class))).thenReturn(categoryToUpdate);
        String expectedJsonResponse = objectMapper.writeValueAsString(categoryToUpdate);
        byte[] expectedBytes = expectedJsonResponse.getBytes(StandardCharsets.UTF_8);

        categoryHandler.handle(mockHttpExchange);

        verify(mockCategoryDAO).update(categoryCaptor.capture());
        Category capturedCategory = categoryCaptor.getValue();
        assertEquals(categoryToUpdate.getIdCategory(), capturedCategory.getIdCategory());
        assertEquals(categoryToUpdate.getName(), capturedCategory.getName());
        assertEquals(categoryToUpdate.getEnabled(), capturedCategory.getEnabled());
        
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), eq((long)expectedBytes.length));
        verify(mockResponseBody).write(expectedBytes);
        verify(mockResponseBody).close();
    }
    
     @Test
    void handlePut_DaoUpdateFails() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        Category categoryToUpdate = new Category();
        String requestJson = objectMapper.writeValueAsString(categoryToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockCategoryDAO.update(any(Category.class))).thenReturn(null); // Simulate failure

        categoryHandler.handle(mockHttpExchange);

        verify(mockCategoryDAO).update(any(Category.class));
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
    }
} 