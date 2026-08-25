package tilbud.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "offer_id", nullable = false, unique = true, length = 30)
    private String offerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chain_id", nullable = false)
    private Chain chain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id", nullable = false)
    private Catalog catalog;

    @Column(nullable = false, length = 255)
    private String heading;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "pre_price")
    private Integer prePrice;

    @Column(length = 3)
    private String currency = "DKK";

    @Column(name = "catalog_page")
    private Integer catalogPage;

    @Column(columnDefinition = "jsonb")
    private String quantity;

    @Column(columnDefinition = "jsonb")
    private String images;

    @Column(name = "heading_normalized", length = 255)
    private String headingNormalized;

    @Column(name = "run_from")
    private Instant runFrom;

    @Column(name = "run_till")
    private Instant runTill;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Offer() {}

    public Offer(String offerId, Chain chain, Catalog catalog, String heading, Integer price) {
        this.offerId = offerId;
        this.chain = chain;
        this.catalog = catalog;
        this.heading = heading;
        this.price = price;
    }

    public UUID getId() { return id; }
    public String getOfferId() { return offerId; }
    public Chain getChain() { return chain; }
    public Catalog getCatalog() { return catalog; }
    public String getHeading() { return heading; }
    public String getDescription() { return description; }
    public String getHeadingNormalized() { return headingNormalized; }
    public Integer getPrice() { return price; }
    public Integer getPrePrice() { return prePrice; }
    public String getCurrency() { return currency; }
    public Integer getCatalogPage() { return catalogPage; }
    public String getQuantity() { return quantity; }
    public String getImages() { return images; }
    public Instant getRunFrom() { return runFrom; }
    public Instant getRunTill() { return runTill; }
    public Instant getCreatedAt() { return createdAt; }

    public void setHeadingNormalized(String headingNormalized) { this.headingNormalized = headingNormalized; }
    public void setDescription(String description) { this.description = description; }
    public void setPrePrice(Integer prePrice) { this.prePrice = prePrice; }
    public void setCatalogPage(Integer catalogPage) { this.catalogPage = catalogPage; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public void setImages(String images) { this.images = images; }
    public void setRunFrom(Instant runFrom) { this.runFrom = runFrom; }
    public void setRunTill(Instant runTill) { this.runTill = runTill; }
}
