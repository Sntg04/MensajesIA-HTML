package com.ia.mensajes.agentemensajesia.resources;

import com.ia.mensajes.agentemensajesia.model.Usuario;
import com.ia.mensajes.agentemensajesia.services.UsuarioService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.stream.Collectors;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    private UsuarioService usuarioService;

    public UsuarioResource() {
        this.usuarioService = new UsuarioService();
    }

    // Método DTO para limpiar el hash de la contraseña antes de enviar la respuesta
    private Usuario prepararUsuarioParaRespuesta(Usuario usuario) {
        if (usuario == null) return null;
        Usuario dto = new Usuario();
        dto.setId(usuario.getId()); // Es crucial que usuario.getId() tenga el ID correcto aquí
        dto.setUsername(usuario.getUsername());
        dto.setRol(usuario.getRol());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setActivo(usuario.isActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        // NUNCA incluir el passwordHash en la respuesta
        return dto;
    }

    private List<Usuario> prepararListaUsuariosParaRespuesta(List<Usuario> usuarios) {
        if (usuarios == null) return null;
        return usuarios.stream()
                       .map(this::prepararUsuarioParaRespuesta)
                       .collect(Collectors.toList());
    }

    @POST
    @RolesAllowed("admin")
    public Response crearUsuario(Usuario nuevoUsuario, @Context SecurityContext securityContext) {
        // El objeto 'nuevoUsuario' que llega del JSON tiene la contraseña en texto plano en el campo 'passwordHash'
        System.out.println("Usuario " + securityContext.getUserPrincipal().getName() + " intentando crear usuario: " + nuevoUsuario.getUsername());
        try {
            Usuario usuarioCreado = usuarioService.crearUsuario(nuevoUsuario); 
            // 'usuarioCreado' es el objeto devuelto por el servicio, ya persistido (con ID) y con la contraseña hasheada (que no se usará en la respuesta)
            
            return Response.status(Response.Status.CREATED).entity(prepararUsuarioParaRespuesta(usuarioCreado)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error al crear usuario: " + e.getMessage() + "\"}").build();
        }
    }

    // Aquí van los otros métodos GET, PUT, DELETE para el CRUD de Usuarios
    // que te proporcioné anteriormente (obtenerTodosLosUsuarios, obtenerUsuarioPorId, actualizarUsuario, desactivarUsuario)
    // Asegúrate de que todos ellos usen prepararUsuarioParaRespuesta() o prepararListaUsuariosParaRespuesta()
    // cuando devuelvan objetos Usuario o listas de Usuario.

    @GET
    @RolesAllowed("admin")
    public Response obtenerTodosLosUsuarios() {
        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return Response.ok(prepararListaUsuariosParaRespuesta(usuarios)).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("admin")
    public Response obtenerUsuarioPorId(@PathParam("id") Integer id) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        if (usuario != null) {
            return Response.ok(prepararUsuarioParaRespuesta(usuario)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Usuario no encontrado\"}").build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("admin")
    public Response actualizarUsuario(@PathParam("id") Integer id, Usuario datosActualizados) {
        try {
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, datosActualizados);
            if (usuarioActualizado != null) {
                return Response.ok(prepararUsuarioParaRespuesta(usuarioActualizado)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Usuario no encontrado para actualizar\"}").build();
            }
        } catch (IllegalArgumentException e) {
             return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error al actualizar usuario: " + e.getMessage() + "\"}").build();
        }
    }
    
    @DELETE 
    @Path("/{id}/desactivar") 
    @RolesAllowed("admin")
    public Response desactivarUsuario(@PathParam("id") Integer id) {
        boolean desactivado = usuarioService.desactivarUsuario(id);
        if (desactivado) {
            return Response.ok("{\"mensaje\":\"Usuario desactivado correctamente\"}").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Usuario no encontrado para desactivar\"}").build();
        }
    }
}