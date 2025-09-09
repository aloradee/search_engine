package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repositories.*;
import searchengine.services.LemmaService;
import searchengine.services.SiteIndexingService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

@Service
@RequiredArgsConstructor
public class SiteIndexingServiceImpl implements SiteIndexingService {
    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaService lemmaService;

    private volatile boolean stopFlag = false;
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();

    @Override
    public void indexSite(searchengine.config.Site siteConfig) {
        stopFlag = false;
        visitedUrls.clear();

        Optional<Site> existingSiteOpt = siteRepository.findByUrl(siteConfig.getUrl());
        Site site;

        if (existingSiteOpt.isPresent()) {
            site = existingSiteOpt.get();
            pageRepository.findBySite(site).forEach(page -> {
                indexRepository.deleteByPage(page);
                pageRepository.delete(page);
            });
            lemmaRepository.findBySite(site).forEach(lemmaRepository::delete);
        } else {
            site = new Site();
            site.setUrl(siteConfig.getUrl());
            site.setName(siteConfig.getName());
        }

        site.setStatus(SiteStatus.INDEXING);
        site.setLastError(null);
        site.setStatusTime(LocalDateTime.now());
        site = siteRepository.save(site);

        try {
            ForkJoinPool pool = new ForkJoinPool();
            pool.invoke(new PageIndexer(site, siteConfig.getUrl() + "/"));
            pool.shutdown();

            if (!stopFlag) {
                site.setStatus(SiteStatus.INDEXED);
            }
        } catch (Exception e) {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Ошибка индексации: " + e.getMessage());
        } finally {
            siteRepository.save(site);
        }
    }

    @Override
    public void stopIndexing() {
        stopFlag = true;
    }

    private class PageIndexer extends RecursiveAction {
        private final Site siteEntity;
        private final String baseUrl;
        private final String url;

        public PageIndexer(Site siteEntity, String url) {
            this.siteEntity = siteEntity;
            this.baseUrl = siteEntity.getUrl();
            this.url = url;
        }

        public PageIndexer(Site siteEntity, String baseUrl, String url) {
            this.siteEntity = siteEntity;
            this.baseUrl = baseUrl;
            this.url = url;
        }

        @Override
        protected void compute() {
            if (stopFlag || visitedUrls.contains(url)) {
                return;
            }

            visitedUrls.add(url);

            try {
                Thread.sleep(sitesList.getDelayBetweenRequests());

                Document doc = Jsoup.connect(url)
                        .userAgent(sitesList.getUserAgent())
                        .referrer(sitesList.getReferrer())
                        .timeout(10000)
                        .get();

                String cleanText = lemmaService.cleanHtml(doc.html());
                Map<String, Integer> lemmas = lemmaService.extractLemmas(cleanText);

                Page page = new Page();
                page.setSite(siteEntity);
                page.setPath(url.replace(baseUrl, "/"));
                page.setCode(doc.connection().response().statusCode());
                page.setContent(doc.html());
                page = pageRepository.save(page);

                saveLemmasAndIndices(page, lemmas);

                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);

                Elements links = doc.select("a[href]");
                List<PageIndexer> subtasks = new ArrayList<>();

                for (Element link : links) {
                    String absUrl = link.attr("abs:href");
                    if (isValidUrl(absUrl) && !visitedUrls.contains(absUrl)) {
                        subtasks.add(new PageIndexer(siteEntity, baseUrl, absUrl));
                    }
                }

                invokeAll(subtasks);

            } catch (IOException | InterruptedException e) {
                if (!stopFlag) {
                    siteEntity.setStatus(SiteStatus.FAILED);
                    siteEntity.setLastError("Ошибка при индексации страницы: " + url + ": " + e.getMessage());
                    siteRepository.save(siteEntity);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при обработке страницы " + url + ": " + e.getMessage());
            }
        }

        private boolean isValidUrl(String url) {
            return url != null &&
                    url.startsWith(baseUrl) &&
                    !url.contains("#") &&
                    !url.matches(".*\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|zip|rar|7z)$");
        }

        private void saveLemmasAndIndices(Page page, Map<String, Integer> lemmasMap) {
            for (Map.Entry<String, Integer> entry : lemmasMap.entrySet()) {
                String lemmaStr = entry.getKey();
                int rank = entry.getValue();

                Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaStr, page.getSite())
                        .orElseGet(() -> {
                            Lemma newLemma = new Lemma();
                            newLemma.setSite(page.getSite());
                            newLemma.setLemma(lemmaStr);
                            newLemma.setFrequency(0);
                            return newLemma;
                        });

                lemma.setFrequency(lemma.getFrequency() + 1);
                lemma = lemmaRepository.save(lemma);

                Index index = new Index();
                index.setPage(page);
                index.setLemma(lemma);
                index.setRank(rank);
                indexRepository.save(index);
            }
        }
    }
}