package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    Optional<Page> findByPathAndSite(String path, Site site);
    List<Page> findBySite(Site site);
    boolean existsByPathAndSite(String path, Site site);

    @Query("SELECT COUNT(p) FROM Page p WHERE p.site = :site")
    int countBySite(Site site);

    @Query("SELECT COUNT(p) FROM Page p")
    int countAllPages();

    @Query("SELECT p FROM Page p WHERE p.code = 200")
    List<Page> findSuccessfulPages();
}