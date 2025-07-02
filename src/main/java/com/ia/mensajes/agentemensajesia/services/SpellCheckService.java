package com.ia.mensajes.agentemensajesia.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
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
            System.out.println("Iniciando SpellCheckService (cargando diccionario)...");
            try (InputStream is = getClass().getResourceAsStream("/es_dictionary.txt")) {
                if (is == null) {
                    throw new IOException("No se pudo encontrar el archivo de diccionario: es_dictionary.txt");
                }
                
                // --- CORRECCIÓN CLAVE ---
                // Ahora, normalizamos cada palabra del diccionario (quitamos tildes)
                // antes de guardarla. Esto asegura una comparación justa.
                new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                        .lines()
                        .forEach(line -> dictionary.add(normalizeWord(line)));

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
        
        // Limpiamos el texto de entrada para quedarnos solo con las palabras.
        String[] words = text.toLowerCase()
                             .replaceAll("[^a-zñáéíóúü\\s]", "") // Mantenemos las letras con tilde para el usuario
                             .split("\\s+");

        return Stream.of(words)
                .filter(word -> !word.isEmpty() && !dictionary.contains(normalizeWord(word)))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Método de ayuda para normalizar una palabra quitándole las tildes.
     * Tanto las palabras del diccionario como las del mensaje pasarán por aquí.
     */
    private String normalizeWord(String word) {
        if (word == null) return "";
        String normalized = Normalizer.normalize(word.toLowerCase(), Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
}