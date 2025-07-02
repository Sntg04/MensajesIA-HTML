package com.ia.mensajes.agentemensajesia.services;

import java.io.IOException;
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
                System.out.println("Iniciando SpellCheckService con LanguageTool (Versión Corregida)...");
                // 1. Cargar la herramienta para el idioma español.
                this.langTool = new JLanguageTool(new Spanish());
                
                // 2. OPTIMIZACIÓN DE VELOCIDAD: Desactivamos todas las reglas que no sean de ortografía.
                // Esto hace que la revisión sea mucho más rápida y se centre solo en lo que nos interesa.
                this.langTool.getAllRules().forEach(rule -> {
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
     * ignorando errores de tildes y fragmentos con números.
     */
    public Map<String, String> findMisspelledWordsWithSuggestions(String text) {
        try {
            List<RuleMatch> matches = langTool.check(text);
            
            return matches.stream()
                    // 1. Filtrar solo por reglas de ortografía del diccionario.
                    .filter(match -> match.getRule().isDictionaryBasedSpellingRule())
                    
                    // 2. Extraer la palabra original que fue marcada como error.
                    .map(match -> {
                        String originalWord = text.substring(match.getFromPos(), match.getToPos());
                        // Si la "palabra" contiene números o es muy corta, la ignoramos.
                        if (originalWord.matches(".*\\d.*") || originalWord.length() <= 2) {
                            return null;
                        }
                        return match;
                    })
                    .filter(match -> match != null && !match.getSuggestedReplacements().isEmpty())
                    
                    // 3. Ignorar si la sugerencia es solo la misma palabra con tilde.
                    .filter(match -> {
                        String originalWord = text.substring(match.getFromPos(), match.getToPos());
                        String suggestedWord = match.getSuggestedReplacements().get(0);
                        return !normalizeText(originalWord).equals(normalizeText(suggestedWord));
                    })
                    
                    // 4. Recolectar en un mapa, evitando duplicados.
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

    /**
     * Método de ayuda para normalizar texto: lo convierte a minúsculas y le quita las tildes.
     */
    private String normalizeText(String texto) {
        if (texto == null) return "";
        String textoNormalizado = Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD);
        return textoNormalizado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
}