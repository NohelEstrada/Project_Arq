package com.sources.app.handlers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import com.sources.app.dao.VerificationDAO;

public class VerificationHandlerTest {

    private static class MockHttpExchange extends HttpExchange {
        private final String method;
        private final URI uri;
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private int responseCode = -1;
        private final InputStream requestBody;

        MockHttpExchange(String method, String uri) { this(method, uri, new byte[0]); }
        MockHttpExchange(String method, String uri, byte[] body) {
            this.method = method;
            this.uri = URI.create(uri);
            this.requestBody = new ByteArrayInputStream(body);
        }

        @Override public Headers getRequestHeaders() { return requestHeaders; }
        @Override public Headers getResponseHeaders() { return responseHeaders; }
        @Override public URI getRequestURI() { return uri; }
        @Override public String getRequestMethod() { return method; }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public void close() {}
        @Override public InputStream getRequestBody() { return requestBody; }
        @Override public OutputStream getResponseBody() { return responseBody; }
        @Override public void sendResponseHeaders(int rCode, long responseLength) throws IOException { this.responseCode = rCode; }
        @Override public InetSocketAddress getRemoteAddress() { return new InetSocketAddress(0); }
        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public void setStreams(InputStream i, OutputStream o) {}
        @Override public com.sun.net.httpserver.HttpPrincipal getPrincipal() { return null; }
        public int getResponseCode() { return responseCode; }
        byte[] getResponseBytes() { return responseBody.toByteArray(); }
    }

    private static class StubVerificationDAO extends VerificationDAO {
        private final boolean result;
        StubVerificationDAO(boolean result) { this.result = result; }
        @Override public boolean verifyUser(String email) { return result; }
    }

    @Test
    public void testOptionsCors() throws Exception {
        VerificationHandler handler = new VerificationHandler();
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/verification");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, OPTIONS", ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods"));
    }

    @Test
    public void testPathMismatchReturns404() throws Exception {
        VerificationHandler handler = new VerificationHandler();
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/verify");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testMethodNotAllowedReturns405() throws Exception {
        VerificationHandler handler = new VerificationHandler();
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/verification");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
    }

    @Test
    public void testMissingEmailReturns400() throws Exception {
        VerificationHandler handler = new VerificationHandler();
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/verification");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
    }

    @Test
    public void testEmailVerifiedReturns1() throws Exception {
        VerificationHandler handler = new VerificationHandler(new StubVerificationDAO(true));
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/verification?email=test@example.com");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        assertEquals("application/json", ex.getResponseHeaders().getFirst("Content-Type"));
        assertEquals("1", new String(ex.getResponseBytes()));
    }

    @Test
    public void testEmailNotVerifiedReturns0() throws Exception {
        VerificationHandler handler = new VerificationHandler(new StubVerificationDAO(false));
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/verification?email=missing@example.com");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        assertEquals("application/json", ex.getResponseHeaders().getFirst("Content-Type"));
        assertEquals("0", new String(ex.getResponseBytes()));
    }
}
