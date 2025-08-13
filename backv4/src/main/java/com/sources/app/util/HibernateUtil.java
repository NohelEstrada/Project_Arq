package com.sources.app.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import com.sources.app.entities.User;

/**
 * Clase de utilidad para gestionar la SessionFactory de Hibernate.
 * Sigue el patrón Singleton para asegurar una única instancia de SessionFactory.
 * Permite cambiar entre Oracle y SQLite según la variable de entorno DB_TYPE.
 */
public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * Constructor privado para prevenir la instanciación de la clase de utilidad.
     */
    private HibernateUtil() {}

    private static SessionFactory buildSessionFactory() {
        try {
            // Determinar qué configuración usar basado en la variable de entorno
            String dbType = System.getProperty("db.type", System.getenv("DB_TYPE"));
            String configFile = "hibernate.cfg.xml"; // Oracle por defecto
            
            if ("sqlite".equalsIgnoreCase(dbType)) {
                configFile = "hibernate-sqlite.cfg.xml";
                System.out.println("Using SQLite database configuration");
            } else {
                System.out.println("Using Oracle database configuration");
            }
            
            return new Configuration()
                    .configure(configFile) // Carga el archivo de configuración apropiado
                    .addAnnotatedClass(User.class) // Registra la entidad User
                    .buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Failed to create SessionFactory: " + ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Obtiene la instancia Singleton de SessionFactory.
     *
     * @return La SessionFactory configurada.
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Configura los headers CORS para una petición
     * @param exchange el intercambio HTTP donde agregar los headers
     */
    public static void setCorsHeaders(com.sun.net.httpserver.HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
