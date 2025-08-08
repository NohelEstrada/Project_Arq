package com.sources.app.entities;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Clase que representa la clave primaria compuesta para la entidad {@link OrderMedicine}.
 * Contiene los identificadores del pedido (Orders) y del medicamento (Medicine).
 * Debe implementar Serializable y sobrescribir equals() y hashCode().
 */
@Embeddable
public class OrderMedicineId implements Serializable {

    /** ID del pedido (Orders). */
    @Column(name = "ID_ORDER")
    private Long orderId;

    /** ID del medicamento (Medicine). */
    @Column(name = "ID_MEDICINE")
    private Long medicineId;

    /**
     * Constructor por defecto requerido.
     */
    public OrderMedicineId() {
    }

    /**
     * Constructor con los IDs de la clave compuesta.
     *
     * @param orderId El ID del pedido.
     * @param medicineId El ID del medicamento.
     */
    public OrderMedicineId(Long orderId, Long medicineId) {
        this.orderId = orderId;
        this.medicineId = medicineId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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
        if (!(o instanceof OrderMedicineId)) return false;
        OrderMedicineId that = (OrderMedicineId) o;
        return Objects.equals(getOrderId(), that.getOrderId()) &&
                Objects.equals(getMedicineId(), that.getMedicineId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrderId(), getMedicineId());
    }
}
