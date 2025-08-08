package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Representa una entidad de servicio ofrecido por un hospital, mapeada a la tabla SERVICES.
 * Contiene detalles del servicio, incluyendo hospital, categorías, costo y estado.
 */
@Entity
@Table(name = "SERVICES")
public class Service {

    /**
     * Identificador único del servicio.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SERVICE")
    private Long idService;

    /**
     * Hospital que ofrece el servicio.
     * Relación Many-to-One obligatoria con la entidad Hospital.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_HOSPITAL", nullable = false)
    private Hospital hospital;

    /**
     * Nombre del servicio.
     * No puede ser nulo. Longitud máxima de 100 caracteres.
     */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /**
     * Descripción del servicio.
     * No puede ser nulo. Longitud máxima de 255 caracteres.
     */
    @Column(name = "DESCRIPTION", nullable = false, length = 255)
    private String description;

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
     * Costo del servicio.
     * No puede ser nulo.
     */
    @Column(name = "COST", nullable = false)
    private Double cost;

    /**
     * Indica si el servicio está habilitado o activo.
     * Típicamente 1 para habilitado, 0 para deshabilitado. No puede ser nulo.
     */
    @Column(name = "ENABLED", nullable = false)
    private Integer enabled;

    /**
     * Constructor por defecto para la entidad Service.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public Service() {}

    // Getters y Setters
    /**
     * Obtiene el ID del servicio.
     * @return el ID del servicio.
     */
    public Long getIdService() {
        return idService;
    }

    /**
     * Establece el ID del servicio.
     * @param idService el ID a establecer.
     */
    public void setIdService(Long idService) {
        this.idService = idService;
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
     * Obtiene el costo del servicio.
     * @return el costo.
     */
    public Double getCost() {
        return cost;
    }

    /**
     * Establece el costo del servicio.
     * @param cost el costo a establecer.
     */
    public void setCost(Double cost) {
        this.cost = cost;
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
