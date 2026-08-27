package tilbud.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import tilbud.entity.Chain;
import tilbud.repository.ChainRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChainRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ChainRepository chainRepository;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @Test
    void saveAndRetrieveChain() {
        Chain chain = new Chain("net01", "Netto");
        chain.setWebsite("https://netto.dk");
        chain.setColor("FF6600");
        chain.setCountry("DK");
        Chain saved = chainRepository.save(chain);

        assertNotNull(saved.getId());
        assertEquals("net01", saved.getDealerId());
        assertEquals("Netto", saved.getName());
        assertEquals("https://netto.dk", saved.getWebsite());
        assertEquals("FF6600", saved.getColor());
        assertEquals("DK", saved.getCountry());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findByDealerIdReturnsPresent() {
        chainRepository.save(new Chain("fak01", "Fakta"));

        Optional<Chain> found = chainRepository.findByDealerId("fak01");

        assertTrue(found.isPresent());
        assertEquals("Fakta", found.get().getName());
    }

    @Test
    void findByDealerIdReturnsEmptyForUnknown() {
        Optional<Chain> found = chainRepository.findByDealerId("xxxxx");

        assertTrue(found.isEmpty());
    }

    @Test
    void existsByDealerIdReturnsTrueWhenExists() {
        chainRepository.save(new Chain("iri01", "Irma"));

        assertTrue(chainRepository.existsByDealerId("iri01"));
    }

    @Test
    void existsByDealerIdReturnsFalseWhenNotExists() {
        assertFalse(chainRepository.existsByDealerId("iri01"));
    }

    @Test
    void dealerIdMustBeUnique() {
        chainRepository.save(new Chain("uni01", "Chain A"));

        assertThrows(DataIntegrityViolationException.class, () ->
                chainRepository.save(new Chain("uni01", "Chain B")));
    }

    @Test
    void updateChainName() {
        Chain chain = chainRepository.save(new Chain("upd01", "Old Name"));

        chain.setName("New Name");
        chainRepository.save(chain);

        Chain found = chainRepository.findByDealerId("upd01").orElseThrow();
        assertEquals("New Name", found.getName());
    }

    @Test
    void deleteChain() {
        Chain chain = chainRepository.save(new Chain("del01", "To Delete"));

        chainRepository.delete(chain);

        assertTrue(chainRepository.findByDealerId("del01").isEmpty());
    }

    @Test
    void findAllReturnsAllChains() {
        chainRepository.save(new Chain("all01", "Chain One"));
        chainRepository.save(new Chain("all02", "Chain Two"));
        chainRepository.save(new Chain("all03", "Chain Three"));

        assertEquals(3, chainRepository.findAll().size());
    }

    @Test
    void saveChainWithMinimalFields() {
        Chain chain = new Chain("min01", "Minimal Chain");
        Chain saved = chainRepository.save(chain);

        assertNotNull(saved.getId());
        assertEquals("DK", saved.getCountry());
        assertNotNull(saved.getCreatedAt());
    }
}
