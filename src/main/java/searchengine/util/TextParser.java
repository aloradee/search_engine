package searchengine.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class TextParser {
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^а-яёa-z\\s]");
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s+");
    private static final Pattern WORD_PATTERN = Pattern.compile("[а-яёa-z]{3,}");

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "это", "как", "так", "и", "в", "над", "к", "до", "не", "на", "но", "за", "то", "с", "ли", "а", "во", "от", "со",
            "для", "о", "же", "ну", "вы", "бы", "что", "кто", "он", "она", "оно", "они", "мы", "вы", "ты", "я", "свой",
            "весь", "который", "где", "когда", "какой", "нибудь", "там", "тут", "вот", "какой", "такой", "такая", "такое",
            "такие", "еще", "уже", "или", "из", "по", "при", "без", "перед", "после", "через", "между", "под", "над", "возле",
            "вокруг", "внутри", "вне", "близ", "вдоль", "поперек", "сквозь", "среди", "против", "ради", "благодаря", "ввиду",
            "вследствие", "вопреки", "согласно", "соответственно", "вроде", "наподобие", "насчет", "включая", "исключая",
            "кроме", "сверх", "свыше", "подобно", "наряду", "вместе", "впереди", "позади", "вверху", "внизу", "справа",
            "слева", "вперед", "назад", "вверх", "вниз", "вправо", "влево", "внутрь", "наружу", "вперед", "назад", "туда",
            "сюда", "оттуда", "отсюда", "везде", "всюду", "нигде", "никуда", "ниоткуда", "некогда", "никогда", "нисколько",
            "ничуть", "никак", "ничем", "нечем", "некого", "нечего", "некуда", "неоткуда", "незачем", "некем", "нечем"
    ));

    /**
     * Очищает HTML от тегов и возвращает чистый текст
     */
    public String cleanHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }

        try {
            Document doc = Jsoup.parse(html);
            // Удаляем скрипты, стили, комментарии
            doc.select("script, style, noscript, comment").remove();

            String text = doc.text()
                    .toLowerCase()
                    .replaceAll("[^а-яёa-z\\s]", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            return text;
        } catch (Exception e) {
            // Fallback: простой regex если Jsoup не сработает
            return HTML_TAG_PATTERN.matcher(html)
                    .replaceAll(" ")
                    .toLowerCase()
                    .replaceAll("[^а-яёa-z\\s]", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
        }
    }

    /**
     * Извлекает слова из текста (без стоп-слов)
     */
    public String[] extractWords(String text) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }

        return Arrays.stream(text.split("\\s+"))
                .filter(word -> WORD_PATTERN.matcher(word).matches())
                .filter(word -> !STOP_WORDS.contains(word))
                .filter(word -> word.length() > 2) // Минимальная длина слова
                .toArray(String[]::new);
    }

    /**
     * Извлекает заголовок из HTML
     */
    public String extractTitle(String html) {
        if (html == null || html.isEmpty()) {
            return "Без названия";
        }

        try {
            Document doc = Jsoup.parse(html);
            String title = doc.title();
            return title != null && !title.isEmpty() ? title : "Без названия";
        } catch (Exception e) {
            return "Без названия";
        }
    }

    /**
     * Создает сниппет с выделением найденных слов
     */
    public String createSnippet(String content, Set<String> queryWords) {
        String cleanText = cleanHtml(content);

        if (cleanText.isEmpty()) {
            return "Текст не найден";
        }

        for (String word : queryWords) {
            int index = cleanText.indexOf(word);
            if (index != -1) {
                int start = Math.max(0, index - 100);
                int end = Math.min(cleanText.length(), index + 200);
                String snippet = cleanText.substring(start, end);

                for (String qWord : queryWords) {
                    snippet = snippet.replaceAll("(?i)(" + Pattern.quote(qWord) + ")", "<b>$1</b>");
                }

                return (start > 0 ? "..." : "") + snippet + (end < cleanText.length() ? "..." : "");
            }
        }

        int length = Math.min(300, cleanText.length());
        return cleanText.substring(0, length) + "...";
    }


    public boolean isStopWord(String word) {
        return STOP_WORDS.contains(word.toLowerCase());
    }


    public String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase()
                .replaceAll("[^а-яёa-z\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}