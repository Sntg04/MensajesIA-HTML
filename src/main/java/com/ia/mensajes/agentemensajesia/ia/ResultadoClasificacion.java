package com.ia.mensajes.agentemensajesia.ia;

public class ResultadoClasificacion {
    private String categoria;
    private double confianza;

    public ResultadoClasificacion(String categoria, double confianza) {
        this.categoria = categoria;
        this.confianza = confianza;
    }
    
    public String getCategoria() { return categoria; }
    public double getConfianza() { return confianza; }
}