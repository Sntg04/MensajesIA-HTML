package com.ia.mensajes.agentemensajesia.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class JPAUtil {
    private static final String PERSISTENCE_UNIT_NAME = "AgenteMensajesIAPU";
    private static EntityManagerFactory factory;

    public static EntityManagerFactory getEntityManagerFactory() {
        if (factory == null) {
            synchronized (JPAUtil.class) {
                if (factory == null) {
                    try {
                        // Leer las credenciales de las variables de entorno de Render
                        String dbUrl = System.getenv("DB_URL");
                        String dbUser = System.getenv("DB_USER");
                        String dbPassword = System.getenv("DB_PASSWORD");

                        if (dbUrl == null || dbUser == null || dbPassword == null) {
                            throw new IllegalStateException("Las variables de entorno de la base de datos (DB_URL, DB_USER, DB_PASSWORD) no están configuradas.");
                        }

                        Map<String, String> properties = new HashMap<>();
                        properties.put("jakarta.persistence.jdbc.url", dbUrl);
                        properties.put("jakarta.persistence.jdbc.user", dbUser);
                        properties.put("jakarta.persistence.jdbc.password", dbPassword);

                        System.out.println("Creando EntityManagerFactory con configuración de Render...");
                        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
                        System.out.println("EntityManagerFactory creado con éxito.");

                    } catch (Exception e) {
                        System.err.println("Error crítico al inicializar EntityManagerFactory: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return factory;
    }

    public static void shutdown() {
        if (factory != null) {
            System.out.println("Cerrando EntityManagerFactory...");
            factory.close();
            System.out.println("EntityManagerFactory cerrado.");
        }
    }
}