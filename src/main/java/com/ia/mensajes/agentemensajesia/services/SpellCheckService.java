package com.ia.mensajes.agentemensajesia.services;

import java.io.IOException;
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
            System.out.println("Iniciando SpellCheckService con LanguageTool...");
            // LanguageTool es una librería completa. "new Spanish()" carga todas las reglas.
            this.langTool = new JLanguageTool(new Spanish());
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
     * Revisa un texto y devuelve una lista de las palabras con errores ortográficos.
     * @param text El texto a revisar.
     * @return Una lista de Strings con las palabras mal escritas.
     */
    public List<String> findMisspelledWords(String text) {
        try {
            List<RuleMatch> matches = langTool.check(text);
            
            return matches.stream()
                    // Filtramos solo los errores que son de tipo ortográfico.
                    // Esto ignora errores gramaticales o de puntuación.
                    .filter(match -> match.getRule().isDictionaryBasedSpellingRule())
                    // Obtenemos la palabra mal escrita del error.
                    .map(match -> {
                        // Extraemos la palabra exacta del texto original.
                        int from = match.getFromPos();
                        int to = match.getToPos();
                        return text.substring(from, to);
                    })
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error al procesar el texto con LanguageTool: " + e.getMessage());
            return List.of(); // Devuelve una lista vacía en caso de error.
        }
    }
}