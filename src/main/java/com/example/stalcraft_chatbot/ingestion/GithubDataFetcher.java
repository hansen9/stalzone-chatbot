package com.example.stalcraft_chatbot.ingestion;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// GithubDataFetcher.java — fetch raw JSON/YAML from the game data repo
@Service
public class GithubDataFetcher {

    private static final String GITHUB_ITEMS_URL = "https://raw.githubusercontent.com/<org>/<repo>/main/items.json";
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<GameDocument> fetchItemData() throws IOException {
        String payload = restClient.get().uri(GITHUB_ITEMS_URL).retrieve().body(String.class);
        JsonNode root = objectMapper.readTree(payload);
        if (!root.isArray()) {
            throw new IOException("Expected items JSON array from GitHub");
        }

        List<GameDocument> documents = new ArrayList<>();
        Iterator<JsonNode> elements = root.iterator();
        while (elements.hasNext()) {
            JsonNode item = elements.next();
            String id = item.path("id").asText(null);
            String category = item.path("category").asText(null);
            JsonNode nameNode = item.path("name");
            String nameKey = nameNode.path("key").asText(null);
            String nameEn = nameNode.path("lines").path("en").asText(null);
            String color = item.path("color").asText(null);
            JsonNode infoBlocks = item.path("infoBlocks");
            documents.add(new GameDocument(id, category, nameKey, nameEn, color, infoBlocks, item.toString()));
        }
        return documents;
    }

    public static record GameDocument(
            String id,
            String category,
            String nameKey,
            String nameEn,
            String color,
            JsonNode infoBlocks,
            String rawJson
    ) {
    }
}
