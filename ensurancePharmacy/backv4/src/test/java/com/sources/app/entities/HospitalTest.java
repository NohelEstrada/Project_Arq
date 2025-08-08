package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HospitalTest {

    private Hospital hospital;
    private final Long id = 1L;
    private final String name = "General Hospital";
    private final String address = "123 Health St";
    private final Long phone = 5551234567L;
    private final String email = "info@generalhospital.com";
    private final Integer enabled = 1;

    @BeforeEach
    void setUp() {
        hospital = new Hospital();
        // Set fields manually
        hospital.setIdHospital(id);
        hospital.setName(name);
        hospital.setAddress(address);
        hospital.setPhone(phone);
        hospital.setEmail(email);
        hospital.setEnabled(enabled);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        Hospital newHospital = new Hospital();
        assertNull(newHospital.getIdHospital());
        assertNull(newHospital.getName());
        assertNull(newHospital.getAddress());
        assertNull(newHospital.getPhone());
        assertNull(newHospital.getEmail());
        assertNull(newHospital.getEnabled());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, hospital.getIdHospital());
        assertEquals(name, hospital.getName());
        assertEquals(address, hospital.getAddress());
        assertEquals(phone, hospital.getPhone());
        assertEquals(email, hospital.getEmail());
        assertEquals(enabled, hospital.getEnabled());

        // Test setters with new values
        Hospital testHospital = new Hospital();

        testHospital.setIdHospital(2L);
        assertEquals(2L, testHospital.getIdHospital());

        testHospital.setName("Community Hospital");
        assertEquals("Community Hospital", testHospital.getName());

        testHospital.setAddress("456 Wellness Ave");
        assertEquals("456 Wellness Ave", testHospital.getAddress());

        testHospital.setPhone(5557654321L);
        assertEquals(5557654321L, testHospital.getPhone());

        testHospital.setEmail("contact@communityhosp.com");
        assertEquals("contact@communityhosp.com", testHospital.getEmail());

        testHospital.setEnabled(0);
        assertEquals(0, testHospital.getEnabled());
    }
    
    // Add equals/hashCode tests if implemented
} 