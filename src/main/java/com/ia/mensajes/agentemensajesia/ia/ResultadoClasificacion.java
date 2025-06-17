package com.ia.mensajes.agentemensajesia.ia;
public class ResultadoClasificacion {
    private final String clasificacion;
    private final String palabraClaveEncontrada;
    public ResultadoClasificacion(String clasificacion, String palabraClaveEncontrada) {
        this.clasificacion = clasificacion;
        this.palabraClaveEncontrada = palabraClaveEncontrada;
    }
    public String getClasificacion() { return clasificacion; }
    public String getPalabraClaveEncontrada() { return palabraClaveEncontrada; }
}