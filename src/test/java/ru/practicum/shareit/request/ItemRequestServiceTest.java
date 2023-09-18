package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ServiceUtil;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
    @Mock
    ItemRequestRepository mockItemRequestRepository;
    @Mock
    UserService userServiceMock;

    private final ItemRequestService itemRequestService;
    private final UserService userService;
    private ItemRequestService itemRequestServiceWithMock;
    public UserDto userDto = new UserDto(1L, getRandomString(), getRandomEmail());
    public ItemRequestDto itemRequestDto = new ItemRequestDto(1L, getRandomString(),
            userDto, LocalDateTime.of(2022, 1, 2, 3, 4, 5), null);

    public UserDto userDto1 = new UserDto(1L, getRandomString(), getRandomEmail());
    public UserDto userDto2 = new UserDto(2L, getRandomString(), getRandomEmail());

    @BeforeEach
    void setUp() {
        userDto1 = new UserDto(1L, getRandomString(), getRandomEmail());
        userDto2 = new UserDto(2L, getRandomString(), getRandomEmail());
        itemRequestServiceWithMock = new ItemRequestService(mockItemRequestRepository,
                null, new ServiceUtil(null, userServiceMock, null));
    }

    private String getRandomEmail() {
        RandomString randomString = new RandomString();
        return randomString.nextString() + "@" + randomString.nextString() + ".ew";
    }

    private String getRandomString() {
        RandomString randomString = new RandomString();
        return randomString.nextString();
    }

    @Test
    void shouldCreateItemRequest() {
        UserDto newUserDto = userService.createUser(userDto1);
        ItemRequestDto returnRequestDto = itemRequestService.createRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2022, 1, 2, 3, 4, 5));
        assertThat(returnRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void shouldException_whenCreateItemRequest_withWrongUserId() {
        NotFoundException exp = assertThrows(NotFoundException.class,
                () -> itemRequestService.createRequest(-2L, itemRequestDto,
                        LocalDateTime.of(2022, 1, 2, 3, 4, 5)));
        assertEquals("Пользователь не найден", exp.getMessage());
    }

    @Test
    void shouldReturnAllItemRequests_whenSizeNotNullAndNull() {
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
    void shouldReturnOwnItemRequests() {
        userService.createUser(userDto1);
        UserDto newUserDto = userService.createUser(userDto2);
        itemRequestService.createRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2023, 1, 2, 3, 4, 5));
        itemRequestService.createRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2024, 1, 2, 3, 4, 5));
        List<ItemRequestDto> listItemRequest = itemRequestService.getRequests(newUserDto.getId());
        System.out.println(listItemRequest.toString());
        assertThat(listItemRequest.size(), equalTo(2));
    }

    @Test
    void shouldReturnItemRequestById() {
        UserDto firstUserDto = userService.createUser(userDto1);
        ItemRequestDto newItemRequestDto = itemRequestService.createRequest(firstUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2023, 1, 2, 3, 4, 5));
        ItemRequestDto returnItemRequestDto = itemRequestService.getRequestById(newItemRequestDto.getId(),
                firstUserDto.getId());
        assertThat(returnItemRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void shouldException_whenGetItemRequest_withWrongId() {
        when(userServiceMock.findUserById(any(Long.class)))
                .thenReturn(new User());
        when(mockItemRequestRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestServiceWithMock.getRequestById(-1L, 1L));
        assertEquals("Запрос не найден", exception.getMessage());
    }
}
