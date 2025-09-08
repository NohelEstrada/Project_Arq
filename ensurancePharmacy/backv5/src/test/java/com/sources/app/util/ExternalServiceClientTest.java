package com.sources.app.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

public class ExternalServiceClientTest {

    @Test
    void testExternalServiceClientInstantiation() {
        // TODO: implement tests for ExternalServiceClient
        ExternalServiceClient instance = new ExternalServiceClient();
        assertNotNull(instance);
    }

    @Test
    void testGetBaseUrlValidation() {
        ExternalServiceClient client = new ExternalServiceClient();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> client.get("unknown", "/ping"));
        assertTrue(ex.getMessage().toLowerCase().contains("tipo de servicio"));
    }

    @Test
    void testGetAsyncDelegatesAndReturnsValue() throws Exception {
        ExternalServiceClient client = spy(new ExternalServiceClient());
        doReturn("OK").when(client).get("HOSPITAL", "/ping");

        String result = client.getAsync("HOSPITAL", "/ping").get(2, TimeUnit.SECONDS);
        assertEquals("OK", result);
        verify(client, times(1)).get("HOSPITAL", "/ping");
    }

    @Test
    void testPostAsyncDelegatesAndReturnsValue() throws Exception {
        ExternalServiceClient client = spy(new ExternalServiceClient());
        doReturn("CREATED").when(client).post(eq("INSURANCE"), eq("/create"), any());

        String result = client.postAsync("INSURANCE", "/create", new Object()).get(2, TimeUnit.SECONDS);
        assertEquals("CREATED", result);
        verify(client, times(1)).post(eq("INSURANCE"), eq("/create"), any());
    }

    @Test
    void testPutAsyncDelegatesAndReturnsValue() throws Exception {
        ExternalServiceClient client = spy(new ExternalServiceClient());
        doReturn("UPDATED").when(client).put(eq("PHARMACY"), eq("/update/1"), any());

        String result = client.putAsync("PHARMACY", "/update/1", new Object()).get(2, TimeUnit.SECONDS);
        assertEquals("UPDATED", result);
        verify(client, times(1)).put(eq("PHARMACY"), eq("/update/1"), any());
    }

    @Test
    void testGetAsyncWrapsIOException() throws InterruptedException, TimeoutException {
        ExternalServiceClient client = spy(new ExternalServiceClient());
        try {
            doThrow(new IOException("boom")).when(client).get("HOSPITAL", "/ping");
        } catch (IOException e) {
            // this catch is never reached; doThrow config doesn't run now
        }

        ExecutionException ee = assertThrows(ExecutionException.class,
                () -> client.getAsync("HOSPITAL", "/ping").get(2, TimeUnit.SECONDS));
        assertTrue(ee.getCause() instanceof RuntimeException);
        assertTrue(ee.getCause().getCause() instanceof IOException);
    }

    @Test
    void testPostBaseUrlValidation() {
        ExternalServiceClient client = new ExternalServiceClient();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> client.post("unknown", "/create", new Object()));
        assertTrue(ex.getMessage().toLowerCase().contains("tipo de servicio"));
    }

    @Test
    void testPutBaseUrlValidation() {
        ExternalServiceClient client = new ExternalServiceClient();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> client.put("unknown", "/update/1", new Object()));
        assertTrue(ex.getMessage().toLowerCase().contains("tipo de servicio"));
    }

    @Test
    void testPostAsyncWrapsIOException() throws InterruptedException, TimeoutException {
        ExternalServiceClient client = spy(new ExternalServiceClient());
        try {
            doThrow(new IOException("boom")).when(client).post(eq("INSURANCE"), eq("/create"), any());
        } catch (IOException e) {
            // not reached now
        }

        ExecutionException ee = assertThrows(ExecutionException.class,
                () -> client.postAsync("INSURANCE", "/create", new Object()).get(2, TimeUnit.SECONDS));
        assertTrue(ee.getCause() instanceof RuntimeException);
        assertTrue(ee.getCause().getCause() instanceof IOException);
    }

    @Test
    void testPutAsyncWrapsIOException() throws InterruptedException, TimeoutException {
        ExternalServiceClient client = spy(new ExternalServiceClient());
        try {
            doThrow(new IOException("boom")).when(client).put(eq("PHARMACY"), eq("/update/1"), any());
        } catch (IOException e) {
            // not reached now
        }

        ExecutionException ee = assertThrows(ExecutionException.class,
                () -> client.putAsync("PHARMACY", "/update/1", new Object()).get(2, TimeUnit.SECONDS));
        assertTrue(ee.getCause() instanceof RuntimeException);
        assertTrue(ee.getCause().getCause() instanceof IOException);
    }

    @Test
    void testAsyncInvalidServiceTypePropagatesForGet() {
        ExternalServiceClient client = new ExternalServiceClient();
        ExecutionException ee = assertThrows(ExecutionException.class,
                () -> client.getAsync("unknown", "/ping").get(2, TimeUnit.SECONDS));
        assertTrue(ee.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void testAsyncInvalidServiceTypePropagatesForPost() {
        ExternalServiceClient client = new ExternalServiceClient();
        ExecutionException ee = assertThrows(ExecutionException.class,
                () -> client.postAsync("unknown", "/create", new Object()).get(2, TimeUnit.SECONDS));
        assertTrue(ee.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void testAsyncInvalidServiceTypePropagatesForPut() {
        ExternalServiceClient client = new ExternalServiceClient();
        ExecutionException ee = assertThrows(ExecutionException.class,
                () -> client.putAsync("unknown", "/update/1", new Object()).get(2, TimeUnit.SECONDS));
        assertTrue(ee.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void testGetSuccessHospitalWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/ping", exchange -> ok(exchange, "pong"));
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.get("HOSPITAL", "/ping");
            assertEquals("pong", resp);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testGetAsyncSuccessWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/ping", exchange -> ok(exchange, "pong"));
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.getAsync("HOSPITAL", "/ping").get(2, TimeUnit.SECONDS);
            assertEquals("pong", resp);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testPostAsyncSuccessWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/pharmacy-insurance/create", exchange -> ok(exchange, "ok"));
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.postAsync("INSURANCE", "/create", java.util.Map.of("x", 1))
                                .get(2, TimeUnit.SECONDS);
            assertEquals("ok", resp);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testPutAsyncSuccessWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/put-async", exchange -> ok(exchange, "ok"));
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.putAsync("PHARMACY", "/put-async", java.util.Map.of("y", true))
                                .get(2, TimeUnit.SECONDS);
            assertEquals("ok", resp);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testPostSuccessInsuranceWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/pharmacy-insurance/create", exchange -> ok(exchange, "ok"));
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.post("INSURANCE", "/create", java.util.Map.of("x", 1));
            assertEquals("ok", resp);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testPostSuccessPharmacyWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/echo", exchange -> ok(exchange, "ok"));
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.post("PHARMACY", "/echo", java.util.Map.of("ok", true));
            assertEquals("ok", resp);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testGetSuccessPharmacyWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/ping", exchange -> ok(exchange, "pong"));
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.get("PHARMACY", "/ping");
            assertEquals("pong", resp);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testPutErrorInsuranceWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/pharmacy-insurance/update", exchange -> error(exchange, 500, "err"));
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.put("INSURANCE", "/update", java.util.Map.of("v", 1));
            assertEquals("err", resp);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testGetNotFoundHospitalWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/missing", exchange -> error(exchange, 404, ""));
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.get("HOSPITAL", "/missing");
            assertNotNull(resp);
            assertEquals("", resp);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testPutSuccessPharmacyWithNullBody() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/putnull", exchange -> ok(exchange, "ok"));
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.put("PHARMACY", "/putnull", null);
            assertEquals("ok", resp);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testGetNoContentHospitalWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/nocontent", this::noContent);
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.get("HOSPITAL", "/nocontent");
            assertEquals("", resp, "204 No Content should yield empty response body");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testPostSendsJsonHeaderAndBodyPharmacy() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/validate-post", exchange -> {
                String ct = exchange.getRequestHeaders().getFirst("Content-Type");
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                boolean okHeader = "application/json".equalsIgnoreCase(ct);
                boolean okBody = body.contains("\"x\":1");
                if (okHeader && okBody) {
                    ok(exchange, "ok");
                } else {
                    error(exchange, 400, "bad");
                }
            });
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.post("PHARMACY", "/validate-post", java.util.Map.of("x", 1));
            assertEquals("ok", resp);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testPutSendsJsonHeaderWithNullBodyInsurance() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/pharmacy-insurance/validate-put-null", exchange -> {
                String ct = exchange.getRequestHeaders().getFirst("Content-Type");
                byte[] bytes = exchange.getRequestBody().readAllBytes();
                boolean okHeader = "application/json".equalsIgnoreCase(ct);
                boolean okLen = bytes.length == 0; // null body should produce no request payload
                if (okHeader && okLen) {
                    ok(exchange, "ok");
                } else {
                    error(exchange, 400, "bad");
                }
            });
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            String resp = client.put("INSURANCE", "/validate-put-null", null);
            assertEquals("ok", resp);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void testServiceTypeMatchingIsCaseInsensitive() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/api/case", exchange -> ok(exchange, "ok"));
            server.start();

            int port = server.getAddress().getPort();
            String hospitalBase = "http://localhost:" + port + "/api";
            String pharmacyBase = "http://localhost:" + port + "/api";
            String insuranceBase = "http://localhost:" + port + "/api/pharmacy-insurance";
            ExternalServiceClient client = new ExternalServiceClient(hospitalBase, pharmacyBase, insuranceBase);
            // Use lowercase service type to ensure toUpperCase() mapping works
            String resp = client.get("hospital", "/case");
            assertEquals("ok", resp);
        } finally {
            server.stop(0);
        }
    }

    private void ok(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void error(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void noContent(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        // 204 must not include a response body
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    
}
