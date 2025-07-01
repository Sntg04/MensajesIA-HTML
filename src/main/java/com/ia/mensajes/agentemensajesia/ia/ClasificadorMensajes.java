package com.ia.mensajes.agentemensajesia.ia;

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
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    private LemmatizerME lemmatizer;

    private static final String SUGERENCIA_REFORMULACION = "Sugerencia: Intente reformular la frase usando un tono más neutral y enfocado en soluciones, evitando palabras que puedan interpretarse como una amenaza o presión.";

    // --- NUEVO SISTEMA DE PUNTUACIÓN DE RIESGO ---
    private static final Map<String, Integer> PUNTUACION_ALERTA = new HashMap<>();
    static {
        // Riesgo Crítico (10 puntos) - Acciones legales inminentes
        PUNTUACION_ALERTA.put("embargo", 10);
        PUNTUACION_ALERTA.put("juridico", 10);
        PUNTUACION_ALERTA.put("desentendido", 10); // Implica ignorar advertencias previas
        PUNTUACION_ALERTA.put("drastica", 10);
        PUNTUACION_ALERTA.put("demanda", 10);

        // Riesgo Alto (5 puntos) - Consecuencias serias
        PUNTUACION_ALERTA.put("incumplimiento", 5);
        PUNTUACION_ALERTA.put("castigado", 5);
        PUNTUACION_ALERTA.put("penalizacion", 5);
        PUNTUACION_ALERTA.put("negativo", 5); // Ej. "Reporte negativo"
        PUNTUACION_ALERTA.put("abogado", 5);
        PUNTUACION_ALERTA.put("presion", 5);


        // Riesgo Medio (3 puntos) - Advertencias formales
        PUNTUACION_ALERTA.put("deuda", 3);
        PUNTUACION_ALERTA.put("buro", 3);
        PUNTUACION_ALERTA.put("reportar", 3);
        PUNTUACION_ALERTA.put("retencion", 3);
        PUNTUACION_ALERTA.put("sancion", 3);

        // Riesgo Bajo (1 punto) - Términos de cobranza estándar
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
            System.out.println("Modelos cargados exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al cargar los modelos de NLP. Asegúrate de que los archivos .bin estén en src/main/resources/models/es/");
            throw new RuntimeException("Fallo al cargar los modelos de NLP.", e);
        }
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
        if (textoMensaje == null || textoMensaje.trim().isEmpty()) {
            return new ResultadoClasificacion("Bueno", "N/A");
        }
        try {
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

            final int UMBRAL_ALERTA = 5; // Umbral para considerar un mensaje como alerta

            if (puntuacionTotal >= UMBRAL_ALERTA) {
                String observacion = "Riesgo: " + puntuacionTotal + "pts. Palabra clave principal: '" + palabraClaveDetectada + "'. " + SUGERENCIA_REFORMULACION;
                return new ResultadoClasificacion("Alerta", observacion);
            }

            return new ResultadoClasificacion("Bueno", "N/A");

        } catch (Exception e) {
            System.err.println("ERROR: Fallo el procesamiento de NLP para un mensaje: " + textoMensaje);
            e.printStackTrace();
            return new ResultadoClasificacion("Error de Análisis", "El motor de IA no pudo procesar este texto.");
        }
    }
}