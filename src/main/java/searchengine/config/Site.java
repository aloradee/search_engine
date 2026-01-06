package searchengine.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Класс конфигурации для сайта.
 * Используется для загрузки настроек сайтов из application.yml.
 *
 * @author Кирилл Христич
 */
@Setter
@Getter
public class Site {
    private String url;
    private String name;
}
