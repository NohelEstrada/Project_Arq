package com.sources.app.dao;

import com.sources.app.entities.SystemConfig;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar configuraciones del sistema almacenadas como entidades {@link SystemConfig}.
 * Proporciona métodos para crear, leer, actualizar y eliminar entradas de configuración (pares clave-valor),
 * y métodos auxiliares para recuperar valores de configuración con valores predeterminados. Utiliza Hibernate.
 */
public class SystemConfigDAO {

    /**
     * Guarda una nueva entrada de configuración o actualiza una existente basada en el configKey.
     * Si la clave existe, actualiza el valor y opcionalmente la descripción.
     * Si la clave no existe, crea un nuevo registro.
     *
     * @param configKey   La clave única que identifica la configuración.
     * @param configValue El valor asociado con la clave.
     * @param description Una descripción opcional para la configuración (si es null o está vacía, la descripción existente no se actualizará).
     * @return La entidad {@link SystemConfig} guardada o actualizada, o null si ocurrió un error.
     */
    public SystemConfig saveOrUpdate(String configKey, String configValue, String description) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Verificar si ya existe la configuración
            SystemConfig config = getByKey(configKey);
            
            if (config == null) {
                // Crear nueva configuración
                config = new SystemConfig();
                config.setConfigKey(configKey);
                config.setConfigValue(configValue);
                config.setDescription(description);
                session.save(config);
            } else {
                // Actualizar configuración existente
                config.setConfigValue(configValue);
                if (description != null && !description.isEmpty()) {
                    config.setDescription(description);
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
     * Recupera una entrada de configuración específica del sistema por su clave única.
     *
     * @param configKey La clave de la configuración a recuperar.
     * @return La entidad {@link SystemConfig} que coincide con la clave, o null si no se encuentra o ocurrió un error.
     */
    public SystemConfig getByKey(String configKey) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SystemConfig> query = session.createQuery(
                "FROM SystemConfig WHERE configKey = :key", SystemConfig.class);
            query.setParameter("key", configKey);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Recupera todas las entradas de configuración del sistema de la base de datos, ordenadas por configKey.
     *
     * @return Una lista de todas las entidades {@link SystemConfig}, o null si ocurrió un error.
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
     * Elimina una entrada de configuración del sistema de la base de datos por su ID.
     *
     * @param idConfig El ID de la entrada de configuración a eliminar.
     * @return {@code true} si la eliminación fue exitosa, {@code false} si la configuración no se encontró o ocurrió un error.
     */
    public boolean delete(Long idConfig) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            SystemConfig config = session.get(SystemConfig.class, idConfig);
            if (config != null) {
                session.delete(config);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Recupera el valor de una configuración como String, devolviendo un valor predeterminado si no se encuentra la clave.
     *
     * @param configKey    La clave de la configuración.
     * @param defaultValue El valor a devolver si la clave de configuración no existe.
     * @return El valor de la configuración como String, o el defaultValue.
     */
    public String getConfigValue(String configKey, String defaultValue) {
        SystemConfig config = getByKey(configKey);
        return (config != null) ? config.getConfigValue() : defaultValue;
    }
    
    /**
     * Recupera el valor de una configuración como Double, devolviendo un valor predeterminado si no se encuentra la clave
     * o si el valor almacenado no se puede parsear como Double.
     *
     * @param configKey    La clave de la configuración.
     * @param defaultValue El valor a devolver si la clave no existe o el valor no es un Double válido.
     * @return El valor de la configuración como Double, o el defaultValue.
     */
    public Double getConfigValueAsDouble(String configKey, Double defaultValue) {
        SystemConfig config = getByKey(configKey);
        if (config != null) {
            try {
                return Double.parseDouble(config.getConfigValue());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Inicializa configuraciones predeterminadas del sistema si aún no existen.
     * Actualmente asegura que la configuración 'MIN_PRESCRIPTION_AMOUNT' esté presente.
     * Este método puede expandirse para incluir otras configuraciones esenciales predeterminadas.
     */
    public void initializeDefaultConfigs() {
        // Configurar el monto mínimo para aprobación de recetas
        if (getByKey("MIN_PRESCRIPTION_AMOUNT") == null) {
            saveOrUpdate("MIN_PRESCRIPTION_AMOUNT", "250.00", 
                "Monto mínimo en quetzales para aprobar una receta");
        }
    }
} 