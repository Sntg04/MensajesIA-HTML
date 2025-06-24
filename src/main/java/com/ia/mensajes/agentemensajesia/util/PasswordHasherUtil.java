package com.ia.mensajes.agentemensajesia.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasherUtil {
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String hashPassword(String passwordPlana) {
        if (passwordPlana == null || passwordPlana.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía.");
        }
        return passwordEncoder.encode(passwordPlana);
    }

    public static boolean verificarPassword(String passwordPlana, String hashAlmacenado) {
        if (passwordPlana == null || hashAlmacenado == null) {
            return false;
        }
        return passwordEncoder.matches(passwordPlana, hashAlmacenado);
    }
}