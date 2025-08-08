package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Entidad de enlace que representa un ítem de medicamento dentro de un pedido (Order).
 * Mapea a la tabla "ORDERS_MEDICINE" y utiliza una clave compuesta {@link OrderMedicineId}.
 */
@Entity
@Table(name = "ORDERS_MEDICINE")
public class OrderMedicine {

    /** Clave primaria compuesta embebida (ID de pedido y ID de medicamento). */
    @EmbeddedId
    private OrderMedicineId id;

    /** Pedido al que pertenece este ítem. Parte de la clave compuesta. */
    @ManyToOne
    @MapsId("orderId") // Mapea el atributo 'orderId' de OrderMedicineId
    @JoinColumn(name = "ID_ORDER", referencedColumnName = "ID_ORDER")
    private Orders orders;

    /** Medicamento incluido en el pedido. Parte de la clave compuesta. */
    @ManyToOne
    @MapsId("medicineId") // Mapea el atributo 'medicineId' de OrderMedicineId
    @JoinColumn(name = "ID_MEDICINE", referencedColumnName = "ID_MEDICINE")
    private Medicine medicine;

    /** Cantidad del medicamento incluida en el pedido. */
    @Column(name = "QUANTITY")
    private Integer quantity;

    /** Costo unitario del medicamento en este pedido. */
    @Column(name = "COST")
    private Double cost;

    /** Costo total para este ítem (cantidad * costo). Como String, revisar necesidad. */
    @Column(name = "TOTAL")
    private String total; // TODO: Considerar si este campo es redundante o debería ser Double

    /**
     * Constructor por defecto requerido por JPA.
     */
    public OrderMedicine() {
    }

    /**
     * Constructor para crear un nuevo ítem de pedido-medicamento.
     * Inicializa la clave compuesta a partir de las entidades Orders y Medicine.
     *
     * @param orders El pedido asociado.
     * @param medicine El medicamento asociado.
     * @param quantity La cantidad del medicamento.
     * @param cost El costo unitario.
     * @param total El costo total para este ítem (String).
     */
    public OrderMedicine(Orders orders, Medicine medicine, Integer quantity, Double cost, String total) {
        this.orders = orders;
        this.medicine = medicine;
        this.quantity = quantity;
        this.cost = cost;
        this.total = total;
        this.id = new OrderMedicineId(orders.getIdOrder(), medicine.getIdMedicine());
    }

    public OrderMedicineId getId() {
        return id;
    }

    public void setId(OrderMedicineId id) {
        this.id = id;
    }

    public Orders getOrders() {
        return orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
        if (this.id == null) {
            this.id = new OrderMedicineId();
        }
        this.id.setOrderId(orders.getIdOrder());
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
        if (this.id == null) {
            this.id = new OrderMedicineId();
        }
        this.id.setMedicineId(medicine.getIdMedicine());
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
