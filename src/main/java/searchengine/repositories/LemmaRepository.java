package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Optional<Lemma> findByLemmaAndSite(String lemma, Site site);
    List<Lemma> findBySite(Site site);
    List<Lemma> findByLemmaInAndSite(List<String> lemmas, Site site);

    @Query("SELECT COUNT(l) FROM Lemma l WHERE l.site = :site")
    int countBySite(Site site);

    @Query("SELECT COUNT(l) FROM Lemma l")
    int countAllLemmas();

    @Query("SELECT l FROM Lemma l WHERE l.lemma IN :lemmas ORDER BY l.frequency ASC")
    List<Lemma> findLemmasOrderByFrequency(List<String> lemmas);
}