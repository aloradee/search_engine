package searchengine.services;

import java.util.Map;

public interface LemmaService {
    Map<String, Integer> extractLemmas(String text);
    String cleanHtml(String html);
    String getSnippet(String content, String query);
}