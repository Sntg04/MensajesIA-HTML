package com.ia.mensajes.agentemensajesia.config;

import com.ia.mensajes.agentemensajesia.ia.ClasificadorMensajes;
import com.ia.mensajes.agentemensajesia.model.Usuario;
import com.ia.mensajes.agentemensajesia.util.JPAUtil;
import com.ia.mensajes.agentemensajesia.util.PasswordHasherUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.Date;

@WebListener
public class InitialDataSeeder implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("INICIADOR: La aplicación web ha arrancado.");

        // --- PRECARGA DE MODELOS DE IA ---
        // Se inicia la carga en un hilo separado para no bloquear el arranque principal de la aplicación.
        new Thread(() -> {
            System.out.println("INICIADOR: Iniciando la carga de modelos de IA en segundo plano...");
            ClasificadorMensajes.getInstance().init();
            System.out.println("INICIADOR: Modelos de IA cargados y listos para usar.");
        }).start();
        
        System.out.println("INICIADOR: Verificando datos iniciales de la base de datos...");
        EntityManager em = null;
        try {
            // Se obtiene un EntityManager para la creación del usuario administrador.
            em = JPAUtil.getEntityManagerFactory().createEntityManager();

            // Se verifica si el usuario 'admin' ya existe.
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.username = :username", Long.class);
            query.setParameter("username", "admin");
            Long adminCount = query.getSingleResult();

            // Si el conteo es 0, se crea el usuario por defecto.
            if (adminCount == 0) {
                System.out.println(">>> Usuario 'admin' no encontrado. Creando usuario por defecto...");

                em.getTransaction().begin();

                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPasswordHash(PasswordHasherUtil.hashPassword("admin123"));
                admin.setRol("admin");
                admin.setNombreCompleto("Administrador del Sistema");
                admin.setActivo(true);
                admin.setFechaCreacion(new Date());

                em.persist(admin);
                em.getTransaction().commit();

                System.out.println(">>> ¡Usuario 'admin' creado exitosamente!");
            } else {
                System.out.println(">>> El usuario 'admin' ya existe en la base de datos.");
            }
        } catch (Throwable t) { 
            System.err.println("!!! ERROR CRÍTICO IRRECUPERABLE DURANTE LA INICIALIZACIÓN DE DATOS !!!");
            t.printStackTrace(); 
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Fallo catastrófico en InitialDataSeeder, la aplicación no puede arrancar.", t);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JPAUtil.shutdown();
        System.out.println("INICIADOR: La aplicación se ha detenido. EntityManagerFactory cerrado.");
    }
}