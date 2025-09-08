package com.sources.app.handlers;

import org.junit.jupiter.api.Test;
import com.sources.app.dao.BillMedicineDAO;
import com.sources.app.entities.BillMedicine;
import com.sources.app.entities.BillMedicineId;
import com.sources.app.entities.Bill;
import com.sources.app.entities.Medicine;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;

public class BillMedicineHandlerTest {

    private static class MockBillMedicineDAO extends BillMedicineDAO {
        BillMedicine createReturn;
        List<BillMedicine> all = List.of();
        BillMedicine byId;
        BillMedicine updateReturn;

        @Override
        public BillMedicine create(Bill bill, Medicine medicine, Integer quantity, Double cost, Double copay, String total) {
            return createReturn;
        }

        @Override
        public List<BillMedicine> getAll() {
            return all;
        }

        @Override
        public BillMedicine getById(BillMedicineId id) {
            return byId;
        }

        @Override
        public BillMedicine update(BillMedicine billMedicine) {
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
    public void testBillMedicineHandlerInstantiation() {
        BillMedicineDAO mockDao = new MockBillMedicineDAO();
        BillMedicineHandler instance = new BillMedicineHandler(mockDao);
        assertNotNull(instance);
    }

    @Test
    public void testOptionsCors() throws Exception {
        BillMedicineHandler handler = new BillMedicineHandler(new MockBillMedicineDAO());
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/bill_medicines");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, OPTIONS", ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods"));
    }

    @Test
    public void testMethodNotAllowed() throws Exception {
        BillMedicineHandler handler = new BillMedicineHandler(new MockBillMedicineDAO());
        MockHttpExchange ex = new MockHttpExchange("DELETE", "http://localhost/api2/bill_medicines");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
    }

    @Test
    public void testPostCreateSuccess() throws Exception {
        MockBillMedicineDAO dao = new MockBillMedicineDAO();
        Bill bill = new Bill(); bill.setIdBill(3L);
        Medicine med = new Medicine(); med.setIdMedicine(4L);
        BillMedicine created = new BillMedicine(bill, med, 2, 5.0, 1.0, "10.0");
        dao.createReturn = created;
        BillMedicineHandler handler = new BillMedicineHandler(dao);

        BillMedicine req = new BillMedicine();
        Bill rb = new Bill(); rb.setIdBill(3L);
        Medicine rm = new Medicine(); rm.setIdMedicine(4L);
        req.setBill(rb); req.setMedicine(rm);
        req.setQuantity(2); req.setCost(5.0); req.setCopay(1.0); req.setTotal("10.0");
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/bill_medicines", body);
        handler.handle(ex);
        assertEquals(201, ex.getResponseCode());
        BillMedicine resp = mapper.readValue(ex.getResponseBytes(), BillMedicine.class);
        assertEquals(3L, resp.getId().getBillId());
        assertEquals(4L, resp.getId().getMedicineId());
    }

    @Test
    public void testPostMissingIds() throws Exception {
        BillMedicineHandler handler = new BillMedicineHandler(new MockBillMedicineDAO());
        BillMedicine req = new BillMedicine();
        req.setBill(new Bill()); // missing id
        req.setMedicine(new Medicine()); // missing id
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/bill_medicines", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Bill ID and Medicine ID are required"));
    }

    @Test
    public void testPostCreateFails() throws Exception {
        MockBillMedicineDAO dao = new MockBillMedicineDAO();
        dao.createReturn = null;
        BillMedicineHandler handler = new BillMedicineHandler(dao);

        BillMedicine req = new BillMedicine();
        Bill b = new Bill(); b.setIdBill(1L);
        Medicine m = new Medicine(); m.setIdMedicine(2L);
        req.setBill(b); req.setMedicine(m);
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/bill_medicines", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Failed to create bill-medicine association"));
    }

    @Test
    public void testPostMalformedJsonReturns500() throws Exception {
        BillMedicineHandler handler = new BillMedicineHandler(new MockBillMedicineDAO());
        byte[] body = "{not-json".getBytes(StandardCharsets.UTF_8);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/bill_medicines", body);
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
    }

    @Test
    public void testGetAllReturnsList() throws Exception {
        MockBillMedicineDAO dao = new MockBillMedicineDAO();
        Bill b1 = new Bill(); b1.setIdBill(10L);
        Medicine m1 = new Medicine(); m1.setIdMedicine(11L);
        BillMedicine bm1 = new BillMedicine(b1, m1, 1, 2.0, 0.0, "2.0");
        Bill b2 = new Bill(); b2.setIdBill(20L);
        Medicine m2 = new Medicine(); m2.setIdMedicine(21L);
        BillMedicine bm2 = new BillMedicine(b2, m2, 2, 3.0, 0.5, "6.0");
        ArrayList<BillMedicine> list = new ArrayList<>(); list.add(bm1); list.add(bm2);
        dao.all = list;
        BillMedicineHandler handler = new BillMedicineHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/bill_medicines");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        BillMedicine[] resp = mapper.readValue(ex.getResponseBytes(), BillMedicine[].class);
        assertEquals(2, resp.length);
    }

    @Test
    public void testGetByCompositeIdFound() throws Exception {
        MockBillMedicineDAO dao = new MockBillMedicineDAO();
        Bill b = new Bill(); b.setIdBill(5L);
        Medicine m = new Medicine(); m.setIdMedicine(6L);
        BillMedicine bm = new BillMedicine(b, m, 1, 1.0, 0.0, "1.0");
        dao.byId = bm;
        BillMedicineHandler handler = new BillMedicineHandler(dao);

        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/bill_medicines?id=5,6");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        ObjectMapper mapper = new ObjectMapper();
        BillMedicine resp = mapper.readValue(ex.getResponseBytes(), BillMedicine.class);
        assertEquals(5L, resp.getId().getBillId());
        assertEquals(6L, resp.getId().getMedicineId());
    }

    @Test
    public void testGetByCompositeIdNotFound() throws Exception {
        MockBillMedicineDAO dao = new MockBillMedicineDAO();
        dao.byId = null;
        BillMedicineHandler handler = new BillMedicineHandler(dao);
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/bill_medicines?id=1,2");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    @Test
    public void testGetByCompositeIdInvalidNumberFormat() throws Exception {
        BillMedicineHandler handler = new BillMedicineHandler(new MockBillMedicineDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/bill_medicines?id=1,abc");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String body = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(body.contains("Invalid composite ID format (expected: billId,medicineId)"));
    }

    @Test
    public void testGetByCompositeIdInvalidParamFormat() throws Exception {
        BillMedicineHandler handler = new BillMedicineHandler(new MockBillMedicineDAO());
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/bill_medicines?id=123");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String body = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(body.contains("Invalid ID parameter format (expected: id=billId,medicineId)"));
    }

    @Test
    public void testPutUpdateSuccess() throws Exception {
        MockBillMedicineDAO dao = new MockBillMedicineDAO();
        BillMedicine updated = new BillMedicine();
        updated.setId(new BillMedicineId(7L, 8L));
        dao.updateReturn = updated;
        BillMedicineHandler handler = new BillMedicineHandler(dao);

        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(updated);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/bill_medicines", body);
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        BillMedicine resp = mapper.readValue(ex.getResponseBytes(), BillMedicine.class);
        assertEquals(7L, resp.getId().getBillId());
        assertEquals(8L, resp.getId().getMedicineId());
    }

    @Test
    public void testPutMissingCompositeId() throws Exception {
        BillMedicineHandler handler = new BillMedicineHandler(new MockBillMedicineDAO());
        BillMedicine req = new BillMedicine();
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/bill_medicines", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Composite ID (billId, medicineId) is required for update"));
    }

    @Test
    public void testPutUpdateFails() throws Exception {
        MockBillMedicineDAO dao = new MockBillMedicineDAO();
        dao.updateReturn = null;
        BillMedicineHandler handler = new BillMedicineHandler(dao);
        BillMedicine req = new BillMedicine();
        req.setId(new BillMedicineId(9L, 10L));
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(req);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/bill_medicines", body);
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String msg = new String(ex.getResponseBytes(), StandardCharsets.UTF_8);
        assertTrue(msg.contains("Failed to update bill-medicine association or association not found"));
    }

    @Test
    public void testPutMalformedJsonReturns500() throws Exception {
        BillMedicineHandler handler = new BillMedicineHandler(new MockBillMedicineDAO());
        byte[] body = "{not-json".getBytes(StandardCharsets.UTF_8);
        MockHttpExchange ex = new MockHttpExchange("PUT", "http://localhost/api2/bill_medicines", body);
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
    }
}
