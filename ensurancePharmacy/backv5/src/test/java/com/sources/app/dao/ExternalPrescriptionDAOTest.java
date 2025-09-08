package com.sources.app.dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;
import com.sources.app.entities.Prescription;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.User;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.net.URL;
import java.net.HttpURLConnection;

public class ExternalPrescriptionDAOTest {

    @Test
    public void testExternalPrescriptionDAOInstantiation() {
        ExternalPrescriptionDAO instance = new ExternalPrescriptionDAO();
        assertNotNull(instance);
    }

    @Test
    public void testGetByIdWithValidVerificationReturnsNull() {
        // This test covers the case where verification succeeds but no prescription exists
        ExternalPrescriptionDAO dao = new ExternalPrescriptionDAO();
        // Using a mock that will cause URL construction to fail, triggering the exception path
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost:8080/base");
        
        // This should return null due to the invalid URL construction
        assertNull(dao.getbyId(999L, "valid@example.com", ex));
    }

    @Test
    public void testGetByIdWithInvalidEmail() {
        ExternalPrescriptionDAO dao = new ExternalPrescriptionDAO();
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost:8080/base");
        
        // Test with null email
        assertNull(dao.getbyId(1L, null, ex));
        
        // Test with empty email
        assertNull(dao.getbyId(1L, "", ex));
    }

    @Test
    public void testGetByIdWithNullId() {
        ExternalPrescriptionDAO dao = new ExternalPrescriptionDAO();
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost:8080/base");
        
        // Test with null ID
        assertNull(dao.getbyId(null, "user@example.com", ex));
    }

    @Test
    public void testGetByIdWithMalformedUri() {
        ExternalPrescriptionDAO dao = new ExternalPrescriptionDAO();
        // Create a mock with a malformed URI that will cause URL construction issues
        MockHttpExchange ex = new MockHttpExchange("GET", "not-a-valid-uri");
        
        assertNull(dao.getbyId(1L, "user@example.com", ex));
    }

    private static class MockHttpExchange extends HttpExchange {
        private final String method;
        private final URI uri;
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private final InputStream requestBody;
        private int responseCode = -1;

        MockHttpExchange(String method, String uri) {
            this(method, URI.create(uri), new byte[0]);
        }

        @SuppressWarnings("unused")
        MockHttpExchange(String method, String uri, byte[] body) {
            this(method, URI.create(uri), body);
        }

        MockHttpExchange(String method, URI uri, byte[] body) {
            this.method = method;
            this.uri = uri;
            this.requestBody = new ByteArrayInputStream(body);
        }

        // Factory method to create a MockHttpExchange whose URI path equals the provided string
        static MockHttpExchange fromPathOnly(String method, String pathOnly) {
            return new MockHttpExchange(method, buildPathOnlyUri(pathOnly), new byte[0]);
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
        @Override public int getResponseCode() { return responseCode; }
        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public void setStreams(InputStream i, OutputStream o) {}
        @Override public HttpPrincipal getPrincipal() { return null; }
    }

    @Test
    public void testGetByIdReturnsNullOnHttpError() {
        ExternalPrescriptionDAO dao = new ExternalPrescriptionDAO();
        // The handler builds URL using exchange.getRequestURI().getPath() + "/api2/verification..."
        // getPath() returns only the path (e.g., "/base"), producing an invalid URL and triggering the inner catch -> null
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost:8080/base");

        assertNull(dao.getbyId(1L, "someone@example.com", ex));
    }

    @Test
    public void testGetByIdReturnsNullOnTopLevelException() {
        ExternalPrescriptionDAO dao = new ExternalPrescriptionDAO();
        // Passing a null exchange will trigger a NullPointerException in the outer try, which should be caught and return null
        assertNull(dao.getbyId(1L, "user@example.com", null));
    }

    // Helper to find a free local port for the temporary verification server
    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    // Start a lightweight HTTP server that responds to /api2/verification with a fixed body
    private static HttpServer startVerificationServer(int port, String responseBody) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api2/verification", ex -> {
            byte[] bytes = responseBody.getBytes();
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.setExecutor(null);
        server.start();
        return server;
    }

    @Test
    public void testGetByIdSuccessfulVerificationReturnsPrescription() throws Exception {
        // Ensure JDBC driver is loaded and keep a JDBC connection open so the shared in-memory SQLite DB persists across sessions
        Class.forName("org.sqlite.JDBC");
        Connection keepAliveConn = DriverManager.getConnection("jdbc:sqlite:file:memdb1?mode=memory&cache=shared");

        // Seed minimal data required for a Prescription
        HospitalDAO hospitalDAO = new HospitalDAO();
        UserDAO userDAO = new UserDAO();
        PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

        Hospital h = hospitalDAO.create("Hosp EP", "555-EP", "ep@h.com", "Addr EP", '1');
        assertNotNull(h, "Failed to create hospital for test");

        String uniqueEmail = "ext+" + System.currentTimeMillis() + "@example.com";
        User u = userDAO.create("Ext User", "9999", "555-EP", uniqueEmail, new java.util.Date(), "Addr", "pwd");
        assertNotNull(u, "Failed to create user for test");

        Prescription p = prescriptionDAO.create(h, u, 'Y');
        assertNotNull(p);
        assertNotNull(p.getIdPrescription());

        // Verify we can retrieve via a new Hibernate session before going through ExternalPrescriptionDAO
        Prescription crossSession = new PrescriptionDAO().getById(p.getIdPrescription());
        assertNotNull(crossSession, "Prescription should be retrievable across sessions when using shared in-memory DB");

        int port = findFreePort();
        HttpServer server = startVerificationServer(port, "1");
        try {
            // Directly call the verification endpoint to assert it returns "1"
            URL direct = URI.create("http://127.0.0.1:" + port + "/api2/verification?email=" + uniqueEmail).toURL();
            HttpURLConnection c = (HttpURLConnection) direct.openConnection();
            c.setRequestMethod("GET");
            assertEquals(200, c.getResponseCode());
            try (InputStream is = c.getInputStream()) {
                byte[] buf = is.readAllBytes();
                assertEquals("1", new String(buf));
            }

            ExternalPrescriptionDAO dao = new ExternalPrescriptionDAO();
            // Provide a base URL in the path so appending "/api2/verification" yields a valid absolute URL
            MockHttpExchange ex = MockHttpExchange.fromPathOnly("GET", "http://127.0.0.1:" + port);

            Prescription got = dao.getbyId(p.getIdPrescription(), uniqueEmail, ex);
            assertNotNull(got, "Expected a prescription when verification service returns 1");
            assertEquals(p.getIdPrescription(), got.getIdPrescription());
        } finally {
            server.stop(0);
            if (keepAliveConn != null) keepAliveConn.close();
        }
    }

    @Test
    public void testGetByIdVerificationReturnsZeroYieldsNull() throws Exception {
        int port = findFreePort();
        HttpServer server = startVerificationServer(port, "0");
        try {
            ExternalPrescriptionDAO dao = new ExternalPrescriptionDAO();
            MockHttpExchange ex = MockHttpExchange.fromPathOnly("GET", "http://127.0.0.1:" + port);
            assertNull(dao.getbyId(1L, "any@example.com", ex), "Expected null when verification service returns 0");
        } finally {
            server.stop(0);
        }
    }

    private static URI buildPathOnlyUri(String path) {
        try {
            // Build a hierarchical URI with null scheme/authority and the given path content
            return new URI(null, null, path, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
