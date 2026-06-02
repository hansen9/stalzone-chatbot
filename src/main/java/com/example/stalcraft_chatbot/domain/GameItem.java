package com.example.stalcraft_chatbot.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class GameItem {

    @Id
    private String id;                    // "y3q3o" — opaque, case-sensitive
    private String nameEn;                // "Prometheus Armored Suit" — for display
    private String nameKey;               // "item.arm.prometheus.name" — for lookup
    private String category;              // "armor/combat" — from path + JSON
    private String color;                 // "RANK_MASTER" — item tier/rank
    private String iconPath;              // derived: global/icons/armor/combat/y3q3o.png

    // Flat stat map — extracted from infoBlocks at ingestion time
    // Stored as JSONB in Postgres so you don't need 40 nullable columns
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Double> stats = new LinkedHashMap<>();

    private String rawJson;              // keep original — RAG needs full context

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        updateIconPath();
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameKey() {
        return nameKey;
    }

    public void setNameKey(String nameKey) {
        this.nameKey = nameKey;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        updateIconPath();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public Map<String, Double> getStats() {
        return stats;
    }

    public void setStats(Map<String, Double> stats) {
        this.stats = stats;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    @JsonProperty("name")
    private void unpackName(JsonNode nameNode) {
        if (nameNode != null && !nameNode.isMissingNode()) {
            this.nameKey = nameNode.path("key").asText(null);
            this.nameEn = nameNode.path("lines").path("en").asText(null);
        }
    }

    @JsonProperty("infoBlocks")
    private void unpackInfoBlocks(JsonNode infoBlocksNode) {
        if (infoBlocksNode == null || !infoBlocksNode.isArray()) {
            return;
        }

        Map<String, Double> extracted = new LinkedHashMap<>();
        for (JsonNode block : infoBlocksNode) {
            if (!"list".equals(block.path("type").asText())) {
                continue;
            }
            for (JsonNode element : block.path("elements")) {
                if (!"numeric".equals(element.path("type").asText())) {
                    continue;
                }
                String statKey = element.path("name").path("key").asText(null);
                if (statKey != null && !statKey.isBlank()) {
                    extracted.put(statKey, element.path("value").asDouble());
                }
            }
        }
        this.stats = extracted;
    }

    private void updateIconPath() {
        if (this.category != null && this.id != null) {
            this.iconPath = String.format("global/icons/%s/%s.png", this.category, this.id);
        }
    }
}