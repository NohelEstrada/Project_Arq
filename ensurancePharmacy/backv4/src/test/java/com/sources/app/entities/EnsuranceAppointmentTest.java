package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

class EnsuranceAppointmentTest {

    private EnsuranceAppointment appointment;
    private final Long id = 1L;
    private final String hospitalAppId = "HOSP_APP_123";
    private final Long userId = 5L;
    private final Date appDate = new Date();
    private final String doctorName = "Dr. House";
    private final String reason = "Consultation";

    @BeforeEach
    void setUp() {
        appointment = new EnsuranceAppointment();
        // Set fields manually as there's no full constructor
        appointment.setIdAppointment(id);
        appointment.setHospitalAppointmentId(hospitalAppId);
        appointment.setIdUser(userId);
        appointment.setAppointmentDate(appDate);
        appointment.setDoctorName(doctorName);
        appointment.setReason(reason);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        EnsuranceAppointment newApp = new EnsuranceAppointment();
        assertNull(newApp.getIdAppointment());
        assertNull(newApp.getHospitalAppointmentId());
        assertNull(newApp.getIdUser());
        assertNull(newApp.getAppointmentDate());
        assertNull(newApp.getDoctorName());
        assertNull(newApp.getReason());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, appointment.getIdAppointment());
        assertEquals(hospitalAppId, appointment.getHospitalAppointmentId());
        assertEquals(userId, appointment.getIdUser());
        assertEquals(appDate, appointment.getAppointmentDate());
        assertEquals(doctorName, appointment.getDoctorName());
        assertEquals(reason, appointment.getReason());

        // Test setters with new values
        EnsuranceAppointment testApp = new EnsuranceAppointment();
        Date newDate = new Date(System.currentTimeMillis() + 50000);

        testApp.setIdAppointment(2L);
        assertEquals(2L, testApp.getIdAppointment());

        testApp.setHospitalAppointmentId("HOSP_APP_456");
        assertEquals("HOSP_APP_456", testApp.getHospitalAppointmentId());

        testApp.setIdUser(6L);
        assertEquals(6L, testApp.getIdUser());

        testApp.setAppointmentDate(newDate);
        assertEquals(newDate, testApp.getAppointmentDate());

        testApp.setDoctorName("Dr. Wilson");
        assertEquals("Dr. Wilson", testApp.getDoctorName());

        testApp.setReason("Follow-up");
        assertEquals("Follow-up", testApp.getReason());
    }
    
    // Add equals/hashCode tests if implemented
} 