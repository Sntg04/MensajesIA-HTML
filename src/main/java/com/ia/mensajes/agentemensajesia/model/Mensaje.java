package com.ia.mensajes.agentemensajesia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aplicacion;
    private String idCliente;
    @Column(columnDefinition = "TEXT")
    private String texto;
    private String nombreAsesor;
    private LocalDateTime fechaHoraMensaje;
    private String clasificacion;
    @Column(columnDefinition = "TEXT")
    private String observacion;
    private String lote;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaProcesamiento;

    // --- NUEVOS CAMPOS PARA FEEDBACK DE CALIDAD ---
    @Column(name = "feedback_estado")
    private String feedbackEstado; // Almacenará "CORRECTA" o "INCORRECTA"

    @Column(name = "feedback_motivo", length = 500)
    private String feedbackMotivo; // Almacenará la razón del analista

    @Column(name = "feedback_usuario")
    private String feedbackUsuario; // Almacenará el username del analista que dio el feedback

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "feedback_fecha")
    private Date feedbackFecha; // Almacenará la fecha y hora del feedback

    // Getters y Setters para todos los campos

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAplicacion() {
        return aplicacion;
    }

    public void setAplicacion(String aplicacion) {
        this.aplicacion = aplicacion;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getNombreAsesor() {
        return nombreAsesor;
    }

    public void setNombreAsesor(String nombreAsesor) {
        this.nombreAsesor = nombreAsesor;
    }

    public LocalDateTime getFechaHoraMensaje() {
        return fechaHoraMensaje;
    }

    public void setFechaHoraMensaje(LocalDateTime fechaHoraMensaje) {
        this.fechaHoraMensaje = fechaHoraMensaje;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
    
    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public Date getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(Date fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }

    // --- GETTERS Y SETTERS PARA LOS NUEVOS CAMPOS ---

    public String getFeedbackEstado() {
        return feedbackEstado;
    }

    public void setFeedbackEstado(String feedbackEstado) {
        this.feedbackEstado = feedbackEstado;
    }

    public String getFeedbackMotivo() {
        return feedbackMotivo;
    }

    public void setFeedbackMotivo(String feedbackMotivo) {
        this.feedbackMotivo = feedbackMotivo;
    }

    public String getFeedbackUsuario() {
        return feedbackUsuario;
    }

    public void setFeedbackUsuario(String feedbackUsuario) {
        this.feedbackUsuario = feedbackUsuario;
    }

    public Date getFeedbackFecha() {
        return feedbackFecha;
    }

    public void setFeedbackFecha(Date feedbackFecha) {
        this.feedbackFecha = feedbackFecha;
    }
}