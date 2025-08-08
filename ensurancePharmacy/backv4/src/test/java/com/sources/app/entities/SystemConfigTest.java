package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

class SystemConfigTest {

    private SystemConfig systemConfig;
    private final Long id = 1L;
    private final String key = "TEST_KEY";
    private final String value = "test_value";
    private final String description = "This is a test configuration";
    private final Date now = new Date();

    @BeforeEach
    void setUp() {
        // Use the constructor that takes key, value, description
        systemConfig = new SystemConfig(key, value, description);
        systemConfig.setIdConfig(id); // Set ID manually for tests
        // Manually set dates for testing getters/setters, as @PrePersist/@PreUpdate require persistence context
        systemConfig.setCreatedAt(now);
        systemConfig.setUpdatedAt(now); 
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        SystemConfig newConfig = new SystemConfig();
        assertNull(newConfig.getIdConfig());
        assertNull(newConfig.getConfigKey());
        assertNull(newConfig.getConfigValue());
        assertNull(newConfig.getConfigDescription());
        assertNull(newConfig.getCreatedAt()); // Will be set by @PrePersist on actual save
        assertNull(newConfig.getUpdatedAt()); // Will be set by @PreUpdate on actual update
    }
    
    @Test
    void constructorWithData_SetsFields() {
         SystemConfig constructedConfig = new SystemConfig(key, value, description);
         assertEquals(key, constructedConfig.getConfigKey());
         assertEquals(value, constructedConfig.getConfigValue());
         assertEquals(description, constructedConfig.getConfigDescription());
         // Dates are not set by this constructor
         assertNull(constructedConfig.getCreatedAt());
         assertNull(constructedConfig.getUpdatedAt());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, systemConfig.getIdConfig());
        assertEquals(key, systemConfig.getConfigKey());
        assertEquals(value, systemConfig.getConfigValue());
        assertEquals(description, systemConfig.getConfigDescription());
        assertEquals(now, systemConfig.getCreatedAt());
        assertEquals(now, systemConfig.getUpdatedAt());

        // Test setters with new values
        SystemConfig testConfig = new SystemConfig();
        Date later = new Date(System.currentTimeMillis() + 10000);

        testConfig.setIdConfig(2L);
        assertEquals(2L, testConfig.getIdConfig());

        testConfig.setConfigKey("NEW_KEY");
        assertEquals("NEW_KEY", testConfig.getConfigKey());

        testConfig.setConfigValue("new_value");
        assertEquals("new_value", testConfig.getConfigValue());

        testConfig.setConfigDescription("New description");
        assertEquals("New description", testConfig.getConfigDescription());
        
        testConfig.setCreatedAt(later);
        assertEquals(later, testConfig.getCreatedAt());
        
        testConfig.setUpdatedAt(later);
        assertEquals(later, testConfig.getUpdatedAt());
    }
    
    // Add equals/hashCode tests if implemented
} 