package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Representa una entidad de usuario mapeada a la tabla USERS.
 * Contiene información detallada sobre los usuarios del sistema.
 */
@Entity
@Table(name = "USERS")
public class User {

    /**
     * Identificador único del usuario (Clave primaria).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USER")
    private Long idUser;  // Clave primaria

    /**
     * Nombre completo del usuario.
     * No puede ser nulo.
     */
    @Column(name = "NAME", nullable = false)
    private String name;  // Nombre del usuario

    /**
     * Código Único de Identificación (CUI) del usuario.
     * No puede ser nulo.
     */
    @Column(name = "CUI", nullable = false)
    private Long cui;  // CUI (identificación única)

    /**
     * Número de teléfono del usuario.
     * Puede ser nulo.
     */
    @Column(name = "PHONE")
    private String phone;  // Teléfono

    /**
     * Dirección de correo electrónico del usuario.
     * Debe ser único y no puede ser nulo.
     */
    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;  // Correo electrónico

    /**
     * Dirección física del usuario.
     * Puede ser nulo.
     */
    @Column(name = "ADDRESS")
    private String address;  // Dirección

    /**
     * Fecha de nacimiento del usuario.
     * Se almacena solo la fecha.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "BIRTHDATE")
    private Date birthDate;  // Fecha de nacimiento

    /**
     * Rol del usuario dentro del sistema (ej. ADMIN, USER).
     * No puede ser nulo.
     */
    @Column(name = "ROL", nullable = false)
    private String role;  // Rol del usuario

    /**
     * Póliza asociada al usuario.
     * Relación Many-to-One opcional con la entidad Policy.
     * Puede ser nulo.
     */
    // Relación ManyToOne opcional con Policy (ya no es obligatoria)
    @ManyToOne(optional = true)
    @JoinColumn(name = "ID_POLICY", nullable = true)
    private Policy policy;

    /**
     * Estado del usuario (habilitado/deshabilitado).
     * Típicamente 1 para habilitado, 0 para deshabilitado. No puede ser nulo.
     */
    @Column(name = "ENABLED", nullable = false)
    private Integer enabled;  // Estado (habilitado/deshabilitado)

    /**
     * Contraseña del usuario (generalmente almacenada hasheada).
     * No puede ser nulo.
     */
    @Column(name = "PASSWORD", nullable = false)
    private String password;

    /**
     * Indica si el usuario ha pagado por el servicio.
     * Puede ser nulo si el estado de pago no está definido.
     */
    @Column(name = "PAID_SERVICE")
    private Boolean paidService;  // Ahora puede ser nulo

    /**
     * Fecha de expiración del servicio pagado.
     * Puede ser nulo.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "EXPIRATION_DATE")
    private Date expirationDate;  // Fecha de expiración del servicio

    /**
     * Fecha y hora de creación del registro del usuario.
     * Se almacena con precisión de timestamp.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT")
    private Date createdAt;  // Fecha de creación del usuario

    /**
     * Constructor por defecto.
     * Inicializa la fecha de creación con la fecha actual y el estado de pago a nulo.
     */
    // Constructor sin argumentos
    public User() {
        this.createdAt = new Date(); // Inicializar con la fecha actual al crear un usuario
        this.paidService = null; // Inicialmente no definido
    }

    /**
     * Constructor completo para crear una instancia de User con todos los detalles.
     * @param name Nombre del usuario.
     * @param cui CUI del usuario.
     * @param phone Teléfono del usuario.
     * @param email Correo electrónico del usuario.
     * @param address Dirección del usuario.
     * @param birthDate Fecha de nacimiento del usuario.
     * @param role Rol del usuario.
     * @param policy Póliza asociada al usuario.
     * @param enabled Estado de habilitación del usuario.
     * @param password Contraseña del usuario.
     * @param paidService Estado de pago del servicio.
     * @param expirationDate Fecha de expiración del servicio.
     */
    // Constructor completo
    public User(String name, Long cui, String phone, String email, String address, Date birthDate, String role, Policy policy, Integer enabled, String password, Boolean paidService, Date expirationDate) {
        this.name = name;
        this.cui = cui;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.birthDate = birthDate;
        this.role = role;
        this.policy = policy;
        this.enabled = enabled;
        this.password = password;
        this.paidService = paidService; // Puede ser null
        this.expirationDate = expirationDate;
        this.createdAt = new Date();
    }

    // Getters y Setters
    /**
     * Obtiene el ID del usuario.
     * @return el ID del usuario.
     */
    public Long getIdUser() { return idUser; }
    /**
     * Establece el ID del usuario.
     * @param idUser el ID del usuario a establecer.
     */
    public void setIdUser(Long idUser) { this.idUser = idUser; }

    /**
     * Obtiene el nombre del usuario.
     * @return el nombre del usuario.
     */
    public String getName() { return name; }
    /**
     * Establece el nombre del usuario.
     * @param name el nombre del usuario a establecer.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Obtiene el CUI del usuario.
     * @return el CUI del usuario.
     */
    public Long getCui() { return cui; }
    /**
     * Establece el CUI del usuario.
     * @param cui el CUI del usuario a establecer.
     */
    public void setCui(Long cui) { this.cui = cui; }

    /**
     * Obtiene el teléfono del usuario.
     * @return el teléfono del usuario.
     */
    public String getPhone() { return phone; }
    /**
     * Establece el teléfono del usuario.
     * @param phone el teléfono del usuario a establecer.
     */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Obtiene el correo electrónico del usuario.
     * @return el correo electrónico del usuario.
     */
    public String getEmail() { return email; }
    /**
     * Establece el correo electrónico del usuario.
     * @param email el correo electrónico del usuario a establecer.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Obtiene la dirección del usuario.
     * @return la dirección del usuario.
     */
    public String getAddress() { return address; }
    /**
     * Establece la dirección del usuario.
     * @param address la dirección del usuario a establecer.
     */
    public void setAddress(String address) { this.address = address; }

    /**
     * Obtiene la fecha de nacimiento del usuario.
     * @return la fecha de nacimiento.
     */
    public Date getBirthDate() { return birthDate; }
    /**
     * Establece la fecha de nacimiento del usuario.
     * @param birthDate la fecha de nacimiento a establecer.
     */
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }

    /**
     * Obtiene el rol del usuario.
     * @return el rol del usuario.
     */
    public String getRole() { return role; }
    /**
     * Establece el rol del usuario.
     * @param role el rol del usuario a establecer.
     */
    public void setRole(String role) { this.role = role; }

    /**
     * Obtiene la póliza asociada al usuario.
     * @return la póliza asociada.
     */
    public Policy getPolicy() { return policy; }
    /**
     * Establece la póliza asociada al usuario.
     * @param policy la póliza a establecer.
     */
    public void setPolicy(Policy policy) { this.policy = policy; }

    /**
     * Obtiene el estado de habilitación del usuario.
     * @return el estado de habilitación.
     */
    public Integer getEnabled() { return enabled; }
    /**
     * Establece el estado de habilitación del usuario.
     * @param enabled el estado de habilitación a establecer.
     */
    public void setEnabled(Integer enabled) { this.enabled = enabled; }

    /**
     * Obtiene la contraseña del usuario.
     * @return la contraseña.
     */
    public String getPassword() { return password; }
    /**
     * Establece la contraseña del usuario.
     * @param password la contraseña a establecer.
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Obtiene la fecha de creación del registro.
     * @return la fecha de creación.
     */
    public Date getCreatedAt() { return createdAt; }
    /**
     * Establece la fecha de creación del registro.
     * @param createdAt la fecha de creación a establecer.
     */
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    /**
     * Obtiene el estado de pago del servicio.
     * @return true si el servicio está pagado, false si no, null si no definido.
     */
    public Boolean getPaidService() { return paidService; }
    /**
     * Establece el estado de pago del servicio.
     * @param paidService el estado de pago a establecer.
     */
    public void setPaidService(Boolean paidService) { this.paidService = paidService; }

    /**
     * Obtiene la fecha de expiración del servicio.
     * @return la fecha de expiración.
     */
    public Date getExpirationDate() { return expirationDate; }
    /**
     * Establece la fecha de expiración del servicio.
     * @param expirationDate la fecha de expiración a establecer.
     */
    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }
}
