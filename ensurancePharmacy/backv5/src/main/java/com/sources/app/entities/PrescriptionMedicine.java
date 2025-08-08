package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Entidad de enlace que representa un medicamento específico dentro de una prescripción.
 * Mapea a la tabla "PRESCRIPTION_MEDICINE" y utiliza una clave compuesta {@link PrescriptionMedicineId}.
 * Contiene detalles como dosis, frecuencia y duración.
 */
@Entity
@Table(name = "PRESCRIPTION_MEDICINE")
public class PrescriptionMedicine {

    /** Clave primaria compuesta embebida (ID de prescripción y ID de medicamento). */
    @EmbeddedId
    private PrescriptionMedicineId id;

    /** Prescripción a la que pertenece este ítem. Parte de la clave compuesta. */
    @ManyToOne
    @MapsId("prescriptionId") // Mapea el atributo 'prescriptionId' de PrescriptionMedicineId
    @JoinColumn(name = "ID_PRESCRIPTION", referencedColumnName = "ID_PRESCRIPTION")
    private Prescription prescription;

    /** Medicamento incluido en la prescripción. Parte de la clave compuesta. */
    @ManyToOne
    @MapsId("medicineId") // Mapea el atributo 'medicineId' de PrescriptionMedicineId
    @JoinColumn(name = "ID_MEDICINE", referencedColumnName = "ID_MEDICINE")
    private Medicine medicine;

    /** Dosis prescrita del medicamento. */
    @Column(name = "DOSIS")
    private Double dosis;

    /** Frecuencia de administración prescrita. */
    @Column(name = "FRECUENCIA")
    private Double frecuencia;

    /** Duración del tratamiento prescrito. */
    @Column(name = "DURACION")
    private Double duracion;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public PrescriptionMedicine() {
    }

    /**
     * Constructor para crear un nuevo ítem de prescripción-medicamento.
     * Inicializa la clave compuesta a partir de las entidades Prescription y Medicine.
     *
     * @param prescription La prescripción asociada.
     * @param medicine El medicamento asociado.
     * @param dosis La dosis prescrita.
     * @param frecuencia La frecuencia de administración.
     * @param duracion La duración del tratamiento.
     */
    public PrescriptionMedicine(Prescription prescription, Medicine medicine, Double dosis, Double frecuencia, Double duracion) {
        this.prescription = prescription;
        this.medicine = medicine;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.duracion = duracion;
        this.id = new PrescriptionMedicineId(prescription.getIdPrescription(), medicine.getIdMedicine());
    }

    // Getters y setters
    public PrescriptionMedicineId getId() {
        return id;
    }

    public void setId(PrescriptionMedicineId id) {
        this.id = id;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
        if (id == null) {
            id = new PrescriptionMedicineId();
        }
        id.setPrescriptionId(prescription.getIdPrescription());
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
        if (id == null) {
            id = new PrescriptionMedicineId();
        }
        id.setMedicineId(medicine.getIdMedicine());
    }

    public Double getDosis() {
        return dosis;
    }

    public void setDosis(Double dosis) {
        this.dosis = dosis;
    }

    public Double getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Double frecuencia) {
        this.frecuencia = frecuencia;
    }

    public Double getDuracion() {
        return duracion;
    }

    public void setDuracion(Double duracion) {
        this.duracion = duracion;
    }
}
