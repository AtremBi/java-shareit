package ru.practicum.shareit.Item;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.exeptions.ItemUnavailable;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.exeptions.WrongUserException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ItemServiceTest {
    @Mock
    private ItemRepository mockItemRepository;
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    private final ItemMapper itemMapper;

    private User user = new User(200L, getRandomString(), getRandomEmail());
    private UserDto userDto1 = new UserDto(201L, getRandomString(), getRandomEmail());
    private UserDto userDto2 = new UserDto(202L, getRandomString(), getRandomEmail());
    private ItemDto itemDto = new ItemDto(200L, "Item1", "Description1", true,
            user.getId(), null, null, null);
    private ItemDto itemDto2 = new ItemDto(202L, "Item2", "Description2", true,
            user.getId(), null, null, null);


    private String getRandomEmail() {
        RandomString randomString = new RandomString();
        return randomString.nextString() + "@" + randomString.nextString() + ".ew";
    }

    private String getRandomString() {
        RandomString randomString = new RandomString();
        return randomString.nextString();
    }

    @Test
    void shouldCreateItem() {
        UserDto newUserDto = userService.createUser(userDto1);
        ItemDto newItemDto = itemService.createItem(newUserDto.getId(), itemDto);
        ItemDto returnItemDto = itemService.getItemById(newItemDto.getId(), newUserDto.getId());
        assertThat(returnItemDto.getDescription(), equalTo(itemDto.getDescription()));
    }

    @Test
    void shouldDeleteWhenUserIsOwner() {
        UserDto ownerDto = userService.createUser(userDto1);
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto);
        itemService.deleteItem(newItemDto.getId());
        NotFoundException exp = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(newItemDto.getId(), ownerDto.getId()));
        assertEquals("Вещь не найдена", exp.getMessage());
    }

    @Test
    void shouldException_whenDeleteItemNotExist() {
        NotFoundException exp = assertThrows(NotFoundException.class,
                () -> itemService.deleteItem(-2L));
        assertEquals("Вещь не найдена", exp.getMessage());
    }

    @Test
    void shouldUpdateItem() {
        UserDto newUserDto = userService.createUser(userDto1);
        ItemDto newItemDto = itemService.createItem(newUserDto.getId(), itemDto);
        newItemDto.setName("NewName");
        newItemDto.setDescription("NewDescription");
        newItemDto.setAvailable(false);
        ItemDto returnItemDto = itemService.updateItem(newUserDto.getId(), newItemDto.getId(), newItemDto);
        assertThat(returnItemDto.getName(), equalTo("NewName"));
        assertThat(returnItemDto.getDescription(), equalTo("NewDescription"));
        assertFalse(returnItemDto.getAvailable());
    }

    @Test
    void shouldException_whenUpdateItemNotOwner() {
        UserDto ownerDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto);
        WrongUserException exp = assertThrows(WrongUserException.class,
                () -> itemService.updateItem(newUserDto.getId(), newItemDto.getId(), newItemDto));
        assertEquals("У пользователя нет такой вещи", exp.getMessage());
    }

    @Test
    void shouldReturnItemsByOwner() {
        UserDto ownerDto = userService.createUser(userDto1);
        itemService.createItem(ownerDto.getId(), itemDto);
        itemService.createItem(ownerDto.getId(), itemDto2);
        List<ItemDto> listItems = itemService.getItems(ownerDto.getId(), 0, 10);
        assertEquals(2, listItems.size());
    }

    @Test
    void shouldReturnItemsBySearch() {
        UserDto ownerDto = userService.createUser(userDto1);
        itemService.createItem(ownerDto.getId(), itemDto);
        itemService.createItem(ownerDto.getId(), itemDto2);
        List<ItemDto> listItems = itemService.searchItems("item", 0, 1);
        assertEquals(1, listItems.size());
    }

    @Test
    void shouldReturnItemsBySearch_whenSizeIsNull() {
        UserDto ownerDto = userService.createUser(userDto1);
        itemService.createItem(ownerDto.getId(), itemDto);
        itemService.createItem(ownerDto.getId(), itemDto2);
        List<ItemDto> listItems = itemService.searchItems("item", 0, null);
        assertEquals(2, listItems.size());
    }

    @Test
    void shouldException_whenCreateCommentWhenUserNotBooker() {
        UserDto ownerDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        CommentDto commentDto = new CommentDto(1L, "Comment1", itemMapper.toItem(ownerDto.getId(), itemDto),
                newUserDto.getName(), LocalDateTime.now());
        ItemUnavailable exp = assertThrows(ItemUnavailable.class,
                () -> itemService.addComment(commentDto, itemDto.getId(), newUserDto.getId()));
        assertEquals("У пользователя нет забронированных вещей", exp.getMessage());
    }

    @Test
    void shouldCreateComment() {
        UserDto ownerDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        ItemDto newItemDto = itemService.createItem(ownerDto.getId(), itemDto);
        BookingInputDto bookingInputDto = new BookingInputDto(
                newItemDto.getId(),
                LocalDateTime.now().plusSeconds(1),
                LocalDateTime.now().plusSeconds(3)
        );
        BookingDto bookingDto = bookingService.create(bookingInputDto, newUserDto.getId());
        bookingService.update(ownerDto.getId(), true, bookingDto.getId());
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        CommentDto commentDto = new CommentDto(1L, "Comment1", itemMapper.toItem(ownerDto.getId(), itemDto),
                newUserDto.getName(), LocalDateTime.now());
        itemService.addComment(commentDto, newItemDto.getId(), newUserDto.getId());
        Assertions.assertEquals(1, itemService.getCommentsByItemId(newItemDto.getId()).size());
    }

    @Test
    void shouldException_whenGetItem_withWrongId() {
        ItemService itemService = new ItemService(mockItemRepository, null, null,
                null);
        when(mockItemRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.getItemById(-1L, 1L));
        Assertions.assertEquals("Вещь не найдена", exception.getMessage());
    }
}
