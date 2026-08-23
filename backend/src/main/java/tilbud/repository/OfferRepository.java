package tilbud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tilbud.entity.Catalog;
import tilbud.entity.Chain;
import tilbud.entity.Offer;
import java.util.List;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {
    List<Offer> findByCatalog(Catalog catalog);
    List<Offer> findByChain(Chain chain);
    long countByCatalog(Catalog catalog);

    @Query(value = "SELECT * FROM offers WHERE to_tsvector('simple', heading || ' ' || COALESCE(description, '')) @@ plainto_tsquery('simple', :query)", nativeQuery = true)
    List<Offer> search(String query);

    void deleteByCatalog(Catalog catalog);
}
