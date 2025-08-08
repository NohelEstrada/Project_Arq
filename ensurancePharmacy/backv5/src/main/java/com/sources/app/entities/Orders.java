package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Entidad que representa un pedido realizado en el sistema.
 * Mapea a la tabla "ORDERS". Contiene el estado del pedido y el usuario asociado.
 */
@Entity
@Table(name = "ORDERS")
public class Orders {

    /** Identificador único del pedido. Generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ORDER")
    private Long idOrder;

    /** Estado actual del pedido (e.g., "PENDING", "PROCESSING", "COMPLETED", "CANCELLED"). */
    @Column(name = "STATUS")
    private String status;

    /** Usuario que realizó el pedido. Cargado de forma EAGER. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Orders() {
    }

    /**
     * Constructor para crear un nuevo pedido especificando su estado inicial.
     * El usuario asociado debe establecerse por separado mediante setUser().
     *
     * @param status El estado inicial del pedido.
     */
    public Orders(String status) {
        this.status = status;
    }

    // Getters y setters
    public Long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(Long idOrder) {
        this.idOrder = idOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
