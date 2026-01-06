package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Site.
 * Предоставляет методы для доступа к данным сайтов.
 *
 * @author Кирилл Христич
 */
@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
    /**
     * Находит сайт по URL.
     *
     * @param url URL сайта
     * @return Optional с найденным сайтом или empty
     */
    Optional<Site> findByUrl(String url);

    /**
     * Находит все сайты с указанным статусом.
     *
     * @param status статус индексации
     * @return список сайтов с указанным статусом
     */
    List<Site> findByStatus(SiteStatus status);
}
