package com.example.stalcraft_chatbot.ingestion;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
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

    @Test
    void happyPath(WireMockRuntimeInfo wmInfo) throws Exception {
        int port = wmInfo.getHttpPort();
        ObjectMapper objectMapper = new ObjectMapper();
        GithubDataFetcher fetcher = new GithubDataFetcher(RestClient.builder(), objectMapper, "http://localhost:" + port);

        // Load fixture data
        String fixtureBody = new String(Files.readAllBytes(
            Paths.get("src/test/resources/fixtures/items.json")));

        stubFor(get(urlEqualTo("/items.json"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(fixtureBody)));

        List<GameDocument> result = fetcher.fetchItemData();
        
        Assert.notNull(result, "Result should not be null");
        Assert.isTrue(result.size() == 1, "Expected 1 item");
        Assert.isTrue(result.get(0).id().equals("0r2g1"), "Expected item with ID '0r2g1'");
    }
}
