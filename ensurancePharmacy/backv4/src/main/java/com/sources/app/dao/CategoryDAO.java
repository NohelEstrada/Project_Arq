package com.sources.app.dao;

import com.sources.app.entities.Category;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades Category (Categoría).
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar) sobre las categorías.
 */
public class CategoryDAO {

    /**
     * Constructor por defecto para CategoryDAO.
     */
    public CategoryDAO() {}

    /**
     * Crea una nueva categoría en la base de datos.
     *
     * @param name Nombre de la categoría.
     * @param enabled Estado de habilitación (1 habilitado, 0 deshabilitado).
     * @return El objeto Category creado, o null si ocurre un error.
     */
    public Category create(String name, Integer enabled) {
        Transaction tx = null;
        Category category = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            category = new Category();
            category.setName(name);
            category.setEnabled(enabled);

            session.save(category);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return category;
    }

    /**
     * Busca una categoría por su ID único.
     *
     * @param id El ID de la categoría a buscar.
     * @return El objeto Category encontrado, o null si no se encuentra o si ocurre un error.
     */
    public Category findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Category.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todas las categorías de la base de datos.
     *
     * @return Una lista de todos los objetos Category, o null si ocurre un error.
     */
    public List<Category> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Category> query = session.createQuery("FROM Category", Category.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza una categoría existente en la base de datos.
     *
     * @param category El objeto Category con los datos actualizados.
     * @return El objeto Category actualizado, o null si ocurre un error.
     */
    public Category update(Category category) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(category);
            tx.commit();
            return category;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}
