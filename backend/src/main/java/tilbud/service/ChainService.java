package tilbud.service;

import org.springframework.stereotype.Service;
import tilbud.entity.Chain;
import tilbud.repository.ChainRepository;
import tilbud.repository.OfferRepository;

import java.util.List;

@Service
public class ChainService {

    private final ChainRepository chainRepository;
    private final OfferRepository offerRepository;

    public ChainService(ChainRepository chainRepository, OfferRepository offerRepository) {
        this.chainRepository = chainRepository;
        this.offerRepository = offerRepository;
    }

    public List<Chain> findAll() {
        return chainRepository.findAll();
    }
}
