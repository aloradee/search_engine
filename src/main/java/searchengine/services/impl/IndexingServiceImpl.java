package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.services.IndexingService;
import searchengine.services.SiteIndexingService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sitesList;
    private final SiteIndexingService siteIndexingService;
    private ExecutorService executorService;
    private Future<?> indexingFuture;
    private volatile boolean isIndexing = false;

    @Override
    public ResponseEntity<IndexingResponse> startIndexing() {
        if (isIndexing) {
            return ResponseEntity.badRequest()
                    .body(new IndexingResponse(false, "Индексация уже запущена"));
        }

        isIndexing = true;
        executorService = Executors.newFixedThreadPool(sitesList.getSites().size());

        indexingFuture = executorService.submit(() -> {
            try {
                for (var siteConfig : sitesList.getSites()) {
                    siteIndexingService.indexSite(siteConfig);
                }
            } finally {
                isIndexing = false;
                executorService.shutdown();
            }
        });

        return ResponseEntity.ok(new IndexingResponse(true));
    }

    @Override
    public ResponseEntity<IndexingResponse> stopIndexing() {
        if (!isIndexing) {
            return ResponseEntity.badRequest()
                    .body(new IndexingResponse(false, "Индексация не запущена"));
        }

        siteIndexingService.stopIndexing();
        if (indexingFuture != null) {
            indexingFuture.cancel(true);
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
        isIndexing = false;

        return ResponseEntity.ok(new IndexingResponse(true));
    }

    @Override
    public ResponseEntity<IndexingResponse> indexPage(String url) {
        // Реализация индексации отдельной страницы
        return ResponseEntity.ok(new IndexingResponse(true));
    }

    @Override
    public boolean isIndexing() {
        return isIndexing;
    }
}