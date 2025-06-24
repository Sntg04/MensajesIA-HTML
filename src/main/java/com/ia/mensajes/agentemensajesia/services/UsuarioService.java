package com.ia.mensajes.agentemensajesia.services;

import com.ia.mensajes.agentemensajesia.dao.UsuarioDAO;
import com.ia.mensajes.agentemensajesia.model.Usuario;
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

    public Usuario crearUsuario(Usuario nuevoUsuario) {
        if (usuarioDAO.buscarPorUsername(nuevoUsuario.getUsername()) != null) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }

        String passwordHasheada = passwordEncoder.encode(nuevoUsuario.getPasswordHash());
        nuevoUsuario.setPasswordHash(passwordHasheada);

        if (nuevoUsuario.getFechaCreacion() == null) {
            nuevoUsuario.setFechaCreacion(new Date());
        }
        nuevoUsuario.setActivo(true);

        return usuarioDAO.crear(nuevoUsuario);
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioDAO.listarTodos();
    }

    public Usuario obtenerUsuarioPorId(Integer id) {
        return usuarioDAO.buscarPorId(id);
    }

    public Usuario actualizarUsuario(Integer id, Usuario datosActualizados) {
        Usuario usuarioExistente = usuarioDAO.buscarPorId(id);
        if (usuarioExistente == null) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }

        if (datosActualizados.getNombreCompleto() != null) {
            usuarioExistente.setNombreCompleto(datosActualizados.getNombreCompleto());
        }
        if (datosActualizados.getRol() != null) {
            usuarioExistente.setRol(datosActualizados.getRol());
        }
        usuarioExistente.setActivo(datosActualizados.isActivo());

        if (datosActualizados.getPasswordHash() != null && !datosActualizados.getPasswordHash().isEmpty()) {
            usuarioExistente.setPasswordHash(passwordEncoder.encode(datosActualizados.getPasswordHash()));
        }

        return usuarioDAO.actualizar(usuarioExistente);
    }

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