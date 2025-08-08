package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.Date;

class PrescriptionTest {

    private Prescription prescription;
    private final Long id = 1L;
    private final Hospital hospital = mock(Hospital.class);
    private final User user = mock(User.class);
    private final Medicine medicine = mock(Medicine.class);
    private final Pharmacy pharmacy = mock(Pharmacy.class);
    private final Date prescriptionDate = new Date();
    private final BigDecimal total = new BigDecimal("120.50");
    private final BigDecimal copay = new BigDecimal("12.05");
    private final String comment = "Take one daily";
    private final Integer secured = 1;
    private final String auth = "AUTH-XYZ";

    @BeforeEach
    void setUp() {
        prescription = new Prescription();
        // Set fields manually
        prescription.setIdPrescription(id);
        prescription.setHospital(hospital);
        prescription.setUser(user);
        prescription.setMedicine(medicine);
        prescription.setPharmacy(pharmacy);
        prescription.setPrescriptionDate(prescriptionDate);
        prescription.setTotal(total);
        prescription.setCopay(copay);
        prescription.setPrescriptionComment(comment);
        prescription.setSecured(secured);
        prescription.setAuth(auth);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        Prescription newPrescription = new Prescription();
        assertNull(newPrescription.getIdPrescription());
        assertNull(newPrescription.getHospital());
        assertNull(newPrescription.getUser());
        assertNull(newPrescription.getMedicine());
        assertNull(newPrescription.getPharmacy());
        assertNull(newPrescription.getPrescriptionDate());
        assertNull(newPrescription.getTotal());
        assertNull(newPrescription.getCopay());
        assertNull(newPrescription.getPrescriptionComment());
        assertNull(newPrescription.getSecured());
        assertNull(newPrescription.getAuth());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, prescription.getIdPrescription());
        assertEquals(hospital, prescription.getHospital());
        assertEquals(user, prescription.getUser());
        assertEquals(medicine, prescription.getMedicine());
        assertEquals(pharmacy, prescription.getPharmacy());
        assertEquals(prescriptionDate, prescription.getPrescriptionDate());
        assertEquals(total, prescription.getTotal());
        assertEquals(copay, prescription.getCopay());
        assertEquals(comment, prescription.getPrescriptionComment());
        assertEquals(secured, prescription.getSecured());
        assertEquals(auth, prescription.getAuth());

        // Test setters with new values
        Prescription testPrescription = new Prescription();
        Hospital newHospital = mock(Hospital.class);
        User newUser = mock(User.class);
        Medicine newMedicine = mock(Medicine.class);
        Pharmacy newPharmacy = mock(Pharmacy.class);
        Date newDate = new Date(System.currentTimeMillis() + 99999);
        BigDecimal newTotal = new BigDecimal("200.00");
        BigDecimal newCopay = new BigDecimal("20.00");

        testPrescription.setIdPrescription(2L);
        assertEquals(2L, testPrescription.getIdPrescription());

        testPrescription.setHospital(newHospital);
        assertEquals(newHospital, testPrescription.getHospital());

        testPrescription.setUser(newUser);
        assertEquals(newUser, testPrescription.getUser());

        testPrescription.setMedicine(newMedicine);
        assertEquals(newMedicine, testPrescription.getMedicine());

        testPrescription.setPharmacy(newPharmacy);
        assertEquals(newPharmacy, testPrescription.getPharmacy());

        testPrescription.setPrescriptionDate(newDate);
        assertEquals(newDate, testPrescription.getPrescriptionDate());

        testPrescription.setTotal(newTotal);
        assertEquals(newTotal, testPrescription.getTotal());

        testPrescription.setCopay(newCopay);
        assertEquals(newCopay, testPrescription.getCopay());

        testPrescription.setPrescriptionComment("New comment");
        assertEquals("New comment", testPrescription.getPrescriptionComment());

        testPrescription.setSecured(0);
        assertEquals(0, testPrescription.getSecured());

        testPrescription.setAuth("NEW-AUTH");
        assertEquals("NEW-AUTH", testPrescription.getAuth());
    }
    
    // Add equals/hashCode tests if implemented
} 