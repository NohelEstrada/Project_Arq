package com.sources.app.dao;

import com.sources.app.entities.Hospital;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades Hospital.
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar) sobre los hospitales.
 */
public class HospitalDAO {

    /**
     * Constructor por defecto para HospitalDAO.
     */
    public HospitalDAO() {}

    /**
     * Crea un nuevo hospital en la base de datos.
     *
     * @param name Nombre del hospital.
     * @param address Dirección del hospital.
     * @param phone Número de teléfono del hospital.
     * @param email Correo electrónico del hospital.
     * @param enabled Estado de habilitación (1 habilitado, 0 deshabilitado).
     * @param port Puerto del servidor API del hospital.
     * @return El objeto Hospital creado, o null si ocurre un error.
     */
    public Hospital create(String name, String address, Long phone, String email, Integer enabled, String port) {
        Transaction tx = null;
        Hospital hospital = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            hospital = new Hospital();
            hospital.setName(name);
            hospital.setAddress(address);
            hospital.setPhone(phone);
            hospital.setEmail(email);
            hospital.setEnabled(enabled);
            hospital.setPort(port);

            session.save(hospital);
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
        return hospital;
    }

    /**
     * Crea un nuevo hospital en la base de datos sin especificar el puerto.
     * Este método se mantiene por compatibilidad.
     *
     * @param name Nombre del hospital.
     * @param address Dirección del hospital.
     * @param phone Número de teléfono del hospital.
     * @param email Correo electrónico del hospital.
     * @param enabled Estado de habilitación (1 habilitado, 0 deshabilitado).
     * @return El objeto Hospital creado, o null si ocurre un error.
     */
    public Hospital create(String name, String address, Long phone, String email, Integer enabled) {
        return create(name, address, phone, email, enabled, null);
    }

    /**
     * Busca un hospital por su ID único.
     *
     * @param id El ID del hospital a buscar.
     * @return El objeto Hospital encontrado, o null si no se encuentra o si ocurre un error.
     */
    public Hospital findById(Long id) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            return session.get(Hospital.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Recupera todos los hospitales de la base de datos.
     *
     * @return Una lista de todos los objetos Hospital, o null si ocurre un error.
     */
    public List<Hospital> findAll() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Hospital> query = session.createQuery("FROM Hospital", Hospital.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Actualiza un hospital existente en la base de datos.
     *
     * @param hospital El objeto Hospital con los datos actualizados.
     * @return El objeto Hospital actualizado, o null si ocurre un error.
     */
    public Hospital update(Hospital hospital) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.update(hospital);
            tx.commit();
            return hospital;
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
