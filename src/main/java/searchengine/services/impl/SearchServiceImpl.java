package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<SearchResponse> search(String query, String siteUrl, int offset, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new SearchResponse(false, "Задан пустой поисковый запрос"));
        }

        try {
            String cleanQuery = lemmaService.cleanHtml(query);
            Map<String, Integer> queryLemmasMap = lemmaService.extractLemmas(cleanQuery);
            List<String> queryLemmas = new ArrayList<>(queryLemmasMap.keySet());

            if (queryLemmas.isEmpty()) {
                return ResponseEntity.ok(new SearchResponse(true, 0, Collections.emptyList()));
            }

            Optional<Site> siteOpt = siteUrl != null ?
                    siteRepository.findByUrl(siteUrl) : Optional.empty();

            List<Lemma> foundLemmas = siteOpt.isPresent() ?
                    lemmaRepository.findByLemmaInAndSite(queryLemmas, siteOpt.get()) :
                    lemmaRepository.findLemmasOrderByFrequency(queryLemmas);

            if (foundLemmas.isEmpty()) {
                return ResponseEntity.ok(new SearchResponse(true, 0, Collections.emptyList()));
            }

            List<Page> foundPages = findPagesWithAllLemmas(foundLemmas);

            if (foundPages.isEmpty()) {
                return ResponseEntity.ok(new SearchResponse(true, 0, Collections.emptyList()));
            }

            Map<Page, Float> relevanceMap = calculateRelevance(foundPages, foundLemmas);

            List<Page> sortedPages = foundPages.stream()
                    .sorted((p1, p2) -> Float.compare(
                            relevanceMap.getOrDefault(p2, 0f),
                            relevanceMap.getOrDefault(p1, 0f)))
                    .collect(Collectors.toList());

            List<SearchItem> searchItems = new ArrayList<>();
            for (int i = offset; i < Math.min(offset + limit, sortedPages.size()); i++) {
                Page page = sortedPages.get(i);
                SearchItem item = createSearchItem(page, query, relevanceMap.get(page));
                searchItems.add(item);
            }

            return ResponseEntity.ok(new SearchResponse(true, sortedPages.size(), searchItems));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new SearchResponse(false, "Ошибка при выполнении поиска: " + e.getMessage()));
        }
    }

    private List<Page> findPagesWithAllLemmas(List<Lemma> lemmas) {
        if (lemmas.isEmpty()) {
            return Collections.emptyList();
        }

        Lemma firstLemma = lemmas.get(0);
        List<Index> firstIndexes = indexRepository.findByLemma(firstLemma);
        Set<Page> candidatePages = firstIndexes.stream()
                .map(index -> index.getPage())
                .collect(Collectors.toSet());

        for (int i = 1; i < lemmas.size(); i++) {
            Lemma lemma = lemmas.get(i);
            List<Index> indexes = indexRepository.findByLemma(lemma);
            Set<Page> lemmaPages = indexes.stream()
                    .map(index -> index.getPage())
                    .collect(Collectors.toSet());

            candidatePages.retainAll(lemmaPages);

            if (candidatePages.isEmpty()) {
                break;
            }
        }

        return new ArrayList<>(candidatePages);
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
            for (Page page : relevanceMap.keySet()) {
                float normalized = relevanceMap.get(page) / maxRelevance;
                relevanceMap.put(page, normalized);
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
            return org.jsoup.Jsoup.parse(html).title();
        } catch (Exception e) {
            return "Без названия";
        }
    }
}