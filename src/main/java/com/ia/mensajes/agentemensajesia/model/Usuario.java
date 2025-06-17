package com.ia.mensajes.agentemensajesia.model;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column(name = "username", nullable = false, unique = true, length = 50) private String username;
    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
    @Column(name = "rol", nullable = false, length = 20) private String rol;
    @Column(name = "nombre_completo", length = 100) private String nombreCompleto;
    @Column(name = "activo", nullable = false) private boolean activo;
    @Column(name = "fecha_creacion", nullable = false, updatable = false) @Temporal(TemporalType.TIMESTAMP) private Date fechaCreacion;
    public Usuario() { this.activo = true; this.fechaCreacion = new Date(); }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}