package searchengine.dto.search;

import lombok.Data;
import java.util.List;

/**
 * DTO для ответа на поисковый запрос.
 * Содержит общее количество результатов и список найденных страниц.
 *
 * @author Кирилл Христич
 */
@Data
public class SearchResponse {
    private boolean result;
    private String error;
    private int count;
    private List<SearchItem> data;

    /**
     * Конструктор для успешного поиска.
     *
     * @param result true - поиск успешен
     * @param count  общее количество найденных страниц
     * @param data   список результатов
     */
    public SearchResponse(boolean result, int count, List<SearchItem> data) {
        this.result = result;
        this.count = count;
        this.data = data;
    }

    /**
     * Конструктор для поиска с ошибкой.
     *
     * @param result true - поиск успешен, false - произошла ошибка
     * @param error  сообщение об ошибке
     */
    public SearchResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
