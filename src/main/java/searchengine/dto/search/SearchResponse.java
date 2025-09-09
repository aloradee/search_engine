package searchengine.dto.search;

import lombok.Data;
import java.util.List;

@Data
public class SearchResponse {
    private boolean result;
    private String error;
    private int count; // перенесем count на верхний уровень
    private List<SearchItem> data; // прямой массив результатов

    public SearchResponse(boolean result, int count, List<SearchItem> data) {
        this.result = result;
        this.count = count;
        this.data = data;
    }

    public SearchResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}