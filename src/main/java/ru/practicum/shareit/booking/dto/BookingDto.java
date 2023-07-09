package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.enums.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Date;

@Data
public class BookingDto {
    private Long id;
    private Date start;
    private Date end;
    private Item item;
    private User booker;
    private BookingStatus status;
}
