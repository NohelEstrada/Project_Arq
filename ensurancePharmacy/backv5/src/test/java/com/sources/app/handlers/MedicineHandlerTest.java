package com.sources.app.handlers;

 import org.junit.jupiter.api.Test;
 import static org.junit.jupiter.api.Assertions.*;
 
 import com.sources.app.dao.MedicineDAO;
 import com.sources.app.entities.Medicine;
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.sun.net.httpserver.Headers;
 import com.sun.net.httpserver.HttpContext;
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpPrincipal;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;

public class MedicineHandlerTest {

    private static class MockMedicineDAO extends MedicineDAO {
        List<Medicine> all = List.of();
        Medicine byId;
        Medicine createReturn;
        Medicine updateReturn;
        boolean throwOnGetAll;
        boolean throwOnGetById;
        boolean throwOnCreate;
        boolean throwOnUpdate;

        @Override
        public Medicine create(String name, String activeMedicament, String description, String image,
                               String concentration, Double presentacion, Integer stock, String brand,
                               Boolean prescription, Double price, Integer soldUnits) {
            if (throwOnCreate) throw new RuntimeException("boom");
            return createReturn;
        }

        @Override
        public List<Medicine> getAll() {
            if (throwOnGetAll) throw new RuntimeException("boom");
            return all;
        }

        @Override
        public Medicine getById(Long id) {
            if (throwOnGetById) throw new RuntimeException("boom");
            return byId;
        }

        @Override
        public Medicine update(Medicine medicine) {
            if (throwOnUpdate) throw new RuntimeException("boom");
            return updateReturn;
        }
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
        public Headers getRequestHeaders() { return requestHeaders; }

        @Override
        public Headers getResponseHeaders() { return responseHeaders; }

        @Override
        public URI getRequestURI() { return uri; }

        @Override
        public String getRequestMethod() { return method; }

        @Override
        public HttpContext getHttpContext() { return null; }

        @Override
        public void close() {}

        @Override
        public InputStream getRequestBody() { return requestBody; }

        @Override
        public OutputStream getResponseBody() { return responseBody; }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) throws IOException { this.responseCode = rCode; }

        @Override
        public InetSocketAddress getRemoteAddress() { return new InetSocketAddress(0); }

        @Override
        public int getResponseCode() { return responseCode; }

        @Override
        public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }

        @Override
        public String getProtocol() { return "HTTP/1.1"; }

        @Override
        public Object getAttribute(String name) { return null; }

        @Override
        public void setAttribute(String name, Object value) {}

        @Override
        public void setStreams(InputStream i, OutputStream o) {}

        @Override
        public HttpPrincipal getPrincipal() { return null; }

        byte[] getResponseBytes() { return responseBody.toByteArray(); }
    }

    @Test
    public void testInstantiation() {
        MedicineHandler handler = new MedicineHandler(new MockMedicineDAO());
        assertNotNull(handler);
    }

    @Test
    public void testOptionsCors() throws Exception {
        MedicineHandler handler = new MedicineHandler(new MockMedicineDAO());
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/medicines");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, OPTIONS", ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods"));
    }

    @Test
    public void testGetAllReturnsList() throws Exception {
        List<Medicine> data = new ArrayList<>();
        data.add(new Medicine("A", "X", "d", "i", "c", 1.0, 10, "b", false, 1.0, 0));
        data.add(new Medicine("B", "Y", "d", "i", "c", 1.0, 20, "b", true, 2.0, 0));
        MockMedicineDAO dao = new MockMedicineDAO();
        dao.all = data;
        MedicineHandler handler = new MedicineHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicines");
        handler.handle(ex);

        assertEquals(200, ex.getResponseCode());
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
        ObjectMapper mapper = new ObjectMapper();
        List<?> resp = mapper.readValue(ex.getResponseBytes(), new TypeReference<List<Object>>(){});
        assertEquals(2, resp.size());
    }

    @Test
    public void testGetByIdFound() throws Exception {
        MockMedicineDAO dao = new MockMedicineDAO();
        Medicine m = new Medicine("DolorOff", "Paracetamol", "d", "i", "c", 1.0, 10, "b", false, 10.0, 0);
        m.setIdMedicine(5L);
        dao.byId = m;
        MedicineHandler handler = new MedicineHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicines/5");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        Medicine resp = mapper.readValue(ex.getResponseBytes(), Medicine.class);
        assertEquals(5L, resp.getIdMedicine());
        assertEquals("DolorOff", resp.getName());
    }

    @Test
    public void testGetByIdNotFound() throws Exception {
        MockMedicineDAO dao = new MockMedicineDAO();
        dao.byId = null;
        MedicineHandler handler = new MedicineHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicines/999");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testGetInvalidIdFormat() throws Exception {
        MedicineHandler handler = new MedicineHandler(new MockMedicineDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicines/abc");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String body = new String(ex.getResponseBytes());
        assertTrue(body.isEmpty() || body.contains("Invalid ID format"));
    }

    @Test
    public void testPostCreateSuccess() throws Exception {
        MockMedicineDAO dao = new MockMedicineDAO();
        Medicine created = new Medicine("Nuevo", "Act", "d", "i", "c", 1.0, 5, "b", false, 3.0, 0);
        created.setIdMedicine(1L);
        dao.createReturn = created;
        MedicineHandler handler = new MedicineHandler(dao);

        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(new Medicine("Nuevo", "Act", "d", "i", "c", 1.0, 5, "b", false, 3.0, 0));
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/medicines", body);
        handler.handle(ex);

        assertEquals(201, ex.getResponseCode());
        Medicine resp = mapper.readValue(ex.getResponseBytes(), Medicine.class);
        assertEquals(1L, resp.getIdMedicine());
        assertEquals("Nuevo", resp.getName());
    }

    @Test
    public void testPostCreateReturnsBadRequestWhenDaoReturnsNull() throws Exception {
        MockMedicineDAO dao = new MockMedicineDAO();
        dao.createReturn = null;
        MedicineHandler handler = new MedicineHandler(dao);

        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(new Medicine("Nuevo", "Act", "d", "i", "c", 1.0, 5, "b", false, 3.0, 0));
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/medicines", body);
        handler.handle(ex);

        assertEquals(400, ex.getResponseCode());
        String resp = new String(ex.getResponseBytes());
        assertTrue(resp.contains("Failed to create medicine"));
        assertEquals("application/json; charset=UTF-8", ex.getResponseHeaders().getFirst("Content-Type"));
    }

    @Test
    public void testPostToIdPathReturnsBadRequest() throws Exception {
        MedicineHandler handler = new MedicineHandler(new MockMedicineDAO());
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/medicines/1", new byte[0]);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
    }

    @Test
    public void testPostMalformedJsonReturns500WithCors() throws Exception {
        MedicineHandler handler = new MedicineHandler(new MockMedicineDAO());
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/medicines", "not-json".getBytes());
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testPutUpdateSuccess() throws Exception {
        MockMedicineDAO dao = new MockMedicineDAO();
        Medicine updated = new Medicine("Upd", "Act", "d", "i", "c", 1.0, 5, "b", true, 5.0, 1);
        updated.setIdMedicine(7L);
        dao.updateReturn = updated;
        MedicineHandler handler = new MedicineHandler(dao);

        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(new Medicine("Upd", "Act", "d", "i", "c", 1.0, 5, "b", true, 5.0, 1));
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/medicines/7", body);
        handler.handle(ex);

        assertEquals(200, ex.getResponseCode());
        Medicine resp = mapper.readValue(ex.getResponseBytes(), Medicine.class);
        assertEquals(7L, resp.getIdMedicine());
    }

    @Test
    public void testPutUpdateNotFound() throws Exception {
        MockMedicineDAO dao = new MockMedicineDAO();
        dao.updateReturn = null;
        MedicineHandler handler = new MedicineHandler(dao);

        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(new Medicine("X", "Y", "d", "i", "c", 1.0, 5, "b", true, 5.0, 1));
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/medicines/123", body);
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testPutInvalidIdFormat() throws Exception {
        MedicineHandler handler = new MedicineHandler(new MockMedicineDAO());
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(new Medicine("X", "Y", "d", "i", "c", 1.0, 5, "b", true, 5.0, 1));
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/medicines/abc", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String resp = new String(ex.getResponseBytes());
        assertTrue(resp.isEmpty() || resp.contains("Invalid ID format"));
    }

    @Test
    public void testInvalidPathNotFound() throws Exception {
        MedicineHandler handler = new MedicineHandler(new MockMedicineDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/other");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testMethodNotAllowed() throws Exception {
        MedicineHandler handler = new MedicineHandler(new MockMedicineDAO());
        MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/medicines");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
    }

    @Test
    public void testDaoExceptionOnGetAllReturns500WithCors() throws Exception {
        MockMedicineDAO dao = new MockMedicineDAO();
        dao.throwOnGetAll = true;
        MedicineHandler handler = new MedicineHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicines");
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testDaoExceptionOnGetByIdReturns500WithCors() throws Exception {
        MockMedicineDAO dao = new MockMedicineDAO();
        dao.throwOnGetById = true;
        MedicineHandler handler = new MedicineHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicines/1");
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testDaoExceptionOnPutReturns500WithCors() throws Exception {
        MockMedicineDAO dao = new MockMedicineDAO();
        dao.throwOnUpdate = true;
        MedicineHandler handler = new MedicineHandler(dao);
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(new Medicine("X", "Y", "d", "i", "c", 1.0, 5, "b", true, 5.0, 1));
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/medicines/1", body);
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }
}
