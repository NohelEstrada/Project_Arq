package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PharmacyTest {

    private Pharmacy pharmacy;
    private final Long id = 1L;
    private final String name = "Downtown Pharmacy";
    private final String address = "456 Center St";
    private final Long phone = 5559876543L;
    private final String email = "contact@downtownpharmacy.com";
    private final Integer enabled = 1;

    @BeforeEach
    void setUp() {
        pharmacy = new Pharmacy();
        // Set fields manually
        pharmacy.setIdPharmacy(id);
        pharmacy.setName(name);
        pharmacy.setAddress(address);
        pharmacy.setPhone(phone);
        pharmacy.setEmail(email);
        pharmacy.setEnabled(enabled);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        Pharmacy newPharmacy = new Pharmacy();
        assertNull(newPharmacy.getIdPharmacy());
        assertNull(newPharmacy.getName());
        assertNull(newPharmacy.getAddress());
        assertNull(newPharmacy.getPhone());
        assertNull(newPharmacy.getEmail());
        assertNull(newPharmacy.getEnabled());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, pharmacy.getIdPharmacy());
        assertEquals(name, pharmacy.getName());
        assertEquals(address, pharmacy.getAddress());
        assertEquals(phone, pharmacy.getPhone());
        assertEquals(email, pharmacy.getEmail());
        assertEquals(enabled, pharmacy.getEnabled());

        // Test setters with new values
        Pharmacy testPharmacy = new Pharmacy();

        testPharmacy.setIdPharmacy(2L);
        assertEquals(2L, testPharmacy.getIdPharmacy());

        testPharmacy.setName("Uptown Pharmacy");
        assertEquals("Uptown Pharmacy", testPharmacy.getName());

        testPharmacy.setAddress("789 High St");
        assertEquals("789 High St", testPharmacy.getAddress());

        testPharmacy.setPhone(5551112222L);
        assertEquals(5551112222L, testPharmacy.getPhone());

        testPharmacy.setEmail("info@uptownpharma.com");
        assertEquals("info@uptownpharma.com", testPharmacy.getEmail());

        testPharmacy.setEnabled(0);
        assertEquals(0, testPharmacy.getEnabled());
    }
    
    // Add equals/hashCode tests if implemented
} 