package tilbud.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OfferDto(
    String id,
    String heading,
    String description,
    @JsonProperty("catalog_page") Integer catalogPage,
    PricingDto pricing,
    QuantityDto quantity,
    @JsonProperty("run_from") String runFrom,
    @JsonProperty("run_till") String runTill,
    @JsonProperty("catalog_id") String catalogId,
    @JsonProperty("dealer_id") String dealerId,
    ImagesDto images
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PricingDto(
        Integer price,
        @JsonProperty("pre_price") Integer prePrice,
        String currency
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QuantityDto(
        UnitDto unit,
        SizeDto size,
        PiecesDto pieces
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record UnitDto(String symbol) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record SizeDto(Integer from, Integer to) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record PiecesDto(Integer from, Integer to, Integer min, Integer max) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImagesDto(String thumb, String view, String zoom) {}
}
