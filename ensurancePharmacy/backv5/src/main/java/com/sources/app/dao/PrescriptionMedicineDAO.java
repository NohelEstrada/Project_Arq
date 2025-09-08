package com.sources.app.dao;

import com.sources.app.entities.PrescriptionMedicine;
import com.sources.app.entities.PrescriptionMedicineId;
import com.sources.app.entities.Prescription;
import com.sources.app.entities.Medicine;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) para gestionar entidades {@link PrescriptionMedicine}.
 * Esta clase proporciona métodos para realizar operaciones CRUD en registros PrescriptionMedicine,
 * que representan la relación entre una {@link Prescription} y un {@link Medicine},
 * incluyendo detalles como dosis y frecuencia. Utiliza Hibernate para interacciones con la base de datos.
 */
public class PrescriptionMedicineDAO {

    private static final Logger LOGGER = Logger.getLogger(PrescriptionMedicineDAO.class.getName());

    /**
     * Crea un nuevo registro PrescriptionMedicine en la base de datos.
     * Este método vincula un medicamento específico a una receta con detalles de dosificación.
     *
     * @param prescription La entidad {@link Prescription} a enlazar.
     * @param medicine La entidad {@link Medicine} a enlazar.
     * @param dosis La dosis del medicamento prescrito.
     * @param frecuencia La frecuencia con la que debe tomarse el medicamento.
     * @param duracion La duración durante la cual debe tomarse el medicamento.
     * @return La entidad {@link PrescriptionMedicine} recién creada, o null si ocurrió un error.
     */
    public PrescriptionMedicine create(Prescription prescription, Medicine medicine,
                                       Double dosis, Double frecuencia, Double duracion) {
        Transaction tx = null;
        PrescriptionMedicine pm = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            pm = new PrescriptionMedicine();
            pm.setPrescription(prescription);
            pm.setMedicine(medicine);
            pm.setDosis(dosis);
            pm.setFrecuencia(frecuencia);
            pm.setDuracion(duracion);

            session.persist(pm);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, "Error creating PrescriptionMedicine (prescriptionNull=" + (prescription == null) + ", medicineNull=" + (medicine == null) + ", dosis=" + dosis + ", frecuencia=" + frecuencia + ", duracion=" + duracion + ")", e);
        }
        return pm;
    }

    /**
     * Recupera todos los registros PrescriptionMedicine de la base de datos.
     *
     * @return Una lista de todas las entidades {@link PrescriptionMedicine}, o null si ocurrió un error.
     */
    public List<PrescriptionMedicine> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<PrescriptionMedicine> query = session.createQuery("FROM PrescriptionMedicine", PrescriptionMedicine.class);
            return query.list();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching all PrescriptionMedicine records", e);
            return null;
        }
    }

    // READ BY ID (clave compuesta)
    public PrescriptionMedicine getById(PrescriptionMedicineId id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(PrescriptionMedicine.class, id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching PrescriptionMedicine by composite id=" + id, e);
            return null;
        }
    }

    // UPDATE
    public PrescriptionMedicine update(PrescriptionMedicine pm) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(pm);
            tx.commit();
            return pm;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, "Error updating PrescriptionMedicine (entity null=" + (pm == null) + ")", e);
            return null;
        }
    }
}
