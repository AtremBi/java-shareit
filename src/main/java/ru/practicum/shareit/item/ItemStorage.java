package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item createItem(Item item);

    Item getItemById(Long itemId);

    List<Item> getItems();

    Item updateItem(Item item);

    void deleteItem(Long itemId);
}
