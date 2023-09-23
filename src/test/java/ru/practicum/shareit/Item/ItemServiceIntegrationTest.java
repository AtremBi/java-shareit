package ru.practicum.shareit.Item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ItemServiceIntegrationTest {
    private final UserService userService;
    private final ItemService itemService;

    private User user;
    private UserDto userDto1;
    private UserDto userDto2;
    private ItemDto itemDto;
    private ItemDto itemDto2;

    @BeforeEach
    public void setUp() {
        user = new User(200L, getRandomString(), getRandomEmail());
        userDto1 = new UserDto(201L, getRandomString(), getRandomEmail());
        userDto2 = new UserDto(202L, getRandomString(), getRandomEmail());
        itemDto = new ItemDto(200L, "Item1", "Description1", true,
                user.getId(), null, null, null);
        itemDto2 = new ItemDto(202L, "Item2", "Description2", true,
                user.getId(), null, null, null);
    }


    @Test
    void updateItem() {
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
    void getItems() {
        UserDto ownerDto = userService.createUser(userDto1);
        itemService.createItem(ownerDto.getId(), itemDto);
        itemService.createItem(ownerDto.getId(), itemDto2);
        List<ItemDto> listItems = itemService.getItems(ownerDto.getId(), 0, 10);
        assertEquals(2, listItems.size());
    }

    @Test
    void searchItems() {
        UserDto ownerDto = userService.createUser(userDto1);
        itemService.createItem(ownerDto.getId(), itemDto);
        itemService.createItem(ownerDto.getId(), itemDto2);
        List<ItemDto> listItems = itemService.searchItems("item", 0, 1);
        assertEquals(1, listItems.size());
    }

}