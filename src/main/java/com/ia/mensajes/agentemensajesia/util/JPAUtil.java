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
                    System.out.println("INICIANDO JPAUtil: Creando EntityManagerFactory...");
                    try {
                        String dbUrlFromEnv = System.getenv("DB_URL");
                        String dbUser = System.getenv("DB_USER");
                        String dbPassword = System.getenv("DB_PASSWORD");

                        if (dbUrlFromEnv == null || dbUser == null || dbPassword == null) {
                            throw new IllegalStateException("Las variables de entorno de la base de datos (DB_URL, DB_USER, DB_PASSWORD) no están configuradas.");
                        }

                        // --- CORRECCIÓN DE ROBUSTEZ ---
                        // Asegura que la URL tenga el prefijo jdbc: requerido por Java
                        String finalDbUrl = dbUrlFromEnv;
                        if (!finalDbUrl.startsWith("jdbc:")) {
                            // Convierte la URL de Render (ej. postgresql://...) a formato JDBC (ej. jdbc:postgresql://...)
                            finalDbUrl = "jdbc:" + finalDbUrl;
                        }
                        // ===================================
                        
                        System.out.println("Intentando conectar a la base de datos con URL: " + finalDbUrl);

                        Map<String, String> properties = new HashMap<>();
                        properties.put("jakarta.persistence.jdbc.url", finalDbUrl);
                        properties.put("jakarta.persistence.jdbc.user", dbUser);
                        properties.put("jakarta.persistence.jdbc.password", dbPassword);
                        
                        // Estas propiedades se leen desde persistence.xml, pero las mantenemos aquí por si acaso
                        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
                        properties.put("hibernate.hbm2ddl.auto", "update");

                        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
                        System.out.println("¡Éxito! EntityManagerFactory creado y conectado a la base de datos.");

                    } catch (Exception e) {
                        System.err.println("--- ERROR CRÍTICO AL INICIALIZAR ENTITYMANAGERFACTORY ---");
                        e.printStackTrace();
                        System.err.println("----------------------------------------------------------");
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return factory;
    }

    public static void shutdown() {
        if (factory != null && factory.isOpen()) {
            System.out.println("Cerrando EntityManagerFactory...");
            factory.close();
        }
    }
}
