package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.stream.Collectors;


@UtilityClass
public class BookingMapper {
    public ShortBookingDto toShortBookingDto(Booking booking) {
        if (booking != null) {
            return new ShortBookingDto(
                    booking.getId(),
                    booking.getBooker().getId(),
                    booking.getStart(),
                    booking.getEnd()
            );
        }
        return null;
    }

    public BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem(),
                booking.getBooker(),
                booking.getStatus()
        );
    }

    public List<BookingDto> toBookingDto(List<Booking> bookings) {
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    public Booking toBooking(BookingDto bookingDto) {
        return new Booking(
                bookingDto.getId(),
                bookingDto.getStart(),
                bookingDto.getEnd(),
                bookingDto.getItem(),
                bookingDto.getBooker(),
                bookingDto.getStatus()
        );
    }

    public Booking toBooking(Long bookerId, BookingInputDto booking,
                             ItemService itemService, UserService userService) {
        return new Booking(
                null,
                booking.getStart(),
                booking.getEnd(),
                itemService.findItemById(booking.getItemId()),
                userService.findUserById(bookerId),
                BookingStatus.WAITING
        );
    }
}
