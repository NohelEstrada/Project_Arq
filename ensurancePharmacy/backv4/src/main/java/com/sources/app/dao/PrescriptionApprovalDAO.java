package com.sources.app.dao;

import com.sources.app.entities.PrescriptionApproval;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades PrescriptionApproval (Aprobación de Receta).
 * Representa el registro de aprobación de una receta médica.
 * Proporciona métodos para guardar y buscar aprobaciones de recetas.
 */
public class PrescriptionApprovalDAO {

    /**
     * Guarda un nuevo registro de aprobación de receta en la base de datos.
     *
     * @param approval El objeto PrescriptionApproval a guardar.
     * @return El objeto PrescriptionApproval guardado, o null si ocurre un error.
     */
    public PrescriptionApproval save(PrescriptionApproval approval) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(approval);
            tx.commit();
            return approval;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca una aprobación de receta por su ID único.
     *
     * @param id El ID de la aprobación a buscar.
     * @return El objeto PrescriptionApproval encontrado, o null si no se encuentra o si ocurre un error.
     */
    public PrescriptionApproval findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(PrescriptionApproval.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todos los registros de aprobación de recetas de la base de datos, ordenados por fecha de aprobación descendente.
     *
     * @return Una lista de todos los objetos PrescriptionApproval, o null si ocurre un error.
     */
    public List<PrescriptionApproval> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<PrescriptionApproval> query = session.createQuery(
                "FROM PrescriptionApproval ORDER BY approvalDate DESC", 
                PrescriptionApproval.class
            );
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<PrescriptionApproval> findByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<PrescriptionApproval> query = session.createQuery(
                "FROM PrescriptionApproval WHERE idUser = :userId ORDER BY approvalDate DESC", 
                PrescriptionApproval.class
            );
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 