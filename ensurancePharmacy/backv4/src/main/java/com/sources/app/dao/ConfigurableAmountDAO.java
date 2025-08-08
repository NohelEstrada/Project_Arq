package com.sources.app.dao;

import com.sources.app.entities.ConfigurableAmount;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar la entidad ConfigurableAmount.
 * Esta entidad almacena montos configurables, como el monto máximo para recetas.
 * Se asume que normalmente habrá una sola fila de configuración.
 * Proporciona métodos para crear, buscar, actualizar y obtener la configuración actual.
 */
public class ConfigurableAmountDAO {

    /**
     * Constructor por defecto para ConfigurableAmountDAO.
     */
    public ConfigurableAmountDAO() {}

    /**
     * Crea un nuevo registro de monto configurable.
     * Dado que se espera una única configuración, este método podría usarse 
     * principalmente para la inicialización si no existe ninguna configuración.
     *
     * @param prescriptionAmount El monto máximo configurable para las recetas.
     * @return El objeto ConfigurableAmount creado, o null si ocurre un error.
     */
    public ConfigurableAmount create(BigDecimal prescriptionAmount) {
        Transaction tx = null;
        ConfigurableAmount confAmount = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            confAmount = new ConfigurableAmount();
            confAmount.setPrescriptionAmount(prescriptionAmount);

            session.save(confAmount);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
        return confAmount;
    }

    /**
     * Busca un registro de ConfigurableAmount por su ID único.
     * Generalmente se usará `findCurrentConfig` en lugar de este método.
     *
     * @param id El ID del registro a buscar.
     * @return El objeto ConfigurableAmount encontrado, o null si no se encuentra o si ocurre un error.
     */
    public ConfigurableAmount findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(ConfigurableAmount.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera todos los registros de ConfigurableAmount. 
     * Usualmente solo debería haber uno.
     *
     * @return Una lista de todos los objetos ConfigurableAmount, o null si ocurre un error.
     */
    public List<ConfigurableAmount> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ConfigurableAmount> query = session.createQuery("FROM ConfigurableAmount", ConfigurableAmount.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro existente de ConfigurableAmount.
     *
     * @param confAmount El objeto ConfigurableAmount con los datos actualizados.
     * @return El objeto ConfigurableAmount actualizado, o null si ocurre un error.
     */
    public ConfigurableAmount update(ConfigurableAmount confAmount) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(confAmount);
            tx.commit();
            return confAmount;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca la configuración de monto actual (se asume que solo existe una).
     * Si no se encuentra ninguna configuración, crea una por defecto con un valor predeterminado.
     *
     * @return El objeto ConfigurableAmount actual, o uno nuevo si no existe.
     */
    public ConfigurableAmount findCurrentConfig() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ConfigurableAmount> query = session.createQuery("FROM ConfigurableAmount", ConfigurableAmount.class);
            query.setMaxResults(1); // Solo nos interesa la primera fila
            ConfigurableAmount confAmount = query.uniqueResult();
            if (confAmount == null) {
                // Si no existe, crear una con valor por defecto
                System.out.println("No se encontró configuración, creando una por defecto con Q250.00");
                return create(new BigDecimal("250.00"));
            }
            return confAmount;
        } catch (Exception e) {
            e.printStackTrace();
            // Si no existe, crear una con valor por defecto
            System.out.println("No se encontró configuración, creando una por defecto con Q250.00");
            return create(new BigDecimal("250.00"));
        }
    }
}
