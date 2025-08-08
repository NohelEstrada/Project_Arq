package com.sources.app.dao;

import com.sources.app.entities.User;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar entidades {@link User}.
 * Proporciona métodos para la autenticación de usuarios (inicio de sesión) y operaciones CRUD estándar
 * (Crear, Leer, Actualizar) utilizando Hibernate.
 */
public class UserDAO {

    /**
     * Autentica a un usuario basado en su email y contraseña.
     * <p>
     * **Advertencia de seguridad:** Este método compara contraseñas directamente.
     * En un entorno de producción, las contraseñas deben ser hasheadas de forma segura antes de ser almacenadas,
     * y este método debería comparar el hash de la contraseña proporcionada con el hash almacenado.
     * </p>
     *
     * @param email    La dirección de correo electrónico del usuario.
     * @param password La contraseña en texto plano del usuario.
     * @return La entidad {@link User} autenticada si las credenciales son válidas, de lo contrario null.
     */
    public User login(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE email = :email AND password = :password", User.class
            );
            query.setParameter("email", email);
            query.setParameter("password", password);
            return query.uniqueResult(); // Retorna null si no encuentra el usuario
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Crea un nuevo registro de Usuario en la base de datos.
     * Establece el rol por defecto a "usuario" y el estado habilitado a '1'.
     * <p>
     * **Nota de seguridad:** La contraseña se guarda tal como se proporciona. El hashing debe implementarse
     * aquí o antes de llamar a este método.
     * </p>
     *
     * @param name      El nombre completo del usuario.
     * @param cui       El CUI (Código Único de Identificación) del usuario.
     * @param phone     El número de teléfono del usuario.
     * @param email     La dirección de correo electrónico del usuario (utilizada para iniciar sesión).
     * @param birthDate La fecha de nacimiento del usuario.
     * @param address   La dirección física del usuario.
     * @param password  La contraseña en texto plano del usuario (debería ser hasheada).
     * @return La entidad {@link User} recién creada, o null si ocurrió un error (p. ej., el email ya existe).
     */
    public User create(String name, String cui, String phone, String email, Date birthDate, String address, String password) {
        Transaction tx = null;
        User user = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            user = new User();
            user.setName(name);
            user.setCui(cui);
            user.setPhone(phone);
            user.setEmail(email);
            user.setBirthDate(birthDate);
            user.setAddress(address);
            user.setPassword(password);
            user.setRole("usuario");
            user.setEnabled('1'); // Se usa '1' como Character

            session.save(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Recupera todos los registros de Usuario de la base de datos.
     *
     * @return Una lista de todas las entidades {@link User}, o null si ocurrió un error.
     */
    public List<User> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera un registro de Usuario específico por su identificador único.
     *
     * @param id El ID del Usuario a recuperar.
     * @return La entidad {@link User} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public User getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro de Usuario existente en la base de datos.
     * Nota: Este método típicamente actualiza la información del perfil. Las actualizaciones de contraseña generalmente
     * deben manejarse por separado con las comprobaciones de seguridad adecuadas.
     *
     * @param user La entidad {@link User} con información actualizada. El ID debe coincidir con un registro existente.
     *             El campo de contraseña en el objeto de entrada podría ignorarse o debería manejarse con cuidado.
     * @return La entidad {@link User} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public User update(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(user);
            tx.commit();
            return user;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }
}
