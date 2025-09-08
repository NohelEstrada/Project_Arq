package com.sources.app.entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Entidad de enlace que representa un ítem de medicamento dentro de una factura.
 * Mapea a la tabla "BILL_MEDICINE" y utiliza una clave compuesta {@link BillMedicineId}.
 */
@Entity
@Table(name = "BILL_MEDICINE")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillMedicine {

    /** Clave primaria compuesta embebida (ID de factura y ID de medicamento). */
    @EmbeddedId
    private BillMedicineId id;

    /** Factura a la que pertenece este ítem. Parte de la clave compuesta. */
    @ManyToOne
    @MapsId("billId") // Mapea el atributo 'billId' de BillMedicineId
    @JoinColumn(name = "ID_BILL", referencedColumnName = "ID_BILL")
    private Bill bill;

    /** Medicamento incluido en la factura. Parte de la clave compuesta. */
    @ManyToOne
    @MapsId("medicineId") // Mapea el atributo 'medicineId' de BillMedicineId
    @JoinColumn(name = "ID_MEDICINE", referencedColumnName = "ID_MEDICINE")
    private Medicine medicine;

    /** Cantidad del medicamento incluida en la factura. */
    @Column(name = "QUANTITY")
    private Integer quantity;

    /** Costo unitario del medicamento en esta factura. */
    @Column(name = "COST")
    private Double cost;

    /** Copago específico para este medicamento en esta factura (si aplica). */
    @Column(name = "COPAY")
    private Double copay;

    /** Costo total para este ítem (cantidad * costo). Como String, revisar necesidad. */
    @Column(name = "TOTAL")
    private String total; // TODO: Considerar si este campo es redundante o debería ser Double

    /**
     * Constructor por defecto requerido por JPA.
     */
    public BillMedicine() {
    }

    /**
     * Constructor para crear un nuevo ítem de factura-medicamento.
     * Inicializa la clave compuesta a partir de las entidades Bill y Medicine.
     *
     * @param bill La factura asociada.
     * @param medicine El medicamento asociado.
     * @param quantity La cantidad del medicamento.
     * @param cost El costo unitario.
     * @param copay El copago (si aplica).
     * @param total El costo total para este ítem (String).
     */
    public BillMedicine(Bill bill, Medicine medicine, Integer quantity, Double cost, Double copay, String total) {
        this.bill = bill;
        this.medicine = medicine;
        this.quantity = quantity;
        this.cost = cost;
        this.copay = copay;
        this.total = total;
        // Inicializamos la clave compuesta
        this.id = new BillMedicineId(bill.getIdBill(), medicine.getIdMedicine());
    }

    public BillMedicineId getId() {
        return id;
    }

    public void setId(BillMedicineId id) {
        this.id = id;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
        if (bill != null) {
            if (id == null) {
                id = new BillMedicineId();
            }
            id.setBillId(bill.getIdBill());
        }
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
        if (medicine != null) {
            if (id == null) {
                id = new BillMedicineId();
            }
            id.setMedicineId(medicine.getIdMedicine());
        }
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

    public Double getCopay() {
        return copay;
    }

    public void setCopay(Double copay) {
        this.copay = copay;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
