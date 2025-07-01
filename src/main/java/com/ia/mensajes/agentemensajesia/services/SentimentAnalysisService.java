package com.ia.mensajes.agentemensajesia.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SentimentAnalysisService {

    private static SentimentAnalysisService instance;

    // --- LÓGICA DE SIMULACIÓN LIGERA ---
    private static final Set<String> PALABRAS_POSITIVAS = new HashSet<>(Arrays.asList("gracias", "ayuda", "solucion", "excelente", "bueno", "amable", "resolver"));
    private static final Set<String> PALABRAS_NEGATIVAS = new HashSet<>(Arrays.asList("problema", "no", "nunca", "pesimo", "molesto", "queja", "odio", "incompetente"));

    private SentimentAnalysisService() {
        // El constructor está vacío. No se cargan modelos pesados.
    }
    
    // El método init() ahora está vacío pero lo mantenemos por consistencia estructural.
    public void init() {
        System.out.println("SentimentAnalysisService iniciado en MODO DESARROLLO (ligero). No se cargan modelos pesados.");
    }

    public static synchronized SentimentAnalysisService getInstance() {
        if (instance == null) {
            instance = new SentimentAnalysisService();
        }
        return instance;
    }

    /**
     * SIMULACIÓN de análisis de sentimiento para desarrollo. No usa modelos reales.
     * @param text El texto a analizar.
     * @return Una cadena simulando el sentimiento ("Positive", "Negative", "Neutral").
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
        
        return "Neutral"; // Si no encuentra ninguna palabra clave, es neutral.
    }
}