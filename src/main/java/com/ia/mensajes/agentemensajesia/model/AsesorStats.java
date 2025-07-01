package com.ia.mensajes.agentemensajesia.model;

public class AsesorStats {
    private String nombreAsesor;
    private long totalMensajes;

    public AsesorStats(String nombreAsesor, long totalMensajes) {
        this.nombreAsesor = nombreAsesor;
        this.totalMensajes = totalMensajes;
    }

    // Getters y Setters necesarios para la serialización a JSON
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
}