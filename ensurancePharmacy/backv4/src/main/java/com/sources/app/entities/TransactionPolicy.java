package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;
import java.math.BigDecimal;

/**
 * Representa una entidad de transacción relacionada con una póliza, mapeada a la tabla TRANSACTION_POLICY.
 * Almacena detalles de pagos o transacciones vinculadas a pólizas de seguro y usuarios.
 */
@Entity
@Table(name = "TRANSACTION_POLICY")
public class TransactionPolicy {

    /**
     * Identificador único de la transacción de póliza.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TRANSACTION_POLICY")
    private Long idTransactionPolicy;

    /**
     * Póliza asociada a la transacción.
     * Relación Many-to-One obligatoria con la entidad Policy.
     */
    // Relación ManyToOne con Policy
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_POLICY", nullable = false)
    private Policy policy;

    /**
     * Usuario asociado a la transacción.
     * Relación Many-to-One obligatoria con la entidad User.
     */
    // Relación ManyToOne con User
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    /**
     * Fecha en que se realizó el pago o la transacción.
     * Se almacena solo la fecha. No puede ser nulo.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "PAY_DATE", nullable = false)
    private Date payDate;

    /**
     * Monto total de la transacción.
     * Se almacena con precisión decimal (10, 2). No puede ser nulo.
     */
    @Column(name = "TOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * Constructor por defecto para la entidad TransactionPolicy.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public TransactionPolicy() {}

    // Getters y Setters
    /**
     * Obtiene el ID de la transacción de póliza.
     * @return el ID de la transacción.
     */
    public Long getIdTransactionPolicy() {
        return idTransactionPolicy;
    }
    /**
     * Establece el ID de la transacción de póliza.
     * @param idTransactionPolicy el ID a establecer.
     */
    public void setIdTransactionPolicy(Long idTransactionPolicy) {
        this.idTransactionPolicy = idTransactionPolicy;
    }

    /**
     * Obtiene la póliza asociada.
     * @return la entidad Policy.
     */
    public Policy getPolicy() {
        return policy;
    }
    /**
     * Establece la póliza asociada.
     * @param policy la entidad Policy a establecer.
     */
    public void setPolicy(Policy policy) {
        this.policy = policy;
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
     * Obtiene la fecha de pago.
     * @return la fecha de pago.
     */
    public Date getPayDate() {
        return payDate;
    }
    /**
     * Establece la fecha de pago.
     * @param payDate la fecha a establecer.
     */
    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    /**
     * Obtiene el monto total de la transacción.
     * @return el monto total.
     */
    public BigDecimal getTotal() {
        return total;
    }
    /**
     * Establece el monto total de la transacción.
     * @param total el monto total a establecer.
     */
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
