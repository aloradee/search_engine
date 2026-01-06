package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.indexing.IndexingResponse;

/**
 * Сервис управления индексацией.
 * Предоставляет методы для запуска, остановки и управления процессом индексации.
 *
 * @author Кирилл Христич
 */
public interface IndexingService {

    /**
     * Запускает полную индексацию всех сайтов из конфигурации.
     *
     * @return ResponseEntity с результатом операции
     */
    ResponseEntity<IndexingResponse> startIndexing();

    /**
     * Останавливает текущую индексацию.
     *
     * @return ResponseEntity с результатом операции
     */
    ResponseEntity<IndexingResponse> stopIndexing();

    /**
     * Индексирует или обновляет отдельную страницу.
     *
     * @param url URL страницы для индексации
     * @return ResponseEntity с результатом операции
     */
    ResponseEntity<IndexingResponse> indexPage(String url);

    /**
     * Проверяет, выполняется ли в данный момент индексация.
     *
     * @return true если индексация выполняется, false в противном случае
     */
    boolean isIndexing();
}
