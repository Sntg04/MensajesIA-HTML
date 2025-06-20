package com.ia.mensajes.agentemensajesia.config;

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
        System.out.println("Verificando si se necesitan datos iniciales...");
        System.out.println("=======================================================");

        // Forzamos la inicialización de JPAUtil para que cree la conexión y las tablas
        try {
            JPAUtil.getEntityManagerFactory();
        } catch (Exception e) {
            System.err.println("No se pudo inicializar la conexión con la BD en el Seeder. El resto del proceso fallará.");
            return;
        }

        UsuarioService usuarioService = new UsuarioService();
        
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            
            if (usuarios == null || usuarios.isEmpty()) {
                System.out.println(">>> La base de datos no tiene usuarios. Creando usuario 'admin' por defecto...");
                
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                // La contraseña en texto plano se pasa aquí, el servicio se encarga de hashearla
                admin.setPasswordHash("admin123"); 
                admin.setRol("admin");
                admin.setNombreCompleto("Administrador del Sistema");
                admin.setActivo(true);
                
                usuarioService.crearUsuario(admin);
                System.out.println(">>> ¡Usuario 'admin' con contraseña 'admin123' creado exitosamente!");
                
            } else {
                System.out.println(">>> La base de datos ya contiene usuarios. No se requiere la creación de datos iniciales.");
            }
        } catch (Exception e) {
             System.err.println("!!! ERROR CRÍTICO DURANTE LA CREACIÓN DEL USUARIO ADMIN POR DEFECTO !!!");
             e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cierra la conexión de la BD cuando la aplicación se detiene
        System.out.println("=======================================================");
        System.out.println("DESTRUCTOR: La aplicación web se está deteniendo. Cerrando conexión a la BD.");
        System.out.println("=======================================================");
        JPAUtil.shutdown();
    }
}
