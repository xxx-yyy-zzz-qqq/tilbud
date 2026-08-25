package tilbud.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import tilbud.entity.Catalog;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CatalogResponse {

    private static final ObjectMapper mapper = new ObjectMapper();

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
        r.categoryIds = parseCategoryIds(catalog.getCategoryIds());
        r.runFrom = catalog.getRunFrom();
        r.runTill = catalog.getRunTill();
        r.offerCount = catalog.getOfferCount();
        return r;
    }

    private static List<String> parseCategoryIds(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
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
