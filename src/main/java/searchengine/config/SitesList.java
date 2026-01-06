package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Конфигурационный класс для списка сайтов и настроек индексации.
 * Загружает настройки из application.yml с префиксом indexing-settings.
 *
 * @author Кирилл Христич
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {
    private List<Site> sites;
    private String userAgent;
    private String referrer;
    private int delayBetweenRequests;
}
