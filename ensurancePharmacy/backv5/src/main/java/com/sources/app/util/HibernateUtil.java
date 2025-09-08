package com.sources.app.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.sources.app.entities.User;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HibernateUtil {

    private static final Logger LOGGER = Logger.getLogger(HibernateUtil.class.getName());
    private static final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private HibernateUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration()
                    .configure(); // Carga hibernate.cfg.xml

            // Configurar esquema desde variable de entorno
            String dbSchema = System.getenv("DB_SCHEMA_PHARMACY");
            if (dbSchema != null && !dbSchema.trim().isEmpty()) {
                LOGGER.log(Level.INFO, () -> "Configurando esquema de BD para farmacia: " + dbSchema);
                configuration.setProperty("hibernate.default_schema", dbSchema);
            } else {
                LOGGER.log(Level.WARNING, () -> "Variable DB_SCHEMA_PHARMACY no definida, usando esquema por defecto");
            }

            return configuration
                    .addAnnotatedClass(User.class) // Registra la entidad User
                    .buildSessionFactory();
        } catch (Throwable ex) {
            LOGGER.log(Level.SEVERE, () -> "Error building SessionFactory: " + ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
