package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

class ServiceApprovalTest {

    private ServiceApproval serviceApproval;
    private final Long id = 1L;
    private final String approvalCode = "AP-TEST123";
    private final Long userId = 10L;
    private final Long hospitalId = 20L;
    private final String serviceId = "HOSP_SVC_456";
    private final String serviceName = "X-Ray";
    private final Double serviceCost = 120.0;
    private final Double coveredAmount = 96.0;
    private final Double patientAmount = 24.0;
    private final String status = "APPROVED";
    private final Date approvalDate = new Date();
    private final Date completedDate = new Date(System.currentTimeMillis() + 10000);
    private final String rejectionReason = null;
    private final Long prescriptionId = 30L;
    private final Double prescriptionTotal = 45.50;

    @BeforeEach
    void setUp() {
        serviceApproval = new ServiceApproval();
        // Set fields manually for testing
        serviceApproval.setId(id);
        serviceApproval.setApprovalCode(approvalCode);
        serviceApproval.setUserId(userId);
        serviceApproval.setHospitalId(hospitalId);
        serviceApproval.setServiceId(serviceId);
        serviceApproval.setServiceName(serviceName);
        serviceApproval.setServiceCost(serviceCost);
        serviceApproval.setCoveredAmount(coveredAmount);
        serviceApproval.setPatientAmount(patientAmount);
        serviceApproval.setStatus(status);
        serviceApproval.setApprovalDate(approvalDate);
        serviceApproval.setCompletedDate(completedDate);
        serviceApproval.setRejectionReason(rejectionReason);
        serviceApproval.setPrescriptionId(prescriptionId);
        serviceApproval.setPrescriptionTotal(prescriptionTotal);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        ServiceApproval newApproval = new ServiceApproval();
        assertNull(newApproval.getId());
        assertNull(newApproval.getApprovalCode());
        assertNull(newApproval.getUserId());
        assertNull(newApproval.getHospitalId());
        assertNull(newApproval.getServiceId());
        assertNull(newApproval.getServiceName());
        assertNull(newApproval.getServiceCost());
        assertNull(newApproval.getCoveredAmount());
        assertNull(newApproval.getPatientAmount());
        assertNull(newApproval.getStatus());
        assertNull(newApproval.getApprovalDate());
        assertNull(newApproval.getCompletedDate());
        assertNull(newApproval.getRejectionReason());
        assertNull(newApproval.getPrescriptionId());
        assertNull(newApproval.getPrescriptionTotal());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, serviceApproval.getId());
        assertEquals(approvalCode, serviceApproval.getApprovalCode());
        assertEquals(userId, serviceApproval.getUserId());
        assertEquals(hospitalId, serviceApproval.getHospitalId());
        assertEquals(serviceId, serviceApproval.getServiceId());
        assertEquals(serviceName, serviceApproval.getServiceName());
        assertEquals(serviceCost, serviceApproval.getServiceCost());
        assertEquals(coveredAmount, serviceApproval.getCoveredAmount());
        assertEquals(patientAmount, serviceApproval.getPatientAmount());
        assertEquals(status, serviceApproval.getStatus());
        assertEquals(approvalDate, serviceApproval.getApprovalDate());
        assertEquals(completedDate, serviceApproval.getCompletedDate());
        assertEquals(rejectionReason, serviceApproval.getRejectionReason());
        assertEquals(prescriptionId, serviceApproval.getPrescriptionId());
        assertEquals(prescriptionTotal, serviceApproval.getPrescriptionTotal());

        // Test setters with new values
        ServiceApproval testApproval = new ServiceApproval();
        Date newApprovalDate = new Date();
        Date newCompletedDate = new Date(System.currentTimeMillis() + 20000);
        
        testApproval.setId(2L);
        assertEquals(2L, testApproval.getId());

        testApproval.setApprovalCode("AP-NEW456");
        assertEquals("AP-NEW456", testApproval.getApprovalCode());
        
        testApproval.setUserId(11L);
        assertEquals(11L, testApproval.getUserId());
        
        testApproval.setHospitalId(21L);
        assertEquals(21L, testApproval.getHospitalId());
        
        testApproval.setServiceId("HOSP_SVC_789");
        assertEquals("HOSP_SVC_789", testApproval.getServiceId());
        
        testApproval.setServiceName("MRI Scan");
        assertEquals("MRI Scan", testApproval.getServiceName());
        
        testApproval.setServiceCost(500.0);
        assertEquals(500.0, testApproval.getServiceCost());
        
        testApproval.setCoveredAmount(400.0);
        assertEquals(400.0, testApproval.getCoveredAmount());
        
        testApproval.setPatientAmount(100.0);
        assertEquals(100.0, testApproval.getPatientAmount());
        
        testApproval.setStatus("PENDING");
        assertEquals("PENDING", testApproval.getStatus());
        
        testApproval.setApprovalDate(newApprovalDate);
        assertEquals(newApprovalDate, testApproval.getApprovalDate());
        
        testApproval.setCompletedDate(newCompletedDate);
        assertEquals(newCompletedDate, testApproval.getCompletedDate());
        
        testApproval.setRejectionReason("Waiting for documents");
        assertEquals("Waiting for documents", testApproval.getRejectionReason());
        
        testApproval.setPrescriptionId(31L);
        assertEquals(31L, testApproval.getPrescriptionId());
        
        testApproval.setPrescriptionTotal(55.0);
        assertEquals(55.0, testApproval.getPrescriptionTotal());
    }
    
    // Add equals/hashCode tests if implemented
} 