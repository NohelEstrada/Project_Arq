package com.sources.app.dao;

import com.sources.app.entities.AppointmentMade;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades AppointmentMade.
 * Representa un registro de una cita que ha sido efectivamente realizada o atendida.
 * Proporciona métodos para crear, buscar y actualizar estos registros.
 */
public class AppointmentMadeDAO {

    /**
     * Constructor por defecto para AppointmentMadeDAO.
     */
    public AppointmentMadeDAO() {}

    /**
     * Crea un nuevo registro que indica que una cita ha sido realizada.
     *
     * @param idCita El ID de la cita original (puede referenciar a Appointment o EnsuranceAppointment).
     * @param idUser El ID del usuario asociado a la cita.
     * @param appointmentMadeDate La fecha y hora en que se marcó la cita como realizada.
     * @return El objeto AppointmentMade creado, o null si ocurre un error.
     */
    public AppointmentMade create(Long idCita, Long idUser, Date appointmentMadeDate) {
        Transaction tx = null;
        AppointmentMade appMade = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            appMade = new AppointmentMade();
            appMade.setIdCita(idCita);
            appMade.setIdUser(idUser);
            appMade.setAppointmentMadeDate(appointmentMadeDate);

            session.save(appMade);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return appMade;
    }

    /**
     * Busca un registro de AppointmentMade por su ID único.
     *
     * @param id El ID del registro AppointmentMade a buscar.
     * @return El objeto AppointmentMade encontrado, o null si no se encuentra o si ocurre un error.
     */
    public AppointmentMade findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(AppointmentMade.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todos los registros de AppointmentMade de la base de datos.
     *
     * @return Una lista de todos los objetos AppointmentMade, o null si ocurre un error.
     */
    public List<AppointmentMade> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<AppointmentMade> query = session.createQuery("FROM AppointmentMade", AppointmentMade.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro existente de AppointmentMade.
     * Usualmente, solo se actualizaría la fecha `appointmentMadeDate` si fuera necesario.
     *
     * @param appMade El objeto AppointmentMade con los datos actualizados.
     * @return El objeto AppointmentMade actualizado, o null si ocurre un error.
     */
    public AppointmentMade update(AppointmentMade appMade) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(appMade);
            tx.commit();
            return appMade;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}
