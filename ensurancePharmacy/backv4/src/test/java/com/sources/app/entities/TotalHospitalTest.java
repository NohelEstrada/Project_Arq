package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.Date;

class TotalHospitalTest {

    private TotalHospital totalHospital;
    private final Long id = 1L;
    private final Hospital hospital = mock(Hospital.class);
    private final Date totalDate = new Date();
    private final BigDecimal total = new BigDecimal("5000.75");

    @BeforeEach
    void setUp() {
        totalHospital = new TotalHospital();
        // Set fields manually
        totalHospital.setIdTotalHospital(id);
        totalHospital.setHospital(hospital);
        totalHospital.setTotalDate(totalDate);
        totalHospital.setTotal(total);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        TotalHospital newTotal = new TotalHospital();
        assertNull(newTotal.getIdTotalHospital());
        assertNull(newTotal.getHospital());
        assertNull(newTotal.getTotalDate());
        assertNull(newTotal.getTotal());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, totalHospital.getIdTotalHospital());
        assertEquals(hospital, totalHospital.getHospital());
        assertEquals(totalDate, totalHospital.getTotalDate());
        assertEquals(total, totalHospital.getTotal());

        // Test setters with new values
        TotalHospital testTotal = new TotalHospital();
        Hospital newHospital = mock(Hospital.class);
        Date newDate = new Date(System.currentTimeMillis() + 10000);
        BigDecimal newTotalAmount = new BigDecimal("6000.00");

        testTotal.setIdTotalHospital(2L);
        assertEquals(2L, testTotal.getIdTotalHospital());

        testTotal.setHospital(newHospital);
        assertEquals(newHospital, testTotal.getHospital());

        testTotal.setTotalDate(newDate);
        assertEquals(newDate, testTotal.getTotalDate());

        testTotal.setTotal(newTotalAmount);
        assertEquals(newTotalAmount, testTotal.getTotal());
    }
    
    // Add equals/hashCode tests if implemented
} 