package com.sources.app.dao;

import com.sources.app.entities.Medicine;
import com.sources.app.entities.Pharmacy;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades Medicine (Medicamento).
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar) sobre los medicamentos.
 */
public class MedicineDAO {

    /**
     * Crea un nuevo medicamento en la base de datos.
     *
     * @param name Nombre del medicamento.
     * @param description Descripción del medicamento.
     * @param price Precio del medicamento.
     * @param pharmacy Farmacia a la que pertenece el medicamento (objeto Pharmacy).
     * @param enabled Estado de habilitación (1 habilitado, 0 deshabilitado).
     * @param activePrinciple Principio activo del medicamento.
     * @param presentation Presentación del medicamento (p. ej., tabletas, jarabe).
     * @param stock Cantidad en inventario.
     * @param brand Marca del medicamento.
     * @param coverage Indicador de cobertura (p. ej., 1 si está cubierto por seguro, 0 si no).
     * @return El objeto Medicine creado, o null si ocurre un error.
     */
    public Medicine create(String name, String description, BigDecimal price, Pharmacy pharmacy,
                           Integer enabled, String activePrinciple, String presentation,
                           Integer stock, String brand, Integer coverage) {
        Transaction tx = null;
        Medicine medicine = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            medicine = new Medicine();
            medicine.setName(name);
            medicine.setDescription(description);
            medicine.setPrice(price);
            medicine.setPharmacy(pharmacy); // Se asigna el objeto Pharmacy
            medicine.setEnabled(enabled);
            medicine.setActivePrinciple(activePrinciple);
            medicine.setPresentation(presentation);
            medicine.setStock(stock);
            medicine.setBrand(brand);
            medicine.setCoverage(coverage); // Se asigna el atributo coverage

            session.save(medicine);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return medicine;
    }

    /**
     * Busca un medicamento por su ID único.
     *
     * @param id El ID del medicamento a buscar.
     * @return El objeto Medicine encontrado, o null si no se encuentra o si ocurre un error.
     */
    public Medicine findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Medicine.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todos los medicamentos de la base de datos.
     *
     * @return Una lista de todos los objetos Medicine, o null si ocurre un error.
     */
    public List<Medicine> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Medicine> query = session.createQuery("FROM Medicine", Medicine.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un medicamento existente en la base de datos.
     *
     * @param medicine El objeto Medicine con los datos actualizados.
     * @return El objeto Medicine actualizado, o null si ocurre un error.
     */
    public Medicine update(Medicine medicine) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(medicine);
            tx.commit();
            return medicine;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}
