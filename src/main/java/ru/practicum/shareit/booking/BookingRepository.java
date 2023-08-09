package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerId(Long bookerId, Sort sort);
    List<Booking> findAllByItem_OwnerId(Long ownerId, Sort sort);
    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);
    List<Booking> findByItem_OwnerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);
    Booking findFirstByItem_IdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime end);
    Booking findFirstByItem_IdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime start);
    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);
    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus bookingStatus, Sort sort);
    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start,
                                                              LocalDateTime end, Sort sort);
    List<Booking> findByItem_OwnerIdAndStatus(Long bookerId, BookingStatus bookingStatus, Sort sort);
    List<Booking> findByItem_OwnerIdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime start,
                                                                   LocalDateTime end, Sort sort);
    List<Booking> findByItem_OwnerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);
    Booking findFirstByItemIdAndBookerIdAndEndIsBeforeAndStatus(Long itemId, Long userId,
                                                                LocalDateTime end, BookingStatus bookingStatus);
}
