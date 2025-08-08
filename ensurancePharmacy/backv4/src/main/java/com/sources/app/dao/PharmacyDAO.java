package com.sources.app.dao;

import com.sources.app.entities.Pharmacy;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades Pharmacy (Farmacia).
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar) sobre las farmacias.
 */
public class PharmacyDAO {

    /**
     * Crea una nueva farmacia en la base de datos.
     *
     * @param name Nombre de la farmacia.
     * @param address Dirección de la farmacia.
     * @param phone Número de teléfono de la farmacia.
     * @param email Correo electrónico de la farmacia.
     * @param enabled Estado de habilitación (1 habilitado, 0 deshabilitado).
     * @return El objeto Pharmacy creado, o null si ocurre un error.
     */
    public Pharmacy create(String name, String address, Long phone, String email, Integer enabled) {
        Transaction tx = null;
        Pharmacy pharmacy = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            pharmacy = new Pharmacy();
            pharmacy.setName(name);
            pharmacy.setAddress(address);
            pharmacy.setPhone(phone);
            pharmacy.setEmail(email);
            pharmacy.setEnabled(enabled);

            session.save(pharmacy);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    rbEx.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return pharmacy;
    }

    /**
     * Busca una farmacia por su ID único.
     *
     * @param id El ID de la farmacia a buscar.
     * @return El objeto Pharmacy encontrado, o null si no se encuentra o si ocurre un error.
     */
    public Pharmacy findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Pharmacy.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todas las farmacias de la base de datos.
     *
     * @return Una lista de todos los objetos Pharmacy, o null si ocurre un error.
     */
    public List<Pharmacy> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Pharmacy> query = session.createQuery("FROM Pharmacy", Pharmacy.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza una farmacia existente en la base de datos.
     *
     * @param pharmacy El objeto Pharmacy con los datos actualizados.
     * @return El objeto Pharmacy actualizado, o null si ocurre un error.
     */
    public Pharmacy update(Pharmacy pharmacy) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.update(pharmacy);
            tx.commit();
            return pharmacy;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    rbEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
