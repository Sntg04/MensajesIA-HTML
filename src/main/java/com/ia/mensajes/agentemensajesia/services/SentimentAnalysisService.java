package com.ia.mensajes.agentemensajesia.services;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.util.Properties;

public class SentimentAnalysisService {

    private static SentimentAnalysisService instance;
    private StanfordCoreNLP pipeline; // No es 'final' para permitir la inicialización controlada

    private SentimentAnalysisService() {
        // El constructor se deja vacío para un control manual de la inicialización.
    }
    
    // Método que realiza la carga pesada de los modelos.
    public void init() {
        if (this.pipeline == null) {
            System.out.println("Iniciando SentimentAnalysisService...");
            Properties props = new Properties();
            // Especifica explícitamente la ruta de cada modelo requerido para español.
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
            props.setProperty("tokenize.language", "es");
            props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish-ud.tagger");
            props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/spanishPCFG.ser.gz");
            props.setProperty("sentiment.model", "edu/stanford/nlp/models/sentiment/sentiment.spanish.unlabeled.uncased.simplification.dev.ser.gz");
            
            this.pipeline = new StanfordCoreNLP(props);
            System.out.println("SentimentAnalysisService iniciado correctamente.");
        }
    }

    public static synchronized SentimentAnalysisService getInstance() {
        if (instance == null) {
            instance = new SentimentAnalysisService();
        }
        return instance;
    }

    public String getSentiment(String text) {
        if (this.pipeline == null) {
            System.err.println("ERROR CRÍTICO: El servicio de sentimiento fue llamado antes de ser inicializado.");
            return "Neutral";
        }
        if (text == null || text.trim().isEmpty()) {
            return "Neutral";
        }

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            return sentence.get(SentimentCoreAnnotations.SentimentClass.class);
        }
        
        return "Neutral";
    }
}