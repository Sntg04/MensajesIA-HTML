package com.ia.mensajes.agentemensajesia.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
// Ya no se importa el Parser, que no se necesita.
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class ContextAnalysisService {

    private static ContextAnalysisService instance;
    private Tokenizer tokenizer;

    // Palabras que indican un contexto seguro o de negación
    private static final Set<String> PALABRAS_NEGACION = new HashSet<>(Arrays.asList(
        "no", "sin", "evite", "evitar", "evitando", "libre", "excepto", "ningun", "ninguna"
    ));

    private ContextAnalysisService() {}

    public void init() {
        if (this.tokenizer == null) {
            try {
                System.out.println("Iniciando ContextAnalysisService (Versión Final Ligera)...");
                // Ahora solo carga el modelo de Tokenizer, que ya tienes y funciona.
                try (InputStream tokenModelIn = getClass().getResourceAsStream("/models/es/es-token.bin")) {
                    if (tokenModelIn == null) throw new IOException("No se encontró el modelo de tokenizer: es-token.bin");
                    this.tokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
                }
                System.out.println("ContextAnalysisService iniciado correctamente.");
            } catch (IOException e) {
                throw new RuntimeException("Fallo al cargar el modelo de tokenizer", e);
            }
        }
    }

    public static synchronized ContextAnalysisService getInstance() {
        if (instance == null) {
            instance = new ContextAnalysisService();
        }
        return instance;
    }

    /**
     * Analiza una oración para determinar si una palabra de alerta está en un contexto de riesgo.
     * @param sentence La oración completa a analizar.
     * @param alertWord La palabra de alerta encontrada.
     * @return `false` si la palabra está negada o en un contexto seguro, `true` en caso contrario.
     */
    public boolean esContextoDeRiesgo(String sentence, String alertWord) {
        String[] tokens = tokenizer.tokenize(sentence.toLowerCase());
        int alertWordIndex = -1;
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals(alertWord)) {
                alertWordIndex = i;
                break;
            }
        }
        if (alertWordIndex == -1) return true; // Si no la encuentra (raro), se asume riesgo

        // Revisa la "ventana" de 3 palabras antes de la palabra de alerta
        int windowSize = 3; 
        for (int i = Math.max(0, alertWordIndex - windowSize); i < alertWordIndex; i++) {
            if (PALABRAS_NEGACION.contains(tokens[i])) {
                System.out.println("Contexto seguro detectado para '" + alertWord + "' debido a la palabra '" + tokens[i] + "'");
                return false; // Se encontró una negación antes de la palabra de alerta
            }
        }
        return true; // No se encontraron negaciones, es contexto de riesgo.
    }
}