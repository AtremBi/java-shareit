package ru.practicum.shareit.UserTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exeptions.AlreadyExistException;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.TestUtil.getRandomEmail;
import static ru.practicum.shareit.TestUtil.getRandomString;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserServiceIntegrationTest {
    @Mock
    private UserRepository userRepository;
    private UserService userService;
    private UserMapper mapper = new UserMapper();
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, getRandomString(), getRandomEmail());
        userService = new UserService(userRepository);
    }

    @Test
    void shouldException_whenGetUser_withWrongId() {
        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(-1L));
        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void shouldException_whenCreateUser_withExistEmail() {
        when(userRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException(""));
        final AlreadyExistException exception = assertThrows(
                AlreadyExistException.class,
                () -> userService.createUser(userDto));
        assertEquals("Пользователь с E-mail=" + userDto.getEmail() + " уже существует!",
                exception.getMessage());
    }

    @Test
    void shouldReturnUser_whenFindUserById() {
        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(mapper.toUser(userDto)));
        User user = userService.findUserById(1L);
        verify(userRepository, Mockito.times(1))
                .findById(1L);
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }
}
