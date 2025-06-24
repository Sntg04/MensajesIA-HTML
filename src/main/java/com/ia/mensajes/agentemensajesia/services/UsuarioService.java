package com.ia.mensajes.agentemensajesia.services;

import com.ia.mensajes.agentemensajesia.dao.UsuarioDAO;
import com.ia.mensajes.agentemensajesia.model.Usuario;
import com.ia.mensajes.agentemensajesia.util.PasswordHasherUtil;
import java.util.Date;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Crea un nuevo usuario. Llama al método correcto del DAO y encripta la contraseña.
     * @param nuevoUsuario El usuario a crear con la contraseña en texto plano.
     * @return El usuario persistido.
     */
    public Usuario crearUsuario(Usuario nuevoUsuario) {
        if (usuarioDAO.buscarPorUsername(nuevoUsuario.getUsername()) != null) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }
        
        // Hashea la contraseña usando tu clase de utilidad o la de Spring Security
        String passwordHasheada = passwordEncoder.encode(nuevoUsuario.getPasswordHash());
        nuevoUsuario.setPasswordHash(passwordHasheada);

        if (nuevoUsuario.getFechaCreacion() == null) {
            nuevoUsuario.setFechaCreacion(new Date());
        }
        nuevoUsuario.setActivo(true);
        
        // Llama al método crear() que existe en tu UsuarioDAO.
        return usuarioDAO.crear(nuevoUsuario);
    }

    /**
     * Obtiene todos los usuarios llamando al método listarTodos() del DAO.
     * @return Una lista de todos los usuarios.
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        // Llama al método listarTodos() que existe en tu UsuarioDAO.
        return usuarioDAO.listarTodos();
    }
    
    public Usuario obtenerUsuarioPorId(Integer id) {
        return usuarioDAO.buscarPorId(id);
    }
    
    /**
     * Actualiza un usuario existente.
     * Este método SÍ coincide con lo que el UsuarioResource espera.
     * @param id El ID del usuario a actualizar.
     * @param datosActualizados El objeto con los nuevos datos.
     * @return El usuario actualizado.
     */
    public Usuario actualizarUsuario(Integer id, Usuario datosActualizados) {
        Usuario usuarioExistente = usuarioDAO.buscarPorId(id);
        if (usuarioExistente == null) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }

        // Actualiza los campos que vienen en el objeto de datos
        if (datosActualizados.getNombreCompleto() != null) {
            usuarioExistente.setNombreCompleto(datosActualizados.getNombreCompleto());
        }
        if (datosActualizados.getRol() != null) {
            usuarioExistente.setRol(datosActualizados.getRol());
        }
        // Permite cambiar el estado activo explícitamente
        usuarioExistente.setActivo(datosActualizados.isActivo());
        
        // Si se proporciona una nueva contraseña, la encripta y la actualiza
        if (datosActualizados.getPasswordHash() != null && !datosActualizados.getPasswordHash().isEmpty()) {
            usuarioExistente.setPasswordHash(passwordEncoder.encode(datosActualizados.getPasswordHash()));
        }

        return usuarioDAO.actualizar(usuarioExistente);
    }
    
    /**
     * Elimina físicamente un usuario de la base de datos.
     * @param id El ID del usuario a eliminar.
     */
    public void eliminarUsuario(Integer id) {
        usuarioDAO.eliminar(id);
    }
    
    /**
     * Desactiva un usuario (pone el campo 'activo' en false).
     * Este es el método que faltaba y que el UsuarioResource necesita.
     * @param id El ID del usuario a desactivar.
     * @return true si se desactivó correctamente, false si no se encontró el usuario.
     */
    public boolean desactivarUsuario(Integer id) {
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario != null) {
            usuario.setActivo(false);
            usuarioDAO.actualizar(usuario);
            return true;
        }
        return false;
    }
}