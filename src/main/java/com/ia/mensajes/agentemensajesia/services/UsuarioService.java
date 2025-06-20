package com.ia.mensajes.agentemensajesia.services;

import com.ia.mensajes.agentemensajesia.dao.UsuarioDAO;
import com.ia.mensajes.agentemensajesia.model.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;
import java.util.Date;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Crea un usuario, encripta la contraseña y llama al método crear() del DAO.
     * @param nuevoUsuario El usuario a crear con la contraseña en texto plano.
     * @return El usuario persistido.
     */
    public Usuario crearUsuario(Usuario nuevoUsuario) {
        // Encriptar la contraseña
        String passwordHasheada = passwordEncoder.encode(nuevoUsuario.getPasswordHash());
        nuevoUsuario.setPasswordHash(passwordHasheada);

        // Establecer fecha y estado si no vienen
        if (nuevoUsuario.getFechaCreacion() == null) {
            nuevoUsuario.setFechaCreacion(new Date());
        }
        nuevoUsuario.setActivo(true);

        // --- ¡CORRECCIÓN CLAVE! ---
        // Llamar al método crear(usuario) que SÍ EXISTE en tu UsuarioDAO.
        return usuarioDAO.crear(nuevoUsuario);
    }

    /**
     * Obtiene todos los usuarios llamando al método listarTodos() del DAO.
     * @return Una lista de todos los usuarios.
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        // --- ¡CORRECCIÓN CLAVE! ---
        // Llamar al método listarTodos() que SÍ EXISTE en tu UsuarioDAO.
        return usuarioDAO.listarTodos();
    }
    
    // El resto de los métodos que ya tenías y que sí son compatibles.
    public Usuario obtenerUsuarioPorId(Integer id) {
        return usuarioDAO.buscarPorId(id);
    }

    public Usuario actualizarUsuario(Usuario usuario) {
        // Si se actualiza la contraseña, debe ser encriptada antes de llamar aquí.
        // Asumimos que la lógica del Resource maneja eso.
        return usuarioDAO.actualizar(usuario);
    }

    public void eliminarUsuario(Integer id) {
        usuarioDAO.eliminar(id);
    }
}