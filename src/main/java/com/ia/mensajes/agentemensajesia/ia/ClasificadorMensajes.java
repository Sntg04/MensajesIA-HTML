package com.ia.mensajes.agentemensajesia.ia;

import com.ia.mensajes.agentemensajesia.services.SentimentAnalysisService;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class ClasificadorMensajes {

    private static ClasificadorMensajes instance;
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    private LemmatizerME lemmatizer;
    private SentimentAnalysisService sentimentService;

    // Banderas de estado para controlar la inicialización de forma segura
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

    private ClasificadorMensajes() {
      // Constructor vacío
    }

    public void init() {
        if (isReady || isInitializing) return;
        synchronized (this) {
            if (isReady || isInitializing) return;
            isInitializing = true;
            try {
                System.out.println("Cargando modelos de OpenNLP...");
                try (InputStream tokenModelIn = getClass().getResourceAsStream("/models/es/es-token.bin");
                     InputStream posModelIn = getClass().getResourceAsStream("/models/es/es-pos-maxent.bin");
                     InputStream lemmaModelIn = getClass().getResourceAsStream("/models/es/es-lemmatizer.bin")) {
                    this.tokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
                    this.posTagger = new POSTaggerME(new POSModel(posModelIn));
                    this.lemmatizer = new LemmatizerME(new LemmatizerModel(lemmaModelIn));
                }
                System.out.println("Modelos OpenNLP cargados.");
                this.sentimentService = SentimentAnalysisService.getInstance();
                this.sentimentService.init();
                isReady = true;
            } catch (Exception e) {
                System.err.println("Error fatal durante la inicialización de los servicios de IA.");
                throw new RuntimeException("Fallo al cargar los modelos de IA.", e);
            } finally {
                isInitializing = false;
            }
        }
    }

    // Método crucial para la sincronización
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
        texto = texto.toLowerCase();
        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        texto = texto.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        texto = texto.replaceAll("[^a-z0-9\\s]", " ");
        return texto;
    }

    public ResultadoClasificacion clasificar(String textoMensaje) {
        if (!isReady) {
             System.err.println("ERROR CRÍTICO: El clasificador fue llamado antes de que la inicialización estuviera completa.");
             // Devolvemos un error claro para que sea visible en el frontend.
             return new ResultadoClasificacion("Error de Sistema", "El motor de IA aún se está inicializando. Por favor, intente de nuevo en unos momentos.");
        }
        if (textoMensaje == null || textoMensaje.trim().isEmpty()) {
            return new ResultadoClasificacion("Bueno", "N/A");
        }
        try {
            String mensajeNormalizado = normalizar(textoMensaje);
            String[] tokens = tokenizer.tokenize(mensajeNormalizado);
            String[] tags = posTagger.tag(tokens);
            String[] lemas = lemmatizer.lemmatize(tokens, tags);

            int puntuacionTotal = 0;
            List<String> palabrasDetectadas = new ArrayList<>();
            for (String lema : lemas) {
                if (PUNTUACION_ALERTA.containsKey(lema)) {
                    puntuacionTotal += PUNTUACION_ALERTA.get(lema);
                    palabrasDetectadas.add(lema);
                }
            }

            String sentimiento = sentimentService.getSentiment(textoMensaje);
            return generarObservacionProfunda(puntuacionTotal, palabrasDetectadas, sentimiento);

        } catch (Exception e) {
            System.err.println("ERROR: Fallo el procesamiento de NLP para un mensaje: " + textoMensaje);
            e.printStackTrace();
            return new ResultadoClasificacion("Error de Análisis", "El motor de IA no pudo procesar este texto.");
        }
    }

    private ResultadoClasificacion generarObservacionProfunda(int puntuacion, List<String> palabrasClave, String sentimiento) {
        final int UMBRAL_PUNTOS = 5;
        boolean esAlertaPorPuntos = puntuacion >= UMBRAL_PUNTOS;
        boolean esAlertaPorTono = "Very negative".equalsIgnoreCase(sentimiento) || "Negative".equalsIgnoreCase(sentimiento);

        if (!esAlertaPorPuntos && !esAlertaPorTono) {
            return new ResultadoClasificacion("Bueno", "N/A");
        }

        StringBuilder sb = new StringBuilder();

        if (esAlertaPorPuntos && esAlertaPorTono) {
            sb.append("Diagnóstico: Alto Riesgo. Se detectaron palabras clave críticas y un tono emocional negativo.\n");
        } else if (esAlertaPorPuntos) {
            sb.append("Diagnóstico: Riesgo por Contenido. Se detectaron palabras clave específicas de cobranza.\n");
        } else {
            sb.append("Diagnóstico: Riesgo por Tono. El mensaje tiene una carga emocional negativa.\n");
        }

        sb.append("\n--- Detalles del Análisis ---\n");
        if (esAlertaPorTono) {
            sb.append("• Análisis de Tono: El sentimiento fue clasificado como '").append(sentimiento).append("'.\n");
        }
        if (esAlertaPorPuntos) {
            String palabras = palabrasClave.stream().distinct().collect(Collectors.joining(", "));
            sb.append("• Palabras de Riesgo: [").append(palabras).append("]. Puntuación total: ").append(puntuacion).append(" pts.\n");
        }

        sb.append("\n--- Sugerencia ---\n");
        sb.append(SUGERENCIA_REFORMULACION);

        return new ResultadoClasificacion("Alerta", sb.toString());
    }
}