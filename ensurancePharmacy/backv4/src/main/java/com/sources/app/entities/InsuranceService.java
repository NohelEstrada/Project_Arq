package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Representa una entidad de servicio de seguro mapeada a la tabla INSURANCE_SERVICES.
 * Contiene información sobre los servicios ofrecidos por el seguro, incluyendo categorías, precio y cobertura.
 */
@Entity
@Table(name = "INSURANCE_SERVICES")
public class InsuranceService {

    /**
     * Identificador único del servicio de seguro.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INSURANCE_SERVICE")
    private Long idInsuranceService;

    /**
     * Nombre del servicio de seguro.
     * No puede ser nulo. Longitud máxima de 100 caracteres.
     */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /**
     * Descripción del servicio de seguro.
     * No puede ser nulo. Longitud máxima de 255 caracteres.
     */
    @Column(name = "DESCRIPTION", nullable = false, length = 255)
    private String description;

    /**
     * Identificador externo del servicio (posiblemente de un sistema tercero).
     * Puede ser nulo. Longitud máxima de 100 caracteres.
     */
    @Column(name = "EXTERNAL_ID", length = 100)
    private String externalId;

    /**
     * Categoría principal a la que pertenece el servicio.
     * Relación Many-to-One obligatoria con la entidad Category.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_CATEGORY", nullable = false)
    private Category category;

    /**
     * Subcategoría a la que pertenece el servicio.
     * Relación Many-to-One obligatoria con la entidad Category.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_SUBCATEGORY", nullable = false)
    private Category subcategory;

    /**
     * Precio o costo base del servicio.
     * No puede ser nulo.
     */
    @Column(name = "PRICE", nullable = false)
    private Double price;

    /**
     * Porcentaje de cobertura ofrecido por el seguro para este servicio.
     * No puede ser nulo.
     */
    @Column(name = "COVERAGE_PERCENTAGE", nullable = false)
    private Integer coveragePercentage;

    /**
     * Indica si el servicio está habilitado o activo.
     * Típicamente 1 para habilitado, 0 para deshabilitado. No puede ser nulo.
     */
    @Column(name = "ENABLED", nullable = false)
    private Integer enabled;

    /**
     * Constructor por defecto para la entidad InsuranceService.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public InsuranceService() {}

    // Getters y Setters
    /**
     * Obtiene el ID del servicio de seguro.
     * @return el ID del servicio.
     */
    public Long getIdInsuranceService() {
        return idInsuranceService;
    }

    /**
     * Establece el ID del servicio de seguro.
     * @param idInsuranceService el ID a establecer.
     */
    public void setIdInsuranceService(Long idInsuranceService) {
        this.idInsuranceService = idInsuranceService;
    }

    /**
     * Obtiene el nombre del servicio.
     * @return el nombre.
     */
    public String getName() {
        return name;
    }

    /**
     * Establece el nombre del servicio.
     * @param name el nombre a establecer.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtiene la descripción del servicio.
     * @return la descripción.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Establece la descripción del servicio.
     * @param description la descripción a establecer.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Obtiene el ID externo del servicio.
     * @return el ID externo.
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * Establece el ID externo del servicio.
     * @param externalId el ID externo a establecer.
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * Obtiene la categoría principal del servicio.
     * @return la entidad Category principal.
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Establece la categoría principal del servicio.
     * @param category la entidad Category a establecer.
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Obtiene la subcategoría del servicio.
     * @return la entidad Category de subcategoría.
     */
    public Category getSubcategory() {
        return subcategory;
    }

    /**
     * Establece la subcategoría del servicio.
     * @param subcategory la entidad Category a establecer.
     */
    public void setSubcategory(Category subcategory) {
        this.subcategory = subcategory;
    }

    /**
     * Obtiene el precio del servicio.
     * @return el precio.
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Establece el precio del servicio.
     * @param price el precio a establecer.
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * Obtiene el porcentaje de cobertura.
     * @return el porcentaje de cobertura.
     */
    public Integer getCoveragePercentage() {
        return coveragePercentage;
    }

    /**
     * Establece el porcentaje de cobertura.
     * @param coveragePercentage el porcentaje a establecer.
     */
    public void setCoveragePercentage(Integer coveragePercentage) {
        this.coveragePercentage = coveragePercentage;
    }

    /**
     * Obtiene el estado de habilitación del servicio.
     * @return el estado de habilitación.
     */
    public Integer getEnabled() {
        return enabled;
    }

    /**
     * Establece el estado de habilitación del servicio.
     * @param enabled el estado a establecer.
     */
    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
} 