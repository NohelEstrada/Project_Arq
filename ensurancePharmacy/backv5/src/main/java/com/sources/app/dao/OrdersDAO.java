package com.sources.app.dao;

import com.sources.app.entities.Orders;
import com.sources.app.entities.User;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) para gestionar entidades {@link Orders}.
 * Esta clase proporciona métodos para realizar operaciones CRUD en Pedidos (Orders),
 * que representan pedidos de clientes en el sistema. Utiliza Hibernate para interacciones con la base de datos.
 */
public class OrdersDAO {

    private static final Logger LOGGER = Logger.getLogger(OrdersDAO.class.getName());

    /**
     * Crea un nuevo Pedido (Order) en la base de datos, asociándolo a un Usuario (User) existente.
     *
     * @param status El estado inicial del pedido (p. ej., "PENDIENTE", "PROCESANDO").
     * @param idUser El ID del {@link User} que realiza el pedido.
     * @return La entidad {@link Orders} recién creada, o null si el usuario no existe o ocurre un error.
     */
    public Orders create(String status, Long idUser) {
        Transaction tx = null;
        Orders order = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            order = new Orders();
            order.setStatus(status);
            User user = session.get(User.class, idUser);
            order.setUser(user);

            session.persist(order);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, "Error creating Orders (status=" + status + ", idUser=" + idUser + ")", e);
        }
        return order;
    }

    /**
     * Recupera todos los registros de Pedido (Order) de la base de datos.
     *
     * @return Una lista de todas las entidades {@link Orders}, o null si ocurrió un error.
     */
    public List<Orders> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Orders> query = session.createQuery("FROM Orders", Orders.class);
            return query.list();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching all Orders records", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera un registro de Pedido (Order) específico por su identificador único.
     *
     * @param id El ID del Pedido (Order) a recuperar.
     * @return La entidad {@link Orders} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public Orders getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Orders.class, id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching Orders by id=" + id, e);
            return null;
        }
    }

    /**
     * Actualiza un registro de Pedido (Order) existente en la base de datos.
     *
     * @param order La entidad {@link Orders} con información actualizada. El ID debe coincidir con un registro existente.
     * @return La entidad {@link Orders} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public Orders update(Orders order) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(order);
            tx.commit();
            return order;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, "Error updating Orders (entity null=" + (order == null) + ")", e);
            return null;
        }
    }
}
