package com.sources.app.dao;

import com.sources.app.entities.TotalPharmacy;
import com.sources.app.entities.Pharmacy;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar las entidades de TotalPharmacy.
 * Representa los totales diarios para cada farmacia.
 * Proporciona métodos para crear, buscar y actualizar estos totales.
 */
public class TotalPharmacyDAO {

    /**
     * Crea un nuevo registro de total diario para una farmacia específica.
     *
     * @param idPharmacy El ID de la farmacia para la cual se registra el total.
     * @param totalDate La fecha a la que corresponde el total.
     * @param total El monto total registrado para esa fecha.
     * @return El objeto TotalPharmacy creado, o null si ocurre un error (p. ej., la farmacia no existe).
     */
    public TotalPharmacy create(Long idPharmacy, Date totalDate, BigDecimal total) {
        Transaction tx = null;
        TotalPharmacy tp = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Recuperar la entidad Pharmacy a partir de su id
            Pharmacy pharmacy = session.get(Pharmacy.class, idPharmacy);
            if (pharmacy == null) {
                throw new RuntimeException("Pharmacy no encontrada para id: " + idPharmacy);
            }

            tp = new TotalPharmacy();
            tp.setPharmacy(pharmacy);  // Asignar la entidad Pharmacy
            tp.setTotalDate(totalDate);
            tp.setTotal(total);

            session.save(tp);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return tp;
    }

    /**
     * Busca un registro de TotalPharmacy por su ID único.
     *
     * @param id El ID del registro TotalPharmacy a buscar.
     * @return El objeto TotalPharmacy encontrado, o null si no se encuentra o si ocurre un error.
     */
    public TotalPharmacy findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(TotalPharmacy.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todos los registros de TotalPharmacy de la base de datos.
     *
     * @return Una lista de todos los objetos TotalPharmacy, o null si ocurre un error.
     */
    public List<TotalPharmacy> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<TotalPharmacy> query = session.createQuery("FROM TotalPharmacy", TotalPharmacy.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro existente de TotalPharmacy en la base de datos.
     *
     * @param tp El objeto TotalPharmacy con los datos actualizados.
     * @return El objeto TotalPharmacy actualizado, o null si ocurre un error.
     */
    public TotalPharmacy update(TotalPharmacy tp) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(tp);
            tx.commit();
            return tp;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
}
