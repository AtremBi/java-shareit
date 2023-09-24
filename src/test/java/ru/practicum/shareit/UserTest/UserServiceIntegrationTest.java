package ru.practicum.shareit.UserTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exeptions.AlreadyExistException;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceIntegrationTest {
    private UserMapper mapper = new UserMapper();
    private UserDto userDto;
    private final UserService userService;
    private final UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, getRandomString(), getRandomEmail());
        user = new User(1L, getRandomString(), getRandomEmail());
    }

    @Test
    void deleteUser() {
        NotFoundException exp = assertThrows(NotFoundException.class, () -> userService.deleteUser(10L));
        assertEquals("Пользователь не найден", exp.getMessage());

        User user = new User(10L, "Ten", "ten@ten.ru");
        UserDto returnUserDto = UserMapper.toUserDto(userRepository.save(user));
        userService.deleteUser(returnUserDto.getId());
        assertTrue(userService.getUsers().isEmpty());
    }

    @Test
    void updateUser() {
        user = new User(2L, "User2", "second@second.ru");
        userRepository.save(user);
        User newUser = new User(3L, "User3", "third@third.ru");
        UserDto returnUserDto = UserMapper.toUserDto(userRepository.save(newUser));
        Long id = returnUserDto.getId();
        returnUserDto.setId(null);
        returnUserDto.setEmail("second@second.ru");
        AlreadyExistException exception = assertThrows(
                AlreadyExistException.class,
                () -> userService.updateUser(id, returnUserDto));
        assertEquals("Пользователь с таким email уже существует",
                exception.getMessage());

        returnUserDto.setId(id);
        returnUserDto.setName("NewName");
        returnUserDto.setEmail("new@email.ru");
        userService.updateUser(returnUserDto.getId(), returnUserDto);
        UserDto updateUserDto = userService.getUserById(returnUserDto.getId());
        assertThat(updateUserDto.getName(), equalTo("NewName"));
        assertThat(updateUserDto.getEmail(), equalTo("new@email.ru"));
    }

}
