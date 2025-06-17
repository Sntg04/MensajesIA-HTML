package com.ia.mensajes.agentemensajesia.ia;

import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Pattern;

public class ClasificadorMensajes {

    // --- NUEVO: ThreadLocal para asegurar una instancia por hilo ---
    private static final ThreadLocal<ClasificadorMensajes> threadInstance = 
        ThreadLocal.withInitial(ClasificadorMensajes::new);

    // --- NUEVO: Método estático para obtener la instancia segura para el hilo actual ---
    public static ClasificadorMensajes getInstance() {
        return threadInstance.get();
    }

    // Lista de palabras clave (lemas)
    private static final Set<String> PALABRAS_ALERTA_LEMAS = Set.of(
        "deber", "obligacion", "incumplimiento", "proceso", "juridico", "abogado",
        "embargo", "retencion", "sancion", "demanda", "legal", "reportar", "cobro",
        "amenaza", "consecuencia", "evasion", "responsabilidad", "deuda", "mora",
        "buro", "presion", "penalizacion", "visita", "tercero", "localizacion", "castigado"
    );

    // Los modelos ahora son privados y no estáticos
    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;
    private final LemmatizerME lemmatizer;

    // El constructor ahora es privado para ser llamado solo por ThreadLocal
    private ClasificadorMensajes() {
        try {
            // Cargar modelo de Tokenizer
            try (InputStream modelIn = getClass().getResourceAsStream("/models/es/es-token.bin")) {
                if (modelIn == null) throw new IOException("No se encontró el modelo de tokenizer: /models/es/es-token.bin");
                TokenizerModel tokenModel = new TokenizerModel(modelIn);
                this.tokenizer = new TokenizerME(tokenModel);
            }

            // Cargar modelo de POS Tagger
            try (InputStream modelIn = getClass().getResourceAsStream("/models/es/es-pos-maxent.bin")) {
                if (modelIn == null) throw new IOException("No se encontró el modelo POS: /models/es/es-pos-maxent.bin");
                POSModel posModel = new POSModel(modelIn);
                this.posTagger = new POSTaggerME(posModel);
            }

            // Cargar MODELO de Lemmatizer
            try (InputStream modelIn = getClass().getResourceAsStream("/models/es/es-lemmatizer.bin")) {
                if (modelIn == null) throw new IOException("No se encontró el modelo de lematización: /models/es/es-lemmatizer.bin");
                LemmatizerModel lemmatizerModel = new LemmatizerModel(modelIn);
                this.lemmatizer = new LemmatizerME(lemmatizerModel);
            }

        } catch (IOException e) {
            throw new RuntimeException("Fallo al cargar los modelos de NLP.", e);
        }
    }
    
    public ResultadoClasificacion clasificar(String textoMensaje) {
        if (textoMensaje == null || textoMensaje.trim().isEmpty()) {
            return new ResultadoClasificacion("Bueno", null);
        }

        String mensajeNormalizado = normalizar(textoMensaje);
        String[] tokens = tokenizer.tokenize(mensajeNormalizado);
        String[] tags = posTagger.tag(tokens);
        String[] lemas = lemmatizer.lemmatize(tokens, tags);

        for (String lema : lemas) {
            if (PALABRAS_ALERTA_LEMAS.contains(lema)) {
                return new ResultadoClasificacion("Alerta", lema);
            }
        }
        
        return new ResultadoClasificacion("Bueno", null);
    }

    public String reescribir(String textoOriginal) {
        return "Sugerencia: Intente reformular la frase usando un tono más neutral y enfocado en soluciones, evitando palabras que puedan interpretarse como una amenaza o presión.";
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        String textoNormalizado = texto.toLowerCase();
        textoNormalizado = Normalizer.normalize(textoNormalizado, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        textoNormalizado = pattern.matcher(textoNormalizado).replaceAll("");
        textoNormalizado = textoNormalizado.replaceAll("\\s+", " ").trim();
        return textoNormalizado;
    }
}