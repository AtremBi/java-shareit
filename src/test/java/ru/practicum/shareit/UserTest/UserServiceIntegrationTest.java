package ru.practicum.shareit.UserTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceIntegrationTest  {
    private UserMapper mapper = new UserMapper();
    private UserDto userDto;
    private final UserService userService;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, getRandomString(), getRandomEmail());
        user = new User(1L, getRandomString(), getRandomEmail());
    }

    @Test
    void createUser() {
        UserDto returnUserDto = userService.createUser(userDto);
        assertThat(returnUserDto.getName(), equalTo(userDto.getName()));
        assertThat(returnUserDto.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void deleteUser() {
        User user = new User(10L, "Ten", "ten@ten.ru");
        UserDto returnUserDto = userService.createUser(UserMapper.toUserDto(user));
        List<UserDto> listUser = userService.getUsers();
        int size = listUser.size();
        userService.deleteUser(returnUserDto.getId());
        listUser = userService.getUsers();
        assertThat(listUser.size(), equalTo(size - 1));
    }

    @Test
    void shouldUpdateUser() {
        UserDto returnUserDto = userService.createUser(UserMapper.toUserDto(user));
        returnUserDto.setName("NewName");
        returnUserDto.setEmail("new@email.ru");
        userService.updateUser(returnUserDto.getId(), returnUserDto);
        UserDto updateUserDto = userService.getUserById(returnUserDto.getId());
        assertThat(updateUserDto.getName(), equalTo("NewName"));
        assertThat(updateUserDto.getEmail(), equalTo("new@email.ru"));
    }

}
