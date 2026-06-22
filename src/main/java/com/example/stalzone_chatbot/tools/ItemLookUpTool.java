package com.example.stalzone_chatbot.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.example.stalzone_chatbot.repository.ItemRepository;

@Component
public class ItemLookUpTool {
    private final ItemRepository itemRepository;
    public ItemLookUpTool(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Tool(description = "Look up an item by its English name. Case-insensitive.")
    public String itemLookUp(String itemName) {
        // if the item is found, return a string with its details; otherwise, return a not-found message
        return itemRepository.findByNameEnIgnoreCase(itemName)
            .map(item -> String.format("Item: %s\nCategory: %s\nColor: %s\nStats: %s",
                    item.getNameEn(), item.getCategory(), item.getColor(), item.getStatsJson()))
            .orElse(String.format("Sorry, I couldn't find an item named '%s'.", itemName));
    }
}
