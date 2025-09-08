package com.sources.app.dao;

import com.sources.app.entities.Subcategory;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) para gestionar entidades {@link Subcategory}.
 * Proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar)
 * en registros de Subcategorías utilizando Hibernate.
 */
public class SubcategoryDAO {

    private static final Logger LOGGER = Logger.getLogger(SubcategoryDAO.class.getName());

    /**
     * Crea un nuevo registro de Subcategoría en la base de datos.
     *
     * @param name El nombre para la nueva subcategoría.
     * @return La entidad {@link Subcategory} recién creada, o null si ocurrió un error.
     */
    public Subcategory create(String name) {
        Transaction tx = null;
        Subcategory subcategory = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            subcategory = new Subcategory();
            subcategory.setName(name);

            session.persist(subcategory);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, "Error creating Subcategory (name=" + name + ")", e);
        }
        return subcategory;
    }

    /**
     * Recupera todos los registros de Subcategoría de la base de datos.
     *
     * @return Una lista de todas las entidades {@link Subcategory}, o null si ocurrió un error.
     */
    public List<Subcategory> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Subcategory> query = session.createQuery("FROM Subcategory", Subcategory.class);
            return query.list();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching all Subcategory records", e);
            return null;
        }
    }

    /**
     * Recupera un registro de Subcategoría específico por su identificador único.
     *
     * @param id El ID de la Subcategoría a recuperar.
     * @return La entidad {@link Subcategory} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public Subcategory getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Subcategory.class, id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching Subcategory by id=" + id, e);
            return null;
        }
    }

    /**
     * Actualiza un registro de Subcategoría existente en la base de datos.
     *
     * @param subcategory La entidad {@link Subcategory} con información actualizada (típicamente el nombre). El ID debe coincidir con un registro existente.
     * @return La entidad {@link Subcategory} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public Subcategory update(Subcategory subcategory) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(subcategory);
            tx.commit();
            return subcategory;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, "Error updating Subcategory (entity null=" + (subcategory == null) + ")", e);
            return null;
        }
    }
}
