package tilbud.service;

import org.springframework.stereotype.Service;
import tilbud.entity.Catalog;
import tilbud.entity.Chain;
import tilbud.repository.CatalogRepository;
import tilbud.repository.ChainRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final ChainRepository chainRepository;

    public CatalogService(CatalogRepository catalogRepository, ChainRepository chainRepository) {
        this.catalogRepository = catalogRepository;
        this.chainRepository = chainRepository;
    }

    public List<Catalog> search(String chain, LocalDate validFrom, LocalDate validTo) {
        List<Catalog> catalogs;

        if (chain != null && !chain.isBlank()) {
            Chain chainEntity = chainRepository.findByDealerId(chain).orElse(null);
            if (chainEntity == null) {
                return List.of();
            }
            catalogs = catalogRepository.findByChainOrderByRunFromDesc(chainEntity);
        } else {
            catalogs = catalogRepository.findAll();
        }

        if (validFrom != null) {
            Instant from = validFrom.atStartOfDay().toInstant(ZoneOffset.UTC);
            catalogs = catalogs.stream()
                .filter(c -> c.getRunFrom() != null && !c.getRunFrom().isBefore(from))
                .toList();
        }

        if (validTo != null) {
            Instant to = validTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            catalogs = catalogs.stream()
                .filter(c -> c.getRunTill() != null && c.getRunTill().isBefore(to))
                .toList();
        }

        return catalogs;
    }
}
