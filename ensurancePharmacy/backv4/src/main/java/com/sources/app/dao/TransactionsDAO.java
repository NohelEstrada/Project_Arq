package com.sources.app.dao;

import com.sources.app.entities.Transactions;
import com.sources.app.entities.User;
import com.sources.app.entities.Hospital;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades de Transacciones.
 * Proporciona métodos para crear, buscar, actualizar y listar transacciones.
 */
public class TransactionsDAO {

    /**
     * Crea una nueva transacción en la base de datos.
     *
     * @param idUser ID del usuario asociado a la transacción.
     * @param idHospital ID del hospital asociado a la transacción.
     * @param transDate Fecha de la transacción.
     * @param total Monto total de la transacción.
     * @param copay Monto del copago.
     * @param transactionComment Comentario sobre la transacción.
     * @param result Resultado de la transacción.
     * @param covered Indicador de si la transacción está cubierta (1) o no (0).
     * @param auth Código de autorización.
     * @return El objeto Transactions creado, o null si ocurre un error.
     */
    public Transactions create(Long idUser, Long idHospital, Date transDate, Double total,
                               Double copay, String transactionComment, String result,
                               Integer covered, String auth) {
        Transaction tx = null;
        Transactions t = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Recuperar las entidades relacionadas a partir de sus IDs
            User user = session.get(User.class, idUser);
            Hospital hospital = session.get(Hospital.class, idHospital);
            if (user == null || hospital == null) {
                throw new RuntimeException("User or Hospital not found.");
            }

            t = new Transactions();
            t.setUser(user);
            t.setHospital(hospital);
            t.setTransDate(transDate);
            t.setTotal(total);
            t.setCopay(copay);
            t.setTransactionComment(transactionComment);
            t.setResult(result);
            t.setCovered(covered);
            t.setAuth(auth);

            session.save(t);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return t;
    }

    /**
     * Busca una transacción por su ID único.
     *
     * @param id El ID de la transacción a buscar.
     * @return El objeto Transactions encontrado, o null si no se encuentra o si ocurre un error.
     */
    public Transactions findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Transactions.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca todas las transacciones asociadas a un ID de usuario específico.
     *
     * @param idUser El ID del usuario cuyas transacciones se quieren buscar.
     * @return Una lista de objetos Transactions asociados al usuario, o null si ocurre un error.
     */
    public List<Transactions> findByUserId(Long idUser) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Transactions> query = session.createQuery(
                    "FROM Transactions t WHERE t.user.idUser = :idUser",
                    Transactions.class
            );
            query.setParameter("idUser", idUser);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Recupera todas las transacciones existentes en la base de datos.
     *
     * @return Una lista de todos los objetos Transactions, o null si ocurre un error.
     */
    public List<Transactions> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Transactions> query = session.createQuery("FROM Transactions", Transactions.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza una transacción existente en la base de datos.
     *
     * @param t El objeto Transactions con los datos actualizados.
     * @return El objeto Transactions actualizado, o null si ocurre un error.
     */
    public Transactions update(Transactions t) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(t);
            tx.commit();
            return t;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}
