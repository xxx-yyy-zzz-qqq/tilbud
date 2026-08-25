package tilbud.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DealerDto(
    String id,
    String name,
    String website,
    String logo,
    String color,
    CountryDto country
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CountryDto(String id) {}
}
