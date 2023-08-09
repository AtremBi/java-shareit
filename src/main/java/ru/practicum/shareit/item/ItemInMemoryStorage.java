package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ItemInMemoryStorage implements ItemStorage {
    private Long id = 1L;
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item createItem(Item item) {
        item.setId(id++);
        items.put(item.getId(), item);
        return items.get(item.getId());
    }

    @Override
    public Item getItemById(Long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        return items.put(item.getId(), item);
    }

    @Override
    public void deleteItem(Long itemId) {
        items.remove(itemId);
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<Item> filterItems = new ArrayList<>();
        getItems().forEach(item -> {
            if (item.getName().toLowerCase().contains(text.toLowerCase())
                    || item.getDescription().toLowerCase().contains(text.toLowerCase())
                    && item.getIsAvailable()) {
                filterItems.add(item);
            }
        });
        return filterItems;
    }

    @Override
    public List<Item> getItemsByUserId(Long userId) {
        List<Item> items = new ArrayList<>();
        getItems().forEach(item -> {
            if (item.getOwnerId().equals(userId)) {
                items.add(item);
            }
        });
        return items;
    }
}
