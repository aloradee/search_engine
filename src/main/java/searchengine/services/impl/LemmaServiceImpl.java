package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.services.LemmaService;
import searchengine.util.LemmaFinder;
import searchengine.util.TextParser;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LemmaServiceImpl implements LemmaService {
    private final LemmaFinder lemmaFinder;
    private final TextParser textParser;

    @Override
    public Map<String, Integer> extractLemmas(String text) {
        String cleanText = textParser.cleanHtml(text);
        return lemmaFinder.getLemmaCount(cleanText);
    }

    @Override
    public String cleanHtml(String html) {
        return textParser.cleanHtml(html);
    }

    @Override
    public String getSnippet(String content, String query) {
        Set<String> queryLemmas = lemmaFinder.getQueryLemmas(query);
        return textParser.createSnippet(content, queryLemmas);
    }

    public Set<String> getQueryLemmas(String query) {
        return lemmaFinder.getQueryLemmas(query);
    }
}
