package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;

/**
 * Репозиторий для работы с сущностью Index.
 * Предоставляет методы для доступа к данным индексов.
 *
 * @author Кирилл Христич
 */
@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {
    /**
     * Находит все индексы для указанной леммы.
     * Использует JOIN FETCH для оптимизации запроса.
     *
     * @param lemma лемма
     * @return список индексов для леммы
     */
    @Query("SELECT i FROM Index i JOIN FETCH i.page JOIN FETCH i.lemma WHERE i.lemma = :lemma")
    List<Index> findByLemma(Lemma lemma);

    /**
     * Удаляет все индексы указанной страницы.
     *
     * @param page страница, индексы которой нужно удалить
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Index i WHERE i.page = :page")
    void deleteByPage(Page page);

    /**
     * Вычисляет релевантность страницы для указанного списка лемм.
     * Суммирует ранки всех индексов, связывающих страницу с леммами из списка.
     *
     * @param page   страница
     * @param lemmas список лемм
     * @return суммарный ранк (релевантность) или null, если связей нет
     */
    @Query("SELECT SUM(i.rank) FROM Index i WHERE i.page = :page AND i.lemma IN :lemmas")
    Float calculateRelevance(Page page, List<Lemma> lemmas);

    /**
     * Удаляет все индексы для списка страниц.
     *
     * @param pages список страниц
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Index i WHERE i.page IN :pages")
    void deleteAllByPages(@Param("pages") List<Page> pages);
}
