package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Lemma.
 * Предоставляет методы для доступа к данным лемм.
 *
 * @author Кирилл Христич
 */
@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    /**
     * Находит лемму по тексту и сайту.
     *
     * @param lemma текст леммы
     * @param site  сайт
     * @return Optional с найденной леммой или empty
     */
    Optional<Lemma> findByLemmaAndSite(String lemma, Site site);

    /**
     * Находит список лемм по списку текстов и сайту.
     *
     * @param lemmas список текстов лемм
     * @param site   сайт
     * @return список найденных лемм
     */
    List<Lemma> findByLemmaInAndSite(List<String> lemmas, Site site);

    /**
     * Подсчитывает количество лемм на указанном сайте.
     *
     * @param site сайт
     * @return количество лемм на сайте
     */
    @Query("SELECT COUNT(l) FROM Lemma l WHERE l.site = :site")
    int countBySite(Site site);

    /**
     * Подсчитывает общее количество лемм во всех сайтах.
     *
     * @return общее количество лемм
     */
    @Query("SELECT COUNT(l) FROM Lemma l")
    int countAllLemmas();

    /**
     * Удаляет все леммы указанного сайта.
     *
     * @param site сайт, леммы которого нужно удалить
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Lemma l WHERE l.site = :site")
    void deleteBySite(@Param("site") Site site);

    /**
     * Находит все леммы с указанным текстом.
     *
     * @param lemmaStr текст леммы
     * @return список лемм с указанным текстом
     */
    @Query("SELECT l FROM Lemma l WHERE l.lemma = :lemmaStr")
    List<Lemma> findByLemma(@Param("lemmaStr") String lemmaStr);
}
