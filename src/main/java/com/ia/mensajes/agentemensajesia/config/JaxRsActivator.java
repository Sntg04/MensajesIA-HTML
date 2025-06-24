package com.ia.mensajes.agentemensajesia.config;

import com.ia.mensajes.agentemensajesia.resources.AuthResource;
import com.ia.mensajes.agentemensajesia.resources.MensajeResource;
import com.ia.mensajes.agentemensajesia.resources.UsuarioResource;
import com.ia.mensajes.agentemensajesia.security.AuthenticationFilter;
import java.util.HashSet;
import java.util.Set;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * Este es el ÚNICO punto de entrada para la configuración de JAX-RS (la API).
 * Define la ruta base como "/api" para toda la aplicación.
 */
@ApplicationPath("/api")
public class JaxRsActivator extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        
        // Registrar todos los endpoints (Resources) de la API
        classes.add(AuthResource.class);
        classes.add(UsuarioResource.class);
        classes.add(MensajeResource.class);
        
        // Registrar el filtro de autenticación
        classes.add(AuthenticationFilter.class);
        
        // Registrar la funcionalidad para subir archivos (multipart)
        classes.add(MultiPartFeature.class);
        
        return classes;
    }
}