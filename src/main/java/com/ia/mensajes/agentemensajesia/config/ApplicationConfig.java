package com.ia.mensajes.agentemensajesia.config;

import com.ia.mensajes.agentemensajesia.resources.AuthResource;
import com.ia.mensajes.agentemensajesia.resources.MensajeResource;
import com.ia.mensajes.agentemensajesia.resources.UsuarioResource;
import com.ia.mensajes.agentemensajesia.security.AuthenticationFilter;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

public class ApplicationConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(AuthResource.class);
        classes.add(UsuarioResource.class);
        classes.add(MensajeResource.class);
        classes.add(AuthenticationFilter.class);
        classes.add(MultiPartFeature.class);
        return classes;
    }
}