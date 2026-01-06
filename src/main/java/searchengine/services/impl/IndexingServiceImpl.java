package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repositories.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.SiteIndexingService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Реализация сервиса управления индексацией.
 * Управляет запуском и остановкой процесса индексации всех сайтов.
 *
 * @author Кирилл Христич
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sitesList;
    private final SiteIndexingService siteIndexingService;
    private final SiteRepository siteRepository;

    private ExecutorService executorService;
    private Future<?> indexingFuture;
    private final AtomicBoolean isIndexing = new AtomicBoolean(false);

    @Override
    public synchronized ResponseEntity<IndexingResponse> startIndexing() {
        log.info("Получен запрос на запуск индексации");

        List<Site> indexingSites = siteRepository.findByStatus(SiteStatus.INDEXING);
        if (!indexingSites.isEmpty()) {
            log.warn("Найдены сайты в статусе INDEXING");
        }

        if (isIndexing.get()) {
            log.warn("Индексация уже запущена в другом потоке");
            return ResponseEntity.badRequest()
                    .body(new IndexingResponse(false, "Индексация уже запущена"));
        }

        isIndexing.set(true);
        executorService = Executors.newFixedThreadPool(Math.min(sitesList.getSites().size(), 10));

        indexingFuture = executorService.submit(() -> {
            try {
                log.info("Начало индексации всех сайтов");
                for (var siteConfig : sitesList.getSites()) {
                    if (!isIndexing.get()) {
                        log.info("Индексация остановлена, пропускаем сайт: {}", siteConfig.getUrl());
                        break;
                    }
                    log.info("Индексация сайта: {}", siteConfig.getUrl());
                    siteIndexingService.indexSite(siteConfig);
                }
                log.info("Индексация всех сайтов завершена");
            } catch (Exception e) {
                log.error("Критическая ошибка при индексации: {}", e.getMessage(), e);
            } finally {
                isIndexing.set(false);
                if (executorService != null && !executorService.isShutdown()) {
                    executorService.shutdown();
                }
                log.info("ExecutorService остановлен");
            }
        });

        return ResponseEntity.ok(new IndexingResponse(true));
    }

    @Override
    public synchronized ResponseEntity<IndexingResponse> stopIndexing() {
        log.info("Получен запрос на остановку индексации");

        boolean isCurrentlyIndexing = isIndexing.get();
        List<Site> indexingSites = siteRepository.findByStatus(SiteStatus.INDEXING);

        if (!isCurrentlyIndexing && indexingSites.isEmpty()) {
            log.warn("Попытка остановить индексацию, когда она не запущена");
            return ResponseEntity.badRequest()
                    .body(new IndexingResponse(false, "Индексация не запущена"));
        }

        siteIndexingService.stopIndexing();
        isIndexing.set(false);

        if (indexingFuture != null && !indexingFuture.isDone()) {
            indexingFuture.cancel(true);
            log.info("Future индексации отменен");
        }

        if (executorService != null && !executorService.isShutdown()) {
            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
                log.info("ExecutorService успешно остановлен");
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
                log.error("Ошибка при остановке ExecutorService", e);
            }
        }

        if (!indexingSites.isEmpty()) {
            for (Site site : indexingSites) {
                site.setStatus(SiteStatus.FAILED);
                site.setLastError("Индексация остановлена пользователем");
                site.setStatusTime(java.time.LocalDateTime.now());
                siteRepository.save(site);
                log.info("Статус сайта {} изменен на FAILED", site.getUrl());
            }
        }

        return ResponseEntity.ok(new IndexingResponse(true));
    }

    @Override
    public ResponseEntity<IndexingResponse> indexPage(String url) {
        log.info("Получен запрос на индексацию страницы: {}", url);

        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new IndexingResponse(false, "Не указан URL страницы"));
        }

        String normalizedUrl = normalizeUrl(url);

        searchengine.config.Site targetSiteConfig = null;
        for (searchengine.config.Site siteConfig : sitesList.getSites()) {
            String siteUrl = normalizeUrl(siteConfig.getUrl());
            if (normalizedUrl.startsWith(siteUrl)) {
                targetSiteConfig = siteConfig;
                break;
            }
        }

        if (targetSiteConfig == null) {
            return ResponseEntity.badRequest()
                    .body(new IndexingResponse(false, "Данная страница находится за пределами сайтов, указанных в конфигурации"));
        }

        try {
            searchengine.config.Site finalTargetSiteConfig = targetSiteConfig;
            Site site = siteRepository.findByUrl(targetSiteConfig.getUrl())
                    .orElseGet(() -> {
                        Site newSite = new Site(finalTargetSiteConfig.getUrl(), finalTargetSiteConfig.getName());
                        newSite.setStatus(SiteStatus.INDEXING);
                        return siteRepository.save(newSite);
                    });

            siteIndexingService.indexSinglePage(site, normalizedUrl);

            return ResponseEntity.ok(new IndexingResponse(true));

        } catch (Exception e) {
            log.error("Ошибка при индексации страницы {}: {}", url, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new IndexingResponse(false, "Ошибка при индексации страницы: " + e.getMessage()));
        }
    }

    @Override
    public boolean isIndexing() {
        return isIndexing.get();
    }

    private String normalizeUrl(String url) {
        if (url == null) return "";
        url = url.trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
