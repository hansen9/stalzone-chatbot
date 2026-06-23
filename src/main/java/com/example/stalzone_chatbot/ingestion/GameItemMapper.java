package com.example.stalzone_chatbot.ingestion;

import com.example.stalzone_chatbot.domain.GameItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

// GameItemMapper — single responsibility: translate a GameDocument into a GameItem entity.
// Nothing else. No fetching, no saving, no AI logic.
//
// Why a separate class instead of a static method?
//   Because it depends on ObjectMapper (a Spring bean) for serialising stats to JSON.
//   Spring manages its lifecycle, and we can mock it cleanly in tests.
@Component
@RequiredArgsConstructor  // Lombok: generates constructor for all 'final' fields — clean DI pattern
@Slf4j                    // Lombok: gives us a 'log' field without boilerplate
public class GameItemMapper {

    // Injected by Spring — this is the globally configured ObjectMapper,
    // so date formats, null handling etc. are consistent across the whole app.
    private final ObjectMapper objectMapper;

    public GameItem toEntity(GithubDataFetcher.GameDocument doc, Map<String, Double> stats) {
        GameItem item = new GameItem();

        item.setId(doc.id());
        item.setNameEn(doc.nameEn());
        item.setNameKey(doc.nameKey());
        item.setCategory(doc.category());
        item.setColor(doc.color());
        item.setRawJson(doc.rawJson());

        // iconPath is derived — compute it here rather than storing it in GameDocument.
        // Convention: if both fields are present, build the path; otherwise leave null.
        if (doc.category() != null && doc.id() != null) {
            item.setIconPath(String.format("global/icons/%s/%s.png", doc.category(), doc.id()));
        }

        // Serialise the flat stats map to a JSON string for TEXT column storage.
        // This is the H2 workaround — see the TODO in GameItem for the Postgres upgrade path.
        item.setStatsJson(serialiseStats(stats));

        return item;
    }

    private String serialiseStats(Map<String, Double> stats) {
        if (stats == null || stats.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(stats);
        } catch (JsonProcessingException e) {
            // This should never happen for a Map<String, Double>, but we log it rather
            // than crashing the entire ingestion run over one bad item.
            log.warn("Failed to serialise stats map, storing empty object. Cause: {}", e.getMessage());
            return "{}";
        }
    }
}
