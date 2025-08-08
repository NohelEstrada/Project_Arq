package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Representa una entidad de configuración del sistema mapeada a la tabla SYSTEM_CONFIG.
 * Almacena pares clave-valor para configuraciones generales del sistema.
 */
@Entity
@Table(name = "SYSTEM_CONFIG")
public class SystemConfig {

    /**
     * Identificador único de la configuración.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CONFIG")
    private Long idConfig;

    /**
     * Clave única que identifica la configuración.
     * No puede ser nulo, es único y tiene una longitud máxima de 50 caracteres.
     */
    @Column(name = "CONFIG_KEY", length = 50, nullable = false, unique = true)
    private String configKey;

    /**
     * Valor asociado a la clave de configuración.
     * No puede ser nulo.
     */
    @Column(name = "CONFIG_VALUE", nullable = false)
    private String configValue;

    /**
     * Descripción opcional de la configuración.
     * Puede ser nulo.
     */
    @Column(name = "CONFIG_DESCRIPTION")
    private String configDescription;

    /**
     * Fecha y hora de creación del registro de configuración.
     * No puede ser nulo. Se establece automáticamente al persistir.
     */
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt;

    /**
     * Fecha y hora de la última actualización del registro de configuración.
     * Puede ser nulo. Se establece automáticamente al actualizar.
     */
    @Column(name = "UPDATED_AT")
    private Date updatedAt;

    /**
     * Método ejecutado antes de persistir la entidad por primera vez.
     * Establece la fecha de creación (`createdAt`).
     */
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    /**
     * Método ejecutado antes de actualizar la entidad.
     * Establece la fecha de actualización (`updatedAt`).
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    /**
     * Constructor por defecto.
     * Requerido por JPA.
     */
    // Constructores
    public SystemConfig() {
    }

    /**
     * Constructor para crear una instancia de SystemConfig con clave, valor y descripción.
     * La fecha de creación se establece automáticamente.
     * @param configKey La clave de configuración.
     * @param configValue El valor de configuración.
     * @param configDescription La descripción de la configuración.
     */
    public SystemConfig(String configKey, String configValue, String configDescription) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.configDescription = configDescription;
    }

    // Getters y Setters
    /**
     * Obtiene el ID de la configuración.
     * @return el ID de la configuración.
     */
    public Long getIdConfig() {
        return idConfig;
    }

    /**
     * Establece el ID de la configuración.
     * @param idConfig el ID a establecer.
     */
    public void setIdConfig(Long idConfig) {
        this.idConfig = idConfig;
    }

    /**
     * Obtiene la clave de la configuración.
     * @return la clave de configuración.
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * Establece la clave de la configuración.
     * @param configKey la clave a establecer.
     */
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    /**
     * Obtiene el valor de la configuración.
     * @return el valor de configuración.
     */
    public String getConfigValue() {
        return configValue;
    }

    /**
     * Establece el valor de la configuración.
     * @param configValue el valor a establecer.
     */
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    /**
     * Obtiene la descripción de la configuración.
     * @return la descripción de la configuración.
     */
    public String getConfigDescription() {
        return configDescription;
    }

    /**
     * Establece la descripción de la configuración.
     * @param configDescription la descripción a establecer.
     */
    public void setConfigDescription(String configDescription) {
        this.configDescription = configDescription;
    }

    /**
     * Obtiene la fecha de creación del registro.
     * @return la fecha de creación.
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Establece la fecha de creación del registro.
     * (Normalmente no se usa, ya que se maneja con @PrePersist).
     * @param createdAt la fecha de creación a establecer.
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Obtiene la fecha de la última actualización.
     * @return la fecha de actualización.
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Establece la fecha de la última actualización.
     * (Normalmente no se usa, ya que se maneja con @PreUpdate).
     * @param updatedAt la fecha de actualización a establecer.
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
} 