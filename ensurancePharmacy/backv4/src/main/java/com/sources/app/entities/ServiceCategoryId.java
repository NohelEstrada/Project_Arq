package com.sources.app.entities;

import java.io.Serializable;
import java.util.Objects;

public class ServiceCategoryId implements Serializable {
    private Long service;   // Corresponde a ID_SERVICE
    private Long category;  // Corresponde a ID_CATEGORY

    public ServiceCategoryId() {}

    public ServiceCategoryId(Long service, Long category) {
        this.service = service;
        this.category = category;
    }

    // equals() y hashCode() son obligatorios para que funcione la clave compuesta
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceCategoryId)) return false;
        ServiceCategoryId that = (ServiceCategoryId) o;
        return Objects.equals(service, that.service) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, category);
    }
}
