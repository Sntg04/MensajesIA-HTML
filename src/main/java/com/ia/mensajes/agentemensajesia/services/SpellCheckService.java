package com.ia.mensajes.agentemensajesia.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpellCheckService {

    private static SpellCheckService instance;
    private final Set<String> dictionary = new HashSet<>();

    private SpellCheckService() {}

    public void init() {
        if (dictionary.isEmpty()) {
            System.out.println("Iniciando SpellCheckService (cargando diccionario local)...");
            try (InputStream is = getClass().getResourceAsStream("/es_dictionary.txt")) {
                if (is == null) {
                    throw new IOException("No se pudo encontrar el archivo de diccionario: es_dictionary.txt");
                }
                new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                        .lines()
                        .forEach(dictionary::add);
                System.out.println("SpellCheckService iniciado correctamente. Palabras cargadas: " + dictionary.size());
            } catch (IOException e) {
                throw new RuntimeException("Fallo al cargar el diccionario de español", e);
            }
        }
    }

    public static synchronized SpellCheckService getInstance() {
        if (instance == null) {
            instance = new SpellCheckService();
        }
        return instance;
    }

    public List<String> findMisspelledWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        // Limpiamos el texto para quedarnos solo con las palabras, quitando acentos para la comparación.
        String textoNormalizado = java.text.Normalizer.normalize(text.toLowerCase(), java.text.Normalizer.Form.NFD)
                                      .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                                      .replaceAll("[^a-zñ\\s]", "");
        
        String[] words = textoNormalizado.split("\\s+");

        return Stream.of(words)
                .filter(word -> !word.isEmpty() && !dictionary.contains(word))
                .distinct()
                .collect(Collectors.toList());
    }
}