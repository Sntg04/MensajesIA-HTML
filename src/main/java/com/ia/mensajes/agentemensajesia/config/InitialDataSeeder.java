package com.ia.mensajes.agentemensajesia.config;

import com.ia.mensajes.agentemensajesia.model.Usuario;
import com.ia.mensajes.agentemensajesia.util.JPAUtil;
import com.ia.mensajes.agentemensajesia.util.PasswordHasherUtil; // Asegúrate de importar esto
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
        System.out.println("INICIADOR: La aplicación web ha arrancado. Verificando datos iniciales...");
        EntityManager em = null;
        try {
            // Obtenemos un EntityManager directamente de la utilidad JPA
            em = JPAUtil.getEntityManagerFactory().createEntityManager();

            // Verificamos si el usuario 'admin' ya existe de una forma más directa
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.username = :username", Long.class);
            query.setParameter("username", "admin");
            Long adminCount = query.getSingleResult();

            // Si no existe, lo creamos
            if (adminCount == 0) {
                System.out.println(">>> Usuario 'admin' no encontrado. Creando usuario por defecto...");

                // Iniciamos una transacción
                em.getTransaction().begin();

                Usuario admin = new Usuario();
                admin.setUsername("admin");
                
                // Hasheamos la contraseña directamente usando la clase de utilidad
                admin.setPasswordHash(PasswordHasherUtil.hashPassword("admin123"));
                
                admin.setRol("admin");
                admin.setNombreCompleto("Administrador del Sistema");
                admin.setActivo(true);
                admin.setFechaCreacion(new Date());

                // Persistimos el nuevo usuario
                em.persist(admin);
                
                // Confirmamos la transacción
                em.getTransaction().commit();

                System.out.println(">>> ¡Usuario 'admin' creado exitosamente!");
            } else {
                System.out.println(">>> El usuario 'admin' ya existe en la base de datos.");
            }
        } catch (Throwable t) { // Usamos Throwable para capturar cualquier tipo de error posible
            System.err.println("!!! ERROR CRÍTICO IRRECUPERABLE DURANTE LA INICIALIZACIÓN DE DATOS !!!");
            t.printStackTrace(); // Imprimimos el error completo
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // Lanzamos una excepción para que el fallo del despliegue sea evidente
            throw new RuntimeException("Fallo catastrófico en InitialDataSeeder, la aplicación no puede arrancar.", t);
        } finally {
            // Cerramos el EntityManager
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