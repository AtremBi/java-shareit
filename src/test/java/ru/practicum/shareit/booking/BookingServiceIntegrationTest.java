package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.user.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class BookingServiceIntegrationTest {
    @Mock
    private BookingRepository mockBookingRepository;
    @Mock
    private UserService userServiceMock;

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
