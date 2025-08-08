package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Entidad que representa una subcategoría, posiblemente anidada dentro de una categoría.
 * Mapea a la tabla "SUBCATEGORY".
 */
@Entity
@Table(name = "SUBCATEGORY")
public class Subcategory {

    /** Identificador único de la subcategoría. Generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SUBCATEGORY")
    private Long idSubcategory;

    /** Nombre de la subcategoría. */
    @Column(name = "NAME")
    private String name;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Subcategory() {
    }

    /**
     * Constructor para crear una nueva subcategoría con un nombre.
     *
     * @param name El nombre de la subcategoría.
     */
    public Subcategory(String name) {
        this.name = name;
    }

    // Getters y setters
    public Long getIdSubcategory() {
        return idSubcategory;
    }

    public void setIdSubcategory(Long idSubcategory) {
        this.idSubcategory = idSubcategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
