package ru.practicum.shareit.Item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.ServiceUtil;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exeptions.ItemUnavailable;
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
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private UserRepository userRepository;
    private BookingRepository bookingRepository;

    @BeforeEach
    public void setUp() {

        user = new User(200L, getRandomString(), getRandomEmail());
        userDto1 = new UserDto(201L, getRandomString(), getRandomEmail());
        userDto2 = new UserDto(202L, getRandomString(), getRandomEmail());
        itemDto = new ItemDto(200L, "Item1", "Description1", true,
                user.getId(), null, null, null);
        itemDto2 = new ItemDto(202L, "Item2", "Description2", true,
                user.getId(), null, null, null);

        userRepository = mock(UserRepository.class);
        bookingRepository = mock(BookingRepository.class);
        ServiceUtil serviceUtil = new ServiceUtil(null, new UserService(userRepository),
                new BookingService(bookingRepository, null, null));
        itemService = new ItemService(itemRepository, commentRepository, serviceUtil,
                itemMapper);
    }

    @Test
    void getItemById() {
        when(itemRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.getItemById(-1L, 1L));
        assertEquals("Вещь не найдена", exception.getMessage());
    }

    @Test
    void addItemComment() {
        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findFirstByItemIdAndBookerIdAndEndIsBeforeAndStatus(
                any(Long.class), any(Long.class), any(LocalDateTime.class), any(BookingStatus.class)))
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
        assertEquals(commentDto.getText(),
                itemService.addComment(commentDto, itemDto.getId(), user.getId()).getText());

        when(bookingRepository.findFirstByItemIdAndBookerIdAndEndIsBeforeAndStatus(
                any(Long.class), any(Long.class), any(LocalDateTime.class), any(BookingStatus.class)))
                .thenReturn(null);

        ItemUnavailable exp = assertThrows(ItemUnavailable.class,
                () -> itemService.addComment(commentDto, itemDto.getId(), user.getId()).getText());
        assertEquals("У пользователя нет забронированных вещей", exp.getMessage());
    }

    @Test
    void createItem() {
        when(userRepository.findById(any()))
                .thenReturn(Optional.of(UserMapper.toUser(userDto1)));
        when(itemRepository.save(any()))
                .thenReturn(itemMapper.toItem(userDto1.getId(), itemDto));
        when(itemRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(itemMapper.toItem(userDto1.getId(), itemDto)));
        itemService.createItem(userDto1.getId(), itemDto);
        ItemDto returnItemDto = itemService.getItemById(itemDto.getId(), userDto1.getId());
        assertThat(returnItemDto.getDescription(), equalTo(itemDto.getDescription()));
    }

    @Test
    void delete() {
        NotFoundException exp2 = assertThrows(NotFoundException.class,
                () -> itemService.deleteItem(-2L));
        assertEquals("Вещь не найдена", exp2.getMessage());
    }

    @Test
    void getItems() {
        when(itemRepository.findItemByOwnerId(any(Long.class), any()))
                .thenReturn(new PageImpl<>(List.of(itemMapper.toItem(userDto1.getId(), itemDto))));

        List<ItemDto> listItems = itemService.getItems(userDto1.getId(), 0, 10);
        assertEquals(1, listItems.size());

        NotFoundException exp2 = assertThrows(NotFoundException.class,
                () -> itemService.deleteItem(-2L));
        assertEquals("Вещь не найдена", exp2.getMessage());
    }

    @Test
    void searchItems() {
        when(itemRepository.searchByQuery(any(String.class), any()))
                .thenReturn(new PageImpl<>(List.of(itemMapper.toItem(user.getId(), itemDto))));

        List<ItemDto> listItems = itemService.searchItems("item", 0, 1);
        assertEquals(1, listItems.size());
    }

}
