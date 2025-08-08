package com.sources.app.entities;

import java.io.Serializable;
import java.util.Objects;

public class MedicinePresId implements Serializable {
    private Long prescription; // Corresponde al id de Prescription
    private Long medicine;     // Corresponde al id de Medicine

    // Constructor por defecto
    public MedicinePresId() {}

    public MedicinePresId(Long prescription, Long medicine) {
        this.prescription = prescription;
        this.medicine = medicine;
    }

    // equals() y hashCode() son obligatorios
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicinePresId)) return false;
        MedicinePresId that = (MedicinePresId) o;
        return Objects.equals(prescription, that.prescription) &&
                Objects.equals(medicine, that.medicine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prescription, medicine);
    }
}
