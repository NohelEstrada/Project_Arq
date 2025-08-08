package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Representa una entidad que registra cuándo se realizó una cita, mapeada a la tabla APPOINTMENT_MADE.
 * Parece almacenar una referencia a una cita y al usuario asociado, junto con la fecha en que se efectuó.
 */
@Entity
@Table(name = "APPOINTMENT_MADE")
public class AppointmentMade {
    /**
     * Identificador de la cita realizada. Parece ser una clave primaria que también podría ser foránea.
     * No se autogenera.
     */
    @Id
    @Column(name = "ID_CITA", nullable = false)
    private Long idCita;

    /**
     * Identificador del usuario asociado a la cita realizada.
     * No puede ser nulo.
     */
    @Column(name = "ID_USER", nullable = false)
    private Long idUser;

    /**
     * Fecha en que se realizó la cita.
     * Se almacena solo la fecha. No puede ser nulo.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "APPOINTMENT_MADE_DATE", nullable = false)
    private Date appointmentMadeDate;

    /**
     * Constructor por defecto para la entidad AppointmentMade.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public AppointmentMade() {}

    // Getters y Setters
    /**
     * Obtiene el ID de la cita realizada.
     * @return el ID de la cita.
     */
    public Long getIdCita() { return idCita; }
    /**
     * Establece el ID de la cita realizada.
     * @param idCita el ID a establecer.
     */
    public void setIdCita(Long idCita) { this.idCita = idCita; }

    /**
     * Obtiene el ID del usuario asociado.
     * @return el ID del usuario.
     */
    public Long getIdUser() { return idUser; }
    /**
     * Establece el ID del usuario asociado.
     * @param idUser el ID a establecer.
     */
    public void setIdUser(Long idUser) { this.idUser = idUser; }

    /**
     * Obtiene la fecha en que se realizó la cita.
     * @return la fecha de realización.
     */
    public Date getAppointmentMadeDate() { return appointmentMadeDate; }
    /**
     * Establece la fecha en que se realizó la cita.
     * @param appointmentMadeDate la fecha a establecer.
     */
    public void setAppointmentMadeDate(Date appointmentMadeDate) { this.appointmentMadeDate = appointmentMadeDate; }
}
