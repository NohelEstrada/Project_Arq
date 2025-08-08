package com.sources.app.dao;

import com.sources.app.entities.MedicinePres;
import com.sources.app.entities.MedicinePresId;
import com.sources.app.entities.Prescription;
import com.sources.app.entities.Medicine;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar la entidad de relación MedicinePres.
 * Representa la asociación entre Medicamentos (Medicine) y Recetas (Prescription).
 * Indica qué medicamentos están incluidos en qué recetas.
 * Proporciona métodos para crear, buscar y actualizar estas relaciones.
 */
public class MedicinePresDAO {

    /**
     * Crea una nueva relación entre un medicamento y una receta.
     *
     * @param idPrescription El ID de la receta.
     * @param idMedicine El ID del medicamento.
     * @return El objeto MedicinePres creado, o null si ocurre un error (p. ej., la receta o el medicamento no existen).
     */
    public MedicinePres create(Long idPrescription, Long idMedicine) {
        Transaction tx = null;
        MedicinePres medPres = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Recuperar las entidades Prescription y Medicine
            Prescription prescription = session.get(Prescription.class, idPrescription);
            Medicine medicine = session.get(Medicine.class, idMedicine);
            if (prescription == null || medicine == null) {
                throw new RuntimeException("Prescription or Medicine not found");
            }

            medPres = new MedicinePres();
            medPres.setPrescription(prescription);
            medPres.setMedicine(medicine);

            session.save(medPres);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return medPres;
    }

    /**
     * Busca una relación MedicinePres por su ID compuesto (ID de receta e ID de medicamento).
     *
     * @param idPrescription El ID de la receta.
     * @param idMedicine El ID del medicamento.
     * @return El objeto MedicinePres encontrado, o null si no se encuentra o si ocurre un error.
     */
    public MedicinePres findById(Long idPrescription, Long idMedicine) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            MedicinePresId key = new MedicinePresId(idPrescription, idMedicine);
            return session.get(MedicinePres.class, key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todas las relaciones MedicinePres de la base de datos.
     *
     * @return Una lista de todos los objetos MedicinePres, o null si ocurre un error.
     */
    public List<MedicinePres> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MedicinePres> query = session.createQuery("FROM MedicinePres", MedicinePres.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza una relación MedicinePres existente.
     * Nota: Dado que la clave primaria es compuesta y define la relación,
     * generalmente no hay atributos adicionales en MedicinePres para actualizar.
     * Este método podría ser útil si se añaden más campos a la tabla de unión.
     *
     * @param medPres El objeto MedicinePres con los datos actualizados.
     * @return El objeto MedicinePres actualizado, o null si ocurre un error.
     */
    public MedicinePres update(MedicinePres medPres) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(medPres);
            tx.commit();
            return medPres;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}
