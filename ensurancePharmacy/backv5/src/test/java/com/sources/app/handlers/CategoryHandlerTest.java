package com.sources.app.handlers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// TODO: Import necessary dependencies for the class under test (e.g., DAOs, Entities)
import com.sources.app.dao.CategoryDAO;
import com.sources.app.entities.Category; // Assuming Category entity is needed
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CategoryHandlerTest {

    private static class MockCategoryDAO extends CategoryDAO {
        List<Category> all = List.of();
        Category byId;
        Category createReturn;
        Category updateReturn;

        @Override
        public Category create(String name) { return createReturn; }

        @Override
        public List<Category> getAll() { return all; }

        @Override
        public Category getById(Long id) { return byId; }

        @Override
        public Category update(Category c) { return updateReturn; }
    }

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
    public void testOptionsCors() throws Exception {
        CategoryHandler handler = new CategoryHandler(new MockCategoryDAO());
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/categories");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, OPTIONS", ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods"));
    }

    @Test
    public void testMethodNotAllowed() throws Exception {
        CategoryHandler handler = new CategoryHandler(new MockCategoryDAO());
        MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/categories");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
    }

    @Test
    public void testPostCreateSuccess() throws Exception {
        MockCategoryDAO dao = new MockCategoryDAO();
        Category created = new Category();
        created.setIdCategory(1L);
        created.setName("Analgesics");
        dao.createReturn = created;
        CategoryHandler handler = new CategoryHandler(dao);

        Category req = new Category();
        req.setName("Analgesics");
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/categories", body);
        handler.handle(ex);

        assertEquals(201, ex.getResponseCode());
        Category resp = mapper.readValue(ex.getResponseBytes(), Category.class);
        assertEquals(1L, resp.getIdCategory());
        assertEquals("Analgesics", resp.getName());
    }

    @Test
    public void testPostMissingName() throws Exception {
        CategoryHandler handler = new CategoryHandler(new MockCategoryDAO());
        Category req = new Category();
        // no name
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/categories", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Category name is required"));
    }

    @Test
    public void testPostBlankName() throws Exception {
        CategoryHandler handler = new CategoryHandler(new MockCategoryDAO());
        Category req = new Category(); req.setName("   ");
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/categories", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Category name is required"));
    }

    @Test
    public void testPostCreateFails() throws Exception {
        MockCategoryDAO dao = new MockCategoryDAO();
        dao.createReturn = null;
        CategoryHandler handler = new CategoryHandler(dao);

        Category req = new Category(); req.setName("X");
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/categories", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Failed to create category"));
    }

    @Test
    public void testPostMalformedJsonReturns500() throws Exception {
        CategoryHandler handler = new CategoryHandler(new MockCategoryDAO());
        byte[] body = "{not-json".getBytes(StandardCharsets.UTF_8);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/categories", body);
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
    }

    @Test
    public void testGetAllReturnsList() throws Exception {
        MockCategoryDAO dao = new MockCategoryDAO();
        ArrayList<Category> data = new ArrayList<>();
        Category a = new Category(); a.setIdCategory(1L); a.setName("A");
        Category b = new Category(); b.setIdCategory(2L); b.setName("B");
        data.add(a); data.add(b);
        dao.all = data;
        CategoryHandler handler = new CategoryHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/categories");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        Category[] resp = mapper.readValue(ex.getResponseBytes(), Category[].class);
        assertEquals(2, resp.length);
    }

    @Test
    public void testGetByIdFound() throws Exception {
        MockCategoryDAO dao = new MockCategoryDAO();
        Category c = new Category(); c.setIdCategory(5L); c.setName("C");
        dao.byId = c;
        CategoryHandler handler = new CategoryHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/categories?id=5");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        Category resp = mapper.readValue(ex.getResponseBytes(), Category.class);
        assertEquals(5L, resp.getIdCategory());
    }

    @Test
    public void testGetByIdNotFound() throws Exception {
        MockCategoryDAO dao = new MockCategoryDAO();
        dao.byId = null;
        CategoryHandler handler = new CategoryHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/categories?id=999");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testGetByIdInvalidFormat() throws Exception {
        CategoryHandler handler = new CategoryHandler(new MockCategoryDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/categories?id=abc");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String body = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(body.contains("Invalid ID format"));
    }

    @Test
    public void testPutUpdateSuccess() throws Exception {
        MockCategoryDAO dao = new MockCategoryDAO();
        Category updated = new Category();
        updated.setIdCategory(7L);
        updated.setName("Updated");
        dao.updateReturn = updated;
        CategoryHandler handler = new CategoryHandler(dao);

        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(updated);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/categories", body);
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        Category resp = mapper.readValue(ex.getResponseBytes(), Category.class);
        assertEquals(7L, resp.getIdCategory());
        assertEquals("Updated", resp.getName());
    }

    @Test
    public void testPutMissingId() throws Exception {
        CategoryHandler handler = new CategoryHandler(new MockCategoryDAO());
        Category req = new Category();
        req.setName("X");
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/categories", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Category ID is required for update"));
    }

    @Test
    public void testPutMissingName() throws Exception {
        CategoryHandler handler = new CategoryHandler(new MockCategoryDAO());
        Category req = new Category();
        req.setIdCategory(7L);
        // no name
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/categories", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Category name is required for update"));
    }

    @Test
    public void testPutBlankName() throws Exception {
        CategoryHandler handler = new CategoryHandler(new MockCategoryDAO());
        Category req = new Category();
        req.setIdCategory(7L);
        req.setName("   ");
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/categories", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Category name is required for update"));
    }

    @Test
    public void testPutUpdateFails() throws Exception {
        MockCategoryDAO dao = new MockCategoryDAO();
        dao.updateReturn = null;
        CategoryHandler handler = new CategoryHandler(dao);
        Category req = new Category();
        req.setIdCategory(7L);
        req.setName("X");
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/categories", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Failed to update category or category not found"));
    }

    @Test
    public void testPutMalformedJsonReturns500() throws Exception {
        CategoryHandler handler = new CategoryHandler(new MockCategoryDAO());
        byte[] body = "{not-json".getBytes(StandardCharsets.UTF_8);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/categories", body);
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
    }
}
