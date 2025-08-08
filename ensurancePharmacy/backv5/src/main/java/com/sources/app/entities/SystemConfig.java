package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Entidad que representa una configuración del sistema como un par clave-valor.
 * Mapea a la tabla "SYSTEM_CONFIG".
 */
@Entity
@Table(name = "SYSTEM_CONFIG")
public class SystemConfig {

    /** Identificador único de la configuración. Generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CONFIG")
    private Long idConfig;

    /** Clave única que identifica la configuración. */
    @Column(name = "CONFIG_KEY", nullable = false, unique = true)
    private String configKey;

    /** Valor asociado a la clave de configuración. */
    @Column(name = "CONFIG_VALUE", nullable = false)
    private String configValue;

    /** Descripción opcional de la configuración. */
    @Column(name = "CONFIG_DESCRIPTION")
    private String description;

    /** Fecha de la última actualización de esta configuración. */
    @Column(name = "LAST_UPDATED", nullable = false)
    private Date lastUpdated;

    /**
     * Método invocado por JPA antes de persistir o actualizar la entidad.
     * Actualiza el campo lastUpdated a la fecha y hora actual.
     */
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = new Date();
    }

    /**
     * Constructor por defecto requerido por JPA.
     */
    public SystemConfig() {
    }

    /**
     * Constructor para crear una nueva configuración.
     * La fecha de última actualización se establece automáticamente.
     *
     * @param configKey La clave de configuración.
     * @param configValue El valor de configuración.
     * @param description Una descripción opcional.
     */
    public SystemConfig(String configKey, String configValue, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
        this.lastUpdated = new Date();
    }

    // Getters y Setters
    public Long getIdConfig() {
        return idConfig;
    }

    public void setIdConfig(Long idConfig) {
        this.idConfig = idConfig;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
} 