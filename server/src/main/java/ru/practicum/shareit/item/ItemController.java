package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto createItems(@RequestHeader(USER_ID) Long userId,
                               @Valid @RequestBody ItemDto itemDto) {
        logInfo("createItems: ", "user - " + userId + "\n itemDto - " + itemDto);
        return itemService.createItem(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(value = USER_ID) Long userId,
                               @PathVariable Long itemId) {
        logInfo("getItemById: ", "userId - " + userId + " itemId - " + itemId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(name = "text") String text,
                                     @RequestParam(name = "from", defaultValue = "0") Integer from,
                                     @RequestParam(name = "size", defaultValue = "20") Integer size) {
        logInfo("searchItems: ", "searchQuery " + text);
        return itemService.searchItems(text, from, size);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader(value = USER_ID) Long userId,
                                  @RequestParam(name = "from", defaultValue = "0") Integer from,
                                  @RequestParam(name = "size", defaultValue = "20") Integer size) {
        logInfo("getItems: ", "userId - " + userId);
        return itemService.getItems(userId, from, size);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(value = USER_ID) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        logInfo("updateItem: ", "userId - " + userId + " itemId -" + itemId + " itemDto -" + itemDto);
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        logInfo("deleteItem: ", "itemId - " + itemId);
        itemService.deleteItem(itemId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@Valid @RequestBody CommentDto commentDto,
                                    @RequestHeader(USER_ID) Long userId,
                                    @PathVariable Long itemId) {
        logInfo("createComment: ", "commentDto " + commentDto + " userId - " + userId + " itemId - "
                + itemId);
        return itemService.addComment(commentDto, itemId, userId);
    }

    private void logInfo(String method, String additionalInfo) {
        log.info("Запрос - " + method + ". " + additionalInfo);
    }
}
