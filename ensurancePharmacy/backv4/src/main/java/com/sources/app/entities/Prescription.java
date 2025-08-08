package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;
import java.math.BigDecimal;

/**
 * Representa una entidad de prescripción médica mapeada a la tabla PRESCRIPTION.
 * Contiene detalles sobre prescripciones, incluyendo relaciones con hospital, usuario, medicamento y farmacia.
 */
@Entity
@Table(name = "PRESCRIPTION")
public class Prescription {
    /**
     * Identificador único de la prescripción.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRESCRIPTION")
    private Long idPrescription;

    /**
     * Hospital donde se emitió la prescripción.
     * Relación Many-to-One obligatoria con la entidad Hospital.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_HOSPITAL", nullable = false)
    private Hospital hospital;

    /**
     * Usuario para quien es la prescripción.
     * Relación Many-to-One obligatoria con la entidad User.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    /**
     * Medicamento prescrito.
     * Relación Many-to-One obligatoria con la entidad Medicine.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_MEDICINE", nullable = false)
    private Medicine medicine;

    /**
     * Farmacia donde se puede surtir la prescripción.
     * Relación Many-to-One obligatoria con la entidad Pharmacy.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_PHARMACY", nullable = false)
    private Pharmacy pharmacy;

    /**
     * Fecha en que se emitió la prescripción.
     * Se almacena solo la fecha. No puede ser nulo.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "PRESCRIPTION_DATE", nullable = false)
    private Date prescriptionDate;

    /**
     * Costo total de la prescripción.
     * Se almacena con precisión decimal (10, 2). No puede ser nulo.
     */
    @Column(name = "TOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * Monto del copago requerido para la prescripción.
     * Se almacena con precisión decimal (10, 2). No puede ser nulo.
     */
    @Column(name = "COPAY", nullable = false, precision = 10, scale = 2)
    private BigDecimal copay;

    /**
     * Comentarios o notas adicionales sobre la prescripción.
     * No puede ser nulo y tiene una longitud máxima de 255 caracteres.
     */
    @Column(name = "PRESCRIPTION_COMMENT", nullable = false, length = 255)
    private String prescriptionComment;

    /**
     * Indica si la prescripción está asegurada (cubierta por seguro).
     * Típicamente 1 para asegurado, 0 para no asegurado. No puede ser nulo.
     */
    @Column(name = "SECURED", nullable = false)
    private Integer secured;

    /**
     * Código o número de autorización para la prescripción.
     * No puede ser nulo y tiene una longitud máxima de 100 caracteres.
     */
    @Column(name = "AUTH", nullable = false, length = 100)
    private String auth;

    /**
     * Constructor por defecto para la entidad Prescription.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public Prescription() {}

    // Getters y Setters
    /**
     * Obtiene el ID de la prescripción.
     * @return el ID de la prescripción.
     */
    public Long getIdPrescription() {
        return idPrescription;
    }
    /**
     * Establece el ID de la prescripción.
     * @param idPrescription el ID a establecer.
     */
    public void setIdPrescription(Long idPrescription) {
        this.idPrescription = idPrescription;
    }

    /**
     * Obtiene el hospital asociado.
     * @return la entidad Hospital.
     */
    public Hospital getHospital() {
        return hospital;
    }
    /**
     * Establece el hospital asociado.
     * @param hospital la entidad Hospital a establecer.
     */
    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    /**
     * Obtiene el usuario asociado.
     * @return la entidad User.
     */
    public User getUser() {
        return user;
    }
    /**
     * Establece el usuario asociado.
     * @param user la entidad User a establecer.
     */
    public void setUser(User user) {
        this.user = user;
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

    /**
     * Obtiene la farmacia asociada.
     * @return la entidad Pharmacy.
     */
    public Pharmacy getPharmacy() {
        return pharmacy;
    }
    /**
     * Establece la farmacia asociada.
     * @param pharmacy la entidad Pharmacy a establecer.
     */
    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
    }

    /**
     * Obtiene la fecha de la prescripción.
     * @return la fecha de prescripción.
     */
    public Date getPrescriptionDate() {
        return prescriptionDate;
    }
    /**
     * Establece la fecha de la prescripción.
     * @param prescriptionDate la fecha a establecer.
     */
    public void setPrescriptionDate(Date prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    /**
     * Obtiene el costo total de la prescripción.
     * @return el costo total.
     */
    public BigDecimal getTotal() {
        return total;
    }
    /**
     * Establece el costo total de la prescripción.
     * @param total el costo total a establecer.
     */
    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    /**
     * Obtiene el monto del copago.
     * @return el monto del copago.
     */
    public BigDecimal getCopay() {
        return copay;
    }
    /**
     * Establece el monto del copago.
     * @param copay el monto del copago a establecer.
     */
    public void setCopay(BigDecimal copay) {
        this.copay = copay;
    }

    /**
     * Obtiene los comentarios de la prescripción.
     * @return los comentarios.
     */
    public String getPrescriptionComment() {
        return prescriptionComment;
    }
    /**
     * Establece los comentarios de la prescripción.
     * @param prescriptionComment los comentarios a establecer.
     */
    public void setPrescriptionComment(String prescriptionComment) {
        this.prescriptionComment = prescriptionComment;
    }

    /**
     * Obtiene el estado de asegurado de la prescripción.
     * @return el estado de asegurado (1 o 0).
     */
    public Integer getSecured() {
        return secured;
    }
    /**
     * Establece el estado de asegurado de la prescripción.
     * @param secured el estado a establecer.
     */
    public void setSecured(Integer secured) {
        this.secured = secured;
    }

    /**
     * Obtiene el código de autorización.
     * @return el código de autorización.
     */
    public String getAuth() {
        return auth;
    }
    /**
     * Establece el código de autorización.
     * @param auth el código a establecer.
     */
    public void setAuth(String auth) {
        this.auth = auth;
    }
}
