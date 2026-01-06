package searchengine.dto.statistics;

import lombok.Data;

import java.util.List;

/**
 * DTO для полной статистики поискового движка.
 * Содержит общую статистику и список детализированной статистики по сайтам.
 *
 * @author Кирилл Христич
 */
@Data
public class StatisticsData {
    private TotalStatistics total;
    private List<DetailedStatisticsItem> detailed;
}
