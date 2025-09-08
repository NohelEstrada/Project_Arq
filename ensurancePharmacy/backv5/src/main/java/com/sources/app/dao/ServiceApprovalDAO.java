package com.sources.app.dao;

import com.sources.app.dto.ServiceApprovalCreateRequest;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) para gestionar entidades {@link ServiceApproval}.
 * Esta clase proporciona métodos para crear, actualizar (estado, detalles de la receta),
 * y consultar aprobaciones de servicio basadas en varios criterios (ID, código de aprobación, usuario, hospital).
 * Utiliza Hibernate para interacciones con la base de datos.
 */
public class ServiceApprovalDAO {

    private static final Logger LOGGER = Logger.getLogger(ServiceApprovalDAO.class.getName());

    /**
     * Crea un nuevo registro de ServiceApproval en la base de datos.
     * Genera automáticamente un código de aprobación único.
     *
     * @param user               El {@link User} asociado con la aprobación.
     * @param hospital           El {@link Hospital} asociado con la aprobación.
     * @param serviceId          El identificador del servicio que se está aprobando.
     * @param serviceName        El nombre del servicio.
     * @param serviceDescription Una descripción del servicio (puede ser null).
     * @return La entidad {@link ServiceApproval} recién creada con su código generado, o null si ocurrió un error.
     */
    public ServiceApproval create(ServiceApprovalCreateRequest request) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            ServiceApproval approval = new ServiceApproval();
            approval.setUser(request.getUser());
            approval.setHospital(request.getHospital());
            approval.setServiceId(request.getServiceId());
            approval.setServiceName(request.getServiceName());
            approval.setServiceDescription(request.getServiceDescription());
            approval.setServiceCost(request.getServiceCost());
            approval.setCoveredAmount(request.getCoveredAmount());
            approval.setPatientAmount(request.getPatientAmount());
            approval.setStatus(request.getStatus());
            approval.setApprovalCode(generateApprovalCode());
            approval.setCreatedAt(new Date());
            
            session.persist(approval);
            transaction.commit();
            return approval;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, () -> "Error creating ServiceApproval (serviceId=" + request.getServiceId() + ", serviceName=" + request.getServiceName() + ")");
            return null;
        }
    }

    /**
     * @deprecated Use {@link #create(ServiceApprovalCreateRequest)} instead.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public ServiceApproval create(User user, Hospital hospital, String serviceId, String serviceName,
                                String serviceDescription, Double serviceCost, Double coveredAmount,
                                Double patientAmount, String status) {
        ServiceApprovalCreateRequest request = new ServiceApprovalCreateRequest(user, hospital, serviceId, serviceName,
                serviceDescription, serviceCost, coveredAmount, patientAmount, status);
        return create(request);
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
                session.merge(approval);
                transaction.commit();
                return approval;
            }
            return null;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, () -> "Error updating ServiceApproval prescription (id=" + approvalId + ")");
            return null;
        }
    }
    
    /**
     * Actualiza el estado de un ServiceApproval existente por ID.
     * Si el nuevo estado es "RECHAZADO", se puede proporcionar una razón de rechazo.
     * Si el nuevo estado es "COMPLETADO", la fecha de finalización se establece automáticamente.
     *
     * @param approvalId      El ID del ServiceApproval a actualizar.
     * @param status          El nuevo estado (p. ej., "APROBADO", "RECHAZADO", "COMPLETADO").
     * @param rejectionReason La razón del rechazo (relevante solo si el estado es "RECHAZADO", puede ser null).
     * @return La entidad {@link ServiceApproval} actualizada, o null si no se encontró la aprobación o ocurrió un error.
     */
    public ServiceApproval updateStatus(Long approvalId, String status, String rejectionReason) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            ServiceApproval serviceApproval = session.get(ServiceApproval.class, approvalId);
            if (serviceApproval == null) {
                return null;
            }
            
            serviceApproval.setStatus(status);
            if ("RECHAZADO".equals(status) || "REJECTED".equals(status)) {
                serviceApproval.setRejectionReason(rejectionReason);
            } else if ("COMPLETADO".equals(status) || "COMPLETED".equals(status)) {
                serviceApproval.setCompletedDate(new Date());
            }
            
            session.merge(serviceApproval);
            transaction.commit();
            return serviceApproval;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, () -> "Error updating ServiceApproval status (id=" + approvalId + ")");
            return null;
        }
    }

    /**
     * Actualiza el estado de un ServiceApproval existente.
     * Si el nuevo estado es "RECHAZADO", se puede proporcionar una razón de rechazo.
     * Si el nuevo estado es "COMPLETADO", la fecha de finalización se establece automáticamente.
     *
     * @param serviceApproval  La entidad {@link ServiceApproval} a actualizar.
     * @param status          El nuevo estado (p. ej., "APROBADO", "RECHAZADO", "COMPLETADO").
     * @param rejectionReason La razón del rechazo (relevante solo si el estado es "RECHAZADO", puede ser null).
     * @return La entidad {@link ServiceApproval} actualizada, o null si no se encontró la aprobación o ocurrió un error.
     */
    public ServiceApproval updateStatus(ServiceApproval serviceApproval, String status, String rejectionReason) {
        return updateStatus(serviceApproval.getIdApproval(), status, rejectionReason);
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
            LOGGER.log(Level.SEVERE, () -> "Error fetching ServiceApproval by id=" + id);
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
            LOGGER.log(Level.SEVERE, () -> "Error fetching ServiceApproval by approvalCode=" + approvalCode);
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
            LOGGER.log(Level.SEVERE, () -> "Error fetching ServiceApproval by userId=" + userId);
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
            LOGGER.log(Level.SEVERE, () -> "Error fetching ServiceApproval by hospital id=" + hospitalId);
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
            LOGGER.log(Level.SEVERE, () -> "Error fetching all ServiceApproval records");
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