package com.ia.mensajes.agentemensajesia.resources;

import com.ia.mensajes.agentemensajesia.model.Usuario;
import com.ia.mensajes.agentemensajesia.services.UsuarioService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    private final UsuarioService usuarioService;

    public UsuarioResource() {
        this.usuarioService = new UsuarioService();
    }

    private Usuario prepararUsuarioParaRespuesta(Usuario usuario) {
        if (usuario == null) return null;
        Usuario dto = new Usuario();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setRol(usuario.getRol());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setActivo(usuario.isActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());
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
    public Response crearUsuario(Usuario nuevoUsuario) {
        try {
            Usuario usuarioCreado = usuarioService.crearUsuario(nuevoUsuario);
            return Response.status(Response.Status.CREATED).entity(prepararUsuarioParaRespuesta(usuarioCreado)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error al crear usuario: " + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @RolesAllowed("admin")
    public Response obtenerTodosLosUsuarios() {
        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return Response.ok(prepararListaUsuariosParaRespuesta(usuarios)).build();
    }

    // --- MÉTODO AÑADIDO PARA SOLUCIONAR EL ERROR ---
    @GET
    @Path("/{id}")
    @RolesAllowed("admin")
    public Response obtenerUsuarioPorId(@PathParam("id") Integer id) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\":\"Usuario no encontrado con ID: " + id + "\"}")
                           .build();
        }
        return Response.ok(prepararUsuarioParaRespuesta(usuario)).build();
    }
    // --- FIN DEL MÉTODO AÑADIDO ---

    @PUT
    @Path("/{id}")
    @RolesAllowed("admin")
    public Response actualizarUsuario(@PathParam("id") Integer id, Usuario datosActualizados) {
        try {
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, datosActualizados);
            return Response.ok(prepararUsuarioParaRespuesta(usuarioActualizado)).build();
        } catch (IllegalArgumentException e) {
             return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
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