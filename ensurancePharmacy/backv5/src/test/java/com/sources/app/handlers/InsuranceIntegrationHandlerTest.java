package com.sources.app.handlers;

import org.junit.jupiter.api.Test;
import com.sources.app.dao.PrescriptionDAO;
import com.sources.app.dao.BillDAO;
import com.sources.app.entities.Prescription;
import com.sources.app.entities.Bill;
import com.sources.app.util.ExternalServiceClient;
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

import static org.junit.jupiter.api.Assertions.*;

public class InsuranceIntegrationHandlerTest {

    private static class MockHttpExchange extends HttpExchange {
        private final String method;
        private final URI uri;
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private ByteArrayInputStream requestBody = new ByteArrayInputStream(new byte[0]);
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private int responseCode = -1;

        MockHttpExchange(String method, String url) {
            this.method = method;
            this.uri = URI.create(url);
        }

        void setRequestBody(String body) {
            this.requestBody = new ByteArrayInputStream(body.getBytes());
        }

        @Override public Headers getRequestHeaders() { return requestHeaders; }
        @Override public Headers getResponseHeaders() { return responseHeaders; }
        @Override public URI getRequestURI() { return uri; }
        @Override public String getRequestMethod() { return method; }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public void close() {}
        @Override public InputStream getRequestBody() { return requestBody; }
        @Override public OutputStream getResponseBody() { return responseBody; }
        @Override
        public void sendResponseHeaders(int responseCode, long responseLength) throws IOException {
            this.responseCode = responseCode;
        }
        @Override public InetSocketAddress getRemoteAddress() { return new InetSocketAddress(0); }
        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public void setStreams(InputStream i, OutputStream o) {}
        @Override public com.sun.net.httpserver.HttpPrincipal getPrincipal() { return null; }
        public int getResponseCode() {
            return responseCode;
        }
        byte[] getResponseBytes() { return responseBody.toByteArray(); }
    }

    private static class MockPrescriptionDAO extends PrescriptionDAO {
        private final Prescription toReturn;
        MockPrescriptionDAO(Prescription toReturn) { this.toReturn = toReturn; }
        @Override public Prescription getById(Long id) { return toReturn; }
    }

    private static class MockBillDAO extends BillDAO {
        private final Bill existing;
        private final Bill toCreate;
        MockBillDAO(Bill existing, Bill toCreate) { this.existing = existing; this.toCreate = toCreate; }
        @Override public Bill getByPrescriptionId(Long prescriptionId) { return existing; }
        @Override public Bill create(Prescription prescription, Double taxes, Double subtotal, Double copay, String total) {
            return toCreate;
        }
    }

    private static class MockExternalServiceClient extends ExternalServiceClient {
        enum Mode { RETURN_SUCCESS, THROW_ERROR }
        private Mode postMode = Mode.RETURN_SUCCESS;
        private Mode getMode = Mode.RETURN_SUCCESS;
        private String postResponse = "{\"success\":true,\"coveredAmount\":700.0,\"patientAmount\":300.0,\"approvalCode\":\"OK\"}";
        private String getResponse = "{\"success\":true,\"coverage\":\"FULL\"}";
        void setPostMode(Mode m) { this.postMode = m; }
        void setGetMode(Mode m) { this.getMode = m; }
        void setPostResponse(String r) { this.postResponse = r; }
        void setGetResponse(String r) { this.getResponse = r; }
        @Override public String post(String serviceType, String endpoint, Object data) throws IOException {
            if (postMode == Mode.THROW_ERROR) throw new IOException("boom");
            return postResponse;
        }
        @Override public String get(String serviceType, String endpoint) throws IOException {
            if (getMode == Mode.THROW_ERROR) throw new IOException("boom");
            return getResponse;
        }
    }

    // 405 on invalid methods
    @Test
    void testValidatePrescriptionGetMethodNotAllowed() throws Exception {
        InsuranceIntegrationHandler handler = new InsuranceIntegrationHandler(new MockPrescriptionDAO(null), new MockBillDAO(null, null));
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/insurance/validate-prescription");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
    }

    @Test
    void testCheckCoverageGetMethodNotAllowed() throws Exception {
        InsuranceIntegrationHandler handler = new InsuranceIntegrationHandler(new MockPrescriptionDAO(null), new MockBillDAO(null, null));
        MockHttpExchange ex = new MockHttpExchange("GET", "http://localhost/api2/insurance/check-coverage");
        handler.handle(ex);
        assertEquals(405, ex.getResponseCode());
    }

    // 404 unknown route
    @Test
    void testUnknownRouteReturns404() throws Exception {
        InsuranceIntegrationHandler handler = new InsuranceIntegrationHandler(new MockPrescriptionDAO(null), new MockBillDAO(null, null));
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/insurance/unknown");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
    }

    // 400 missing fields validate-prescription
    @Test
    void testValidatePrescriptionMissingFieldsReturns400() throws Exception {
        InsuranceIntegrationHandler handler = new InsuranceIntegrationHandler(new MockPrescriptionDAO(null), new MockBillDAO(null, null));
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/insurance/validate-prescription");
        ex.setRequestBody("{\"prescriptionId\": 5}"); // missing approvalCode
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String body = new String(ex.getResponseBytes());
        assertTrue(body.contains("Campos requeridos"));
    }

    // 404 when prescription not found
    @Test
    void testValidatePrescriptionPrescriptionNotFoundReturns404() throws Exception {
        InsuranceIntegrationHandler handler = new InsuranceIntegrationHandler(new MockPrescriptionDAO(null), new MockBillDAO(null, null));
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/insurance/validate-prescription");
        ex.setRequestBody("{\"prescriptionId\": 5, \"approvalCode\": \"ABC\"}");
        handler.handle(ex);
        assertEquals(404, ex.getResponseCode());
        String body = new String(ex.getResponseBytes());
        assertTrue(body.contains("Receta no encontrada"));
    }

    // 400 when already billed
    @Test
    void testValidatePrescriptionAlreadyBilledReturns400() throws Exception {
        Prescription p = new Prescription();
        Bill existing = new Bill();
        InsuranceIntegrationHandler handler = new InsuranceIntegrationHandler(new MockPrescriptionDAO(p), new MockBillDAO(existing, null));
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/insurance/validate-prescription");
        ex.setRequestBody("{\"prescriptionId\": 5, \"approvalCode\": \"ABC\"}");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String body = new String(ex.getResponseBytes());
        assertTrue(body.contains("ya fue procesada"));
    }

    // 500 when external client throws (validate-prescription)
    @Test
    void testValidatePrescriptionExternalErrorReturns500() throws Exception {
        Prescription p = new Prescription();
        MockExternalServiceClient client = new MockExternalServiceClient();
        client.setPostMode(MockExternalServiceClient.Mode.THROW_ERROR);
        InsuranceIntegrationHandler handler = new InsuranceIntegrationHandler(new MockPrescriptionDAO(p), new MockBillDAO(null, null), client);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/insurance/validate-prescription");
        ex.setRequestBody("{\"prescriptionId\": 5, \"approvalCode\": \"ABC\"}");
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        String body = new String(ex.getResponseBytes());
        assertTrue(body.contains("Error al comunicarse con el seguro"));
    }

    // 400 missing field for check-coverage
    @Test
    void testCheckCoverageMissingPrescriptionIdReturns400() throws Exception {
        InsuranceIntegrationHandler handler = new InsuranceIntegrationHandler(new MockPrescriptionDAO(null), new MockBillDAO(null, null));
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/insurance/check-coverage");
        ex.setRequestBody("{\"foo\": 1}");
        handler.handle(ex);
        assertEquals(400, ex.getResponseCode());
        String body = new String(ex.getResponseBytes());
        assertTrue(body.contains("Campo requerido"));
    }

    // 500 when external client throws (check-coverage)
    @Test
    void testCheckCoverageExternalErrorReturns500() throws Exception {
        MockExternalServiceClient client = new MockExternalServiceClient();
        client.setGetMode(MockExternalServiceClient.Mode.THROW_ERROR);
        InsuranceIntegrationHandler handler = new InsuranceIntegrationHandler(new MockPrescriptionDAO(null), new MockBillDAO(null, null), client);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/insurance/check-coverage");
        ex.setRequestBody("{\"prescriptionId\": 5}");
        handler.handle(ex);
        assertEquals(500, ex.getResponseCode());
        String body = new String(ex.getResponseBytes());
        assertTrue(body.contains("Error al comunicarse con el seguro"));
    }

    // 200 success path for validate-prescription creates bill and returns billId
    @Test
    void testValidatePrescriptionSuccessReturns200WithBillId() throws Exception {
        Prescription p = new Prescription();
        Bill created = new Bill();
        created.setIdBill(10L);
        MockExternalServiceClient client = new MockExternalServiceClient();
        client.setPostMode(MockExternalServiceClient.Mode.RETURN_SUCCESS);
        InsuranceIntegrationHandler handler = new InsuranceIntegrationHandler(new MockPrescriptionDAO(p), new MockBillDAO(null, created), client);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/insurance/validate-prescription");
        ex.setRequestBody("{\"prescriptionId\": 5, \"approvalCode\": \"OK\"}");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        String body = new String(ex.getResponseBytes());
        assertTrue(body.contains("\"billId\":10"));
        assertTrue(body.contains("\"success\":true"));
    }

    // 200 success path for check-coverage proxies insurance response
    @Test
    void testCheckCoverageSuccessReturns200() throws Exception {
        MockExternalServiceClient client = new MockExternalServiceClient();
        client.setGetMode(MockExternalServiceClient.Mode.RETURN_SUCCESS);
        InsuranceIntegrationHandler handler = new InsuranceIntegrationHandler(new MockPrescriptionDAO(null), new MockBillDAO(null, null), client);
        MockHttpExchange ex = new MockHttpExchange("POST", "http://localhost/api2/insurance/check-coverage");
        ex.setRequestBody("{\"prescriptionId\": 5}");
        handler.handle(ex);
        assertEquals(200, ex.getResponseCode());
        String body = new String(ex.getResponseBytes());
        assertTrue(body.contains("\"success\":true"));
        assertTrue(body.contains("\"coverage\":"));
    }

    // OPTIONS CORS preflight
    @Test
    void testOptionsCorsReturns204AndHeaders() throws Exception {
        InsuranceIntegrationHandler handler = new InsuranceIntegrationHandler(new MockPrescriptionDAO(null), new MockBillDAO(null, null));
        MockHttpExchange ex = new MockHttpExchange("OPTIONS", "http://localhost/api2/insurance/validate-prescription");
        handler.handle(ex);
        assertEquals(204, ex.getResponseCode());
        assertEquals("*", ex.getResponseHeaders().getFirst("Access-Control-Allow-Origin"));
        assertEquals("POST, OPTIONS", ex.getResponseHeaders().getFirst("Access-Control-Allow-Methods"));
    }
}
