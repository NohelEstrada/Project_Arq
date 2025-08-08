package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Representa una entidad de categoría mapeada a la tabla CATEGORY.
 * Utilizada para clasificar otros elementos como servicios o medicamentos.
 */
@Entity
@Table(name = "CATEGORY")
public class Category {
    /**
     * El identificador único para la categoría.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CATEGORY")
    private Long idCategory;

    /**
     * El nombre de la categoría.
     * No puede ser nulo y tiene una longitud máxima de 100 caracteres.
     */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /**
     * Indica si la categoría está habilitada o activa.
     * Típicamente 1 para habilitado, 0 para deshabilitado. No puede ser nulo.
     */
    @Column(name = "ENABLED", nullable = false)
    private Integer enabled;

    /**
     * Constructor por defecto para la entidad Category.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public Category() {}

    // Getters y Setters
    /**
     * Obtiene el identificador único de la categoría.
     * @return el ID de la categoría.
     */
    public Long getIdCategory() { return idCategory; }
    /**
     * Establece el identificador único de la categoría.
     * @param idCategory el ID de la categoría a establecer.
     */
    public void setIdCategory(Long idCategory) { this.idCategory = idCategory; }

    /**
     * Obtiene el nombre de la categoría.
     * @return el nombre de la categoría.
     */
    public String getName() { return name; }
    /**
     * Establece el nombre de la categoría.
     * @param name el nombre de la categoría a establecer.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Obtiene el estado habilitado de la categoría.
     * @return el estado habilitado.
     */
    public Integer getEnabled() { return enabled; }
    /**
     * Establece el estado habilitado de la categoría.
     * @param enabled el estado habilitado a establecer.
     */
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
}
