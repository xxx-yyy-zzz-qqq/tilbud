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
    long countByChain(Chain chain);

    @Query(value = """
        SELECT *,
            ts_rank_cd(to_tsvector('simple', heading_normalized),
                       to_tsquery('simple', :query)) AS rank
        FROM offers
        WHERE to_tsvector('simple', heading_normalized) @@ to_tsquery('simple', :query)
        ORDER BY rank DESC, price ASC
        """, nativeQuery = true)
    List<Offer> search(String query);

    @Query(value = """
        SELECT *,
            ts_rank_cd(to_tsvector('simple', heading_normalized),
                       to_tsquery('simple', :query)) AS rank
        FROM offers
        WHERE to_tsvector('simple', heading_normalized) @@ to_tsquery('simple', :query)
        AND (:chainId IS NULL OR chain_id = :chainId)
        AND (:minPrice IS NULL OR price >= :minPrice)
        AND (:maxPrice IS NULL OR price <= :maxPrice)
        ORDER BY rank DESC, price ASC
        """, nativeQuery = true)
    List<Offer> searchWithFilters(String query, UUID chainId, Integer minPrice, Integer maxPrice);

    void deleteByCatalog(Catalog catalog);
}
