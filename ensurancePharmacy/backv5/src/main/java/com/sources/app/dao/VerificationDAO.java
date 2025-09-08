package com.sources.app.dao;

import com.sources.app.entities.User;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) para verificar la existencia de usuarios externos basados en el email.
 */
public class VerificationDAO {
    
    private static final Logger LOGGER = Logger.getLogger(VerificationDAO.class.getName());
    
    /**
     * Verifica si existe al menos un usuario con el email proporcionado y el rol 'externo'.
     * <p>
     * Nota: Este método inicia una transacción de Hibernate pero no la confirma (commit) ni la revierte (rollback),
     * lo cual es innecesario para una operación de solo lectura. La gestión de transacciones podría eliminarse.
     * También incluye impresiones en System.out para depuración que podrían eliminarse en código de producción.
     * </p>
     * 
     * @param email La dirección de correo electrónico del usuario a verificar.
     * @return {@code true} si se encuentra al menos un usuario con el email y rol 'externo' especificados,
     *         {@code false} si no se encuentra dicho usuario o si ocurre un error durante la consulta.
     */
    public boolean verifyUser(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            LOGGER.info("Verifying external user existence for email=" + email);
            Query<User> query = session.createQuery("FROM User WHERE email = :email AND role = 'externo'", User.class);
            query.setParameter("email", email);
            List<User> users = query.getResultList();
            if (users.isEmpty()) {
                LOGGER.info("External user not found for email=" + email);
                return false;
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying external user by email=" + email, e);
            return false;
        }
    }
}
