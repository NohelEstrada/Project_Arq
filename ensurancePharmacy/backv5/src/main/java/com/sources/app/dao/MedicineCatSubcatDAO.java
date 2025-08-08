package com.sources.app.dao;

import com.sources.app.entities.MedicineCatSubcat;
import com.sources.app.entities.MedicineCatSubcatId;
import com.sources.app.entities.Medicine;
import com.sources.app.entities.Category;
import com.sources.app.entities.Subcategory;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar entidades de enlace {@link MedicineCatSubcat}.
 * Esta clase proporciona métodos para realizar operaciones CRUD en la asociación
 * entre {@link Medicine}, {@link Category}, y {@link Subcategory}.
 * Utiliza Hibernate para interacciones con la base de datos y maneja la clave compuesta {@link MedicineCatSubcatId}.
 */
public class MedicineCatSubcatDAO {

    /**
     * Crea un nuevo registro de asociación que vincula un Medicamento, Categoría y Subcategoría.
     *
     * @param medicine    La entidad {@link Medicine} a enlazar.
     * @param category    La entidad {@link Category} a enlazar.
     * @param subcategory La entidad {@link Subcategory} a enlazar.
     * @return La entidad {@link MedicineCatSubcat} recién creada, o null si ocurrió un error.
     */
    public MedicineCatSubcat create(Medicine medicine, Category category, Subcategory subcategory) {
        Transaction tx = null;
        MedicineCatSubcat mcs = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            mcs = new MedicineCatSubcat();
            mcs.setMedicine(medicine);
            mcs.setCategory(category);
            mcs.setSubcategory(subcategory);

            session.save(mcs);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return mcs;
    }

    /**
     * Recupera todos los registros de asociación Medicamento-Categoría-Subcategoría de la base de datos.
     *
     * @return Una lista de todas las entidades {@link MedicineCatSubcat}, o null si ocurrió un error.
     */
    public List<MedicineCatSubcat> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MedicineCatSubcat> query = session.createQuery("FROM MedicineCatSubcat", MedicineCatSubcat.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera un registro de asociación Medicamento-Categoría-Subcategoría específico por su clave primaria compuesta.
     *
     * @param id La clave compuesta {@link MedicineCatSubcatId} (medicineId, categoryId, subcategoryId) a recuperar.
     * @return La entidad {@link MedicineCatSubcat} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public MedicineCatSubcat getById(MedicineCatSubcatId id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(MedicineCatSubcat.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro de asociación Medicamento-Categoría-Subcategoría existente.
     * Nota: La actualización de entidades de enlace puede ser menos común o estar restringida según la lógica de negocio,
     * ya que la propia clave primaria define la relación.
     *
     * @param mcs La entidad {@link MedicineCatSubcat} con información actualizada (si algún campo además del ID es actualizable).
     *            El ID compuesto debe coincidir con un registro existente.
     * @return La entidad {@link MedicineCatSubcat} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public MedicineCatSubcat update(MedicineCatSubcat mcs) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(mcs);
            tx.commit();
            return mcs;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }
}
