package com.example.stalcraft_chatbot.repository;

import com.example.stalcraft_chatbot.domain.GameItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// JpaRepository<T, ID>:
//   T  = the entity type this repository manages
//   ID = the Java type of the @Id field on that entity
//
// By extending JpaRepository, Spring Data auto-generates the implementation at runtime.
// You get save(), saveAll(), findById(), findAll(), count(), deleteAll() etc. for free —
// no SQL, no boilerplate.
@Repository
public interface ItemRepository extends JpaRepository<GameItem, String> {

    // Spring Data derives the SQL from the method name — no @Query needed for simple cases.
    // "findBy" + "Category" → WHERE category = ?
    List<GameItem> findByCategory(String category);

    // Case-insensitive name search — useful for the chatbot's item lookup tool
    // "IgnoreCase" → LOWER(name_en) = LOWER(?)
    Optional<GameItem> findByNameEnIgnoreCase(String nameEn);

    // Used by the future TODO: skip ingestion if data already exists
    // count() is inherited from JpaRepository — no need to declare it here
}
