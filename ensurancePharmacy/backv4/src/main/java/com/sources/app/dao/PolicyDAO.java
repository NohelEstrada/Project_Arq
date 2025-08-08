package com.sources.app.dao;

import com.sources.app.entities.Policy;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades Policy (Póliza).
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre las pólizas.
 */
public class PolicyDAO {

    /**
     * Crea una nueva póliza en la base de datos.
     *
     * @param percentage Porcentaje de cobertura de la póliza.
     * @param creation_date Fecha de creación de la póliza.
     * @param exp_date Fecha de expiración de la póliza.
     * @param cost Costo de la póliza.
     * @param enabled Estado de habilitación de la póliza (1 habilitado, 0 deshabilitado).
     * @return El objeto Policy creado, o null si ocurre un error.
     */
    public Policy create(Float percentage, Date creation_date, Date exp_date, Float cost, Integer enabled) {
        Transaction tx = null;
        Policy policy = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Creamos una instancia de Policy y asignamos los valores
            policy = new Policy();
            policy.setPercentage(percentage);
            policy.setCreationDate(creation_date);
            policy.setExpDate(exp_date);
            policy.setCost(cost);
            policy.setEnabled(enabled);

            // Guardamos la policy en la base de datos
            session.save(policy);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            // Verificamos si la transacción está activa antes de hacer rollback
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        }
        return policy;
    }


    /**
     * Busca una póliza por su ID único.
     *
     * @param idPolicy El ID de la póliza a buscar.
     * @return El objeto Policy encontrado, o null si no se encuentra o si ocurre un error.
     */
    public Policy find(Long idPolicy) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Policy.class, idPolicy);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todas las pólizas de la base de datos.
     *
     * @return Una lista de todos los objetos Policy, o null si ocurre un error.
     */
    public List<Policy> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Policy> query = session.createQuery("FROM Policy", Policy.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza una póliza existente en la base de datos.
     *
     * @param policy El objeto Policy con los datos actualizados.
     * @return El objeto Policy actualizado, o null si ocurre un error.
     */
    public Policy update(Policy policy) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(policy);
            tx.commit();
            return policy;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Elimina una póliza de la base de datos por su ID.
     *
     * @param idPolicy El ID de la póliza a eliminar.
     * @return true si la póliza fue eliminada con éxito, false en caso contrario.
     */
    public boolean delete(Long idPolicy) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Policy policy = session.get(Policy.class, idPolicy);
            if (policy != null) {
                session.delete(policy);
                tx.commit();
                return true;
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return false;
    }
}
