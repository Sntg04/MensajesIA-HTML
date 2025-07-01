package com.ia.mensajes.agentemensajesia.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// NO SE USAN LAS LIBRERÍAS PESADAS DE STANFORD EN ESTA VERSIÓN
// import edu.stanford.nlp.pipeline.StanfordCoreNLP; 

public class SentimentAnalysisService {

    private static SentimentAnalysisService instance;
    // La variable 'pipeline' de Stanford se ha eliminado para ahorrar memoria

    // --- LÓGICA DE SIMULACIÓN ---
    private static final Set<String> PALABRAS_POSITIVAS = new HashSet<>(Arrays.asList("gracias", "ayuda", "solucion", "excelente", "bueno", "amable"));
    private static final Set<String> PALABRAS_NEGATIVAS = new HashSet<>(Arrays.asList("problema", "no", "nunca", "pesimo", "molesto", "queja", "odio"));

    private SentimentAnalysisService() {
        // El constructor está vacío. No hay modelos que cargar.
    }
    
    // El método init() ahora está vacío pero lo mantenemos por consistencia estructural.
    public void init() {
        System.out.println("SentimentAnalysisService iniciado en MODO DESARROLLO (ligero).");
    }

    public static synchronized SentimentAnalysisService getInstance() {
        if (instance == null) {
            instance = new SentimentAnalysisService();
        }
        return instance;
    }

    /**
     * SIMULACIÓN de análisis de sentimiento para desarrollo.
     * @param text El texto a analizar.
     * @return Una cadena simulando el sentimiento.
     */
    public String getSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Neutral";
        }

        String textoNormalizado = text.toLowerCase();

        for (String palabra : PALABRAS_NEGATIVAS) {
            if (textoNormalizado.contains(palabra)) {
                return "Negative"; // Simula un resultado negativo
            }
        }

        for (String palabra : PALABRAS_POSITIVAS) {
            if (textoNormalizado.contains(palabra)) {
                return "Positive"; // Simula un resultado positivo
            }
        }
        
        return "Neutral"; // Si no encuentra ninguna palabra clave
    }
}