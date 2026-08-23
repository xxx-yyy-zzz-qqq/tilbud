package tilbud.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tilbud.dto.CatalogResponse;
import tilbud.service.CatalogService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/catalogs")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<CatalogResponse> list(
            @RequestParam(required = false) String chain,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validTo) {
        return catalogService.search(chain, validFrom, validTo).stream()
            .map(CatalogResponse::from)
            .toList();
    }
}
