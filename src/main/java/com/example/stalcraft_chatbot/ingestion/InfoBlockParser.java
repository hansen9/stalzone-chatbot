package com.example.stalcraft_chatbot.ingestion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// InfoBlockParser — owns all logic for reading the infoBlocks structure from GitHub JSON.
// Two responsibilities:
//   1. Deserialise a raw JsonNode into typed InfoBlock objects (parseInfoBlocks)
//   2. Extract specific stats from those typed objects (extractNumericStat, extractAllNumericStats)
@Component
@RequiredArgsConstructor
@Slf4j
public class InfoBlockParser {

    // Injected ObjectMapper — consistent config, and enables testing with a mock
    private final ObjectMapper objectMapper;

    // Entry point for the ingestion pipeline.
    // Takes the raw infoBlocks JsonNode from GithubDataFetcher.GameDocument
    // and deserialises it into a typed list we can work with.
    public List<InfoBlock> parseInfoBlocks(JsonNode infoBlocksNode) {
        if (infoBlocksNode == null || !infoBlocksNode.isArray() || infoBlocksNode.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            // ObjectMapper reads the JSON array into a List<InfoBlock>.
            // @JsonIgnoreProperties on each inner class handles unknown fields gracefully.
            return objectMapper.readerForListOf(InfoBlock.class).readValue(infoBlocksNode);
        } catch (Exception e) {
            // Don't crash ingestion over one item's malformed infoBlocks
            log.warn("Failed to parse infoBlocks: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // Extract a specific stat by its stable key
    // e.g. key = "stalker.artefact_properties.factor.bullet_dmg_factor"
    public Optional<Double> extractNumericStat(List<InfoBlock> blocks, String statKey) {
        return blocks.stream()
                .filter(b -> "list".equals(b.getType()))
                .flatMap(b -> b.getElements().stream())
                .filter(e -> "numeric".equals(e.getType()))
                .filter(e -> e.getName() != null && statKey.equals(e.getName().getKey()))
                .map(Element::getValue)
                .findFirst();
    }

    // Extract ALL numeric stats as a flat map — used by GameItemMapper and the comparator tool
    // Map<statKey, value>  e.g. {"bullet_dmg_factor" -> 231.0, "weight" -> 30.0}
    public Map<String, Double> extractAllNumericStats(List<InfoBlock> blocks) {
        return blocks.stream()
                .filter(b -> "list".equals(b.getType()))
                .flatMap(b -> b.getElements().stream())
                .filter(e -> "numeric".equals(e.getType()))
                .filter(e -> e.getName() != null && e.getName().getKey() != null)
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        e -> e.getName().getKey(),
                        Element::getValue,
                        (a, b) -> a  // keep first on key collision — same stat in multiple blocks
                ));
    }

    // -------------------------------------------------------------------------
    // Inner classes — typed representation of the GitHub infoBlocks structure.
    // @Data (Lombok) generates getters, setters, equals, hashCode, toString.
    // @JsonIgnoreProperties(ignoreUnknown = true) silently skips unknown fields —
    // important because the game schema has fields we don't need.
    // -------------------------------------------------------------------------

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoBlock {
        private String type;
        private InfoBlockTitle title;
        private List<Element> elements = Collections.emptyList();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoBlockTitle {
        private String type;
        private String text;
        private String key;
        private Map<String, String> lines;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Element {
        private String type;
        private ElementName key;
        private ElementName name;
        private Formatted formatted;

        // value can be a number, string, or boolean in the GitHub data.
        // We store it as Object and coerce to Double on read.
        private Object value;

        @JsonProperty("value")
        public void setValue(Object value) {
            this.value = value;
        }

        // Coerce to Double — handles Integer, Long, Float, Double from Jackson
        public Double getValue() {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return null;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ElementName {
        private String type;
        private String key;
        private Map<String, String> lines;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Formatted {
        private Map<String, String> value;
        private String nameColor;
        private String valueColor;
    }
}