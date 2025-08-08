package com.sources.app.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import nl.jqno.equalsverifier.EqualsVerifier;

class MedicinePresIdTest {

    @Test
    void noArgsConstructor_Exists() {
        MedicinePresId id = new MedicinePresId();
        assertNotNull(id);
    }

    @Test
    void constructorWithArgs_SetsFields() {
        Long prescriptionId = 1L;
        Long medicineId = 2L;
        MedicinePresId id = new MedicinePresId(prescriptionId, medicineId);
        // No getters to verify directly, rely on equals/hashCode tests
        assertNotNull(id);
    }

    @Test
    void equalsContract() {
        // Requires EqualsVerifier dependency
        EqualsVerifier.forClass(MedicinePresId.class).verify();
    }

    // Manual equals and hashCode tests
    @Test
    void equals_ReturnsTrueForSameObject() {
        MedicinePresId id1 = new MedicinePresId(1L, 2L);
        assertTrue(id1.equals(id1));
    }

    @Test
    void equals_ReturnsTrueForEqualObjects() {
        MedicinePresId id1 = new MedicinePresId(1L, 2L);
        MedicinePresId id2 = new MedicinePresId(1L, 2L);
        assertTrue(id1.equals(id2));
        assertTrue(id2.equals(id1));
    }

    @Test
    void equals_ReturnsFalseForDifferentPrescriptionId() {
        MedicinePresId id1 = new MedicinePresId(1L, 2L);
        MedicinePresId id2 = new MedicinePresId(3L, 2L);
        assertFalse(id1.equals(id2));
        assertFalse(id2.equals(id1));
    }

    @Test
    void equals_ReturnsFalseForDifferentMedicineId() {
        MedicinePresId id1 = new MedicinePresId(1L, 2L);
        MedicinePresId id2 = new MedicinePresId(1L, 3L);
        assertFalse(id1.equals(id2));
        assertFalse(id2.equals(id1));
    }

    @Test
    void equals_ReturnsFalseForNullPrescriptionId() {
        MedicinePresId id1 = new MedicinePresId(null, 2L);
        MedicinePresId id2 = new MedicinePresId(1L, 2L);
        MedicinePresId id3 = new MedicinePresId(null, 2L);
        assertFalse(id1.equals(id2));
        assertFalse(id2.equals(id1));
        assertTrue(id1.equals(id3));
    }

    @Test
    void equals_ReturnsFalseForNullMedicineId() {
        MedicinePresId id1 = new MedicinePresId(1L, null);
        MedicinePresId id2 = new MedicinePresId(1L, 2L);
        MedicinePresId id3 = new MedicinePresId(1L, null);
        assertFalse(id1.equals(id2));
        assertFalse(id2.equals(id1));
        assertTrue(id1.equals(id3));
    }

    @Test
    void equals_ReturnsFalseForNullObject() {
        MedicinePresId id1 = new MedicinePresId(1L, 2L);
        assertFalse(id1.equals(null));
    }

    @Test
    void equals_ReturnsFalseForDifferentClass() {
        MedicinePresId id1 = new MedicinePresId(1L, 2L);
        assertFalse(id1.equals(new Object()));
    }

    @Test
    void hashCode_IsConsistentForEqualObjects() {
        MedicinePresId id1 = new MedicinePresId(1L, 2L);
        MedicinePresId id2 = new MedicinePresId(1L, 2L);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void hashCode_DiffersForUnequalObjects() {
        MedicinePresId id1 = new MedicinePresId(1L, 2L);
        MedicinePresId id2 = new MedicinePresId(3L, 2L);
        MedicinePresId id3 = new MedicinePresId(1L, 3L);
        assertNotEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.hashCode(), id3.hashCode());
    }

    @Test
    void hashCode_HandlesNulls() {
        MedicinePresId idNullPrescription = new MedicinePresId(null, 2L);
        MedicinePresId idNullMedicine = new MedicinePresId(1L, null);
        MedicinePresId idBothNull = new MedicinePresId(null, null);

        assertDoesNotThrow(() -> idNullPrescription.hashCode());
        assertDoesNotThrow(() -> idNullMedicine.hashCode());
        assertDoesNotThrow(() -> idBothNull.hashCode());

        MedicinePresId idNotNull = new MedicinePresId(1L, 2L);
        assertNotEquals(idNotNull.hashCode(), idNullPrescription.hashCode());
        assertNotEquals(idNotNull.hashCode(), idNullMedicine.hashCode());
    }
} 