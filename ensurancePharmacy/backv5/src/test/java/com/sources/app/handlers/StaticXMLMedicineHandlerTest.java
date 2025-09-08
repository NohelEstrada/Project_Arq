package com.sources.app.handlers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class StaticXMLMedicineHandlerTest {

    @Test
    public void testStaticXMLMedicineHandlerInstantiation() {
        // TODO: implement tests for StaticXMLMedicineHandler
        StaticXMLMedicineHandler instance = new StaticXMLMedicineHandler();
        assertNotNull(instance);
    }

    private static class MockHttpExchange extends HttpExchange {

        private final String method;
        private final URI uri;
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private int responseCode = -1;
        private final InputStream requestBody;

        MockHttpExchange(String method, String uri) {
            this(method, uri, new byte[0]);
        }

        MockHttpExchange(String method, String uri, byte[] body) {
            this.method = method;
            this.uri = URI.create(uri);
            this.requestBody = new ByteArrayInputStream(body);
        }

        @Override
        public Headers getRequestHeaders() {
            return requestHeaders;
        }

        @Override
        public Headers getResponseHeaders() {
            return responseHeaders;
        }

        @Override
        public URI getRequestURI() {
            return uri;
        }

        @Override
        public String getRequestMethod() {
            return method;
        }

        @Override
        public HttpContext getHttpContext() {
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public InputStream getRequestBody() {
            return requestBody;
        }

        @Override
        public OutputStream getResponseBody() {
            return responseBody;
        }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) {
            this.responseCode = rCode;
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return new InetSocketAddress(0);
        }

        @Override
        public int getResponseCode() {
            return responseCode;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return new InetSocketAddress(0);
        }

        @Override
        public String getProtocol() {
            return "HTTP/1.1";
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public void setAttribute(String name, Object value) {
        }

        @Override
        public void setStreams(InputStream i, OutputStream o) {
        }

        @Override
        public HttpPrincipal getPrincipal() {
            return null;
        }

        byte[] getResponseBytes() {
            return responseBody.toByteArray();
        }
    }

    @Test
    public void testGetXmlSuccess() throws Exception {
        StaticXMLMedicineHandler handler = new StaticXMLMedicineHandler();
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/static-medicines-xml");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        assertEquals("application/xml", ex.getResponseHeaders().getFirst("Content-Type"));
        String xml = new String(ex.getResponseBytes());
        assertTrue(xml.contains("<medicines>"));
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testOptionsCors() throws Exception {
        StaticXMLMedicineHandler handler = new StaticXMLMedicineHandler();
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/static-medicines-xml");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testWrongPathNotFound() throws Exception {
        StaticXMLMedicineHandler handler = new StaticXMLMedicineHandler();
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/not-static-medicines-xml");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testMethodNotAllowed() throws Exception {
        StaticXMLMedicineHandler handler = new StaticXMLMedicineHandler();
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/static-medicines-xml");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
    }
}
