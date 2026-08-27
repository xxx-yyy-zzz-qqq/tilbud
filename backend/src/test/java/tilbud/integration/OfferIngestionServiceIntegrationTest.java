package tilbud.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tilbud.client.CatalogDto;
import tilbud.client.DealerDto;
import tilbud.client.OfferDto;
import tilbud.entity.Catalog;
import tilbud.entity.Chain;
import tilbud.entity.Offer;
import tilbud.repository.CatalogRepository;
import tilbud.repository.ChainRepository;
import tilbud.repository.OfferRepository;
import tilbud.service.OfferIngestionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
class OfferIngestionServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OfferIngestionService ingestionService;

    @Autowired
    private ChainRepository chainRepository;

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private OfferRepository offerRepository;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @Test
    void fetchAllChainsCreatesChainsFromDealers() {
        DealerDto dealer = new DealerDto("net01", "Netto", "https://netto.dk", "logo.png", "FF6600", new DealerDto.CountryDto("DK"));
        BDDMockito.given(etilbudsavisClient.getDealers()).willReturn(List.of(dealer));
        BDDMockito.given(etilbudsavisClient.getCatalogs("net01")).willReturn(List.of());
        BDDMockito.given(etilbudsavisClient.getOffers(anyString())).willReturn(List.of());

        ingestionService.fetchAllChains();

        assertTrue(chainRepository.existsByDealerId("net01"));
        Chain saved = chainRepository.findByDealerId("net01").orElseThrow();
        assertEquals("Netto", saved.getName());
        assertEquals("DK", saved.getCountry());
    }

    @Test
    void fetchAllChainsUpdatesExistingDealers() {
        Chain existing = chainRepository.save(new Chain("net01", "Old Name"));

        DealerDto dealer = new DealerDto("net01", "New Name", null, null, null, null);
        BDDMockito.given(etilbudsavisClient.getDealers()).willReturn(List.of(dealer));
        BDDMockito.given(etilbudsavisClient.getCatalogs("net01")).willReturn(List.of());
        BDDMockito.given(etilbudsavisClient.getOffers(anyString())).willReturn(List.of());

        ingestionService.fetchAllChains();

        Chain updated = chainRepository.findByDealerId("net01").orElseThrow();
        assertEquals("New Name", updated.getName());
        assertEquals(existing.getId(), updated.getId());
    }

    @Test
    void fetchChainCreatesCatalogsAndOffers() {
        Chain chain = chainRepository.save(new Chain("net01", "Netto"));

        CatalogDto catalogDto = new CatalogDto("cat0001", "Weekly Deals", "net01",
                "2026-01-01T00:00:00+00:00", "2026-01-07T23:59:59+00:00", 2, null);

        OfferDto offerDto = new OfferDto("off0001", "Organic Milk", "Fresh organic milk",
                1, new OfferDto.PricingDto(2500, 3000, "DKK"), null,
                "2026-01-01T00:00:00+00:00", "2026-01-07T23:59:59+00:00",
                "cat0001", "net01", null);

        BDDMockito.given(etilbudsavisClient.getCatalogs("net01")).willReturn(List.of(catalogDto));
        BDDMockito.given(etilbudsavisClient.getOffers("cat0001")).willReturn(List.of(offerDto));

        ingestionService.fetchChain(chain);

        List<Catalog> catalogs = catalogRepository.findByChain(chain);
        assertEquals(1, catalogs.size());
        assertEquals("cat0001", catalogs.getFirst().getCatalogId());
        assertEquals("Weekly Deals", catalogs.getFirst().getLabel());

        List<Offer> offers = offerRepository.findByChain(chain);
        assertEquals(1, offers.size());
        assertEquals("off0001", offers.getFirst().getOfferId());
        assertEquals(2500, offers.getFirst().getPrice());
        assertEquals("organic milk", offers.getFirst().getHeadingNormalized());
    }

    @Test
    void fetchChainDeleteAllThenInsertStrategy() {
        Chain chain = chainRepository.save(new Chain("net01", "Netto"));

        Catalog oldCatalog = catalogRepository.save(new Catalog("old0001", chain, "Old Catalog"));
        Offer oldOffer = new Offer("oldoff01", chain, oldCatalog, "Old Offer", 1000);
        oldOffer.setHeadingNormalized("old offer");
        offerRepository.save(oldOffer);

        CatalogDto newCatalogDto = new CatalogDto("new0001", "New Catalog", "net01",
                "2026-01-01T00:00:00+00:00", "2026-01-07T23:59:59+00:00", 1, null);
        OfferDto newOfferDto = new OfferDto("newoff01", "New Offer", "Brand new offer",
                5, new OfferDto.PricingDto(3500, null, "DKK"), null,
                "2026-01-01T00:00:00+00:00", "2026-01-07T23:59:59+00:00",
                "new0001", "net01", null);

        BDDMockito.given(etilbudsavisClient.getCatalogs("net01")).willReturn(List.of(newCatalogDto));
        BDDMockito.given(etilbudsavisClient.getOffers("new0001")).willReturn(List.of(newOfferDto));

        ingestionService.fetchChain(chain);

        assertTrue(catalogRepository.findByCatalogId("old0001").isEmpty());
        assertEquals(0, offerRepository.findByChain(chain).stream()
                .filter(o -> o.getOfferId().equals("oldoff01")).count());

        assertTrue(catalogRepository.findByCatalogId("new0001").isPresent());
        assertEquals(1, offerRepository.findByChain(chain).stream()
                .filter(o -> o.getOfferId().equals("newoff01")).count());
    }

    @Test
    void fetchChainSkipsNonWeeklyCatalogs() {
        Chain chain = chainRepository.save(new Chain("net01", "Netto"));

        CatalogDto noDatesCatalog = new CatalogDto("cat0001", "No Dates", "net01",
                null, null, 5, null);

        BDDMockito.given(etilbudsavisClient.getCatalogs("net01")).willReturn(List.of(noDatesCatalog));

        ingestionService.fetchChain(chain);

        assertTrue(catalogRepository.findByChain(chain).isEmpty());
    }

    @Test
    void fetchChainPerCatalogErrorHandlingDoesNotKillChain() {
        Chain chain = chainRepository.save(new Chain("net01", "Netto"));

        CatalogDto goodCatalog = new CatalogDto("good001", "Good Catalog", "net01",
                "2026-01-01T00:00:00+00:00", "2026-01-07T23:59:59+00:00", 1, null);
        CatalogDto badCatalog = new CatalogDto("bad0001", "Bad Catalog", "net01",
                "2026-01-01T00:00:00+00:00", "2026-01-07T23:59:59+00:00", 1, null);

        OfferDto goodOffer = new OfferDto("off0001", "Good Offer", "A good offer",
                1, new OfferDto.PricingDto(1500, null, "DKK"), null,
                "2026-01-01T00:00:00+00:00", "2026-01-07T23:59:59+00:00",
                "good001", "net01", null);

        BDDMockito.given(etilbudsavisClient.getCatalogs("net01")).willReturn(List.of(goodCatalog, badCatalog));
        BDDMockito.given(etilbudsavisClient.getOffers("good001")).willReturn(List.of(goodOffer));
        BDDMockito.given(etilbudsavisClient.getOffers("bad0001")).willThrow(new RuntimeException("API error"));

        ingestionService.fetchChain(chain);

        assertTrue(catalogRepository.findByCatalogId("good001").isPresent());
        assertEquals(1, offerRepository.findByChain(chain).stream()
                .filter(o -> o.getOfferId().equals("off0001")).count());

        assertEquals(1, ingestionService.getLastErrors());
    }

    @Test
    void fetchChainDeduplicatesOffers() {
        Chain chain = chainRepository.save(new Chain("net01", "Netto"));

        CatalogDto catalogDto = new CatalogDto("cat0001", "Catalog", "net01",
                "2026-01-01T00:00:00+00:00", "2026-01-07T23:59:59+00:00", 1, null);

        OfferDto offerDto = new OfferDto("dup0001", "Duplicate Offer", "Same offer",
                1, new OfferDto.PricingDto(2500, null, "DKK"), null,
                "2026-01-01T00:00:00+00:00", "2026-01-07T23:59:59+00:00",
                "cat0001", "net01", null);

        BDDMockito.given(etilbudsavisClient.getCatalogs("net01")).willReturn(List.of(catalogDto));
        BDDMockito.given(etilbudsavisClient.getOffers("cat0001")).willReturn(List.of(offerDto, offerDto));

        ingestionService.fetchChain(chain);

        List<Offer> offers = offerRepository.findByChain(chain);
        assertEquals(1, offers.size());
    }

    @Test
    void fetchChainSkipsEmptyOfferCatalogs() {
        Chain chain = chainRepository.save(new Chain("net01", "Netto"));

        CatalogDto catalogDto = new CatalogDto("cat0001", "Empty Catalog", "net01",
                "2026-01-01T00:00:00+00:00", "2026-01-07T23:59:59+00:00", 0, null);

        BDDMockito.given(etilbudsavisClient.getCatalogs("net01")).willReturn(List.of(catalogDto));
        BDDMockito.given(etilbudsavisClient.getOffers("cat0001")).willReturn(List.of());

        ingestionService.fetchChain(chain);

        assertTrue(catalogRepository.findByCatalogId("cat0001").isEmpty());
    }

    @Test
    void ingestionServiceReportsStats() {
        Chain chain = chainRepository.save(new Chain("net01", "Netto"));

        CatalogDto catalogDto = new CatalogDto("cat0001", "Catalog", "net01",
                "2026-01-01T00:00:00+00:00", "2026-01-07T23:59:59+00:00", 1, null);
        OfferDto offerDto = new OfferDto("off0001", "Offer", "An offer",
                1, new OfferDto.PricingDto(1000, null, "DKK"), null,
                "2026-01-01T00:00:00+00:00", "2026-01-07T23:59:59+00:00",
                "cat0001", "net01", null);

        BDDMockito.given(etilbudsavisClient.getDealers()).willReturn(List.of(
                new DealerDto("net01", "Netto", null, null, null, null)));
        BDDMockito.given(etilbudsavisClient.getCatalogs("net01")).willReturn(List.of(catalogDto));
        BDDMockito.given(etilbudsavisClient.getOffers("cat0001")).willReturn(List.of(offerDto));

        ingestionService.fetchAllChains();

        assertEquals(1, ingestionService.getLastChainsProcessed());
        assertEquals(1, ingestionService.getLastCatalogsProcessed());
        assertEquals(1, ingestionService.getLastOffersInserted());
        assertNotNull(ingestionService.getLastRunTime());
    }
}
