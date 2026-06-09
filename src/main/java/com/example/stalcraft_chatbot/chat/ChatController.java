package com.example.stalcraft_chatbot.chat;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stalcraft_chatbot.chat.dto.ChatRequest;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // Define your endpoints here, e.g., for sending messages, retrieving chat history, etc.
    @PostMapping("/send")
    public String sendMessage(@RequestBody ChatRequest message) {
        
        chatService.chat(message.chatId(), message.message());
        return "Message sent successfully"; 
    }
}
