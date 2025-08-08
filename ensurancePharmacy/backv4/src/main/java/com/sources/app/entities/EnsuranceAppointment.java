package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Entidad para almacenar citas del sistema de seguros mapeada a la tabla ENSURANCE_APPOINTMENT.
 * Esta tabla tendrá una estructura más simple que no depende de la conversión de IDs,
 * almacenando el ID original de la cita del hospital y el ID del usuario del sistema de seguros.
 */
@Entity
@Table(name = "ENSURANCE_APPOINTMENT")
public class EnsuranceAppointment {
    /**
     * Identificador único de la cita en el sistema de seguros.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_APPOINTMENT")
    private Long idAppointment;

    /**
     * ID original de la cita en el sistema del hospital (ej. MongoDB).
     * No puede ser nulo.
     */
    @Column(name = "HOSPITAL_APPOINTMENT_ID", nullable = false)
    private String hospitalAppointmentId;  // ID original de la cita en el hospital (MongoDB)

    /**
     * ID del usuario en el sistema de seguros.
     * No puede ser nulo.
     */
    @Column(name = "ID_USER", nullable = false)
    private Long idUser;  // ID del usuario en el sistema de seguros

    /**
     * Fecha de la cita.
     * Se almacena solo la fecha. No puede ser nulo.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "APPOINTMENT_DATE", nullable = false)
    private Date appointmentDate;  // Fecha de la cita

    /**
     * Nombre del doctor asociado a la cita.
     * Puede ser nulo.
     */
    @Column(name = "DOCTOR_NAME")
    private String doctorName;  // Nombre del doctor

    /**
     * Motivo de la cita.
     * Tiene una longitud máxima de 500 caracteres. Puede ser nulo.
     */
    @Column(name = "APPOINTMENT_REASON", length = 500)
    private String reason;  // Motivo de la cita

    /**
     * Constructor por defecto para la entidad EnsuranceAppointment.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public EnsuranceAppointment() {}

    // Getters y Setters
    /**
     * Obtiene el ID de la cita en el sistema de seguros.
     * @return el ID de la cita.
     */
    public Long getIdAppointment() {
        return idAppointment;
    }

    /**
     * Establece el ID de la cita en el sistema de seguros.
     * @param idAppointment el ID de la cita a establecer.
     */
    public void setIdAppointment(Long idAppointment) {
        this.idAppointment = idAppointment;
    }

    /**
     * Obtiene el ID original de la cita en el sistema del hospital.
     * @return el ID de la cita del hospital.
     */
    public String getHospitalAppointmentId() {
        return hospitalAppointmentId;
    }

    /**
     * Establece el ID original de la cita en el sistema del hospital.
     * @param hospitalAppointmentId el ID de la cita del hospital a establecer.
     */
    public void setHospitalAppointmentId(String hospitalAppointmentId) {
        this.hospitalAppointmentId = hospitalAppointmentId;
    }

    /**
     * Obtiene el ID del usuario en el sistema de seguros.
     * @return el ID del usuario.
     */
    public Long getIdUser() {
        return idUser;
    }

    /**
     * Establece el ID del usuario en el sistema de seguros.
     * @param idUser el ID del usuario a establecer.
     */
    public void setIdUser(Long idUser) {
        this.idUser = idUser;
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
     * Obtiene el nombre del doctor.
     * @return el nombre del doctor.
     */
    public String getDoctorName() {
        return doctorName;
    }

    /**
     * Establece el nombre del doctor.
     * @param doctorName el nombre del doctor a establecer.
     */
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    /**
     * Obtiene el motivo de la cita.
     * @return el motivo de la cita.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Establece el motivo de la cita.
     * @param reason el motivo de la cita a establecer.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
} 