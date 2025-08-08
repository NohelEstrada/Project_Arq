package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Representa una entidad de hospital mapeada a la tabla HOSPITALS.
 * Contiene información sobre hospitales, como nombre, dirección, teléfono, email y estado.
 */
@Entity
@Table(name = "HOSPITALS")
public class Hospital {
    /**
     * El identificador único para el hospital.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HOSPITAL")
    private Long idHospital;

    /**
     * El nombre del hospital.
     * No puede ser nulo y tiene una longitud máxima de 100 caracteres.
     */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /**
     * La dirección del hospital.
     * No puede ser nulo y tiene una longitud máxima de 255 caracteres.
     */
    @Column(name = "ADDRESS", nullable = false, length = 255)
    private String address;

    /**
     * El número de teléfono del hospital.
     * No puede ser nulo.
     */
    @Column(name = "PHONE", nullable = false)
    private Long phone;

    /**
     * La dirección de correo electrónico del hospital.
     * No puede ser nulo y tiene una longitud máxima de 100 caracteres.
     */
    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

    /**
     * Indica si el hospital está habilitado o activo.
     * Típicamente 1 para habilitado, 0 para deshabilitado. No puede ser nulo.
     */
    @Column(name = "ENABLED", nullable = false)
    private Integer enabled;
    
    /**
     * El puerto del servidor del hospital para conectarse a su API.
     * Puede ser nulo y tiene una longitud máxima de 10 caracteres.
     */
    @Column(name = "PORT", length = 10)
    private String port;

    /**
     * Constructor por defecto para la entidad Hospital.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public Hospital() {}

    // Getters y Setters
    /**
     * Obtiene el identificador único del hospital.
     * @return el ID del hospital.
     */
    public Long getIdHospital() { return idHospital; }
    /**
     * Establece el identificador único del hospital.
     * @param idHospital el ID del hospital a establecer.
     */
    public void setIdHospital(Long idHospital) { this.idHospital = idHospital; }

    /**
     * Obtiene el nombre del hospital.
     * @return el nombre del hospital.
     */
    public String getName() { return name; }
    /**
     * Establece el nombre del hospital.
     * @param name el nombre del hospital a establecer.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Obtiene la dirección del hospital.
     * @return la dirección del hospital.
     */
    public String getAddress() { return address; }
    /**
     * Establece la dirección del hospital.
     * @param address la dirección del hospital a establecer.
     */
    public void setAddress(String address) { this.address = address; }

    /**
     * Obtiene el número de teléfono del hospital.
     * @return el número de teléfono.
     */
    public Long getPhone() { return phone; }
    /**
     * Establece el número de teléfono del hospital.
     * @param phone el número de teléfono a establecer.
     */
    public void setPhone(Long phone) { this.phone = phone; }

    /**
     * Obtiene la dirección de correo electrónico del hospital.
     * @return la dirección de correo electrónico.
     */
    public String getEmail() { return email; }
    /**
     * Establece la dirección de correo electrónico del hospital.
     * @param email la dirección de correo electrónico a establecer.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Obtiene el estado habilitado del hospital.
     * @return el estado habilitado.
     */
    public Integer getEnabled() { return enabled; }
    /**
     * Establece el estado habilitado del hospital.
     * @param enabled el estado habilitado a establecer.
     */
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
    
    /**
     * Obtiene el puerto del servidor del hospital.
     * @return el puerto del servidor del hospital.
     */
    public String getPort() { return port; }
    /**
     * Establece el puerto del servidor del hospital.
     * @param port el puerto del servidor del hospital a establecer.
     */
    public void setPort(String port) { this.port = port; }
}
