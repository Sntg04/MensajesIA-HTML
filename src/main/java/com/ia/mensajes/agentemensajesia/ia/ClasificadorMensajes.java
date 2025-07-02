package com.ia.mensajes.agentemensajesia.ia;

import com.ia.mensajes.agentemensajesia.services.ContextAnalysisService;
import com.ia.mensajes.agentemensajesia.services.SpellCheckService;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class ClasificadorMensajes {

    private static ClasificadorMensajes instance;
    private TokenizerME tokenizer;
    private ContextAnalysisService contextService;
    private SpellCheckService spellCheckService;

    private volatile boolean isInitializing = false;
    private volatile boolean isReady = false;

    private static final String SUGERENCIA_REFORMULACION = "Intente comunicar la misma información con un enfoque en soluciones y sin urgencia, por ejemplo: 'Le contactamos para revisar su caso y ofrecerle opciones para regularizar su situación.'";
    private static final Map<String, Integer> PUNTUACION_ALERTA = new HashMap<>();
    static {
        PUNTUACION_ALERTA.put("embargo", 10);
        PUNTUACION_ALERTA.put("juridico", 10);
        PUNTUACION_ALERTA.put("desentendido", 10);
        PUNTUACION_ALERTA.put("drastica", 10);
        PUNTUACION_ALERTA.put("demanda", 10);
        PUNTUACION_ALERTA.put("incumplimiento", 5);
        PUNTUACION_ALERTA.put("castigado", 5);
        PUNTUACION_ALERTA.put("penalizacion", 5);
        PUNTUACION_ALERTA.put("negativo", 5);
        PUNTUACION_ALERTA.put("abogado", 5);
        PUNTUACION_ALERTA.put("presion", 5);
        PUNTUACION_ALERTA.put("deuda", 3);
        PUNTUACION_ALERTA.put("buro", 3);
        PUNTUACION_ALERTA.put("reportar", 3);
        PUNTUACION_ALERTA.put("retencion", 3);
        PUNTUACION_ALERTA.put("sancion", 3);
        PUNTUACION_ALERTA.put("cobro", 1);
        PUNTUACION_ALERTA.put("credito", 1);
        PUNTUACION_ALERTA.put("localizacion", 1);
        PUNTUACION_ALERTA.put("visita", 1);
        PUNTUACION_ALERTA.put("proceso", 1);
        PUNTUACION_ALERTA.put("responsabilidad", 1);
    }
    
    private ClasificadorMensajes() {}

    public void init() {
        if (isReady || isInitializing) return;
        synchronized (this) {
            if (isReady || isInitializing) return;
            isInitializing = true;
            try {
                System.out.println("Cargando modelos de OpenNLP...");
                try (InputStream tokenModelIn = getClass().getResourceAsStream("/models/es/es-token.bin")) { 
                    if (tokenModelIn == null) throw new IOException("No se encontró el modelo de tokenizer (es-token.bin).");
                    this.tokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
                }
                System.out.println("Modelo de Tokenizer cargado.");
                
                this.contextService = ContextAnalysisService.getInstance();
                this.contextService.init();
                
                this.spellCheckService = SpellCheckService.getInstance();
                this.spellCheckService.init();

                isReady = true;
            } catch (Exception e) {
                System.err.println("Error fatal durante la inicialización de los servicios de IA.");
                throw new RuntimeException("Fallo al cargar los modelos de IA.", e);
            } finally {
                isInitializing = false;
            }
        }
    }
    
    public void waitForReady() {
        if (isReady) return;
        System.out.println("Un proceso está esperando a que los modelos de IA terminen de cargar...");
        while(!isReady) {
            try {
                Thread.sleep(1000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Modelos de IA listos. El proceso continúa.");
    }

    public static synchronized ClasificadorMensajes getInstance() {
        if (instance == null) {
            instance = new ClasificadorMensajes();
        }
        return instance;
    }
    
    private String normalizar(String texto) {
        if (texto == null) return "";
        String textoNormalizado = texto.toLowerCase();
        textoNormalizado = Normalizer.normalize(textoNormalizado, Normalizer.Form.NFD);
        textoNormalizado = textoNormalizado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return textoNormalizado.trim();
    }

    public ResultadoClasificacion clasificar(String textoMensaje) {
        if (!isReady) {
             return new ResultadoClasificacion("Error de Sistema", "El motor de IA aún se está inicializando.");
        }
        if (textoMensaje == null || textoMensaje.trim().isEmpty()) {
            return new ResultadoClasificacion("Bueno", "N/A");
        }
        try {
            // Análisis de Riesgo
            int puntuacionTotal = 0;
            List<String> palabrasDetectadas = new ArrayList<>();
            String mensajeNormalizado = normalizar(textoMensaje);
            String[] tokens = tokenizer.tokenize(mensajeNormalizado);
            for (String token : tokens) {
                if (PUNTUACION_ALERTA.containsKey(token)) {
                    if (contextService.esContextoDeRiesgo(textoMensaje, token)) {
                        puntuacionTotal += PUNTUACION_ALERTA.get(token);
                        palabrasDetectadas.add(token);
                    }
                }
            }
            
            // Análisis Ortográfico
            List<String> erroresOrtograficos = spellCheckService.findMisspelledWords(textoMensaje);
            
            return generarObservacionProfunda(puntuacionTotal, palabrasDetectadas, erroresOrtograficos);

        } catch (Exception e) {
            System.err.println("ERROR: Fallo el procesamiento de NLP para el mensaje: '" + textoMensaje + "'");
            e.printStackTrace();
            return new ResultadoClasificacion("Error de Análisis", "El motor de IA no pudo procesar este texto.");
        }
    }

    private ResultadoClasificacion generarObservacionProfunda(int puntuacion, List<String> palabrasClave, List<String> erroresOrtograficos) {
        final int UMBRAL_PUNTOS = 5;
        boolean esAlertaPorPuntos = puntuacion >= UMBRAL_PUNTOS;
        boolean hayErroresOrtograficos = !erroresOrtograficos.isEmpty();

        if (!esAlertaPorPuntos && !hayErroresOrtograficos) {
            return new ResultadoClasificacion("Bueno", "N/A");
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (esAlertaPorPuntos) {
            sb.append("Diagnóstico: Uso Indebido de Palabras.\n");
            sb.append("\n--- Detalles del Análisis ---\n");
            String palabras = palabrasClave.stream().distinct().collect(Collectors.joining(", "));
            sb.append("• Palabras de Riesgo: [").append(palabras).append("]. Puntuación total: ").append(puntuacion).append(" pts.\n");
            sb.append("\n--- Sugerencia ---\n");
            sb.append(SUGERENCIA_REFORMULACION).append("\n");
        }
        
        if (hayErroresOrtograficos) {
            if (sb.length() > 0) {
                sb.append("\n----------------------------------\n\n");
            }
            sb.append("Diagnóstico: Error de Ortografía.\n");
            sb.append("\n--- Detalles del Análisis ---\n");
            String errores = erroresOrtograficos.stream().distinct().collect(Collectors.joining(", "));
            sb.append("• Palabras con posible error: [").append(errores).append("].\n");
            sb.append("\n--- Sugerencia ---\n");
            sb.append("Revisar la escritura de las palabras señaladas para asegurar la calidad y claridad del mensaje.");
        }
        
        return new ResultadoClasificacion("Alerta", sb.toString());
    }
}