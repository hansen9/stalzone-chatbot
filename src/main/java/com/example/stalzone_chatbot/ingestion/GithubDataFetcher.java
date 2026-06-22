package com.example.stalzone_chatbot.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

    private final RestClient restApiClient;
    private final RestClient restRawClient;
    private final ObjectMapper objectMapper;
    private final String githubBaseUrl;

    // Constructor injection — the right way to inject dependencies in Spring.
    // Why not @Autowired on fields? Constructor injection makes dependencies explicit
    // and allows the class to be instantiated in tests without a Spring context.
    //
    // @Value reads from application.properties: stalzone.github.base-url
    // RestClient.Builder is auto-configured by Spring Boot — we customise it here.
    // the Constructor build 2 url from github repo for api url and raw base url, so we can use the same RestClient for both.
    public GithubDataFetcher(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${stalzone.github.repo}") String githubRepo
    ) {
        String apiUrl = "https://api.github.com/repos/" + githubRepo + "/git/trees/main?recursive=1";
        String rawBaseUrl = "https://raw.githubusercontent.com/" + githubRepo + "/refs/heads/main";
        // Configure the RestClient once at construction time — base URL, timeouts etc.
        // All requests from this class share this configuration.
        this.restApiClient = restClientBuilder
                .baseUrl(apiUrl)
                .build();
        this.restRawClient = restClientBuilder
                .baseUrl(rawBaseUrl)
                .build();
        this.objectMapper = objectMapper;
        this.githubBaseUrl = githubRepo;
    }

    public List<GameDocument> fetchItemData() throws IOException {
        log.info("Fetching item data from {}", githubBaseUrl);
        // fetch the git tree to get a list of all files in the repo
        List<String> paths = fetchTree();

        // filter the path with static predicate, only keep the path that ends with .json and contains "items"
        // X.startsWith("global/items/weapon/") && X.endsWith(".json")
        List<String> itemPaths = paths.stream()
                .filter(path -> path.startsWith("global/items/weapon/") && path.endsWith(".json"))
                .toList();
        
        // for each path, get raw content, parse, try catch, log and skip the truncated files and errors, and convert to GameDocument
        List<GameDocument> documents = new ArrayList<>();
        for (String path : itemPaths) {
            try {
                String rawContent = restRawClient.get()
                        .uri("/" + path)
                        .retrieve()
                        .body(String.class);
                JsonNode itemJson = objectMapper.readTree(rawContent);
                GameDocument doc = fetchAndParseItem(itemJson);
                if (doc.id() != null && !doc.id().isBlank()) {
                    documents.add(doc);
                } else {
                    log.warn("Skipping item with missing or blank id: {}", path);
                }
            } catch (Exception e) {
                log.error("Failed to fetch or parse item at path {}: {}", path, e.getMessage(), e);
            }
        }
        return documents;
    }

    private List<String> fetchTree() {
        // this method fetches the git tree and return a list of every file path in the repo
        try {
            String payload = restApiClient.get()
                    .uri("")
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(payload);
            JsonNode tree = root.path("tree");
            if (!tree.isArray()) {
                log.error("Expected 'tree' to be an array in GitHub API response, got: {}", tree.getNodeType());
                return List.of();
            }

            List<String> paths = new ArrayList<>();
            for (JsonNode node : tree) {
                String path = node.path("path").asText(null);
                if (path != null) {
                    paths.add(path);
                }
            }
            return paths;
        } catch (Exception e) {
            log.error("Failed to fetch git tree from GitHub: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private GameDocument fetchAndParseItem(JsonNode item) {
        // This method can be used to fetch and parse individual item files if the data is split across multiple files.
        // For now, we assume all item data is in a single JSON file that we fetch directly.
        
        return new GameDocument(
                item.path("id").asText(null),
                item.path("category").asText(null),
                item.path("name").path("key").asText(null),
                item.path("name").path("lines").path("en").asText(null),
                item.path("color").asText(null),
                item.path("infoBlocks"),
                item.toString()
        );
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
