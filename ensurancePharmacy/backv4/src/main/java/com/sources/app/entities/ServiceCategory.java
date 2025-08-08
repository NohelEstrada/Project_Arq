package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Representa la relación entre un servicio y una categoría, mapeada a la tabla SERVICE_CATEGORY.
 * Utiliza una clave primaria compuesta definida en {@link ServiceCategoryId}.
 * Esta entidad actúa como una tabla de unión para una relación Many-to-Many entre Service y Category.
 */
@Entity
@Table(name = "SERVICE_CATEGORY")
@IdClass(ServiceCategoryId.class)
public class ServiceCategory {

    /**
     * Parte de la clave primaria compuesta, representa el servicio.
     * Relación Many-to-One obligatoria con la entidad Service.
     */
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_SERVICE", nullable = false)
    private Service service;

    /**
     * Parte de la clave primaria compuesta, representa la categoría.
     * Relación Many-to-One obligatoria con la entidad Category.
     */
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_CATEGORY", nullable = false)
    private Category category;

    /**
     * Constructor por defecto para la entidad ServiceCategory.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public ServiceCategory() {}

    // Getters y Setters
    /**
     * Obtiene el servicio asociado.
     * @return la entidad Service.
     */
    public Service getService() {
        return service;
    }

    /**
     * Establece el servicio asociado.
     * @param service la entidad Service a establecer.
     */
    public void setService(Service service) {
        this.service = service;
    }

    /**
     * Obtiene la categoría asociada.
     * @return la entidad Category.
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Establece la categoría asociada.
     * @param category la entidad Category a establecer.
     */
    public void setCategory(Category category) {
        this.category = category;
    }
}
