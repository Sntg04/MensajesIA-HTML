package com.ia.mensajes.agentemensajesia.services;

import com.ia.mensajes.agentemensajesia.dao.UsuarioDAO;
import com.ia.mensajes.agentemensajesia.model.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Importamos el que ya usas

import java.util.Date;
import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Crea un nuevo usuario. Hashea la contraseña y llama al método crear() del DAO.
     */
    public Usuario crearUsuario(Usuario nuevoUsuario) throws IllegalArgumentException {
        if (usuarioDAO.buscarPorUsername(nuevoUsuario.getUsername()) != null) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }
        
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
     * Obtiene todos los usuarios. Llama al método listarTodos() del DAO.
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        // Llama al método listarTodos() que existe en tu UsuarioDAO.
        return usuarioDAO.listarTodos();
    }

    public Usuario obtenerUsuarioPorId(Integer id) {
        return usuarioDAO.buscarPorId(id);
    }
    
    // --- MÉTODO CORREGIDO PARA QUE COINCIDA CON EL RESOURCE ---
    /**
     * Actualiza un usuario existente. Acepta un ID y los datos a actualizar.
     */
    public Usuario actualizarUsuario(Integer id, Usuario datosActualizados) {
        Usuario usuarioExistente = usuarioDAO.buscarPorId(id);
        if (usuarioExistente == null) {
            return null; // O lanzar una excepción
        }

        // Actualiza los campos que vienen en el objeto
        if (datosActualizados.getNombreCompleto() != null) {
            usuarioExistente.setNombreCompleto(datosActualizados.getNombreCompleto());
        }
        if (datosActualizados.getRol() != null) {
            usuarioExistente.setRol(datosActualizados.getRol());
        }
        usuarioExistente.setActivo(datosActualizados.isActivo());
        
        // Si se proporciona una nueva contraseña, la encripta y la actualiza
        if (datosActualizados.getPasswordHash() != null && !datosActualizados.getPasswordHash().isEmpty()) {
            usuarioExistente.setPasswordHash(passwordEncoder.encode(datosActualizados.getPasswordHash()));
        }

        return usuarioDAO.actualizar(usuarioExistente);
    }

    // --- MÉTODO AÑADIDO QUE FALTABA ---
    /**
     * Desactiva un usuario (pone el campo 'activo' en false).
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

    /**
     * Elimina físicamente un usuario.
     */
    public void eliminarUsuario(Integer id) {
        usuarioDAO.eliminar(id);
    }
}
