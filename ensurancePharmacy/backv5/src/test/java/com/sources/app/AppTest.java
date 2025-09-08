package com.sources.app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    void sanity() {
        // Verifica que la clase App esté disponible y el nombre del paquete sea el esperado
        assertEquals("com.sources.app.App", App.class.getName());
    }
}
