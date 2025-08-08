package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ServiceTest {

    private Service service;
    private final Long id = 1L;
    private final Hospital hospital = mock(Hospital.class);
    private final String name = "Blood Test";
    private final String description = "Complete blood count";
    private final Category category = mock(Category.class);
    private final Category subcategory = mock(Category.class);
    private final Double cost = 75.0;
    private final Integer enabled = 1;

    @BeforeEach
    void setUp() {
        service = new Service();
        // Set fields manually
        service.setIdService(id);
        service.setHospital(hospital);
        service.setName(name);
        service.setDescription(description);
        service.setCategory(category);
        service.setSubcategory(subcategory);
        service.setCost(cost);
        service.setEnabled(enabled);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        Service newService = new Service();
        assertNull(newService.getIdService());
        assertNull(newService.getHospital());
        assertNull(newService.getName());
        assertNull(newService.getDescription());
        assertNull(newService.getCategory());
        assertNull(newService.getSubcategory());
        assertNull(newService.getCost());
        assertNull(newService.getEnabled());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, service.getIdService());
        assertEquals(hospital, service.getHospital());
        assertEquals(name, service.getName());
        assertEquals(description, service.getDescription());
        assertEquals(category, service.getCategory());
        assertEquals(subcategory, service.getSubcategory());
        assertEquals(cost, service.getCost());
        assertEquals(enabled, service.getEnabled());

        // Test setters with new values
        Service testService = new Service();
        Hospital newHospital = mock(Hospital.class);
        Category newCat = mock(Category.class);
        Category newSubCat = mock(Category.class);

        testService.setIdService(2L);
        assertEquals(2L, testService.getIdService());

        testService.setHospital(newHospital);
        assertEquals(newHospital, testService.getHospital());

        testService.setName("Ultrasound");
        assertEquals("Ultrasound", testService.getName());

        testService.setDescription("Abdominal Ultrasound");
        assertEquals("Abdominal Ultrasound", testService.getDescription());

        testService.setCategory(newCat);
        assertEquals(newCat, testService.getCategory());

        testService.setSubcategory(newSubCat);
        assertEquals(newSubCat, testService.getSubcategory());

        testService.setCost(120.0);
        assertEquals(120.0, testService.getCost());

        testService.setEnabled(0);
        assertEquals(0, testService.getEnabled());
    }
    
    // Add equals/hashCode tests if implemented
} 