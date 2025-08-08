package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Representa una entidad de cita mapeada a la tabla APPOINTMENT.
 * Contiene información sobre citas médicas, incluyendo el hospital, usuario, fecha y estado.
 */
@Entity
@Table(name = "APPOINTMENT")
public class Appointment {
    /**
     * El identificador único para la cita.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_APPOINTMENT")
    private Long idAppointment;

    /**
     * El hospital donde está programada la cita.
     * Este es un campo obligatorio que representa una relación Many-to-One con la entidad Hospital.
     */
    // Relación ManyToOne con Hospital
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_HOSPITAL", nullable = false)
    private Hospital hospital;

    /**
     * El usuario que programó la cita.
     * Este es un campo obligatorio que representa una relación Many-to-One con la entidad User.
     */
    // Relación ManyToOne con User
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    /**
     * La fecha de la cita.
     * Se almacena como un tipo Date y no puede ser nulo.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "APPOINTMENT_DATE", nullable = false)
    private Date appointmentDate;

    /**
     * Indica si la cita está habilitada o activa.
     * Típicamente 1 para habilitado, 0 para deshabilitado. No puede ser nulo.
     */
    @Column(name = "ENABLED", nullable = false)
    private Integer enabled;

    /**
     * Constructor por defecto para la entidad Appointment.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public Appointment() {}

    // Getters y Setters
    /**
     * Obtiene el identificador único para la cita.
     * @return el ID de la cita.
     */
    public Long getIdAppointment() {
        return idAppointment;
    }
    /**
     * Establece el identificador único para la cita.
     * @param idAppointment el ID de la cita a establecer.
     */
    public void setIdAppointment(Long idAppointment) {
        this.idAppointment = idAppointment;
    }

    /**
     * Obtiene el hospital asociado con la cita.
     * @return la entidad Hospital.
     */
    public Hospital getHospital() {
        return hospital;
    }
    /**
     * Establece el hospital asociado con la cita.
     * @param hospital la entidad Hospital a establecer.
     */
    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    /**
     * Obtiene el usuario asociado con la cita.
     * @return la entidad User.
     */
    public User getUser() {
        return user;
    }
    /**
     * Establece el usuario asociado con la cita.
     * @param user la entidad User a establecer.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Obtiene la fecha de la cita.
     * @return la fecha de la cita.
     */
    public Date getAppointmentDate() {
        return appointmentDate;
    }
    /**
     * Establece la fecha de la cita.
     * @param appointmentDate la fecha de la cita a establecer.
     */
    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    /**
     * Obtiene el estado habilitado de la cita.
     * @return el estado habilitado (ej., 1 para habilitado, 0 para deshabilitado).
     */
    public Integer getEnabled() {
        return enabled;
    }
    /**
     * Establece el estado habilitado de la cita.
     * @param enabled el estado habilitado a establecer.
     */
    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}
