package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.search.SearchResponse;

public interface SearchService {
    ResponseEntity<SearchResponse> search(String query, String site, int offset, int limit);
}