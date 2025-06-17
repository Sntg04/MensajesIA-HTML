package com.ia.mensajes.agentemensajesia.util; // O el paquete que elijas

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasherUtil {

    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        String rawPasswordAdmin = "admin123";
        String hashedPasswordAdmin = passwordEncoder.encode(rawPasswordAdmin);
        System.out.println("Contraseña en texto plano (Admin): " + rawPasswordAdmin);
        System.out.println("Hash bcrypt (Admin): " + hashedPasswordAdmin);
        System.out.println("¿Coincide 'admin123' con el hash? " + passwordEncoder.matches(rawPasswordAdmin, hashedPasswordAdmin));

        System.out.println("\n------------------------------------\n");

        String rawPasswordCalidad = "calidad123";
        String hashedPasswordCalidad = passwordEncoder.encode(rawPasswordCalidad);
        System.out.println("Contraseña en texto plano (Calidad): " + rawPasswordCalidad);
        System.out.println("Hash bcrypt (Calidad): " + hashedPasswordCalidad);
        System.out.println("¿Coincide 'calidad123' con el hash? " + passwordEncoder.matches(rawPasswordCalidad, hashedPasswordCalidad));
    }
}