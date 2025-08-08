package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Entidad que representa un hospital o centro médico.
 * Mapea a la tabla "HOSPITAL".
 */
@Entity
@Table(name = "HOSPITAL")
public class Hospital {

    /** Identificador único del hospital. Generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HOSPITAL")
    private Long idHospital;

    /** Nombre del hospital. */
    @Column(name = "NAME")
    private String name;

    /** Número de teléfono del hospital. */
    @Column(name = "PHONE")
    private String phone;

    /** Dirección de correo electrónico del hospital. */
    @Column(name = "EMAIL")
    private String email;

    /** Dirección física del hospital. */
    @Column(name = "ADDRESS")
    private String address;

    /** Indica si el hospital está habilitado ('1' habilitado, '0' deshabilitado). */
    @Column(name = "ENABLED")
    private Character enabled; // Usamos Character para mapear CHAR(1)

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Hospital() {
    }

    /**
     * Constructor para crear un nuevo hospital.
     *
     * @param name El nombre del hospital.
     * @param phone El número de teléfono.
     * @param email El correo electrónico.
     * @param address La dirección.
     * @param enabled El estado de habilitación ('1' o '0').
     */
    public Hospital(String name, String phone, String email, String address, Character enabled) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.enabled = enabled;
    }

    // Getters y Setters
    public Long getIdHospital() {
        return idHospital;
    }

    public void setIdHospital(Long idHospital) {
        this.idHospital = idHospital;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Character getEnabled() {
        return enabled;
    }

    public void setEnabled(Character enabled) {
        this.enabled = enabled;
    }
}
