package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.exeptions.WrongUserException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;

    public ItemDto createItem(Long userId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(userId, itemDto);
        userService.getUserById(userId);
        item.setOwnerId(userId);
        return ItemMapper.toItemDto(itemStorage.createItem(item));
    }

    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return ItemMapper.toItemDto(itemStorage.searchItems(text));
    }

    public ItemDto getItemById(Long itemId) {
        Item item = itemStorage.getItemById(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь не найдена");
        }
        return ItemMapper.toItemDto(item);
    }

    public List<ItemDto> getItems(Long userId) {
        return ItemMapper.toItemDto(itemStorage.getItemsByUserId(userId));
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        itemDto.setId(itemId);
        Item item = ItemMapper.toItem(userId, itemDto);
        validateOwner(userId, itemId);
        userService.getUserById(userId);
        Item oldItem = ItemMapper.toItem(userId, getItemById(itemId));
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        return ItemMapper.toItemDto(itemStorage.updateItem(oldItem));
    }

    public void deleteItem(Long itemId) {
        getItemById(itemId);
        itemStorage.deleteItem(itemId);
    }

    private boolean validateOwner(Long userId, Long itemId) {
        if (userId.equals(itemStorage.getItemById(itemId).getOwnerId())) {
            return true;
        } else {
            throw new WrongUserException("Владельца нельзя сменить");
        }
    }
}
