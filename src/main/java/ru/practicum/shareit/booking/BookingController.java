package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto create(@NotNull @Valid @RequestBody BookingInputDto bookingDto,
                             @RequestHeader(value = USER_ID) Long userId) {
        logInfo("create: ", "bookingDto - " + bookingDto + " userId - " + userId);
        return bookingService.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto update(@RequestHeader(value = USER_ID) Long userId,
                             @RequestParam Boolean approved,
                             @PathVariable Long bookingId) {
        logInfo("update: ", "userId - " + userId + " approved - " + approved + " bookingId - "
                + bookingId);
        return bookingService.update(userId, approved, bookingId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader(value = USER_ID) Long userId,
                                     @PathVariable Long bookingId) {
        logInfo("getBookingById: ", "userId " + userId + " bookingId - " + bookingId);
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getBookings(@RequestHeader(value = USER_ID) Long userId,
                                        @RequestParam(defaultValue = "ALL") String state,
                                        @RequestParam(name = "from", defaultValue = "0") Integer from,
                                        @RequestParam(name = "size", defaultValue = "20") Integer size) {
        logInfo("getBookings: ", "userId - " + userId + " state - " + state);
        return bookingService.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwner(@RequestHeader(value = USER_ID) Long userId,
                                               @RequestParam(defaultValue = "ALL") String state,
                                               @RequestParam(name = "from", defaultValue = "0") Integer from,
                                               @RequestParam(name = "size", defaultValue = "20") Integer size) {
        logInfo("getBookingsByOwner: ", "userId - " + userId + " state - " + state);
        return bookingService.getBookingsByOwner(userId, state, from, size);
    }

    private void logInfo(String method, String additionalInfo) {
        log.info("Запрос - " + method + " " + additionalInfo);
    }
}
