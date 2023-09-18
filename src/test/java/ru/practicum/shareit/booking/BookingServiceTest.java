package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.exeptions.ValidationException;
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
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    BookingRepository mockBookingRepository;
    @Mock
    UserService userServiceMock;
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    private User user = new User(300L, getRandomString(), getRandomEmail());
    private UserDto userDto1 = new UserDto(301L, getRandomString(), getRandomEmail());
    private UserDto userDto2 = new UserDto(302L, getRandomString(), getRandomEmail());
    private ItemDto itemDto1 = new ItemDto(301L, getRandomString(), getRandomString(), true,
            user.getId(), null, null, null);
    private ItemDto itemDto2 = new ItemDto(302L, getRandomString(), getRandomString(), true,
            user.getId(), null, null, null);

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
        UserDto ownerDto = userService.createUser(userDto1);
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto1);
        BookingInputDto bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        NotFoundException exp = assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingInputDto, ownerDto.getId()));
        assertEquals("Вещь не доступна для бронирования",
                exp.getMessage());
    }

    @Test
    void shouldException_whenGetBooking_byNotOwnerOrNotBooker() {
        UserDto ownerDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        UserDto userDto3 = new UserDto(303L, getRandomString(), getRandomEmail());
        userDto3 = userService.createUser(userDto3);
        Long userId = userDto3.getId();
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto1);
        BookingInputDto bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        BookingDto bookingDto = bookingService.create(bookingInputDto, newUserDto.getId());
        NotFoundException exp = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(bookingDto.getId(), userId));
        assertEquals("Просмотр бронирвания доступен владельцу вещи или бронирующему",
                exp.getMessage());
    }

    @Test
    void shouldReturnBookings_whenGetBookings_byBookerAndSizeIsNull() {
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
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "ALL", 0, null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookings_byBookerAndSizeIsNullads() {
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
        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.getBookings(newUserDto.getId(), "ALL", 0, -1));
        assertEquals("from или size не должны быть отрицательными",
                exp.getMessage());
    }

    @Test
    void shouldReturnBookings_whenGetBookings_byBookerAndSizeIsNotNull() {
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
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "ALL", 0, 1);
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookingsInWaitingStatus_byBookerAndSizeIsNull() {
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
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.create(bookingInputDto1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "WAITING",
                0, null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookingsInWaitingStatus_byBookerAndSizeNotNull() {
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
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "WAITING",
                0, 1);
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookingsInRejectedStatus_byBookerAndSizeIsNull() {
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
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "REJECTED",
                0, null);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookingsInRejectedStatus_byBookerAndSizeNotNull() {
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
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "REJECTED",
                0, 1);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookings_byOwnerAndSizeIsNull() {
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
        List<BookingDto> listBookings = bookingService.getBookingsByOwner(ownerDto.getId(), "ALL",
                0, null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookings_byOwnerAndSizeNotNull() {
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
        List<BookingDto> listBookings = bookingService.getBookingsByOwner(ownerDto.getId(), "ALL",
                0, 1);
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookings_byOwnerAndStatusWaitingAndSizeIsNull() {
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
        List<BookingDto> listBookings = bookingService.getBookingsByOwner(ownerDto.getId(), "WAITING",
                0, null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookings_byOwnerAndStatusWaitingAndSizeNotNull() {
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
        List<BookingDto> listBookings = bookingService.getBookingsByOwner(ownerDto.getId(), "WAITING",
                0, 1);
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookings_byOwnerAndStatusRejectedAndSizeIsNull() {
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
        List<BookingDto> listBookings = bookingService.getBookingsByOwner(ownerDto.getId(), "REJECTED",
                0, null);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookings_byOwnerAndStatusRejectedAndSizeNotNull() {
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
