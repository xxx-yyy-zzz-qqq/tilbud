package tilbud.dto;

import tilbud.entity.Chain;

public class ChainResponse {

    private String id;
    private String dealerId;
    private String name;
    private String website;
    private String logoUrl;
    private String color;
    private long offerCount;

    public static ChainResponse from(Chain chain, long offerCount) {
        ChainResponse r = new ChainResponse();
        r.id = chain.getId().toString();
        r.dealerId = chain.getDealerId();
        r.name = chain.getName();
        r.website = chain.getWebsite();
        r.logoUrl = chain.getLogoUrl();
        r.color = chain.getColor();
        r.offerCount = offerCount;
        return r;
    }

    public String getId() { return id; }
    public String getDealerId() { return dealerId; }
    public String getName() { return name; }
    public String getWebsite() { return website; }
    public String getLogoUrl() { return logoUrl; }
    public String getColor() { return color; }
    public long getOfferCount() { return offerCount; }
}
