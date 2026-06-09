package com.example.stalcraft_chatbot.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import com.example.stalcraft_chatbot.tools.ItemLookUpTool;

@Service
public class ChatService {
    private final ChatClient chatClient;
    private final ItemLookUpTool itemLookUpTool;
    private final ChatMemory chatMemory;

    public ChatService(
        ChatClient chatClient,
        ItemLookUpTool itemLookUpTool,
        ChatMemory chatMemory
    ){
        this.chatClient = chatClient;
        this.itemLookUpTool = itemLookUpTool;
        this.chatMemory = chatMemory;
    }

    public String chat(String chatId, String userMessage) {
        
        String response = chatClient.prompt()
            .user(userMessage)
            .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(chatId)
                .build())
            .tools(itemLookUpTool)
            .call()
            .content();

        // For now, let's just return a simple response.
        return response;
    }
}