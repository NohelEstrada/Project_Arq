package com.sources.app.handlers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// TODO: Import necessary dependencies for the class under test (e.g., DAOs, Entities)
import com.sources.app.dao.SubcategoryDAO;
import com.sources.app.entities.Subcategory; // Assuming Subcategory entity is needed
import java.util.List;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

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

public class SubcategoryHandlerTest {

    // Inline mock DAO supporting success, failure and exception paths
    private static class MockSubcategoryDAO extends SubcategoryDAO {
        List<Subcategory> all = new ArrayList<>();
        boolean throwOnGetAll = false;
        boolean throwOnGetById = false;
        Subcategory byId;
        Subcategory createReturn;
        boolean createReturnsNull = false;
        Subcategory updateReturn;
        boolean throwOnCreate = false;
        boolean throwOnUpdate = false;

        @Override
        public List<Subcategory> getAll() {
            if (throwOnGetAll) {
                throw new RuntimeException("boom");
            }
            return all;
        }

        @Override
        public Subcategory getById(Long id) {
            if (throwOnGetById) {
                throw new RuntimeException("boom");
            }
            return byId;
        }

        @Override
        public Subcategory create(String name) {
            if (throwOnCreate) {
                throw new RuntimeException("boom");
            }
            if (createReturnsNull) return null;
            return createReturn;
        }

        @Override
        public Subcategory update(Subcategory subcategory) {
            if (throwOnUpdate) {
                throw new RuntimeException("boom");
            }
            return updateReturn;
        }
    }

    // Reusable MockHttpExchange (mirrors pattern in ExternalMedicineHandlerTest)
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

    @Test
    public void testSubcategoryHandlerInstantiation() {
        // TODO: Instantiate SubcategoryHandler with required dependencies.
        
        // Create mock DAO instance
        SubcategoryDAO mockDao = new MockSubcategoryDAO();
        // Instantiate the handler with mock DAO
        SubcategoryHandler instance = new SubcategoryHandler(mockDao);
        
        // Placeholder assertion - replace with actual test logic
        assertNotNull(instance, "Instance should not be null");
    }

    @Test
    public void testOptionsReturns204WithCORS() throws Exception {
        SubcategoryHandler handler = new SubcategoryHandler(new MockSubcategoryDAO());
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/subcategories");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, OPTIONS", ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods"));
        assertEquals("Content-Type, Authorization", ex.getResponseHeaders().getFirst("Access-Control-Allow-Headers"));
    }

    @Test
    public void testMethodNotAllowedDeleteReturns405() throws Exception {
        SubcategoryHandler handler = new SubcategoryHandler(new MockSubcategoryDAO());
        MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/subcategories");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, OPTIONS", ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods"));
        assertEquals("Content-Type, Authorization", ex.getResponseHeaders().getFirst("Access-Control-Allow-Headers"));
    }

    @Test
    public void testPostValidationNameMissingReturns400() throws Exception {
        SubcategoryHandler handler = new SubcategoryHandler(new MockSubcategoryDAO());
        String body = "{}";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/subcategories", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertEquals("{\"error\": \"Subcategory name is required\"}", resp);
    }

    @Test
    public void testPostMalformedJsonReturns500() throws Exception {
        SubcategoryHandler handler = new SubcategoryHandler(new MockSubcategoryDAO());
        String body = "not json";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/subcategories", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        // CORS headers still set
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testPostSuccessReturns201WithBody() throws Exception {
        MockSubcategoryDAO dao = new MockSubcategoryDAO();
        Subcategory created = new Subcategory();
        created.setIdSubcategory(42L);
        created.setName("Analgesics");
        dao.createReturn = created;
        SubcategoryHandler handler = new SubcategoryHandler(dao);

        String body = "{\"name\":\"Analgesics\"}";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/subcategories", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(201, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(resp.contains("\"idSubcategory\":42"));
        assertTrue(resp.contains("\"name\":\"Analgesics\""));
    }

    @Test
    public void testPostCreateFailureReturns400WithMessage() throws Exception {
        MockSubcategoryDAO dao = new MockSubcategoryDAO();
        dao.createReturnsNull = true;
        SubcategoryHandler handler = new SubcategoryHandler(dao);
        String body = "{\"name\":\"Anything\"}";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/subcategories", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertEquals("{\"error\": \"Failed to create subcategory\"}", resp);
    }

    @Test
    public void testGetAllReturns200JsonArray() throws Exception {
        MockSubcategoryDAO dao = new MockSubcategoryDAO();
        Subcategory a = new Subcategory(); a.setIdSubcategory(1L); a.setName("A");
        Subcategory b = new Subcategory(); b.setIdSubcategory(2L); b.setName("B");
        dao.all = new ArrayList<>(List.of(a, b));
        SubcategoryHandler handler = new SubcategoryHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/subcategories");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(resp.startsWith("["));
        assertTrue(resp.contains("\"idSubcategory\":1"));
        assertTrue(resp.contains("\"idSubcategory\":2"));
    }

    @Test
    public void testGetByIdSuccessReturns200() throws Exception {
        MockSubcategoryDAO dao = new MockSubcategoryDAO();
        Subcategory found = new Subcategory();
        found.setIdSubcategory(10L);
        found.setName("Found");
        dao.byId = found;
        SubcategoryHandler handler = new SubcategoryHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/subcategories?id=10");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(resp.contains("\"idSubcategory\":10"));
        assertTrue(resp.contains("\"name\":\"Found\""));
    }

    @Test
    public void testGetByIdNotFoundReturns404() throws Exception {
        MockSubcategoryDAO dao = new MockSubcategoryDAO();
        dao.byId = null;
        SubcategoryHandler handler = new SubcategoryHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/subcategories?id=999");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testGetByIdInvalidFormatReturns400WithMessage() throws Exception {
        SubcategoryHandler handler = new SubcategoryHandler(new MockSubcategoryDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/subcategories?id=abc");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertEquals("{\"error\": \"Invalid ID format\"}", resp);
    }

    @Test
    public void testPutValidationMissingIdReturns400() throws Exception {
        SubcategoryHandler handler = new SubcategoryHandler(new MockSubcategoryDAO());
        String body = "{\"name\":\"NewName\"}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/subcategories", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertEquals("{\"error\": \"Subcategory ID is required for update\"}", resp);
    }

    @Test
    public void testPutValidationMissingNameReturns400() throws Exception {
        SubcategoryHandler handler = new SubcategoryHandler(new MockSubcategoryDAO());
        String body = "{\"idSubcategory\":5}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/subcategories", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertEquals("{\"error\": \"Subcategory name is required for update\"}", resp);
    }

    @Test
    public void testPutMalformedJsonReturns500() throws Exception {
        SubcategoryHandler handler = new SubcategoryHandler(new MockSubcategoryDAO());
        String body = "{bad json";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/subcategories", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testPutUpdateSuccessReturns200WithBody() throws Exception {
        MockSubcategoryDAO dao = new MockSubcategoryDAO();
        Subcategory updated = new Subcategory();
        updated.setIdSubcategory(7L);
        updated.setName("Updated");
        dao.updateReturn = updated;
        SubcategoryHandler handler = new SubcategoryHandler(dao);

        String body = "{\"idSubcategory\":7,\"name\":\"Updated\"}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/subcategories", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(resp.contains("\"idSubcategory\":7"));
        assertTrue(resp.contains("\"name\":\"Updated\""));
    }

    @Test
    public void testPutUpdateFailureReturns400WithMessage() throws Exception {
        MockSubcategoryDAO dao = new MockSubcategoryDAO();
        dao.updateReturn = null;
        SubcategoryHandler handler = new SubcategoryHandler(dao);
        String body = "{\"idSubcategory\":8,\"name\":\"X\"}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/subcategories", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertEquals("{\"error\": \"Failed to update subcategory or subcategory not found\"}", resp);
    }

    @Test
    public void testDaoExceptionOnGetAllYields500() throws Exception {
        MockSubcategoryDAO dao = new MockSubcategoryDAO();
        dao.throwOnGetAll = true;
        SubcategoryHandler handler = new SubcategoryHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/subcategories");
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
    }

    @Test
    public void testDaoExceptionOnPostYields500() throws Exception {
        MockSubcategoryDAO dao = new MockSubcategoryDAO();
        dao.throwOnCreate = true;
        SubcategoryHandler handler = new SubcategoryHandler(dao);
        String body = "{\"name\":\"X\"}";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/subcategories", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testDaoExceptionOnPutYields500() throws Exception {
        MockSubcategoryDAO dao = new MockSubcategoryDAO();
        dao.throwOnUpdate = true;
        SubcategoryHandler handler = new SubcategoryHandler(dao);
        String body = "{\"idSubcategory\":1,\"name\":\"Y\"}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/subcategories", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testDaoExceptionOnGetByIdYields500() throws Exception {
        MockSubcategoryDAO dao = new MockSubcategoryDAO();
        dao.throwOnGetById = true;
        SubcategoryHandler handler = new SubcategoryHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/subcategories?id=1");
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }
}

