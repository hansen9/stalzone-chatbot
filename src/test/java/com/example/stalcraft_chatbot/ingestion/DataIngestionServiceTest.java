package com.example.stalcraft_chatbot.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stalcraft_chatbot.domain.GameItem;
import com.example.stalcraft_chatbot.repository.ItemRepository;
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
            "0r2g1",
            "weapon/assault_rifle",
            "item.wpn.9a91.name",
            "9A-91",
            "RANK_VETERAN",
            JsonNodeFactory.instance.arrayNode().add("damage: 33.9").add("durability: 100"),
            "{\"id\": \"0r2g1\", \"category\": \"weapon/assault_rifle\", \"nameKey\": \"item.wpn.9a91.name\"}"
        );
        nullIdDoc = new GithubDataFetcher.GameDocument(
            null,
            "weapon/assault_rifle",
            "item.wpn.9a91.name",
            "9A-91",
            "RANK_VETERAN",
            JsonNodeFactory.instance.arrayNode().add("damage: 33.9").add("durability: 100"),
            "{\"id\": null, \"category\": \"weapon/assault_rifle\", \"nameKey\": \"item.wpn.9a91.name\"}"
        );
    }

    @Test
    void shouldSaveItemsForValidDocuments() throws Exception{
        
        List<InfoBlockParser.InfoBlock> mockInfoBlocks = List.of(); // assuming infoBlocks is an array of strings for simplicity
        
        // 1. stub all three mocks to return something sensible
        when(githubDataFetcher.fetchItemData()).thenReturn(List.of(validDoc));
        when(infoBlockParser.parseInfoBlocks(validDoc.infoBlocks())).thenReturn(mockInfoBlocks);
        when(infoBlockParser.extractAllNumericStats(mockInfoBlocks)).thenReturn(Map.of());
        when(gameItemMapper.toEntity(validDoc, Map.of())).thenReturn(new GameItem());

        // 2. call ingestItems()
        dataIngestionService.ingestItems();

        // 3. verify saveAll was called with the right items
        // Note: you may want to use ArgumentCaptor here to capture the argument passed to saveAll and assert on it
        // assert
        ArgumentCaptor<List<GameItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(itemRepository).saveAll(captor.capture());  // capture happens HERE
        assertThat(captor.getValue()).hasSize(1);           // inspect AFTER capture
    }

    @Test
    void shouldFilterOutDocumentsWithNullId() throws Exception {
        List<InfoBlockParser.InfoBlock> mockInfoBlocks = List.of(); // assuming infoBlocks is an array of strings for simplicity

        // 1. stub fetcher to return List.of(validDoc, nullIdDoc)
        when(githubDataFetcher.fetchItemData()).thenReturn(List.of(validDoc, nullIdDoc));

        // 2. stub parser and mapper for the valid doc
        when(infoBlockParser.parseInfoBlocks(validDoc.infoBlocks())).thenReturn(mockInfoBlocks);
        when(infoBlockParser.extractAllNumericStats(mockInfoBlocks)).thenReturn(Map.of());
        when(gameItemMapper.toEntity(validDoc, Map.of())).thenReturn(new GameItem());
        
        // 3. call ingestItems()
        dataIngestionService.ingestItems();

        // 4. use ArgumentCaptor to assert only 1 item reached saveAll
        // assert
        ArgumentCaptor<List<GameItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(itemRepository).saveAll(captor.capture());  // capture happens HERE
        assertThat(captor.getValue()).hasSize(1);           // inspect AFTER capture
    }
}