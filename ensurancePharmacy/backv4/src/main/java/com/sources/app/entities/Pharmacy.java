package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Representa una entidad de farmacia mapeada a la tabla PHARMACY.
 * Contiene información sobre farmacias, como nombre, dirección, teléfono, email y estado.
 */
@Entity
@Table(name = "PHARMACY")
public class Pharmacy {
    /**
     * El identificador único para la farmacia.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PHARMACY")
    private Long idPharmacy;

    /**
     * El nombre de la farmacia.
     * No puede ser nulo y tiene una longitud máxima de 100 caracteres.
     */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /**
     * La dirección de la farmacia.
     * No puede ser nulo y tiene una longitud máxima de 255 caracteres.
     */
    @Column(name = "ADDRESS", nullable = false, length = 255)
    private String address;

    /**
     * El número de teléfono de la farmacia.
     * No puede ser nulo.
     */
    @Column(name = "PHONE", nullable = false)
    private Long phone;

    /**
     * La dirección de correo electrónico de la farmacia.
     * Puede ser nulo.
     */
    @Column(name = "EMAIL")
    private String email;

    /**
     * Indica si la farmacia está habilitada o activa.
     * Típicamente 1 para habilitado, 0 para deshabilitado. No puede ser nulo.
     */
    @Column(name = "ENABLED", nullable = false)
    private Integer enabled;

    /**
     * Constructor por defecto para la entidad Pharmacy.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public Pharmacy() {}

    // Getters y Setters
    /**
     * Obtiene el identificador único de la farmacia.
     * @return el ID de la farmacia.
     */
    public Long getIdPharmacy() { return idPharmacy; }
    /**
     * Establece el identificador único de la farmacia.
     * @param idPharmacy el ID de la farmacia a establecer.
     */
    public void setIdPharmacy(Long idPharmacy) { this.idPharmacy = idPharmacy; }

    /**
     * Obtiene el nombre de la farmacia.
     * @return el nombre de la farmacia.
     */
    public String getName() { return name; }
    /**
     * Establece el nombre de la farmacia.
     * @param name el nombre de la farmacia a establecer.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Obtiene la dirección de la farmacia.
     * @return la dirección de la farmacia.
     */
    public String getAddress() { return address; }
    /**
     * Establece la dirección de la farmacia.
     * @param address la dirección de la farmacia a establecer.
     */
    public void setAddress(String address) { this.address = address; }

    /**
     * Obtiene el número de teléfono de la farmacia.
     * @return el número de teléfono.
     */
    public Long getPhone() { return phone; }
    /**
     * Establece el número de teléfono de la farmacia.
     * @param phone el número de teléfono a establecer.
     */
    public void setPhone(Long phone) { this.phone = phone; }

    /**
     * Obtiene la dirección de correo electrónico de la farmacia.
     * @return la dirección de correo electrónico.
     */
    public String getEmail() { return email; }
    /**
     * Establece la dirección de correo electrónico de la farmacia.
     * @param email la dirección de correo electrónico a establecer.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Obtiene el estado habilitado de la farmacia.
     * @return el estado habilitado.
     */
    public Integer getEnabled() { return enabled; }
    /**
     * Establece el estado habilitado de la farmacia.
     * @param enabled el estado habilitado a establecer.
     */
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
}
