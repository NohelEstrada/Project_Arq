package com.sources.app.handlers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.CommentsDAO;
import com.sources.app.entities.Comments;
import com.sources.app.entities.Medicine;
import com.sources.app.entities.User;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CommentsHandlerTest {

    // Minimal HttpExchange double reused across handlers
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

    // Inline configurable DAO double
    private static class MockCommentsDAO extends CommentsDAO {
        List<Comments> all = new ArrayList<>();
        Comments byId;
        Comments createReturn;
        Comments updateReturn;
        boolean throwOnCreate;
        boolean throwOnGetAll;
        boolean throwOnGetById;
        boolean throwOnUpdate;

        @Override
        public Comments create(User user, Comments prevComment, String commentText, Medicine medicine) {
            if (throwOnCreate) throw new RuntimeException("boom");
            return createReturn;
        }

        @Override
        public List<Comments> getAll() {
            if (throwOnGetAll) throw new RuntimeException("boom");
            return all;
        }

        @Override
        public Comments getById(Long id) {
            if (throwOnGetById) throw new RuntimeException("boom");
            return byId;
        }

        @Override
        public Comments update(Comments comments) {
            if (throwOnUpdate) throw new RuntimeException("boom");
            return updateReturn;
        }
    }

    @Test
    public void testOptionsCors() throws Exception {
        CommentsHandler handler = new CommentsHandler(new MockCommentsDAO());
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/comments");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        assertTrue(ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods").contains("GET"));
        assertTrue(ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods").contains("POST"));
        assertTrue(ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods").contains("PUT"));
        assertTrue(ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods").contains("OPTIONS"));
    }

    @Test
    public void testPostMalformedJsonReturns500() throws Exception {
        CommentsHandler handler = new CommentsHandler(new MockCommentsDAO());
        byte[] body = "{not-json".getBytes(StandardCharsets.UTF_8);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/comments", body);
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
    }

    @Test
    public void testPutMalformedJsonReturns500() throws Exception {
        CommentsHandler handler = new CommentsHandler(new MockCommentsDAO());
        byte[] body = "{not-json".getBytes(StandardCharsets.UTF_8);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/comments", body);
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
    }

    @Test
    public void testPostCreateFailsReturns400() throws Exception {
        MockCommentsDAO dao = new MockCommentsDAO();
        dao.createReturn = null; // force DAO failure path
        CommentsHandler handler = new CommentsHandler(dao);

        Comments req = new Comments();
        User u = new User(); u.setIdUser(10L); req.setUser(u);
        Medicine m = new Medicine(); m.setIdMedicine(20L); req.setMedicine(m);
        req.setCommentText("Test comment");
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/comments", body);
        handler.handle(ex);

        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Failed to create comment"));
    }

    @Test
    public void testPostValidations() throws Exception {
        CommentsHandler handler = new CommentsHandler(new MockCommentsDAO());
        ObjectMapper mapper = new ObjectMapper();

        // missing user
        Comments c1 = new Comments();
        c1.setMedicine(new Medicine());
        c1.getMedicine().setIdMedicine(1L);
        c1.setCommentText("hi");
        MockHttpExchange ex1 = new MockHttpExchange("POST", "http://localhost/api2/comments", mapper.writeValueAsBytes(c1));
        handler.handle(ex1);
        assertEquals(400, ex1.getResponseCode());
        assertTrue(new String(ex1.getResponseBytes(), StandardCharsets.UTF_8).contains("User ID is required"));

        // missing medicine
        Comments c2 = new Comments();
        User u = new User(); u.setIdUser(2L); c2.setUser(u);
        c2.setCommentText("hi");
        MockHttpExchange ex2 = new MockHttpExchange("POST", "http://localhost/api2/comments", mapper.writeValueAsBytes(c2));
        handler.handle(ex2);
        assertEquals(400, ex2.getResponseCode());
        assertTrue(new String(ex2.getResponseBytes(), StandardCharsets.UTF_8).contains("Medicine ID is required"));

        // missing text
        Comments c3 = new Comments();
        c3.setUser(u);
        Medicine m = new Medicine(); m.setIdMedicine(3L); c3.setMedicine(m);
        MockHttpExchange ex3 = new MockHttpExchange("POST", "http://localhost/api2/comments", mapper.writeValueAsBytes(c3));
        handler.handle(ex3);
        assertEquals(400, ex3.getResponseCode());
        assertTrue(new String(ex3.getResponseBytes(), StandardCharsets.UTF_8).contains("Comment text is required"));
    }

    @Test
    public void testPostCreateSuccessReturns201() throws Exception {
        MockCommentsDAO dao = new MockCommentsDAO();
        Comments created = new Comments();
        created.setIdComments(99L);
        dao.createReturn = created;
        CommentsHandler handler = new CommentsHandler(dao);

        Comments req = new Comments();
        User u = new User(); u.setIdUser(10L); req.setUser(u);
        Medicine m = new Medicine(); m.setIdMedicine(20L); req.setMedicine(m);
        req.setCommentText("ok");

        ObjectMapper mapper = new ObjectMapper();
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/comments", mapper.writeValueAsBytes(req));
        handler.handle(ex);
        assertEquals(201, ex.getResponseCode());
        Comments resp = mapper.readValue(ex.getResponseBytes(), Comments.class);
        assertEquals(99L, resp.getIdComments());
    }

    @Test
    public void testGetAllSuccess() throws Exception {
        MockCommentsDAO dao = new MockCommentsDAO();
        Comments c = new Comments(); c.setIdComments(1L);
        dao.all.add(c);
        CommentsHandler handler = new CommentsHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/comments");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        List<Comments> list = mapper.readValue(ex.getResponseBytes(), new TypeReference<List<Comments>>(){});
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getIdComments());
    }

    @Test
    public void testGetByIdSuccess() throws Exception {
        MockCommentsDAO dao = new MockCommentsDAO();
        Comments c = new Comments(); c.setIdComments(5L); dao.byId = c;
        CommentsHandler handler = new CommentsHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/comments?id=5");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        Comments got = mapper.readValue(ex.getResponseBytes(), Comments.class);
        assertEquals(5L, got.getIdComments());
    }

    @Test
    public void testGetByIdNotFound() throws Exception {
        MockCommentsDAO dao = new MockCommentsDAO();
        dao.byId = null;
        CommentsHandler handler = new CommentsHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/comments?id=123");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testGetByIdInvalidFormat() throws Exception {
        CommentsHandler handler = new CommentsHandler(new MockCommentsDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/comments?id=abc");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        assertTrue(new String(ex.getResponseBytes(), StandardCharsets.UTF_8).contains("Invalid ID format"));
    }

    @Test
    public void testPutValidations() throws Exception {
        CommentsHandler handler = new CommentsHandler(new MockCommentsDAO());
        ObjectMapper mapper = new ObjectMapper();

        // missing id
        Comments c1 = new Comments();
        c1.setUser(new User());
        c1.setMedicine(new Medicine());
        c1.setCommentText("x");
        MockHttpExchange ex1 = new MockHttpExchange("PUT", "http://localhost/api2/comments", mapper.writeValueAsBytes(c1));
        handler.handle(ex1);
        assertEquals(400, ex1.getResponseCode());
        assertTrue(new String(ex1.getResponseBytes(), StandardCharsets.UTF_8).contains("Comment ID is required for update"));

        // missing text
        Comments c2 = new Comments();
        c2.setIdComments(10L);
        c2.setUser(new User());
        c2.setMedicine(new Medicine());
        MockHttpExchange ex2 = new MockHttpExchange("PUT", "http://localhost/api2/comments", mapper.writeValueAsBytes(c2));
        handler.handle(ex2);
        assertEquals(400, ex2.getResponseCode());
        assertTrue(new String(ex2.getResponseBytes(), StandardCharsets.UTF_8).contains("Comment text is required for update"));
    }

    @Test
    public void testPutUpdateSuccess() throws Exception {
        MockCommentsDAO dao = new MockCommentsDAO();
        Comments updated = new Comments(); updated.setIdComments(100L);
        dao.updateReturn = updated;
        CommentsHandler handler = new CommentsHandler(dao);
        ObjectMapper mapper = new ObjectMapper();
        Comments bodyObj = new Comments();
        bodyObj.setIdComments(100L);
        bodyObj.setUser(new User());
        bodyObj.setMedicine(new Medicine());
        bodyObj.setCommentText("x");
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/comments", mapper.writeValueAsBytes(bodyObj));
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        Comments resp = mapper.readValue(ex.getResponseBytes(), Comments.class);
        assertEquals(100L, resp.getIdComments());
    }

    @Test
    public void testPutUpdateFails() throws Exception {
        MockCommentsDAO dao = new MockCommentsDAO();
        dao.updateReturn = null;
        CommentsHandler handler = new CommentsHandler(dao);
        Comments bodyObj = new Comments();
        bodyObj.setIdComments(100L);
        bodyObj.setUser(new User());
        bodyObj.setMedicine(new Medicine());
        bodyObj.setCommentText("x");
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(bodyObj);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/comments", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Failed to update comment or comment not found"));
    }

    @Test
    public void testDaoExceptionOnGetAllYields500WithCors() throws Exception {
        MockCommentsDAO dao = new MockCommentsDAO();
        dao.throwOnGetAll = true;
        CommentsHandler handler = new CommentsHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/comments");
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testDaoExceptionOnGetByIdYields500WithCors() throws Exception {
        MockCommentsDAO dao = new MockCommentsDAO();
        dao.throwOnGetById = true;
        CommentsHandler handler = new CommentsHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/comments?id=1");
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testDaoExceptionOnPostYields500WithCors() throws Exception {
        MockCommentsDAO dao = new MockCommentsDAO();
        dao.throwOnCreate = true;
        CommentsHandler handler = new CommentsHandler(dao);
        Comments req = new Comments();
        User u = new User(); u.setIdUser(1L); req.setUser(u);
        Medicine m = new Medicine(); m.setIdMedicine(2L); req.setMedicine(m);
        req.setCommentText("x");
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/comments", body);
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testDaoExceptionOnPutYields500WithCors() throws Exception {
        MockCommentsDAO dao = new MockCommentsDAO();
        dao.throwOnUpdate = true;
        CommentsHandler handler = new CommentsHandler(dao);
        Comments req = new Comments();
        req.setIdComments(5L);
        req.setUser(new User());
        req.setMedicine(new Medicine());
        req.setCommentText("x");
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/comments", body);
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    public void testMethodNotAllowed() throws Exception {
        CommentsHandler handler = new CommentsHandler(new MockCommentsDAO());
        MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/comments");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
    }
}
