package com.sources.app.handlers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.sources.app.dao.MedicineDAO;
import com.sources.app.entities.Medicine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;

public class SearchMedicineHandlerTest {

    private static class MockMedicineDAO extends MedicineDAO {

        private final List<Medicine> data;
        boolean throwOnGetAll;

        MockMedicineDAO(List<Medicine> data) {
            this.data = data;
        }

        @Override
        public Medicine create(String name, String activeMedicament, String description, String image,
                String concentration, Double presentacion, Integer stock, String brand,
                Boolean prescription, Double price, Integer soldUnits) {
            return null;
        }

        @Override
        public List<Medicine> getAll() {
            if (throwOnGetAll) throw new RuntimeException("boom");
            return data;
        }

        @Override
        public Medicine getById(Long id) {
            return null;
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
        public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
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
    public void testSearchMedicineHandlerInstantiation() {
        MedicineDAO mockDao = new MockMedicineDAO(List.of());
        SearchMedicineHandler handler = new SearchMedicineHandler(mockDao);
        assertNotNull(handler);
    }

    @Test
    public void testHandleSearchByActiveMedicament() throws Exception {
        List<Medicine> data = new ArrayList<>();
        data.add(new Medicine("DolorOff", "Paracetamol", "d", "i", "c", 1.0, 10, "b", false, 10.0, 0));
        data.add(new Medicine("Paracetamol Plus", "PARACETAMOL", "d", "i", "c", 1.0, 10, "b", false, 12.0, 0));
        data.add(new Medicine("Aspirina", "Ácido acetilsalicílico", "d", "i", "c", 1.0, 10, "b", false, 8.0, 0));

        SearchMedicineHandler handler = new SearchMedicineHandler(new MockMedicineDAO(data));
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicines/search?activeMedicament=paracetamol");
        handler.handle(ex);

        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        List<?> resp = mapper.readValue(ex.getResponseBytes(), new TypeReference<List<Object>>() {
        });
        assertEquals(2, resp.size());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testHandleSearchByName() throws Exception {
        List<Medicine> data = new ArrayList<>();
        data.add(new Medicine("Ibuprofeno Forte", "Ibuprofeno", "d", "i", "c", 1.0, 10, "b", false, 15.0, 0));
        data.add(new Medicine("Jarabe de Tos", "Dextrometorfano", "d", "i", "c", 1.0, 10, "b", false, 9.0, 0));
        data.add(new Medicine("Ibu Light", "Ibuprofeno", "d", "i", "c", 1.0, 10, "b", false, 7.0, 0));

        SearchMedicineHandler handler = new SearchMedicineHandler(new MockMedicineDAO(data));
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicines/search?name=ibu");
        handler.handle(ex);

        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        List<?> resp = mapper.readValue(ex.getResponseBytes(), new TypeReference<List<Object>>() {
        });
        assertEquals(2, resp.size());
    }

    @Test
    public void testHandleSearchNoParamsReturnsAll() throws Exception {
        List<Medicine> data = new ArrayList<>();
        data.add(new Medicine("A", "X", "d", "i", "c", 1.0, 10, "b", false, 1.0, 0));
        data.add(new Medicine("B", "Y", "d", "i", "c", 1.0, 10, "b", false, 2.0, 0));

        SearchMedicineHandler handler = new SearchMedicineHandler(new MockMedicineDAO(data));
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicines/search");
        handler.handle(ex);

        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        List<?> resp = mapper.readValue(ex.getResponseBytes(), new TypeReference<List<Object>>() {
        });
        assertEquals(2, resp.size());
    }

    @Test
    public void testHandleOptionsCors() throws Exception {
        SearchMedicineHandler handler = new SearchMedicineHandler(new MockMedicineDAO(List.of()));
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/medicines/search");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testHandleMethodNotAllowed() throws Exception {
        SearchMedicineHandler handler = new SearchMedicineHandler(new MockMedicineDAO(List.of()));
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/medicines/search");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
        String body = new String(ex.getResponseBytes());
        assertTrue(body.contains("Método no permitido"));
    }

    @Test
    public void testDaoExceptionOnGetAllWithParamsReturns500WithCors() throws Exception {
        List<Medicine> data = new ArrayList<>();
        MockMedicineDAO dao = new MockMedicineDAO(data);
        dao.throwOnGetAll = true;
        SearchMedicineHandler handler = new SearchMedicineHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicines/search?name=ibu");
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        String body = new String(ex.getResponseBytes());
        assertTrue(body.contains("Error interno al buscar medicamentos"));
        assertEquals("application/json", ex.getResponseHeaders().getFirst("Content-Type"));
    }

    @Test
    public void testDaoExceptionOnGetAllNoParamsReturns500WithCors() throws Exception {
        List<Medicine> data = new ArrayList<>();
        MockMedicineDAO dao = new MockMedicineDAO(data);
        dao.throwOnGetAll = true;
        SearchMedicineHandler handler = new SearchMedicineHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/medicines/search");
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        String body = new String(ex.getResponseBytes());
        assertTrue(body.contains("Error interno al buscar medicamentos"));
        assertEquals("application/json", ex.getResponseHeaders().getFirst("Content-Type"));
    }
}
