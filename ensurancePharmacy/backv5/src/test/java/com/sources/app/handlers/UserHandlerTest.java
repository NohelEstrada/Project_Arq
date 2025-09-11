package com.sources.app.handlers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.sources.app.dao.UserDAO;
import com.sources.app.entities.User;
import com.sources.app.dto.UserCreateRequest;
import java.util.Date;
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

public class UserHandlerTest {

    private static class MockUserDAO extends UserDAO {

        List<User> users = new ArrayList<>();
        boolean throwOnCreate;
        boolean throwOnGetAll;
        boolean throwOnGetById;
        boolean throwOnUpdate;
        boolean createReturnNull;

        @Override
        public User create(UserCreateRequest request) {
            if (throwOnCreate) throw new RuntimeException("boom");
            if (createReturnNull) return null;
            User u = new User();
            u.setIdUser((long) (users.size() + 1));
            u.setName(request.getName());
            u.setCui(request.getCui());
            u.setPhone(request.getPhone());
            u.setEmail(request.getEmail());
            u.setBirthDate(request.getBirthDate());
            u.setAddress(request.getAddress());
            u.setPassword(request.getPassword());
            users.add(u);
            return u;
        }

        // Método de conveniencia para los tests que usan parámetros individuales
        public User create(String name, String cui, String phone, String email, Date birthDate, String address, String password) {
            UserCreateRequest request = new UserCreateRequest(name, cui, phone, email, birthDate, address, password);
            return create(request);
        }

        @Override
        public List<User> getAll() {
            if (throwOnGetAll) throw new RuntimeException("boom");
            return users;
        }

        @Override
        public User getById(Long id) {
            if (throwOnGetById) throw new RuntimeException("boom");
            return users.stream().filter(u -> id.equals(u.getIdUser())).findFirst().orElse(null);
        }

        @Override
        public User update(User user) {
            if (throwOnUpdate) throw new RuntimeException("boom");
            User existing = getById(user.getIdUser());
            if (existing == null) {
                return null;
            }
            existing.setName(user.getName());
            existing.setAddress(user.getAddress());
            existing.setPhone(user.getPhone());
            return existing;
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
        UserHandler handler = new UserHandler(new MockUserDAO());
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/users");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testPostMissingEmailPassword() throws Exception {
        UserHandler handler = new UserHandler(new MockUserDAO());
        String body = "{\"name\":\"A\"}";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/users", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertTrue(new String(ex.getResponseBytes()).contains("Email and password are required"));
    }

    @Test
    public void testGetByIdNotFound() throws Exception {
        UserHandler handler = new UserHandler(new MockUserDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/users?id=99");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testGetByIdInvalid() throws Exception {
        UserHandler handler = new UserHandler(new MockUserDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/users?id=abc");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertTrue(new String(ex.getResponseBytes()).contains("Invalid ID format"));
    }

    @Test
    public void testGetAllOmitsPassword() throws Exception {
        MockUserDAO dao = new MockUserDAO();
        User u = dao.create("N", "1", "p", "e@e.com", new java.util.Date(), "addr", "secret");
        assertNotNull(u);
        UserHandler handler = new UserHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/users");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        String json = new String(ex.getResponseBytes());
        assertFalse(json.contains("secret"));
    }

    @Test
    public void testPutMissingId() throws Exception {
        UserHandler handler = new UserHandler(new MockUserDAO());
        String body = "{\"name\":\"B\"}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/users", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertTrue(new String(ex.getResponseBytes()).contains("User ID is required for update"));
    }

    @Test
    public void testMethodNotAllowed() throws Exception {
        UserHandler handler = new UserHandler(new MockUserDAO());
        MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/users");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
    }

    @Test
    public void testPostCreateSuccess201OmitPassword() throws Exception {
        MockUserDAO dao = new MockUserDAO();
        UserHandler handler = new UserHandler(dao);
        String body = "{\"name\":\"John\",\"cui\":\"123\",\"phone\":\"555\",\"email\":\"john@x.com\",\"birthDate\":null,\"address\":\"addr\",\"password\":\"pw\"}";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/users", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(201, ex.getResponseCode());
        String json = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertFalse(json.contains("pw"));
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testPostCreateReturnsNullYields400() throws Exception {
        MockUserDAO dao = new MockUserDAO();
        dao.createReturnNull = true;
        UserHandler handler = new UserHandler(dao);
        String body = "{\"name\":\"A\",\"email\":\"a@a.com\",\"password\":\"pw\"}";
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/users", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
    }

    @Test
    public void testPostMalformedJsonYields500WithCors() throws Exception {
        UserHandler handler = new UserHandler(new MockUserDAO());
        String bad = "{\"name\":\"broken\""; // missing closing brace
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/users", bad.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testGetByIdSuccess200OmitPassword() throws Exception {
        MockUserDAO dao = new MockUserDAO();
        User u = dao.create("Jane", "2", "777", "j@x.com", new java.util.Date(), "addr", "secret");
        assertNotNull(u);
        UserHandler handler = new UserHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/users?id=" + u.getIdUser());
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        String json = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertFalse(json.contains("secret"));
    }

    @Test
    public void testGetAllDaoExceptionYields500WithCors() throws Exception {
        MockUserDAO dao = new MockUserDAO();
        dao.throwOnGetAll = true;
        UserHandler handler = new UserHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/users");
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testPutSuccess200OmitPassword() throws Exception {
        MockUserDAO dao = new MockUserDAO();
        User existing = dao.create("N", "1", "p", "e@e.com", new java.util.Date(), "addr", "secret");
        UserHandler handler = new UserHandler(dao);
        String body = "{\"idUser\":" + existing.getIdUser() + ",\"name\":\"New\",\"address\":\"A\",\"phone\":\"123\"}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/users", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        String json = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertFalse(json.contains("secret"));
    }

    @Test
    public void testPutUpdateNotFoundYields400() throws Exception {
        MockUserDAO dao = new MockUserDAO();
        UserHandler handler = new UserHandler(dao);
        String body = "{\"idUser\":999,\"name\":\"X\"}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/users", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
    }

    @Test
    public void testDaoExceptionOnPutYields500WithCors() throws Exception {
        MockUserDAO dao = new MockUserDAO();
        User existing = dao.create("N", "1", "p", "e@e.com", new java.util.Date(), "addr", "secret");
        dao.throwOnUpdate = true;
        UserHandler handler = new UserHandler(dao);
        String body = "{\"idUser\":" + existing.getIdUser() + ",\"name\":\"New\"}";
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/users", body.getBytes(StandardCharsets.UTF_8));
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }
}
