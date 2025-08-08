package com.sources.app.dao;

import com.sources.app.entities.ServiceCategory;
import com.sources.app.entities.ServiceCategoryId;
import com.sources.app.entities.Service;
import com.sources.app.entities.Category;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar la entidad de relación ServiceCategory.
 * Representa la asociación entre Servicios (Service) y Categorías (Category).
 * Proporciona métodos para crear, buscar y actualizar estas relaciones.
 */
public class ServiceCategoryDAO {

    /**
     * Crea una nueva relación entre un servicio y una categoría.
     *
     * @param idService El ID del servicio.
     * @param idCategory El ID de la categoría.
     * @return El objeto ServiceCategory creado, o null si ocurre un error (p. ej., el servicio o la categoría no existen).
     */
    public ServiceCategory create(Long idService, Long idCategory) {
        Transaction tx = null;
        ServiceCategory serviceCategory = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Recuperar las entidades relacionadas
            Service service = session.get(Service.class, idService);
            Category category = session.get(Category.class, idCategory);

            if (service == null || category == null) {
                throw new RuntimeException("Service o Category no encontrado.");
            }

            serviceCategory = new ServiceCategory();
            serviceCategory.setService(service);
            serviceCategory.setCategory(category);

            session.save(serviceCategory);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return serviceCategory;
    }

    /**
     * Busca una relación ServiceCategory por su ID compuesto (ID de servicio e ID de categoría).
     *
     * @param idService El ID del servicio.
     * @param idCategory El ID de la categoría.
     * @return El objeto ServiceCategory encontrado, o null si no se encuentra o si ocurre un error.
     */
    public ServiceCategory findById(Long idService, Long idCategory) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            ServiceCategoryId scId = new ServiceCategoryId(idService, idCategory);
            return session.get(ServiceCategory.class, scId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todas las relaciones ServiceCategory de la base de datos.
     *
     * @return Una lista de todos los objetos ServiceCategory, o null si ocurre un error.
     */
    public List<ServiceCategory> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ServiceCategory> query = session.createQuery("FROM ServiceCategory", ServiceCategory.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza una relación ServiceCategory existente en la base de datos.
     * Nota: Dado que la clave primaria es compuesta y define la relación, 
     * generalmente no hay atributos adicionales en ServiceCategory para actualizar.
     * Este método podría ser útil si se añaden más campos a la tabla de unión.
     *
     * @param serviceCategory El objeto ServiceCategory con los datos actualizados.
     * @return El objeto ServiceCategory actualizado, o null si ocurre un error.
     */
    public ServiceCategory update(ServiceCategory serviceCategory) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(serviceCategory);
            tx.commit();
            return serviceCategory;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}
