package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exeptions.AlreadyExistException;
import ru.practicum.shareit.exeptions.OwnerNotChangeException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.impl.ItemInMemory;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ItemService {
    private final ItemInMemory itemInMemory;
    private final UserService userService;

    public ItemDto createItem(Long userId, ItemDto itemDto){
        Item item = ItemMapper.toItem(userId, itemDto);
        if (!checkItemInStorage(item.getId()) && userService.getUserById(userId) != null){
            item.setOwnerId(userId);
            return ItemMapper.toItemDto(itemInMemory.createItem(item));
        } else {
            throw new AlreadyExistException("Вещь уже существует id - " + item.getId());
        }
    }

    public List<ItemDto> searchItems(String text){
        if (!text.isBlank()){
            List<ItemDto> filterItems = new ArrayList<>();
            itemInMemory.getItems().forEach(item -> {
                if (item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())
                        && item.getAvailable()){
                    filterItems.add(ItemMapper.toItemDto(item));
                }
            });
            return filterItems;
        } else {
            return new ArrayList<>();
        }
    }

    public ItemDto getItemById(Long itemId){
        if (checkItemInStorage(itemId)){
            return ItemMapper.toItemDto(itemInMemory.getItemById(itemId));
        } else {
            throw new AlreadyExistException("Вещь не найдена");
        }
    }

    public List<ItemDto> getItems(Long userId){
        List<ItemDto> items = new ArrayList<>();
        itemInMemory.getItems().forEach(item -> {
            if (item.getOwnerId().equals(userId)){
                items.add(ItemMapper.toItemDto(item));
            }
        });
        return items;
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto){
        itemDto.setId(itemId);
        Item item = ItemMapper.toItem(userId, itemDto);
        if (checkItemInStorage(item.getId()) && userService.getUserById(userId) != null
                && ownerNotChange(userId, itemId)){
            Item oldItem = ItemMapper.toItem(userId, getItemById(itemId));
            if (item.getAvailable() != null){
                oldItem.setAvailable(item.getAvailable());
            }
            if (item.getDescription() != null){
                oldItem.setDescription(item.getDescription());
            }
            if (item.getName() != null){
                oldItem.setName(item.getName());
            }
            return ItemMapper.toItemDto(itemInMemory.updateItem(oldItem));
        } else {
            throw new AlreadyExistException("Вещь не найдена");
        }
    }

    public void deleteItem(Long itemId){
        if (checkItemInStorage(itemId)){
            itemInMemory.deleteItem(itemId);
        } else {
            throw new AlreadyExistException("Вещь не найдена");
        }
    }

    private boolean ownerNotChange(Long userId, Long itemId){
        if (userId.equals(itemInMemory.getItemById(itemId).getOwnerId())){
            return true;
        } else {
            throw new OwnerNotChangeException("Владельца нельзя сменить");
        }
    }

    private boolean checkItemInStorage(Long itemId) {
        return itemInMemory.getItemById(itemId) != null;
    }
}
