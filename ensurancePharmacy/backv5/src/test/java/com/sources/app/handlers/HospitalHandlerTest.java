package com.sources.app.handlers;

 import org.junit.jupiter.api.Test;
 import static org.junit.jupiter.api.Assertions.*;
 
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.sources.app.dao.HospitalDAO;
 import com.sources.app.entities.Hospital;
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
 
 public class HospitalHandlerTest {
 
     private static class MockHospitalDAO extends HospitalDAO {
         List<Hospital> all = List.of();
         Hospital byId;
         Hospital createReturn;
         Hospital updateReturn;
         Hospital lastUpdated;
 
         @Override
         public Hospital create(String name, String phone, String email, String address, Character enabled) {
             return createReturn;
         }
 
         @Override
         public List<Hospital> getAll() { return all; }
 
         @Override
         public Hospital getById(Long id) { return byId; }
 
         @Override
         public Hospital update(Hospital hospital) { lastUpdated = hospital; return updateReturn; }
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
         @Override public int getResponseCode() { return responseCode; }
         @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }
         @Override public String getProtocol() { return "HTTP/1.1"; }
         @Override public Object getAttribute(String name) { return null; }
         @Override public void setAttribute(String name, Object value) {}
         @Override public void setStreams(InputStream i, OutputStream o) {}
         @Override public HttpPrincipal getPrincipal() { return null; }
         byte[] getResponseBytes() { return responseBody.toByteArray(); }
     }
 
     @Test
     public void testInstantiation() {
         HospitalHandler handler = new HospitalHandler(new MockHospitalDAO());
         assertNotNull(handler);
     }
 
     @Test
     public void testOptionsCors() throws Exception {
         HospitalHandler handler = new HospitalHandler(new MockHospitalDAO());
         MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/hospitals");
         handler.handle(ex);
         assertEquals(204, ex.getResponseCode());
         assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
         assertEquals("GET, POST, PUT, OPTIONS", ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods"));
     }
 
     @Test
     public void testPostCreateSuccess() throws Exception {
         MockHospitalDAO dao = new MockHospitalDAO();
         Hospital created = new Hospital("General Hospital", "555-1111", "g@h.com", "Main St", '1');
         created.setIdHospital(1L);
         dao.createReturn = created;
         HospitalHandler handler = new HospitalHandler(dao);
 
         Hospital bodyObj = new Hospital("General Hospital", "555-1111", "g@h.com", "Main St", '1');
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(bodyObj);
         MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/hospitals", body);
         handler.handle(ex);
 
         assertEquals(201, ex.getResponseCode());
         Hospital resp = mapper.readValue(ex.getResponseBytes(), Hospital.class);
         assertEquals(1L, resp.getIdHospital());
         assertEquals("General Hospital", resp.getName());
     }
 
     @Test
     public void testPostMissingName() throws Exception {
         HospitalHandler handler = new HospitalHandler(new MockHospitalDAO());
         Hospital bodyObj = new Hospital();
         // name null
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(bodyObj);
         MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/hospitals", body);
         handler.handle(ex);
         assertEquals(400, ex.getResponseCode());
         String msg = new String(ex.getResponseBytes());
         assertTrue(msg.contains("Hospital name is required"));
     }
 
     @Test
     public void testPostBlankName() throws Exception {
         HospitalHandler handler = new HospitalHandler(new MockHospitalDAO());
         Hospital bodyObj = new Hospital("   ", null, null, null, '1');
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(bodyObj);
         MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/hospitals", body);
         handler.handle(ex);
         assertEquals(400, ex.getResponseCode());
         String msg = new String(ex.getResponseBytes());
         assertTrue(msg.contains("Hospital name is required"));
     }
 
     @Test
     public void testPostCreateFailure() throws Exception {
         MockHospitalDAO dao = new MockHospitalDAO();
         dao.createReturn = null;
         HospitalHandler handler = new HospitalHandler(dao);
         Hospital bodyObj = new Hospital("General Hospital", null, null, null, '1');
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(bodyObj);
         MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/hospitals", body);
         handler.handle(ex);
         assertEquals(400, ex.getResponseCode());
         String msg = new String(ex.getResponseBytes());
         assertTrue(msg.contains("Failed to create hospital"));
     }
 
     @Test
     public void testGetAllReturnsList() throws Exception {
         List<Hospital> data = new ArrayList<>();
         Hospital a = new Hospital("A", null, null, null, '1'); a.setIdHospital(1L);
         Hospital b = new Hospital("B", null, null, null, '1'); b.setIdHospital(2L);
         data.add(a); data.add(b);
         MockHospitalDAO dao = new MockHospitalDAO();
         dao.all = data;
         HospitalHandler handler = new HospitalHandler(dao);
 
         MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/hospitals");
         handler.handle(ex);
 
         assertEquals(200, ex.getResponseCode());
         ObjectMapper mapper = new ObjectMapper();
         List<?> resp = mapper.readValue(ex.getResponseBytes(), new TypeReference<List<Object>>(){ });
         assertEquals(2, resp.size());
     }
 
     @Test
     public void testGetByIdFound() throws Exception {
         MockHospitalDAO dao = new MockHospitalDAO();
         Hospital h = new Hospital("A", null, null, null, '1'); h.setIdHospital(5L);
         dao.byId = h;
         HospitalHandler handler = new HospitalHandler(dao);
 
         MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/hospitals?id=5");
         handler.handle(ex);
         assertEquals(200, ex.getResponseCode());
         ObjectMapper mapper = new ObjectMapper();
         Hospital resp = mapper.readValue(ex.getResponseBytes(), Hospital.class);
         assertEquals(5L, resp.getIdHospital());
     }
 
     @Test
     public void testGetByIdNotFound() throws Exception {
         MockHospitalDAO dao = new MockHospitalDAO();
         dao.byId = null;
         HospitalHandler handler = new HospitalHandler(dao);
 
         MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/hospitals?id=999");
         handler.handle(ex);
         assertEquals(404, ex.getResponseCode());
     }
 
     @Test
     public void testGetByIdInvalidFormat() throws Exception {
         HospitalHandler handler = new HospitalHandler(new MockHospitalDAO());
         MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/hospitals?id=abc");
         handler.handle(ex);
         assertEquals(400, ex.getResponseCode());
         String body = new String(ex.getResponseBytes());
         assertTrue(body.contains("Invalid ID format"));
     }
 
     @Test
     public void testPutUpdateSuccess() throws Exception {
         MockHospitalDAO dao = new MockHospitalDAO();
         Hospital updated = new Hospital("New Name", null, null, null, '1');
         updated.setIdHospital(7L);
         dao.updateReturn = updated;
         HospitalHandler handler = new HospitalHandler(dao);
 
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(updated);
         MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/hospitals", body);
         handler.handle(ex);
 
         assertEquals(200, ex.getResponseCode());
         Hospital resp = mapper.readValue(ex.getResponseBytes(), Hospital.class);
         assertEquals(7L, resp.getIdHospital());
         assertEquals("New Name", resp.getName());
         // verify DAO received the updated entity
         assertNotNull(dao.lastUpdated);
         assertEquals(7L, dao.lastUpdated.getIdHospital());
         assertEquals("New Name", dao.lastUpdated.getName());
     }
 
     @Test
     public void testPutMissingId() throws Exception {
         HospitalHandler handler = new HospitalHandler(new MockHospitalDAO());
         Hospital bodyObj = new Hospital("Name", null, null, null, '1');
         // id null
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(bodyObj);
         MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/hospitals", body);
         handler.handle(ex);
         assertEquals(400, ex.getResponseCode());
         String msg = new String(ex.getResponseBytes());
         assertTrue(msg.contains("Hospital ID is required for update"));
     }
 
     @Test
     public void testPutUpdateFails() throws Exception {
         MockHospitalDAO dao = new MockHospitalDAO();
         dao.updateReturn = null;
         HospitalHandler handler = new HospitalHandler(dao);
         Hospital bodyObj = new Hospital("Name", null, null, null, '1');
         bodyObj.setIdHospital(100L);
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(bodyObj);
         MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/hospitals", body);
         handler.handle(ex);
         assertEquals(400, ex.getResponseCode());
         String msg = new String(ex.getResponseBytes());
         assertTrue(msg.contains("Failed to update hospital or hospital not found"));
     }
 
     @Test
     public void testMethodNotAllowed() throws Exception {
         HospitalHandler handler = new HospitalHandler(new MockHospitalDAO());
         MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/hospitals");
         handler.handle(ex);
         assertEquals(405, ex.getResponseCode());
     }
 }
