package com.ia.mensajes.agentemensajesia.ia;

public class ResultadoClasificacion {
    private final String categoria;
    private final String observacion;

    public ResultadoClasificacion(String categoria, String observacion) {
        this.categoria = categoria;
        this.observacion = observacion;
    }
    
    public String getCategoria() { return categoria; }
    public String getObservacion() { return observacion; }
}