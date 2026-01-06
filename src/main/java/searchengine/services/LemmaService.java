package searchengine.services;

import java.util.Map;
import java.util.Set;

/**
 * Сервис лемматизации текста.
 * Предоставляет методы для извлечения лемм из текста и поисковых запросов.
 *
 * @author Кирилл Христич
 */
public interface LemmaService {
    /**
     * Извлекает леммы из текста и подсчитывает их частоту.
     *
     * @param text исходный текст
     * @return Map, где ключ - лемма, значение - частота встречаемости
     */
    Map<String, Integer> extractLemmas(String text);

    /**
     * Очищает HTML-текст, удаляя теги и оставляя только текст.
     *
     * @param html HTML-текст
     * @return очищенный текст
     */
    String cleanHtml(String html);

    /**
     * Создает сниппет для страницы на основе поискового запроса.
     * Выделяет в тексте слова из запроса.
     *
     * @param content HTML-содержимое страницы
     * @param query   поисковый запрос
     * @return сниппет с выделенными словами
     */
    String getSnippet(String content, String query);

    /**
     * Извлекает леммы из поискового запроса.
     *
     * @param query поисковый запрос
     * @return множество лемм из запроса
     */
    Set<String> getQueryLemmas(String query);
}
