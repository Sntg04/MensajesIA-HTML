package com.ia.mensajes.agentemensajesia.services;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
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
            // Se inicializa la herramienta para el idioma español.
            this.langTool = new JLanguageTool(new Spanish());

            // --- OPTIMIZACIÓN DE VELOCIDAD ---
            // Desactivamos todas las reglas excepto las de ortografía del diccionario.
            // Esto hace que la revisión sea mucho más rápida.
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
     * Revisa un texto y devuelve una lista de las palabras con errores ortográficos REALES,
     * ignorando los errores que son solo por falta de tildes.
     * @param text El texto a revisar.
     * @return Una lista de Strings con las palabras mal escritas.
     */
    public List<String> findMisspelledWords(String text) {
        try {
            List<RuleMatch> matches = langTool.check(text);
            
            return matches.stream()
                    // Filtramos por errores de diccionario, que son los de ortografía.
                    .filter(match -> match.getRule().isDictionaryBasedSpellingRule())
                    // Verificamos si es un error real o solo una tilde.
                    .filter(match -> {
                        if (match.getSuggestedReplacements().isEmpty()) {
                            return true; // Si no hay sugerencias, es un error real.
                        }
                        String originalWord = text.substring(match.getFromPos(), match.getToPos());
                        String suggestedWord = match.getSuggestedReplacements().get(0); // Tomamos la primera sugerencia
                        
                        // Normalizamos ambas palabras (sin tildes) y las comparamos.
                        String normalizedOriginal = normalizeText(originalWord);
                        String normalizedSuggested = normalizeText(suggestedWord);
                        
                        // Si son iguales después de normalizar, era solo un error de tilde y lo ignoramos.
                        return !normalizedOriginal.equals(normalizedSuggested);
                    })
                    .map(match -> text.substring(match.getFromPos(), match.getToPos()))
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error al procesar el texto con LanguageTool: " + e.getMessage());
            return List.of();
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