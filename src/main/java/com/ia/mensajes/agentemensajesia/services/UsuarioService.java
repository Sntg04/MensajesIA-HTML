package com.ia.mensajes.agentemensajesia.services;

import com.ia.mensajes.agentemensajesia.dao.UsuarioDAO;
import com.ia.mensajes.agentemensajesia.model.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.List;
// import java.util.regex.Pattern; // Si necesitas validaciones regex más adelante

public class UsuarioService {

    private UsuarioDAO usuarioDAO;
    private BCryptPasswordEncoder passwordEncoder;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Crea un nuevo usuario. Hashea la contraseña antes de guardarla.
     * @param nuevoUsuario El objeto Usuario con los datos del nuevo usuario (contraseña en texto plano en getPasswordHash()).
     * @return El Usuario creado y persistido (con ID y contraseña hasheada).
     * @throws IllegalArgumentException Si el username ya existe o los datos son inválidos.
     */
    public Usuario crearUsuario(Usuario nuevoUsuario) throws IllegalArgumentException {
        if (nuevoUsuario == null || nuevoUsuario.getUsername() == null || nuevoUsuario.getPasswordHash() == null || nuevoUsuario.getRol() == null) {
            throw new IllegalArgumentException("Username, contraseña y rol son requeridos.");
        }
        if (nuevoUsuario.getUsername().trim().isEmpty() || nuevoUsuario.getPasswordHash().isEmpty() || nuevoUsuario.getRol().trim().isEmpty()) {
            throw new IllegalArgumentException("Username, contraseña y rol no pueden estar vacíos.");
        }

        // Validar que el username no exista ya
        if (usuarioDAO.buscarPorUsername(nuevoUsuario.getUsername().trim()) != null) {
            throw new IllegalArgumentException("El nombre de usuario '" + nuevoUsuario.getUsername() + "' ya existe.");
        }
        
        // Validar rol (ejemplo básico)
        if (!nuevoUsuario.getRol().equals("admin") && !nuevoUsuario.getRol().equals("calidad")) {
            throw new IllegalArgumentException("El rol debe ser 'admin' o 'calidad'.");
        }

        // Hashear la contraseña (que viene en texto plano en nuevoUsuario.getPasswordHash())
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(nuevoUsuario.getPasswordHash()));
        
        if (nuevoUsuario.getFechaCreacion() == null) {
            nuevoUsuario.setFechaCreacion(new Date());
        }
        nuevoUsuario.setActivo(true); 

        // Llamar al DAO y obtener el usuario persistido (con ID)
        return usuarioDAO.crear(nuevoUsuario);
    }

    /**
     * Obtiene un usuario por su ID.
     * @param id El ID del usuario.
     * @return El Usuario encontrado o null si no existe.
     */
    public Usuario obtenerUsuarioPorId(Integer id) {
        return usuarioDAO.buscarPorId(id);
    }

    /**
     * Obtiene todos los usuarios.
     * @return Lista de todos los usuarios.
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioDAO.listarTodos();
    }

    /**
     * Actualiza un usuario existente.
     * Si se provee una nueva contraseña (en getPasswordHash(), no nula y no vacía), se hashea y actualiza.
     * @param id El ID del usuario a actualizar.
     * @param datosActualizados Objeto Usuario con los campos a actualizar.
     * @return El Usuario actualizado o null si el usuario no se encuentra.
     * @throws IllegalArgumentException Si se intenta cambiar el username a uno que ya existe o rol inválido.
     */
    public Usuario actualizarUsuario(Integer id, Usuario datosActualizados) throws IllegalArgumentException {
        Usuario usuarioExistente = usuarioDAO.buscarPorId(id);
        if (usuarioExistente == null) {
            return null; 
        }

        if (datosActualizados.getNombreCompleto() != null) {
            usuarioExistente.setNombreCompleto(datosActualizados.getNombreCompleto());
        }

        if (datosActualizados.getRol() != null && !datosActualizados.getRol().trim().isEmpty()) {
             if (!datosActualizados.getRol().equals("admin") && !datosActualizados.getRol().equals("calidad")) {
                throw new IllegalArgumentException("El rol debe ser 'admin' o 'calidad'.");
            }
            usuarioExistente.setRol(datosActualizados.getRol());
        }
        
        if (datosActualizados.getPasswordHash() != null && !datosActualizados.getPasswordHash().isEmpty()) {
            usuarioExistente.setPasswordHash(passwordEncoder.encode(datosActualizados.getPasswordHash()));
        }
        
        // Para el campo 'activo', el objeto 'datosActualizados' debería traer el valor deseado.
        // Si el campo 'activo' en 'datosActualizados' no es explícitamente seteado por el cliente,
        // podría mantener el valor que tenga por defecto el objeto (que podría ser 'false' para boolean).
        // Es mejor ser explícito o tener un DTO que maneje esto claramente.
        // Aquí asumimos que 'datosActualizados.isActivo()' tiene el valor deseado.
        usuarioExistente.setActivo(datosActualizados.isActivo());


        return usuarioDAO.actualizar(usuarioExistente);
    }

    /**
     * Desactiva un usuario (cambia su estado 'activo' a false).
     * @param id El ID del usuario a desactivar.
     * @return true si se desactivó, false si el usuario no se encontró.
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
     * Elimina un usuario físicamente de la BD. Usar con precaución.
     * @param id El ID del usuario a eliminar.
     */
    public void eliminarUsuarioFisicamente(Integer id) {
        usuarioDAO.eliminar(id);
    }
}