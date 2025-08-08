package com.sources.app.dao;

import com.sources.app.entities.Policy;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar entidades {@link Policy}.
 * Esta clase proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en registros de Pólizas utilizando Hibernate para interacciones con la base de datos.
 */
public class PolicyDAO {

    /**
     * Crea un nuevo registro de Póliza (Policy) en la base de datos.
     *
     * @param percentage El porcentaje de cobertura ofrecido por la póliza.
     * @param enabled    El estado habilitado de la póliza ('1' para habilitado, '0' para deshabilitado).
     * @return La entidad {@link Policy} recién creada, o null si ocurrió un error.
     */
    public Policy create(Double percentage, Character enabled) {
        Transaction tx = null;
        Policy policy = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            policy = new Policy();
            policy.setPercentage(percentage);
            policy.setEnabled(enabled);

            session.save(policy);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return policy;
    }

    /**
     * Recupera todos los registros de Póliza (Policy) de la base de datos.
     *
     * @return Una lista de todas las entidades {@link Policy}, o null si ocurrió un error.
     */
    public List<Policy> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Policy> query = session.createQuery("FROM Policy", Policy.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera un registro de Póliza (Policy) específico por su identificador único.
     *
     * @param id El ID de la Póliza (Policy) a recuperar.
     * @return La entidad {@link Policy} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public Policy getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Policy.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro de Póliza (Policy) existente en la base de datos.
     *
     * @param policy La entidad {@link Policy} con información actualizada. El ID debe coincidir con un registro existente.
     * @return La entidad {@link Policy} actualizada, o null si la actualización falló o ocurrió un error.
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
}
