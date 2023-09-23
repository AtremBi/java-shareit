package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceIntegrationTest {
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;
    private User user;
    private UserDto userDto1;
    private UserDto userDto2;
    private ItemDto itemDto1;
    @BeforeEach
    public void setUp() {
        user = new User(300L, getRandomString(), getRandomEmail());
        userDto1 = new UserDto(301L, getRandomString(), getRandomEmail());
        userDto2 = new UserDto(302L, getRandomString(), getRandomEmail());
        itemDto1 = new ItemDto(301L, getRandomString(), getRandomString(), true,
                user.getId(), null, null, null);
    }

    @Test
    void getBookings() {
        UserDto ownerDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto1);
        BookingInputDto bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        bookingService.create(bookingInputDto, newUserDto.getId());
        BookingInputDto bookingInputDto1 = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.create(bookingInputDto1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "FUTURE",
                0, 20);
        assertEquals(2, listBookings.size());
    }
}
