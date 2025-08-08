package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Entidad que representa una prescripción médica.
 * Mapea a la tabla "PRESCRIPTION". Asocia una prescripción a un hospital y un usuario,
 * e indica si está aprobada.
 */
@Entity
@Table(name = "PRESCRIPTION")
public class Prescription {

    /** Identificador único de la prescripción. Generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRESCRIPTION")
    private Long idPrescription;

    /** Hospital que emitió la prescripción. */
    @ManyToOne
    @JoinColumn(name = "ID_HOSPITAL", referencedColumnName = "ID_HOSPITAL")
    private Hospital hospital;

    /** Usuario (paciente) al que pertenece la prescripción. */
    @ManyToOne
    @JoinColumn(name = "ID_USER", referencedColumnName = "ID_USER")
    private User user;

    /** Estado de aprobación de la prescripción ('1' aprobado, '0' pendiente/rechazado). */
    @Column(name = "APPROVED")
    private Character approved;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Prescription() {
    }

    /**
     * Constructor para crear una nueva prescripción.
     *
     * @param hospital El hospital emisor.
     * @param user El usuario (paciente).
     * @param approved El estado de aprobación.
     */
    public Prescription(Hospital hospital, User user, Character approved) {
        this.hospital = hospital;
        this.user = user;
        this.approved = approved;
    }

    // Getters y setters
    public Long getIdPrescription() {
        return idPrescription;
    }

    public void setIdPrescription(Long idPrescription) {
        this.idPrescription = idPrescription;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Character getApproved() {
        return approved;
    }

    public void setApproved(Character approved) {
        this.approved = approved;
    }

    /**
     * Calcula el costo total de la prescripción.
     * **Nota:** Esta implementación es un placeholder.
     * La lógica real debería sumar los costos de los medicamentos asociados
     * (a través de la entidad PrescriptionMedicine).
     * 
     * @return El costo total calculado (actualmente un valor fijo de 1000.0).
     */
    public double calculateTotal() {
        // In a real implementation, this would calculate based on medication items associated with the prescription
        // For example: return prescriptionItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        
        // Placeholder implementation - returns a fixed value
        return 1000.0;
    }
}
