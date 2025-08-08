package com.sources.app.dao;

import com.sources.app.entities.Prescription;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.User;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las operaciones CRUD de la entidad {@link Prescription}.
 * Utiliza Hibernate para interactuar con la base de datos.
 */
public class PrescriptionDAO {

    /**
     * Crea una nueva prescripción en la base de datos.
     *
     * @param hospital El hospital asociado.
     * @param user El usuario (paciente) asociado.
     * @param approved El estado de aprobación inicial ('1' o '0').
     * @return La entidad Prescription creada y persistida, o null si ocurre un error.
     */
    public Prescription create(Hospital hospital, User user, Character approved) {
        Transaction tx = null;
        Prescription prescription = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            prescription = new Prescription();
            prescription.setHospital(hospital);
            prescription.setUser(user);
            prescription.setApproved(approved);

            session.save(prescription);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return prescription;
    }

    /**
     * Obtiene todas las prescripciones registradas.
     *
     * @return Una lista de todas las entidades Prescription, o null si ocurre un error.
     */
    public List<Prescription> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Prescription> query = session.createQuery("FROM Prescription", Prescription.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene una prescripción específica por su identificador único.
     *
     * @param id El ID de la prescripción a buscar.
     * @return La entidad Prescription encontrada, o null si no existe o si ocurre un error.
     */
    public Prescription getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Prescription.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza los datos de una prescripción existente.
     *
     * @param prescription La entidad Prescription con los datos actualizados (debe tener un ID válido).
     * @return La entidad Prescription actualizada, o null si ocurre un error durante la actualización.
     */
    public Prescription update(Prescription prescription) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(prescription);
            tx.commit();
            return prescription;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }
}
