package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService service;

    @PostMapping
    public ItemRequestDto addRequest(@RequestHeader(value = "X-Sharer-User-Id") Long requestorId,
                                     @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return service.createRequest(requestorId, itemRequestDto, LocalDateTime.now());
    }

    @GetMapping
    public List<ItemRequestDto> getRequests(@RequestHeader(value = "X-Sharer-User-Id") Long requestorId) {
        return service.getRequests(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getRequests(@RequestHeader(value = "X-Sharer-User-Id") Long requestorId,
                                            @RequestParam(name = "from", defaultValue = "0") Integer from,
                                            @RequestParam(name = "size", defaultValue = "20") Integer size) {
        return service.getAllRequests(from, size, requestorId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@PathVariable Long requestId,
                                         @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return service.getRequestById(requestId, userId);
    }
}
