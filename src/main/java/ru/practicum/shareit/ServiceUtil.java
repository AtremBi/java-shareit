package ru.practicum.shareit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;

@Service
@AllArgsConstructor
public class ServiceUtil {
    @Getter
    private final ItemService itemService;
    @Getter
    private final UserService userService;
    @Getter
    private final BookingService bookingService;

}
