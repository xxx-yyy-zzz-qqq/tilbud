package tilbud.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import tilbud.entity.Catalog;
import tilbud.entity.Chain;
import tilbud.entity.Offer;
import tilbud.repository.CatalogRepository;
import tilbud.repository.ChainRepository;
import tilbud.repository.OfferRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CatalogRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private ChainRepository chainRepository;

    @Autowired
    private OfferRepository offerRepository;

    private Chain chain;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        chain = chainRepository.save(new Chain("test01", "Test Store"));
    }

    @Test
    void saveAndRetrieveCatalog() {
        Catalog catalog = new Catalog("cat0001", chain, "Weekly Deals");
        catalog.setCatalogType("MAIN");
        catalog.setOfferCount(42);
        Catalog saved = catalogRepository.save(catalog);

        assertNotNull(saved.getId());
        assertEquals("cat0001", saved.getCatalogId());
        assertEquals("Weekly Deals", saved.getLabel());
        assertEquals("MAIN", saved.getCatalogType());
        assertEquals(42, saved.getOfferCount());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findByCatalogIdReturnsPresent() {
        catalogRepository.save(new Catalog("cat0002", chain, "Food Deals"));

        Optional<Catalog> found = catalogRepository.findByCatalogId("cat0002");

        assertTrue(found.isPresent());
        assertEquals("Food Deals", found.get().getLabel());
    }

    @Test
    void findByCatalogIdReturnsEmptyForUnknown() {
        Optional<Catalog> found = catalogRepository.findByCatalogId("xxxx");

        assertTrue(found.isEmpty());
    }

    @Test
    void existsByCatalogIdReturnsTrueWhenExists() {
        catalogRepository.save(new Catalog("cat0003", chain, "Test"));

        assertTrue(catalogRepository.existsByCatalogId("cat0003"));
    }

    @Test
    void existsByCatalogIdReturnsFalseWhenNotExists() {
        assertFalse(catalogRepository.existsByCatalogId("cat0003"));
    }

    @Test
    void catalogIdMustBeUnique() {
        catalogRepository.save(new Catalog("uni001", chain, "Catalog A"));

        assertThrows(DataIntegrityViolationException.class, () ->
                catalogRepository.save(new Catalog("uni001", chain, "Catalog B")));
    }

    @Test
    void findByChainReturnsCorrectCatalogs() {
        Chain otherChain = chainRepository.save(new Chain("test02", "Other Store"));

        catalogRepository.save(new Catalog("cat0010", chain, "Chain 1 Catalog"));
        catalogRepository.save(new Catalog("cat0011", chain, "Chain 1 Catalog 2"));
        catalogRepository.save(new Catalog("cat0012", otherChain, "Chain 2 Catalog"));

        List<Catalog> results = catalogRepository.findByChain(chain);

        assertEquals(2, results.size());
    }

    @Test
    void deleteCatalogCascadeDeletesOffers() {
        Catalog catalog = new Catalog("cat0020", chain, "To Be Deleted");
        catalog = catalogRepository.save(catalog);

        Offer offer = new Offer("off0010", chain, catalog, "Milk", 2500);
        offer.setHeadingNormalized("milk");
        offerRepository.save(offer);

        assertEquals(1, offerRepository.findByCatalog(catalog).size());

        catalogRepository.delete(catalog);

        assertTrue(offerRepository.findByCatalog(catalog).isEmpty());
    }

    @Test
    void deleteChainCascadeDeletesCatalogs() {
        Catalog catalog1 = catalogRepository.save(new Catalog("cat0030", chain, "Catalog 1"));
        Catalog catalog2 = catalogRepository.save(new Catalog("cat0031", chain, "Catalog 2"));

        Offer offer = new Offer("off0020", chain, catalog1, "Bread", 3000);
        offer.setHeadingNormalized("bread");
        offerRepository.save(offer);

        assertEquals(2, catalogRepository.findByChain(chain).size());

        chainRepository.delete(chain);

        assertEquals(0, catalogRepository.findByChain(chain).size());
        assertTrue(offerRepository.findByCatalog(catalog1).isEmpty());
    }

    @Test
    void saveCatalogWithDates() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant till = Instant.parse("2026-01-07T23:59:59Z");

        Catalog catalog = new Catalog("cat0040", chain, "Dated Catalog");
        catalog.setRunFrom(from);
        catalog.setRunTill(till);
        catalog = catalogRepository.save(catalog);

        Catalog found = catalogRepository.findByCatalogId("cat0040").orElseThrow();
        assertEquals(from, found.getRunFrom());
        assertEquals(till, found.getRunTill());
    }

    @Test
    void saveCatalogWithCategoryIds() {
        Catalog catalog = new Catalog("cat0050", chain, "Categorized");
        catalog.setCategoryIds("[\"dairy\",\"meat\"]");
        catalog = catalogRepository.save(catalog);

        Catalog found = catalogRepository.findByCatalogId("cat0050").orElseThrow();
        assertEquals("[\"dairy\",\"meat\"]", found.getCategoryIds());
    }
}
