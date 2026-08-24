package tilbud.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OfferControllerIntegrationTest {

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
    void getByIdReturns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/offers/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound());
    }
}
