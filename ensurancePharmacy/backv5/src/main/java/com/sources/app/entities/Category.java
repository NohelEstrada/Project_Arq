package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Entidad que representa una categoría de medicamentos u otros ítems.
 * Mapea a la tabla "CATEGORY".
 */
@Entity
@Table(name = "CATEGORY")
public class Category {

    /** Identificador único de la categoría. Generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CATEGORY")
    private Long idCategory;

    /** Nombre de la categoría. */
    @Column(name = "NAME")
    private String name;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Category() {
    }

    /**
     * Constructor para crear una nueva categoría con un nombre.
     *
     * @param name El nombre de la categoría.
     */
    public Category(String name) {
        this.name = name;
    }

    // Getters y setters
    public Long getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Long idCategory) {
        this.idCategory = idCategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
