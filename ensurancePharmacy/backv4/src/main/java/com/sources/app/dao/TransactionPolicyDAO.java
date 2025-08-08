package com.sources.app.dao;

import com.sources.app.entities.TransactionPolicy;
import com.sources.app.entities.Policy;
import com.sources.app.entities.User;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades TransactionPolicy.
 * Representa las transacciones asociadas al pago de pólizas por parte de los usuarios.
 * Proporciona métodos para crear, buscar y actualizar estas transacciones.
 */
public class TransactionPolicyDAO {

    /**
     * Crea un nuevo registro de transacción de póliza.
     *
     * @param idPolicy El ID de la póliza asociada a la transacción.
     * @param idUser El ID del usuario que realiza la transacción.
     * @param payDate La fecha en que se realizó el pago.
     * @param total El monto total de la transacción.
     * @return El objeto TransactionPolicy creado, o null si ocurre un error (p. ej., la póliza o el usuario no existen).
     */
    public TransactionPolicy create(Long idPolicy, Long idUser, Date payDate, BigDecimal total) {
        Transaction tx = null;
        TransactionPolicy tp = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Recuperar las entidades relacionadas
            Policy policy = session.get(Policy.class, idPolicy);
            User user = session.get(User.class, idUser);
            if (policy == null || user == null) {
                throw new RuntimeException("Policy o User no encontrados.");
            }

            tp = new TransactionPolicy();
            tp.setPolicy(policy); // Asignar la entidad Policy
            tp.setUser(user);     // Asignar la entidad User
            tp.setPayDate(payDate);
            tp.setTotal(total);

            session.save(tp);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return tp;
    }

    /**
     * Busca un registro de TransactionPolicy por su ID único.
     *
     * @param id El ID del registro TransactionPolicy a buscar.
     * @return El objeto TransactionPolicy encontrado, o null si no se encuentra o si ocurre un error.
     */
    public TransactionPolicy findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(TransactionPolicy.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todos los registros de TransactionPolicy de la base de datos.
     *
     * @return Una lista de todos los objetos TransactionPolicy, o null si ocurre un error.
     */
    public List<TransactionPolicy> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<TransactionPolicy> query = session.createQuery("FROM TransactionPolicy", TransactionPolicy.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro existente de TransactionPolicy en la base de datos.
     *
     * @param tp El objeto TransactionPolicy con los datos actualizados.
     * @return El objeto TransactionPolicy actualizado, o null si ocurre un error.
     */
    public TransactionPolicy update(TransactionPolicy tp) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(tp);
            tx.commit();
            return tp;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}
