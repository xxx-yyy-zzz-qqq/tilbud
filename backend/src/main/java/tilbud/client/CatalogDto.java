package tilbud.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogDto(
    String id,
    String label,
    @JsonProperty("dealer_id") String dealerId,
    @JsonProperty("run_from") String runFrom,
    @JsonProperty("run_till") String runTill,
    @JsonProperty("offer_count") Integer offerCount,
    @JsonProperty("category_ids") List<String> categoryIds
) {}
