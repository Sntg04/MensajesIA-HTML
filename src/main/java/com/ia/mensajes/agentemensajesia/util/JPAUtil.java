package com.ia.mensajes.agentemensajesia.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.net.URI;
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

                        if (dbUrlFromEnv == null || dbUrlFromEnv.trim().isEmpty()) {
                             throw new IllegalStateException("La variable de entorno DB_URL no está configurada.");
                        }

                        URI dbUri = new URI(dbUrlFromEnv);

                        String userInfo = dbUri.getUserInfo();
                        if (userInfo == null || !userInfo.contains(":")) {
                            throw new Exception("La URL de la BD no contiene 'usuario:contraseña'.");
                        }

                        String username = userInfo.substring(0, userInfo.indexOf(':'));
                        String password = userInfo.substring(userInfo.indexOf(':') + 1);

                        int port = dbUri.getPort();
                        if (port == -1) {
                            System.out.println("Puerto no detectado en la URL, usando puerto por defecto 5432.");
                            port = 5432;
                        }

                        String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + port + dbUri.getPath();

                        if (dbUri.getQuery() == null || !dbUri.getQuery().contains("sslmode")) {
                           jdbcUrl += "?sslmode=require";
                        } else {
                           jdbcUrl += "?" + dbUri.getQuery();
                        }

                        System.out.println("URL JDBC construida: " + jdbcUrl);
                        System.out.println("Usuario detectado: " + username);

                        Map<String, String> properties = new HashMap<>();
                        properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
                        properties.put("jakarta.persistence.jdbc.user", username);
                        properties.put("jakarta.persistence.jdbc.password", password);

                        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
                        System.out.println("¡ÉXITO! EntityManagerFactory creado y conectado a la base de datos.");

                    } catch (Exception e) {
                        System.err.println("--- ERROR CRÍTICO AL INICIALIZAR ENTITYMANAGERFACTORY ---");
                        e.printStackTrace();
                        throw new RuntimeException("Fallo al inicializar JPA", e);
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
            factory = null;
        }
    }
}