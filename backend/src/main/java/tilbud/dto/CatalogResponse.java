package tilbud.dto;

import tilbud.entity.Catalog;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CatalogResponse {

    private UUID id;
    private String catalogId;
    private ChainSummary chain;
    private String label;
    private String catalogType;
    private List<String> categoryIds;
    private Instant runFrom;
    private Instant runTill;
    private Integer offerCount;

    public static CatalogResponse from(Catalog catalog) {
        CatalogResponse r = new CatalogResponse();
        r.id = catalog.getId();
        r.catalogId = catalog.getCatalogId();
        r.chain = ChainSummary.from(catalog.getChain());
        r.label = catalog.getLabel();
        r.catalogType = catalog.getCatalogType();
        r.categoryIds = catalog.getCategoryIds();
        r.runFrom = catalog.getRunFrom();
        r.runTill = catalog.getRunTill();
        r.offerCount = catalog.getOfferCount();
        return r;
    }

    public UUID getId() { return id; }
    public String getCatalogId() { return catalogId; }
    public ChainSummary getChain() { return chain; }
    public String getLabel() { return label; }
    public String getCatalogType() { return catalogType; }
    public List<String> getCategoryIds() { return categoryIds; }
    public Instant getRunFrom() { return runFrom; }
    public Instant getRunTill() { return runTill; }
    public Integer getOfferCount() { return offerCount; }
}
