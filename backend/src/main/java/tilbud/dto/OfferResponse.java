package tilbud.dto;

import tilbud.entity.Offer;
import java.time.Instant;
import java.util.UUID;

public class OfferResponse {

    private UUID id;
    private String offerId;
    private ChainSummary chain;
    private String heading;
    private String description;
    private Integer price;
    private Integer prePrice;
    private String currency;
    private Integer catalogPage;
    private String quantity;
    private String images;
    private Instant runFrom;
    private Instant runTill;

    public static OfferResponse from(Offer offer) {
        OfferResponse r = new OfferResponse();
        r.id = offer.getId();
        r.offerId = offer.getOfferId();
        r.chain = ChainSummary.from(offer.getChain());
        r.heading = offer.getHeading();
        r.description = offer.getDescription();
        r.price = offer.getPrice();
        r.prePrice = offer.getPrePrice();
        r.currency = offer.getCurrency();
        r.catalogPage = offer.getCatalogPage();
        r.quantity = offer.getQuantity();
        r.images = offer.getImages();
        r.runFrom = offer.getRunFrom();
        r.runTill = offer.getRunTill();
        return r;
    }

    public UUID getId() { return id; }
    public String getOfferId() { return offerId; }
    public ChainSummary getChain() { return chain; }
    public String getHeading() { return heading; }
    public String getDescription() { return description; }
    public Integer getPrice() { return price; }
    public Integer getPrePrice() { return prePrice; }
    public String getCurrency() { return currency; }
    public Integer getCatalogPage() { return catalogPage; }
    public String getQuantity() { return quantity; }
    public String getImages() { return images; }
    public Instant getRunFrom() { return runFrom; }
    public Instant getRunTill() { return runTill; }
}
