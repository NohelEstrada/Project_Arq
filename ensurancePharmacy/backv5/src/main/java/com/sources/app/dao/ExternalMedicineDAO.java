package com.sources.app.dao;

import com.sources.app.entities.Medicine;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para recuperar datos de medicamentos.
 * <p>
 * Nota: A pesar del nombre "External", este DAO actualmente recupera todos los medicamentos
 * de la tabla local {@link Medicine} usando Hibernate.
 * </p>
 */
public class ExternalMedicineDAO {
    
    /**
     * Recupera una lista de todas las entidades {@link Medicine} de la base de datos.
     *
     * @return Una lista que contiene todos los registros de Medicamentos, o null si ocurre un error durante la consulta.
     */
    public List<Medicine> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Medicine> query = session.createQuery("FROM Medicine", Medicine.class);
            return query.list();
        }
    }
}
