package com.example.stalcraft_chatbot.ingestion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stalcraft_chatbot.repository.ItemRepository;

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