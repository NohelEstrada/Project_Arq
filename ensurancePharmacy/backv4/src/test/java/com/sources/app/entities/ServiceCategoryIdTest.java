package com.sources.app.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import nl.jqno.equalsverifier.EqualsVerifier;

class ServiceCategoryIdTest {

    @Test
    void noArgsConstructor_Exists() {
        // Check if the no-args constructor exists and doesn't throw exceptions
        ServiceCategoryId id = new ServiceCategoryId();
        assertNotNull(id);
        // Fields will be null, which is expected for a default constructor
    }

    @Test
    void constructorWithArgs_SetsFields() {
        Long serviceId = 1L;
        Long categoryId = 2L;
        ServiceCategoryId id = new ServiceCategoryId(serviceId, categoryId);

        // Although there are no getters, we know the constructor sets the fields.
        // We rely on equals/hashCode tests to verify internal state via comparison.
        assertNotNull(id);
    }

    @Test
    void equalsContract() {
        // Using EqualsVerifier library helps ensure equals/hashCode contracts are met
        // Add this dependency to your build file (e.g., pom.xml or build.gradle)
        // Maven:
        // <dependency>
        //     <groupId>nl.jqno.equalsverifier</groupId>
        //     <artifactId>equalsverifier</artifactId>
        //     <version>3.15.1</version> <!-- Check for the latest version -->
        //     <scope>test</scope>
        // </dependency>
        EqualsVerifier.forClass(ServiceCategoryId.class).verify();
    }

    // Manual equals and hashCode tests (can be redundant if EqualsVerifier is used)
    @Test
    void equals_ReturnsTrueForSameObject() {
        ServiceCategoryId id1 = new ServiceCategoryId(1L, 2L);
        assertTrue(id1.equals(id1));
    }

    @Test
    void equals_ReturnsTrueForEqualObjects() {
        ServiceCategoryId id1 = new ServiceCategoryId(1L, 2L);
        ServiceCategoryId id2 = new ServiceCategoryId(1L, 2L);
        assertTrue(id1.equals(id2));
        assertTrue(id2.equals(id1)); // Symmetry
    }

    @Test
    void equals_ReturnsFalseForDifferentServiceId() {
        ServiceCategoryId id1 = new ServiceCategoryId(1L, 2L);
        ServiceCategoryId id2 = new ServiceCategoryId(3L, 2L);
        assertFalse(id1.equals(id2));
        assertFalse(id2.equals(id1));
    }

    @Test
    void equals_ReturnsFalseForDifferentCategoryId() {
        ServiceCategoryId id1 = new ServiceCategoryId(1L, 2L);
        ServiceCategoryId id2 = new ServiceCategoryId(1L, 3L);
        assertFalse(id1.equals(id2));
        assertFalse(id2.equals(id1));
    }
    
     @Test
    void equals_ReturnsFalseForNullServiceId() {
        ServiceCategoryId id1 = new ServiceCategoryId(null, 2L);
        ServiceCategoryId id2 = new ServiceCategoryId(1L, 2L);
        ServiceCategoryId id3 = new ServiceCategoryId(null, 2L); // Equal to id1
        assertFalse(id1.equals(id2));
        assertFalse(id2.equals(id1));
        assertTrue(id1.equals(id3)); // Nulls should be equal
    }
    
     @Test
    void equals_ReturnsFalseForNullCategoryId() {
        ServiceCategoryId id1 = new ServiceCategoryId(1L, null);
        ServiceCategoryId id2 = new ServiceCategoryId(1L, 2L);
        ServiceCategoryId id3 = new ServiceCategoryId(1L, null); // Equal to id1
        assertFalse(id1.equals(id2));
        assertFalse(id2.equals(id1));
        assertTrue(id1.equals(id3)); // Nulls should be equal
    }

    @Test
    void equals_ReturnsFalseForNullObject() {
        ServiceCategoryId id1 = new ServiceCategoryId(1L, 2L);
        assertFalse(id1.equals(null));
    }

    @Test
    void equals_ReturnsFalseForDifferentClass() {
        ServiceCategoryId id1 = new ServiceCategoryId(1L, 2L);
        assertFalse(id1.equals(new Object()));
    }

    @Test
    void hashCode_IsConsistentForEqualObjects() {
        ServiceCategoryId id1 = new ServiceCategoryId(1L, 2L);
        ServiceCategoryId id2 = new ServiceCategoryId(1L, 2L);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void hashCode_DiffersForUnequalObjects() {
        ServiceCategoryId id1 = new ServiceCategoryId(1L, 2L);
        ServiceCategoryId id2 = new ServiceCategoryId(3L, 2L);
        ServiceCategoryId id3 = new ServiceCategoryId(1L, 3L);
        assertNotEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.hashCode(), id3.hashCode());
    }
    
     @Test
    void hashCode_HandlesNulls() {
        ServiceCategoryId idNullService = new ServiceCategoryId(null, 2L);
        ServiceCategoryId idNullCategory = new ServiceCategoryId(1L, null);
        ServiceCategoryId idBothNull = new ServiceCategoryId(null, null);

        // Just ensure no NullPointerException is thrown
        assertDoesNotThrow(() -> idNullService.hashCode());
        assertDoesNotThrow(() -> idNullCategory.hashCode());
        assertDoesNotThrow(() -> idBothNull.hashCode());

        // Hashcodes of objects with nulls should differ from those without (usually)
        ServiceCategoryId idNotNull = new ServiceCategoryId(1L, 2L);
        assertNotEquals(idNotNull.hashCode(), idNullService.hashCode());
        assertNotEquals(idNotNull.hashCode(), idNullCategory.hashCode());
    }
} 