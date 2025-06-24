package com.ia.mensajes.agentemensajesia.ia;

import java.io.IOException;
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

    private static ClasificadorMensajes instance;
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    private LemmatizerME lemmatizer;

    private static final Set<String> PALABRAS_ALERTA_LEMAS = new HashSet<>(Arrays.asList(
            "urgente", "importante", "premio", "ganador", "oferta", "gratis",
            "promocion", "descuento", "exclusivo", "limitado", "click", "aqui",
            "verificar", "cuenta", "banco", "tarjeta", "credito", "contrasena",
            "seguridad", "suspension", "factura", "deuda", "fraude", "hack"
    ));

    private ClasificadorMensajes() {
        try {
            System.out.println("Cargando modelos de OpenNLP...");
            try (InputStream tokenModelIn = getClass().getResourceAsStream("/models/es/es-token.bin"); InputStream posModelIn = getClass().getResourceAsStream("/models/es/es-pos-maxent.bin"); InputStream lemmaModelIn = getClass().getResourceAsStream("/models/es/es-lemmatizer.bin")) {

                if (tokenModelIn == null) {
                    throw new IOException("No se encontró el modelo de tokenizer: /models/es/es-token.bin");
                }
                TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
                this.tokenizer = new TokenizerME(tokenModel);

                if (posModelIn == null) {
                    throw new IOException("No se encontró el modelo POS: /models/es/es-pos-maxent.bin");
                }
                POSModel posModel = new POSModel(posModelIn);
                this.posTagger = new POSTaggerME(posModel);

                if (lemmaModelIn == null) {
                    throw new IOException("No se encontró el modelo de lematización: /models/es/es-lemmatizer.bin");
                }
                LemmatizerModel lemmaModel = new LemmatizerModel(lemmaModelIn);
                this.lemmatizer = new LemmatizerME(lemmaModel);
            }
            System.out.println("Modelos cargados exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al cargar los modelos de NLP. Asegúrate de que los archivos .bin estén en src/main/resources/models/es/");
            throw new RuntimeException("Fallo al cargar los modelos de NLP.", e);
        }
    }

    public static synchronized ClasificadorMensajes getInstance() {
        if (instance == null) {
            instance = new ClasificadorMensajes();
        }
        return instance;
    }

    // En ClasificadorMensajes.java, dentro del método normalizar
    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        texto = texto.toLowerCase();
        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        texto = texto.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        // Línea extra para quitar más símbolos:
        texto = texto.replaceAll("[^a-z0-9\\s]", " "); // Reemplaza cualquier cosa que no sea letra, número o espacio.
        return texto;
    }

    /**
     * Clasifica un mensaje. Si ocurre un error interno en la librería de NLP,
     * lo captura, lo reporta en los logs y devuelve un estado de error.
     *
     * @param textoMensaje El texto a clasificar.
     * @return Un objeto ResultadoClasificacion.
     */
    public ResultadoClasificacion clasificar(String textoMensaje) {
        if (textoMensaje == null || textoMensaje.trim().isEmpty()) {
            return new ResultadoClasificacion("Bueno", "N/A");
        }

        try {
            // --- INICIO DEL CÓDIGO FRÁGIL ---
            String mensajeNormalizado = normalizar(textoMensaje);
            String[] tokens = tokenizer.tokenize(mensajeNormalizado);
            String[] tags = posTagger.tag(tokens);
            String[] lemas = lemmatizer.lemmatize(tokens, tags);

            for (String lema : lemas) {
                if (PALABRAS_ALERTA_LEMAS.contains(lema)) {
                    return new ResultadoClasificacion("Alerta", "Palabra clave: '" + lema + "'");
                }
            }
            // --- FIN DEL CÓDIGO FRÁGIL ---

            return new ResultadoClasificacion("Bueno", "N/A");

        } catch (Exception e) {
            // Si algo falla en el bloque try, lo capturamos aquí.
            System.err.println("-------------------------------------------------");
            System.err.println("ERROR: Fallo el procesamiento de NLP para un mensaje.");
            System.err.println("Causa: " + e.getMessage());
            System.err.println("Texto del Mensaje Problemático: " + textoMensaje);
            System.err.println("-------------------------------------------------");

            // Devolvemos un resultado especial para poder identificarlo en la base de datos.
            return new ResultadoClasificacion("Error de Análisis", "El motor de IA no pudo procesar este texto.");
        }
    }
}
