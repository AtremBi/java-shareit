package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.ServiceUtil;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class ItemMapper {
    private final ServiceUtil serviceUtil;

    public ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId() != null ? item.getRequestId() : null,
                null,
                null,
                serviceUtil.getItemService().getCommentsByItemId(item.getId())
        );
    }

    public ItemDto toItemDtoWithLastAndEndNextBooking(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId() != null ? item.getRequestId() : null,
                serviceUtil.getBookingService().getLastBooking(item.getId()),
                serviceUtil.getBookingService().getNextBooking(item.getId()),
                serviceUtil.getItemService().getCommentsByItemId(item.getId())
        );
    }

    public List<ItemDto> toItemDtoWithLastAndEndNextBooking(List<Item> items) {
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            toItemDtoWithLastAndEndNextBooking(item);
            itemDtos.add(toItemDtoWithLastAndEndNextBooking(item));
        }
        return itemDtos;

    }

    public List<ItemDto> toItemDto(List<Item> items) {
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            itemDtos.add(toItemDto(item));
        }
        return itemDtos;
    }

    public Item toItem(Long ownerId, ItemDto itemDto) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                ownerId,
                itemDto.getRequestId()
        );
    }

    public CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                toItemDto(comment.getItem()),
                comment.getAuthor().getName(),
                comment.getCreated()
        );

    }
}
