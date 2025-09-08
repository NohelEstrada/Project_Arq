package com.sources.app.dto;

/**
 * DTO para la creación de medicamentos que encapsula los múltiples parámetros
 * necesarios para crear un nuevo medicamento.
 */
public class MedicineCreateRequest {
    private String name;
    private String activeMedicament;
    private String description;
    private String image;
    private String concentration;
    private Double presentacion;
    private Integer stock;
    private String brand;
    private Boolean prescription;
    private Double price;
    private Integer soldUnits;

    public MedicineCreateRequest() {}

    /**
     * @deprecated Use builder pattern instead: MedicineCreateRequest.builder()...build()
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public MedicineCreateRequest(String name, String activeMedicament, String description, String image,
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String activeMedicament;
        private String description;
        private String image;
        private String concentration;
        private Double presentacion;
        private Integer stock;
        private String brand;
        private Boolean prescription;
        private Double price;
        private Integer soldUnits;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder activeMedicament(String activeMedicament) {
            this.activeMedicament = activeMedicament;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public Builder concentration(String concentration) {
            this.concentration = concentration;
            return this;
        }

        public Builder presentacion(Double presentacion) {
            this.presentacion = presentacion;
            return this;
        }

        public Builder stock(Integer stock) {
            this.stock = stock;
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder prescription(Boolean prescription) {
            this.prescription = prescription;
            return this;
        }

        public Builder price(Double price) {
            this.price = price;
            return this;
        }

        public Builder soldUnits(Integer soldUnits) {
            this.soldUnits = soldUnits;
            return this;
        }

        public MedicineCreateRequest build() {
            MedicineCreateRequest request = new MedicineCreateRequest();
            request.name = this.name;
            request.activeMedicament = this.activeMedicament;
            request.description = this.description;
            request.image = this.image;
            request.concentration = this.concentration;
            request.presentacion = this.presentacion;
            request.stock = this.stock;
            request.brand = this.brand;
            request.prescription = this.prescription;
            request.price = this.price;
            request.soldUnits = this.soldUnits;
            return request;
        }
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getActiveMedicament() { return activeMedicament; }
    public void setActiveMedicament(String activeMedicament) { this.activeMedicament = activeMedicament; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getConcentration() { return concentration; }
    public void setConcentration(String concentration) { this.concentration = concentration; }

    public Double getPresentacion() { return presentacion; }
    public void setPresentacion(Double presentacion) { this.presentacion = presentacion; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Boolean getPrescription() { return prescription; }
    public void setPrescription(Boolean prescription) { this.prescription = prescription; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getSoldUnits() { return soldUnits; }
    public void setSoldUnits(Integer soldUnits) { this.soldUnits = soldUnits; }
}
