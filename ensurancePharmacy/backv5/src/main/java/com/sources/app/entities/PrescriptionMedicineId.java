package com.sources.app.entities;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Clase que representa la clave primaria compuesta para la entidad {@link PrescriptionMedicine}.
 * Contiene los identificadores de la prescripción (Prescription) y del medicamento (Medicine).
 * Debe implementar Serializable y sobrescribir equals() y hashCode().
 */
@Embeddable
public class PrescriptionMedicineId implements Serializable {

    /** ID de la prescripción (Prescription). */
    @Column(name = "ID_PRESCRIPTION")
    private Long prescriptionId;

    /** ID del medicamento (Medicine). */
    @Column(name = "ID_MEDICINE")
    private Long medicineId;

    /**
     * Constructor por defecto requerido.
     */
    public PrescriptionMedicineId() {
    }

    /**
     * Constructor con los IDs de la clave compuesta.
     *
     * @param prescriptionId El ID de la prescripción.
     * @param medicineId El ID del medicamento.
     */
    public PrescriptionMedicineId(Long prescriptionId, Long medicineId) {
        this.prescriptionId = prescriptionId;
        this.medicineId = medicineId;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrescriptionMedicineId)) return false;
        PrescriptionMedicineId that = (PrescriptionMedicineId) o;
        return Objects.equals(getPrescriptionId(), that.getPrescriptionId()) &&
                Objects.equals(getMedicineId(), that.getMedicineId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPrescriptionId(), getMedicineId());
    }
}
