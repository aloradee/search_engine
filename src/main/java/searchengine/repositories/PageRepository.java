package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Page.
 * Предоставляет методы для доступа к данным страниц.
 *
 * @author Кирилл Христич
 */
@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    /**
     * Находит страницу по пути и сайту.
     *
     * @param path путь к странице
     * @param site сайт, к которому принадлежит страница
     * @return Optional с найденной страницей или empty
     */
    Optional<Page> findByPathAndSite(String path, Site site);

    /**
     * Находит все страницы указанного сайта.
     *
     * @param site сайт
     * @return список страниц сайта
     */
    List<Page> findBySite(Site site);

    /**
     * Подсчитывает количество страниц на указанном сайте.
     *
     * @param site сайт
     * @return количество страниц на сайте
     */
    @Query("SELECT COUNT(p) FROM Page p WHERE p.site = :site")
    int countBySite(Site site);

    /**
     * Подсчитывает общее количество страниц во всех сайтах.
     *
     * @return общее количество страниц
     */
    @Query("SELECT COUNT(p) FROM Page p")
    int countAllPages();

    /**
     * Удаляет все страницы указанного сайта.
     *
     * @param site сайт, страницы которого нужно удалить
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Page p WHERE p.site = :site")
    void deleteBySite(@Param("site") Site site);
}
