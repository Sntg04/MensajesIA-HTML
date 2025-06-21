package com.ia.mensajes.agentemensajesia.model;

public class EstadisticaMensaje {
    
    private long totalMensajes;
    private double confianzaPromedioSpam;
    private double confianzaPromedioNoSpam;

    public EstadisticaMensaje() {}

    public EstadisticaMensaje(long totalMensajes, double confianzaPromedioSpam, double confianzaPromedioNoSpam) {
        this.totalMensajes = totalMensajes;
        this.confianzaPromedioSpam = confianzaPromedioSpam;
        this.confianzaPromedioNoSpam = confianzaPromedioNoSpam;
    }

    // Getters
    public long getTotalMensajes() {
        return totalMensajes;
    }

    public double getConfianzaPromedioSpam() {
        return confianzaPromedioSpam;
    }

    public double getConfianzaPromedioNoSpam() {
        return confianzaPromedioNoSpam;
    }

    // --- SETTERS AÑADIDOS QUE SOLUCIONAN EL ERROR ---
    public void setTotalMensajes(long totalMensajes) {
        this.totalMensajes = totalMensajes;
    }

    public void setConfianzaPromedioSpam(double confianzaPromedioSpam) {
        this.confianzaPromedioSpam = confianzaPromedioSpam;
    }

    public void setConfianzaPromedioNoSpam(double confianzaPromedioNoSpam) {
        this.confianzaPromedioNoSpam = confianzaPromedioNoSpam;
    }
}