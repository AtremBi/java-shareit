package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.exeptions.ChangeStatusException;
import ru.practicum.shareit.exeptions.ItemUnavailable;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.exeptions.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceUnitTest {
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
    void shouldException_whenNotValidTime() {
        UserDto ownerDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto1);
        BookingInputDto bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2012, 12, 25, 12, 0, 0),
                LocalDateTime.of(2013, 12, 26, 12, 0, 0));
        ItemUnavailable exp = assertThrows(ItemUnavailable.class,
                () -> bookingService.create(bookingInputDto, newUserDto.getId()));
        assertEquals("Не верно указан временной промежуток / во временном промежутке указан null",
                exp.getMessage());
    }

    @Test
    void shouldException_whenUpdate_setStatusRejected() {
        UserDto ownerDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto1);
        BookingInputDto bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        Long bookingId = bookingService.create(bookingInputDto, newUserDto.getId()).getId();
        bookingService.update(newUserDto.getId(), false, bookingId);
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "ALL", 0, 20);
        assertEquals(BookingStatus.CANCELED, listBookings.get(0).getStatus());
    }

    @Test
    void shouldException_whenUpdate_TimeOff() {
        UserDto ownerDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto1);
        BookingInputDto bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.now().plusSeconds(1),
                LocalDateTime.now().plusSeconds(3));
        Long bookingId = bookingService.create(bookingInputDto, newUserDto.getId()).getId();
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.update(ownerDto.getId(), true, bookingId));
        assertEquals("Время бронирования вышло",
                exp.getMessage());
    }

    @Test
    void shouldUpdateBookingStatusApproved() {
        UserDto ownerDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto1);
        BookingInputDto bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        Long bookingId = bookingService.create(bookingInputDto, newUserDto.getId()).getId();
        bookingService.update(ownerDto.getId(), true, bookingId);
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "ALL", 0, 20);
        assertEquals(BookingStatus.APPROVED, listBookings.get(0).getStatus());
    }

    @Test
    void shouldException_whenReplayUpdateBooking() {
        UserDto ownerDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto1);
        BookingInputDto bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        Long bookingId = bookingService.create(bookingInputDto, newUserDto.getId()).getId();
        bookingService.update(ownerDto.getId(), true, bookingId);
        ChangeStatusException exp = assertThrows(ChangeStatusException.class,
                () -> bookingService.update(ownerDto.getId(), true, bookingId));
        assertEquals("Повторно изменить статус нельзя",
                exp.getMessage());
    }

    @Test
    void shouldException_whenNotBookerUpdate() {
        UserDto ownerDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto1);
        BookingInputDto bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        Long bookingId = bookingService.create(bookingInputDto, newUserDto.getId()).getId();
        NotFoundException exp = assertThrows(NotFoundException.class,
                () -> bookingService.update(newUserDto.getId(), true, bookingId));
        assertEquals("Только владелец может подтвердить бронирование",
                exp.getMessage());
    }

    @Test
    void shouldException_whenNotBookerUpdate2() {
        UserDto ownerDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        userDto2.setId(322L);
        userDto2.setEmail(getRandomEmail());
        UserDto newUserDto2 = userService.createUser(userDto2);
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto1);
        BookingInputDto bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 0, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0, 0));
        Long bookingId = bookingService.create(bookingInputDto, newUserDto.getId()).getId();
        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.update(newUserDto2.getId(), true, bookingId));
        assertEquals("Подтвердить бронирование может только владелец",
                exp.getMessage());
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
    void shouldReturnBookings_whenGetBookings_byBookerAndSizeNegative() {
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
    void shouldReturnBookings_whenGetBookingsInFutureStatus_byBookerAndSizeIsNull() {
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

    @Test
    void shouldNotReturnBookings_whenGetBookingsInCurrentStatus_byBooker() {
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
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "CURRENT",
                0, 20);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookingsInPastStatus_byBookerAndSizeIsNull() {
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
        List<BookingDto> listBookings = bookingService.getBookings(newUserDto.getId(), "PAST",
                0, 20);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldNotReturnBookings_whenGetBookingsInRejectedStatus_byBookerAndSize() {
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
    void shouldReturnBookings_whenGetBookings_byOwnerAndSize() {
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
                0, 20);
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
        BookingDto bookingDto = bookingService.create(bookingInputDto1, newUserDto.getId());
        bookingService.update(ownerDto.getId(), false, bookingDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsByOwner(ownerDto.getId(), "REJECTED",
                0, 20);
        assertEquals(1, listBookings.size());
    }

    @Test
    void shouldReturnBookings_whenGetBookings_byOwnerAndStatusFutureAndSizeNotNull() {
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
        List<BookingDto> listBookings = bookingService.getBookingsByOwner(ownerDto.getId(), "FUTURE",
                0, 1);
        assertEquals(1, listBookings.size());
    }
}
