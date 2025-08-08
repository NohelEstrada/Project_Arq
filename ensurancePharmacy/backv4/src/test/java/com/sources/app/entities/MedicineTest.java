package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;

class MedicineTest {

    private Medicine medicine;
    private final Long id = 1L;
    private final String name = "Paracetamol";
    private final String description = "Fever and pain relief";
    private final BigDecimal price = new BigDecimal("5.99");
    private final Pharmacy pharmacy = mock(Pharmacy.class);
    private final Integer enabled = 1;
    private final String activePrinciple = "Paracetamol";
    private final String presentation = "500mg tablets";
    private final Integer stock = 200;
    private final String brand = "Generic";
    private final Integer coverage = 1; // Assuming 1 means covered

    @BeforeEach
    void setUp() {
        medicine = new Medicine();
        // Set fields manually
        medicine.setIdMedicine(id);
        medicine.setName(name);
        medicine.setDescription(description);
        medicine.setPrice(price);
        medicine.setPharmacy(pharmacy);
        medicine.setEnabled(enabled);
        medicine.setActivePrinciple(activePrinciple);
        medicine.setPresentation(presentation);
        medicine.setStock(stock);
        medicine.setBrand(brand);
        medicine.setCoverage(coverage);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        Medicine newMed = new Medicine();
        assertNull(newMed.getIdMedicine());
        assertNull(newMed.getName());
        assertNull(newMed.getDescription());
        assertNull(newMed.getPrice());
        assertNull(newMed.getPharmacy());
        assertNull(newMed.getEnabled());
        assertNull(newMed.getActivePrinciple());
        assertNull(newMed.getPresentation());
        assertNull(newMed.getStock());
        assertNull(newMed.getBrand());
        assertNull(newMed.getCoverage());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, medicine.getIdMedicine());
        assertEquals(name, medicine.getName());
        assertEquals(description, medicine.getDescription());
        assertEquals(price, medicine.getPrice());
        assertEquals(pharmacy, medicine.getPharmacy());
        assertEquals(enabled, medicine.getEnabled());
        assertEquals(activePrinciple, medicine.getActivePrinciple());
        assertEquals(presentation, medicine.getPresentation());
        assertEquals(stock, medicine.getStock());
        assertEquals(brand, medicine.getBrand());
        assertEquals(coverage, medicine.getCoverage());

        // Test setters with new values
        Medicine testMed = new Medicine();
        Pharmacy newPharmacy = mock(Pharmacy.class);
        BigDecimal newPrice = new BigDecimal("10.50");

        testMed.setIdMedicine(2L);
        assertEquals(2L, testMed.getIdMedicine());

        testMed.setName("Ibuprofen");
        assertEquals("Ibuprofen", testMed.getName());

        testMed.setDescription("Anti-inflammatory");
        assertEquals("Anti-inflammatory", testMed.getDescription());

        testMed.setPrice(newPrice);
        assertEquals(newPrice, testMed.getPrice());

        testMed.setPharmacy(newPharmacy);
        assertEquals(newPharmacy, testMed.getPharmacy());

        testMed.setEnabled(0);
        assertEquals(0, testMed.getEnabled());

        testMed.setActivePrinciple("Ibuprofen");
        assertEquals("Ibuprofen", testMed.getActivePrinciple());

        testMed.setPresentation("200mg capsules");
        assertEquals("200mg capsules", testMed.getPresentation());

        testMed.setStock(150);
        assertEquals(150, testMed.getStock());

        testMed.setBrand("Advil");
        assertEquals("Advil", testMed.getBrand());

        testMed.setCoverage(0); // Assuming 0 means not covered
        assertEquals(0, testMed.getCoverage());
    }
    
    // Add equals/hashCode tests if implemented
} 