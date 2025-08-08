package com.sources.app.dao;

import com.sources.app.entities.Service;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades Service (Servicio Médico).
 * Representa un servicio ofrecido por un hospital, clasificado por categoría y subcategoría.
 * Proporciona métodos para CRUD y para obtener servicios con sus detalles asociados (hospital, categorías).
 */
public class ServiceDAO {

    /**
     * Crea un nuevo servicio en la base de datos.
     * Se espera que el objeto Service proporcionado ya contenga las referencias
     * a las entidades Hospital, Category (principal) y Category (subcategoría).
     *
     * @param service El objeto Service a crear, con sus relaciones ya establecidas.
     * @return El objeto Service creado, o null si ocurre un error.
     */
    public Service create(Service service) {
        Transaction tx = null;
        Service createdService = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            session.save(service);
            tx.commit();
            createdService = service;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return createdService;
    }

    /**
     * Busca un servicio por su ID único, sin cargar sus relaciones asociadas (carga perezosa).
     *
     * @param id El ID del servicio a buscar.
     * @return El objeto Service encontrado (sin detalles cargados), o null si no se encuentra o si ocurre un error.
     */
    public Service findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Service.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca un servicio por su ID único y carga explícitamente sus relaciones asociadas
     * (hospital, categoría principal, subcategoría) mediante JOIN FETCH.
     *
     * @param id El ID del servicio a buscar.
     * @return El objeto Service encontrado con sus detalles cargados, o null si no se encuentra o si ocurre un error.
     */
    public Service findByIdWithDetails(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select s from Service s " +
                    "join fetch s.hospital h " +
                    "join fetch s.category c " +
                    "join fetch s.subcategory sc " +
                    "where s.idService = :id";
            Query<Service> query = session.createQuery(hql, Service.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todos los servicios de la base de datos, sin cargar sus relaciones asociadas (carga perezosa).
     *
     * @return Una lista de todos los objetos Service (sin detalles cargados), o null si ocurre un error.
     */
    public List<Service> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Service> query = session.createQuery("FROM Service", Service.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todos los servicios de la base de datos, cargando explícitamente sus relaciones asociadas
     * (hospital, categoría principal, subcategoría) mediante JOIN FETCH.
     *
     * @return Una lista de todos los objetos Service con sus detalles cargados, o null si ocurre un error.
     */
    public List<Service> findAllWithDetails() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select s from Service s " +
                    "join fetch s.hospital h " +
                    "join fetch s.category c " +
                    "join fetch s.subcategory sc";
            Query<Service> query = session.createQuery(hql, Service.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un servicio existente en la base de datos.
     * Se asume que el objeto Service proporcionado ya tiene configuradas las asociaciones necesarias
     * (Hospital, Category, Subcategory).
     *
     * @param service El objeto Service con los datos actualizados.
     * @return El objeto Service actualizado, o null si ocurre un error.
     */
    public Service update(Service service) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(service);
            tx.commit();
            return service;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}
