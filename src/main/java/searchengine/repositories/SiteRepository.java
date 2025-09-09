package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
    Optional<Site> findByUrl(String url);
    List<Site> findByStatus(SiteStatus status);
    boolean existsByUrl(String url);

    @Query("SELECT COUNT(s) FROM Site s")
    int countSites();

    @Query("SELECT COUNT(s) FROM Site s WHERE s.status = 'INDEXING'")
    int countIndexingSites();
}