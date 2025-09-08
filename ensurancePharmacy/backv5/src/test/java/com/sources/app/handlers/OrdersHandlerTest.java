package com.sources.app.handlers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.sources.app.dao.OrdersDAO;
import com.sources.app.entities.Orders;
import com.sources.app.entities.User;
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

public class OrdersHandlerTest {

    private static class MockOrdersDAO extends OrdersDAO {
        List<Orders> all = List.of();
        Orders byId;
        Orders createReturn;
        Orders updateReturn;
        Orders lastUpdated;
        boolean throwOnCreate;
        boolean throwOnGetAll;
        boolean throwOnGetById;
        boolean throwOnUpdate;

        @Override
        public Orders create(String status, Long idUser) {
            if (throwOnCreate) throw new RuntimeException("boom");
            return createReturn;
        }

        @Override
        public List<Orders> getAll() {
            if (throwOnGetAll) throw new RuntimeException("boom");
            return all;
        }

        @Override
        public Orders getById(Long id) {
            if (throwOnGetById) throw new RuntimeException("boom");
            return byId;
        }

        @Override
        public Orders update(Orders order) {
            if (throwOnUpdate) throw new RuntimeException("boom");
            lastUpdated = order;
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
        OrdersHandler handler = new OrdersHandler(new MockOrdersDAO());
        assertNotNull(handler);
    }

    @Test
    public void testOptionsCors() throws Exception {
        OrdersHandler handler = new OrdersHandler(new MockOrdersDAO());
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/orders");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, OPTIONS", ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods"));
    }

    @Test
    public void testPostCreateSuccess() throws Exception {
        MockOrdersDAO dao = new MockOrdersDAO();
        Orders created = new Orders("PENDING");
        created.setIdOrder(1L);
        User u = new User(); u.setIdUser(42L);
        created.setUser(u);
        dao.createReturn = created;
        OrdersHandler handler = new OrdersHandler(dao);

        Orders bodyObj = new Orders("PENDING");
        User bodyUser = new User(); bodyUser.setIdUser(42L);
        bodyObj.setUser(bodyUser);
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(bodyObj);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/orders", body);
        handler.handle(ex);

        assertEquals(201, ex.getResponseCode());
        Orders resp = mapper.readValue(ex.getResponseBytes(), Orders.class);
        assertEquals(1L, resp.getIdOrder());
        assertEquals("PENDING", resp.getStatus());
        assertNotNull(resp.getUser());
        assertEquals(42L, resp.getUser().getIdUser());
    }

    @Test
    public void testPostMissingUser() throws Exception {
        OrdersHandler handler = new OrdersHandler(new MockOrdersDAO());
        Orders bodyObj = new Orders("PENDING");
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(bodyObj);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/orders", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes());
        assertTrue(msg.contains("User ID is required"));
    }

    @Test
    public void testPostMissingUserId() throws Exception {
        OrdersHandler handler = new OrdersHandler(new MockOrdersDAO());
        Orders bodyObj = new Orders("PENDING");
        bodyObj.setUser(new User()); // id null
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(bodyObj);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/orders", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes());
        assertTrue(msg.contains("User ID is required"));
    }

    @Test
    public void testPostMissingStatus() throws Exception {
        OrdersHandler handler = new OrdersHandler(new MockOrdersDAO());
        Orders bodyObj = new Orders();
        User bodyUser = new User(); bodyUser.setIdUser(42L);
        bodyObj.setUser(bodyUser);
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(bodyObj);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/orders", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes());
        assertTrue(msg.contains("Order status is required"));
    }

    @Test
    public void testPostBlankStatus() throws Exception {
        OrdersHandler handler = new OrdersHandler(new MockOrdersDAO());
        Orders bodyObj = new Orders("   ");
        User bodyUser = new User(); bodyUser.setIdUser(42L);
        bodyObj.setUser(bodyUser);
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(bodyObj);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/orders", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes());
        assertTrue(msg.contains("Order status is required"));
    }

    @Test
    public void testGetAllReturnsList() throws Exception {
        List<Orders> data = new ArrayList<>();
        Orders a = new Orders("PENDING"); a.setIdOrder(1L); a.setUser(new User());
        Orders b = new Orders("COMPLETED"); b.setIdOrder(2L); b.setUser(new User());
        data.add(a); data.add(b);
        MockOrdersDAO dao = new MockOrdersDAO();
        dao.all = data;
        OrdersHandler handler = new OrdersHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/orders");
        handler.handle(ex);

        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        List<?> resp = mapper.readValue(ex.getResponseBytes(), new TypeReference<List<Object>>(){});
        assertEquals(2, resp.size());
    }

    @Test
    public void testGetByIdFound() throws Exception {
        MockOrdersDAO dao = new MockOrdersDAO();
        Orders o = new Orders("PENDING");
        o.setIdOrder(5L);
        User u = new User(); u.setIdUser(50L); o.setUser(u);
        dao.byId = o;
        OrdersHandler handler = new OrdersHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/orders?id=5");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        Orders resp = mapper.readValue(ex.getResponseBytes(), Orders.class);
        assertEquals(5L, resp.getIdOrder());
    }

    @Test
    public void testGetByIdNotFound() throws Exception {
        MockOrdersDAO dao = new MockOrdersDAO();
        dao.byId = null;
        OrdersHandler handler = new OrdersHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/orders?id=999");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testGetByIdInvalidFormat() throws Exception {
        OrdersHandler handler = new OrdersHandler(new MockOrdersDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/orders?id=abc");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String body = new String(ex.getResponseBytes());
        assertTrue(body.contains("Invalid ID format"));
    }

    @Test
    public void testPutUpdateSuccess() throws Exception {
        MockOrdersDAO dao = new MockOrdersDAO();
        Orders updated = new Orders("PROCESSING");
        updated.setIdOrder(7L);
        updated.setUser(new User());
        dao.updateReturn = updated;
        OrdersHandler handler = new OrdersHandler(dao);

        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(updated);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/orders", body);
        handler.handle(ex);

        assertEquals(200, ex.getResponseCode());
        Orders resp = mapper.readValue(ex.getResponseBytes(), Orders.class);
        assertEquals(7L, resp.getIdOrder());
        assertEquals("PROCESSING", resp.getStatus());
    }

    @Test
    public void testPutMissingId() throws Exception {
        OrdersHandler handler = new OrdersHandler(new MockOrdersDAO());
        Orders bodyObj = new Orders("PENDING");
        bodyObj.setUser(new User());
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(bodyObj);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/orders", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes());
        assertTrue(msg.contains("Order ID is required for update"));
    }

    @Test
    public void testPutUpdateFails() throws Exception {
        MockOrdersDAO dao = new MockOrdersDAO();
        dao.updateReturn = null;
        OrdersHandler handler = new OrdersHandler(dao);
        Orders bodyObj = new Orders("PENDING");
        bodyObj.setIdOrder(100L);
        bodyObj.setUser(new User());
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(bodyObj);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/orders", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes());
        assertTrue(msg.contains("Failed to update order"));
    }

    @Test
    public void testMethodNotAllowed() throws Exception {
        OrdersHandler handler = new OrdersHandler(new MockOrdersDAO());
        MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/orders");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
    }

    @Test
    public void testPostDaoExceptionReturns500WithCors() throws Exception {
        MockOrdersDAO dao = new MockOrdersDAO();
        dao.throwOnCreate = true;
        OrdersHandler handler = new OrdersHandler(dao);

        Orders bodyObj = new Orders("PENDING");
        User user = new User(); user.setIdUser(1L);
        bodyObj.setUser(user);
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(bodyObj);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/orders", body);
        handler.handle(ex);

        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testGetAllDaoExceptionReturns500WithCors() throws Exception {
        MockOrdersDAO dao = new MockOrdersDAO();
        dao.throwOnGetAll = true;
        OrdersHandler handler = new OrdersHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/orders");
        handler.handle(ex);

        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testGetByIdDaoExceptionReturns500WithCors() throws Exception {
        MockOrdersDAO dao = new MockOrdersDAO();
        dao.throwOnGetById = true;
        OrdersHandler handler = new OrdersHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/orders?id=1");
        handler.handle(ex);

        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testPutDaoExceptionReturns500WithCors() throws Exception {
        MockOrdersDAO dao = new MockOrdersDAO();
        dao.throwOnUpdate = true;
        OrdersHandler handler = new OrdersHandler(dao);

        Orders bodyObj = new Orders("PENDING");
        bodyObj.setIdOrder(10L);
        bodyObj.setUser(new User());
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(bodyObj);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/orders", body);
        handler.handle(ex);

        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }
}
