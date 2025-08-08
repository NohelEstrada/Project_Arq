package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Representa una entidad de póliza de seguro mapeada a la tabla POLICY.
 * Contiene información sobre las pólizas, como porcentaje de cobertura, fechas y costo.
 */
@Entity
@Table(name = "POLICY")
public class Policy {

    /**
     * Identificador único de la póliza.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_POLICY")
    private Long idPolicy;

    /**
     * Porcentaje de cobertura que ofrece la póliza.
     * Puede ser nulo.
     */
    @Column(name = "PERCENTAGE")
    private Float percentage;

    /**
     * Fecha de creación de la póliza.
     * Se almacena solo la fecha.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "CREATION_DATE")
    private Date creationDate;

    /**
     * Fecha de expiración de la póliza.
     * Se almacena solo la fecha.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "EXP_DATE")
    private Date expDate;

    /**
     * Costo de la póliza.
     * Puede ser nulo.
     */
    @Column(name = "COST")
    private Float cost;

    /**
     * Indica si la póliza está habilitada o activa.
     * Típicamente 1 para habilitado, 0 para deshabilitado. Puede ser nulo.
     */
    @Column(name = "ENABLED")
    private Integer enabled;

    /**
     * Constructor por defecto para la entidad Policy.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public Policy() {
    }

    /**
     * Constructor con parámetros para crear una instancia de Policy con detalles específicos.
     * @param percentage Porcentaje de cobertura.
     * @param creationDate Fecha de creación.
     * @param expDate Fecha de expiración.
     * @param cost Costo de la póliza.
     * @param enabled Estado de habilitación.
     */
    // Constructor con parámetros
    public Policy(Float percentage, Date creationDate, Date expDate, Float cost, Integer enabled) {
        this.percentage = percentage;
        this.creationDate = creationDate;
        this.expDate = expDate;
        this.cost = cost;
        this.enabled = enabled;
    }

    // Getters y Setters
    /**
     * Obtiene el ID de la póliza.
     * @return el ID de la póliza.
     */
    public Long getIdPolicy() {
        return idPolicy;
    }

    /**
     * Establece el ID de la póliza.
     * @param idPolicy el ID a establecer.
     */
    public void setIdPolicy(Long idPolicy) {
        this.idPolicy = idPolicy;
    }

    /**
     * Obtiene el porcentaje de cobertura.
     * @return el porcentaje de cobertura.
     */
    public Float getPercentage() {
        return percentage;
    }

    /**
     * Establece el porcentaje de cobertura.
     * @param percentage el porcentaje a establecer.
     */
    public void setPercentage(Float percentage) {
        this.percentage = percentage;
    }

    /**
     * Obtiene la fecha de creación.
     * @return la fecha de creación.
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Establece la fecha de creación.
     * @param creationDate la fecha a establecer.
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Obtiene la fecha de expiración.
     * @return la fecha de expiración.
     */
    public Date getExpDate() {
        return expDate;
    }

    /**
     * Establece la fecha de expiración.
     * @param expDate la fecha a establecer.
     */
    public void setExpDate(Date expDate) {
        this.expDate = expDate;
    }

    /**
     * Obtiene el costo de la póliza.
     * @return el costo.
     */
    public Float getCost() {
        return cost;
    }

    /**
     * Establece el costo de la póliza.
     * @param cost el costo a establecer.
     */
    public void setCost(Float cost) {
        this.cost = cost;
    }

    /**
     * Obtiene el estado de habilitación.
     * @return el estado de habilitación.
     */
    public Integer getEnabled() {
        return enabled;
    }

    /**
     * Establece el estado de habilitación.
     * @param enabled el estado a establecer.
     */
    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}