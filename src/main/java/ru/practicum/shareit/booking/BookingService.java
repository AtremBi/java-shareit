package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public BookingService(BookingRepository bookingRepository, UserService userService,
                              @Lazy ItemService itemService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.itemService = itemService;
    }

    public BookingDto create(BookingInputDto bookingInputDto, Long bookerId){
        if (Objects.equals(bookerId, itemService.findItemById(bookingInputDto.getItemId()).getOwnerId())){
            throw new NotFoundException("Вещь не доступна для бронирования");
        }
        if (!itemService.findItemById(bookingInputDto.getItemId()).getIsAvailable()){
            throw new ItemUnavailable("Вещь не доступна для бронирования");
        }
        timeCheck(bookingInputDto.getStart(), bookingInputDto.getEnd());
        return BookingMapper.toBookingDto(bookingRepository.save(
                BookingMapper.toBooking(bookerId, bookingInputDto, itemService, userService)));
    }

    public BookingDto update(Long userId, Boolean approved, Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        if (booking.getEnd().equals(LocalDateTime.now())){
            throw new ValidationException("Время бронирования вышло");
        }

        if (userId.equals(booking.getBooker().getId())){
            if (!approved){
                booking.setStatus(BookingStatus.CANCELED);
            } else {
                throw new NotFoundException("Только владелец может подтвердить бронирование");
            }
        } else if (booking.getItem().getOwnerId().equals(userId)
                && !booking.getStatus().equals(BookingStatus.CANCELED)){
            if (!booking.getStatus().equals(BookingStatus.WAITING)){
                throw new ChangeStatusException("Повторно изменить статус нельзя");
            }
            if (approved){
                booking.setStatus(BookingStatus.APPROVED);
            }
            else {
                booking.setStatus(BookingStatus.REJECTED);
            }
        } else {
            throw new ValidationException("Подтвердить бронирование может только владелец");
//            if (booking.getStatus().equals(BookingStatus.CANCELED)){
//                throw new ValidationException("Бронирование было отменено");
//            }
        }
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    public BookingDto getBookingById(Long bookingId, Long userId){
        userService.findUserById(userId);
        BookingDto bookingDto = BookingMapper.toBookingDto(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено")));
        if (bookingDto.getBooker().getId().equals(userId) || bookingDto.getItem().getOwnerId().equals(userId)){
            return bookingDto;
        } else {
            throw new NotFoundException("Просмотр бронирвания доступен владельцу вещи или бронирующему");
        }
    }

    public List<BookingDto> getBookings(Long userId, String state){
        userService.findUserById(userId);
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");
        if (state == null){
            state = "ALL";
        }
        switch (state){
            case "ALL":
                return BookingMapper.toBookingDto(bookingRepository.findAllByBookerId(userId, sortByStartDesc));
            case "FUTURE":
                return BookingMapper.toBookingDto(bookingRepository.
                        findByBookerIdAndStartIsAfter(userId, LocalDateTime.now(),sortByStartDesc));
            case "WAITING":
                return BookingMapper.toBookingDto(bookingRepository
                        .findByBookerIdAndStatus(userId, BookingStatus.WAITING, sortByStartDesc));
            case "REJECTED":
                return BookingMapper.toBookingDto(bookingRepository
                        .findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sortByStartDesc));
            case "CURRENT":
                return BookingMapper.toBookingDto(bookingRepository
                        .findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                                LocalDateTime.now(), sortByStartDesc));
            case "PAST":
                return BookingMapper.toBookingDto(bookingRepository
                        .findByBookerIdAndEndIsBefore(userId, LocalDateTime.now()
                                ,sortByStartDesc));
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    public List<BookingDto> getBookingsByOwner(Long userId, String state){
        userService.findUserById(userId);
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");
        if (state == null){
            state = "ALL";
        }
        switch (state){
            case "ALL":
                return BookingMapper.toBookingDto(bookingRepository.findAllByItem_OwnerId(userId, sortByStartDesc));
            case "FUTURE":
                return BookingMapper.toBookingDto(bookingRepository.
                        findByItem_OwnerIdAndStartIsAfter(userId, LocalDateTime.now(),sortByStartDesc));
            case "WAITING":
                return BookingMapper.toBookingDto(bookingRepository
                        .findByItem_OwnerIdAndStatus(userId, BookingStatus.WAITING, sortByStartDesc));

            case "REJECTED":
                return BookingMapper.toBookingDto(bookingRepository
                        .findByItem_OwnerIdAndStatus(userId, BookingStatus.REJECTED, sortByStartDesc));
            case "CURRENT":
                return BookingMapper.toBookingDto(bookingRepository
                        .findByItem_OwnerIdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                                LocalDateTime.now(), sortByStartDesc));
            case "PAST":
                return BookingMapper.toBookingDto(bookingRepository
                        .findByItem_OwnerIdAndEndIsBefore(userId, LocalDateTime.now()
                                ,sortByStartDesc));
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }
    @Transactional(readOnly = true)
    public ShortBookingDto getLastBooking(Long itemId){
        return BookingMapper.toShortBookingDto(bookingRepository.
                findFirstByItem_IdAndEndBeforeOrderByEndDesc(itemId, LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public ShortBookingDto getNextBooking(Long itemId){
        return BookingMapper.toShortBookingDto(bookingRepository.
                findFirstByItem_IdAndStartAfterOrderByStartAsc(itemId, LocalDateTime.now()));
    }
    public Booking getBookingWithUserBookedItem(Long itemId, Long userId) {
        return bookingRepository.findFirstByItemIdAndBookerIdAndEndIsBeforeAndStatus(itemId,
                userId, LocalDateTime.now(), BookingStatus.APPROVED);
    }

    private void timeCheck(LocalDateTime start, LocalDateTime end){
        if (start == null || end == null
                || start.equals(end)
                || start.isAfter(end)
                || start.isBefore(LocalDateTime.now())){
            throw new ItemUnavailable("Не верно указан временной промежуток / во временном промежутке указан null");
        }
    }

}
