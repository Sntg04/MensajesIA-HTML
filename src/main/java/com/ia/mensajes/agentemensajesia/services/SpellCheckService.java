package com.ia.mensajes.agentemensajesia.services;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
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

    private SpellCheckService() {}

    public void init() {
        if (langTool == null) {
            try {
                System.out.println("Iniciando SpellCheckService con Diccionario Personalizado Extendido...");
                this.langTool = new JLanguageTool(new Spanish());

                // --- DICCIONARIO PERSONALIZADO EXTENDIDO ---
                // Se añaden todas las palabras de negocio. Se convierten a minúsculas
                // para asegurar que la IA las reconozca sin importar el caso (mayúscula/minúscula).
                List<String> palabrasDeNegocio = Arrays.asList(
                    // Palabras originales
                    "preaprobado", "credito", "whatsapp", "info", "wst",
                    // Nueva lista de palabras (ya en minúsculas)
                    "frscompania", "inf", "t&c", "sobrecostos", "dto", "dcto", "sured",
                    "billetecla", "dinerbacano", "morlong", "platanow", "platax",
                    "transfelicia", "htt", "morloan", "tyc", "fintech", "dctos"
                );
                
                // Se busca la regla de ortografía y se le añaden nuestras palabras a la lista de excepciones.
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

    public Map<String, String> findMisspelledWordsWithSuggestions(String text) {
        try {
            List<RuleMatch> matches = langTool.check(text);
            
            return matches.stream()
                    .filter(match -> match.getRule().isDictionaryBasedSpellingRule())
                    .filter(match -> !match.getSuggestedReplacements().isEmpty())
                    .filter(match -> {
                        String originalWord = text.substring(match.getFromPos(), match.getToPos());
                        if (originalWord.matches(".*\\d.*") || originalWord.length() <= 2) {
                            return false;
                        }
                        String suggestedWord = match.getSuggestedReplacements().get(0);
                        return !normalizeText(originalWord).equals(normalizeText(suggestedWord));
                    })
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