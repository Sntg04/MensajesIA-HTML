package com.ia.mensajes.agentemensajesia.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "texto", length = 1000, nullable = false)
    private String texto;

    @Column(name = "clasificacion", length = 50)
    private String clasificacion;

    @Column(name = "confianza")
    private double confianza;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_procesamiento", nullable = false)
    private Date fechaProcesamiento;

    @Column(name = "lote", length = 100)
    private String lote;

    // --- GETTERS Y SETTERS ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }
    public double getConfianza() { return confianza; }
    public void setConfianza(double confianza) { this.confianza = confianza; }
    public Date getFechaProcesamiento() { return fechaProcesamiento; }
    public void setFechaProcesamiento(Date fechaProcesamiento) { this.fechaProcesamiento = fechaProcesamiento; }
    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }
}