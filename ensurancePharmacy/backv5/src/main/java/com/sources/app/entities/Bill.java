package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Entidad que representa una factura en la base de datos.
 * Mapea a la tabla "BILL". Contiene detalles de costos, la prescripción asociada,
 * información de cobertura del seguro y estado.
 */
@Entity
@Table(name = "BILL")
public class Bill {

    /** Identificador único de la factura. Generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_BILL")
    private Long idBill;

    /** Prescripción asociada a esta factura. */
    @ManyToOne
    @JoinColumn(name = "ID_PRESCRIPTION", referencedColumnName = "ID_PRESCRIPTION")
    private Prescription prescription;

    /** Monto de impuestos aplicados a la factura. */
    @Column(name = "TAXES")
    private Double taxes;

    /** Monto de la factura antes de impuestos. */
    @Column(name = "SUBTOTAL")
    private Double subtotal;

    /** Monto de copago aplicado (si corresponde). */
    @Column(name = "COPAY")
    private Double copay;

    /** Monto total de la factura (como String, revisar si es necesario). */
    @Column(name = "TOTAL")
    private String total;

    /** Monto total calculado de la factura (posiblemente redundante con 'total'). */
    @Column(name = "TOTAL_AMOUNT")
    private Double totalAmount;

    /** Monto cubierto por el seguro. */
    @Column(name = "COVERED_AMOUNT")
    private Double coveredAmount;

    /** Monto a pagar por el paciente. */
    @Column(name = "PATIENT_AMOUNT")
    private Double patientAmount;

    /** Código de aprobación proporcionado por el seguro. */
    @Column(name = "INSURANCE_APPROVAL_CODE")
    private String insuranceApprovalCode;

    /** Fecha y hora de creación de la factura. */
    @Column(name = "CREATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    /** Estado actual de la factura (e.g., "PENDING", "PAID", "CANCELLED"). */
    @Column(name = "STATUS")
    private String status;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Bill() {
    }

    /**
     * Constructor con campos esenciales para crear una factura.
     * Otros campos como totalAmount, coveredAmount, etc., se establecerían después.
     *
     * @param prescription La prescripción asociada.
     * @param taxes Los impuestos.
     * @param subtotal El subtotal.
     * @param copay El copago.
     * @param total El monto total (String).
     */
    public Bill(Prescription prescription, Double taxes, Double subtotal, Double copay, String total) {
        this.prescription = prescription;
        this.taxes = taxes;
        this.subtotal = subtotal;
        this.copay = copay;
        this.total = total;
    }

    // Getters y setters
    public Long getIdBill() {
        return idBill;
    }

    public void setIdBill(Long idBill) {
        this.idBill = idBill;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public Double getTaxes() {
        return taxes;
    }

    public void setTaxes(Double taxes) {
        this.taxes = taxes;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
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

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getCoveredAmount() {
        return coveredAmount;
    }

    public void setCoveredAmount(Double coveredAmount) {
        this.coveredAmount = coveredAmount;
    }

    public Double getPatientAmount() {
        return patientAmount;
    }

    public void setPatientAmount(Double patientAmount) {
        this.patientAmount = patientAmount;
    }

    public String getInsuranceApprovalCode() {
        return insuranceApprovalCode;
    }

    public void setInsuranceApprovalCode(String insuranceApprovalCode) {
        this.insuranceApprovalCode = insuranceApprovalCode;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return idBill;
    }
}
