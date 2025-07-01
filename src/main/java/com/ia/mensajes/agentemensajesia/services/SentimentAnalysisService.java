package com.ia.mensajes.agentemensajesia.services;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;

public class SentimentAnalysisService {

    private static SentimentAnalysisService instance;
    private final StanfordCoreNLP pipeline;

    private SentimentAnalysisService() {
        System.out.println("Iniciando SentimentAnalysisService...");
        Properties props = new Properties();
        
        // --- CORRECCIÓN CLAVE ---
        // Se especifica explícitamente la ruta de cada modelo requerido para español.
        // Esto evita que la biblioteca busque los modelos por defecto en inglés.
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
        props.setProperty("tokenize.language", "es");
        props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish-ud.tagger");
        props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/spanishPCFG.ser.gz");
        props.setProperty("sentiment.model", "edu/stanford/nlp/models/sentiment/sentiment.spanish.unlabeled.uncased.simplification.dev.ser.gz");
        // La propiedad 'props' ya no es necesaria, ya que hemos sido más específicos.
        
        this.pipeline = new StanfordCoreNLP(props);
        System.out.println("SentimentAnalysisService iniciado correctamente.");
    }

    public static synchronized SentimentAnalysisService getInstance() {
        if (instance == null) {
            instance = new SentimentAnalysisService();
        }
        return instance;
    }

    /**
     * Analiza el sentimiento de un texto y lo clasifica.
     * @param text El texto a analizar.
     * @return Una cadena que representa el sentimiento: "Very negative", "Negative", "Neutral", "Positive", "Very positive".
     */
    public String getSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Neutral";
        }

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        // El sentimiento se analiza por oración. Usaremos el de la primera oración como el sentimiento general.
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            // Retorna la etiqueta de sentimiento (en inglés, ya que es el output del modelo)
            return sentence.get(SentimentCoreAnnotations.SentimentClass.class);
        }
        
        return "Neutral"; // Si no se encuentra ninguna oración.
    }
}