package com.example.stalcraft_chatbot.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class AiConfig {
    @Value("classpath:stalcraft-x-assistant-prompt.md")
    private Resource promptsResource;

    @Bean
    public ChatClient chatClient(ChatModel chatModel) throws IOException {
        String systemPrompt = promptsResource.getContentAsString(StandardCharsets.UTF_8);
        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .build();
    }
}
