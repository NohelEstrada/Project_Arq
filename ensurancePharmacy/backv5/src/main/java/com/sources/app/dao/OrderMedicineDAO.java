package com.sources.app.dao;

import com.sources.app.entities.OrderMedicine;
import com.sources.app.entities.OrderMedicineId;
import com.sources.app.entities.Orders;
import com.sources.app.entities.Medicine;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar entidades de enlace {@link OrderMedicine}.
 * Esta clase proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * en registros OrderMedicine, que representan elementos individuales de medicamentos dentro de un {@link Orders}.
 * Utiliza Hibernate para interacciones con la base de datos y maneja la clave compuesta {@link OrderMedicineId}.
 */
public class OrderMedicineDAO {

    /**
     * Crea una nueva asociación entre un Pedido (Order) y un Medicamento (Medicine) en la base de datos.
     *
     * @param orders   La entidad {@link Orders} a enlazar.
     * @param medicine La entidad {@link Medicine} a enlazar.
     * @param quantity La cantidad del medicamento pedido.
     * @param cost     El costo por unidad del medicamento para este elemento del pedido.
     * @param total    El costo total para este elemento (cantidad * costo), almacenado como String.
     * @return La entidad {@link OrderMedicine} recién creada, o null si ocurrió un error.
     */
    public OrderMedicine create(Orders orders, Medicine medicine, Integer quantity, Double cost, String total) {
        Transaction tx = null;
        OrderMedicine om = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            om = new OrderMedicine();
            om.setOrders(orders);
            om.setMedicine(medicine);
            om.setQuantity(quantity);
            om.setCost(cost);
            om.setTotal(total);

            session.save(om);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return om;
    }

    /**
     * Recupera todos los registros OrderMedicine de la base de datos.
     *
     * @return Una lista de todas las entidades {@link OrderMedicine}, o null si ocurrió un error.
     */
    public List<OrderMedicine> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<OrderMedicine> query = session.createQuery("FROM OrderMedicine", OrderMedicine.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Elimina un registro OrderMedicine de la base de datos basado en su clave primaria compuesta.
     *
     * @param id La clave compuesta {@link OrderMedicineId} (orderId, medicineId) del registro a eliminar.
     * @return {@code true} si la eliminación fue exitosa, {@code false} si la entidad no se encontró o ocurrió un error.
     */
    public boolean deleteById(OrderMedicineId id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            OrderMedicine om = session.get(OrderMedicine.class, id);
            if (om != null) {
                session.delete(om);
                tx.commit();
                return true;
            } else {
                tx.rollback();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recupera un registro OrderMedicine específico por su clave primaria compuesta.
     *
     * @param id La clave compuesta {@link OrderMedicineId} (orderId, medicineId) del registro a recuperar.
     * @return La entidad {@link OrderMedicine} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public OrderMedicine getById(OrderMedicineId id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(OrderMedicine.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro OrderMedicine existente en la base de datos.
     *
     * @param om La entidad {@link OrderMedicine} con información actualizada. El ID compuesto debe coincidir con un registro existente.
     * @return La entidad {@link OrderMedicine} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public OrderMedicine update(OrderMedicine om) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(om);
            tx.commit();
            return om;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }
}
