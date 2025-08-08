package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Representa una entidad de transacción mapeada a la tabla TRANSACTIONS.
 * Almacena información sobre transacciones financieras relacionadas con usuarios y hospitales.
 */
@Entity
@Table(name = "TRANSACTIONS")
public class Transactions {

    /**
     * Identificador único de la transacción.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TRANSACTION")
    private Long idTransaction;

    /**
     * Usuario asociado a la transacción.
     * Relación Many-to-One obligatoria con la entidad User.
     */
    // Relación ManyToOne con User (ID_USER es llave foránea)
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    /**
     * Hospital asociado a la transacción.
     * Relación Many-to-One obligatoria con la entidad Hospital.
     */
    // Relación ManyToOne con Hospital (ID_HOSPITAL es llave foránea)
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_HOSPITAL", nullable = false)
    private Hospital hospital;

    /**
     * Fecha en que ocurrió la transacción.
     * Se almacena solo la fecha. No puede ser nulo.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "TRANS_DATE", nullable = false)
    private Date transDate;

    /**
     * Monto total de la transacción.
     * No puede ser nulo.
     */
    @Column(name = "TOTAL", nullable = false)
    private Double total;

    /**
     * Monto del copago asociado a la transacción.
     * No puede ser nulo.
     */
    @Column(name = "COPAY", nullable = false)
    private Double copay;

    /**
     * Comentario o descripción de la transacción.
     * No puede ser nulo y tiene longitud máxima de 255 caracteres.
     */
    @Column(name = "TRANSACTION_COMMENT", nullable = false, length = 255)
    private String transactionComment;

    /**
     * Resultado o estado de la transacción.
     * No puede ser nulo y tiene longitud máxima de 100 caracteres.
     */
    @Column(name = "RESULT", nullable = false, length = 100)
    private String result;

    /**
     * Indica si la transacción está cubierta por seguro.
     * Típicamente 1 para cubierto, 0 para no cubierto. No puede ser nulo.
     */
    @Column(name = "COVERED", nullable = false)
    private Integer covered;

    /**
     * Código o número de autorización de la transacción.
     * No puede ser nulo y tiene longitud máxima de 100 caracteres.
     */
    @Column(name = "AUTH", nullable = false, length = 100)
    private String auth;

    /**
     * Constructor por defecto para la entidad Transactions.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public Transactions() {}

    // Getters y Setters
    /**
     * Obtiene el ID de la transacción.
     * @return el ID de la transacción.
     */
    public Long getIdTransaction() {
        return idTransaction;
    }
    /**
     * Establece el ID de la transacción.
     * @param idTransaction el ID a establecer.
     */
    public void setIdTransaction(Long idTransaction) {
        this.idTransaction = idTransaction;
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
     * Obtiene la fecha de la transacción.
     * @return la fecha de la transacción.
     */
    public Date getTransDate() {
        return transDate;
    }
    /**
     * Establece la fecha de la transacción.
     * @param transDate la fecha a establecer.
     */
    public void setTransDate(Date transDate) {
        this.transDate = transDate;
    }

    /**
     * Obtiene el monto total.
     * @return el monto total.
     */
    public Double getTotal() {
        return total;
    }
    /**
     * Establece el monto total.
     * @param total el monto total a establecer.
     */
    public void setTotal(Double total) {
        this.total = total;
    }

    /**
     * Obtiene el monto del copago.
     * @return el monto del copago.
     */
    public Double getCopay() {
        return copay;
    }
    /**
     * Establece el monto del copago.
     * @param copay el monto del copago a establecer.
     */
    public void setCopay(Double copay) {
        this.copay = copay;
    }

    /**
     * Obtiene el comentario de la transacción.
     * @return el comentario.
     */
    public String getTransactionComment() {
        return transactionComment;
    }
    /**
     * Establece el comentario de la transacción.
     * @param transactionComment el comentario a establecer.
     */
    public void setTransactionComment(String transactionComment) {
        this.transactionComment = transactionComment;
    }

    /**
     * Obtiene el resultado de la transacción.
     * @return el resultado.
     */
    public String getResult() {
        return result;
    }
    /**
     * Establece el resultado de la transacción.
     * @param result el resultado a establecer.
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * Obtiene el estado de cobertura.
     * @return el estado de cobertura (1 o 0).
     */
    public Integer getCovered() {
        return covered;
    }
    /**
     * Establece el estado de cobertura.
     * @param covered el estado a establecer.
     */
    public void setCovered(Integer covered) {
        this.covered = covered;
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
