package ru.practicum.shareit.Item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ServiceUtil;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceUnitTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    private ItemService itemService;

    private final ItemMapper itemMapper;

    private User user;
    private UserDto userDto1;
    private UserDto userDto2;
    private ItemDto itemDto;
    private ItemDto itemDto2;
    private UserService userService;
    private BookingService bookingService;

    @BeforeEach
    public void setUp() {

        user = new User(200L, getRandomString(), getRandomEmail());
        userDto1 = new UserDto(201L, getRandomString(), getRandomEmail());
        userDto2 = new UserDto(202L, getRandomString(), getRandomEmail());
        itemDto = new ItemDto(200L, "Item1", "Description1", true,
                user.getId(), null, null, null);
        itemDto2 = new ItemDto(202L, "Item2", "Description2", true,
                user.getId(), null, null, null);

        userService = mock(UserService.class);
        bookingService = mock(BookingService.class);
        ServiceUtil serviceUtil = new ServiceUtil(null, userService, bookingService);
        itemService = new ItemService(itemRepository, commentRepository, serviceUtil,
                itemMapper);
    }

    @Test
    void getItemById() {
        when(itemRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.getItemById(-1L, 1L));
        Assertions.assertEquals("Вещь не найдена", exception.getMessage());
    }

    @Test
    void addItemComment() {
        when(userService.findUserById(any()))
                .thenReturn(user);
        when(bookingService.getBookingWithUserBookedItem(any(Long.class), any(Long.class)))
                .thenReturn(new Booking(1L,
                        LocalDateTime.of(2012, 12, 25, 12, 0, 0),
                        LocalDateTime.of(2013, 12, 26, 12, 0, 0),
                        itemMapper.toItem(userDto1.getId(), itemDto),
                        user,
                        BookingStatus.CANCELED));
        CommentDto commentDto = new CommentDto(1L, "Comment1", itemMapper.toItem(user.getId(), itemDto),
                user.getName(), LocalDateTime.now());
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setCreated(LocalDateTime.now());
        comment.setItem(itemMapper.toItem(user.getId(), itemDto));
        comment.setAuthor(user);
        comment.setText(commentDto.getText());
        when(commentRepository.save(any()))
                .thenReturn(comment);
        Assertions.assertEquals(commentDto.getText(),
                itemService.addComment(commentDto, itemDto.getId(), user.getId()).getText());
    }

}
