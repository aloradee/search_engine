package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.*;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.StatisticsService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        total.setSites(siteRepository.countSites());
        total.setPages(pageRepository.countAllPages());
        total.setLemmas(lemmaRepository.countAllLemmas());
        total.setIndexing(siteRepository.countIndexingSites() > 0);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sites = siteRepository.findAll();

        for (Site site : sites) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setUrl(site.getUrl());
            item.setName(site.getName());
            item.setStatus(site.getStatus().name());
            item.setStatusTime(site.getStatusTime().toEpochSecond(java.time.ZoneOffset.UTC));
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

        return response;
    }
}