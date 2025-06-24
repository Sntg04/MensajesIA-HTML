package com.ia.mensajes.agentemensajesia.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
public class Mensaje implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // --- Campos extraídos del Excel ---
    @Column(name = "aplicacion", length = 100)
    private String aplicacion;

    @Column(name = "id_cliente", length = 100)
    private String idCliente;

    @Column(name = "nombre_asesor", length = 150)
    private String nombreAsesor;

    @Column(name = "fecha_hora_mensaje")
    private LocalDateTime fechaHoraMensaje;
    
    @Column(name = "texto_mensaje", columnDefinition = "TEXT", nullable = false)
    private String texto;

    // --- Campos generados por la IA ---
    @Column(name = "clasificacion", length = 50)
    private String clasificacion;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion; // Para guardar por qué es crítico

    // --- Campos de control del sistema ---
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_procesamiento", nullable = false)
    private Date fechaProcesamiento;

    @Column(name = "lote", length = 100)
    private String lote;

    // --- Getters y Setters ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getAplicacion() { return aplicacion; }
    public void setAplicacion(String aplicacion) { this.aplicacion = aplicacion; }
    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }
    public String getNombreAsesor() { return nombreAsesor; }
    public void setNombreAsesor(String nombreAsesor) { this.nombreAsesor = nombreAsesor; }
    public LocalDateTime getFechaHoraMensaje() { return fechaHoraMensaje; }
    public void setFechaHoraMensaje(LocalDateTime fechaHoraMensaje) { this.fechaHoraMensaje = fechaHoraMensaje; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public Date getFechaProcesamiento() { return fechaProcesamiento; }
    public void setFechaProcesamiento(Date fechaProcesamiento) { this.fechaProcesamiento = fechaProcesamiento; }
    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }
}