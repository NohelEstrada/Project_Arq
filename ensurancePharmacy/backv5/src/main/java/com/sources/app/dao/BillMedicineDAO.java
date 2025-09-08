package com.sources.app.dao;

import com.sources.app.entities.BillMedicine;
import com.sources.app.entities.BillMedicineId;
import com.sources.app.entities.Bill;
import com.sources.app.entities.Medicine;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) para gestionar entidades de enlace {@link BillMedicine}.
 * Esta clase proporciona métodos para realizar operaciones CRUD en registros BillMedicine,
 * que representan elementos individuales de medicamentos dentro de una {@link Bill}.
 * Utiliza Hibernate para interacciones con la base de datos y maneja la clave compuesta {@link BillMedicineId}.
 */
public class BillMedicineDAO {
    private static final Logger LOGGER = Logger.getLogger(BillMedicineDAO.class.getName());

    /**
     * Crea una nueva asociación entre una Factura (Bill) y un Medicamento (Medicine) en la base de datos.
     *
     * @param bill     La entidad {@link Bill} a enlazar.
     * @param medicine La entidad {@link Medicine} a enlazar.
     * @param quantity La cantidad del medicamento facturado.
     * @param cost     El costo por unidad del medicamento para este elemento de la factura.
     * @param copay    El monto del copago específico para este elemento (puede ser null).
     * @param total    El costo total para este elemento (cantidad * costo), almacenado como String.
     * @return La entidad {@link BillMedicine} recién creada, o null si ocurrió un error.
     */
    public BillMedicine create(Bill bill, Medicine medicine, Integer quantity, Double cost, Double copay, String total) {
        Transaction tx = null;
        BillMedicine billMedicine = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            billMedicine = new BillMedicine();
            billMedicine.setBill(bill);
            billMedicine.setMedicine(medicine);
            billMedicine.setQuantity(quantity);
            billMedicine.setCost(cost);
            billMedicine.setCopay(copay);
            billMedicine.setTotal(total);

            // La clave compuesta se asigna automáticamente en setBill y setMedicine
            session.persist(billMedicine);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, "Error creating BillMedicine", e);
        }
        return billMedicine;
    }

    /**
     * Recupera todos los registros BillMedicine de la base de datos.
     *
     * @return Una lista de todas las entidades {@link BillMedicine}, o null si ocurrió un error.
     */
    public List<BillMedicine> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BillMedicine> query = session.createQuery("FROM BillMedicine", BillMedicine.class);
            return query.list();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching all BillMedicine records", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera un registro BillMedicine específico por su clave primaria compuesta.
     *
     * @param id La clave compuesta {@link BillMedicineId} (billId, medicineId) del registro a recuperar.
     * @return La entidad {@link BillMedicine} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public BillMedicine getById(BillMedicineId id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(BillMedicine.class, id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching BillMedicine by id: " + id, e);
            return null;
        }
    }

    /**
     * Actualiza un registro BillMedicine existente en la base de datos.
     * Esto típicamente implica cambiar la cantidad, el costo o el copago.
     *
     * @param billMedicine La entidad {@link BillMedicine} con información actualizada. El ID compuesto debe coincidir con un registro existente.
     * @return La entidad {@link BillMedicine} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public BillMedicine update(BillMedicine billMedicine) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(billMedicine);
            tx.commit();
            return billMedicine;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, "Error updating BillMedicine", e);
            return null;
        }
    }
}
