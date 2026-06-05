package com.example.stalcraft_chatbot.ingestion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.springframework.web.client.RestClient;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import com.example.stalcraft_chatbot.ingestion.GithubDataFetcher.GameDocument;
import com.fasterxml.jackson.databind.ObjectMapper;

@WireMockTest
class GithubDataFetcherTest {
    private GithubDataFetcher fetcher;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmInfo) {
        fetcher = new GithubDataFetcher(
            RestClient.builder(),
            new ObjectMapper(),
            "http://localhost:" + wmInfo.getHttpPort()
        );
    }   

    @Test
    void happyPath(WireMockRuntimeInfo wmInfo) throws Exception {

        // Load fixture data
        String fixtureBody = new String(
            getClass().getResourceAsStream("/fixtures/items.json").readAllBytes()
        );

        stubFor(get(urlEqualTo("/items.json"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(fixtureBody)));

        List<GameDocument> result = fetcher.fetchItemData();
        
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo("0r2g1");
    }

    @Test
    void NonArrayResponse(WireMockRuntimeInfo wmInfo) throws Exception {

        stubFor(get(urlEqualTo("/items.json"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{}")));
        
        assertThatThrownBy(() -> fetcher.fetchItemData())
            .isInstanceOf(IOException.class)
            .hasMessageContaining("OBJECT");
    }

    @Test
    void EmptyArray(WireMockRuntimeInfo wmInfo) throws Exception {

        stubFor(get(urlEqualTo("/items.json"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        List<GameDocument> result = fetcher.fetchItemData();
        
        assertThat(result).isEmpty();  // cleaner than isNullOrEmpty + hasSize(0) — pick one
    }
}
