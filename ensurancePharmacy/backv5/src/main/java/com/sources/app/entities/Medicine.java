package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Entidad que representa un medicamento en la base de datos.
 * Mapea a la tabla "MEDICINE".
 */
@Entity
@Table(name = "MEDICINE")
public class Medicine {

    /** Identificador único del medicamento. Generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MEDICINE")
    private Long idMedicine;

    /** Nombre comercial del medicamento. */
    @Column(name = "NAME")
    private String name;

    /** Principio activo del medicamento. */
    @Column(name = "ACTIVE_MEDICAMENT")
    private String activeMedicament;

    /** Descripción del medicamento. */
    @Column(name = "DESCRIPTION")
    private String description;

    /** URL o ruta de la imagen del medicamento. */
    @Column(name = "IMAGE")
    private String image;

    /** Concentración del principio activo. */
    @Column(name = "CONCENTRATION")
    private String concentration;

    /** Presentación del medicamento (e.g., cantidad por envase, volumen). */
    @Column(name = "PRESENTACION")
    private Double presentacion;

    /** Cantidad disponible en stock. */
    @Column(name = "STOCK")
    private Integer stock;

    /** Marca o fabricante del medicamento. */
    @Column(name = "BRAND")
    private String brand;

    /** Indica si el medicamento requiere prescripción médica. */
    @Column(name = "PRESCRIPTION")
    private Boolean prescription;

    /** Precio unitario del medicamento. */
    @Column(name = "PRICE")
    private Double price;

    /** Número de unidades vendidas. */
    @Column(name = "SOLD_UNITS")
    private Integer soldUnits;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Medicine() {
    }

    /**
     * Constructor con todos los campos excepto el ID (que es autogenerado).
     *
     * @param name Nombre comercial.
     * @param activeMedicament Principio activo.
     * @param description Descripción.
     * @param image URL de la imagen.
     * @param concentration Concentración.
     * @param presentacion Presentación.
     * @param stock Stock disponible.
     * @param brand Marca.
     * @param prescription Requiere prescripción.
     * @param price Precio.
     * @param soldUnits Unidades vendidas.
     */
    public Medicine(String name, String activeMedicament, String description, String image,
                    String concentration, Double presentacion, Integer stock, String brand,
                    Boolean prescription, Double price, Integer soldUnits) {
        this.name = name;
        this.activeMedicament = activeMedicament;
        this.description = description;
        this.image = image;
        this.concentration = concentration;
        this.presentacion = presentacion;
        this.stock = stock;
        this.brand = brand;
        this.prescription = prescription;
        this.price = price;
        this.soldUnits = soldUnits;
    }

    // Getters y Setters
    public Long getIdMedicine() {
        return idMedicine;
    }

    public void setIdMedicine(Long idMedicine) {
        this.idMedicine = idMedicine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActiveMedicament() {
        return activeMedicament;
    }

    public void setActiveMedicament(String activeMedicament) {
        this.activeMedicament = activeMedicament;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getConcentration() {
        return concentration;
    }

    public void setConcentration(String concentration) {
        this.concentration = concentration;
    }

    public Double getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(Double presentacion) {
        this.presentacion = presentacion;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Boolean getPrescription() {
        return prescription;
    }

    public void setPrescription(Boolean prescription) {
        this.prescription = prescription;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getSoldUnits() {
        return soldUnits;
    }

    public void setSoldUnits(Integer soldUnits) {
        this.soldUnits = soldUnits;
    }
}
