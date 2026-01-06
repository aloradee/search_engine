package searchengine.dto.indexing;

import lombok.Data;

/**
 * DTO для ответов операций индексации.
 * Содержит результат операции и сообщение об ошибке (если есть).
 *
 * @author Кирилл Христич
 */
@Data
public class IndexingResponse {
    private boolean result;
    private String error;

    /**
     * Конструктор для успешной операции.
     *
     * @param result true - операция успешна
     */
    public IndexingResponse(boolean result) {
        this.result = result;
    }

    /**
     * Конструктор для операции с ошибкой.
     *
     * @param result true - операция успешна, false - произошла ошибка
     * @param error  сообщение об ошибке
     */
    public IndexingResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
