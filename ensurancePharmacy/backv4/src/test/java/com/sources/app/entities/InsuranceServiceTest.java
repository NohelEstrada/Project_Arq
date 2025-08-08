package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class InsuranceServiceTest {

    private InsuranceService insuranceService;
    private final Long id = 1L;
    private final String name = "General Consultation";
    private final String description = "Standard medical checkup";
    private final String externalId = "CONSULT-001";
    private final Category category = mock(Category.class);
    private final Category subcategory = mock(Category.class);
    private final Double price = 150.00;
    private final Integer coveragePercentage = 80;
    private final Integer enabled = 1;

    @BeforeEach
    void setUp() {
        insuranceService = new InsuranceService();
        // Set fields manually
        insuranceService.setIdInsuranceService(id);
        insuranceService.setName(name);
        insuranceService.setDescription(description);
        insuranceService.setExternalId(externalId);
        insuranceService.setCategory(category);
        insuranceService.setSubcategory(subcategory);
        insuranceService.setPrice(price);
        insuranceService.setCoveragePercentage(coveragePercentage);
        insuranceService.setEnabled(enabled);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        InsuranceService newService = new InsuranceService();
        assertNull(newService.getIdInsuranceService());
        assertNull(newService.getName());
        assertNull(newService.getDescription());
        assertNull(newService.getExternalId());
        assertNull(newService.getCategory());
        assertNull(newService.getSubcategory());
        assertNull(newService.getPrice());
        assertNull(newService.getCoveragePercentage());
        assertNull(newService.getEnabled());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, insuranceService.getIdInsuranceService());
        assertEquals(name, insuranceService.getName());
        assertEquals(description, insuranceService.getDescription());
        assertEquals(externalId, insuranceService.getExternalId());
        assertEquals(category, insuranceService.getCategory());
        assertEquals(subcategory, insuranceService.getSubcategory());
        assertEquals(price, insuranceService.getPrice());
        assertEquals(coveragePercentage, insuranceService.getCoveragePercentage());
        assertEquals(enabled, insuranceService.getEnabled());

        // Test setters with new values
        InsuranceService testService = new InsuranceService();
        Category newCat = mock(Category.class);
        Category newSubCat = mock(Category.class);

        testService.setIdInsuranceService(2L);
        assertEquals(2L, testService.getIdInsuranceService());

        testService.setName("Specialist Visit");
        assertEquals("Specialist Visit", testService.getName());

        testService.setDescription("Consultation with specialist");
        assertEquals("Consultation with specialist", testService.getDescription());

        testService.setExternalId("SPEC-002");
        assertEquals("SPEC-002", testService.getExternalId());

        testService.setCategory(newCat);
        assertEquals(newCat, testService.getCategory());

        testService.setSubcategory(newSubCat);
        assertEquals(newSubCat, testService.getSubcategory());

        testService.setPrice(300.50);
        assertEquals(300.50, testService.getPrice());

        testService.setCoveragePercentage(75);
        assertEquals(75, testService.getCoveragePercentage());

        testService.setEnabled(0);
        assertEquals(0, testService.getEnabled());
    }
    
    // Add equals/hashCode tests if implemented
} 