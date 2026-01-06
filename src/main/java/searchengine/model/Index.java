package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Сущность, представляющая связь между страницей и леммой.
 * Хранит информацию о вхождении леммы на страницу с ранком.
 *
 * @author Кирилл Христич
 */
@Entity
@Table(name = "search_index")
@Getter
@Setter
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false, foreignKey = @ForeignKey(name = "fk_index_page"))
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", nullable = false, foreignKey = @ForeignKey(name = "fk_index_lemma"))
    private Lemma lemma;

    @Column(name = "`rank`", nullable = false)
    private float rank;
}
