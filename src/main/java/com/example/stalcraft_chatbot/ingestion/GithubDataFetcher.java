package com.example.stalcraft_chatbot.ingestion;

// GithubDataFetcher.java — fetch raw JSON/YAML from the game data repo
@Service
public class GithubDataFetcher {
    
    private final RestClient restClient; // Spring 6 replacement for RestTemplate
    
    public List<GameDocument> fetchItemData() {
        // Fetch from raw.githubusercontent.com/{repo}/items.json
        // Return as structured documents ready for chunking
    }
}
