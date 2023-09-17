package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    BookingRepository mockBookingRepository;
    @Mock
    UserService userServiceMock;
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    public User user;
    public UserDto userDto1;
    public UserDto userDto2;
    public ItemDto itemDto1;
    public ItemDto itemDto2;
    public UserDto ownerDto;
    public UserDto newUserDto;
    public ItemDto newItemDto;
    public BookingInputDto bookingInputDto;
    public BookingInputDto bookingInputDto1;


    @BeforeAll
    public void setUp() {
        user = new User(4L, getRandomString(), getRandomEmail());
        userDto1 = new UserDto(1L, getRandomString(), getRandomEmail());
        userDto2 = new UserDto(2L, getRandomString(), getRandomEmail());
        itemDto1 = new ItemDto(1L, getRandomString(),
                getRandomString(), true, null, null, null, null);

        itemDto2 = new ItemDto(2L, getRandomString(),
                getRandomString(), true, null, null, null, null);

        ownerDto = userService.createUser(userDto1);
        newUserDto = userService.createUser(userDto2);
        newItemDto = itemService.createItem(ownerDto.getId(), itemDto1);
        bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2023, 12, 25, 12, 0, 0),
                LocalDateTime.of(2023, 12, 26, 12, 0, 0));
        bookingService.create(bookingInputDto, newUserDto.getId());
        bookingInputDto1 = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2024, 12, 25, 12, 0, 0),
                LocalDateTime.of(2024, 12, 26, 12, 0, 0));
    }

    private String getRandomEmail() {
        RandomString randomString = new RandomString();
        return randomString.nextString() + "@" + randomString.nextString() + ".ew";
    }

    private String getRandomString() {
        RandomString randomString = new RandomString();
        return randomString.nextString();
    }

    @Test
    void shouldException_whenCreateBooking_byOwnerItem() {
        NotFoundException exp = assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingInputDto, ownerDto.getId()));
        assertEquals("Вещь не доступна для бронирования",
                exp.getMessage());
    }

    @Test
    void shouldException_whenGetBooking_byNotOwnerOrNotBooker() {
        UserDto userDto3 = new UserDto(3L, getRandomString(), getRandomEmail());
        userDto3 = userService.createUser(userDto3);
        Long userId = userDto3.getId();

        BookingDto bookingDto = bookingService.create(bookingInputDto, newUserDto.getId());
        NotFoundException exp = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(bookingDto.getId(), userId));
        assertEquals("Просмотр бронирвания доступен владельцу вещи или бронирующему", exp.getMessage());
    }

    @Test
    void shouldReturnBookings_whenGetBookings() {
        bookingService.create(bookingInputDto1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "ALL", 0, 1);
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookingsInWaitingStatus() {
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "WAITING",
                0, 1);
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookingsInRejectedStatus() {
        BookingInputDto bookingInputDto1 = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.create(bookingInputDto1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "REJECTED",
                0, 1);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookingsByOwner() {
        BookingInputDto bookingInputDto1 = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.create(bookingInputDto1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsByOwner(ownerDto.getId(), "ALL",
                0, 1);
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookingsByOwnerAndStatusWaiting() {
        BookingInputDto bookingInputDto1 = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.create(bookingInputDto1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsByOwner(ownerDto.getId(), "WAITING",
                0, 1);
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookingsByOwnerAndStatusRejected() {
        BookingInputDto bookingInputDto1 = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 0, 0),
                LocalDateTime.of(2031, 12, 26, 12, 0, 0));
        bookingService.create(bookingInputDto1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsByOwner(ownerDto.getId(), "REJECTED",
                0, 1);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldException_whenGetBooking_withWrongId() {
        BookingService bookingService = new BookingService(mockBookingRepository, userServiceMock,
                null);
        when(mockBookingRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBookingById(-1L, 1L));
        assertEquals("Бронирование не найдено", exception.getMessage());
    }
}
