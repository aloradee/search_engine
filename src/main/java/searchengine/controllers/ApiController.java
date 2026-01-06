package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

/**
 * Основной контроллер API поискового движка.
 * Предоставляет REST-эндпоинты для управления индексацией, поиска и получения статистики.
 *
 * @author Кирилл Христич
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    /**
     * Возвращает статистику поискового движка.
     * Включает общую статистику и детализированную по каждому сайту.
     *
     * @return ResponseEntity со статистикой в формате JSON
     */
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    /**
     * Запускает полную индексацию всех сайтов из конфигурации.
     * Если индексация уже запущена, возвращает ошибку.
     *
     * @return ResponseEntity с результатом операции
     */
    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        return indexingService.startIndexing();
    }

    /**
     * Останавливает текущую индексацию.
     * Если индексация не запущена, возвращает ошибку.
     *
     * @return ResponseEntity с результатом операции
     */
    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {
        return indexingService.stopIndexing();
    }

    /**
     * Индексирует или обновляет отдельную страницу по URL.
     * URL должен принадлежать одному из сайтов, указанных в конфигурации.
     *
     * @param url URL страницы для индексации
     * @return ResponseEntity с результатом операции
     */
    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestParam String url) {
        return indexingService.indexPage(url);
    }

    /**
     * Выполняет поиск по проиндексированным данным.
     * Поддерживает поиск по всем сайтам или конкретному сайту с пагинацией.
     *
     * @param query  поисковый запрос (обязательный)
     * @param site   URL сайта для ограничения поиска (опционально)
     * @param offset смещение для пагинации (по умолчанию 0)
     * @param limit  количество результатов на странице (по умолчанию 20)
     * @return ResponseEntity с результатами поиска
     * @throws IllegalArgumentException если поисковый запрос пустой
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam String query,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {

        if (query.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new SearchResponse(false, "Поисковый запрос не может быть пустым"));
        }

        return searchService.search(query, site, offset, limit);
    }
}
