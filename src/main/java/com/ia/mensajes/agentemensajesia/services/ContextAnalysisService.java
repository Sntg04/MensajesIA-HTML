package com.ia.mensajes.agentemensajesia.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class ContextAnalysisService {

    private static ContextAnalysisService instance;
    private Parser parser;
    private Tokenizer tokenizer;

    private static final Set<String> PALABRAS_NEGACION = new HashSet<>(Arrays.asList(
        "no", "sin", "evite", "evitar", "evitando", "libre", "excepto", "ningun", "ninguna"
    ));

    private ContextAnalysisService() {}

    public void init() {
        if (this.parser == null) {
            try {
                System.out.println("Iniciando ContextAnalysisService (desde paquete)...");
                try (InputStream modelIn = getClass().getResourceAsStream("/opennlp/models/es/es-parser-chunking.bin");
                     InputStream tokenModelIn = getClass().getResourceAsStream("/opennlp/models/es/es-token.bin")) {
                    if (modelIn == null) throw new IOException("No se encontró el modelo de parser desde el paquete: es-parser-chunking.bin");
                    if (tokenModelIn == null) throw new IOException("No se encontró el modelo de tokenizer desde el paquete: es-token.bin");
                    
                    ParserModel model = new ParserModel(modelIn);
                    this.parser = ParserFactory.create(model);
                    this.tokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
                }
                System.out.println("ContextAnalysisService iniciado correctamente.");
            } catch (IOException e) {
                throw new RuntimeException("Fallo al cargar el modelo de parser desde el paquete", e);
            }
        }
    }

    public static synchronized ContextAnalysisService getInstance() {
        if (instance == null) {
            instance = new ContextAnalysisService();
        }
        return instance;
    }

    public boolean esContextoDeRiesgo(String sentence, String alertWord) {
        String[] tokens = tokenizer.tokenize(sentence.toLowerCase());
        int alertWordIndex = -1;
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals(alertWord)) {
                alertWordIndex = i;
                break;
            }
        }
        if (alertWordIndex == -1) return true; 

        int windowSize = 3; 
        for (int i = Math.max(0, alertWordIndex - windowSize); i < alertWordIndex; i++) {
            if (PALABRAS_NEGACION.contains(tokens[i])) {
                System.out.println("Contexto seguro detectado para '" + alertWord + "' debido a la palabra '" + tokens[i] + "'");
                return false;
            }
        }
        return true;
    }
}