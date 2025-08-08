package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Entidad que representa una póliza de seguro o una política de cobertura.
 * Mapea a la tabla "POLICY".
 */
@Entity
@Table(name = "POLICY")
public class Policy {

    /** Identificador único de la póliza. Generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_POLICY")
    private Long idPolicy;

    /** Porcentaje de cobertura ofrecido por la póliza (e.g., 0.8 para 80%). */
    @Column(name = "PERCENTAGE")
    private Double percentage;

    /** Indica si la póliza está activa/habilitada ('1' para habilitada, '0' para deshabilitada). */
    @Column(name = "ENABLED")
    private Character enabled;

    /** Fecha de expiración de la póliza (opcional). */
    @Temporal(TemporalType.DATE)
    @Column(name = "EXPIRATION_DATE")
    private Date expirationDate;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Policy() {
    }

    /**
     * Constructor para crear una póliza con porcentaje y estado de habilitación.
     *
     * @param percentage El porcentaje de cobertura.
     * @param enabled El estado de habilitación ('1' o '0').
     */
    public Policy(Double percentage, Character enabled) {
        this.percentage = percentage;
        this.enabled = enabled;
    }

    // Getters y setters
    public Long getIdPolicy() {
        return idPolicy;
    }

    public void setIdPolicy(Long idPolicy) {
        this.idPolicy = idPolicy;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Character getEnabled() {
        return enabled;
    }

    public void setEnabled(Character enabled) {
        this.enabled = enabled;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Double getCoveragePercentage() {
        return percentage;
    }
}
