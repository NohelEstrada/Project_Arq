package com.sources.app.dao;

import com.sources.app.entities.Appointment;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.User;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades Appointment (Cita).
 * Representa una cita médica programada en un hospital por un usuario.
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar).
 */
public class AppointmentDAO {

    /**
     * Constructor por defecto para AppointmentDAO.
     */
    public AppointmentDAO() {}

    /**
     * Crea una nueva cita en la base de datos.
     *
     * @param idHospital ID del hospital donde se programa la cita.
     * @param idUser ID del usuario que programa la cita.
     * @param appointmentDate Fecha y hora de la cita.
     * @param enabled Estado de habilitación (1 habilitado, 0 deshabilitado/cancelado).
     * @return El objeto Appointment creado, o null si ocurre un error (p. ej., hospital o usuario no encontrados).
     */
    public Appointment create(Long idHospital, Long idUser, Date appointmentDate, Integer enabled) {
        Transaction tx = null;
        Appointment appointment = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Recuperar las entidades Hospital y User usando sus identificadores
            Hospital hospital = session.get(Hospital.class, idHospital);
            User user = session.get(User.class, idUser);

            // Validamos que las entidades existan
            if (hospital == null || user == null) {
                throw new RuntimeException("Hospital or User not found");
            }

            // Creamos y configuramos la cita con las entidades recuperadas
            appointment = new Appointment();
            appointment.setHospital(hospital);
            appointment.setUser(user);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setEnabled(enabled);

            session.save(appointment);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return appointment;
    }

    /**
     * Busca todas las citas asociadas a un ID de usuario específico.
     *
     * @param idUser El ID del usuario cuyas citas se quieren buscar.
     * @return Una lista de objetos Appointment asociados al usuario, o null si ocurre un error.
     */
    public List<Appointment> findByUserId(Long idUser) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Appointment> query = session.createQuery(
                    "FROM Appointment a WHERE a.user.id = :userId", Appointment.class
            );
            query.setParameter("userId", idUser);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca una cita por su ID único.
     *
     * @param id El ID de la cita a buscar.
     * @return El objeto Appointment encontrado, o null si no se encuentra o si ocurre un error.
     */
    public Appointment findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Appointment.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todas las citas de la base de datos.
     *
     * @return Una lista de todos los objetos Appointment, o null si ocurre un error.
     */
    public List<Appointment> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Appointment> query = session.createQuery("FROM Appointment", Appointment.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza una cita existente en la base de datos.
     *
     * @param appointment El objeto Appointment con los datos actualizados.
     * @return El objeto Appointment actualizado, o null si ocurre un error.
     */
    public Appointment update(Appointment appointment) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(appointment);
            tx.commit();
            return appointment;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}
