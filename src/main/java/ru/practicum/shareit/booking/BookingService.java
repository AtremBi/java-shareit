package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public BookingService(BookingRepository bookingRepository, UserService userService,
                          ItemService itemService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.itemService = itemService;
    }

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
        if (booking.getEnd().equals(LocalDateTime.now())) {
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

    public List<BookingDto> getBookings(Long userId, String state, Integer from, Integer size) {
        userService.findUserById(userId);
        if (size == null) {
            size = 20;
        }
        fromAndSizeValidation(from, size);
        PageRequest pageRequest;

        pageRequest = PageRequest.of(from / size, size, Sort.Direction.DESC, "start");
        if (state == null) {
            state = "ALL";
        }
        switch (state) {
            case "ALL":
                return bookingRepository.findAllByBookerId(userId, pageRequest)
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository
                        .findByBookerIdAndStartIsAfter(userId, LocalDateTime.now(), pageRequest)
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case "WAITING":
                return bookingRepository
                        .findByBookerIdAndStatus(userId, BookingStatus.WAITING, pageRequest)
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository
                        .findByBookerIdAndStatus(userId, BookingStatus.REJECTED, pageRequest)
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository
                        .findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                                LocalDateTime.now(), pageRequest)
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case "PAST":
                return bookingRepository
                        .findByBookerIdAndEndIsBefore(userId, LocalDateTime.now(), pageRequest)
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }

    }

    public List<BookingDto> getBookingsByOwner(Long userId, String state, Integer from, Integer size) {
        userService.findUserById(userId);
        if (size == null) {
            size = 20;
        }
        fromAndSizeValidation(from, size);
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.Direction.DESC, "start");
        if (state == null) {
            state = "ALL";
        }
        switch (state) {
            case "ALL":
                return bookingRepository.findAllByItemOwnerId(userId, pageRequest)
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository
                        .findByItemOwnerIdAndStartIsAfter(userId, LocalDateTime.now(), pageRequest)
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, pageRequest)
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

            case "REJECTED":
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, pageRequest)
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                                LocalDateTime.now(), pageRequest)
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findByItemOwnerIdAndEndIsBefore(userId, LocalDateTime.now(), pageRequest)
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }
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
