package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {
    @Query("SELECT i FROM Index i JOIN FETCH i.page JOIN FETCH i.lemma WHERE i.lemma = :lemma")
    List<Index> findByLemma(Lemma lemma);
    List<Index> findByPage(Page page);
    void deleteByPage(Page page);
    void deleteByLemma(Lemma lemma);

    @Query("SELECT i FROM Index i WHERE i.lemma IN :lemmas")
    List<Index> findByLemmas(List<Lemma> lemmas);

    @Query("SELECT SUM(i.rank) FROM Index i WHERE i.page = :page AND i.lemma IN :lemmas")
    Float calculateRelevance(Page page, List<Lemma> lemmas);
}