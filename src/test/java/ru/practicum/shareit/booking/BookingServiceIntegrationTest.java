package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

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
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private User user;
    private User user1;
    private Item item1;

    @BeforeEach
    public void setUp() {
        user = new User(null, getRandomString(), getRandomEmail());
        user1 = new User(null, getRandomString(), getRandomEmail());
        item1 = new Item(null, getRandomString(), getRandomString(), true,
                null, null);
    }

    @Test
    void getBookings() {
        UserDto ownerDto = UserMapper.toUserDto(userRepository.save(user));
        UserDto newUserDto = UserMapper.toUserDto(userRepository.save(user1));
        item1.setOwnerId(ownerDto.getId());
        ItemDto newItemDto = itemMapper.toItemDto(itemRepository.save(item1));
        bookingRepository.save((new Booking(
                1L,
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0),
                itemMapper.toItem(user.getId(), newItemDto),
                UserMapper.toUser(newUserDto),
                BookingStatus.WAITING)));
        bookingRepository.save((new Booking(
                2L,
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0),
                itemMapper.toItem(user.getId(), newItemDto),
                UserMapper.toUser(newUserDto),
                BookingStatus.WAITING)));
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "FUTURE",
                0, 20);
        assertEquals(2, listBookings.size());
    }
}
