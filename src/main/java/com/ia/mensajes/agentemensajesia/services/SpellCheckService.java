package com.ia.mensajes.agentemensajesia.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                
                // Se lee cada palabra del diccionario y se normaliza ANTES de guardarla.
                new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                        .lines()
                        .forEach(line -> dictionary.add(normalizeText(line)));

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

    /**
     * Revisa un texto y devuelve una lista de las palabras con errores ortográficos.
     * @param text El texto a revisar.
     * @return Una lista de Strings con las palabras mal escritas.
     */
    public List<String> findMisspelledWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        // --- CORRECCIÓN CLAVE ---
        // 1. Limpiamos el texto de entrada para quedarnos solo con las palabras.
        // 2. Usamos una expresión regular para dividir el texto en palabras, preservando los caracteres originales.
        String[] words = text.toLowerCase().split("[^a-zñáéíóúü]+");
        
        // 3. Comparamos la versión normalizada de cada palabra con el diccionario (también normalizado).
        return Arrays.stream(words)
                .filter(word -> !word.isEmpty() && !dictionary.contains(normalizeText(word)))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Método de ayuda para normalizar texto: lo convierte a minúsculas y le quita las tildes.
     * Este método se usa tanto para el diccionario como para las palabras de los mensajes.
     */
    private String normalizeText(String texto) {
        if (texto == null) return "";
        // Normaliza el texto a su forma descompuesta (letra + diacrítico)
        String textoNormalizado = Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD);
        // Elimina los diacríticos (tildes)
        return textoNormalizado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
}