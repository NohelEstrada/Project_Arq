package com.sources.app.dao;

import com.sources.app.entities.EnsuranceAppointment;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades EnsuranceAppointment (Citas de Seguro).
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre las citas.
 */
public class EnsuranceAppointmentDAO {

    /**
     * Constructor por defecto para EnsuranceAppointmentDAO.
     */
    public EnsuranceAppointmentDAO() {}

    /**
     * Crea una nueva cita de seguro en la base de datos.
     *
     * @param hospitalAppointmentId ID de la cita en el sistema del hospital (puede ser diferente al ID interno).
     * @param idUser ID del usuario asociado a la cita.
     * @param appointmentDate Fecha y hora de la cita.
     * @param doctorName Nombre del doctor (opcional).
     * @param reason Motivo de la cita (opcional).
     * @return El objeto EnsuranceAppointment creado, o null si ocurre un error.
     */
    public EnsuranceAppointment create(String hospitalAppointmentId, Long idUser, Date appointmentDate, String doctorName, String reason) {
        Transaction tx = null;
        EnsuranceAppointment appointment = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            appointment = new EnsuranceAppointment();
            appointment.setHospitalAppointmentId(hospitalAppointmentId);
            appointment.setIdUser(idUser);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setDoctorName(doctorName);
            appointment.setReason(reason);

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
     * Busca una cita de seguro por su ID interno único.
     *
     * @param id El ID interno de la cita a buscar.
     * @return El objeto EnsuranceAppointment encontrado, o null si no se encuentra o si ocurre un error.
     */
    public EnsuranceAppointment findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(EnsuranceAppointment.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca una cita de seguro por el ID que tiene asignado en el sistema del hospital.
     *
     * @param hospitalAppointmentId El ID de la cita en el sistema del hospital.
     * @return El objeto EnsuranceAppointment encontrado, o null si no se encuentra o si ocurre un error.
     */
    public EnsuranceAppointment findByHospitalAppointmentId(String hospitalAppointmentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EnsuranceAppointment> query = session.createQuery(
                    "FROM EnsuranceAppointment WHERE hospitalAppointmentId = :hospitalId",
                    EnsuranceAppointment.class
            );
            query.setParameter("hospitalId", hospitalAppointmentId);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca todas las citas de seguro asociadas a un ID de usuario específico, ordenadas por fecha descendente.
     *
     * @param idUser El ID del usuario cuyas citas se quieren buscar.
     * @return Una lista de objetos EnsuranceAppointment asociados al usuario, o null si ocurre un error.
     */
    public List<EnsuranceAppointment> findByUserId(Long idUser) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EnsuranceAppointment> query = session.createQuery(
                    "FROM EnsuranceAppointment WHERE idUser = :userId ORDER BY appointmentDate DESC",
                    EnsuranceAppointment.class
            );
            query.setParameter("userId", idUser);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todas las citas de seguro de la base de datos, ordenadas por fecha descendente.
     *
     * @return Una lista de todos los objetos EnsuranceAppointment, o null si ocurre un error.
     */
    public List<EnsuranceAppointment> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EnsuranceAppointment> query = session.createQuery(
                    "FROM EnsuranceAppointment ORDER BY appointmentDate DESC",
                    EnsuranceAppointment.class
            );
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza una cita de seguro existente en la base de datos.
     *
     * @param appointment El objeto EnsuranceAppointment con los datos actualizados.
     * @return El objeto EnsuranceAppointment actualizado, o null si ocurre un error.
     */
    public EnsuranceAppointment update(EnsuranceAppointment appointment) {
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

    /**
     * Elimina una cita de seguro de la base de datos por su ID interno.
     *
     * @param id El ID interno de la cita a eliminar.
     * @return true si la cita fue eliminada con éxito, false en caso contrario.
     */
    public boolean delete(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            EnsuranceAppointment appointment = session.get(EnsuranceAppointment.class, id);
            if (appointment != null) {
                session.delete(appointment);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina una cita de seguro de la base de datos utilizando el ID del sistema del hospital.
     *
     * @param hospitalAppointmentId El ID de la cita en el sistema del hospital.
     * @return true si la cita fue eliminada con éxito, false en caso contrario (p. ej., no se encontró la cita).
     */
    public boolean deleteByHospitalAppointmentId(String hospitalAppointmentId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query<EnsuranceAppointment> query = session.createQuery(
                    "FROM EnsuranceAppointment WHERE hospitalAppointmentId = :hospitalId",
                    EnsuranceAppointment.class
            );
            query.setParameter("hospitalId", hospitalAppointmentId);
            EnsuranceAppointment appointment = query.uniqueResult();
            
            if (appointment != null) {
                session.delete(appointment);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca todas las citas de seguro que ocurren en una fecha específica (considerando el día completo).
     *
     * @param date La fecha para la cual buscar citas.
     * @return Una lista de objetos EnsuranceAppointment para la fecha dada, o null si ocurre un error.
     */
    public List<EnsuranceAppointment> findByDate(Date date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Crear una fecha de inicio (00:00:00) y fin (23:59:59) para el día especificado
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date startDate = calendar.getTime();
            
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date endDate = calendar.getTime();
            
            Query<EnsuranceAppointment> query = session.createQuery(
                "FROM EnsuranceAppointment WHERE appointmentDate BETWEEN :startDate AND :endDate",
                EnsuranceAppointment.class
            );
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Busca todas las citas de seguro programadas para el día de hoy.
     * Es un método de conveniencia que llama a findByDate con la fecha actual.
     *
     * @return Una lista de objetos EnsuranceAppointment para hoy, o null si ocurre un error.
     */
    public List<EnsuranceAppointment> findTodayAppointments() {
        return findByDate(new Date());
    }
} 