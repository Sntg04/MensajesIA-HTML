package com.ia.mensajes.agentemensajesia.config; // <-- Paquete corregido

import com.ia.mensajes.agentemensajesia.model.Usuario;
import com.ia.mensajes.agentemensajesia.services.UsuarioService;
import com.ia.mensajes.agentemensajesia.util.JPAUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.List;

@WebListener
public class InitialDataSeeder implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=======================================================");
        System.out.println("INICIADOR: La aplicación web ha arrancado.");
        System.out.println("=======================================================");

        try {
            JPAUtil.getEntityManagerFactory();
        } catch (Exception e) {
            System.err.println("No se pudo inicializar la conexión con la BD en el Seeder.");
            return;
        }

        UsuarioService usuarioService = new UsuarioService();
        
        try {
            // Llama al método corregido del servicio
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            
            if (usuarios == null || usuarios.isEmpty()) {
                System.out.println(">>> La base de datos está vacía. Creando usuario 'admin' por defecto...");
                
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPasswordHash("admin123"); 
                admin.setRol("admin");
                admin.setNombreCompleto("Administrador del Sistema");
                
                // Llama al método corregido del servicio
                usuarioService.crearUsuario(admin);
                System.out.println(">>> ¡Usuario 'admin' con contraseña 'admin123' creado exitosamente!");
                
            } else {
                System.out.println(">>> La base de datos ya contiene usuarios. No se requiere la creación de datos iniciales.");
            }
        } catch (Exception e) {
             System.err.println("!!! ERROR CRÍTICO DURANTE LA CREACIÓN DEL USUARIO ADMIN !!!");
             e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JPAUtil.shutdown();
    }
}
