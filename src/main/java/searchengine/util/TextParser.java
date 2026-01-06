package searchengine.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Класс для парсинга и обработки текста.
 * Очищает HTML и создает сниппеты для результатов поиска.
 *
 * @author Кирилл Христич
 */
@Component
public class TextParser {
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final int SNIPPET_LENGTH = 300;

    /**
     * Очищает HTML-текст, удаляя все теги и оставляя только текст.
     * Удаляет скрипты, стили и другие невидимые элементы.
     *
     * @param html HTML-текст
     * @return очищенный текст в нижнем регистре
     */
    public String cleanHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }

        try {
            Document doc = Jsoup.parse(html);
            doc.select("script, style, noscript, comment, iframe, embed, object").remove();

            return doc.text()
                    .toLowerCase()
                    .replaceAll("[^а-яёa-z\\s]", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
        } catch (Exception e) {
            return HTML_TAG_PATTERN.matcher(html)
                    .replaceAll(" ")
                    .toLowerCase()
                    .replaceAll("[^а-яёa-z\\s]", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
        }
    }

    /**
     * Создает сниппет для страницы на основе поискового запроса.
     * Находит первое вхождение любого слова из запроса и выделяет его в тексте.
     *
     * @param content     HTML-содержимое страницы
     * @param queryWords  множество лемм из поискового запроса
     * @return сниппет с выделенными словами или сообщение об отсутствии текста
     */
    public String createSnippet(String content, Set<String> queryWords) {
        if (content == null || content.isEmpty() || queryWords == null || queryWords.isEmpty()) {
            return "Текст не найден";
        }

        String cleanText = cleanHtml(content);
        if (cleanText.isEmpty()) {
            return "Текст не найден";
        }

        for (String word : queryWords) {
            int index = cleanText.indexOf(word);
            if (index != -1) {
                int start = Math.max(0, index - SNIPPET_LENGTH / 2);
                int end = Math.min(cleanText.length(), index + SNIPPET_LENGTH / 2);

                while (start > 0 && !Character.isWhitespace(cleanText.charAt(start - 1))) {
                    start--;
                }
                while (end < cleanText.length() && !Character.isWhitespace(cleanText.charAt(end))) {
                    end++;
                }

                String snippet = cleanText.substring(start, end);

                for (String queryWord : queryWords) {
                    snippet = snippet.replaceAll("(?i)(" + Pattern.quote(queryWord) + ")", "<b>$1</b>");
                }

                return (start > 0 ? "..." : "") + snippet + (end < cleanText.length() ? "..." : "");
            }
        }

        int length = Math.min(SNIPPET_LENGTH, cleanText.length());
        return cleanText.substring(0, length) + (cleanText.length() > length ? "..." : "");
    }
}