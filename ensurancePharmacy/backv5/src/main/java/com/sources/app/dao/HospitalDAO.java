package com.sources.app.dao;

import com.sources.app.entities.Hospital;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar entidades {@link Hospital}.
 * Esta clase proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar)
 * en registros de Hospitales utilizando Hibernate para interacciones con la base de datos.
 */
public class HospitalDAO {

    /**
     * Crea un nuevo registro de Hospital en la base de datos.
     *
     * @param name    El nombre del hospital.
     * @param phone   El número de teléfono del hospital.
     * @param email   La dirección de correo electrónico del hospital.
     * @param address La dirección física del hospital.
     * @param enabled El estado habilitado del hospital ('1' para habilitado, '0' para deshabilitado).
     * @return La entidad {@link Hospital} recién creada, o null si ocurrió un error.
     */
    public Hospital create(String name, String phone, String email, String address, Character enabled) {
        Transaction tx = null;
        Hospital hospital = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            hospital = new Hospital();
            hospital.setName(name);
            hospital.setPhone(phone);
            hospital.setEmail(email);
            hospital.setAddress(address);
            hospital.setEnabled(enabled);

            session.save(hospital);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return hospital;
    }

    /**
     * Recupera todos los registros de Hospital de la base de datos.
     *
     * @return Una lista de todas las entidades {@link Hospital}, o null si ocurrió un error.
     */
    public List<Hospital> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Hospital> query = session.createQuery("FROM Hospital", Hospital.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera un registro de Hospital específico por su identificador único.
     *
     * @param id El ID del Hospital a recuperar.
     * @return La entidad {@link Hospital} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public Hospital getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Hospital.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro de Hospital existente en la base de datos.
     *
     * @param hospital La entidad {@link Hospital} con información actualizada. El ID debe coincidir con un registro existente.
     * @return La entidad {@link Hospital} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public Hospital update(Hospital hospital) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(hospital);
            tx.commit();
            return hospital;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }
}
