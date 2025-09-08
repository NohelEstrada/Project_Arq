package com.sources.app.handlers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.sources.app.dao.OrderMedicineDAO;
import com.sources.app.entities.Medicine;
import com.sources.app.entities.OrderMedicine;
import com.sources.app.entities.OrderMedicineId;
import com.sources.app.entities.Orders;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

public class OrderMedicineHandlerTest {

    private static class MockOMDAO extends OrderMedicineDAO {

        List<OrderMedicine> data = new ArrayList<>();

        @Override
        public OrderMedicine create(Orders orders, Medicine medicine, Integer quantity, Double cost, String total) {
            OrderMedicine om = new OrderMedicine();
            om.setOrders(orders);
            om.setMedicine(medicine);
            om.setQuantity(quantity);
            om.setCost(cost);
            om.setTotal(total);
            om.setId(new OrderMedicineId(orders.getIdOrder(), medicine.getIdMedicine()));
            data.add(om);
            return om;
        }

        @Override
        public List<OrderMedicine> getAll() {
            return data;
        }

        @Override
        public OrderMedicine getById(OrderMedicineId id) {
            return data.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
        }

        @Override
        public OrderMedicine update(OrderMedicine om) {
            return getById(om.getId()) == null ? null : om;
        }

        @Override
        public boolean deleteById(OrderMedicineId id) {
            return data.removeIf(x -> x.getId().equals(id));
        }
    }

    private static class MockOMDAOCreateFail extends MockOMDAO {
        @Override
        public OrderMedicine create(Orders orders, Medicine medicine, Integer quantity, Double cost, String total) {
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
    public void testOptionsCors() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/order_medicines");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testPostMissingIds() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        String body = "{\"quantity\":1}";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/order_medicines", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertTrue(new String(ex.getResponseBytes()).contains("Order ID and Medicine ID are required"));
    }

    @Test
    public void testGetByIdInvalidComposite() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/order_medicines?id=1,abc");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertTrue(new String(ex.getResponseBytes()).contains("Invalid composite ID format"));
    }

    @Test
    public void testGetByIdNotFound() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/order_medicines?id=1,2");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testPutMissingCompositeId() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        String body = "{\"quantity\":2}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/order_medicines", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertTrue(new String(ex.getResponseBytes()).contains("Composite ID (orderId, medicineId) is required for update"));
    }

    @Test
    public void testDeleteMissingParam() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/order_medicines");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertTrue(new String(ex.getResponseBytes()).contains("Composite ID (id=orderId,medicineId) is required for delete"));
    }

    @Test
    public void testMethodNotAllowed() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        MockHttpExchange ex = new MockHttpExchange("PATCH", "http://localhost/api2/order_medicines");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
    }

    @Test
    public void testPostSuccess() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        String body = "{" +
                "\"orders\":{\"idOrder\":1}," +
                "\"medicine\":{\"idMedicine\":2}," +
                "\"quantity\":3," +
                "\"cost\":12.5," +
                "\"total\":\"37.5\"" +
                "}";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/order_medicines", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(201, ex.getResponseCode());
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(resp.contains("\"orders\""));
        assertTrue(resp.contains("\"medicine\""));
    }

    @Test
    public void testPostCreateFails() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAOCreateFail());
        String body = "{" +
                "\"orders\":{\"idOrder\":1}," +
                "\"medicine\":{\"idMedicine\":2}," +
                "\"quantity\":3," +
                "\"cost\":12.5," +
                "\"total\":\"37.5\"" +
                "}";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/order_medicines", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(resp.contains("Failed to create order-medicine association"));
    }

    @Test
    public void testGetAllSuccess() throws Exception {
        MockOMDAO dao = new MockOMDAO();
        // Pre-populate
        Orders o = new Orders();
        o.setIdOrder(1L);
        Medicine m = new Medicine();
        m.setIdMedicine(2L);
        dao.create(o, m, 1, 10.0, "10.0");

        OrderMedicineHandler handler = new OrderMedicineHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/order_medicines");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(resp.startsWith("["));
        assertTrue(resp.contains("\"orders\""));
    }

    @Test
    public void testGetByIdSuccess() throws Exception {
        MockOMDAO dao = new MockOMDAO();
        Orders o = new Orders();
        o.setIdOrder(1L);
        Medicine m = new Medicine();
        m.setIdMedicine(2L);
        dao.create(o, m, 1, 10.0, "10.0");

        OrderMedicineHandler handler = new OrderMedicineHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/order_medicines?id=1,2");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(resp.contains("\"orders\""));
    }

    @Test
    public void testGetByIdInvalidParamFormat() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/order_medicines?id=1");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(resp.contains("Invalid ID parameter format"));
    }

    @Test
    public void testPutSuccess() throws Exception {
        MockOMDAO dao = new MockOMDAO();
        Orders o = new Orders();
        o.setIdOrder(1L);
        Medicine m = new Medicine();
        m.setIdMedicine(2L);
        dao.create(o, m, 1, 10.0, "10.0");

        OrderMedicineHandler handler = new OrderMedicineHandler(dao);
        String body = "{" +
                "\"id\":{\"orderId\":1,\"medicineId\":2}," +
                "\"orders\":{\"idOrder\":1}," +
                "\"medicine\":{\"idMedicine\":2}," +
                "\"quantity\":5," +
                "\"cost\":12.5," +
                "\"total\":\"62.5\"" +
                "}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/order_medicines", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(resp.contains("\"quantity\":5"));
    }

    @Test
    public void testPutNotFound() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        String body = "{" +
                "\"id\":{\"orderId\":99,\"medicineId\":88}," +
                "\"orders\":{\"idOrder\":99}," +
                "\"medicine\":{\"idMedicine\":88}," +
                "\"quantity\":1," +
                "\"cost\":1.0," +
                "\"total\":\"1.0\"" +
                "}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/order_medicines", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(resp.contains("Failed to update order-medicine association"));
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        MockOMDAO dao = new MockOMDAO();
        Orders o = new Orders();
        o.setIdOrder(1L);
        Medicine m = new Medicine();
        m.setIdMedicine(2L);
        dao.create(o, m, 1, 10.0, "10.0");

        OrderMedicineHandler handler = new OrderMedicineHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/order_medicines?id=1,2");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
    }

    @Test
    public void testDeleteNotFound() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/order_medicines?id=9,8");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testDeleteInvalidIdFormat() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/order_medicines?id=1,abc");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String resp = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(resp.contains("Invalid composite ID format"));
    }

    @Test
    public void testPostMalformedJsonReturns500() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        String body = "{\"orders\":{\"idOrder\":1},"; // malformed JSON
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/order_medicines", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
    }

    @Test
    public void testPutMalformedJsonReturns500() throws Exception {
        OrderMedicineHandler handler = new OrderMedicineHandler(new MockOMDAO());
        String body = "{\"id\":{\"orderId\":1,\"medicineId\":2}"; // malformed JSON
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/order_medicines", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
    }
}
