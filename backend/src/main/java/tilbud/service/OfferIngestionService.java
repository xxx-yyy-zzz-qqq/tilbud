package tilbud.service;

import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tilbud.client.CatalogDto;
import tilbud.client.DealerDto;
import tilbud.client.EtilbudsavisClient;
import tilbud.client.OfferDto;
import tilbud.entity.Catalog;
import tilbud.entity.Chain;
import tilbud.entity.Offer;
import tilbud.repository.CatalogRepository;
import tilbud.repository.ChainRepository;
import tilbud.repository.OfferRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OfferIngestionService {

    private static final Logger log = LoggerFactory.getLogger(OfferIngestionService.class);
    private static final DateTimeFormatter API_DATE_FORMAT = new DateTimeFormatterBuilder()
        .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        .optionalStart().appendOffset("+HH:MM", "+00:00").optionalEnd()
        .optionalStart().appendOffset("+HHMM", "+0000").optionalEnd()
        .optionalStart().appendOffset("+HH", "Z").optionalEnd()
        .toFormatter();

    private final EtilbudsavisClient client;
    private final ChainRepository chainRepository;
    private final CatalogRepository catalogRepository;
    private final OfferRepository offerRepository;
    private final MeterRegistry meterRegistry;
    private final JdbcTemplate jdbc;
    private final int fetchConcurrency;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger lastChainsProcessed = new AtomicInteger(0);
    private final AtomicInteger lastCatalogsProcessed = new AtomicInteger(0);
    private final AtomicInteger lastOffersInserted = new AtomicInteger(0);
    private final AtomicInteger lastErrors = new AtomicInteger(0);
    private volatile Instant lastRunTime;

    public OfferIngestionService(
            EtilbudsavisClient client,
            ChainRepository chainRepository,
            CatalogRepository catalogRepository,
            OfferRepository offerRepository,
            MeterRegistry meterRegistry,
            JdbcTemplate jdbc,
            @org.springframework.beans.factory.annotation.Value("${ingestion.fetch-concurrency:10}") int fetchConcurrency) {
        this.client = client;
        this.chainRepository = chainRepository;
        this.catalogRepository = catalogRepository;
        this.offerRepository = offerRepository;
        this.meterRegistry = meterRegistry;
        this.jdbc = jdbc;
        this.fetchConcurrency = fetchConcurrency;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        fetchOnStartup();
    }

    public void fetchOnStartup() {
        Thread.startVirtualThread(this::fetchAllChains);
    }

    public boolean fetchAllChains() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Fetch already in progress, rejecting");
            return false;
        }

        log.info("Starting fetch cycle");
        lastRunTime = Instant.now();
        lastChainsProcessed.set(0);
        lastCatalogsProcessed.set(0);
        lastOffersInserted.set(0);
        lastErrors.set(0);

        try {
            discoverDealers();

            List<Chain> chains = chainRepository.findAll();

            Timer.Sample sample = Timer.start(meterRegistry);

            var semaphore = new Semaphore(fetchConcurrency);
            log.info("Fetching {} chains with concurrency {}", chains.size(), fetchConcurrency);
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<CompletableFuture<Void>> futures = chains.stream()
                    .map(chain -> CompletableFuture.runAsync(() -> {
                        semaphore.acquireUninterruptibly();
                        try {
                            fetchChain(chain);
                            lastChainsProcessed.incrementAndGet();
                        } catch (Exception e) {
                            log.error("Error fetching chain {}: {}", chain.getDealerId(), e.getMessage(), e);
                            lastErrors.incrementAndGet();
                            meterRegistry.counter("etilbudsavis_fetch_errors_total",
                                "chain", chain.getDealerId()).increment();
                        } finally {
                            semaphore.release();
                        }
                    }, executor))
                    .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }

            sample.stop(Timer.builder("etilbudsavis_fetch_duration_seconds")
                .description("Time to fetch all chains")
                .register(meterRegistry));

            log.info("Fetch cycle complete: {} chains, {} catalogs, {} offers, {} errors",
                lastChainsProcessed.get(), lastCatalogsProcessed.get(),
                lastOffersInserted.get(), lastErrors.get());

            return true;
        } finally {
            running.set(false);
        }
    }

    private void discoverDealers() {
        log.info("Discovering dealers from API");
        List<DealerDto> dealers = client.getDealers();

        int created = 0;
        int updated = 0;

        for (DealerDto dealer : dealers) {
            Chain existing = chainRepository.findByDealerId(dealer.id()).orElse(null);

            if (existing == null) {
                Chain chain = new Chain(dealer.id(), dealer.name());
                chain.setWebsite(dealer.website());
                chain.setLogoUrl(dealer.logo());
                chain.setColor(dealer.color());
                if (dealer.country() != null) {
                    chain.setCountry(dealer.country().id());
                }
                chainRepository.save(chain);
                created++;
            } else {
                boolean changed = false;
                if (dealer.name() != null && !dealer.name().equals(existing.getName())) {
                    existing.setName(dealer.name());
                    changed = true;
                }
                if (dealer.website() != null && !dealer.website().equals(existing.getWebsite())) {
                    existing.setWebsite(dealer.website());
                    changed = true;
                }
                if (dealer.logo() != null && !dealer.logo().equals(existing.getLogoUrl())) {
                    existing.setLogoUrl(dealer.logo());
                    changed = true;
                }
                if (dealer.color() != null && !dealer.color().equals(existing.getColor())) {
                    existing.setColor(dealer.color());
                    changed = true;
                }
                if (changed) {
                    chainRepository.save(existing);
                    updated++;
                }
            }
        }

        log.info("Dealer discovery: {} created, {} updated, {} total", created, updated, dealers.size());
    }

    @Retry(name = "etilbudsavis")
    public void fetchChain(Chain chain) {
        log.debug("Fetching catalogs for chain {} ({})", chain.getName(), chain.getDealerId());

        jdbc.update("DELETE FROM catalogs WHERE chain_id = ?", chain.getId());

        List<CatalogDto> catalogs = client.getCatalogs(chain.getDealerId());
        List<CatalogDto> weeklyCatalogs = catalogs.stream()
            .filter(this::isWeeklyCatalog)
            .toList();

        log.debug("Chain {} has {} catalogs, {} weekly",
            chain.getDealerId(), catalogs.size(), weeklyCatalogs.size());

        for (CatalogDto catalogDto : weeklyCatalogs) {
            try {
                fetchCatalogOffers(chain, catalogDto);
            } catch (Exception e) {
                log.error("Error fetching offers for catalog {} in chain {}: {}",
                    catalogDto.id(), chain.getDealerId(), e.getMessage());
                lastErrors.incrementAndGet();
            }
        }
    }

    private void fetchCatalogOffers(Chain chain, CatalogDto catalogDto) {
        List<OfferDto> offers = client.getOffers(catalogDto.id());

        if (offers.isEmpty()) {
            log.debug("Catalog {} has 0 offers, skipping", catalogDto.id());
            return;
        }

        Catalog catalog = createCatalog(chain, catalogDto);

        List<OfferDto> uniqueOffers = offers.stream()
            .collect(Collectors.toMap(
                OfferDto::id,
                o -> o,
                (a, b) -> a
            ))
            .values()
            .stream()
            .toList();

        for (OfferDto offerDto : uniqueOffers) {
            Offer offer = mapToOffer(offerDto, chain, catalog);
            offerRepository.save(offer);
        }

        lastOffersInserted.addAndGet(uniqueOffers.size());
        lastCatalogsProcessed.incrementAndGet();

        meterRegistry.counter("etilbudsavis_offers_fetched_total",
            "chain", chain.getDealerId()).increment(uniqueOffers.size());
        meterRegistry.counter("etilbudsavis_offers_inserted_total",
            "chain", chain.getDealerId()).increment(uniqueOffers.size());

        log.debug("Catalog {}: inserted {} offers", catalogDto.id(), uniqueOffers.size());
    }

    private Catalog createCatalog(Chain chain, CatalogDto dto) {
        Catalog catalog = new Catalog(dto.id(), chain, dto.label());
        catalog.setRunFrom(parseInstant(dto.runFrom()));
        catalog.setRunTill(parseInstant(dto.runTill()));
        catalog.setOfferCount(dto.offerCount());
        catalog.setCategoryIds(dto.categoryIds() != null ? new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(dto.categoryIds()).toString() : null);
        return catalogRepository.save(catalog);
    }

    private boolean isWeeklyCatalog(CatalogDto catalog) {
        return catalog.runFrom() != null && catalog.runTill() != null;
    }

    private Offer mapToOffer(OfferDto dto, Chain chain, Catalog catalog) {
        Offer offer = new Offer(dto.id(), chain, catalog, dto.heading(), dto.pricing().price());
        offer.setDescription(dto.description());
        offer.setPrePrice(dto.pricing().prePrice());
        offer.setCatalogPage(dto.catalogPage());
        offer.setRunFrom(parseInstant(dto.runFrom()));
        offer.setRunTill(parseInstant(dto.runTill()));
        offer.setHeadingNormalized(dto.heading().toLowerCase().replaceAll("[^a-z0-9æøå]", " "));

        if (dto.quantity() != null) {
            offer.setQuantity(toJson(dto.quantity()));
        }
        if (dto.images() != null) {
            offer.setImages(toJson(dto.images()));
        }

        return offer;
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Instant parseInstant(String dateTime) {
        return OffsetDateTime.parse(dateTime, API_DATE_FORMAT).toInstant();
    }

    public boolean isRunning() {
        return running.get();
    }

    public Instant getLastRunTime() {
        return lastRunTime;
    }

    public int getLastChainsProcessed() {
        return lastChainsProcessed.get();
    }

    public int getLastCatalogsProcessed() {
        return lastCatalogsProcessed.get();
    }

    public int getLastOffersInserted() {
        return lastOffersInserted.get();
    }

    public int getLastErrors() {
        return lastErrors.get();
    }
}
