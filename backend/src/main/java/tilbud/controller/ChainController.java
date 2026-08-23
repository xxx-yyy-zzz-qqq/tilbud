package tilbud.controller;

import org.springframework.web.bind.annotation.*;
import tilbud.dto.ChainResponse;
import tilbud.entity.Chain;
import tilbud.repository.OfferRepository;
import tilbud.service.ChainService;

import java.util.List;

@RestController
@RequestMapping("/api/chains")
public class ChainController {

    private final ChainService chainService;
    private final OfferRepository offerRepository;

    public ChainController(ChainService chainService, OfferRepository offerRepository) {
        this.chainService = chainService;
        this.offerRepository = offerRepository;
    }

    @GetMapping
    public List<ChainResponse> list() {
        return chainService.findAll().stream()
            .map(chain -> ChainResponse.from(chain, offerRepository.countByChain(chain)))
            .toList();
    }
}
