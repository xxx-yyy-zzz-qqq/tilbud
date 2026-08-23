package tilbud.dto;

import tilbud.entity.Chain;

public class ChainSummary {

    private String id;
    private String dealerId;
    private String name;

    public ChainSummary(String id, String dealerId, String name) {
        this.id = id;
        this.dealerId = dealerId;
        this.name = name;
    }

    public static ChainSummary from(Chain chain) {
        return new ChainSummary(
            chain.getId().toString(),
            chain.getDealerId(),
            chain.getName()
        );
    }

    public String getId() { return id; }
    public String getDealerId() { return dealerId; }
    public String getName() { return name; }
}
