package com.ia.mensajes.agentemensajesia.services;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;

public class SentimentAnalysisService {

    private static SentimentAnalysisService instance;
    private final StanfordCoreNLP pipeline;

    private SentimentAnalysisService() {
        System.out.println("Iniciando SentimentAnalysisService...");
        Properties props = new Properties();
        // El 'sentiment' annotator depende de 'parse', que a su vez depende de 'tokenize,ssplit,pos,lemma'
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
        props.setProperty("coref.algorithm", "neural");
        // Especifica que las propiedades son para español
        props.setProperty("props", "StanfordCoreNLP-spanish.properties");
        
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
     * @return Una cadena que representa el sentimiento: "Muy Negativo", "Negativo", "Neutral", "Positivo", "Muy Positivo".
     */
    public String getSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Neutral";
        }

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        // El sentimiento se analiza por oración. Usaremos el de la primera oración como el sentimiento general.
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            // RNNCoreAnnotations.getPredictedClass(tree) devuelve un número de 0 (Muy Negativo) a 4 (Muy Positivo)
            // Aquí simplemente retornamos la etiqueta de texto.
            return sentence.get(SentimentCoreAnnotations.SentimentClass.class);
        }
        
        return "Neutral"; // Si no se encuentra ninguna oración.
    }
}