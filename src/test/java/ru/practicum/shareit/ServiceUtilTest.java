package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ServiceUtilTest {
    private final ServiceUtil serviceUtil;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    @Test
    void getItemService(){
        assertEquals(itemService, serviceUtil.getItemService());
    }

    @Test
    void getUserService(){
        assertEquals(userService, serviceUtil.getUserService());
    }

    @Test
    void getBookingService(){
        assertEquals(bookingService, serviceUtil.getBookingService());
    }
}
