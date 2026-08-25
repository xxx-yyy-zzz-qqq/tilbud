package tilbud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tilbud.service.OfferIngestionService;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {

    private final OfferIngestionService ingestionService;

    public IngestionController(OfferIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> trigger() {
        if (ingestionService.isRunning()) {
            return ResponseEntity.status(409).body(Map.of(
                "error", "Fetch already in progress"
            ));
        }

        ingestionService.fetchOnStartup();

        return ResponseEntity.accepted().body(Map.of(
            "status", "started",
            "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
            "running", ingestionService.isRunning(),
            "lastRun", ingestionService.getLastRunTime() != null
                ? ingestionService.getLastRunTime().toString()
                : null,
            "lastRunResult", Map.of(
                "chainsProcessed", ingestionService.getLastChainsProcessed(),
                "catalogsProcessed", ingestionService.getLastCatalogsProcessed(),
                "offersInserted", ingestionService.getLastOffersInserted(),
                "errors", ingestionService.getLastErrors()
            )
        ));
    }
}
