package com.sources.app.handlers;

 import org.junit.jupiter.api.Test;
 import static org.junit.jupiter.api.Assertions.*;
 
 import com.sources.app.dao.PrescriptionDAO;
 import com.sources.app.entities.Prescription;
 import com.sources.app.entities.User;
 import com.sources.app.entities.Hospital;
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
 
 public class PrescriptionHandlerTest {
 
     private static class MockPrescriptionDAO extends PrescriptionDAO {
         List<Prescription> all = List.of();
         Prescription byId;
         Prescription createReturn;
         Prescription updateReturn;
         Prescription lastUpdated;
 
         @Override
         public Prescription create(Hospital hospital, User user, Character approved) {
             return createReturn;
         }
 
         @Override
         public List<Prescription> getAll() {
             return all;
         }
 
         @Override
         public Prescription getById(Long id) {
             return byId;
         }
 
         @Override
         public Prescription update(Prescription prescription) {
             lastUpdated = prescription;
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
         PrescriptionHandler handler = new PrescriptionHandler(new MockPrescriptionDAO());
         assertNotNull(handler);
     }
 
     @Test
     public void testOptionsCors() throws Exception {
         PrescriptionHandler handler = new PrescriptionHandler(new MockPrescriptionDAO());
         MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/prescriptions");
         handler.handle(ex);
         assertEquals(204, ex.getResponseCode());
         assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
         assertEquals("GET, POST, PUT, OPTIONS", ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods"));
     }
 
     @Test
     public void testPostCreateSuccess() throws Exception {
         MockPrescriptionDAO dao = new MockPrescriptionDAO();
         Hospital h = new Hospital(); h.setIdHospital(10L);
         User u = new User(); u.setIdUser(20L);
         Prescription created = new Prescription(h, u, '1');
         created.setIdPrescription(1L);
         dao.createReturn = created;
         PrescriptionHandler handler = new PrescriptionHandler(dao);
 
         Prescription bodyObj = new Prescription(h, u, '1');
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(bodyObj);
         MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/prescriptions", body);
         handler.handle(ex);
 
         assertEquals(201, ex.getResponseCode());
         Prescription resp = mapper.readValue(ex.getResponseBytes(), Prescription.class);
         assertEquals(1L, resp.getIdPrescription());
         assertEquals('1', resp.getApproved());
     }
 
     @Test
     public void testPostMissingHospitalOrUser() throws Exception {
         PrescriptionHandler handler = new PrescriptionHandler(new MockPrescriptionDAO());
         // Missing hospital id
         Hospital h = new Hospital(); // id null
         User u = new User(); u.setIdUser(20L);
         Prescription bodyObj = new Prescription(h, u, '1');
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(bodyObj);
         MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/prescriptions", body);
         handler.handle(ex);
         assertEquals(400, ex.getResponseCode());
         String msg = new String(ex.getResponseBytes());
         assertTrue(msg.contains("Hospital ID and User ID are required"));
     }
 
     @Test
     public void testPostMissingApproved() throws Exception {
         PrescriptionHandler handler = new PrescriptionHandler(new MockPrescriptionDAO());
         Hospital h = new Hospital(); h.setIdHospital(10L);
         User u = new User(); u.setIdUser(20L);
         Prescription bodyObj = new Prescription(h, u, null);
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(bodyObj);
         MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/prescriptions", body);
         handler.handle(ex);
         assertEquals(400, ex.getResponseCode());
         String msg = new String(ex.getResponseBytes());
         assertTrue(msg.contains("Approved status is required"));
     }
 
     @Test
     public void testGetAllReturnsList() throws Exception {
         List<Prescription> data = new ArrayList<>();
         Hospital h = new Hospital(); h.setIdHospital(1L);
         User u = new User(); u.setIdUser(2L);
         data.add(new Prescription(h, u, '0'));
         data.add(new Prescription(h, u, '1'));
         MockPrescriptionDAO dao = new MockPrescriptionDAO();
         dao.all = data;
         PrescriptionHandler handler = new PrescriptionHandler(dao);
 
         MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/prescriptions");
         handler.handle(ex);
 
         assertEquals(200, ex.getResponseCode());
         ObjectMapper mapper = new ObjectMapper();
         List<?> resp = mapper.readValue(ex.getResponseBytes(), new TypeReference<List<Object>>(){});
         assertEquals(2, resp.size());
     }
 
     @Test
     public void testGetByIdFound() throws Exception {
         MockPrescriptionDAO dao = new MockPrescriptionDAO();
         Hospital h = new Hospital(); h.setIdHospital(1L);
         User u = new User(); u.setIdUser(2L);
         Prescription p = new Prescription(h, u, '1');
         p.setIdPrescription(5L);
         dao.byId = p;
         PrescriptionHandler handler = new PrescriptionHandler(dao);
 
         MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/prescriptions?id=5");
         handler.handle(ex);
         assertEquals(200, ex.getResponseCode());
         ObjectMapper mapper = new ObjectMapper();
         Prescription resp = mapper.readValue(ex.getResponseBytes(), Prescription.class);
         assertEquals(5L, resp.getIdPrescription());
     }
 
     @Test
     public void testGetByIdNotFound() throws Exception {
         MockPrescriptionDAO dao = new MockPrescriptionDAO();
         dao.byId = null;
         PrescriptionHandler handler = new PrescriptionHandler(dao);
 
         MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/prescriptions?id=999");
         handler.handle(ex);
         assertEquals(404, ex.getResponseCode());
     }
 
     @Test
     public void testGetByIdInvalidFormat() throws Exception {
         PrescriptionHandler handler = new PrescriptionHandler(new MockPrescriptionDAO());
         MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/prescriptions?id=abc");
         handler.handle(ex);
         assertEquals(400, ex.getResponseCode());
         String body = new String(ex.getResponseBytes());
         assertTrue(body.contains("Invalid ID format"));
     }
 
     @Test
     public void testPutUpdateSuccess() throws Exception {
         MockPrescriptionDAO dao = new MockPrescriptionDAO();
         Hospital h = new Hospital(); h.setIdHospital(1L);
         User u = new User(); u.setIdUser(2L);
         Prescription updated = new Prescription(h, u, '0');
         updated.setIdPrescription(7L);
         dao.updateReturn = updated;
         PrescriptionHandler handler = new PrescriptionHandler(dao);
 
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(updated);
         MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/prescriptions", body);
         handler.handle(ex);
 
         assertEquals(200, ex.getResponseCode());
         Prescription resp = mapper.readValue(ex.getResponseBytes(), Prescription.class);
         assertEquals(7L, resp.getIdPrescription());
     }
 
     @Test
     public void testPutMissingId() throws Exception {
         PrescriptionHandler handler = new PrescriptionHandler(new MockPrescriptionDAO());
         Hospital h = new Hospital(); h.setIdHospital(1L);
         User u = new User(); u.setIdUser(2L);
         Prescription bodyObj = new Prescription(h, u, '1'); // id null
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(bodyObj);
         MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/prescriptions", body);
         handler.handle(ex);
         assertEquals(400, ex.getResponseCode());
         String msg = new String(ex.getResponseBytes());
         assertTrue(msg.contains("Prescription ID is required for update"));
     }
 
     @Test
     public void testPutUpdateFails() throws Exception {
         MockPrescriptionDAO dao = new MockPrescriptionDAO();
         dao.updateReturn = null;
         PrescriptionHandler handler = new PrescriptionHandler(dao);
         Hospital h = new Hospital(); h.setIdHospital(1L);
         User u = new User(); u.setIdUser(2L);
         Prescription bodyObj = new Prescription(h, u, '1');
         bodyObj.setIdPrescription(100L);
         ObjectMapper mapper = new ObjectMapper();
         byte[] body = mapper.writeValueAsBytes(bodyObj);
         MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/prescriptions", body);
         handler.handle(ex);
         assertEquals(400, ex.getResponseCode());
         String msg = new String(ex.getResponseBytes());
         assertTrue(msg.contains("Failed to update prescription"));
     }
 
     @Test
     public void testMethodNotAllowed() throws Exception {
         PrescriptionHandler handler = new PrescriptionHandler(new MockPrescriptionDAO());
         MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/prescriptions");
         handler.handle(ex);
         assertEquals(405, ex.getResponseCode());
     }
 }
