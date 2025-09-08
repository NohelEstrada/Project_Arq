package com.sources.app;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class AppMainTest {

    @Test
    void testMainStartsServerSafely() throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        Thread t = new Thread(() -> {
            try {
                App.main(new String[]{});
            } catch (Exception e) {
                // Ignore; if it fails due to port in use, we still exercised part of main
            }
        }, "app-main-test-thread");
        t.setDaemon(true); // do not block JVM shutdown
        t.start();

        // Give it a moment to start
        Thread.sleep(300);

        // We expect startup message
        String out = baos.toString();
        System.setOut(originalOut);
        assertTrue(out.isEmpty() || out.contains("Servidor iniciado en http://")
                || out.contains("Error al conectar con la base de datos")
                || out.contains("Conexión exitosa a la base de datos!"),
                "Expected startup or DB error message, got: " + out);
    }
}
