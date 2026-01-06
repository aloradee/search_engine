package searchengine.util;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Класс для морфологического анализа текста.
 * Выполняет лемматизацию русских и английских слов.
 *
 * @author Кирилл Христич
 */
@Component
public class LemmaFinder {
    private final LuceneMorphology russianMorphology;
    private final LuceneMorphology englishMorphology;

    private static final Pattern RUSSIAN_WORD_PATTERN = Pattern.compile("[а-яё]+");
    private static final Pattern ENGLISH_WORD_PATTERN = Pattern.compile("[a-z]+");
    private static final Set<String> EXCLUDED_TYPES = Set.of(
            "МЕЖД", "СОЮЗ", "ПРЕДЛ", "ЧАСТ", "INT", "CONJ", "PREP", "PART"
    );

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "это", "как", "так", "и", "в", "над", "к", "до", "не", "на", "но", "за", "то", "с", "ли", "а", "во", "от",
            "со", "для", "о", "же", "ну", "вы", "бы", "что", "кто", "он", "она", "оно", "они", "мы", "вы", "ты", "я",
            "свой", "весь", "который", "где", "когда", "какой", "нибудь", "там", "тут", "вот", "такой", "такая",
            "такое", "такие", "еще", "уже", "или", "из", "по", "при", "без", "перед", "после", "через", "между",
            "под", "над", "возле", "вокруг", "внутри", "вне", "близ", "вдоль", "поперек", "сквозь", "среди",
            "против", "ради", "благодаря", "ввиду", "вследствие", "вопреки", "согласно", "соответственно",
            "вроде", "наподобие", "насчет", "включая", "исключая", "кроме", "сверх", "свыше", "подобно",
            "наряду", "вместе", "впереди", "позади", "вверху", "внизу", "справа", "слева", "вперед", "назад",
            "вверх", "вниз", "вправо", "влево", "внутрь", "наружу", "туда", "сюда", "оттуда", "отсюда",
            "везде", "всюду", "нигде", "никуда", "ниоткуда", "некогда", "никогда", "нисколько", "ничуть",
            "никак", "ничем", "нечем", "некого", "нечего", "некуда", "неоткуда", "незачем", "некем", "нечем",
            "the", "and", "or", "but", "if", "because", "as", "what", "which", "this", "that", "these", "those",
            "then", "just", "so", "than", "such", "both", "through", "about", "for", "is", "of", "while", "during"
    ));

    /**
     * Конструктор инициализирует морфологические анализаторы.
     *
     * @throws RuntimeException если не удалось инициализировать анализаторы
     */
    public LemmaFinder() {
        try {
            this.russianMorphology = new RussianLuceneMorphology();
            this.englishMorphology = new EnglishLuceneMorphology();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации лемматизаторов", e);
        }
    }

    /**
     * Извлекает леммы из текста и подсчитывает их частоту.
     * Удаляет стоп-слова и служебные части речи.
     *
     * @param text исходный текст
     * @return Map, где ключ - лемма, значение - частота встречаемости
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
            if (word.isEmpty() || word.length() < 2 || STOP_WORDS.contains(word)) {
                continue;
            }

            try {
                Set<String> lemmas = getLemmas(word);
                for (String lemma : lemmas) {
                    if (!STOP_WORDS.contains(lemma)) {
                        lemmaCount.put(lemma, lemmaCount.getOrDefault(lemma, 0) + 1);
                    }
                }
            } catch (Exception e) {
            }
        }

        return lemmaCount;
    }

    /**
     * Извлекает леммы из одного слова.
     * Определяет язык слова и применяет соответствующий морфологический анализатор.
     *
     * @param word исходное слово
     * @return множество лемм (нормальных форм) слова
     */
    public Set<String> getLemmas(String word) {
        Set<String> lemmas = new HashSet<>();

        if (word == null || word.isEmpty() || STOP_WORDS.contains(word.toLowerCase())) {
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
     * Проверяет, содержит ли слово служебную часть речи.
     *
     * @param morphInfo морфологическая информация о слове
     * @return true если слово является служебным, false в противном случае
     */
    private boolean isExcluded(List<String> morphInfo) {
        return morphInfo.stream()
                .anyMatch(info -> EXCLUDED_TYPES.stream().anyMatch(info::contains));
    }

    /**
     * Извлекает леммы из поискового запроса.
     * Очищает запрос от лишних символов и удаляет стоп-слова.
     *
     * @param query поисковый запрос
     * @return множество лемм из запроса
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
            if (word.length() < 2 || STOP_WORDS.contains(word)) {
                continue;
            }
            Set<String> wordLemmas = getLemmas(word);
            wordLemmas.removeAll(STOP_WORDS);
            allLemmas.addAll(wordLemmas);
        }
        return allLemmas;
    }
}