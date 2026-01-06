package searchengine.dto.statistics;

import lombok.Data;

/**
 * DTO для детализированной статистики по одному сайту.
 *
 * @author Кирилл Христич
 */
@Data
public class DetailedStatisticsItem {
    private String url;
    private String name;
    private String status;
    private long statusTime;
    private String error;
    private int pages;
    private int lemmas;
}
