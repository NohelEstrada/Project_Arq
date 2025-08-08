package com.sources.app.dao;

import com.sources.app.entities.Medicine;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar entidades {@link Medicine}.
 * Esta clase proporciona métodos para realizar operaciones CRUD en registros de Medicamentos,
 * que representan los productos farmacéuticos disponibles. Utiliza Hibernate para interacciones con la base de datos.
 */
public class MedicineDAO {

    /**
     * Crea un nuevo registro de Medicamento en la base de datos.
     *
     * @param name             El nombre comercial del medicamento.
     * @param activeMedicament El(los) principio(s) activo(s) del medicamento.
     * @param description      Una descripción del medicamento.
     * @param image            URL o ruta a una imagen del medicamento.
     * @param concentration    La concentración del principio activo.
     * @param presentacion     Los detalles de la presentación (p. ej., tamaño del envase, volumen).
     * @param stock            La cantidad actual en stock disponible.
     * @param brand            La marca o fabricante del medicamento.
     * @param prescription     Indica si el medicamento requiere receta (true/false).
     * @param price            El precio unitario del medicamento.
     * @param soldUnits        El número inicial de unidades vendidas (típicamente 0).
     * @return La entidad {@link Medicine} recién creada, o null si ocurrió un error.
     */
    public Medicine create(String name, String activeMedicament, String description, String image,
                           String concentration, Double presentacion, Integer stock, String brand,
                           Boolean prescription, Double price, Integer soldUnits) {
        Transaction tx = null;
        Medicine med = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            med = new Medicine();
            med.setName(name);
            med.setActiveMedicament(activeMedicament);
            med.setDescription(description);
            med.setImage(image);
            med.setConcentration(concentration);
            med.setPresentacion(presentacion);
            med.setStock(stock);
            med.setBrand(brand);
            med.setPrescription(prescription);
            med.setPrice(price);
            med.setSoldUnits(soldUnits);

            session.save(med);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return med;
    }

    /**
     * Recupera todos los registros de Medicamento de la base de datos.
     *
     * @return Una lista de todas las entidades {@link Medicine}, o null si ocurrió un error.
     */
    public List<Medicine> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Medicine> query = session.createQuery("FROM Medicine", Medicine.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera un registro de Medicamento específico por su identificador único.
     *
     * @param id El ID del Medicamento a recuperar.
     * @return La entidad {@link Medicine} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public Medicine getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Medicine.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro de Medicamento existente en la base de datos.
     *
     * @param medicine La entidad {@link Medicine} con información actualizada. El ID debe coincidir con un registro existente.
     * @return La entidad {@link Medicine} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public Medicine update(Medicine medicine) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(medicine);
            tx.commit();
            return medicine;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }
}
