package com.sources.app.dao;

import com.sources.app.entities.Prescription;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.User;
import com.sources.app.entities.Medicine;
import com.sources.app.entities.Pharmacy;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades Prescription (Receta).
 * Representa una receta médica emitida por un hospital para un usuario, asociada a un medicamento y una farmacia.
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar) y búsqueda por usuario.
 */
public class PrescriptionDAO {

    /**
     * Crea una nueva receta en la base de datos.
     *
     * @param idHospital ID del hospital que emite la receta.
     * @param idUser ID del usuario al que se le emite la receta.
     * @param idMedicine ID del medicamento recetado.
     * @param idPharmacy ID de la farmacia donde se puede surtir (o se surtió) la receta.
     * @param prescriptionDate Fecha de emisión de la receta.
     * @param total Monto total del medicamento.
     * @param copay Monto del copago (si aplica).
     * @param prescriptionComment Comentarios adicionales sobre la receta.
     * @param secured Indicador si la receta está cubierta por seguro (1 sí, 0 no).
     * @param auth Código de autorización (si aplica).
     * @return El objeto Prescription creado, o null si ocurre un error (p. ej., alguna entidad relacionada no existe).
     */
    public Prescription create(Long idHospital, Long idUser, Long idMedicine, Long idPharmacy,
                               Date prescriptionDate, BigDecimal total, BigDecimal copay,
                               String prescriptionComment, Integer secured, String auth) {
        Transaction tx = null;
        Prescription prescription = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Recuperar las entidades relacionadas
            Hospital hospital = session.get(Hospital.class, idHospital);
            User user = session.get(User.class, idUser);
            Medicine medicine = session.get(Medicine.class, idMedicine);
            Pharmacy pharmacy = session.get(Pharmacy.class, idPharmacy);

            if(hospital == null || user == null || medicine == null || pharmacy == null) {
                throw new RuntimeException("Alguna entidad relacionada no fue encontrada.");
            }

            prescription = new Prescription();
            prescription.setHospital(hospital);
            prescription.setUser(user);
            prescription.setMedicine(medicine);
            prescription.setPharmacy(pharmacy);
            prescription.setPrescriptionDate(prescriptionDate);
            prescription.setTotal(total);
            prescription.setCopay(copay);
            prescription.setPrescriptionComment(prescriptionComment);
            prescription.setSecured(secured);
            prescription.setAuth(auth);

            session.save(prescription);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return prescription;
    }

    /**
     * Busca todas las recetas asociadas a un ID de usuario específico.
     *
     * @param idUser El ID del usuario cuyas recetas se quieren buscar.
     * @return Una lista de objetos Prescription asociados al usuario, o null si ocurre un error.
     */
    public List<Prescription> findByUserId(Long idUser) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Prescription> query = session.createQuery(
                    "FROM Prescription p WHERE p.user.idUser = :idUser", Prescription.class
            );
            query.setParameter("idUser", idUser);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Busca una receta por su ID único.
     *
     * @param id El ID de la receta a buscar.
     * @return El objeto Prescription encontrado, o null si no se encuentra o si ocurre un error.
     */
    public Prescription findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Prescription.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todas las recetas de la base de datos.
     *
     * @return Una lista de todos los objetos Prescription, o null si ocurre un error.
     */
    public List<Prescription> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Prescription> query = session.createQuery("FROM Prescription", Prescription.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza una receta existente en la base de datos.
     *
     * @param prescription El objeto Prescription con los datos actualizados.
     * @return El objeto Prescription actualizado, o null si ocurre un error.
     */
    public Prescription update(Prescription prescription) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(prescription);
            tx.commit();
            return prescription;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}
