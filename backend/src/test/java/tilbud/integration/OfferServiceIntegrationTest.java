package tilbud.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import tilbud.dto.OfferSearchQuery;
import tilbud.entity.Catalog;
import tilbud.entity.Chain;
import tilbud.entity.Offer;
import tilbud.repository.CatalogRepository;
import tilbud.repository.ChainRepository;
import tilbud.repository.OfferRepository;
import tilbud.service.OfferService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OfferServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OfferService offerService;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private ChainRepository chainRepository;

    @Autowired
    private CatalogRepository catalogRepository;

    private Chain chain;
    private Catalog catalog;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        chain = chainRepository.save(new Chain("net01", "Netto"));
        catalog = catalogRepository.save(new Catalog("cat0001", chain, "Weekly Deals"));
    }

    private Offer createOffer(String offerId, String heading, String headingNormalized, int price) {
        Offer offer = new Offer(offerId, chain, catalog, heading, price);
        offer.setHeadingNormalized(headingNormalized);
        offer.setRunFrom(Instant.parse("2026-01-01T00:00:00Z"));
        offer.setRunTill(Instant.parse("2026-01-07T23:59:59Z"));
        return offerRepository.save(offer);
    }

    @Test
    void searchWithTextQueryReturnsMatchingOffers() {
        createOffer("off001", "Organic Milk", "organic milk", 2500);
        createOffer("off002", "Sourdough Bread", "sourdough bread", 3000);

        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");

        Page<Offer> results = offerService.search(query);

        assertEquals(1, results.getTotalElements());
        assertEquals("off001", results.getContent().getFirst().getOfferId());
    }

    @Test
    void searchWithNoQueryReturnsAllOffers() {
        createOffer("off001", "Organic Milk", "organic milk", 2500);
        createOffer("off002", "Sourdough Bread", "sourdough bread", 3000);

        OfferSearchQuery query = new OfferSearchQuery();

        Page<Offer> results = offerService.search(query);

        assertEquals(2, results.getTotalElements());
    }

    @Test
    void searchWithChainFilterReturnsOnlyMatchingChain() {
        Chain otherChain = chainRepository.save(new Chain("fak01", "Fakta"));
        Catalog otherCatalog = catalogRepository.save(new Catalog("cat0002", otherChain, "Fakta Deals"));

        createOffer("off001", "Organic Milk", "organic milk", 2500);
        Offer otherOffer = new Offer("off002", otherChain, otherCatalog, "Cheap Milk", 1500);
        otherOffer.setHeadingNormalized("cheap milk");
        offerRepository.save(otherOffer);

        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setChain(List.of("net01"));

        Page<Offer> results = offerService.search(query);

        assertEquals(1, results.getTotalElements());
        assertEquals("off001", results.getContent().getFirst().getOfferId());
    }

    @Test
    void searchWithPriceFilterReturnsMatchingOffers() {
        createOffer("off001", "Cheap Milk", "cheap milk", 1500);
        createOffer("off002", "Premium Milk", "premium milk", 5000);

        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setMinPrice(2000);

        Page<Offer> results = offerService.search(query);

        assertEquals(1, results.getTotalElements());
        assertEquals(5000, results.getContent().getFirst().getPrice());
    }

    @Test
    void searchWithMaxPriceFilter() {
        createOffer("off001", "Cheap Milk", "cheap milk", 1500);
        createOffer("off002", "Premium Milk", "premium milk", 5000);

        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setMaxPrice(3000);

        Page<Offer> results = offerService.search(query);

        assertEquals(1, results.getTotalElements());
        assertEquals(1500, results.getContent().getFirst().getPrice());
    }

    @Test
    void searchWithPagination() {
        for (int i = 1; i <= 25; i++) {
            createOffer("off" + String.format("%03d", i), "Milk " + i, "milk " + i, 100 * i);
        }

        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setPage(0);
        query.setSize(10);

        Page<Offer> firstPage = offerService.search(query);

        assertEquals(25, firstPage.getTotalElements());
        assertEquals(3, firstPage.getTotalPages());
        assertEquals(10, firstPage.getContent().size());

        OfferSearchQuery secondPageQuery = new OfferSearchQuery();
        secondPageQuery.setQ("milk");
        secondPageQuery.setPage(1);
        secondPageQuery.setSize(10);

        Page<Offer> secondPage = offerService.search(secondPageQuery);

        assertEquals(10, secondPage.getContent().size());
    }

    @Test
    void searchSortByPriceAsc() {
        createOffer("off001", "Expensive Milk", "expensive milk", 5000);
        createOffer("off002", "Cheap Milk", "cheap milk", 1500);

        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setSort("price_asc");

        Page<Offer> results = offerService.search(query);

        assertEquals(1500, results.getContent().get(0).getPrice());
        assertEquals(5000, results.getContent().get(1).getPrice());
    }

    @Test
    void searchSortByPriceDesc() {
        createOffer("off001", "Expensive Milk", "expensive milk", 5000);
        createOffer("off002", "Cheap Milk", "cheap milk", 1500);

        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setSort("price_desc");

        Page<Offer> results = offerService.search(query);

        assertEquals(5000, results.getContent().get(0).getPrice());
        assertEquals(1500, results.getContent().get(1).getPrice());
    }

    @Test
    void searchRejectsInvalidSort() {
        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setSort("invalid_sort");

        assertThrows(IllegalArgumentException.class, () -> offerService.search(query));
    }

    @Test
    void searchRejectsNegativeMinPrice() {
        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setMinPrice(-1);

        assertThrows(IllegalArgumentException.class, () -> offerService.search(query));
    }

    @Test
    void searchRejectsNegativeMaxPrice() {
        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setMaxPrice(-1);

        assertThrows(IllegalArgumentException.class, () -> offerService.search(query));
    }

    @Test
    void searchRejectsNegativePage() {
        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setPage(-1);

        assertThrows(IllegalArgumentException.class, () -> offerService.search(query));
    }

    @Test
    void searchRejectsSizeZero() {
        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setSize(0);

        assertThrows(IllegalArgumentException.class, () -> offerService.search(query));
    }

    @Test
    void searchRejectsSizeOver100() {
        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setSize(101);

        assertThrows(IllegalArgumentException.class, () -> offerService.search(query));
    }

    @Test
    void searchNoQueryWithDateFilter() {
        Offer offer1 = createOffer("off001", "Milk Jan", "milk jan", 2500);
        offer1.setRunFrom(Instant.parse("2026-01-10T00:00:00Z"));
        offer1.setRunTill(Instant.parse("2026-01-15T23:59:59Z"));
        offerRepository.save(offer1);

        Offer offer2 = createOffer("off002", "Milk Feb", "milk feb", 3000);
        offer2.setRunFrom(Instant.parse("2026-02-01T00:00:00Z"));
        offer2.setRunTill(Instant.parse("2026-02-15T23:59:59Z"));
        offerRepository.save(offer2);

        OfferSearchQuery query = new OfferSearchQuery();
        query.setValidFrom(LocalDate.of(2026, 1, 10));
        query.setValidTo(LocalDate.of(2026, 1, 20));

        Page<Offer> results = offerService.search(query);

        assertEquals(1, results.getTotalElements());
        assertEquals("off001", results.getContent().getFirst().getOfferId());
    }

    @Test
    void findByIdReturnsOfferWhenExists() {
        Offer offer = createOffer("off001", "Organic Milk", "organic milk", 2500);

        assertTrue(offerService.findById(offer.getId()).isPresent());
        assertEquals("off001", offerService.findById(offer.getId()).get().getOfferId());
    }

    @Test
    void findByIdReturnsEmptyWhenNotExists() {
        assertTrue(offerService.findById(java.util.UUID.randomUUID()).isEmpty());
    }

    @Test
    void searchWithMultipleChainFilters() {
        Chain otherChain = chainRepository.save(new Chain("fak01", "Fakta"));
        Catalog otherCatalog = catalogRepository.save(new Catalog("cat0002", otherChain, "Fakta Deals"));

        createOffer("off001", "Milk A", "milk a", 2500);
        Offer milkB = new Offer("off002", otherChain, otherCatalog, "Milk B", 3000);
        milkB.setHeadingNormalized("milk b");
        offerRepository.save(milkB);
        Offer bread = new Offer("off003", otherChain, otherCatalog, "Bread", 2000);
        bread.setHeadingNormalized("bread");
        offerRepository.save(bread);

        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");
        query.setChain(List.of("net01", "fak01"));

        Page<Offer> results = offerService.search(query);

        assertEquals(2, results.getTotalElements());
    }

    @Test
    void searchDefaultPaginationIsPage0Size20() {
        for (int i = 1; i <= 25; i++) {
            createOffer("off" + String.format("%03d", i), "Milk " + i, "milk " + i, 100 * i);
        }

        OfferSearchQuery query = new OfferSearchQuery();
        query.setQ("milk");

        Page<Offer> results = offerService.search(query);

        assertEquals(0, results.getNumber());
        assertEquals(20, results.getContent().size());
        assertEquals(25, results.getTotalElements());
    }
}
