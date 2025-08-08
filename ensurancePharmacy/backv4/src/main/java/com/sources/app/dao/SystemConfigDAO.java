package com.sources.app.dao;

import com.sources.app.entities.SystemConfig;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar la configuración del sistema (SystemConfig).
 * Almacena pares clave-valor para parámetros de configuración.
 * Proporciona métodos para guardar, obtener, eliminar y convertir configuraciones.
 */
public class SystemConfigDAO {

    /**
     * Guarda una nueva configuración o actualiza una existente si la clave ya existe.
     *
     * @param key La clave única de la configuración.
     * @param value El valor de la configuración.
     * @param description Una descripción opcional de la configuración.
     * @return El objeto SystemConfig guardado o actualizado, o null si ocurre un error.
     */
    public SystemConfig saveOrUpdate(String key, String value, String description) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Buscar si ya existe la configuración
            Query<SystemConfig> query = session.createQuery(
                "FROM SystemConfig WHERE configKey = :key", SystemConfig.class);
            query.setParameter("key", key);
            SystemConfig config = query.uniqueResult();
            
            if (config == null) {
                // Crear nueva configuración
                config = new SystemConfig(key, value, description);
                session.save(config);
            } else {
                // Actualizar configuración existente
                config.setConfigValue(value);
                if (description != null) {
                    config.setConfigDescription(description);
                }
                session.update(config);
            }
            
            transaction.commit();
            return config;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene un registro de configuración completo por su clave.
     *
     * @param key La clave de la configuración a buscar.
     * @return El objeto SystemConfig encontrado, o null si no existe o si ocurre un error.
     */
    public SystemConfig getByKey(String key) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SystemConfig> query = session.createQuery(
                "FROM SystemConfig WHERE configKey = :key", SystemConfig.class);
            query.setParameter("key", key);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene el valor de una configuración como String, devolviendo un valor por defecto si no se encuentra.
     *
     * @param key La clave de la configuración.
     * @param defaultValue El valor a devolver si la clave no existe.
     * @return El valor de la configuración como String, o el valor por defecto.
     */
    public String getConfigValue(String key, String defaultValue) {
        SystemConfig config = getByKey(key);
        return config != null ? config.getConfigValue() : defaultValue;
    }
    
    /**
     * Obtiene el valor de una configuración como Integer, devolviendo un valor por defecto si no se encuentra o no se puede convertir.
     *
     * @param key La clave de la configuración.
     * @param defaultValue El valor a devolver si la clave no existe o el valor no es un entero válido.
     * @return El valor de la configuración como Integer, o el valor por defecto.
     */
    public Integer getConfigValueAsInt(String key, Integer defaultValue) {
        String value = getConfigValue(key, null);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Obtiene el valor de una configuración como Double, devolviendo un valor por defecto si no se encuentra o no se puede convertir.
     *
     * @param key La clave de la configuración.
     * @param defaultValue El valor a devolver si la clave no existe o el valor no es un número decimal válido.
     * @return El valor de la configuración como Double, o el valor por defecto.
     */
    public Double getConfigValueAsDouble(String key, Double defaultValue) {
        String value = getConfigValue(key, null);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Obtiene el valor de una configuración como Boolean, devolviendo un valor por defecto si no se encuentra.
     * Considera "true", "1", "yes" (ignorando mayúsculas/minúsculas) como verdadero.
     *
     * @param key La clave de la configuración.
     * @param defaultValue El valor a devolver si la clave no existe.
     * @return El valor de la configuración como Boolean, o el valor por defecto.
     */
    public Boolean getConfigValueAsBoolean(String key, Boolean defaultValue) {
        String value = getConfigValue(key, null);
        if (value == null) {
            return defaultValue;
        }
        
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }
    
    /**
     * Elimina una configuración de la base de datos por su clave.
     *
     * @param key La clave de la configuración a eliminar.
     * @return true si la configuración fue eliminada con éxito, false en caso contrario.
     */
    public boolean delete(String key) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Query<?> query = session.createQuery("DELETE FROM SystemConfig WHERE configKey = :key");
            query.setParameter("key", key);
            int result = query.executeUpdate();
            
            transaction.commit();
            return result > 0;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene todos los registros de configuración de la base de datos, ordenados por clave.
     *
     * @return Una lista de todos los objetos SystemConfig, o null si ocurre un error.
     */
    public List<SystemConfig> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SystemConfig> query = session.createQuery(
                "FROM SystemConfig ORDER BY configKey", SystemConfig.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Inicializa las configuraciones por defecto si no existen
     */
    public void initDefaultConfigs() {
        // Monto mínimo para aprobación de recetas
        if (getByKey("MIN_PRESCRIPTION_AMOUNT") == null) {
            saveOrUpdate("MIN_PRESCRIPTION_AMOUNT", "250.00", 
                "Monto mínimo para la aprobación de recetas en quetzales");
        }
        
        // Otras configuraciones por defecto que se necesiten
    }
} 