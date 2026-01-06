package searchengine.dto.search;

import lombok.Data;

/**
 * DTO для представления одного результата поиска.
 * Содержит всю информацию, необходимую для отображения в интерфейсе.
 *
 * @author Кирилл Христич
 */
@Data
public class SearchItem {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;
}
