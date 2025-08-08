package com.sources.app.dao;

import com.sources.app.entities.User;
import com.sources.app.entities.Policy;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Date;

/**
 * Data Access Object (DAO) para gestionar las entidades de Usuario (User).
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * y otras operaciones relacionadas con los usuarios, como inicio de sesión y validaciones.
 */
public class UserDAO {

    /**
     * Autentica a un usuario basado en su correo electrónico y contraseña.
     *
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return El objeto User si la autenticación es exitosa, null en caso contrario o si ocurre un error.
     */
    public User login(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email AND password = :password", User.class);
            query.setParameter("email", email);
            query.setParameter("password", password);
            User user = query.uniqueResult();
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifica si ya existe un usuario con el correo electrónico proporcionado.
     *
     * @param email El correo electrónico a verificar.
     * @return true si existe un usuario con ese email, false en caso contrario.
     */
    public boolean existsUserWithEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica si ya existe un usuario con el CUI (Código Único de Identificación) proporcionado.
     *
     * @param cui El CUI a verificar.
     * @return true si existe un usuario con ese CUI, false en caso contrario.
     */
    public boolean existsUserWithCUI(Long cui) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.cui = :cui", Long.class);
            query.setParameter("cui", cui);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Crea un nuevo usuario en la base de datos.
     * Verifica previamente si ya existe un usuario con el mismo email o CUI.
     *
     * @param name Nombre del usuario.
     * @param cui CUI del usuario.
     * @param phone Teléfono del usuario.
     * @param email Correo electrónico del usuario.
     * @param birthdate Fecha de nacimiento del usuario.
     * @param address Dirección del usuario.
     * @param password Contraseña del usuario.
     * @param policy Póliza asociada al usuario.
     * @return El objeto User creado, o null si ya existe un usuario con el mismo email/CUI o si ocurre un error.
     */
    public User create(String name, Long cui, String phone, String email, Date birthdate, String address, String password, Policy policy) {
        if (existsUserWithEmail(email)) {
            System.out.println("ERROR: Ya existe un usuario con el email: " + email);
            return null;
        }
        
        if (existsUserWithCUI(cui)) {
            System.out.println("ERROR: Ya existe un usuario con el CUI: " + cui);
            return null;
        }
        
        Transaction tx = null;
        Session session = null;
        User user = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            user = new User();
            user.setName(name);
            user.setCui(cui);
            user.setPhone(phone);
            user.setEmail(email);
            user.setBirthDate(birthdate);
            user.setAddress(address);
            user.setPassword(password);
            user.setRole(" ");
            user.setEnabled(0);
            user.setPolicy(policy);
            
            // Valores por defecto para los nuevos campos
            user.setPaidService(null); // Inicialmente nulo, sin valor definido
            user.setExpirationDate(null); // Sin fecha de expiración

            session.save(user);

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    rbEx.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return user;
    }

    /**
     * Recupera todos los usuarios de la base de datos.
     *
     * @return Una lista de todos los objetos User, o null si ocurre un error.
     */
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca un usuario por su ID único.
     *
     * @param idUser El ID del usuario a buscar.
     * @return El objeto User encontrado, o null si no se encuentra o si ocurre un error.
     */
    public User findById(Long idUser) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, idUser);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza la información de un usuario existente en la base de datos.
     * También verifica y actualiza el estado de expiración del servicio pagado.
     *
     * @param user El objeto User con la información actualizada.
     * @return El objeto User actualizado, o null si el usuario no existe o si ocurre un error.
     */
    public User update(User user) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            User existingUser = session.get(User.class, user.getIdUser());
            if (existingUser == null) {
                return null;
            }

            existingUser.setName(user.getName());
            existingUser.setCui(user.getCui());
            existingUser.setPhone(user.getPhone());
            existingUser.setEmail(user.getEmail());
            existingUser.setAddress(user.getAddress());
            existingUser.setBirthDate(user.getBirthDate());
            existingUser.setRole(user.getRole());
            existingUser.setEnabled(user.getEnabled());
            existingUser.setPassword(user.getPassword());
            
            // Actualizar campos de servicio
            existingUser.setPaidService(user.getPaidService());
            existingUser.setExpirationDate(user.getExpirationDate());

            // Verificar si hay que limpiar la fecha de expiración
            if (user.getPaidService() != null && !user.getPaidService()) {
                existingUser.setExpirationDate(null); // Si no tiene servicio pagado, no tiene fecha de expiración
            }

            // Verificar expiración del servicio
            checkServiceExpiration(existingUser);

            // Actualizar política según el servicio
            if (user.getPolicy() != null && 
                (existingUser.getPolicy() == null || 
                 !existingUser.getPolicy().getIdPolicy().equals(user.getPolicy().getIdPolicy()))) {
                existingUser.setPolicy(user.getPolicy());
            } else if (user.getPolicy() == null || 
                      (user.getPaidService() != null && !user.getPaidService())) {
                // Si el usuario ya no tiene póliza asignada o el servicio no está pagado
                existingUser.setPolicy(null);
            }

            session.update(existingUser);
            tx.commit();
            
            return existingUser;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    rbEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Verifica si el servicio del usuario ha expirado y actualiza su estado
     * si paidService es true y la expirationDate es anterior a la fecha actual.
     * Si expira, establece paidService a false y policy a null.
     * 
     * @param user Usuario a verificar
     */
    private void checkServiceExpiration(User user) {
        // Ignorar usuarios sin servicio definido o sin fecha de expiración
        if (user.getPaidService() == null || user.getExpirationDate() == null) {
            return;
        }
        
        // Solo verificar expiración si el servicio está pagado
        if (user.getPaidService()) {
            Date today = new Date();
            if (user.getExpirationDate().before(today)) {
                // El servicio ha expirado
                user.setPaidService(false);
                user.setPolicy(null); // Quitar la póliza asignada
                System.out.println("Servicio expirado para el usuario: " + user.getEmail());
            }
        }
    }
    
    /**
     * Verifica y actualiza el estado de expiración del servicio para todos los usuarios.
     * Busca usuarios con `paidService = true` y `expirationDate` anterior a hoy,
     * y para ellos, establece `paidService = false` y `policy = null`.
     *
     * @return El número de usuarios cuyo estado de servicio fue actualizado a expirado.
     */
    public int checkAllUsersServiceExpiration() {
        int updatedCount = 0;
        Transaction tx = null;
        Session session = null;
        
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            // Buscar usuarios con servicio pagado y fecha de expiración anterior a hoy
            Date today = new Date();
            Query<User> query = session.createQuery(
                "FROM User WHERE paidService = true AND expirationDate < :today", 
                User.class
            );
            query.setParameter("today", today);
            List<User> expiredUsers = query.getResultList();
            
            for (User user : expiredUsers) {
                user.setPaidService(false);
                user.setPolicy(null);
                session.update(user);
                updatedCount++;
            }
            
            tx.commit();
            System.out.println("Servicios expirados actualizados: " + updatedCount);
            return updatedCount;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    rbEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return 0;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email El correo electrónico del usuario a buscar.
     * @return El objeto User encontrado, o null si no se encuentra o si ocurre un error.
     */
    public User findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
