package tilbud.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tilbud.entity.Catalog;
import tilbud.entity.Chain;
import tilbud.entity.Offer;
import tilbud.repository.CatalogRepository;
import tilbud.repository.ChainRepository;
import tilbud.repository.OfferRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OfferRepositoryIntegrationTest extends AbstractIntegrationTest {

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

        chain = chainRepository.save(new Chain("test01", "Test Grocery"));
        catalog = catalogRepository.save(new Catalog("cat0001", chain, "Weekly Deals"));
    }

    private Offer createOffer(String offerId, String heading, String headingNormalized, int price) {
        Offer offer = new Offer(offerId, chain, catalog, heading, price);
        offer.setHeadingNormalized(headingNormalized);
        return offerRepository.save(offer);
    }

    @Test
    void searchFindsExactWordMatch() {
        createOffer("off001", "Organic Milk", "organic milk", 2500);
        createOffer("off002", "Sourdough Bread", "sourdough bread", 3000);

        List<Offer> results = offerRepository.search("milk");

        assertEquals(1, results.size());
        assertEquals("off001", results.getFirst().getOfferId());
    }

    @Test
    void searchIsCaseInsensitive() {
        createOffer("off001", "Organic Milk", "organic milk", 2500);

        List<Offer> results = offerRepository.search("MILK");

        assertEquals(1, results.size());
        assertEquals("off001", results.getFirst().getOfferId());
    }

    @Test
    void searchOrMatchesMultipleWords() {
        createOffer("off001", "Organic Milk", "organic milk", 2500);
        createOffer("off002", "Sourdough Bread", "sourdough bread", 3000);
        createOffer("off003", "Fresh Eggs", "fresh eggs", 4000);

        List<Offer> results = offerRepository.search("milk | bread");

        assertEquals(2, results.size());
        List<String> offerIds = results.stream().map(Offer::getOfferId).toList();
        assertTrue(offerIds.contains("off001"));
        assertTrue(offerIds.contains("off002"));
    }

    @Test
    void searchReturnsResultsOrderedByPriceAscending() {
        createOffer("off001", "Premium Milk", "premium milk", 5000);
        createOffer("off002", "Organic Milk", "organic milk", 2500);
        createOffer("off003", "Fresh Milk", "fresh milk", 3500);

        List<Offer> results = offerRepository.search("milk");

        assertEquals(3, results.size());
        assertEquals(2500, results.get(0).getPrice());
        assertEquals(3500, results.get(1).getPrice());
        assertEquals(5000, results.get(2).getPrice());
    }

    @Test
    void searchReturnsEmptyListForNoMatch() {
        createOffer("off001", "Organic Milk", "organic milk", 2500);

        List<Offer> results = offerRepository.search("cheese");

        assertTrue(results.isEmpty());
    }

    @Test
    void searchWithFiltersByChain() {
        Chain otherChain = chainRepository.save(new Chain("test02", "Other Store"));
        Catalog otherCatalog = catalogRepository.save(new Catalog("cat0002", otherChain, "Other Deals"));

        createOffer("off001", "Organic Milk", "organic milk", 2500);
        Offer otherOffer = new Offer("off002", otherChain, otherCatalog, "Cheap Milk", 1500);
        otherOffer.setHeadingNormalized("cheap milk");
        offerRepository.save(otherOffer);

        List<Offer> results = offerRepository.searchWithFilters("milk", chain.getId(), null, null);

        assertEquals(1, results.size());
        assertEquals("off001", results.getFirst().getOfferId());
    }

    @Test
    void searchWithFiltersByPriceRange() {
        createOffer("off001", "Cheap Milk", "cheap milk", 1500);
        createOffer("off002", "Organic Milk", "organic milk", 3500);
        createOffer("off003", "Premium Milk", "premium milk", 6000);

        List<Offer> results = offerRepository.searchWithFilters("milk", null, 2000, 5000);

        assertEquals(1, results.size());
        assertEquals(3500, results.getFirst().getPrice());
    }

    @Test
    void searchWithFiltersByMinPriceOnly() {
        createOffer("off001", "Cheap Milk", "cheap milk", 1500);
        createOffer("off002", "Premium Milk", "premium milk", 6000);

        List<Offer> results = offerRepository.searchWithFilters("milk", null, 3000, null);

        assertEquals(1, results.size());
        assertEquals(6000, results.getFirst().getPrice());
    }

    @Test
    void searchWithFiltersByMaxPriceOnly() {
        createOffer("off001", "Cheap Milk", "cheap milk", 1500);
        createOffer("off002", "Premium Milk", "premium milk", 6000);

        List<Offer> results = offerRepository.searchWithFilters("milk", null, null, 3000);

        assertEquals(1, results.size());
        assertEquals(1500, results.getFirst().getPrice());
    }

    @Test
    void searchWithFiltersByChainAndPrice() {
        Chain otherChain = chainRepository.save(new Chain("test02", "Other Store"));
        Catalog otherCatalog = catalogRepository.save(new Catalog("cat0002", otherChain, "Other Deals"));

        createOffer("off001", "Organic Milk", "organic milk", 3500);
        Offer otherOffer = new Offer("off002", otherChain, otherCatalog, "Cheap Milk", 1500);
        otherOffer.setHeadingNormalized("cheap milk");
        offerRepository.save(otherOffer);

        List<Offer> results = offerRepository.searchWithFilters("milk", chain.getId(), 2000, 5000);

        assertEquals(1, results.size());
        assertEquals("off001", results.getFirst().getOfferId());
    }

    @Test
    void searchWithFiltersAllNullsReturnsAllMatching() {
        createOffer("off001", "Organic Milk", "organic milk", 2500);
        createOffer("off002", "Sourdough Bread", "sourdough bread", 3000);

        List<Offer> results = offerRepository.searchWithFilters("milk | bread", null, null, null);

        assertEquals(2, results.size());
    }

    @Test
    void searchPartialWordDoesNotMatch() {
        createOffer("off001", "Organic Milk", "organic milk", 2500);

        List<Offer> results = offerRepository.search("org");

        assertTrue(results.isEmpty());
    }

    @Test
    void searchWithSpecialCharactersInQuery() {
        createOffer("off001", "Vitamin C+ tablets", "vitamin c  tablets", 4500);

        List<Offer> results = offerRepository.search("vitamin");

        assertEquals(1, results.size());
    }

    @Test
    void searchUsesHeadingNormalizedNotHeading() {
        Offer offer = new Offer("off001", chain, catalog, "ORGANIC MILK", 2500);
        offer.setHeadingNormalized("organic milk");
        offerRepository.save(offer);

        List<Offer> results = offerRepository.search("organic & milk");

        assertEquals(1, results.size());
    }
}
