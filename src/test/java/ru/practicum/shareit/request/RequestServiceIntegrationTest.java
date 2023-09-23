package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceIntegrationTest {
    private final ItemRequestService itemRequestService;
    private final UserService userService;
    public ItemRequestDto itemRequestDto;
    public UserDto userDto1;
    public UserDto userDto2;

    @BeforeEach
    void setUp() {
        userDto1 = new UserDto(1L, getRandomString(), getRandomEmail());
        userDto2 = new UserDto(2L, getRandomString(), getRandomEmail());
        itemRequestDto = new ItemRequestDto(1L, getRandomString(),
                new UserDto(1L, getRandomString(), getRandomEmail()),
                LocalDateTime.of(2022, 1, 2, 3, 4, 5), null);
    }

    @Test
    void createRequest() {
        UserDto newUserDto = userService.createUser(userDto1);
        ItemRequestDto returnRequestDto = itemRequestService.createRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2022, 1, 2, 3, 4, 5));
        assertThat(returnRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void getAllRequests() {
        UserDto firstUserDto = userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        itemRequestService.createRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2023, 1, 2, 3, 4, 5));
        itemRequestService.createRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2024, 1, 2, 3, 4, 5));
        List<ItemRequestDto> listItemRequest = itemRequestService.getAllRequests(0, 10, firstUserDto.getId());
        assertThat(listItemRequest.size(), equalTo(2));
    }

    @Test
    void getRequests() {
        userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        itemRequestService.createRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2023, 1, 2, 3, 4, 5));
        itemRequestService.createRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2024, 1, 2, 3, 4, 5));
        List<ItemRequestDto> listItemRequest = itemRequestService.getRequests(newUserDto.getId());
        assertThat(listItemRequest.size(), equalTo(2));
    }

    @Test
    void getRequestById() {
        UserDto firstUserDto = userService.createUser(userDto1);
        ItemRequestDto newItemRequestDto = itemRequestService.createRequest(firstUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2023, 1, 2, 3, 4, 5));
        ItemRequestDto returnItemRequestDto = itemRequestService.getRequestById(newItemRequestDto.getId(),
                firstUserDto.getId());
        assertThat(returnItemRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

}
