package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.Item;

import java.util.List;

public interface ItemStorage {
    Item createItem(Item item);

    Item getItemById(Long itemId);

    List<Item> getItems();

    Item updateItem(Item item);

    void deleteItem(Long itemId);

    List<Item> searchItems(String text);

    List<Item> getItemsByUserId(Long userId);
}
