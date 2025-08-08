package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Entidad que representa un usuario del sistema.
 * Mapea a la tabla "USERS". Contiene información personal, credenciales,
 * rol, estado y la póliza asociada.
 */
@Entity
@Table(name = "\"USERS\"")
public class User {

    /** Identificador único del usuario. Generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USER")
    private Long idUser;

    /** Nombre completo del usuario. */
    @Column(name = "NAME")
    private String name;

    /** Código Único de Identificación (CUI) del usuario. */
    @Column(name = "CUI")
    private String cui;

    /** Número de teléfono del usuario. */
    @Column(name = "PHONE")
    private String phone;

    /** Dirección de correo electrónico del usuario (usado para login). */
    @Column(name = "EMAIL")
    private String email;

    /** Dirección física del usuario. */
    @Column(name = "ADDRESS")
    private String address;

    /** Fecha de nacimiento del usuario. Formateada como "yyyy-MM-dd" en JSON. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "BIRTHDATE")
    private Date birthDate;

    /** Rol asignado al usuario (e.g., "administrador", "usuario"). */
    @Column(name = "ROL")
    private String role;

    /** Indica si el usuario está habilitado ('1' para habilitado, '0' para deshabilitado). */
    @Column(name = "ENABLED")
    private Character enabled;

    /** Contraseña del usuario (se espera que esté hasheada en la BD). */
    @Column(name = "PASSWORD")
    private String password;

    /** Póliza de seguro asociada al usuario. */
    @ManyToOne
    @JoinColumn(name = "ID_POLICY", referencedColumnName = "ID_POLICY")
    private Policy policy;
    
    /** Estado actual del usuario (e.g., "activo", "pendiente_verificacion", "bloqueado"). */
    @Column(name = "STATUS")
    private String status;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public User() {
    }

    /**
     * Constructor para crear un nuevo usuario con datos esenciales.
     * Establece el rol por defecto a "usuario" y habilitado a '1'.
     *
     * @param name Nombre completo.
     * @param cui CUI.
     * @param phone Teléfono.
     * @param email Correo electrónico.
     * @param birthDate Fecha de nacimiento.
     * @param address Dirección.
     * @param password Contraseña (sin hashear, el DAO debería manejarlo).
     */
    public User(String name, String cui, String phone, String email, Date birthDate, String address, String password) {
        this.name = name;
        this.cui = cui;
        this.phone = phone;
        this.email = email;
        this.birthDate = birthDate;
        this.address = address;
        this.password = password;
        this.role = "usuario";
        this.enabled = '1';  // Usamos '1' como Character
    }

    // Getters y Setters
    public Long getIdUser() {
        return idUser;
    }
    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCui() {
        return cui;
    }
    public void setCui(String cui) {
        this.cui = cui;
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
    public Date getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public Character getEnabled() {
        return enabled;
    }
    public void setEnabled(Character enabled) {
        this.enabled = enabled;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Policy getPolicy() {
        return policy;
    }
    
    public void setPolicy(Policy policy) {
        this.policy = policy;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
