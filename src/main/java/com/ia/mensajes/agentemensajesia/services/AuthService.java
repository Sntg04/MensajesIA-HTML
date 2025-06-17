package com.ia.mensajes.agentemensajesia.services; // Asegúrate que este sea tu paquete correcto

import com.ia.mensajes.agentemensajesia.dao.UsuarioDAO;
import com.ia.mensajes.agentemensajesia.model.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // ¡NUEVA IMPORTACIÓN!

public class AuthService {

    private UsuarioDAO usuarioDAO;
    private BCryptPasswordEncoder passwordEncoder; // ¡NUEVO CAMPO!

    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
        this.passwordEncoder = new BCryptPasswordEncoder(); // ¡INICIALIZAR EL ENCODER!
    }

    /**
     * Valida las credenciales de un usuario utilizando bcrypt.
     * @param username El nombre de usuario.
     * @param password La contraseña proporcionada en texto plano.
     * @return El objeto Usuario si las credenciales son válidas y el usuario está activo, 
     * null en caso contrario.
     */
    public Usuario login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return null; 
        }

        Usuario usuario = usuarioDAO.buscarPorUsername(username.trim());

        if (usuario != null && usuario.isActivo()) {
            // ¡CAMBIO IMPORTANTE AQUÍ!
            // Ahora usamos passwordEncoder.matches() para comparar la contraseña en texto plano
            // ingresada por el usuario con el hash almacenado en la base de datos.
            if (passwordEncoder.matches(password, usuario.getPasswordHash())) { 
                return usuario; // Login exitoso
            }
        }
        return null; // Usuario no encontrado, no activo, o contraseña incorrecta
    }

    /**
     * (Para futura implementación de registro de usuarios)
     * Este método tomaría una contraseña en texto plano, la hashearía y la guardaría.
     * @param rawPassword Contraseña en texto plano.
     * @return El hash bcrypt de la contraseña.
     */
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}