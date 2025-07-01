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

    private static final String SUGERENCIA_REFORMULACION = "Sugerencia: Intente reformular la frase usando un tono más neutral y enfocado en soluciones, evitando palabras que puedan interpretarse como una amenaza o presión.";

    // --- LISTA DE PALABRAS ACTUALIZADA SEGÚN LA IMAGEN ---
    private static final Set<String> PALABRAS_ALERTA_LEMAS = new HashSet<>(Arrays.asList(
            "abogado", "accion", "aplicacion", "automatico", "buro", "busqueda",
            "cartera", "castigado", "cobranza", "cobro", "contacto", "credito",
            "critico", "cuenta", "deber", "debito", "definitivo", "departamento",
            "derecha", "desentendido", "deuda", "domiciliaria", "drastica",
            "embargo", "escala", "evasion", "evasivo", "excusa", "externo",
            "financiero", "hacer", "honorario", "ignorar", "incumplimiento",
            "inmediato", "instancia", "interno", "irresponsable", "juridico",
            "legal", "localizacion", "medida", "mora", "negativo", "obligacion",
            "pago", "penalizacion", "portafolio", "presion", "proceder", "proceso",
            "protocolo", "referencia", "renuencia", "reportar", "responsabilidad",
            "retencion", "sancion", "silencio", "tercero", "tomar", "traslado", "visita"
    ));

    private ClasificadorMensajes() {
        try {
            System.out.println("Cargando modelos de OpenNLP...");
            try (InputStream tokenModelIn = getClass().getResourceAsStream("/models/es/es-token.bin");
                 InputStream posModelIn = getClass().getResourceAsStream("/models/es/es-pos-maxent.bin");
                 InputStream lemmaModelIn = getClass().getResourceAsStream("/models/es/es-lemmatizer.bin")) {

                if (tokenModelIn == null) throw new IOException("No se encontró el modelo de tokenizer: /models/es/es-token.bin");
                TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
                this.tokenizer = new TokenizerME(tokenModel);

                if (posModelIn == null) throw new IOException("No se encontró el modelo POS: /models/es/es-pos-maxent.bin");
                POSModel posModel = new POSModel(posModelIn);
                this.posTagger = new POSTaggerME(posModel);

                if (lemmaModelIn == null) throw new IOException("No se encontró el modelo de lematización: /models/es/es-lemmatizer.bin");
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

    private String normalizar(String texto) {
        if (texto == null) return "";
        texto = texto.toLowerCase();
        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        texto = texto.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        texto = texto.replaceAll("[^a-z0-9\\s]", " ");
        return texto;
    }

    public ResultadoClasificacion clasificar(String textoMensaje) {
        if (textoMensaje == null || textoMensaje.trim().isEmpty()) {
            return new ResultadoClasificacion("Bueno", "N/A");
        }
        try {
            String mensajeNormalizado = normalizar(textoMensaje);
            String[] tokens = tokenizer.tokenize(mensajeNormalizado);
            String[] tags = posTagger.tag(tokens);
            String[] lemas = lemmatizer.lemmatize(tokens, tags);

            for (String lema : lemas) {
                if (PALABRAS_ALERTA_LEMAS.contains(lema)) {
                    // La lógica de observación mejorada se mantiene
                    String observacion = "Palabra clave: '" + lema + "'. " + SUGERENCIA_REFORMULACION;
                    return new ResultadoClasificacion("Alerta", observacion);
                }
            }
            return new ResultadoClasificacion("Bueno", "N/A");

        } catch (Exception e) {
            System.err.println("-------------------------------------------------");
            System.err.println("ERROR: Fallo el procesamiento de NLP para un mensaje.");
            System.err.println("Causa: " + e.getMessage());
            System.err.println("Texto del Mensaje Problemático: " + textoMensaje);
            System.err.println("-------------------------------------------------");
            return new ResultadoClasificacion("Error de Análisis", "El motor de IA no pudo procesar este texto.");
        }
    }
}