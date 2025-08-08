package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Representa la relación entre una prescripción y un medicamento, mapeada a la tabla MEDICINE_PRES.
 * Utiliza una clave primaria compuesta definida en {@link MedicinePresId}.
 * Esta entidad actúa como una tabla de unión para una relación Many-to-Many entre Prescription y Medicine.
 */
@Entity
@Table(name = "MEDICINE_PRES")
@IdClass(MedicinePresId.class)
public class MedicinePres {

    /**
     * Parte de la clave primaria compuesta, representa la prescripción.
     * Relación Many-to-One obligatoria con la entidad Prescription.
     */
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_PRESCRIPTION", nullable = false)
    private Prescription prescription;

    /**
     * Parte de la clave primaria compuesta, representa el medicamento.
     * Relación Many-to-One obligatoria con la entidad Medicine.
     */
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_MEDICINE", nullable = false)
    private Medicine medicine;

    /**
     * Constructor por defecto para la entidad MedicinePres.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public MedicinePres() {}

    // Getters y Setters
    /**
     * Obtiene la prescripción asociada.
     * @return la entidad Prescription.
     */
    public Prescription getPrescription() {
        return prescription;
    }

    /**
     * Establece la prescripción asociada.
     * @param prescription la entidad Prescription a establecer.
     */
    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    /**
     * Obtiene el medicamento asociado.
     * @return la entidad Medicine.
     */
    public Medicine getMedicine() {
        return medicine;
    }

    /**
     * Establece el medicamento asociado.
     * @param medicine la entidad Medicine a establecer.
     */
    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }
}
