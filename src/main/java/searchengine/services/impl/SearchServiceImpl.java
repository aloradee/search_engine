package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.search.SearchItem;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.LemmaService;
import searchengine.services.SearchService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса поиска.
 * Выполняет поиск по проиндексированным данным с учетом морфологии.
 *
 * @author Кирилл Христич
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;

    private static final float MAX_LEMMA_FREQUENCY_PERCENT = 0.8f;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<SearchResponse> search(String query, String siteUrl, int offset, int limit) {
        log.info("Поисковый запрос: '{}', сайт: {}, offset: {}, limit: {}", query, siteUrl, offset, limit);

        if (siteUrl != null) {
            siteUrl = normalizeUrl(siteUrl);
        }

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new SearchResponse(false, "Задан пустой поисковый запрос"));
        }

        try {
            String cleanQuery = lemmaService.cleanHtml(query);
            Set<String> queryLemmasSet = lemmaService.getQueryLemmas(cleanQuery);
            List<String> queryLemmas = new ArrayList<>(queryLemmasSet);

            if (queryLemmas.isEmpty()) {
                return ResponseEntity.ok(new SearchResponse(true, 0, Collections.emptyList()));
            }

            Optional<Site> siteOpt = siteUrl != null ?
                    siteRepository.findByUrl(siteUrl) : Optional.empty();

            List<Lemma> foundLemmas = siteOpt.isPresent() ?
                    lemmaRepository.findByLemmaInAndSite(queryLemmas, siteOpt.get()) :
                    getFilteredLemmas(queryLemmas, siteOpt.orElse(null));

            if (foundLemmas.isEmpty()) {
                return ResponseEntity.ok(new SearchResponse(true, 0, Collections.emptyList()));
            }

            foundLemmas.sort(Comparator.comparingInt(Lemma::getFrequency));

            List<Page> foundPages = findPagesWithAllLemmas(foundLemmas);

            if (foundPages.isEmpty()) {
                return ResponseEntity.ok(new SearchResponse(true, 0, Collections.emptyList()));
            }

            Map<Page, Float> relevanceMap = calculateRelevance(foundPages, foundLemmas);

            List<Page> sortedPages = foundPages.stream()
                    .sorted((p1, p2) -> Float.compare(
                            relevanceMap.getOrDefault(p2, 0f),
                            relevanceMap.getOrDefault(p1, 0f)))
                    .toList();

            List<SearchItem> searchItems = new ArrayList<>();
            int endIndex = Math.min(offset + limit, sortedPages.size());

            for (int i = offset; i < endIndex; i++) {
                Page page = sortedPages.get(i);
                SearchItem item = createSearchItem(page, query, relevanceMap.get(page));
                searchItems.add(item);
            }

            log.info("Найдено {} страниц, возвращается {} результатов", sortedPages.size(), searchItems.size());
            return ResponseEntity.ok(new SearchResponse(true, sortedPages.size(), searchItems));

        } catch (Exception e) {
            log.error("Ошибка при выполнении поиска: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new SearchResponse(false, "Ошибка при выполнении поиска: " + e.getMessage()));
        }
    }

    private List<Lemma> getFilteredLemmas(List<String> queryLemmas, Site site) {
        List<Lemma> allFoundLemmas;

        if (site != null) {
            allFoundLemmas = lemmaRepository.findByLemmaInAndSite(queryLemmas, site);
        } else {
            allFoundLemmas = new ArrayList<>();
            for (String lemmaStr : queryLemmas) {
                allFoundLemmas.addAll(lemmaRepository.findByLemma(lemmaStr));
            }
        }

        if (allFoundLemmas.isEmpty()) {
            return Collections.emptyList();
        }

        return allFoundLemmas.stream()
                .filter(lemma -> {
                    int totalPagesForSite = pageRepository.countBySite(lemma.getSite());
                    if (totalPagesForSite == 0) return true;
                    float frequencyPercent = (float) lemma.getFrequency() / totalPagesForSite;
                    return frequencyPercent <= MAX_LEMMA_FREQUENCY_PERCENT;
                })
                .sorted(Comparator.comparingInt(Lemma::getFrequency))
                .collect(Collectors.toList());
    }

    private List<Page> findPagesWithAllLemmas(List<Lemma> lemmas) {
        if (lemmas.isEmpty()) {
            return Collections.emptyList();
        }

        Lemma firstLemma = lemmas.get(0);
        List<Index> firstIndexes = indexRepository.findByLemma(firstLemma);
        Set<Integer> candidatePageIds = firstIndexes.stream()
                .map(index -> index.getPage().getId())
                .collect(Collectors.toSet());

        for (int i = 1; i < lemmas.size() && !candidatePageIds.isEmpty(); i++) {
            Lemma lemma = lemmas.get(i);
            List<Index> indexes = indexRepository.findByLemma(lemma);
            Set<Integer> lemmaPageIds = indexes.stream()
                    .map(index -> index.getPage().getId())
                    .collect(Collectors.toSet());

            candidatePageIds.retainAll(lemmaPageIds);
        }

        if (candidatePageIds.isEmpty()) {
            return Collections.emptyList();
        }

        return pageRepository.findAllById(candidatePageIds);
    }

    private Map<Page, Float> calculateRelevance(List<Page> pages, List<Lemma> lemmas) {
        Map<Page, Float> relevanceMap = new HashMap<>();
        float maxRelevance = 0f;

        for (Page page : pages) {
            Float relevance = indexRepository.calculateRelevance(page, lemmas);
            if (relevance != null) {
                relevanceMap.put(page, relevance);
                maxRelevance = Math.max(maxRelevance, relevance);
            }
        }

        if (maxRelevance > 0) {
            for (Map.Entry<Page, Float> entry : relevanceMap.entrySet()) {
                float normalized = entry.getValue() / maxRelevance;
                relevanceMap.put(entry.getKey(), normalized);
            }
        }

        return relevanceMap;
    }

    private SearchItem createSearchItem(Page page, String query, float relevance) {
        SearchItem item = new SearchItem();
        item.setSite(page.getSite().getUrl());
        item.setSiteName(page.getSite().getName());
        item.setUri(page.getPath());
        item.setTitle(extractTitle(page.getContent()));
        item.setSnippet(lemmaService.getSnippet(page.getContent(), query));
        item.setRelevance(relevance);
        return item;
    }

    private String extractTitle(String html) {
        try {
            String title = org.jsoup.Jsoup.parse(html).title();
            return title != null && !title.isEmpty() ? title : "Без названия";
        } catch (Exception e) {
            return "Без названия";
        }
    }

    private String normalizeUrl(String url) {
        if (url == null) return null;
        url = url.trim();
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }
}
