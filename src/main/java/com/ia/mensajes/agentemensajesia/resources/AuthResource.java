package com.ia.mensajes.agentemensajesia.resources;

import com.ia.mensajes.agentemensajesia.model.LoginRequest;
import com.ia.mensajes.agentemensajesia.model.Usuario;
import com.ia.mensajes.agentemensajesia.services.AuthService;
import com.ia.mensajes.agentemensajesia.util.JwtUtil;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/auth")
public class AuthResource {

    private final AuthService authService = new AuthService();

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest loginRequest) {
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            // Devuelve un error 400 Bad Request con un cuerpo JSON
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(Map.of("error", "Usuario y contraseña son requeridos"))
                           .build();
        }

        try {
            Usuario usuarioAutenticado = authService.login(loginRequest.getUsername(), loginRequest.getPassword());

            if (usuarioAutenticado != null) {
                // Login exitoso: genera el token y devuelve los datos del usuario.
                String token = JwtUtil.generateToken(usuarioAutenticado);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("token", token);
                responseData.put("username", usuarioAutenticado.getUsername());
                responseData.put("role", usuarioAutenticado.getRol());
                return Response.ok(responseData).build();
            } else {
                // Login fallido: Devuelve un error 401 Unauthorized con un cuerpo JSON.
                // Esta es la corrección clave para tu problema.
                return Response.status(Response.Status.UNAUTHORIZED)
                               .entity(Map.of("error", "Credenciales inválidas o usuario inactivo"))
                               .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Error general del servidor: Devuelve un error 500 con un cuerpo JSON.
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(Map.of("error", "Error interno del servidor"))
                           .build();
        }
    }
}