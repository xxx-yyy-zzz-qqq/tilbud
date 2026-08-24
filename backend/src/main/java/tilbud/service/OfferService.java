package tilbud.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tilbud.dto.OfferSearchQuery;
import tilbud.entity.Catalog;
import tilbud.entity.Chain;
import tilbud.entity.Offer;
import tilbud.repository.CatalogRepository;
import tilbud.repository.ChainRepository;
import tilbud.repository.OfferRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final ChainRepository chainRepository;
    private final CatalogRepository catalogRepository;

    public OfferService(OfferRepository offerRepository,
                        ChainRepository chainRepository,
                        CatalogRepository catalogRepository) {
        this.offerRepository = offerRepository;
        this.chainRepository = chainRepository;
        this.catalogRepository = catalogRepository;
    }

    public Page<Offer> search(OfferSearchQuery query) {
        // Validate inputs
        if (query.getSort() != null) {
            switch (query.getSort()) {
                case "price_asc", "price_desc", "date_asc", "date_desc", "relevance" -> {}
                default -> throw new IllegalArgumentException("Invalid sort value: " + query.getSort());
            }
        }
        if (query.getMinPrice() != null && query.getMinPrice() < 0) {
            throw new IllegalArgumentException("min_price must be >= 0");
        }
        if (query.getMaxPrice() != null && query.getMaxPrice() < 0) {
            throw new IllegalArgumentException("max_price must be >= 0");
        }
        if (query.getPage() != null && query.getPage() < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (query.getSize() != null && (query.getSize() < 1 || query.getSize() > 100)) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }

        List<Offer> allOffers = offerRepository.findAll();
        List<Offer> filtered = new ArrayList<>();

        for (Offer offer : allOffers) {
            if (matchesQuery(offer, query)) {
                filtered.add(offer);
            }
        }

        // Sort
        if (query.getSort() != null) {
            filtered.sort((a, b) -> switch (query.getSort()) {
                case "price_asc" -> Integer.compare(a.getPrice(), b.getPrice());
                case "price_desc" -> Integer.compare(b.getPrice(), a.getPrice());
                case "date_asc" -> compareNullable(a.getRunFrom(), b.getRunFrom());
                case "date_desc" -> compareNullable(b.getRunFrom(), a.getRunFrom());
                case "relevance" -> compareNullable(b.getCreatedAt(), a.getCreatedAt());
                default -> throw new IllegalArgumentException("Invalid sort value: " + query.getSort());
            });
        } else if (query.getQ() != null && !query.getQ().isBlank()) {
            filtered.sort((a, b) -> compareNullable(b.getCreatedAt(), a.getCreatedAt()));
        } else {
            filtered.sort((a, b) -> compareNullable(b.getCreatedAt(), a.getCreatedAt()));
        }

        // Paginate
        int page = query.getPage() != null ? query.getPage() : 0;
        int size = query.getSize() != null ? query.getSize() : 20;
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Offer> pageContent = start < filtered.size() ? filtered.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    private boolean matchesQuery(Offer offer, OfferSearchQuery query) {
        if (query.getQ() != null && !query.getQ().isBlank()) {
            String searchLower = query.getQ().toLowerCase();
            boolean headingMatch = offer.getHeading() != null && offer.getHeading().toLowerCase().contains(searchLower);
            boolean descMatch = offer.getDescription() != null && offer.getDescription().toLowerCase().contains(searchLower);
            if (!headingMatch && !descMatch) return false;
        }

        if (query.getChain() != null && !query.getChain().isEmpty()) {
            boolean chainMatch = query.getChain().contains(offer.getChain().getDealerId());
            if (!chainMatch) return false;
        }

        if (query.getCategory() != null && !query.getCategory().isBlank()) {
            Catalog catalog = offer.getCatalog();
            if (catalog.getCategoryIds() == null || !catalog.getCategoryIds().contains(query.getCategory())) {
                return false;
            }
        }

        if (query.getMinPrice() != null && offer.getPrice() < query.getMinPrice()) {
            return false;
        }

        if (query.getMaxPrice() != null && offer.getPrice() > query.getMaxPrice()) {
            return false;
        }

        if (query.getValidFrom() != null) {
            Instant from = query.getValidFrom().atStartOfDay().toInstant(ZoneOffset.UTC);
            if (offer.getRunFrom() == null || offer.getRunFrom().isBefore(from)) {
                return false;
            }
        }

        if (query.getValidTo() != null) {
            Instant to = query.getValidTo().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            if (offer.getRunTill() == null || !offer.getRunTill().isBefore(to)) {
                return false;
            }
        }

        if (query.getCatalogId() != null && !query.getCatalogId().isBlank()) {
            Optional<Catalog> catalog = catalogRepository.findByCatalogId(query.getCatalogId());
            if (catalog.isEmpty() || !catalog.get().getId().equals(offer.getCatalog().getId())) {
                return false;
            }
        }

        return true;
    }

    private int compareNullable(Instant a, Instant b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }

    public Optional<Offer> findById(UUID id) {
        return offerRepository.findById(id);
    }
}
