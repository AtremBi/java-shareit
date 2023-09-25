package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceIntegrationTest {
    private final ItemRequestService itemRequestService;
    private final UserRepository userRepository;
    public ItemRequestDto itemRequestDto;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User(null, getRandomString(), getRandomEmail());
        user2 = new User(null, getRandomString(), getRandomEmail());
        itemRequestDto = new ItemRequestDto(1L, getRandomString(),
                new UserDto(1L, getRandomString(), getRandomEmail()),
                LocalDateTime.of(2022, 1, 2, 3, 4, 5), null);
    }

    @Test
    void createRequest() {
        UserDto newUserDto = UserMapper.toUserDto(userRepository.save(user1));

        ItemRequestDto returnRequestDto = itemRequestService.createRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2022, 1, 2, 3, 4, 5));
        assertThat(returnRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

}
