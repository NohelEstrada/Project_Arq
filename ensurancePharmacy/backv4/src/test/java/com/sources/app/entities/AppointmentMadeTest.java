package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

class AppointmentMadeTest {

    private AppointmentMade appointmentMade;
    private final Long citaId = 1L;
    private final Long userId = 2L;
    private final Date madeDate = new Date();

    @BeforeEach
    void setUp() {
        appointmentMade = new AppointmentMade();
        // Set fields manually
        appointmentMade.setIdCita(citaId);
        appointmentMade.setIdUser(userId);
        appointmentMade.setAppointmentMadeDate(madeDate);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        AppointmentMade newApp = new AppointmentMade();
        assertNull(newApp.getIdCita());
        assertNull(newApp.getIdUser());
        assertNull(newApp.getAppointmentMadeDate());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(citaId, appointmentMade.getIdCita());
        assertEquals(userId, appointmentMade.getIdUser());
        assertEquals(madeDate, appointmentMade.getAppointmentMadeDate());

        // Test setters with new values
        AppointmentMade testApp = new AppointmentMade();
        Date newDate = new Date(System.currentTimeMillis() + 10000);

        testApp.setIdCita(3L);
        assertEquals(3L, testApp.getIdCita());

        testApp.setIdUser(4L);
        assertEquals(4L, testApp.getIdUser());

        testApp.setAppointmentMadeDate(newDate);
        assertEquals(newDate, testApp.getAppointmentMadeDate());
    }
    
    // Add equals/hashCode tests if implemented (especially important if this is used as part of a composite key elsewhere)
} 