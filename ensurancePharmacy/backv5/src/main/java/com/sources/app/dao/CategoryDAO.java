package com.sources.app.dao;

import com.sources.app.entities.Category;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar entidades {@link Category}.
 * Esta clase proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar)
 * en registros de Categorías, que representan categorías de productos. Utiliza Hibernate para interacciones con la base de datos.
 */
public class CategoryDAO {

    /**
     * Crea un nuevo registro de Categoría en la base de datos.
     *
     * @param name El nombre para la nueva categoría.
     * @return La entidad {@link Category} recién creada, o null si ocurrió un error.
     */
    public Category create(String name) {
        Transaction tx = null;
        Category category = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            category = new Category();
            category.setName(name);

            session.save(category);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return category;
    }

    /**
     * Recupera todos los registros de Categoría de la base de datos.
     *
     * @return Una lista de todas las entidades {@link Category}, o null si ocurrió un error.
     */
    public List<Category> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Category> query = session.createQuery("FROM Category", Category.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera un registro de Categoría específico por su identificador único.
     *
     * @param id El ID de la Categoría a recuperar.
     * @return La entidad {@link Category} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public Category getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Category.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro de Categoría existente en la base de datos.
     *
     * @param category La entidad {@link Category} con información actualizada (típicamente el nombre). El ID debe coincidir con un registro existente.
     * @return La entidad {@link Category} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public Category update(Category category) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(category);
            tx.commit();
            return category;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }
}
