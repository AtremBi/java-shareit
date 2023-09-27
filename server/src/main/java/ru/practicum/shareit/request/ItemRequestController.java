package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService service;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto addRequest(@RequestHeader(value = USER_ID) Long requestorId,
                                     @RequestBody ItemRequestDto itemRequestDto) {
        logInfo("addRequest: ", "requestorId - " + requestorId + " itemRequestDto - "
                + itemRequestDto);
        return service.createRequest(requestorId, itemRequestDto, LocalDateTime.now());
    }

    @GetMapping
    public List<ItemRequestDto> getRequests(@RequestHeader(value = USER_ID) Long requestorId) {
        logInfo("getRequests: ", "requestorId - " + requestorId);
        return service.getRequests(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getRequests(@RequestHeader(value = USER_ID) Long requestorId,
                                            @RequestParam(name = "from", defaultValue = "0") Integer from,
                                            @RequestParam(name = "size", defaultValue = "20") Integer size) {
        logInfo("getRequests: ", "requestorId - " + requestorId);
        return service.getAllRequests(from, size, requestorId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@PathVariable Long requestId,
                                         @RequestHeader(value = USER_ID) Long userId) {
        logInfo("getRequestById: ", " requestId - " + requestId + " userId - " + userId);
        return service.getRequestById(requestId, userId);
    }

    private void logInfo(String method, String additionalInfo) {
        log.info("Запрос - " + method + " " + additionalInfo);
    }
}
