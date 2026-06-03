package com.example.stalcraft_chatbot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


// GameItem is ONLY a JPA entity — a database record.
// No JSON parsing, no business logic. Those live in InfoBlockParser and GameItemMapper.
@Entity
@Getter
@Setter
@NoArgsConstructor  // JPA requires a no-arg constructor to instantiate entities via reflection
public class GameItem {

    @Id
    private String id;          // "y3q3o" — opaque, case-sensitive

    private String nameEn;      // "Prometheus Armored Suit" — for display
    private String nameKey;     // "item.arm.prometheus.name" — for lookup
    private String category;    // "armor/combat" — from path + JSON
    private String color;       // "RANK_MASTER" — item tier/rank
    private String iconPath;    // derived: global/icons/armor/combat/y3q3o.png

    // Stats stored as a serialised string for H2 compatibility.
    // TODO: when migrating to Postgres, change this to:
    //   @JdbcTypeCode(SqlTypes.JSON)
    //   @Column(columnDefinition = "jsonb")
    // That removes the need for manual serialisation entirely.
    @Column(columnDefinition = "TEXT")
    private String statsJson;   // flat stat map serialised as JSON string

    @Column(columnDefinition = "TEXT")
    private String rawJson;     // original GitHub payload — RAG needs full context
}