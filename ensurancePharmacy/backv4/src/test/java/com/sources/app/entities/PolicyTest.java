package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

class PolicyTest {

    private Policy policy;
    private final Long id = 1L;
    private final Float percentage = 0.75f;
    private final Date creationDate = new Date();
    private final Date expDate = new Date(System.currentTimeMillis() + 31536000000L); // 1 year later
    private final Float cost = 1200.0f;
    private final Integer enabled = 1;

    @BeforeEach
    void setUp() {
        // Use the full constructor
        policy = new Policy(percentage, creationDate, expDate, cost, enabled);
        policy.setIdPolicy(id); // Set ID manually
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        Policy newPolicy = new Policy();
        assertNull(newPolicy.getIdPolicy());
        assertNull(newPolicy.getPercentage());
        assertNull(newPolicy.getCreationDate());
        assertNull(newPolicy.getExpDate());
        assertNull(newPolicy.getCost());
        assertNull(newPolicy.getEnabled());
    }

    @Test
    void constructorWithArgs_SetsFields() {
        // Assertions based on setUp values (using the policy object created in setUp)
        assertEquals(id, policy.getIdPolicy());
        assertEquals(percentage, policy.getPercentage());
        assertEquals(creationDate, policy.getCreationDate());
        assertEquals(expDate, policy.getExpDate());
        assertEquals(cost, policy.getCost());
        assertEquals(enabled, policy.getEnabled());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        Policy testPolicy = new Policy(); // Start with a default object
        Date newCreationDate = new Date(System.currentTimeMillis() - 86400000); // Yesterday
        Date newExpDate = new Date(System.currentTimeMillis() + (86400000 * 30)); // 30 days later

        testPolicy.setIdPolicy(2L);
        assertEquals(2L, testPolicy.getIdPolicy());

        testPolicy.setPercentage(0.9f);
        assertEquals(0.9f, testPolicy.getPercentage());

        testPolicy.setCreationDate(newCreationDate);
        assertEquals(newCreationDate, testPolicy.getCreationDate());

        testPolicy.setExpDate(newExpDate);
        assertEquals(newExpDate, testPolicy.getExpDate());

        testPolicy.setCost(1500.50f);
        assertEquals(1500.50f, testPolicy.getCost());

        testPolicy.setEnabled(0);
        assertEquals(0, testPolicy.getEnabled());
    }
    
    // Add equals/hashCode tests if implemented
} 