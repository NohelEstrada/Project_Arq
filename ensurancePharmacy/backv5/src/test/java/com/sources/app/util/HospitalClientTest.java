package com.sources.app.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

public class HospitalClientTest {

    @Test
    void testHospitalClientInstantiation() {
        // TODO: implement tests for HospitalClient
        HospitalClient instance = new HospitalClient();
        assertNotNull(instance);
    }

    @Test
    void testGetSuccessWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/ping", exchange -> ok(exchange, "pong"));
            server.start();

            int port = server.getAddress().getPort();
            HospitalClient.setBaseUrlForTests("http://localhost:" + port);

            String resp = HospitalClient.get("/ping");
            assertEquals("pong", resp);
        } finally {
            HospitalClient.setBaseUrlForTests("http://localhost:8000");
            server.stop(0);
        }
    }

    @Test
    void testPostSuccessWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/echo", exchange -> ok(exchange, "ok"));
            server.start();

            int port = server.getAddress().getPort();
            HospitalClient.setBaseUrlForTests("http://localhost:" + port);

            String resp = HospitalClient.post("/echo", java.util.Map.of("ok", true));
            assertEquals("ok", resp);
        } finally {
            HospitalClient.setBaseUrlForTests("http://localhost:8000");
            server.stop(0);
        }
    }

    @Test
    void testPutSuccessWithEmbeddedServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/update", exchange -> ok(exchange, "updated"));
            server.start();

            int port = server.getAddress().getPort();
            HospitalClient.setBaseUrlForTests("http://localhost:" + port);

            String resp = HospitalClient.put("/update", java.util.Map.of("v", 1));
            assertEquals("updated", resp);
        } finally {
            HospitalClient.setBaseUrlForTests("http://localhost:8000");
            server.stop(0);
        }
    }

    @Test
    void testErrorPathReturnsJson() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            server.createContext("/error", exchange -> error(exchange, 500, "{\"error\":true,\"statusCode\":500,\"message\":\"failure\"}"));
            server.start();

            int port = server.getAddress().getPort();
            HospitalClient.setBaseUrlForTests("http://localhost:" + port);

            String resp = HospitalClient.get("/error");
            assertTrue(resp.contains("\"error\":true"));
            assertTrue(resp.contains("\"statusCode\":500"));
            assertTrue(resp.contains("failure"));
        } finally {
            HospitalClient.setBaseUrlForTests("http://localhost:8000");
            server.stop(0);
        }
    }

    @Test
    void testGetThrowsIOExceptionWhenServerUnavailable() {
        HospitalClient.setBaseUrlForTests("http://nonexistent.invalid");
        Throwable t = assertThrows(IOException.class, () -> HospitalClient.get("/unavailable"));
        assertNotNull(t);
        HospitalClient.setBaseUrlForTests("http://localhost:8000");
    }

    @Test
    void testPostThrowsIOExceptionWhenServerUnavailable() {
        HospitalClient.setBaseUrlForTests("http://nonexistent.invalid");
        Throwable t = assertThrows(IOException.class, () -> HospitalClient.post("/unavailable", java.util.Map.of("x", 1)));
        assertNotNull(t);
        HospitalClient.setBaseUrlForTests("http://localhost:8000");
    }

    @Test
    void testPutThrowsIOExceptionWhenServerUnavailable() {
        HospitalClient.setBaseUrlForTests("http://nonexistent.invalid");
        Throwable t = assertThrows(IOException.class, () -> HospitalClient.put("/unavailable", java.util.Map.of("x", 1)));
        assertNotNull(t);
        HospitalClient.setBaseUrlForTests("http://localhost:8000");
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
}
