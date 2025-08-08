package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

class PrescriptionApprovalTest {

    private PrescriptionApproval prescriptionApproval;
    private final Long id = 1L;
    private final String authNumber = "AUTH12345";
    private final Long userId = 10L;
    private final String hospitalPrescriptionId = "HOSP_PRES_001";
    private final String details = "{\"medicine\":\"Paracetamol\", \"dosage\":\"500mg\"}";
    private final Double cost = 25.50;
    private final String status = "APPROVED";
    private final String rejectionReason = null; // Or a reason if testing rejection
    private Date approvalDate;

    @BeforeEach
    void setUp() {
        prescriptionApproval = new PrescriptionApproval();
        // Set fields manually for testing getters/setters, 
        // as there is no full constructor
        prescriptionApproval.setIdApproval(id);
        prescriptionApproval.setAuthorizationNumber(authNumber);
        prescriptionApproval.setIdUser(userId);
        prescriptionApproval.setPrescriptionIdHospital(hospitalPrescriptionId);
        prescriptionApproval.setPrescriptionDetails(details);
        prescriptionApproval.setPrescriptionCost(cost);
        prescriptionApproval.setStatus(status);
        prescriptionApproval.setRejectionReason(rejectionReason);
        // Capture the date set by the constructor or set manually if needed
        approvalDate = prescriptionApproval.getApprovalDate(); 
    }

    @Test
    void noArgsConstructor_InitializesApprovalDate() {
        PrescriptionApproval newApproval = new PrescriptionApproval();
        assertNotNull(newApproval.getApprovalDate());
        assertNull(newApproval.getIdApproval()); // ID should be null initially
        // Check other fields are null by default
        assertNull(newApproval.getAuthorizationNumber());
        assertNull(newApproval.getIdUser());
        assertNull(newApproval.getPrescriptionCost());
        assertNull(newApproval.getStatus());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on values set in setUp()
        assertEquals(id, prescriptionApproval.getIdApproval());
        assertEquals(authNumber, prescriptionApproval.getAuthorizationNumber());
        assertEquals(userId, prescriptionApproval.getIdUser());
        assertEquals(hospitalPrescriptionId, prescriptionApproval.getPrescriptionIdHospital());
        assertEquals(details, prescriptionApproval.getPrescriptionDetails());
        assertEquals(cost, prescriptionApproval.getPrescriptionCost());
        assertEquals(status, prescriptionApproval.getStatus());
        assertEquals(rejectionReason, prescriptionApproval.getRejectionReason());
        assertEquals(approvalDate, prescriptionApproval.getApprovalDate());

        // Test setters with new values
        PrescriptionApproval testApproval = new PrescriptionApproval();
        Date newDate = new Date(System.currentTimeMillis() - 100000);

        testApproval.setIdApproval(2L);
        assertEquals(2L, testApproval.getIdApproval());

        testApproval.setAuthorizationNumber("NEW_AUTH");
        assertEquals("NEW_AUTH", testApproval.getAuthorizationNumber());

        testApproval.setIdUser(20L);
        assertEquals(20L, testApproval.getIdUser());
        
        testApproval.setPrescriptionIdHospital("HOSP_PRES_002");
        assertEquals("HOSP_PRES_002", testApproval.getPrescriptionIdHospital());

        testApproval.setPrescriptionDetails("New Details");
        assertEquals("New Details", testApproval.getPrescriptionDetails());

        testApproval.setPrescriptionCost(50.75);
        assertEquals(50.75, testApproval.getPrescriptionCost());

        testApproval.setApprovalDate(newDate);
        assertEquals(newDate, testApproval.getApprovalDate());

        testApproval.setStatus("REJECTED");
        assertEquals("REJECTED", testApproval.getStatus());

        testApproval.setRejectionReason("Invalid cost");
        assertEquals("Invalid cost", testApproval.getRejectionReason());
    }
    
    // Add equals/hashCode tests if implemented
} 