package tilbud.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tilbud.integration.AbstractIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OfferControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchReturnsEmptyPageWhenNoData() throws Exception {
        mockMvc.perform(get("/api/offers/search")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void searchRejectsInvalidSort() throws Exception {
        mockMvc.perform(get("/api/offers/search")
                .param("sort", "invalid_sort"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void searchRejectsNegativePrice() throws Exception {
        mockMvc.perform(get("/api/offers/search")
                .param("minPrice", "-1"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void searchRejectsPageSizeOver100() throws Exception {
        mockMvc.perform(get("/api/offers/search")
                .param("size", "101"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void searchWithEmptyQueryReturnsAllOffers() throws Exception {
        mockMvc.perform(get("/api/offers/search"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void searchAcceptsValidSortOptions() throws Exception {
        mockMvc.perform(get("/api/offers/search")
                .param("sort", "price_asc"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/offers/search")
                .param("sort", "price_desc"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/offers/search")
                .param("sort", "date_asc"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/offers/search")
                .param("sort", "date_desc"))
            .andExpect(status().isOk());
    }

    @Test
    void searchAcceptsPaginationParams() throws Exception {
        mockMvc.perform(get("/api/offers/search")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void searchAcceptsPriceRangeFilter() throws Exception {
        mockMvc.perform(get("/api/offers/search")
                .param("minPrice", "10")
                .param("maxPrice", "100"))
            .andExpect(status().isOk());
    }

    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/offers/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound());
    }
}
