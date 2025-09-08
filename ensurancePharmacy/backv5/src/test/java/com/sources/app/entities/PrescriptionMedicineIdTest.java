package com.sources.app.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PrescriptionMedicineIdTest {

    @Test
    void testPrescriptionMedicineIdInstantiation() {
        PrescriptionMedicineId instance = new PrescriptionMedicineId();
        assertNotNull(instance);
    }

    @Test
    void testPrescriptionMedicineIdConstructorWithParameters() {
        Long prescriptionId = 1L;
        Long medicineId = 2L;
        PrescriptionMedicineId id = new PrescriptionMedicineId(prescriptionId, medicineId);
        
        assertEquals(prescriptionId, id.getPrescriptionId());
        assertEquals(medicineId, id.getMedicineId());
    }

    @Test
    void testPrescriptionMedicineIdSettersAndGetters() {
        PrescriptionMedicineId id = new PrescriptionMedicineId();
        Long prescriptionId = 10L;
        Long medicineId = 20L;
        
        id.setPrescriptionId(prescriptionId);
        id.setMedicineId(medicineId);
        
        assertEquals(prescriptionId, id.getPrescriptionId());
        assertEquals(medicineId, id.getMedicineId());
    }

    @Test
    void testPrescriptionMedicineIdEquals() {
        PrescriptionMedicineId id1 = new PrescriptionMedicineId(1L, 2L);
        PrescriptionMedicineId id2 = new PrescriptionMedicineId(1L, 2L);
        PrescriptionMedicineId id3 = new PrescriptionMedicineId(2L, 3L);
        
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertEquals(id1, id1);
        assertNotEquals(id1, null);
        assertNotEquals(id1, "not a PrescriptionMedicineId");
    }

    @Test
    void testPrescriptionMedicineIdHashCode() {
        PrescriptionMedicineId id1 = new PrescriptionMedicineId(1L, 2L);
        PrescriptionMedicineId id2 = new PrescriptionMedicineId(1L, 2L);
        PrescriptionMedicineId id3 = new PrescriptionMedicineId(2L, 3L);
        
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.hashCode(), id3.hashCode());
    }

    @Test
    void testPrescriptionMedicineIdWithNullValues() {
        PrescriptionMedicineId id1 = new PrescriptionMedicineId(null, null);
        PrescriptionMedicineId id2 = new PrescriptionMedicineId(null, null);
        PrescriptionMedicineId id3 = new PrescriptionMedicineId(1L, null);
        
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
