package searchengine.dto.statistics;

import lombok.Data;

/**
 * DTO для ответа API статистики.
 *
 * @author Кирилл Христич
 */
@Data
public class StatisticsResponse {
    private boolean result;
    private StatisticsData statistics;
}
