package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    private Category category;
    private final Long id = 1L;
    private final String name = "Consultations";
    private final Integer enabled = 1;

    @BeforeEach
    void setUp() {
        category = new Category();
        // Set fields manually
        category.setIdCategory(id);
        category.setName(name);
        category.setEnabled(enabled);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        Category newCategory = new Category();
        assertNull(newCategory.getIdCategory());
        assertNull(newCategory.getName());
        assertNull(newCategory.getEnabled());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, category.getIdCategory());
        assertEquals(name, category.getName());
        assertEquals(enabled, category.getEnabled());

        // Test setters with new values
        Category testCategory = new Category();

        testCategory.setIdCategory(2L);
        assertEquals(2L, testCategory.getIdCategory());

        testCategory.setName("Laboratory");
        assertEquals("Laboratory", testCategory.getName());

        testCategory.setEnabled(0);
        assertEquals(0, testCategory.getEnabled());
    }
    
    // Add equals/hashCode tests if implemented
} 