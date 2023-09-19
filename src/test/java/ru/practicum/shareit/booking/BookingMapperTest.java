package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingMapperTest {
    private final UserService userService;
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @Test
    void toBookingDtoFromBookingList() {
        Booking booking1 = new Booking(
                1L,
                LocalDateTime.of(2023, 12, 25, 12, 0),
                LocalDateTime.of(2023, 12, 26, 12, 0),
                new Item(),
                new User(2L, "asd", "asd@asd.ew"),
                BookingStatus.WAITING
        );
        Booking booking2 = new Booking(
                2L,
                LocalDateTime.of(2023, 12, 25, 12, 0),
                LocalDateTime.of(2023, 12, 26, 12, 0),
                new Item(),
                new User(2L, "asd", "asd123@aasdвsd.ew"),
                BookingStatus.WAITING
        );
        List<Booking> bookings = new ArrayList<>(List.of(booking1, booking2));
        BookingDto bookingDto1 = new BookingDto(
                1L,
                LocalDateTime.of(2023, 12, 25, 12, 0),
                LocalDateTime.of(2023, 12, 26, 12, 0),
                new Item(),
                new User(2L, "asd", "asd@asd.ew"),
                BookingStatus.WAITING
        );

        BookingDto bookingDto2 = new BookingDto(
                2L,
                LocalDateTime.of(2023, 12, 25, 12, 0),
                LocalDateTime.of(2023, 12, 26, 12, 0),
                new Item(),
                new User(2L, "asd", "asd123@aasdвsd.ew"),
                BookingStatus.WAITING
        );
        List<BookingDto> exp = new ArrayList<>(List.of(bookingDto1, bookingDto2));
        List<BookingDto> act = BookingMapper.toBookingDto(bookings);

        assertEquals(exp.get(0), act.get(0));
        assertEquals(exp.get(1), act.get(1));
    }

    @Test
    void toBookingFromBookingDto(){
        Booking booking1 = new Booking(
                1L,
                LocalDateTime.of(2023, 12, 25, 12, 0),
                LocalDateTime.of(2023, 12, 26, 12, 0),
                new Item(),
                new User(2L, "asd", "asd@asd.ew"),
                BookingStatus.WAITING
        );

        BookingDto bookingDto1 = new BookingDto(
                1L,
                LocalDateTime.of(2023, 12, 25, 12, 0),
                LocalDateTime.of(2023, 12, 26, 12, 0),
                new Item(),
                new User(2L, "asd", "asd@asd.ew"),
                BookingStatus.WAITING
        );
        assertEquals(booking1, BookingMapper.toBooking(bookingDto1));
    }
}
