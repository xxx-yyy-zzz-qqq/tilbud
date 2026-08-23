package tilbud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tilbud.entity.Catalog;
import tilbud.entity.Chain;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogRepository extends JpaRepository<Catalog, UUID> {
    Optional<Catalog> findByCatalogId(String catalogId);
    List<Catalog> findByChain(Chain chain);
    List<Catalog> findByChainOrderByRunFromDesc(Chain chain);
    boolean existsByCatalogId(String catalogId);
}
