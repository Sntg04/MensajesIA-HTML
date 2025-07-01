package com.ia.mensajes.agentemensajesia.ia;

import com.ia.mensajes.agentemensajesia.services.SentimentAnalysisService; // Importar el nuevo servicio
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class ClasificadorMensajes {

    private static ClasificadorMensajes instance;
    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;
    private final LemmatizerME lemmatizer;
    private final SentimentAnalysisService sentimentService; // Instancia del nuevo servicio

    private static final String SUGERENCIA_REFORMULACION = "Sugerencia: Intente reformular la frase usando un tono más neutral y enfocado en soluciones, evitando palabras que puedan interpretarse como una amenaza o presión.";

    private static final Map<String, Integer> PUNTUACION_ALERTA = new HashMap<>();
    static {
        // (Tu sistema de puntuación se mantiene igual)
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
        try {
            System.out.println("Cargando modelos de OpenNLP...");
            // ... (Carga de modelos de OpenNLP se mantiene igual)
            try (InputStream tokenModelIn = getClass().getResourceAsStream("/models/es/es-token.bin");
                 InputStream posModelIn = getClass().getResourceAsStream("/models/es/es-pos-maxent.bin");
                 InputStream lemmaModelIn = getClass().getResourceAsStream("/models/es/es-lemmatizer.bin")) {

                if (tokenModelIn == null) throw new IOException("No se encontró el modelo de tokenizer.");
                this.tokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
                if (posModelIn == null) throw new IOException("No se encontró el modelo POS.");
                this.posTagger = new POSTaggerME(new POSModel(posModelIn));
                if (lemmaModelIn == null) throw new IOException("No se encontró el modelo de lematización.");
                this.lemmatizer = new LemmatizerME(new LemmatizerModel(lemmaModelIn));
            }
            System.out.println("Modelos OpenNLP cargados.");

            // Inicializar el nuevo servicio de sentimiento
            this.sentimentService = SentimentAnalysisService.getInstance();

        } catch (Exception e) {
            System.err.println("Error fatal durante la inicialización de los servicios de IA.");
            e.printStackTrace();
            throw new RuntimeException("Fallo al cargar los modelos de IA.", e);
        }
    }
    
    // getInstance() y normalizar() se mantienen iguales
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


    // --- MÉTODO CLASIFICAR ACTUALIZADO ---
    public ResultadoClasificacion clasificar(String textoMensaje) {
        if (textoMensaje == null || textoMensaje.trim().isEmpty()) {
            return new ResultadoClasificacion("Bueno", "N/A");
        }
        try {
            // --- 1. Clasificación por Puntuación de Riesgo (como antes) ---
            String mensajeNormalizado = normalizar(textoMensaje);
            String[] tokens = tokenizer.tokenize(mensajeNormalizado);
            String[] tags = posTagger.tag(tokens);
            String[] lemas = lemmatizer.lemmatize(tokens, tags);

            int puntuacionTotal = 0;
            String palabraClaveDetectada = "";
            int maxPuntuacionPalabra = 0;

            for (String lema : lemas) {
                if (PUNTUACION_ALERTA.containsKey(lema)) {
                    int puntuacionPalabra = PUNTUACION_ALERTA.get(lema);
                    puntuacionTotal += puntuacionPalabra;
                    if (puntuacionPalabra > maxPuntuacionPalabra) {
                        maxPuntuacionPalabra = puntuacionPalabra;
                        palabraClaveDetectada = lema;
                    }
                }
            }

            final int UMBRAL_ALERTA_PUNTOS = 5;
            if (puntuacionTotal >= UMBRAL_ALERTA_PUNTOS) {
                String observacion = "Riesgo por palabras clave (" + puntuacionTotal + "pts). Principal: '" + palabraClaveDetectada + "'. " + SUGERENCIA_REFORMULACION;
                return new ResultadoClasificacion("Alerta", observacion);
            }

            // --- 2. Análisis de Sentimiento (nueva capa de inteligencia) ---
            String sentimiento = sentimentService.getSentiment(textoMensaje);

            if ("Muy Negativo".equals(sentimiento) || "Negativo".equals(sentimiento)) {
                 String observacion = "Riesgo por tono emocional. Sentimiento detectado: " + sentimiento + ". " + SUGERENCIA_REFORMULACION;
                 return new ResultadoClasificacion("Alerta", observacion);
            }

            // Si pasa ambas pruebas, es un mensaje bueno.
            return new ResultadoClasificacion("Bueno", "N/A");

        } catch (Exception e) {
            System.err.println("ERROR: Fallo el procesamiento de NLP para un mensaje: " + textoMensaje);
            e.printStackTrace();
            return new ResultadoClasificacion("Error de Análisis", "El motor de IA no pudo procesar este texto.");
        }
    }
}