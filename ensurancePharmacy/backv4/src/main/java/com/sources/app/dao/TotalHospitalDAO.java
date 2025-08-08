package com.sources.app.dao;

import com.sources.app.entities.TotalHospital;
import com.sources.app.entities.Hospital;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades de TotalHospital.
 * Representa los totales diarios para cada hospital.
 * Proporciona métodos para crear, buscar y actualizar estos totales.
 */
public class TotalHospitalDAO {

    /**
     * Crea un nuevo registro de total diario para un hospital específico.
     *
     * @param idHospital El ID del hospital para el cual se registra el total.
     * @param totalDate La fecha a la que corresponde el total.
     * @param total El monto total registrado para esa fecha.
     * @return El objeto TotalHospital creado, o null si ocurre un error (p. ej., el hospital no existe).
     */
    public TotalHospital create(Long idHospital, Date totalDate, BigDecimal total) {
        Transaction tx = null;
        TotalHospital th = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Recuperar la entidad Hospital a partir del id
            Hospital hospital = session.get(Hospital.class, idHospital);
            if (hospital == null) {
                throw new RuntimeException("Hospital no encontrado para id: " + idHospital);
            }

            th = new TotalHospital();
            th.setHospital(hospital);  // Se asigna la entidad Hospital
            th.setTotalDate(totalDate);
            th.setTotal(total);

            session.save(th);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return th;
    }

    /**
     * Busca un registro de TotalHospital por su ID único.
     *
     * @param id El ID del registro TotalHospital a buscar.
     * @return El objeto TotalHospital encontrado, o null si no se encuentra o si ocurre un error.
     */
    public TotalHospital findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(TotalHospital.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todos los registros de TotalHospital de la base de datos.
     *
     * @return Una lista de todos los objetos TotalHospital, o null si ocurre un error.
     */
    public List<TotalHospital> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<TotalHospital> query = session.createQuery("FROM TotalHospital", TotalHospital.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro existente de TotalHospital en la base de datos.
     *
     * @param th El objeto TotalHospital con los datos actualizados.
     * @return El objeto TotalHospital actualizado, o null si ocurre un error.
     */
    public TotalHospital update(TotalHospital th) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(th);
            tx.commit();
            return th;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}
