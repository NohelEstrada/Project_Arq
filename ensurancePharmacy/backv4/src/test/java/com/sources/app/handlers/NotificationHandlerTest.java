package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationHandlerTest {

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

    // We don't mock DAOs here as this handler doesn't use them directly
    // It uses jakarta.mail which we'll mock statically

    @InjectMocks
    private NotificationHandler notificationHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;
    @Captor
    ArgumentCaptor<MimeMessage> messageCaptor;

    private static final String API_ENDPOINT = "/api/notifications/email";

    private MockedStatic<Transport> mockedTransport;

    @BeforeEach
    void setUp() {
        lenient().when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT));
        lenient().when(mockHttpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockHttpExchange.getResponseBody()).thenReturn(mockResponseBody);
        lenient().when(mockHttpExchange.getRequestHeaders()).thenReturn(mockRequestHeaders);
        
        // Mock the static Transport.send method
        mockedTransport = Mockito.mockStatic(Transport.class);
        // Default behavior: do nothing when send is called
        mockedTransport.when(() -> Transport.send(any(MimeMessage.class))).thenAnswer(invocation -> null);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockedTransport.close();
        // verify(mockResponseBody, atLeastOnce()).close(); // Add if needed
    }
    
    @Test
    void handle_OptionsRequest_SendsNoContent() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        notificationHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        mockedTransport.verifyNoInteractions();
    }

    @Test
    void handle_WrongPath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/wrong"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        notificationHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        mockedTransport.verifyNoInteractions();
    }
    
     @Test
    void handle_GetRequest_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET"); // Unsupported
        notificationHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    // --- POST Tests ---

    @Test
    void handlePost_SendEmail_Success() throws IOException, MessagingException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test email body.";
        Map<String, String> requestMap = Map.of("to", to, "subject", subject, "body", body);
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        // Expect Transport.send to be called
        mockedTransport.when(() -> Transport.send(any(MimeMessage.class))).thenAnswer(invocation -> null); // Success

        notificationHandler.handle(mockHttpExchange);

        // Verify Transport.send was called with the correct details
        mockedTransport.verify(() -> Transport.send(messageCaptor.capture()));
        MimeMessage capturedMessage = messageCaptor.getValue();
        assertEquals(to, capturedMessage.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals(subject, capturedMessage.getSubject());
        // Note: Verifying body might require more complex handling depending on content type
        assertTrue(((String)capturedMessage.getContent()).contains(body)); 

        // Verify success response
        verify(mockHttpExchange).sendResponseHeaders(eq(200), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String responseJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(responseJson.contains("\"success\":true"));
        assertTrue(responseJson.contains("Email enviado con éxito"));
        verify(mockResponseBody).close();
    }
    
    @Test
    void handlePost_SendEmail_TransportThrowsException() throws IOException, MessagingException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        Map<String, String> requestMap = Map.of("to", "r@e.com", "subject", "S", "body", "B");
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        // Mock Transport.send to throw an exception
        MessagingException mailException = new MessagingException("SMTP Connection failed");
        mockedTransport.when(() -> Transport.send(any(MimeMessage.class))).thenThrow(mailException);

        notificationHandler.handle(mockHttpExchange);

        mockedTransport.verify(() -> Transport.send(any(MimeMessage.class)));
        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L));
        verify(mockResponseBody, never()).write(any(byte[].class)); // No response body on 500
    }
    
    @Test
    void handlePost_MissingData_SendsBadRequest() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        // Missing 'subject'
        Map<String, String> requestMap = Map.of("to", "r@e.com", "body", "B"); 
        String requestJson = objectMapper.writeValueAsString(requestMap);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        notificationHandler.handle(mockHttpExchange);

        verify(mockHttpExchange).sendResponseHeaders(eq(400), eq(-1L));
        mockedTransport.verifyNoInteractions(); // Should not attempt to send email
    }
    
    @Test
    void handlePost_InvalidJson_SendsInternalError() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"to\": \"r@e.com\", "; // Malformed
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        notificationHandler.handle(mockHttpExchange);

        verify(mockHttpExchange).sendResponseHeaders(eq(500), eq(-1L)); // Caught by generic Exception handler
        mockedTransport.verifyNoInteractions();
    }
} 