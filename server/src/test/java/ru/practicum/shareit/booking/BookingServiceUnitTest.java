package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.exeptions.ChangeStatusException;
import ru.practicum.shareit.exeptions.ItemUnavailable;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.exeptions.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceUnitTest {
    private BookingService bookingService;
    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private BookingRepository bookingRepository;
    private final ItemMapper itemMapper;
    private User user;
    private UserDto userDto1;
    private UserDto userDto2;
    private ItemDto itemDto1;
    private Booking booking;
    private Booking currentBooking;
    private Booking pastBooking;
    private Booking futureBooking;
    private Booking waitingBooking;
    private Booking rejectedBooking;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        itemRepository = mock(ItemRepository.class);
        bookingRepository = mock(BookingRepository.class);

        bookingService = new BookingService(bookingRepository, new UserService(userRepository),
                new ItemService(itemRepository, null, null, null));
        user = new User(300L, getRandomString(), getRandomEmail());
        userDto1 = new UserDto(301L, getRandomString(), getRandomEmail());
        userDto2 = new UserDto(302L, getRandomString(), getRandomEmail());
        itemDto1 = new ItemDto(301L, getRandomString(), getRandomString(), true,
                user.getId(), null, null, null);

        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto1.getId(),
                LocalDateTime.of(2012, 12, 25, 12, 0, 0),
                LocalDateTime.of(2013, 12, 26, 12, 0, 0));

        booking = new Booking(1L,
                bookingInputDto.getStart(),
                bookingInputDto.getEnd(),
                itemMapper.toItem(userDto1.getId(), itemDto1),
                user,
                BookingStatus.CANCELED);

        //Current
        currentBooking = new Booking(2L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                itemMapper.toItem(user.getId(), itemDto1),
                user,
                BookingStatus.APPROVED);

        //Past
        pastBooking = new Booking(
                3L,
                LocalDateTime.now().minusDays(1000),
                LocalDateTime.now().minusDays(999),
                itemMapper.toItem(user.getId(), itemDto1),
                user,
                BookingStatus.APPROVED);

        //Future
        futureBooking = new Booking(
                4L,
                LocalDateTime.now().minusDays(999),
                LocalDateTime.now().minusDays(1000),
                itemMapper.toItem(user.getId(), itemDto1),
                user,
                BookingStatus.APPROVED);

        //Waiting
        waitingBooking = new Booking(
                5L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(2),
                itemMapper.toItem(user.getId(), itemDto1),
                user,
                BookingStatus.WAITING);

        //Rejected
        rejectedBooking = new Booking(
                5L,
                LocalDateTime.now().plusDays(100),
                LocalDateTime.now().plusDays(101),
                itemMapper.toItem(user.getId(), itemDto1),
                user,
                BookingStatus.REJECTED);
    }

    @Test
    void create() {
        when(itemRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(itemMapper.toItem(user.getId(), itemDto1)));

        Item item = itemMapper.toItem(user.getId(), itemDto1);
        item.setAvailable(false);
        BookingInputDto bookingInputDto = new BookingInputDto(
                itemDto1.getId(),
                LocalDateTime.of(2012, 12, 25, 12, 0, 0),
                LocalDateTime.of(2013, 12, 26, 12, 0, 0));

        ItemUnavailable exp1 = assertThrows(ItemUnavailable.class,
                () -> bookingService.create(bookingInputDto, userDto2.getId()));
        assertEquals("Не верно указан временной промежуток / во временном промежутке указан null",
                exp1.getMessage());

        NotFoundException exp2 = assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingInputDto, user.getId()));
        assertEquals("Вещь не доступна для бронирования",
                exp2.getMessage());

        when(itemRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(item));

        ItemUnavailable exp3 = assertThrows(ItemUnavailable.class,
                () -> bookingService.create(bookingInputDto, 44L));
        assertEquals("Вещь не доступна для бронирования",
                exp3.getMessage());

        when(bookingRepository.save(any()))
                .thenReturn(new Booking());
        user.setId(44L);
        when(userRepository.save(any()))
                .thenReturn(user);
        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(user));
        item.setAvailable(true);
        bookingInputDto.setStart(LocalDateTime.of(2030, 12, 25, 12, 0, 0));
        bookingInputDto.setEnd(LocalDateTime.of(2031, 12, 25, 12, 0, 0));
        userRepository.save(UserMapper.toUser(userDto1));

        assertEquals(new BookingDto(null, null, null, null, null, null),
                bookingService.create(bookingInputDto, user.getId()));
    }

    @Test
    void getBookings() {

        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerId(any(Long.class), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));
        when(bookingRepository.findByBookerIdAndStartIsAfter(any(Long.class), any(LocalDateTime.class), any()))
                .thenReturn(new PageImpl<>(List.of(futureBooking)));
        when(bookingRepository.findByBookerIdAndStatus(any(Long.class), any(BookingStatus.class), any()))
                .thenReturn(new PageImpl<>(List.of(waitingBooking)));
        when(bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(any(Long.class), any(LocalDateTime.class),
                any(LocalDateTime.class), any()))
                .thenReturn(new PageImpl<>(List.of(pastBooking)));
        when(bookingRepository.findByBookerIdAndEndIsBefore(any(Long.class), any(LocalDateTime.class), any()))
                .thenReturn(new PageImpl<>(List.of(currentBooking)));

        List<BookingDto> listBookings1 = bookingService.getBookings(user.getId(), "ALL", 0, 20);
        assertEquals(BookingStatus.CANCELED, listBookings1.get(0).getStatus());

        List<BookingDto> listBookings2 = bookingService.getBookings(user.getId(), "FUTURE", 0, 20);
        assertEquals(BookingStatus.APPROVED, listBookings2.get(0).getStatus());

        List<BookingDto> listBookings3 = bookingService.getBookings(user.getId(), "WAITING", 0, 20);
        assertEquals(BookingStatus.WAITING, listBookings3.get(0).getStatus());

        List<BookingDto> listBookings4 = bookingService.getBookings(user.getId(), "PAST", 0, 20);
        assertEquals(BookingStatus.APPROVED, listBookings4.get(0).getStatus());

        List<BookingDto> listBookings5 = bookingService.getBookings(user.getId(), "CURRENT", 0, 20);
        assertEquals(BookingStatus.APPROVED, listBookings5.get(0).getStatus());

        List<BookingDto> listBookings6 = bookingService.getBookings(user.getId(), "REJECTED", 0, 20);
        assertEquals(BookingStatus.WAITING, listBookings6.get(0).getStatus());

        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.getBookings(user.getId(), "ALL", 0, -1));
        assertEquals("from или size не должны быть отрицательными",
                exp.getMessage());
    }


    @Test
    void getBookingsByOwner() {

        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findAllByItemOwnerId(any(Long.class), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));
        when(bookingRepository.findByItemOwnerIdAndStartIsAfter(any(Long.class), any(LocalDateTime.class), any()))
                .thenReturn(new PageImpl<>(List.of(futureBooking)));
        when(bookingRepository.findByItemOwnerIdAndStatus(any(Long.class), any(BookingStatus.class), any()))
                .thenReturn(new PageImpl<>(List.of(waitingBooking)));
        when(bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(any(Long.class), any(LocalDateTime.class),
                any(LocalDateTime.class), any()))
                .thenReturn(new PageImpl<>(List.of(pastBooking)));
        when(bookingRepository.findByItemOwnerIdAndEndIsBefore(any(Long.class), any(LocalDateTime.class), any()))
                .thenReturn(new PageImpl<>(List.of(currentBooking)));

        List<BookingDto> listBookings1 = bookingService.getBookingsByOwner(user.getId(), "ALL", 0, 20);
        assertEquals(BookingStatus.CANCELED, listBookings1.get(0).getStatus());

        List<BookingDto> listBookings2 = bookingService.getBookingsByOwner(user.getId(), "FUTURE", 0, 20);
        assertEquals(BookingStatus.APPROVED, listBookings2.get(0).getStatus());

        List<BookingDto> listBookings3 = bookingService.getBookingsByOwner(user.getId(), "WAITING", 0, 20);
        assertEquals(BookingStatus.WAITING, listBookings3.get(0).getStatus());

        List<BookingDto> listBookings4 = bookingService.getBookingsByOwner(user.getId(), "PAST", 0, 20);
        assertEquals(BookingStatus.APPROVED, listBookings4.get(0).getStatus());

        List<BookingDto> listBookings5 = bookingService.getBookingsByOwner(user.getId(), "CURRENT", 0, 20);
        assertEquals(BookingStatus.APPROVED, listBookings5.get(0).getStatus());

        List<BookingDto> listBookings6 = bookingService.getBookingsByOwner(user.getId(), "REJECTED", 0, 20);
        assertEquals(BookingStatus.WAITING, listBookings6.get(0).getStatus());

        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.getBookingsByOwner(user.getId(), "ALL", 0, -1));
        assertEquals("from или size не должны быть отрицательными",
                exp.getMessage());
    }


    @Test
    void update() {
        waitingBooking.setStart(LocalDateTime.now().withNano(1));
        waitingBooking.setEnd(LocalDateTime.now().withNano(50));
        when(bookingRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(waitingBooking));

        ValidationException exp1 = assertThrows(ValidationException.class,
                () -> bookingService.update(user.getId(), true, waitingBooking.getId()));
        assertEquals("Время бронирования вышло",
                exp1.getMessage());

        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any()))
                .thenReturn(booking);
        BookingDto bookingAfterUpdate = bookingService.update(booking.getItem().getOwnerId(),
                true, booking.getId());
        booking.setStatus(BookingStatus.APPROVED);
        assertEquals(BookingMapper.toBookingDto(booking), bookingAfterUpdate);
        ChangeStatusException exp2 = assertThrows(ChangeStatusException.class,
                () -> bookingService.update(booking.getItem().getOwnerId(),
                        true, booking.getId()));
        assertEquals("Повторно изменить статус нельзя",
                exp2.getMessage());

        booking.setStatus(BookingStatus.WAITING);
        NotFoundException exp3 = assertThrows(NotFoundException.class,
                () -> bookingService.update(booking.getBooker().getId(),
                        true, booking.getId()));
        assertEquals("Только владелец может подтвердить бронирование",
                exp3.getMessage());

        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.update(55L,
                        true, booking.getId()));
        assertEquals("Подтвердить бронирование может только владелец",
                exp.getMessage());

    }

    @Test
    void getBookingById() {
        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(waitingBooking));

        NotFoundException exp = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(waitingBooking.getId(), 44L));
        assertEquals("Просмотр бронирвания доступен владельцу вещи или бронирующему",
                exp.getMessage());

        when(bookingRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBookingById(-1L, 1L));
        assertEquals("Бронирование не найдено", exception.getMessage());
    }
}
