package com.ia.mensajes.agentemensajesia.model; // O com.ia.mensajes.agentemensajesia.dto si creaste ese paquete

// No necesita anotaciones JPA porque no es una entidad
public class LoginRequest {
    private String username;
    private String password;

    // Constructor vacío necesario para la deserialización JSON
    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}