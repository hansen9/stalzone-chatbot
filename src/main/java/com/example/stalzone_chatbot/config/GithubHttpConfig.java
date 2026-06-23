package com.example.stalzone_chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GithubHttpConfig {
    @Value("${stalzone.github.repo}")
    private String githubRepo;

    @Value("${stalzone.github.api-host}")
    private String githubApiHost;

    @Value("${stalzone.github.raw-host}")
    private String githubRawHost;
    
    @Bean
    public RestClient githubApiClient(RestClient.Builder builder) {
        return builder
                .baseUrl(githubApiHost + "/repos/" + githubRepo)
                .build();
    }

    @Bean
    public RestClient githubRawClient(RestClient.Builder builder) {
        return builder
                .baseUrl(githubRawHost + "/" + githubRepo + "/refs/heads/main")
                .build();
    }
}
