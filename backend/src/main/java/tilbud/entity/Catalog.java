package tilbud.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "catalogs")
public class Catalog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "catalog_id", nullable = false, unique = true, length = 8)
    private String catalogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chain_id", nullable = false)
    private Chain chain;

    @Column(length = 255)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "catalog_type")
    private CatalogType catalogType;

    @Column(name = "category_ids")
    private List<String> categoryIds;

    @Column(name = "run_from")
    private Instant runFrom;

    @Column(name = "run_till")
    private Instant runTill;

    @Column(name = "offer_count")
    private Integer offerCount;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Catalog() {}

    public Catalog(String catalogId, Chain chain, String label) {
        this.catalogId = catalogId;
        this.chain = chain;
        this.label = label;
    }

    public UUID getId() { return id; }
    public String getCatalogId() { return catalogId; }
    public Chain getChain() { return chain; }
    public String getLabel() { return label; }
    public CatalogType getCatalogType() { return catalogType; }
    public List<String> getCategoryIds() { return categoryIds; }
    public Instant getRunFrom() { return runFrom; }
    public Instant getRunTill() { return runTill; }
    public Integer getOfferCount() { return offerCount; }
    public String getPdfUrl() { return pdfUrl; }
    public Instant getCreatedAt() { return createdAt; }

    public void setCatalogType(CatalogType catalogType) { this.catalogType = catalogType; }
    public void setCategoryIds(List<String> categoryIds) { this.categoryIds = categoryIds; }
    public void setRunFrom(Instant runFrom) { this.runFrom = runFrom; }
    public void setRunTill(Instant runTill) { this.runTill = runTill; }
    public void setOfferCount(Integer offerCount) { this.offerCount = offerCount; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
}
