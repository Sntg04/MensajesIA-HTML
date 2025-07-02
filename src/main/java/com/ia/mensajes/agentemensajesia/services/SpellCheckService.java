package com.ia.mensajes.agentemensajesia.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Spanish;
import org.languagetool.rules.RuleMatch;

public class SpellCheckService {

    private static SpellCheckService instance;
    private JLanguageTool langTool;

    private SpellCheckService() {}

    public void init() {
        if (langTool == null) {
            try {
                System.out.println("Iniciando SpellCheckService con LanguageTool y Diccionario Personalizado...");
                this.langTool = new JLanguageTool(new Spanish());

                // Cargar palabras del diccionario personalizado
                try (InputStream is = getClass().getResourceAsStream("/custom_dictionary.txt")) {
                    if (is != null) {
                        List<String> userWords = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                                .lines()
                                .collect(Collectors.toList());
                        langTool.getUserWords().addAll(userWords);
                        System.out.println("Diccionario personalizado cargado con " + userWords.size() + " palabras.");
                    } else {
                        System.out.println("Advertencia: No se encontró el diccionario personalizado (custom_dictionary.txt).");
                    }
                }

                // OPTIMIZACIÓN DE VELOCIDAD: Desactivamos todas las reglas excepto las de ortografía.
                langTool.getAllRules().forEach(rule -> {
                    if (!rule.isDictionaryBasedSpellingRule()) {
                        langTool.disableRule(rule.getId());
                    }
                });
                
                System.out.println("SpellCheckService (LanguageTool) iniciado correctamente.");
            } catch (IOException e) {
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
     * ignorando errores de tildes y fragmentos con números.
     */
    public Map<String, String> findMisspelledWordsWithSuggestions(String text) {
        try {
            List<RuleMatch> matches = langTool.check(text);
            
            return matches.stream()
                    // 1. Filtrar solo por reglas de ortografía del diccionario.
                    .filter(match -> match.getRule().isDictionaryBasedSpellingRule())
                    
                    // 2. Extraer la palabra original que fue marcada como error.
                    .map(match -> text.substring(match.getFromPos(), match.getToPos()))
                    
                    // 3. ¡SOLUCIÓN! Ignorar cualquier "palabra" que contenga un número.
                    .filter(word -> !word.matches(".*\\d.*"))
                    
                    // 4. Volvemos a revisar con la herramienta para obtener las sugerencias de las palabras filtradas.
                    .flatMap(word -> {
                        try {
                            return langTool.check(word).stream();
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(match -> match != null && !match.getSuggestedReplacements().isEmpty())
                    
                    // 5. Ignorar si la sugerencia es solo la misma palabra con tilde.
                    .filter(match -> {
                        String originalWord = text.substring(match.getFromPos(), match.getToPos());
                        String suggestedWord = match.getSuggestedReplacements().get(0);
                        return !normalizeText(originalWord).equals(normalizeText(suggestedWord));
                    })
                    
                    // 6. Recolectar en un mapa, evitando duplicados.
                    .collect(Collectors.toMap(
                        match -> text.substring(match.getFromPos(), match.getToPos()),
                        match -> match.getSuggestedReplacements().get(0),
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