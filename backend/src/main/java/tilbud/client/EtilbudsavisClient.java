package tilbud.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class EtilbudsavisClient {

    private static final Logger log = LoggerFactory.getLogger(EtilbudsavisClient.class);
    private static final int PAGE_SIZE = 100;

    private final RestClient restClient;

    public EtilbudsavisClient(@Value("${api.base-url}") String baseUrl) {
        var httpClient = java.net.http.HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

        var requestFactory = new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
    }

    public List<CatalogDto> getCatalogs(String dealerId) {
        String url = UriComponentsBuilder.fromPath("/catalogs")
            .queryParam("dealer_ids", dealerId)
            .queryParam("limit", 100)
            .toUriString();

        log.debug("Fetching catalogs for dealer {}", dealerId);
        CatalogDto[] catalogs = restClient.get()
            .uri(url)
            .retrieve()
            .body(CatalogDto[].class);

        if (catalogs == null) {
            return List.of();
        }
        return List.of(catalogs);
    }

    public List<OfferDto> getOffers(String catalogId) {
        List<OfferDto> allOffers = new ArrayList<>();
        int offset = 0;

        while (true) {
            String url = UriComponentsBuilder.fromPath("/offers")
                .queryParam("catalog_ids", catalogId)
                .queryParam("limit", PAGE_SIZE)
                .queryParam("offset", offset)
                .toUriString();

            log.debug("Fetching offers for catalog {} offset {}", catalogId, offset);
            OfferDto[] page = restClient.get()
                .uri(url)
                .retrieve()
                .body(OfferDto[].class);

            if (page == null || page.length == 0) {
                break;
            }

            allOffers.addAll(List.of(page));

            if (page.length < PAGE_SIZE) {
                break;
            }

            offset += PAGE_SIZE;
        }

        return allOffers;
    }

    public List<DealerDto> getDealers() {
        List<DealerDto> allDealers = new ArrayList<>();
        int offset = 0;

        while (true) {
            String url = UriComponentsBuilder.fromPath("/dealers")
                .queryParam("limit", PAGE_SIZE)
                .queryParam("offset", offset)
                .toUriString();

            log.debug("Fetching dealers offset {}", offset);
            DealerDto[] page = restClient.get()
                .uri(url)
                .retrieve()
                .body(DealerDto[].class);

            if (page == null || page.length == 0) {
                break;
            }

            allDealers.addAll(List.of(page));

            if (page.length < PAGE_SIZE) {
                break;
            }

            offset += PAGE_SIZE;
        }

        log.info("Fetched {} dealers from API", allDealers.size());
        return allDealers;
    }
}
