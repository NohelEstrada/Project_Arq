package com.sources.app.entities;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Clase que representa la clave primaria compuesta para la entidad {@link BillMedicine}.
 * Contiene los identificadores de la factura (Bill) y del medicamento (Medicine).
 * Debe implementar Serializable y sobrescribir equals() y hashCode().
 */
@Embeddable
public class BillMedicineId implements Serializable {

    /** ID de la factura (Bill). */
    @Column(name = "ID_BILL")
    private Long billId;

    /** ID del medicamento (Medicine). */
    @Column(name = "ID_MEDICINE")
    private Long medicineId;

    /**
     * Constructor por defecto requerido.
     */
    public BillMedicineId() {
    }

    /**
     * Constructor con los IDs de la clave compuesta.
     *
     * @param billId El ID de la factura.
     * @param medicineId El ID del medicamento.
     */
    public BillMedicineId(Long billId, Long medicineId) {
        this.billId = billId;
        this.medicineId = medicineId;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
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
        if (!(o instanceof BillMedicineId)) return false;
        BillMedicineId that = (BillMedicineId) o;
        return Objects.equals(billId, that.billId) &&
                Objects.equals(medicineId, that.medicineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId, medicineId);
    }
}
