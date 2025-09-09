package searchengine.util;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class LemmaFinder {
    private final LuceneMorphology russianMorphology;
    private final LuceneMorphology englishMorphology;

    private static final Pattern RUSSIAN_WORD_PATTERN = Pattern.compile("[а-яё]+");
    private static final Pattern ENGLISH_WORD_PATTERN = Pattern.compile("[a-z]+");
    private static final Set<String> EXCLUDED_TYPES = Set.of("МЕЖД", "СОЮЗ", "ПРЕДЛ", "ЧАСТ", "INT", "CONJ", "PREP", "PART");

    public LemmaFinder() {
        try {
            this.russianMorphology = new RussianLuceneMorphology();
            this.englishMorphology = new EnglishLuceneMorphology();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации лемматизаторов", e);
        }
    }

    /**
     * Получает леммы и их количество из текста
     */
    public Map<String, Integer> getLemmaCount(String text) {
        Map<String, Integer> lemmaCount = new HashMap<>();
        if (text == null || text.isEmpty()) {
            return lemmaCount;
        }

        String[] words = text.toLowerCase()
                .replaceAll("[^а-яёa-z\\s]", " ")
                .split("\\s+");

        for (String word : words) {
            if (word.isEmpty() || word.length() < 3) {
                continue;
            }

            try {
                Set<String> lemmas = getLemmas(word);
                for (String lemma : lemmas) {
                    lemmaCount.put(lemma, lemmaCount.getOrDefault(lemma, 0) + 1);
                }
            } catch (Exception e) {
                e.getMessage();
            }
        }

        return lemmaCount;
    }

    /**
     * Получает леммы для одного слова
     */
    public Set<String> getLemmas(String word) {
        Set<String> lemmas = new HashSet<>();

        if (word == null || word.isEmpty()) {
            return lemmas;
        }

        boolean isRussian = RUSSIAN_WORD_PATTERN.matcher(word).matches();
        boolean isEnglish = ENGLISH_WORD_PATTERN.matcher(word).matches();

        if (!isRussian && !isEnglish) {
            return lemmas;
        }

        try {
            LuceneMorphology morphology = isRussian ? russianMorphology : englishMorphology;
            List<String> wordBaseForms = morphology.getNormalForms(word);
            List<String> morphInfo = morphology.getMorphInfo(word);

            if (isExcluded(morphInfo)) {
                return lemmas;
            }

            lemmas.addAll(wordBaseForms);
        } catch (Exception e) {
            lemmas.add(word);
        }

        return lemmas;
    }

    /**
     * Проверяет, является ли слово служебной частью речи
     */
    private boolean isExcluded(List<String> morphInfo) {
        return morphInfo.stream()
                .anyMatch(info -> EXCLUDED_TYPES.stream().anyMatch(info::contains));
    }

    /**
     * Получает все леммы из текста (без повторений)
     */
    public Set<String> getAllLemmas(String text) {
        return getLemmaCount(text).keySet();
    }

    /**
     * Получает леммы из поискового запроса
     */
    public Set<String> getQueryLemmas(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptySet();
        }

        String cleanQuery = query.toLowerCase()
                .replaceAll("[^а-яёa-z\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        Set<String> allLemmas = new HashSet<>();
        String[] words = cleanQuery.split("\\s+");

        for (String word : words) {
            if (word.length() < 3) {
                continue;
            }

            Set<String> wordLemmas = getLemmas(word);
            allLemmas.addAll(wordLemmas);
        }

        return allLemmas;
    }

    /**
     * Получает нормальную форму слова (первую лемму)
     */
    public String getNormalForm(String word) {
        Set<String> lemmas = getLemmas(word);
        return lemmas.isEmpty() ? word : lemmas.iterator().next();
    }

    /**
     * Анализирует морфологическую информацию слова
     */
    public String analyzeWord(String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }

        try {
            boolean isRussian = RUSSIAN_WORD_PATTERN.matcher(word).matches();
            LuceneMorphology morphology = isRussian ? russianMorphology : englishMorphology;

            List<String> morphInfo = morphology.getMorphInfo(word);
            return String.join("; ", morphInfo);
        } catch (Exception e) {
            return "Не удалось проанализировать";
        }
    }
}