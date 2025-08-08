package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Date;

class AppointmentTest {

    private Appointment appointment;
    private final Long id = 1L;
    private final Hospital hospital = mock(Hospital.class);
    private final User user = mock(User.class);
    private final Date appointmentDate = new Date();
    private final Integer enabled = 1;

    @BeforeEach
    void setUp() {
        appointment = new Appointment();
        // Set fields manually
        appointment.setIdAppointment(id);
        appointment.setHospital(hospital);
        appointment.setUser(user);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setEnabled(enabled);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        Appointment newApp = new Appointment();
        assertNull(newApp.getIdAppointment());
        assertNull(newApp.getHospital());
        assertNull(newApp.getUser());
        assertNull(newApp.getAppointmentDate());
        assertNull(newApp.getEnabled());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, appointment.getIdAppointment());
        assertEquals(hospital, appointment.getHospital());
        assertEquals(user, appointment.getUser());
        assertEquals(appointmentDate, appointment.getAppointmentDate());
        assertEquals(enabled, appointment.getEnabled());

        // Test setters with new values
        Appointment testApp = new Appointment();
        Hospital newHospital = mock(Hospital.class);
        User newUser = mock(User.class);
        Date newDate = new Date(System.currentTimeMillis() + 60000);

        testApp.setIdAppointment(2L);
        assertEquals(2L, testApp.getIdAppointment());

        testApp.setHospital(newHospital);
        assertEquals(newHospital, testApp.getHospital());

        testApp.setUser(newUser);
        assertEquals(newUser, testApp.getUser());

        testApp.setAppointmentDate(newDate);
        assertEquals(newDate, testApp.getAppointmentDate());

        testApp.setEnabled(0);
        assertEquals(0, testApp.getEnabled());
    }
    
    // Add equals/hashCode tests if implemented
} 