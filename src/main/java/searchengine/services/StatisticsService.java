package searchengine.services;

import searchengine.dto.statistics.StatisticsResponse;

/**
 * Сервис получения статистики поискового движка.
 *
 * @author Кирилл Христич
 */
public interface StatisticsService {

    /**
     * Получает полную статистику поискового движка.
     * Включает общую статистику и детализированную по каждому сайту.
     *
     * @return объект StatisticsResponse со статистикой
     */
    StatisticsResponse getStatistics();
}
