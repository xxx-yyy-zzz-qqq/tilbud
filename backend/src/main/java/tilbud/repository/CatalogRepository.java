package tilbud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tilbud.entity.Catalog;
import tilbud.entity.Chain;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CatalogRepository extends JpaRepository<Catalog, UUID> {
    Optional<Catalog> findByCatalogId(String catalogId);
    List<Catalog> findByChain(Chain chain);
    List<Catalog> findByChainOrderByRunFromDesc(Chain chain);
    boolean existsByCatalogId(String catalogId);

    @Modifying
    @Query("DELETE FROM Catalog c WHERE c.chain = :chain AND c.catalogId NOT IN :ids")
    void deleteByChainAndCatalogIdNotIn(@Param("chain") Chain chain, @Param("ids") Set<String> ids);
}
