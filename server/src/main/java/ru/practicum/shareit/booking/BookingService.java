package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.Pagination.Pagination;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.exeptions.ChangeStatusException;
import ru.practicum.shareit.exeptions.ItemUnavailable;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.exeptions.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    public BookingDto create(BookingInputDto bookingInputDto, Long bookerId) {
        Item itemId = itemService.findItemById(bookingInputDto.getItemId());
        if (Objects.equals(bookerId, itemId.getOwnerId())) {
            throw new NotFoundException("Вещь не доступна для бронирования");
        }
        if (!itemId.getAvailable()) {
            throw new ItemUnavailable("Вещь не доступна для бронирования");
        }
        timeCheck(bookingInputDto.getStart(), bookingInputDto.getEnd());
        return BookingMapper.toBookingDto(bookingRepository.save(
                BookingMapper.toBooking(bookerId, bookingInputDto, itemService, userService)));
    }

    public BookingDto update(Long userId, Boolean approved, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Время бронирования вышло");
        }

        if (userId.equals(booking.getBooker().getId())) {
            if (!approved) {
                booking.setStatus(BookingStatus.CANCELED);
            } else {
                throw new NotFoundException("Только владелец может подтвердить бронирование");
            }
        } else if (booking.getItem().getOwnerId().equals(userId)
                && !booking.getStatus().equals(BookingStatus.CANCELED)) {
            if (!booking.getStatus().equals(BookingStatus.WAITING)) {
                throw new ChangeStatusException("Повторно изменить статус нельзя");
            }
            if (approved) {
                booking.setStatus(BookingStatus.APPROVED);
            } else {
                booking.setStatus(BookingStatus.REJECTED);
            }
        } else {
            throw new ValidationException("Подтвердить бронирование может только владелец");
        }
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long bookingId, Long userId) {
        userService.findUserById(userId);
        BookingDto bookingDto = BookingMapper.toBookingDto(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено")));
        if (bookingDto.getBooker().getId().equals(userId) || bookingDto.getItem().getOwnerId().equals(userId)) {
            return bookingDto;
        } else {
            throw new NotFoundException("Просмотр бронирвания доступен владельцу вещи или бронирующему");
        }
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getBookings(Long userId, String state, Integer from, Integer size) {
        userService.findUserById(userId);
        fromAndSizeValidation(from, size);
        PageRequest pageRequest;

        pageRequest = Pagination.of(from, size, Sort.Direction.DESC, "start");
        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findAllByBookerId(userId, pageRequest)
                        .stream().collect(Collectors.toList());
                break;
            case "FUTURE":
                bookings = bookingRepository
                        .findByBookerIdAndStartIsAfter(userId, LocalDateTime.now(), pageRequest)
                        .stream().collect(Collectors.toList());
                break;
            case "WAITING":
                bookings = bookingRepository
                        .findByBookerIdAndStatus(userId, BookingStatus.WAITING, pageRequest)
                        .stream().collect(Collectors.toList());
                break;
            case "REJECTED":
                bookings = bookingRepository
                        .findByBookerIdAndStatus(userId, BookingStatus.REJECTED, pageRequest)
                        .stream().collect(Collectors.toList());
                break;
            case "CURRENT":
                bookings = bookingRepository
                        .findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                                LocalDateTime.now(), pageRequest)
                        .stream().collect(Collectors.toList());
                break;
            case "PAST":
                bookings = bookingRepository
                        .findByBookerIdAndEndIsBefore(userId, LocalDateTime.now(), pageRequest)
                        .stream().collect(Collectors.toList());
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }
        return BookingMapper.toBookingDto(bookings);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByOwner(Long userId, String state, Integer from, Integer size) {
        userService.findUserById(userId);
        fromAndSizeValidation(from, size);
        PageRequest pageRequest = Pagination.of(from, size, Sort.Direction.DESC, "start");
        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findAllByItemOwnerId(userId, pageRequest)
                        .stream().collect(Collectors.toList());
                break;
            case "FUTURE":
                bookings = bookingRepository
                        .findByItemOwnerIdAndStartIsAfter(userId, LocalDateTime.now(), pageRequest)
                        .stream().collect(Collectors.toList());
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, pageRequest)
                        .stream().collect(Collectors.toList());
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, pageRequest)
                        .stream().collect(Collectors.toList());
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                                LocalDateTime.now(), pageRequest)
                        .stream().collect(Collectors.toList());
                break;
            case "PAST":
                bookings = bookingRepository.findByItemOwnerIdAndEndIsBefore(userId, LocalDateTime.now(), pageRequest)
                        .stream().collect(Collectors.toList());
                break;
            default:
                throw new ValidationException("Unknown state: " + state);

        }
        return BookingMapper.toBookingDto(bookings);
    }

    @Transactional(readOnly = true)
    public ShortBookingDto getLastBooking(Long itemId) {
        return BookingMapper.toShortBookingDto(bookingRepository
                .findTopByItemIdAndStartBeforeOrderByStartDesc(itemId, LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public ShortBookingDto getNextBooking(Long itemId) {
        return BookingMapper.toShortBookingDto(bookingRepository
                .findTopByItemIdAndStartAfterOrderByStartAsc(itemId, LocalDateTime.now()));
    }

    public Booking getBookingWithUserBookedItem(Long itemId, Long userId) {
        return bookingRepository.findFirstByItemIdAndBookerIdAndEndIsBeforeAndStatus(itemId,
                userId, LocalDateTime.now(), BookingStatus.APPROVED);
    }

    private void fromAndSizeValidation(Integer from, Integer size) {
        if (from < 0 || size < 0) {
            throw new ValidationException("from или size не должны быть отрицательными");
        }
    }

    private void timeCheck(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null
                || start.equals(end)
                || start.isAfter(end)
                || start.isBefore(LocalDateTime.now())) {
            throw new ItemUnavailable("Не верно указан временной промежуток / во временном промежутке указан null");
        }
    }

}
