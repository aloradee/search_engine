package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность, представляющая сайт в поисковом движке.
 * Хранит информацию о сайте и его статусе индексации.
 *
 * @Entity Указывает, что класс является сущностью JPA
 * @Table Задает имя таблицы в базе данных
 *
 * @author Кирилл Христич
 */
@Entity
@Table(name = "site")
@Getter
@Setter
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')", nullable = false)
    private SiteStatus status;

    @Column(name = "status_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String url;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Page> pages = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Lemma> lemmas = new ArrayList<>();

    /**
     * Конструктор по умолчанию.
     * Инициализирует время статуса текущим временем.
     */
    public Site() {
        this.statusTime = LocalDateTime.now();
    }

    /**
     * Конструктор с параметрами.
     *
     * @param url  URL сайта
     * @param name название сайта
     */
    public Site(String url, String name) {
        this();
        this.url = url;
        this.name = name;
        this.status = SiteStatus.INDEXING;
    }
}
