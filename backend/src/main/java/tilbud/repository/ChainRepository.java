package tilbud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tilbud.entity.Chain;
import java.util.Optional;
import java.util.UUID;

public interface ChainRepository extends JpaRepository<Chain, UUID> {
    Optional<Chain> findByDealerId(String dealerId);
    boolean existsByDealerId(String dealerId);
}
