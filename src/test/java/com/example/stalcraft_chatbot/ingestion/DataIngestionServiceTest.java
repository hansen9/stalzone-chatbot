package com.example.stalcraft_chatbot.ingestion;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stalcraft_chatbot.repository.ItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

@ExtendWith(MockitoExtension.class)
class DataIngestionServiceTest {

    @Mock private GithubDataFetcher githubDataFetcher;
    @Mock private InfoBlockParser infoBlockParser;
    @Mock private GameItemMapper gameItemMapper;
    @Mock private ItemRepository itemRepository;

    @InjectMocks
    private DataIngestionService dataIngestionService;

    private GithubDataFetcher.GameDocument validDoc;
    private GithubDataFetcher.GameDocument nullIdDoc;
    
    @BeforeEach
    void setUp() {
        // Build validDoc and nullIdDoc here
        // Hint: GameDocument is a record — look at its fields
        validDoc = new GithubDataFetcher.GameDocument(
            JsonNodeFactory.instance.numberNode(1).asText(), // id
            "weapon", // category
            "item.sword.name", // nameKey
            "Sword of Testing", // nameEn
            "blue", // color
            JsonNodeFactory.instance.arrayNode().add("damage: 10").add("durability: 100"), // infoBlocks
            "{\"id\": 1, \"category\": \"weapon\", \"nameKey\": \"item.sword.name\"}" // rawJson
        );
        nullIdDoc = new GithubDataFetcher.GameDocument(
            null, // id is null
            "armor",
            "item.shield.name",
            "Shield of Nulls",
            "red",
            JsonNodeFactory.instance.arrayNode().add("defense: 20").add("durability: 150"),
            "{\"id\": null, \"category\": \"armor\", \"nameKey\": \"item.shield.name\"}"
        );
    }

    @Test
    void shouldSaveItemsForValidDocuments() {
        // 1. stub all three mocks to return something sensible
        
        // 2. call ingestItems()
        // 3. verify saveAll was called with the right items
    }

    @Test
    void shouldFilterOutDocumentsWithNullId() {
        // 1. stub fetcher to return List.of(validDoc, nullIdDoc)
        // 2. stub parser and mapper for the valid doc
        // 3. call ingestItems()
        // 4. use ArgumentCaptor to assert only 1 item reached saveAll
    }
}