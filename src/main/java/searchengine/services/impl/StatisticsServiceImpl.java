package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.*;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.StatisticsService;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса статистики.
 * Собирает статистику по всем сайтам и общую статистику системы.
 *
 * @author Кирилл Христич
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        log.debug("Получение статистики");

        TotalStatistics total = new TotalStatistics();

        List<Site> allSites = siteRepository.findAll();
        total.setSites(allSites.size());
        total.setPages(pageRepository.countAllPages());
        total.setLemmas(lemmaRepository.countAllLemmas());

        boolean isAnySiteIndexing = allSites.stream()
                .anyMatch(site -> site.getStatus() == SiteStatus.INDEXING);
        total.setIndexing(isAnySiteIndexing);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        for (Site site : allSites) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setUrl(site.getUrl());
            item.setName(site.getName());
            item.setStatus(site.getStatus().name());
            item.setStatusTime(site.getStatusTime().toEpochSecond(ZoneOffset.UTC));
            item.setError(site.getLastError());
            item.setPages(pageRepository.countBySite(site));
            item.setLemmas(lemmaRepository.countBySite(site));
            detailed.add(item);
        }

        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);

        StatisticsResponse response = new StatisticsResponse();
        response.setResult(true);
        response.setStatistics(data);

        log.debug("Статистика сформирована: {} сайтов, {} страниц, {} лемм, индексация: {}",
                total.getSites(), total.getPages(), total.getLemmas(), total.isIndexing());

        return response;
    }
}
