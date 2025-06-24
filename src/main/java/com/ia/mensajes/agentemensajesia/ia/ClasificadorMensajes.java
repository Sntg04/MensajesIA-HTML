package com.ia.mensajes.agentemensajesia.ia;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class ClasificadorMensajes {

    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    private LemmatizerME lemmatizer;
    
    private static final Set<String> PALABRAS_ALERTA_LEMAS = new HashSet<>(Arrays.asList(
            "urgente", "importante", "premio", "ganador", "oferta", "gratis",
            "promocion", "descuento", "exclusivo", "limitado", "click", "aqui",
            "verificar", "cuenta", "banco", "tarjeta", "credito", "contrasena",
            "seguridad", "suspension", "factura", "deuda", "fraude", "hack"
    ));

    public void cargarModelos() {
        try {
            System.out.println("Cargando modelos de OpenNLP...");
            InputStream tokenModelIn = getClass().getResourceAsStream("/models/es/es-token.bin");
            TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
            this.tokenizer = new TokenizerME(tokenModel);

            InputStream posModelIn = getClass().getResourceAsStream("/models/es/es-pos-maxent.bin");
            POSModel posModel = new POSModel(posModelIn);
            this.posTagger = new POSTaggerME(posModel);

            InputStream lemmaModelIn = getClass().getResourceAsStream("/models/es/es-lemmatizer.bin");
            LemmatizerModel lemmaModel = new LemmatizerModel(lemmaModelIn);
            this.lemmatizer = new LemmatizerME(lemmaModel);
            System.out.println("Modelos cargados exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al cargar los modelos de NLP. Asegúrate de que los archivos .bin estén en src/main/resources/models/es/");
            throw new RuntimeException("Fallo al cargar los modelos de NLP.", e);
        }
    }

    private String normalizar(String texto) {
        if (texto == null) return "";
        texto = texto.toLowerCase();
        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        texto = texto.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        texto = texto.replaceAll("[^a-z0-9\\s]", "");
        return texto;
    }

    public ResultadoClasificacion clasificar(String textoMensaje) {
        if (textoMensaje == null || textoMensaje.trim().isEmpty()) {
            return new ResultadoClasificacion("Bueno", 1.0);
        }

        String mensajeNormalizado = normalizar(textoMensaje);
        String[] tokens = tokenizer.tokenize(mensajeNormalizado);
        String[] tags = posTagger.tag(tokens);
        String[] lemas = lemmatizer.lemmatize(tokens, tags);

        for (String lema : lemas) {
            if (PALABRAS_ALERTA_LEMAS.contains(lema)) {
                // Devolver confianza 0.9 (90%) si encuentra una palabra de alerta
                return new ResultadoClasificacion("Alerta", 0.9);
            }
        }
        // Devolver confianza 1.0 (100%) si es un mensaje bueno
        return new ResultadoClasificacion("Bueno", 1.0);
    }
}