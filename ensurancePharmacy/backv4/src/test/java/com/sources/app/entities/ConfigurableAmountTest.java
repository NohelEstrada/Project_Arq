package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

class ConfigurableAmountTest {

    private ConfigurableAmount configurableAmount;
    private final Long id = 1L;
    private final BigDecimal amount = new BigDecimal("250.00");

    @BeforeEach
    void setUp() {
        configurableAmount = new ConfigurableAmount();
        // Set fields manually
        configurableAmount.setIdConfigurableAmount(id);
        configurableAmount.setPrescriptionAmount(amount);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        ConfigurableAmount newAmount = new ConfigurableAmount();
        assertNull(newAmount.getIdConfigurableAmount());
        assertNull(newAmount.getPrescriptionAmount());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, configurableAmount.getIdConfigurableAmount());
        assertEquals(amount, configurableAmount.getPrescriptionAmount());

        // Test setters with new values
        ConfigurableAmount testAmount = new ConfigurableAmount();
        BigDecimal newAmountValue = new BigDecimal("300.50");

        testAmount.setIdConfigurableAmount(2L);
        assertEquals(2L, testAmount.getIdConfigurableAmount());

        testAmount.setPrescriptionAmount(newAmountValue);
        assertEquals(newAmountValue, testAmount.getPrescriptionAmount());
    }
    
    // Add equals/hashCode tests if implemented
} 