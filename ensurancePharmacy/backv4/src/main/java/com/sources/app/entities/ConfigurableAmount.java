package com.sources.app.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Representa una entidad para almacenar montos configurables, mapeada a la tabla CONFIGURABLE_AMOUNT.
 * Actualmente, parece contener solo un monto relacionado con prescripciones.
 */
@Entity
@Table(name = "CONFIGURABLE_AMOUNT")
public class ConfigurableAmount {
    /**
     * Identificador único para el monto configurable.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CONFIGURABLE_AMOUNT")
    private Long idConfigurableAmount;

    /**
     * Monto configurable relacionado con prescripciones.
     * Se almacena con precisión decimal (10, 2). No puede ser nulo.
     */
    @Column(name = "PRESCRIPTION_AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal prescriptionAmount;

    /**
     * Constructor por defecto para la entidad ConfigurableAmount.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public ConfigurableAmount() {}

    // Getters y Setters
    /**
     * Obtiene el ID del monto configurable.
     * @return el ID del monto configurable.
     */
    public Long getIdConfigurableAmount() { return idConfigurableAmount; }
    /**
     * Establece el ID del monto configurable.
     * @param idConfigurableAmount el ID a establecer.
     */
    public void setIdConfigurableAmount(Long idConfigurableAmount) { this.idConfigurableAmount = idConfigurableAmount; }

    /**
     * Obtiene el monto configurable para prescripciones.
     * @return el monto de prescripción.
     */
    public BigDecimal getPrescriptionAmount() { return prescriptionAmount; }
    /**
     * Establece el monto configurable para prescripciones.
     * @param prescriptionAmount el monto a establecer.
     */
    public void setPrescriptionAmount(BigDecimal prescriptionAmount) { this.prescriptionAmount = prescriptionAmount; }
}
