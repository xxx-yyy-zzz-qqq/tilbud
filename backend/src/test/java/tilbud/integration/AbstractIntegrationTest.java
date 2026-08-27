package tilbud.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tilbud.client.EtilbudsavisClient;
import tilbud.client.OfferDto;

import java.util.List;

import static org.mockito.BDDMockito.given;

public abstract class AbstractIntegrationTest {

    static final org.testcontainers.containers.PostgreSQLContainer<?> postgres;

    static {
        postgres = new org.testcontainers.containers.PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("tilbud_test")
                .withUsername("test")
                .withPassword("test");
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @MockitoBean
    protected EtilbudsavisClient etilbudsavisClient;

    @Autowired
    protected JdbcTemplate jdbc;

    protected void cleanDatabase() {
        jdbc.execute("TRUNCATE offers, catalogs, chains CASCADE");
    }
}
