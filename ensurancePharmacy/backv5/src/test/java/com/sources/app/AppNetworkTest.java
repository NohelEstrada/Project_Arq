package com.sources.app;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

public class AppNetworkTest {

    @Test
    void testGetLocalExternalIp_viaReflection() throws Exception {
        // Access private static method getLocalExternalIp()
        Method m = App.class.getDeclaredMethod("getLocalExternalIp");
        m.setAccessible(true);
        Object result = m.invoke(null);
        assertNotNull(result);
        String ip = result.toString();
        assertFalse(ip.isEmpty(), "IP no debe ser vacía");
        // Basic sanity: IPv4-like or localhost fallback
        assertTrue(ip.equals("127.0.0.1") || ip.matches("\\d{1,3}(\\.\\d{1,3}){3}"),
                "IP debe ser IPv4 o 127.0.0.1, was: " + ip);
    }
}
