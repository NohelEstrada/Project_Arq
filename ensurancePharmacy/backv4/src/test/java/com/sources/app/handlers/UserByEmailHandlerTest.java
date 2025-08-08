package com.sources.app.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserByEmailHandlerTest {

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
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private UserByEmailHandler userByEmailHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String BASE_ENDPOINT = "/api/users/by-email/";

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
        // when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT)); // Unnecessary stubbing reported at line 74
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        userByEmailHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockUserDAO);
    }

    @Test
    void handle_WrongBasePath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/users/other/test@example.com"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        userByEmailHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockUserDAO);
    }
    
    @Test
    void handle_WrongMethod_SendsNotFound() throws IOException {
        // This handler sends 404 for wrong method on correct base path
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + "test@example.com"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST"); 
        userByEmailHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L)); 
        verifyNoInteractions(mockUserDAO);
    }

    // --- GET /by-email/{email} Tests ---

    @Test
    void handleGet_UserFound_ReturnsUserAndOk() throws IOException {
        String email = "found@example.com";
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + email));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");

        User user = new User(); 
        user.setIdUser(1L);
        user.setEmail(email);
        when(mockUserDAO.findByEmail(email)).thenReturn(user);
        String expectedJson = objectMapper.writeValueAsString(user);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        userByEmailHandler.handle(mockHttpExchange);

        verify(mockUserDAO).findByEmail(email);
        verify(mockResponseHeaders).set(eq("Content-Type"), eq("application/json"));
        verify(mockHttpExchange).sendResponseHeaders(eq(200), eq((long) expectedBytes.length));
        verify(mockResponseBody).write(expectedBytes);
        verify(mockResponseBody).close();
    }
    
     @Test
    void handleGet_UserFound_WithUrlEncodedEmail() throws IOException {
        String encodedEmail = "found%40example.com"; // found@example.com
        String decodedEmail = "found@example.com";
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + encodedEmail));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");

        User user = new User(); 
        user.setIdUser(1L);
        user.setEmail(decodedEmail);
        when(mockUserDAO.findByEmail(decodedEmail)).thenReturn(user);
        String expectedJson = objectMapper.writeValueAsString(user);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        userByEmailHandler.handle(mockHttpExchange);

        verify(mockUserDAO).findByEmail(decodedEmail);
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_UserNotFound_ReturnsNotFoundWithError() throws IOException {
        String email = "notfound@example.com";
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(BASE_ENDPOINT + email));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockUserDAO.findByEmail(email)).thenReturn(null);
        
        Map<String, String> expectedError = Map.of("error", "No se encontró un usuario con el correo: " + email);
        String expectedJson = objectMapper.writeValueAsString(expectedError);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        userByEmailHandler.handle(mockHttpExchange);

        verify(mockUserDAO).findByEmail(email);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq((long) expectedBytes.length));
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        assertArrayEquals(expectedBytes, responseBodyCaptor.getValue());
        verify(mockResponseBody).close();
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