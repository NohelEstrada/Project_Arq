package com.sources.app.dao;

import com.sources.app.entities.ServiceApproval;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object (DAO) para gestionar las entidades ServiceApproval (Aprobación de Servicio).
 * Representa la aprobación de un servicio médico para un usuario en un hospital específico.
 * Proporciona métodos para crear, actualizar y buscar aprobaciones.
 */
public class ServiceApprovalDAO {

    /**
     * Crea un nuevo registro de aprobación de servicio.
     * Genera automáticamente un código de aprobación único y establece la fecha de aprobación si no se proporcionan.
     *
     * @param approval El objeto ServiceApproval a crear.
     * @return El objeto ServiceApproval creado, o null si ocurre un error.
     */
    public ServiceApproval create(ServiceApproval approval) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Generar código de aprobación único si no existe
            if (approval.getApprovalCode() == null || approval.getApprovalCode().isEmpty()) {
                String approvalCode = generateApprovalCode();
                approval.setApprovalCode(approvalCode);
            }
            
            // Establecer fecha de aprobación si no existe
            if (approval.getApprovalDate() == null) {
                approval.setApprovalDate(new Date());
            }
            
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
     * Actualiza un registro de aprobación de servicio existente.
     *
     * @param approval El objeto ServiceApproval con los datos actualizados.
     * @return El objeto ServiceApproval actualizado, o null si ocurre un error.
     */
    public ServiceApproval update(ServiceApproval approval) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(approval);
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
     * Actualiza la información de la receta asociada a una aprobación existente, buscándola por su código de aprobación.
     *
     * @param approvalCode El código de la aprobación a actualizar.
     * @param prescriptionId El nuevo ID de la receta asociada.
     * @param prescriptionTotal El nuevo monto total de la receta asociada.
     * @return El objeto ServiceApproval actualizado, o null si no se encuentra la aprobación o si ocurre un error.
     */
    public ServiceApproval updatePrescriptionInfo(String approvalCode, Long prescriptionId, Double prescriptionTotal) {
        try {
            ServiceApproval approval = getByApprovalCode(approvalCode);
            if (approval == null) {
                return null;
            }
            
            approval.setPrescriptionId(prescriptionId);
            approval.setPrescriptionTotal(prescriptionTotal);
            
            return update(approval);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Actualiza el estado de una aprobación existente, buscándola por su código.
     * Si el nuevo estado es "REJECTED", también actualiza el motivo del rechazo.
     * Si el nuevo estado es "COMPLETED", establece la fecha de finalización.
     *
     * @param approvalCode El código de la aprobación a actualizar.
     * @param newStatus El nuevo estado (p. ej., "APPROVED", "REJECTED", "COMPLETED").
     * @param rejectionReason El motivo del rechazo (solo relevante si newStatus es "REJECTED").
     * @return El objeto ServiceApproval actualizado, o null si no se encuentra la aprobación o si ocurre un error.
     */
    public ServiceApproval updateStatus(String approvalCode, String newStatus, String rejectionReason) {
        try {
            ServiceApproval approval = getByApprovalCode(approvalCode);
            if (approval == null) {
                return null;
            }
            
            approval.setStatus(newStatus);
            
            if (newStatus.equals("REJECTED") && rejectionReason != null) {
                approval.setRejectionReason(rejectionReason);
            }
            
            if (newStatus.equals("COMPLETED")) {
                approval.setCompletedDate(new Date());
            }
            
            return update(approval);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene una aprobación de servicio por su ID único.
     *
     * @param id El ID de la aprobación a buscar.
     * @return El objeto ServiceApproval encontrado, o null si no se encuentra o si ocurre un error.
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
     * Obtiene una aprobación de servicio por su código de aprobación único.
     *
     * @param approvalCode El código de la aprobación a buscar.
     * @return El objeto ServiceApproval encontrado, o null si no se encuentra o si ocurre un error.
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
     * Obtiene todas las aprobaciones de servicio asociadas a un ID de usuario específico, ordenadas por fecha descendente.
     *
     * @param userId El ID del usuario cuyas aprobaciones se quieren buscar.
     * @return Una lista de objetos ServiceApproval asociados al usuario, o null si ocurre un error.
     */
    public List<ServiceApproval> getByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ServiceApproval> query = session.createQuery(
                "FROM ServiceApproval WHERE userId = :userId ORDER BY approvalDate DESC", ServiceApproval.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene todas las aprobaciones de servicio asociadas a un ID de hospital específico, ordenadas por fecha descendente.
     *
     * @param hospitalId El ID del hospital cuyas aprobaciones se quieren buscar.
     * @return Una lista de objetos ServiceApproval asociados al hospital, o null si ocurre un error.
     */
    public List<ServiceApproval> getByHospitalId(Long hospitalId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ServiceApproval> query = session.createQuery(
                "FROM ServiceApproval WHERE hospitalId = :hospitalId ORDER BY approvalDate DESC", ServiceApproval.class);
            query.setParameter("hospitalId", hospitalId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene todos los registros de aprobación de servicio de la base de datos, ordenados por fecha descendente.
     *
     * @return Una lista de todos los objetos ServiceApproval, o null si ocurre un error.
     */
    public List<ServiceApproval> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ServiceApproval> query = session.createQuery(
                "FROM ServiceApproval ORDER BY approvalDate DESC", ServiceApproval.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Genera un código de aprobación único utilizando UUID.
     * Formato: "AP" seguido de 8 caracteres hexadecimales en mayúsculas.
     *
     * @return Un código de aprobación único generado.
     */
    private String generateApprovalCode() {
        // Generar un UUID y tomar los primeros 8 caracteres
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "AP" + uuid;
    }

    /**
     * Obtiene todas las aprobaciones. Alias para `getAll()` por compatibilidad.
     *
     * @return Una lista de todos los objetos ServiceApproval, o null si ocurre un error.
     */
    public List<ServiceApproval> findAll() {
        return getAll();
    }

    /**
     * Obtiene una aprobación por su código. Alias para `getByApprovalCode()` por compatibilidad.
     *
     * @param approvalCode El código de la aprobación a buscar.
     * @return El objeto ServiceApproval encontrado, o null si no se encuentra o si ocurre un error.
     */
    public ServiceApproval findByApprovalCode(String approvalCode) {
        return getByApprovalCode(approvalCode);
    }

    /**
     * Obtiene una aprobación de servicio por el ID de la receta asociada.
     *
     * @param prescriptionId El ID de la receta a buscar.
     * @return El objeto ServiceApproval asociado a esa receta, o null si no se encuentra o si ocurre un error.
     */
    public ServiceApproval findByPrescriptionId(long prescriptionId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ServiceApproval> query = session.createQuery(
                "FROM ServiceApproval WHERE prescriptionId = :prescriptionId", ServiceApproval.class);
            query.setParameter("prescriptionId", prescriptionId);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 