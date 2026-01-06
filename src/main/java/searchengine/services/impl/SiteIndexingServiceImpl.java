package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.concurrent.TimeUnit;

@Slf4j
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
    @Transactional
    public void indexSite(searchengine.config.Site siteConfig) {
        log.info("Начало индексации сайта: {}", siteConfig.getUrl());
        stopFlag = false;
        visitedUrls.clear();

        Optional<Site> existingSiteOpt = siteRepository.findByUrl(siteConfig.getUrl());
        Site site;

        if (existingSiteOpt.isPresent()) {
            site = existingSiteOpt.get();
            log.info("Очистка старых данных для сайта: {}", site.getUrl());

            clearSiteDataCascade(site);

            site.setStatus(SiteStatus.INDEXING);
            site.setLastError(null);
        } else {
            site = new Site();
            site.setUrl(siteConfig.getUrl());
            site.setName(siteConfig.getName());
            site.setStatus(SiteStatus.INDEXING);
        }

        site.setStatusTime(LocalDateTime.now());
        site = siteRepository.save(site);

        try {
            ForkJoinPool pool = new ForkJoinPool();
            pool.invoke(new PageIndexer(site, site.getUrl() + "/"));

            pool.shutdown();
            boolean terminated = pool.awaitTermination(30, TimeUnit.MINUTES);

            if (!terminated) {
                log.warn("Таймаут индексации сайта: {}", site.getUrl());
                pool.shutdownNow();
                site.setStatus(SiteStatus.FAILED);
                site.setLastError("Таймаут индексации");
            } else if (!stopFlag) {
                site.setStatus(SiteStatus.INDEXED);
                site.setLastError(null);
                log.info("Сайт {} успешно проиндексирован", site.getUrl());
            } else {
                site.setStatus(SiteStatus.FAILED);
                site.setLastError("Индексация остановлена пользователем");
                log.info("Индексация сайта {} остановлена пользователем", site.getUrl());
            }
        } catch (Exception e) {
            log.error("Ошибка при индексации сайта {}: {}", site.getUrl(), e.getMessage(), e);
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Ошибка индексации: " + e.getMessage());
        } finally {
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
            visitedUrls.clear();
            log.info("Завершение индексации сайта: {}, статус: {}", site.getUrl(), site.getStatus());
        }
    }

    @Transactional
    protected void clearSiteDataCascade(Site site) {
        log.debug("Каскадная очистка данных для сайта: {}", site.getId());
        List<Page> pages = pageRepository.findBySite(site);
        if (!pages.isEmpty()) {
            indexRepository.deleteAllByPages(pages);
        }
        lemmaRepository.deleteBySite(site);
        pageRepository.deleteBySite(site);
    }

    @Override
    public void stopIndexing() {
        log.info("Запрос на остановку индексации");
        stopFlag = true;
    }

    @Override
    @Transactional
    public void indexSinglePage(Site site, String pageUrl) {
        try {
            Thread.sleep(sitesList.getDelayBetweenRequests());

            Document doc = Jsoup.connect(pageUrl)
                    .userAgent(sitesList.getUserAgent())
                    .referrer(sitesList.getReferrer())
                    .timeout(30000)
                    .maxBodySize(50 * 1024 * 1024)
                    .get();

            int statusCode = doc.connection().response().statusCode();

            if (statusCode == 200) {
                String cleanText = lemmaService.cleanHtml(doc.html());
                Map<String, Integer> lemmasMap = lemmaService.extractLemmas(cleanText);

                String path = pageUrl.replace(site.getUrl(), "/");
                Optional<Page> existingPage = pageRepository.findByPathAndSite(path, site);

                if (existingPage.isPresent()) {
                    Page page = existingPage.get();
                    indexRepository.deleteByPage(page);
                    page.setContent(doc.html());
                    page.setCode(statusCode);
                    pageRepository.save(page);

                    saveLemmasAndIndices(page, lemmasMap);
                } else {
                    Page page = new Page();
                    page.setSite(site);
                    page.setPath(path);
                    page.setCode(statusCode);
                    page.setContent(doc.html());
                    page = pageRepository.save(page);

                    if (!lemmasMap.isEmpty()) {
                        saveLemmasAndIndices(page, lemmasMap);
                    }
                }

                log.info("Страница {} успешно проиндексирована", pageUrl);
            }

        } catch (Exception e) {
            log.error("Ошибка при индексации страницы {}: {}", pageUrl, e.getMessage(), e);
            throw new RuntimeException("Ошибка индексации страницы", e);
        }
    }

    @Transactional
    protected void saveLemmasAndIndices(Page page, Map<String, Integer> lemmasMap) {
        if (lemmasMap.isEmpty()) {
            return;
        }

        Site site = page.getSite();

        for (Map.Entry<String, Integer> entry : lemmasMap.entrySet()) {
            String lemmaStr = entry.getKey();
            int rank = entry.getValue();

            Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaStr, site)
                    .orElseGet(() -> {
                        Lemma newLemma = new Lemma();
                        newLemma.setSite(site);
                        newLemma.setLemma(lemmaStr);
                        newLemma.setFrequency(0);
                        return lemmaRepository.save(newLemma);
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

    private class PageIndexer extends RecursiveAction {
        private final Site siteEntity;
        private final String baseUrl;
        private final String url;

        public PageIndexer(Site siteEntity, String url) {
            this.siteEntity = siteEntity;
            this.baseUrl = siteEntity.getUrl();
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
                        .timeout(30000)
                        .maxBodySize(50 * 1024 * 1024)
                        .get();

                int statusCode = doc.connection().response().statusCode();

                if (statusCode == 200) {
                    savePageInTransaction(doc, statusCode);

                    log.debug("Проиндексирована страница: {}", url);
                } else {
                    log.warn("Страница {} вернула код ошибки: {}", url, statusCode);
                }

                updateSiteStatusTime();

                Elements links = doc.select("a[href]");
                List<PageIndexer> subtasks = new ArrayList<>();

                for (Element link : links) {
                    String absUrl = link.attr("abs:href");
                    if (isValidUrl(absUrl) && !visitedUrls.contains(absUrl)) {
                        subtasks.add(new PageIndexer(siteEntity, absUrl));
                    }
                }

                if (!subtasks.isEmpty()) {
                    invokeAll(subtasks);
                }

            } catch (IOException e) {
                log.error("Ошибка соединения для {}: {}", url, e.getMessage());
                saveFailedPage(siteEntity, url, "Connection error: " + e.getMessage());
            } catch (Exception e) {
                log.error("Неожиданная ошибка для {}: {}", url, e.getMessage(), e);
                saveFailedPage(siteEntity, url, "Unexpected error: " + e.getMessage());
            }
        }

        @Transactional
        protected void savePageInTransaction(Document doc, int statusCode) {
            if (stopFlag) return;

            String cleanText = lemmaService.cleanHtml(doc.html());
            Map<String, Integer> lemmasMap = lemmaService.extractLemmas(cleanText);

            Page page = new Page();
            page.setSite(siteEntity);
            page.setPath(url.replace(baseUrl, "/"));
            page.setCode(statusCode);
            page.setContent(doc.html());
            page = pageRepository.save(page);

            if (!lemmasMap.isEmpty()) {
                saveLemmasAndIndices(page, lemmasMap);
            }
        }

        @Transactional
        protected void updateSiteStatusTime() {
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);
        }

        private void saveLemmasAndIndices(Page page, Map<String, Integer> lemmasMap) {
            if (lemmasMap.isEmpty()) {
                return;
            }

            for (Map.Entry<String, Integer> entry : lemmasMap.entrySet()) {
                String lemmaStr = entry.getKey();
                int rank = entry.getValue();

                Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaStr, siteEntity)
                        .orElseGet(() -> {
                            Lemma newLemma = new Lemma();
                            newLemma.setSite(siteEntity);
                            newLemma.setLemma(lemmaStr);
                            newLemma.setFrequency(0);
                            return lemmaRepository.save(newLemma);
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

        private boolean isValidUrl(String url) {
            if (url == null || url.isEmpty()) {
                return false;
            }

            return url.startsWith(baseUrl) &&
                    !url.contains("#") &&
                    !url.matches(".*\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|zip|rar|7z|jpg|jpeg|png|gif|mp4|avi|mov)$") &&
                    !url.contains("?") &&
                    !url.endsWith(".xml") &&
                    !url.endsWith(".json");
        }

        @Transactional
        private void saveFailedPage(Site site, String url, String error) {
            Page page = new Page();
            page.setSite(site);
            page.setPath(url.replace(site.getUrl(), "/"));
            page.setCode(500);
            page.setContent("");
            pageRepository.save(page);
        }
    }
}
