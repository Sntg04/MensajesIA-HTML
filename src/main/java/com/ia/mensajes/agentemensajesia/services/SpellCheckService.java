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
            System.out.println("Iniciando SpellCheckService con LanguageTool (Modo Rápido)...");
            this.langTool = new JLanguageTool(new Spanish());

            // OPTIMIZACIÓN DE VELOCIDAD: Desactivamos todas las reglas excepto las de ortografía.
            langTool.getAllRules().forEach(rule -> {
                if (!rule.isDictionaryBasedSpellingRule()) {
                    langTool.disableRule(rule.getId());
                }
            });
            
            System.out.println("SpellCheckService (LanguageTool) iniciado correctamente.");
        }
    }

    public static synchronized SpellCheckService getInstance() {
        if (instance == null) {
            instance = new SpellCheckService();
        }
        return instance;
    }

    /**
     * Revisa un texto y devuelve un mapa de palabras erróneas con sus sugerencias.
     * @param text El texto a revisar.
     * @return Un Mapa donde la clave es la palabra errónea y el valor es la sugerencia.
     */
    public Map<String, String> findMisspelledWordsWithSuggestions(String text) {
        try {
            List<RuleMatch> matches = langTool.check(text);
            
            return matches.stream()
                    .filter(match -> match.getRule().isDictionaryBasedSpellingRule())
                    .filter(match -> !match.getSuggestedReplacements().isEmpty()) // Solo errores con sugerencias.
                    .filter(match -> {
                        String originalWord = text.substring(match.getFromPos(), match.getToPos());
                        String suggestedWord = match.getSuggestedReplacements().get(0);
                        return !normalizeText(originalWord).equals(normalizeText(suggestedWord));
                    })
                    .collect(Collectors.toMap(
                        match -> text.substring(match.getFromPos(), match.getToPos()), // Clave: la palabra errónea
                        match -> match.getSuggestedReplacements().get(0),              // Valor: la primera sugerencia
                        (existing, replacement) -> existing                          // Evita duplicados
                    ));
        } catch (IOException e) {
            System.err.println("Error al procesar el texto con LanguageTool: " + e.getMessage());
            return Map.of(); // Devuelve un mapa vacío en caso de error.
        }
    }

    private String normalizeText(String texto) {
        if (texto == null) return "";
        String textoNormalizado = Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD);
        return textoNormalizado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
}