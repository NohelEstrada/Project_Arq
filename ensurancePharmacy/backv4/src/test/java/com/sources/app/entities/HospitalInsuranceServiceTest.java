package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Date;

class HospitalInsuranceServiceTest {

    private HospitalInsuranceService hospitalInsuranceService;
    private final Long id = 1L;
    private final Hospital hospital = mock(Hospital.class);
    private final InsuranceService insuranceService = mock(InsuranceService.class);
    private final Integer approved = 1;
    private final Date approvalDate = new Date();
    private final String notes = "Approved for direct billing.";

    @BeforeEach
    void setUp() {
        hospitalInsuranceService = new HospitalInsuranceService();
        // Set fields manually
        hospitalInsuranceService.setIdHospitalService(id);
        hospitalInsuranceService.setHospital(hospital);
        hospitalInsuranceService.setInsuranceService(insuranceService);
        hospitalInsuranceService.setApproved(approved);
        hospitalInsuranceService.setApprovalDate(approvalDate);
        hospitalInsuranceService.setNotes(notes);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        HospitalInsuranceService newService = new HospitalInsuranceService();
        assertNull(newService.getIdHospitalService());
        assertNull(newService.getHospital());
        assertNull(newService.getInsuranceService());
        assertNull(newService.getApproved());
        assertNull(newService.getApprovalDate());
        assertNull(newService.getNotes());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, hospitalInsuranceService.getIdHospitalService());
        assertEquals(hospital, hospitalInsuranceService.getHospital());
        assertEquals(insuranceService, hospitalInsuranceService.getInsuranceService());
        assertEquals(approved, hospitalInsuranceService.getApproved());
        assertEquals(approvalDate, hospitalInsuranceService.getApprovalDate());
        assertEquals(notes, hospitalInsuranceService.getNotes());

        // Test setters with new values
        HospitalInsuranceService testService = new HospitalInsuranceService();
        Hospital newHospital = mock(Hospital.class);
        InsuranceService newInsuranceService = mock(InsuranceService.class);
        Date newDate = new Date(System.currentTimeMillis() - 50000);

        testService.setIdHospitalService(2L);
        assertEquals(2L, testService.getIdHospitalService());

        testService.setHospital(newHospital);
        assertEquals(newHospital, testService.getHospital());

        testService.setInsuranceService(newInsuranceService);
        assertEquals(newInsuranceService, testService.getInsuranceService());

        testService.setApproved(0);
        assertEquals(0, testService.getApproved());

        testService.setApprovalDate(newDate);
        assertEquals(newDate, testService.getApprovalDate());

        testService.setNotes("Revoked.");
        assertEquals("Revoked.", testService.getNotes());
    }
    
    // Add equals/hashCode tests if implemented
} 