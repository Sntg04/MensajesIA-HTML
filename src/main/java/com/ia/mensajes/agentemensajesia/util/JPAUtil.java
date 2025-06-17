package com.ia.mensajes.agentemensajesia.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {
    // El nombre "AgenteMensajesIAPU" debe coincidir con persistence.xml
    private static final String PERSISTENCE_UNIT_NAME = "AgenteMensajesIAPU";

    // La instancia única y estática de EntityManagerFactory
    private static EntityManagerFactory factory;

    /**
     * Devuelve la instancia única de EntityManagerFactory.
     * La crea solo la primera vez que se llama.
     * @return La instancia de EntityManagerFactory.
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (factory == null) {
            // Sincronizado para ser seguro en entornos multihilo
            synchronized (JPAUtil.class) {
                if (factory == null) {
                    System.out.println("Creando EntityManagerFactory por primera vez...");
                    factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                    System.out.println("EntityManagerFactory creado.");
                }
            }
        }
        return factory;
    }

    /**
     * Cierra el EntityManagerFactory cuando la aplicación se detiene.
     */
    public static void shutdown() {
        if (factory != null) {
            System.out.println("Cerrando EntityManagerFactory...");
            factory.close();
            System.out.println("EntityManagerFactory cerrado.");
        }
    }
}