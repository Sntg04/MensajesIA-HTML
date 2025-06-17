package com.ia.mensajes.agentemensajesia.model;
public class EstadisticaMensaje {
    private String nombreAsesor;
    private String textoOriginal;
    private long cantidad;
    public EstadisticaMensaje(String nombreAsesor, String textoOriginal, long cantidad) {
        this.nombreAsesor = nombreAsesor;
        this.textoOriginal = textoOriginal;
        this.cantidad = cantidad;
    }
    public String getNombreAsesor() { return nombreAsesor; }
    public String getTextoOriginal() { return textoOriginal; }
    public long getCantidad() { return cantidad; }
}