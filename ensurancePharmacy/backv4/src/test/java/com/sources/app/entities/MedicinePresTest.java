package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MedicinePresTest {

    private MedicinePres medicinePres;
    private final Prescription prescription = mock(Prescription.class);
    private final Medicine medicine = mock(Medicine.class);

    @BeforeEach
    void setUp() {
        medicinePres = new MedicinePres();
        // Set fields manually
        medicinePres.setPrescription(prescription);
        medicinePres.setMedicine(medicine);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        MedicinePres newMp = new MedicinePres();
        assertNull(newMp.getPrescription());
        assertNull(newMp.getMedicine());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(prescription, medicinePres.getPrescription());
        assertEquals(medicine, medicinePres.getMedicine());

        // Test setters with new values
        MedicinePres testMp = new MedicinePres();
        Prescription newPrescription = mock(Prescription.class);
        Medicine newMedicine = mock(Medicine.class);

        testMp.setPrescription(newPrescription);
        assertEquals(newPrescription, testMp.getPrescription());

        testMp.setMedicine(newMedicine);
        assertEquals(newMedicine, testMp.getMedicine());
    }
    
    // Testing the composite key class (MedicinePresId) directly might be more useful
    // Add equals/hashCode tests if implemented for MedicinePres itself
} 