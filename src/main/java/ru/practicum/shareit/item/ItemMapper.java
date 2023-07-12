package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.ArrayList;
import java.util.List;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId() != null ? item.getRequestId() : null
        );
    }

    public static List<ItemDto> toItemDto(List<Item> items) {
        List<ItemDto> itemDtos = new ArrayList<>();
        items.forEach(item -> {
            itemDtos.add(toItemDto(item));
        });
        return itemDtos;
    }

    public static Item toItem(Long ownerId, ItemDto itemDto) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                ownerId,
                itemDto.getRequestId()
        );
    }
}
