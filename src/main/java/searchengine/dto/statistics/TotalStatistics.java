package searchengine.dto.statistics;

import lombok.Data;

/**
 * DTO для общей статистики поискового движка.
 *
 * @author Кирилл Христич
 */
@Data
public class TotalStatistics {
    private int sites;
    private int pages;
    private int lemmas;
    private boolean indexing;
}
