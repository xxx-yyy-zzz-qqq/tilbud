package tilbud.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tilbud.dto.OfferResponse;
import tilbud.dto.OfferSearchQuery;
import tilbud.entity.Offer;
import tilbud.service.OfferService;

import java.util.UUID;

@RestController
@RequestMapping("/api/offers")
@Validated
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping("/search")
    public Page<OfferResponse> search(@Validated @ModelAttribute OfferSearchQuery query) {
        return offerService.search(query).map(OfferResponse::from);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferResponse> getById(@PathVariable UUID id) {
        return offerService.findById(id)
            .map(OfferResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
