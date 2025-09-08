package com.sources.app.dao;

import com.sources.app.entities.Bill;
import com.sources.app.entities.Prescription;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) para gestionar entidades {@link Bill}.
 * Esta clase proporciona métodos para realizar operaciones CRUD en Facturas (Bills),
 * que representan facturas asociadas con {@link Prescription}s. Utiliza Hibernate para interacciones con la base de datos.
 */
public class BillDAO {

    private static final Logger LOGGER = Logger.getLogger(BillDAO.class.getName());

    /**
     * Crea un nuevo registro de Factura (Bill) en la base de datos, vinculándolo a una Receta (Prescription).
     * Inicializa la factura con detalles financieros básicos.
     *
     * @param prescription La {@link Prescription} asociada con esta factura.
     * @param taxes        El monto de impuestos aplicado a la factura.
     * @param subtotal     El monto del subtotal antes de impuestos.
     * @param copay        El monto del copago requerido.
     * @param total        El monto total de la factura (almacenado como String).
     * @return La entidad {@link Bill} recién creada, o null si ocurrió un error.
     */
    public Bill create(Prescription prescription, Double taxes, Double subtotal, Double copay, String total) {
        Transaction tx = null;
        Bill bill = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            bill = new Bill();
            bill.setPrescription(prescription);
            bill.setTaxes(taxes);
            bill.setSubtotal(subtotal);
            bill.setCopay(copay);
            bill.setTotal(total);

            session.persist(bill);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, "Failed to create Bill", e);
        }
        return bill;
    }

    /**
     * Recupera todos los registros de Factura (Bill) de la base de datos.
     *
     * @return Una lista de todas las entidades {@link Bill}, o null si ocurrió un error.
     */
    public List<Bill> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Bill> query = session.createQuery("FROM Bill", Bill.class);
            return query.list();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch all Bills", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera un registro de Factura (Bill) específico por su identificador único.
     *
     * @param id El ID de la Factura (Bill) a recuperar.
     * @return La entidad {@link Bill} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public Bill getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Bill.class, id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch Bill by id: " + id, e);
            return null;
        }
    }

    /**
     * Recupera la Factura (Bill) asociada con un ID de Receta (Prescription) específico.
     * Útil para encontrar la factura generada para una receta en particular.
     *
     * @param prescriptionId El ID de la {@link Prescription}.
     * @return La entidad {@link Bill} asociada con el ID de receta dado, o null si no se encuentra o ocurrió un error.
     */
    public Bill getByPrescriptionId(Long prescriptionId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Bill> query = session.createQuery(
                "FROM Bill b WHERE b.prescription.id = :prescriptionId", 
                Bill.class
            );
            query.setParameter("prescriptionId", prescriptionId);
            return query.uniqueResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch Bill by prescriptionId: " + prescriptionId, e);
            return null;
        }
    }

    /**
     * Actualiza un registro de Factura (Bill) existente en la base de datos.
     *
     * @param bill La entidad {@link Bill} con información actualizada. El ID debe coincidir con un registro existente.
     * @return La entidad {@link Bill} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public Bill update(Bill bill) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(bill);
            tx.commit();
            return bill;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, "Failed to update Bill", e);
            return null;
        }
    }
}
