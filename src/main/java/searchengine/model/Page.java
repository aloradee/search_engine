package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность, представляющая веб-страницу.
 * Хранит содержимое страницы и связи с сайтом и леммами.
 *
 * @author Кирилл Христич
 */
@Entity
@Table(name = "page", indexes = {
        @jakarta.persistence.Index(name = "path_index", columnList = "path")
})
@Getter
@Setter
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "fk_page_site"))
    private Site site;

    @Column(columnDefinition = "VARCHAR(500)", nullable = false)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Index> indexes = new ArrayList<>();
}
