package searchengine.services;

import searchengine.config.Site;

/**
 * Сервис индексации отдельных сайтов.
 * Предоставляет методы для индексации сайтов и отдельных страниц.
 *
 * @author Кирилл Христич
 */
public interface SiteIndexingService {
    /**
     * Индексирует указанный сайт.
     * Выполняет обход всех страниц сайта и их индексацию.
     *
     * @param siteConfig конфигурация сайта
     */
    void indexSite(Site siteConfig);

    /**
     * Останавливает текущую индексацию.
     */
    void stopIndexing();

    /**
     * Индексирует отдельную страницу на указанном сайте.
     *
     * @param site    сайт
     * @param pageUrl полный URL страницы
     */
    void indexSinglePage(searchengine.model.Site site, String pageUrl);
}
