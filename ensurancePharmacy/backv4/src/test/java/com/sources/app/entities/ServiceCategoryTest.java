package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ServiceCategoryTest {

    private ServiceCategory serviceCategory;
    private final Service service = mock(Service.class);
    private final Category category = mock(Category.class);

    @BeforeEach
    void setUp() {
        serviceCategory = new ServiceCategory();
        // Set fields manually
        serviceCategory.setService(service);
        serviceCategory.setCategory(category);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        ServiceCategory newSc = new ServiceCategory();
        assertNull(newSc.getService());
        assertNull(newSc.getCategory());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(service, serviceCategory.getService());
        assertEquals(category, serviceCategory.getCategory());

        // Test setters with new values
        ServiceCategory testSc = new ServiceCategory();
        Service newService = mock(Service.class);
        Category newCategory = mock(Category.class);

        testSc.setService(newService);
        assertEquals(newService, testSc.getService());

        testSc.setCategory(newCategory);
        assertEquals(newCategory, testSc.getCategory());
    }
    
    // Testing the composite key class (ServiceCategoryId) directly might be more useful
    // Add equals/hashCode tests if implemented for ServiceCategory itself
} 