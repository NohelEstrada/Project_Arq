package com.sources.app.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Representa una entidad de medicamento mapeada a la tabla MEDICINE.
 * Contiene información detallada sobre medicamentos, incluyendo precio, farmacia asociada, y otros atributos.
 */
@Entity
@Table(name = "MEDICINE")
public class Medicine {
    /**
     * Identificador único del medicamento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MEDICINE")
    private Long idMedicine;

    /**
     * Nombre del medicamento.
     * No puede ser nulo y tiene longitud máxima de 100 caracteres.
     */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /**
     * Descripción del medicamento.
     * No puede ser nulo y tiene longitud máxima de 255 caracteres.
     */
    @Column(name = "DESCRIPTION", nullable = false, length = 255)
    private String description;

    /**
     * Precio del medicamento.
     * Se almacena con precisión decimal (10, 2). No puede ser nulo.
     */
    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Farmacia que suministra el medicamento.
     * Relación Many-to-One obligatoria con la entidad Pharmacy.
     */
    // Relación ManyToOne con Pharmacy (ID_PHARMACY es llave foránea)
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_PHARMACY", nullable = false)
    private Pharmacy pharmacy;

    /**
     * Indica si el medicamento está habilitado o activo.
     * Típicamente 1 para habilitado, 0 para deshabilitado. No puede ser nulo.
     */
    @Column(name = "ENABLED", nullable = false)
    private Integer enabled;

    /**
     * Principio activo del medicamento.
     * Puede ser nulo. Longitud máxima de 255 caracteres.
     */
    @Column(name = "ACTIVE_PRINCIPLE", nullable = true, length = 255)
    private String activePrinciple;

    /**
     * Presentación del medicamento (ej. tabletas, jarabe).
     * Puede ser nulo. Longitud máxima de 255 caracteres.
     */
    @Column(name = "PRESENTATION", nullable = true, length = 255)
    private String presentation;

    /**
     * Cantidad en stock del medicamento.
     * Puede ser nulo.
     */
    @Column(name = "STOCK", nullable = true)
    private Integer stock;

    /**
     * Marca o fabricante del medicamento.
     * Puede ser nulo. Longitud máxima de 100 caracteres.
     */
    @Column(name = "BRAND", nullable = true, length = 100)
    private String brand;

    /**
     * Indica el nivel de cobertura del medicamento (ej. 0 o 1).
     * No puede ser nulo. Precisión de 1 dígito.
     */
    // Nuevo atributo coverage (NUMBER(1,0))
    @Column(name = "COVERAGE", nullable = false, precision = 1, scale = 0)
    private Integer coverage;

    /**
     * Constructor por defecto para la entidad Medicine.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public Medicine() {}

    // Getters y Setters
    /**
     * Obtiene el ID del medicamento.
     * @return el ID del medicamento.
     */
    public Long getIdMedicine() {
        return idMedicine;
    }

    /**
     * Establece el ID del medicamento.
     * @param idMedicine el ID a establecer.
     */
    public void setIdMedicine(Long idMedicine) {
        this.idMedicine = idMedicine;
    }

    /**
     * Obtiene el nombre del medicamento.
     * @return el nombre.
     */
    public String getName() {
        return name;
    }

    /**
     * Establece el nombre del medicamento.
     * @param name el nombre a establecer.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtiene la descripción del medicamento.
     * @return la descripción.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Establece la descripción del medicamento.
     * @param description la descripción a establecer.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Obtiene el precio del medicamento.
     * @return el precio.
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Establece el precio del medicamento.
     * @param price el precio a establecer.
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
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
     * Obtiene el estado de habilitación del medicamento.
     * @return el estado de habilitación.
     */
    public Integer getEnabled() {
        return enabled;
    }

    /**
     * Establece el estado de habilitación del medicamento.
     * @param enabled el estado a establecer.
     */
    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    /**
     * Obtiene el principio activo.
     * @return el principio activo.
     */
    public String getActivePrinciple() {
        return activePrinciple;
    }

    /**
     * Establece el principio activo.
     * @param activePrinciple el principio activo a establecer.
     */
    public void setActivePrinciple(String activePrinciple) {
        this.activePrinciple = activePrinciple;
    }

    /**
     * Obtiene la presentación del medicamento.
     * @return la presentación.
     */
    public String getPresentation() {
        return presentation;
    }

    /**
     * Establece la presentación del medicamento.
     * @param presentation la presentación a establecer.
     */
    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    /**
     * Obtiene la cantidad en stock.
     * @return el stock.
     */
    public Integer getStock() {
        return stock;
    }

    /**
     * Establece la cantidad en stock.
     * @param stock el stock a establecer.
     */
    public void setStock(Integer stock) {
        this.stock = stock;
    }

    /**
     * Obtiene la marca del medicamento.
     * @return la marca.
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Establece la marca del medicamento.
     * @param brand la marca a establecer.
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * Obtiene el nivel de cobertura.
     * @return el nivel de cobertura.
     */
    public Integer getCoverage() {
        return coverage;
    }

    /**
     * Establece el nivel de cobertura.
     * @param coverage el nivel de cobertura a establecer.
     */
    public void setCoverage(Integer coverage) {
        this.coverage = coverage;
    }
}
