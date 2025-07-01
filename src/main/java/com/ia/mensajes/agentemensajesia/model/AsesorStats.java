package com.ia.mensajes.agentemensajesia.model;

public class AsesorStats {
    private String nombreAsesor;
    private long totalMensajes;
    private long mensajesBuenos;
    private long mensajesAlertas;

    public AsesorStats(String nombreAsesor, long totalMensajes, long mensajesBuenos, long mensajesAlertas) {
        this.nombreAsesor = nombreAsesor;
        this.totalMensajes = totalMensajes;
        this.mensajesBuenos = mensajesBuenos;
        this.mensajesAlertas = mensajesAlertas;
    }

    // Getters y Setters
    public String getNombreAsesor() {
        return nombreAsesor;
    }

    public void setNombreAsesor(String nombreAsesor) {
        this.nombreAsesor = nombreAsesor;
    }

    public long getTotalMensajes() {
        return totalMensajes;
    }

    public void setTotalMensajes(long totalMensajes) {
        this.totalMensajes = totalMensajes;
    }

    public long getMensajesBuenos() {
        return mensajesBuenos;
    }

    public void setMensajesBuenos(long mensajesBuenos) {
        this.mensajesBuenos = mensajesBuenos;
    }

    public long getMensajesAlertas() {
        return mensajesAlertas;
    }

    public void setMensajesAlertas(long mensajesAlertas) {
        this.mensajesAlertas = mensajesAlertas;
    }
}