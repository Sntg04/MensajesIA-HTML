package com.ia.mensajes.agentemensajesia.config;

import com.ia.mensajes.agentemensajesia.resources.AuthResource;
import com.ia.mensajes.agentemensajesia.resources.MensajeResource;
import com.ia.mensajes.agentemensajesia.resources.UsuarioResource;
import com.ia.mensajes.agentemensajesia.security.AuthenticationFilter;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

// Esta clase ya NO necesita la anotación @ApplicationPath.
// web.xml se encarga de definir la ruta.
public class ApplicationConfig extends Application {

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