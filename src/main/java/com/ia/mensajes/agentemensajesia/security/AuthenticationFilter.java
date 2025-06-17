package com.ia.mensajes.agentemensajesia.security; // O el paquete donde lo hayas creado

import com.ia.mensajes.agentemensajesia.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.security.Principal;
import java.util.Date; // Import para new Date() en el log

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final String REALM = "example";
    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        // Log para ver la ruta exacta que intercepta el filtro
        System.out.println("AuthenticationFilter: Filtrando solicitud para: " + path + " (Timestamp: " + new Date().getTime() + ")");

        // Rutas públicas (no requieren token)
        // Ajustado para que /api/hello (texto) sea público, pero /api/hello/json no.
        if (path.equals("auth/login") || path.equals("hello")) {
            System.out.println("AuthenticationFilter: Ruta no protegida, permitiendo acceso: " + path);
            return; // No aplicar filtro a estas rutas exactas
        }

        // Para todas las demás rutas, se requiere autenticación
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext, "Se requiere cabecera de autorización Bearer.");
            return;
        }

        String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

        Jws<Claims> claimsJws = JwtUtil.validateTokenAndGetClaims(token);
        if (claimsJws == null) {
            abortWithUnauthorized(requestContext, "Token inválido o expirado.");
            return;
        }

        final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
        final String username = claimsJws.getBody().getSubject();
        final String role = claimsJws.getBody().get("role", String.class);

        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> username;
            }

            @Override
            public boolean isUserInRole(String requiredRole) {
                return role != null && role.equalsIgnoreCase(requiredRole);
            }

            @Override
            public boolean isSecure() {
                return currentSecurityContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return AUTHENTICATION_SCHEME;
            }
        });
        
        System.out.println("AuthenticationFilter: Token válido. Usuario: " + username + ", Rol: " + role + ". Acceso permitido a: " + path);
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.toLowerCase()
                .startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        System.err.println("AuthenticationFilter: Acceso no autorizado. Mensaje: " + message);
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
                        .entity("{\"error\":\"" + message + "\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build());
    }
}