package com.sources.app.dao;

import com.sources.app.entities.HospitalInsuranceService;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.InsuranceService;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Date;

/**
 * Data Access Object (DAO) para gestionar la relación entre Hospitales y Servicios de Seguro.
 * Proporciona métodos para aprobar, revocar, buscar y eliminar relaciones hospital-servicio de seguro.
 */
public class HospitalInsuranceServiceDAO {

    /**
     * Constructor por defecto para HospitalInsuranceServiceDAO.
     */
    public HospitalInsuranceServiceDAO() {}

    /**
     * Aprueba un servicio de seguro para un hospital específico. Si la relación
     * ya existe, actualiza el estado de aprobación y las notas. De lo contrario, crea
     * un nuevo registro de relación.
     *
     * @param hospital El hospital que aprueba el servicio.
     * @param service El servicio de seguro que se aprueba.
     * @param notes Notas opcionales sobre la aprobación.
     * @return La relación HospitalInsuranceService actualizada o recién creada, o null si ocurre un error.
     */
    public HospitalInsuranceService approveService(Hospital hospital, InsuranceService service, String notes) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            // Verificar si ya existe la relación
            Query<HospitalInsuranceService> query = session.createQuery(
                "FROM HospitalInsuranceService WHERE hospital = :hospital AND insuranceService = :service",
                HospitalInsuranceService.class
            );
            query.setParameter("hospital", hospital);
            query.setParameter("service", service);
            
            HospitalInsuranceService relation = query.uniqueResult();
            
            if (relation == null) {
                // Si no existe, crear una nueva
                relation = new HospitalInsuranceService();
                relation.setHospital(hospital);
                relation.setInsuranceService(service);
            }
            
            // Actualizar estado y datos
            relation.setApproved(1);
            relation.setApprovalDate(new Date());
            relation.setNotes(notes);
            
            session.saveOrUpdate(relation);
            
            tx.commit();
            return relation;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
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
     * Revoca la aprobación de un servicio de seguro para un hospital específico.
     * Establece el estado 'approved' a 0.
     *
     * @param hospital El hospital que revoca la aprobación.
     * @param service El servicio de seguro cuya aprobación se revoca.
     * @return true si la aprobación se revocó con éxito, false en caso contrario (p. ej., relación no encontrada o error).
     */
    public boolean revokeApproval(Hospital hospital, InsuranceService service) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            // Buscar la relación
            Query<HospitalInsuranceService> query = session.createQuery(
                "FROM HospitalInsuranceService WHERE hospital = :hospital AND insuranceService = :service",
                HospitalInsuranceService.class
            );
            query.setParameter("hospital", hospital);
            query.setParameter("service", service);
            
            HospitalInsuranceService relation = query.uniqueResult();
            
            if (relation != null) {
                relation.setApproved(0);
                session.update(relation);
                tx.commit();
                return true;
            }
            
            return false;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Busca todos los servicios de seguro aprobados para un hospital específico.
     *
     * @param hospital El hospital para el cual buscar servicios aprobados.
     * @return Una lista de objetos HospitalInsuranceService que representan los servicios aprobados, o null si ocurre un error.
     */
    public List<HospitalInsuranceService> findApprovedByHospital(Hospital hospital) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<HospitalInsuranceService> query = session.createQuery(
                "FROM HospitalInsuranceService WHERE hospital = :hospital AND approved = 1",
                HospitalInsuranceService.class
            );
            query.setParameter("hospital", hospital);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Busca todos los hospitales que ofrecen un servicio de seguro específico aprobado.
     *
     * @param service El servicio de seguro a buscar.
     * @return Una lista de objetos HospitalInsuranceService que representan los hospitales que ofrecen el servicio, o null si ocurre un error.
     */
    public List<HospitalInsuranceService> findHospitalsByService(InsuranceService service) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<HospitalInsuranceService> query = session.createQuery(
                "FROM HospitalInsuranceService WHERE insuranceService = :service AND approved = 1",
                HospitalInsuranceService.class
            );
            query.setParameter("service", service);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Busca una relación HospitalInsuranceService por su ID único.
     *
     * @param id El ID de la relación a buscar.
     * @return El objeto HospitalInsuranceService si se encuentra, null en caso contrario o si ocurre un error.
     */
    public HospitalInsuranceService findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(HospitalInsuranceService.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Elimina una relación HospitalInsuranceService por su ID único.
     *
     * @param id El ID de la relación a eliminar.
     * @return true si la relación se eliminó con éxito, false en caso contrario (p. ej., relación not found or error occurred).
     */
    public boolean delete(Long id) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            HospitalInsuranceService relation = session.get(HospitalInsuranceService.class, id);
            if (relation != null) {
                session.delete(relation);
                tx.commit();
                return true;
            }
            
            return false;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
} 