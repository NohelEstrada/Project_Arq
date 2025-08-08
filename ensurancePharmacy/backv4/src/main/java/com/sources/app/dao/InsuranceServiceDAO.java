package com.sources.app.dao;

import com.sources.app.entities.InsuranceService;
import com.sources.app.entities.Category;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades InsuranceService (Servicio de Seguro).
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre los servicios de seguro,
 * así como búsquedas por categoría, subcategoría e ID externo.
 */
public class InsuranceServiceDAO {

    /**
     * Constructor por defecto para InsuranceServiceDAO.
     */
    public InsuranceServiceDAO() {}

    /**
     * Crea un nuevo servicio de seguro en la base de datos.
     *
     * @param service El objeto InsuranceService a crear.
     * @return El objeto InsuranceService creado, o null si ocurre un error.
     */
    public InsuranceService create(InsuranceService service) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            session.save(service);
            
            tx.commit();
            return service;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Recupera todos los servicios de seguro de la base de datos.
     *
     * @return Una lista de todos los objetos InsuranceService, o null si ocurre un error.
     */
    public List<InsuranceService> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<InsuranceService> query = session.createQuery("FROM InsuranceService", InsuranceService.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca servicios de seguro por su categoría principal.
     *
     * @param category La categoría principal (objeto Category).
     * @return Una lista de objetos InsuranceService que pertenecen a esa categoría, o null si ocurre un error.
     */
    public List<InsuranceService> findByCategory(Category category) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<InsuranceService> query = session.createQuery(
                "FROM InsuranceService WHERE category = :category", 
                InsuranceService.class
            );
            query.setParameter("category", category);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca servicios de seguro por su subcategoría.
     *
     * @param subcategory La subcategoría (objeto Category).
     * @return Una lista de objetos InsuranceService que pertenecen a esa subcategoría, o null si ocurre un error.
     */
    public List<InsuranceService> findBySubcategory(Category subcategory) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<InsuranceService> query = session.createQuery(
                "FROM InsuranceService WHERE subcategory = :subcategory", 
                InsuranceService.class
            );
            query.setParameter("subcategory", subcategory);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca un servicio de seguro por su ID único.
     *
     * @param idService El ID del servicio a buscar.
     * @return El objeto InsuranceService encontrado, o null si no se encuentra o si ocurre un error.
     */
    public InsuranceService findById(Long idService) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(InsuranceService.class, idService);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un servicio de seguro existente en la base de datos.
     *
     * @param service El objeto InsuranceService a actualizar.
     * @return El objeto InsuranceService actualizado, o null si ocurre un error.
     */
    public InsuranceService update(InsuranceService service) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            session.update(service);
            
            tx.commit();
            return service;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Eliminar un servicio de seguro existente en la base de datos.
     *
     * @param idService El ID del servicio a eliminar.
     * @return true si el servicio se elimina correctamente, false si ocurre un error.
     */
    public boolean delete(Long idService) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            InsuranceService service = session.get(InsuranceService.class, idService);
            if (service != null) {
                session.delete(service);
                tx.commit();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Busca servicios de seguro por su ID externo.
     *
     * @param externalId El ID externo del servicio a buscar.
     * @return Una lista de objetos InsuranceService que coinciden con el ID externo, o null si ocurre un error.
     */
    public List<InsuranceService> findByExternalId(String externalId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<InsuranceService> query = session.createQuery(
                "FROM InsuranceService WHERE externalId = :externalId",
                InsuranceService.class
            );
            query.setParameter("externalId", externalId);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 