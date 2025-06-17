package com.ia.mensajes.agentemensajesia.model;
import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import java.time.LocalTime;
@Entity
@Table(name = "mensajes")
public class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column(name = "aplicacion", length = 100) private String aplicacion;
    @Column(name = "nombre_asesor", nullable = false) private String nombreAsesor;
    @Column(name = "texto_original", columnDefinition = "TEXT", nullable = false) private String textoOriginal;
    @Column(name = "fecha_mensaje") private Date fechaMensaje;
    @Column(name = "hora_mensaje") private LocalTime horaMensaje;
    @Column(name = "clasificacion", length = 50) private String clasificacion;
    @Column(name = "texto_reescrito", columnDefinition = "TEXT") private String textoReescrito;
    @Column(name = "necesita_revision") private boolean necesitaRevision;
    @Column(name = "conteo_palabras") private Integer conteoPalabras;
    @Column(name = "conteo_caracteres") private Integer conteoCaracteres;
    @Column(name = "lote_carga", length = 100) private String loteCarga;
    @Column(name = "fecha_carga_db", nullable = false, updatable = false) @Temporal(TemporalType.TIMESTAMP) private java.util.Date fechaCargaDb;
    public Mensaje() { this.fechaCargaDb = new java.util.Date(); }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getAplicacion() { return aplicacion; }
    public void setAplicacion(String aplicacion) { this.aplicacion = aplicacion; }
    public String getNombreAsesor() { return nombreAsesor; }
    public void setNombreAsesor(String nombreAsesor) { this.nombreAsesor = nombreAsesor; }
    public String getTextoOriginal() { return textoOriginal; }
    public void setTextoOriginal(String textoOriginal) { this.textoOriginal = textoOriginal; }
    public Date getFechaMensaje() { return fechaMensaje; }
    public void setFechaMensaje(Date fechaMensaje) { this.fechaMensaje = fechaMensaje; }
    public LocalTime getHoraMensaje() { return horaMensaje; }
    public void setHoraMensaje(LocalTime horaMensaje) { this.horaMensaje = horaMensaje; }
    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }
    public String getTextoReescrito() { return textoReescrito; }
    public void setTextoReescrito(String textoReescrito) { this.textoReescrito = textoReescrito; }
    public boolean isNecesitaRevision() { return necesitaRevision; }
    public void setNecesitaRevision(boolean necesitaRevision) { this.necesitaRevision = necesitaRevision; }
    public Integer getConteoPalabras() { return conteoPalabras; }
    public void setConteoPalabras(Integer conteoPalabras) { this.conteoPalabras = conteoPalabras; }
    public Integer getConteoCaracteres() { return conteoCaracteres; }
    public void setConteoCaracteres(Integer conteoCaracteres) { this.conteoCaracteres = conteoCaracteres; }
    public String getLoteCarga() { return loteCarga; }
    public void setLoteCarga(String loteCarga) { this.loteCarga = loteCarga; }
    public java.util.Date getFechaCargaDb() { return fechaCargaDb; }
    public void setFechaCargaDb(java.util.Date fechaCargaDb) { this.fechaCargaDb = fechaCargaDb; }
}