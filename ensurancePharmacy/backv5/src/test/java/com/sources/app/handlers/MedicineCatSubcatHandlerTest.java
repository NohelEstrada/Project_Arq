package com.sources.app.handlers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// TODO: Import necessary dependencies for the class under test (e.g., DAOs, Entities)
import com.sources.app.dao.MedicineCatSubcatDAO;
import com.sources.app.entities.MedicineCatSubcat; // Assuming entity is needed
import com.sources.app.entities.MedicineCatSubcatId;
import com.sources.app.entities.Medicine;
import com.sources.app.entities.Category;
import com.sources.app.entities.Subcategory;
import java.util.List;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

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

public class MedicineCatSubcatHandlerTest {

    // Simple inline mock for MedicineCatSubcatDAO
    private static class MockMedicineCatSubcatDAO extends MedicineCatSubcatDAO {
        List<MedicineCatSubcat> all = List.of();
        MedicineCatSubcat byId;
        MedicineCatSubcat createReturn;
        MedicineCatSubcat updateReturn;
        boolean failCreate = false;
        boolean failUpdate = false;
        boolean throwOnGetAll = false;
        boolean throwOnGetById = false;
        boolean throwOnCreate = false;
        boolean throwOnUpdate = false;

        @Override
        public MedicineCatSubcat create(Medicine medicine, Category category, Subcategory subcategory) {
            if (throwOnCreate) throw new RuntimeException("boom");
            if (failCreate) return null;
            if (createReturn != null) return createReturn;
            return new MedicineCatSubcat(medicine, category, subcategory);
        }
        @Override
        public List<MedicineCatSubcat> getAll() {
            if (throwOnGetAll) throw new RuntimeException("boom");
            return all;
        }
        @Override
        public MedicineCatSubcat getById(MedicineCatSubcatId id) {
            if (throwOnGetById) throw new RuntimeException("boom");
            return byId;
        }
        @Override
        public MedicineCatSubcat update(MedicineCatSubcat mcs) {
            if (throwOnUpdate) throw new RuntimeException("boom");
            if (failUpdate) return null;
            return updateReturn != null ? updateReturn : mcs;
        }
    }

    // Reusable MockHttpExchange similar to other handler tests
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
    public void testMedicineCatSubcatHandlerInstantiation() {
        // TODO: Instantiate MedicineCatSubcatHandler with required dependencies.
        
        // Create mock DAO instance
        MedicineCatSubcatDAO mockDao = new MockMedicineCatSubcatDAO();
        // Instantiate the handler with mock DAO
        MedicineCatSubcatHandler instance = new MedicineCatSubcatHandler(mockDao);
        
        // Placeholder assertion - replace with actual test logic
        assertNotNull(instance, "Instance should not be null");
    }

    @Test
    public void testOptionsCors() throws Exception {
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(new MockMedicineCatSubcatDAO());
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/medicine_catsubcats");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, OPTIONS", ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods"));
        assertEquals("Content-Type, Authorization", ex.getResponseHeaders().getFirst("Access-Control-Allow-Headers"));
    }

    @Test
    public void testMethodNotAllowedDelete() throws Exception {
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(new MockMedicineCatSubcatDAO());
        MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/medicine_catsubcats");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, OPTIONS", ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods"));
        assertEquals("Content-Type, Authorization", ex.getResponseHeaders().getFirst("Access-Control-Allow-Headers"));
    }

    @Test
    public void testPostValidationMissingIds() throws Exception {
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(new MockMedicineCatSubcatDAO());
        byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/medicine_catsubcats", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Medicine ID, Category ID, and Subcategory ID are required"));
    }

    @Test
    public void testPostMalformedJsonReturns500() throws Exception {
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(new MockMedicineCatSubcatDAO());
        byte[] body = "{not-json".getBytes(StandardCharsets.UTF_8);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/medicine_catsubcats", body);
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
    }

    @Test
    public void testPostCreateSuccess() throws Exception {
        MockMedicineCatSubcatDAO dao = new MockMedicineCatSubcatDAO();
        Medicine med = new Medicine(); med.setIdMedicine(1L);
        Category cat = new Category(); cat.setIdCategory(2L);
        Subcategory sub = new Subcategory(); sub.setIdSubcategory(3L);
        dao.createReturn = new MedicineCatSubcat(med, cat, sub);
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(dao);

        String json = "{\"medicine\":{\"idMedicine\":1},\"category\":{\"idCategory\":2},\"subcategory\":{\"idSubcategory\":3}}";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/medicine_catsubcats", json.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(201, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        ObjectMapper mapper = new ObjectMapper();
        MedicineCatSubcat resp = mapper.readValue(ex.getResponseBytes(), MedicineCatSubcat.class);
        assertNotNull(resp.getId());
        assertEquals(1L, resp.getId().getMedicineId());
        assertEquals(2L, resp.getId().getCategoryId());
        assertEquals(3L, resp.getId().getSubcategoryId());
    }

    @Test
    public void testPostCreateFailsReturns400() throws Exception {
        MockMedicineCatSubcatDAO dao = new MockMedicineCatSubcatDAO();
        dao.failCreate = true;
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(dao);

        String json = "{\"medicine\":{\"idMedicine\":1},\"category\":{\"idCategory\":2},\"subcategory\":{\"idSubcategory\":3}}";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/medicine_catsubcats", json.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Failed to create association"));
    }

    @Test
    public void testGetAllReturnsList() throws Exception {
        MockMedicineCatSubcatDAO dao = new MockMedicineCatSubcatDAO();
        Medicine med = new Medicine(); med.setIdMedicine(1L);
        Category cat = new Category(); cat.setIdCategory(2L);
        Subcategory sub = new Subcategory(); sub.setIdSubcategory(3L);
        MedicineCatSubcat a = new MedicineCatSubcat(med, cat, sub);
        MedicineCatSubcat b = new MedicineCatSubcat(med, cat, sub);
        dao.all = new ArrayList<>(List.of(a, b));
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicine_catsubcats");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        ObjectMapper mapper = new ObjectMapper();
        MedicineCatSubcat[] resp = mapper.readValue(ex.getResponseBytes(), MedicineCatSubcat[].class);
        assertEquals(2, resp.length);
    }

    @Test
    public void testGetByIdFound() throws Exception {
        MockMedicineCatSubcatDAO dao = new MockMedicineCatSubcatDAO();
        Medicine med = new Medicine(); med.setIdMedicine(10L);
        Category cat = new Category(); cat.setIdCategory(20L);
        Subcategory sub = new Subcategory(); sub.setIdSubcategory(30L);
        dao.byId = new MedicineCatSubcat(med, cat, sub);
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicine_catsubcats?id=10,20,30");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        MedicineCatSubcat resp = mapper.readValue(ex.getResponseBytes(), MedicineCatSubcat.class);
        assertEquals(10L, resp.getId().getMedicineId());
        assertEquals(20L, resp.getId().getCategoryId());
        assertEquals(30L, resp.getId().getSubcategoryId());
    }

    @Test
    public void testGetByIdNotFound() throws Exception {
        MockMedicineCatSubcatDAO dao = new MockMedicineCatSubcatDAO();
        dao.byId = null;
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicine_catsubcats?id=1,2,3");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testGetByIdInvalidCompositeIdFormat() throws Exception {
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(new MockMedicineCatSubcatDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicine_catsubcats?id=1,xyz,3");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String body = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(body.contains("Invalid composite ID format"));
    }

    @Test
    public void testGetByIdInvalidParamFormat() throws Exception {
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(new MockMedicineCatSubcatDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicine_catsubcats?id=1,2");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String body = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(body.contains("Invalid ID parameter format"));
    }

    @Test
    public void testPutValidationMissingCompositeId() throws Exception {
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(new MockMedicineCatSubcatDAO());
        String json = "{\"medicine\":{\"idMedicine\":1}}"; // id missing
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/medicine_catsubcats", json.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Composite ID (medId, catId, subcatId) is required for update"));
    }

    @Test
    public void testPutMalformedJsonReturns500() throws Exception {
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(new MockMedicineCatSubcatDAO());
        byte[] body = "{not-json".getBytes(StandardCharsets.UTF_8);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/medicine_catsubcats", body);
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
    }

    @Test
    public void testPutUpdateSuccess() throws Exception {
        MockMedicineCatSubcatDAO dao = new MockMedicineCatSubcatDAO();
        MedicineCatSubcatId id = new MedicineCatSubcatId(5L, 6L, 7L);
        MedicineCatSubcat updated = new MedicineCatSubcat();
        updated.setId(id);
        dao.updateReturn = updated;
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(dao);

        String json = "{\"id\":{\"medicineId\":5,\"categoryId\":6,\"subcategoryId\":7}}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/medicine_catsubcats", json.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(ex.getResponseBytes());
        assertEquals(5L, node.get("id").get("medicineId").asLong());
        assertEquals(6L, node.get("id").get("categoryId").asLong());
        assertEquals(7L, node.get("id").get("subcategoryId").asLong());
    }

    @Test
    public void testPutUpdateFailsReturns400() throws Exception {
        MockMedicineCatSubcatDAO dao = new MockMedicineCatSubcatDAO();
        dao.failUpdate = true;
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(dao);
        String json = "{\"id\":{\"medicineId\":1,\"categoryId\":2,\"subcategoryId\":3}}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/medicine_catsubcats", json.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Failed to update association or association not found"));
    }

    @Test
    public void testDaoExceptionOnGetAllYields500() throws Exception {
        MockMedicineCatSubcatDAO dao = new MockMedicineCatSubcatDAO();
        dao.throwOnGetAll = true;
        MedicineCatSubcatHandler handler = new MedicineCatSubcatHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicine_catsubcats");
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
    }
}
