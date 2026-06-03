package com.example.stalcraft_chatbot.ingestion;

import com.example.stalcraft_chatbot.domain.GameItem;
import com.example.stalcraft_chatbot.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

// DataIngestionService — orchestrates the full ingestion pipeline:
//   fetch → parse → map → save
//
// It knows WHAT to do and in what order.
// It delegates HOW to each specialist: GithubDataFetcher, InfoBlockParser, GameItemMapper.
// This is the "conductor" pattern — thin orchestration, no business logic of its own.
@Service
@RequiredArgsConstructor
@Slf4j
public class DataIngestionService {

    private final GithubDataFetcher githubDataFetcher;
    private final InfoBlockParser infoBlockParser;
    private final GameItemMapper gameItemMapper;
    private final ItemRepository itemRepository;

    // ApplicationReadyEvent fires after the full Spring context is loaded —
    // all beans are initialised, DB schema is created by Hibernate.
    // Safer than @PostConstruct, which can fire before JPA is fully ready.
    @EventListener(ApplicationReadyEvent.class)
    public void ingestOnStartup() {
        log.info("=== Starting Stalcraft data ingestion ===");
        try {
            ingestItems();
        } catch (Exception e) {
            // Log and continue — a failed ingestion shouldn't crash the whole app.
            // The app starts with an empty DB; features that need data will return empty results.
            log.error("Ingestion failed: {}", e.getMessage(), e);
        }
        log.info("=== Ingestion complete. Items in DB: {} ===", itemRepository.count());
    }

    // @Transactional: wraps the entire saveAll in a single DB transaction.
    // If anything fails mid-batch, the whole operation rolls back cleanly.
    // Without this, you could end up with a half-ingested dataset.
    @Transactional
    public void ingestItems() throws Exception {
        List<GithubDataFetcher.GameDocument> documents = githubDataFetcher.fetchItemData();
        log.info("Fetched {} documents from GitHub", documents.size());

        // TODO (Postgres migration): replace this block with:
        //   if (itemRepository.count() > 0) {
        //       log.info("Items already present, skipping ingestion");
        //       return;
        //   }
        // This avoids re-ingesting on every restart once the DB persists between runs.

        List<GameItem> items = documents.stream()
                .filter(doc -> doc.id() != null && !doc.id().isBlank())  // skip malformed entries
                .map(doc -> {
                    // InfoBlockParser needs a typed list — deserialise infoBlocks node first
                    List<InfoBlockParser.InfoBlock> infoBlocks = infoBlockParser
                            .parseInfoBlocks(doc.infoBlocks());

                    // Extract flat stats map from the typed infoBlocks
                    Map<String, Double> stats = infoBlockParser
                            .extractAllNumericStats(infoBlocks);

                    // Translate to entity — GameItemMapper owns this logic
                    return gameItemMapper.toEntity(doc, stats);
                })
                .toList();  // Java 16+ — produces an unmodifiable list

        // saveAll with batching is far more efficient than calling save() in a loop.
        // Each save() is a separate DB round-trip; saveAll() can batch them.
        // To enable actual JDBC batching, add to application.properties:
        //   spring.jpa.properties.hibernate.jdbc.batch_size=50
        //   spring.jpa.properties.hibernate.order_inserts=true
        itemRepository.saveAll(items);
        log.info("Saved {} items to database", items.size());
    }
}
