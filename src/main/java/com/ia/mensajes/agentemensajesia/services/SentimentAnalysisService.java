package com.ia.mensajes.agentemensajesia.services;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class SentimentAnalysisService {

    private static SentimentAnalysisService instance;

    // --- Léxico de Sentimiento en Español (positivo > 0, negativo < 0) ---
    // Este diccionario es ligero y rápido, ideal para desarrollo.
    private static final Map<String, Integer> LEXICON = new HashMap<>();
    static {
        // Palabras Positivas y su "peso"
        LEXICON.put("acuerdo", 2);
        LEXICON.put("ayuda", 2);
        LEXICON.put("beneficio", 3);
        LEXICON.put("bien", 1);
        LEXICON.put("bueno", 2);
        LEXICON.put("calidad", 2);
        LEXICON.put("confianza", 2);
        LEXICON.put("correcto", 1);
        LEXICON.put("descuento", 3);
        LEXICON.put("disponible", 1);
        LEXICON.put("excelente", 4);
        LEXICON.put("exito", 4);
        LEXICON.put("facil", 1);
        LEXICON.put("felicidades", 4);
        LEXICON.put("gracias", 3);
        LEXICON.put("genial", 3);
        LEXICON.put("gusta", 2);
        LEXICON.put("oportunidad", 2);
        LEXICON.put("perfecto", 4);
        LEXICON.put("posibilidad", 1);
        LEXICON.put("premio", 3);
        LEXICON.put("resolver", 3);
        LEXICON.put("solucion", 3);

        // Palabras Negativas y su "peso"
        LEXICON.put("abuso", -3);
        LEXICON.put("amenaza", -4);
        LEXICON.put("contra", -2);
        LEXICON.put("cuidado", -2);
        LEXICON.put("culpa", -3);
        LEXICON.put("dificil", -1);
        LEXICON.put("error", -2);
        LEXICON.put("estafa", -5);
        LEXICON.put("evitar", -1);
        LEXICON.put("fallo", -3);
        LEXICON.put("fraude", -5);
        LEXICON.put("imposible", -2);
        LEXICON.put("incumplir", -3);
        LEXICON.put("jamás", -2);
        LEXICON.put("malo", -2);
        LEXICON.put("molesto", -2);
        LEXICON.put("nunca", -2);
        LEXICON.put("obligacion", -3);
        LEXICON.put("odio", -4);
        LEXICON.put("peligro", -3);
        LEXICON.put("pero", -1);
        LEXICON.put("pesimo", -4);
        LEXICON.put("problema", -2);
        LEXICON.put("queja", -2);
        LEXICON.put("riesgo", -2);
        LEXICON.put("terrible", -4);
    }

    private SentimentAnalysisService() {
        // El constructor está vacío. No hay modelos pesados que cargar.
    }
    
    // El método init() ahora solo imprime un mensaje informativo.
    public void init() {
        System.out.println("SentimentAnalysisService iniciado en MODO LIGERO (basado en léxico).");
    }

    public static synchronized SentimentAnalysisService getInstance() {
        if (instance == null) {
            instance = new SentimentAnalysisService();
        }
        return instance;
    }

    /**
     * Analiza el sentimiento de un texto sumando las puntuaciones de las palabras encontradas en el léxico.
     * @param text El texto a analizar.
     * @return Una cadena que representa el sentimiento: "Positive", "Negative" o "Neutral".
     */
    public String getSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Neutral";
        }

        String[] words = normalizar(text).split("\\s+");
        int score = 0;

        for (String word : words) {
            score += LEXICON.getOrDefault(word, 0);
        }

        if (score > 1) {
            return "Positive";
        }
        if (score < -1) {
            return "Negative";
        }
        return "Neutral";
    }
    
    // Método de ayuda para limpiar el texto, consistente con el clasificador principal.
    private String normalizar(String texto) {
        if (texto == null) return "";
        texto = texto.toLowerCase();
        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        texto = texto.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        texto = texto.replaceAll("[^a-zñ\\s]", " "); 
        return texto.trim().replaceAll("\\s+", " ");
    }
}