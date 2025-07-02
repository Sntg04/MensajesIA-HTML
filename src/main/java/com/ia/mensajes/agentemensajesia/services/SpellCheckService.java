package com.ia.mensajes.agentemensajesia.services;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Spanish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

public class SpellCheckService {

    private static SpellCheckService instance;
    private JLanguageTool langTool;

    // --- DICCIONARIO DE CORRECCIONES PERSONALIZADAS ---
    private static final Map<String, String> CUSTOM_CORRECTIONS = new HashMap<>();
    static {
        CUSTOM_CORRECTIONS.put("info", "información");
        CUSTOM_CORRECTIONS.put("wst", "whatsapp");
        CUSTOM_CORRECTIONS.put("whats", "whatsapp");
        CUSTOM_CORRECTIONS.put("cel", "celular");
        // Puedes añadir más abreviaturas y sus correcciones aquí
    }

    private SpellCheckService() {}

    public void init() {
        if (langTool == null) {
            try {
                System.out.println("Iniciando SpellCheckService con LanguageTool y Diccionario Personalizado...");
                this.langTool = new JLanguageTool(new Spanish());

                // Añadimos nuestro vocabulario de negocio que SIEMPRE será correcto.
                List<String> palabrasDeNegocio = Arrays.asList("preaprobado", "credito", "whatsapp");
                for (Rule rule : langTool.getAllRules()) {
                    if (rule instanceof SpellingCheckRule) {
                        ((SpellingCheckRule) rule).addIgnoreTokens(palabrasDeNegocio);
                    }
                }
                System.out.println("Diccionario personalizado cargado con " + palabrasDeNegocio.size() + " palabras de negocio.");
                
                // OPTIMIZACIÓN DE VELOCIDAD: Desactivamos el resto de reglas.
                langTool.getAllRules().forEach(rule -> {
                    if (!rule.isDictionaryBasedSpellingRule()) {
                        langTool.disableRule(rule.getId());
                    }
                });
                
                System.out.println("SpellCheckService (LanguageTool) iniciado correctamente.");
            } catch (Exception e) {
                throw new RuntimeException("Fallo al inicializar LanguageTool", e);
            }
        }
    }

    public static synchronized SpellCheckService getInstance() {
        if (instance == null) {
            instance = new SpellCheckService();
        }
        return instance;
    }

    /**
     * Revisa un texto y devuelve un mapa de palabras erróneas con sus sugerencias,
     * priorizando las correcciones personalizadas.
     */
    public Map<String, String> findMisspelledWordsWithSuggestions(String text) {
        try {
            List<RuleMatch> matches = langTool.check(text);
            
            return matches.stream()
                    .filter(match -> match.getRule().isDictionaryBasedSpellingRule())
                    .filter(match -> {
                        String originalWord = text.substring(match.getFromPos(), match.getToPos());
                        // Ignorar cualquier "palabra" que contenga números o sea muy corta.
                        if (originalWord.matches(".*\\d.*") || originalWord.length() <= 2) {
                            return false;
                        }
                        // Si la palabra está en nuestro diccionario personalizado de correcciones, SÍ la procesamos.
                        if (CUSTOM_CORRECTIONS.containsKey(originalWord.toLowerCase())) {
                            return true;
                        }
                        // Si no, verificamos que no sea solo un error de tilde.
                        if (match.getSuggestedReplacements().isEmpty()) {
                           return true; // Es un error real sin sugerencia
                        }
                        String suggestedWord = match.getSuggestedReplacements().get(0);
                        return !normalizeText(originalWord).equals(normalizeText(suggestedWord));
                    })
                    .collect(Collectors.toMap(
                        match -> text.substring(match.getFromPos(), match.getToPos()),
                        match -> {
                            // --- LÓGICA DE SUGERENCIA INTELIGENTE ---
                            String originalWord = text.substring(match.getFromPos(), match.getToPos()).toLowerCase();
                            // Si tenemos una corrección personalizada, la usamos.
                            if (CUSTOM_CORRECTIONS.containsKey(originalWord)) {
                                return CUSTOM_CORRECTIONS.get(originalWord);
                            }
                            // Si no, usamos la sugerencia de LanguageTool.
                            return match.getSuggestedReplacements().get(0);
                        },
                        (existing, replacement) -> existing 
                    ));
        } catch (IOException e) {
            System.err.println("Error al procesar el texto con LanguageTool: " + e.getMessage());
            return Map.of();
        }
    }

    private String normalizeText(String texto) {
        if (texto == null) return "";
        String textoNormalizado = Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD);
        return textoNormalizado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
}