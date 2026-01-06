package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.search.SearchResponse;

/**
 * Сервис поиска по проиндексированным данным.
 * Предоставляет методы для выполнения поисковых запросов.
 *
 * @author Кирилл Христич
 */
public interface SearchService {

    /**
     * Выполняет поиск по указанному запросу.
     * Поддерживает поиск по всем сайтам или конкретному сайту.
     *
     * @param query  поисковый запрос
     * @param site   URL сайта для ограничения поиска (null для поиска по всем сайтам)
     * @param offset смещение для пагинации
     * @param limit  количество результатов на странице
     * @return ResponseEntity с результатами поиска
     */
    ResponseEntity<SearchResponse> search(String query, String site, int offset, int limit);
}
