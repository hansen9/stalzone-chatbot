package com.example.stalcraft_chatbot.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// GithubDataFetcher — single responsibility: fetch raw JSON from GitHub and
// convert it into GameDocuments. No parsing of stats, no DB writes.
@Service
@Slf4j
public class GithubDataFetcher {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String githubBaseUrl;

    // Constructor injection — the right way to inject dependencies in Spring.
    // Why not @Autowired on fields? Constructor injection makes dependencies explicit
    // and allows the class to be instantiated in tests without a Spring context.
    //
    // @Value reads from application.properties: stalcraft.github.base-url
    // RestClient.Builder is auto-configured by Spring Boot — we customise it here.
    public GithubDataFetcher(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${stalcraft.github.base-url}") String githubBaseUrl
    ) {
        // Configure the RestClient once at construction time — base URL, timeouts etc.
        // All requests from this class share this configuration.
        this.restClient = restClientBuilder
                .baseUrl(githubBaseUrl)
                .build();
        this.objectMapper = objectMapper;
        this.githubBaseUrl = githubBaseUrl;
    }

    public List<GameDocument> fetchItemData() throws IOException {
        log.info("Fetching item data from {}", githubBaseUrl);

        // Path is relative to the base URL set in the constructor
        String payload = restClient.get()
                .uri("/items.json")
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(payload);
        if (!root.isArray()) {
            throw new IOException("Expected items JSON array from GitHub, got: " + root.getNodeType());
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

        log.info("Fetched {} items from GitHub", documents.size());
        return documents;
    }

    // A record is perfect here — it's immutable, has equals/hashCode/toString for free,
    // and signals clearly that this is a data carrier, not a behaviour class.
    public record GameDocument(
            String id,
            String category,
            String nameKey,
            String nameEn,
            String color,
            JsonNode infoBlocks,
            String rawJson
    ) {}
}
