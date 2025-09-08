package com.sources.app.dao;

import com.sources.app.entities.User;
import com.sources.app.dto.UserCreateRequest;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) para gestionar entidades {@link User}.
 * Proporciona métodos para la autenticación de usuarios (inicio de sesión) y operaciones CRUD estándar
 * (Crear, Leer, Actualizar) utilizando Hibernate.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

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
            LOGGER.log(Level.SEVERE, () -> "Error during login for email=" + email);
            return null;
        }
    }

    /**
     * Crea un nuevo registro de Usuario en la base de datos.
     * Genera automáticamente valores por defecto para el rol ("usuario") y el estado habilitado ('1').
     *
     * @param request El objeto {@link UserCreateRequest} con todos los datos del usuario.
     * @return La entidad {@link User} recién creada, o null si ocurrió un error (p. ej., el email ya existe).
     */
    public User create(UserCreateRequest request) {
        Transaction tx = null;
        User user = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            user = new User();
            user.setName(request.getName());
            user.setCui(request.getCui());
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());
            user.setBirthDate(request.getBirthDate());
            user.setAddress(request.getAddress());
            user.setPassword(request.getPassword());
            user.setRole("usuario");
            user.setEnabled('1'); // Se usa '1' como Character

            session.persist(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, () -> "Error creating User (email=" + request.getEmail() + ")");
        }
        return user;
    }

    /**
     * Método de conveniencia para mantener compatibilidad con código existente.
     * @deprecated Use create(UserCreateRequest) instead
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public User create(String name, String cui, String phone, String email, Date birthDate, String address, String password) {
        UserCreateRequest request = new UserCreateRequest(name, cui, phone, email, birthDate, address, password);
        return create(request);
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
            LOGGER.log(Level.SEVERE, "Error fetching all User records", e);
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
            LOGGER.log(Level.SEVERE, () -> "Error fetching User by id=" + id);
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
            session.merge(user);
            tx.commit();
            return user;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, () -> "Error updating User (id=" + (user != null ? user.getIdUser() : null) + ")");
            return null;
        }
    }
}
