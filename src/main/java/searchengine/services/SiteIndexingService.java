package searchengine.services;

import searchengine.config.Site;

public interface SiteIndexingService {
    void indexSite(Site siteConfig);
    void stopIndexing();
}