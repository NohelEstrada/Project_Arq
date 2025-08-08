package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Entidad de enlace que representa la asociación entre un Medicamento,
 * una Categoría y una Subcategoría.
 * Mapea a la tabla "MEDICINE_CATSUBCAT" y utiliza una clave compuesta {@link MedicineCatSubcatId}.
 */
@Entity
@Table(name = "MEDICINE_CATSUBCAT")
public class MedicineCatSubcat {

    /** Clave primaria compuesta embebida (ID de medicamento, ID de categoría, ID de subcategoría). */
    @EmbeddedId
    private MedicineCatSubcatId id;

    /** Medicamento asociado. Parte de la clave compuesta. */
    @ManyToOne
    @MapsId("medicineId") // Mapea el atributo 'medicineId' de MedicineCatSubcatId
    @JoinColumn(name = "ID_MEDICINE", referencedColumnName = "ID_MEDICINE")
    private Medicine medicine;

    /** Categoría asociada. Parte de la clave compuesta. */
    @ManyToOne
    @MapsId("categoryId") // Mapea el atributo 'categoryId' de MedicineCatSubcatId
    @JoinColumn(name = "ID_CATEGORY", referencedColumnName = "ID_CATEGORY")
    private Category category;

    /** Subcategoría asociada. Parte de la clave compuesta. */
    @ManyToOne
    @MapsId("subcategoryId") // Mapea el atributo 'subcategoryId' de MedicineCatSubcatId
    @JoinColumn(name = "ID_SUBCATEGORY", referencedColumnName = "ID_SUBCATEGORY")
    private Subcategory subcategory;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public MedicineCatSubcat() {
    }

    /**
     * Constructor para crear una nueva asociación medicamento-categoría-subcategoría.
     * Inicializa la clave compuesta a partir de las entidades proporcionadas.
     *
     * @param medicine El medicamento asociado.
     * @param category La categoría asociada.
     * @param subcategory La subcategoría asociada.
     */
    public MedicineCatSubcat(Medicine medicine, Category category, Subcategory subcategory) {
        this.medicine = medicine;
        this.category = category;
        this.subcategory = subcategory;
        this.id = new MedicineCatSubcatId(
                medicine.getIdMedicine(),
                category.getIdCategory(),
                subcategory.getIdSubcategory()
        );
    }

    public MedicineCatSubcatId getId() {
        return id;
    }

    public void setId(MedicineCatSubcatId id) {
        this.id = id;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
        if (id == null) {
            id = new MedicineCatSubcatId();
        }
        id.setMedicineId(medicine.getIdMedicine());
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
        if (id == null) {
            id = new MedicineCatSubcatId();
        }
        id.setCategoryId(category.getIdCategory());
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Subcategory subcategory) {
        this.subcategory = subcategory;
        if (id == null) {
            id = new MedicineCatSubcatId();
        }
        id.setSubcategoryId(subcategory.getIdSubcategory());
    }
}
