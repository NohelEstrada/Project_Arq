package com.sources.app.dao;

import com.sources.app.entities.Hospital;
import com.sources.app.entities.ServiceApproval;
import com.sources.app.entities.User;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object (DAO) para gestionar entidades {@link ServiceApproval}.
 * Esta clase proporciona métodos para crear, actualizar (estado, detalles de la receta),
 * y consultar aprobaciones de servicio basadas en varios criterios (ID, código de aprobación, usuario, hospital).
 * Utiliza Hibernate para interacciones con la base de datos.
 */
public class ServiceApprovalDAO {

    /**
     * Crea un nuevo registro de ServiceApproval en la base de datos.
     * Genera automáticamente un código de aprobación único.
     *
     * @param user               El {@link User} asociado con la aprobación.
     * @param hospital           El {@link Hospital} asociado con la aprobación.
     * @param serviceId          El identificador del servicio que se está aprobando.
     * @param serviceName        El nombre del servicio.
     * @param serviceDescription Una descripción del servicio (puede ser null).
     * @param serviceCost        El costo total del servicio.
     * @param coveredAmount      El monto cubierto por el seguro o la póliza.
     * @param patientAmount      El monto a pagar por el paciente.
     * @param status             El estado inicial de la aprobación (p. ej., "PENDIENTE", "APROBADO").
     * @return La entidad {@link ServiceApproval} recién creada con su código generado, o null si ocurrió un error.
     */
    public ServiceApproval create(User user, Hospital hospital, String serviceId, String serviceName,
                                String serviceDescription, Double serviceCost, Double coveredAmount,
                                Double patientAmount, String status) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            ServiceApproval approval = new ServiceApproval();
            approval.setUser(user);
            approval.setHospital(hospital);
            approval.setServiceId(serviceId);
            approval.setServiceName(serviceName);
            approval.setServiceDescription(serviceDescription);
            approval.setServiceCost(serviceCost);
            approval.setCoveredAmount(coveredAmount);
            approval.setPatientAmount(patientAmount);
            approval.setStatus(status);
            approval.setApprovalDate(new Date());
            
            // Generar código de aprobación único
            String approvalCode = generateApprovalCode();
            approval.setApprovalCode(approvalCode);
            
            session.save(approval);
            transaction.commit();
            return approval;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Actualiza la información relacionada con la receta para un ServiceApproval existente.
     *
     * @param approvalId        El ID del registro de ServiceApproval a actualizar.
     * @param prescriptionId    El ID de la receta asociada (puede ser null o actualizado).
     * @param prescriptionTotal El costo total de la receta asociada (puede ser null o actualizado).
     * @return La entidad {@link ServiceApproval} actualizada, o null si no se encontró la aprobación o ocurrió un error.
     */
    public ServiceApproval updatePrescription(Long approvalId, String prescriptionId, Double prescriptionTotal) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            ServiceApproval approval = session.get(ServiceApproval.class, approvalId);
            if (approval != null) {
                approval.setPrescriptionId(prescriptionId);
                approval.setPrescriptionTotal(prescriptionTotal);
                session.update(approval);
                transaction.commit();
                return approval;
            }
            return null;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Actualiza el estado de un ServiceApproval existente.
     * Si el nuevo estado es "RECHAZADO", se puede proporcionar una razón de rechazo.
     * Si el nuevo estado es "COMPLETADO", la fecha de finalización se establece automáticamente.
     *
     * @param approvalId      El ID del registro de ServiceApproval a actualizar.
     * @param status          El nuevo estado (p. ej., "APROBADO", "RECHAZADO", "COMPLETADO").
     * @param rejectionReason La razón del rechazo (relevante solo si el estado es "RECHAZADO", puede ser null).
     * @return La entidad {@link ServiceApproval} actualizada, o null si no se encontró la aprobación o ocurrió un error.
     */
    public ServiceApproval updateStatus(Long approvalId, String status, String rejectionReason) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            ServiceApproval approval = session.get(ServiceApproval.class, approvalId);
            if (approval != null) {
                approval.setStatus(status);
                if (rejectionReason != null) {
                    approval.setRejectionReason(rejectionReason);
                }
                
                if ("COMPLETED".equals(status)) {
                    approval.setCompletedDate(new Date());
                }
                
                session.update(approval);
                transaction.commit();
                return approval;
            }
            return null;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Recupera un registro de ServiceApproval específico por su identificador único.
     *
     * @param id El ID del ServiceApproval a recuperar.
     * @return La entidad {@link ServiceApproval} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public ServiceApproval getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(ServiceApproval.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Recupera un registro de ServiceApproval específico por su código de aprobación único.
     *
     * @param approvalCode El código de aprobación único a buscar.
     * @return La entidad {@link ServiceApproval} correspondiente al código dado, o null si no se encuentra o ocurrió un error.
     */
    public ServiceApproval getByApprovalCode(String approvalCode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ServiceApproval> query = session.createQuery(
                "FROM ServiceApproval WHERE approvalCode = :code", ServiceApproval.class);
            query.setParameter("code", approvalCode);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Recupera todos los registros de ServiceApproval asociados con un usuario específico, ordenados por fecha de creación descendente.
     *
     * @param userId El ID del {@link User}.
     * @return Una lista de entidades {@link ServiceApproval} para el usuario especificado, o null si ocurrió un error.
     */
    public List<ServiceApproval> getByUser(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ServiceApproval> query = session.createQuery(
                "FROM ServiceApproval WHERE user.idUser = :userId ORDER BY createdAt DESC", ServiceApproval.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Recupera todos los registros de ServiceApproval asociados con un hospital específico, ordenados por fecha de creación descendente.
     *
     * @param hospitalId El ID del {@link Hospital}.
     * @return Una lista de entidades {@link ServiceApproval} para el hospital especificado, o null si ocurrió un error.
     */
    public List<ServiceApproval> getByHospital(Long hospitalId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ServiceApproval> query = session.createQuery(
                "FROM ServiceApproval WHERE hospital.idHospital = :hospitalId ORDER BY createdAt DESC", ServiceApproval.class);
            query.setParameter("hospitalId", hospitalId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Recupera todos los registros de ServiceApproval de la base de datos, ordenados por fecha de creación descendente.
     *
     * @return Una lista de todas las entidades {@link ServiceApproval}, o null si ocurrió un error.
     */
    public List<ServiceApproval> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ServiceApproval> query = session.createQuery(
                "FROM ServiceApproval ORDER BY createdAt DESC", ServiceApproval.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Genera un código de aprobación único usando una porción de un UUID.
     * El formato del código es "AP" seguido de 8 caracteres hexadecimales en mayúsculas.
     *
     * @return Un código de aprobación String único (p. ej., "AP1A2B3C4D").
     */
    private String generateApprovalCode() {
        // Generar un UUID y tomar los primeros 8 caracteres
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "AP" + uuid;
    }
} 