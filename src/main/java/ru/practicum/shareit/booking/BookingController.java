package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@AllArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    @PostMapping
    public BookingDto create(@NotNull @Valid @RequestBody BookingInputDto bookingDto,
                             @RequestHeader(value = "X-Sharer-User-Id") Long userId){
        return bookingService.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto update(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                             @RequestParam Boolean approved,
                             @PathVariable Long bookingId){
        return bookingService.update(userId, approved, bookingId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                     @PathVariable Long bookingId){
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getBookings(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                        @RequestParam(required = false) String state){
        return bookingService.getBookings(userId, state);
    }
    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwner(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                               @RequestParam(required = false) String state){
        return bookingService.getBookingsByOwner(userId, state);
    }
}
