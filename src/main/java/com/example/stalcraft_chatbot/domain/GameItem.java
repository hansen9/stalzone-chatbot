package com.example.stalcraft_chatbot.domain;

@Entity
public class GameItem {
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
    private Map<String, Double> stats;   // {"bullet_dmg_factor": 231.0, "weight": 30.0, ...}

    private String rawJson;              // keep original — RAG needs full context
}