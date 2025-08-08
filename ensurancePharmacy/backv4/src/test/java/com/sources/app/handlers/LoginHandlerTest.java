package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.UserDAO;
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
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginHandlerTest {

    @Mock
    private UserDAO mockUserDAO;
    @Mock
    private HttpExchange mockHttpExchange;
    @Mock
    private Headers mockRequestHeaders;
    @Mock
    private Headers mockResponseHeaders;
    @Mock
    private OutputStream mockResponseBody;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper(); // Use real ObjectMapper

    @InjectMocks
    private LoginHandler loginHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/login";

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
        loginHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockUserDAO);
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/log_in")); // Wrong path
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        loginHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockUserDAO);
    }

    @Test
    void handle_GetRequest_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        loginHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
        verifyNoInteractions(mockUserDAO);
    }

    @Test
    void handlePost_LoginSuccess_ReturnsUserAndOk() throws IOException {
        String email = "test@success.com";
        String password = "password";
        String requestJson = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        User authenticatedUser = new User(); 
        authenticatedUser.setIdUser(1L);
        authenticatedUser.setEmail(email);
        when(mockUserDAO.login(email, password)).thenReturn(authenticatedUser);
        
        String expectedJson = objectMapper.writeValueAsString(authenticatedUser);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        loginHandler.handle(mockHttpExchange);

        verify(mockUserDAO).login(email, password);
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), eq((long) expectedBytes.length));
        verify(mockResponseBody).write(expectedBytes);
        verify(mockResponseBody).close();
    }

    @Test
    void handlePost_LoginFailed_SendsUnauthorized() throws IOException {
        String email = "test@fail.com";
        String password = "wrongpassword";
        String requestJson = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockUserDAO.login(email, password)).thenReturn(null); // Simulate login failure

        loginHandler.handle(mockHttpExchange);

        verify(mockUserDAO).login(email, password);
        verify(mockHttpExchange).sendResponseHeaders(eq(401), eq(-1L));
        verify(mockResponseBody, never()).write(any(byte[].class));
    }
    
     @Test
    void handlePost_InvalidJson_SendsBadRequestOrInternalError() throws IOException {
        // Depending on how specific the ObjectMapper config is, this might be 400 or 500
        // Here, assuming it bubbles up and results in a 500 from the generic catch block
        String invalidJson = "{\"email\": \"test@bad.com\", \"password\":"; // Malformed JSON
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        loginHandler.handle(mockHttpExchange);

        verify(mockUserDAO, never()).login(anyString(), anyString());
        // The handler catches Exception and sends 500
        // verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L)); 
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); 
    }
    
    @Test
    void handlePost_DaoThrowsException_SendsInternalError() throws IOException {
        String email = "test@error.com";
        String password = "password";
        String requestJson = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        when(mockUserDAO.login(email, password)).thenThrow(new RuntimeException("DAO Error"));

        loginHandler.handle(mockHttpExchange);

        verify(mockUserDAO).login(email, password);
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Exception caught
    }
} 