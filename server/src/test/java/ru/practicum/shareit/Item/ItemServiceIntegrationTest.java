package ru.practicum.shareit.Item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.exeptions.WrongUserException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ItemServiceIntegrationTest {
    private final UserRepository userRepository;
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    private User user;
    private UserDto userDto1;
    private ItemDto itemDto;
    private ItemDto itemDto2;

    @BeforeEach
    public void setUp() {
        user = new User(200L, getRandomString(), getRandomEmail());
        userDto1 = new UserDto(201L, getRandomString(), getRandomEmail());
        itemDto = new ItemDto(200L, "Item1", "Description1", true,
                user.getId(), null, null, null);
        itemDto2 = new ItemDto(202L, "Item2", "Description2", true,
                user.getId(), null, null, null);
    }

    @Test
    void delete() {
        UserDto ownerDto = UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto1)));
        ItemDto newItemDto = itemMapper.toItemDto(itemRepository.save(itemMapper.toItem(ownerDto.getId(), itemDto)));
        itemService.deleteItem(newItemDto.getId());
        NotFoundException exp1 = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(newItemDto.getId(), ownerDto.getId()));
        assertEquals("Вещь не найдена", exp1.getMessage());
    }

    @Test
    void updateItem() {
        UserDto newUserDto = UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto1)));
        ItemDto newItemDto = itemMapper.toItemDto(itemRepository.save(itemMapper.toItem(newUserDto.getId(), itemDto)));
        newItemDto.setName("NewName");
        newItemDto.setDescription("NewDescription");
        newItemDto.setAvailable(false);
        ItemDto returnItemDto = itemService.updateItem(newUserDto.getId(), newItemDto.getId(), newItemDto);

        assertThat(returnItemDto.getName(), equalTo("NewName"));
        assertThat(returnItemDto.getDescription(), equalTo("NewDescription"));
        assertFalse(returnItemDto.getAvailable());

        WrongUserException exp = assertThrows(WrongUserException.class,
                () -> itemService.updateItem(55L, newItemDto.getId(), newItemDto));
        assertEquals("У пользователя нет такой вещи", exp.getMessage());
    }

}
