package ru.practicum.shareit.UserTest;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exeptions.AlreadyExistException;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository mockUserRepository;
    private UserService userServiceWithMock;
    private UserMapper mapper = new UserMapper();
    private UserDto userDto = new UserDto(1L, getRandomString(), getRandomEmail());
    private final UserService userService;
    //    private final UserMapper mapper;
    private User user = new User(1L, getRandomString(), getRandomEmail());

    @BeforeEach
    void seyUp() {
        userServiceWithMock = new UserService(mockUserRepository);
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
    void shouldReturnUser_whenGetUserById() {
        UserDto returnUserDto = userService.createUser(userDto);
        assertThat(returnUserDto.getName(), equalTo(userDto.getName()));
        assertThat(returnUserDto.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void shouldException_whenDeleteUser_withWrongId() {
        NotFoundException exp = assertThrows(NotFoundException.class, () -> userService.deleteUser(10L));
        assertEquals("Пользователь не найден", exp.getMessage());
    }

    @Test
    void shouldDeleteUser() {
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

    @Test
    void shouldException_whenUpdateUser_withExistEmail() {
        user = new User(2L, "User2", "second@second.ru");
        userService.createUser(UserMapper.toUserDto(user));
        User newUser = new User(3L, "User3", "third@third.ru");
        UserDto returnUserDto = userService.createUser(UserMapper.toUserDto(newUser));
        Long id = returnUserDto.getId();
        returnUserDto.setId(null);
        returnUserDto.setEmail("second@second.ru");
        final AlreadyExistException exception = assertThrows(
                AlreadyExistException.class,
                () -> userService.updateUser(id, returnUserDto));
        assertEquals("Пользователь с таким email уже существует",
                exception.getMessage());
    }

    @Test
    void shouldException_whenGetUser_withWrongId() {
        when(mockUserRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userServiceWithMock.getUserById(-1L));
        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void shouldException_whenCreateUser_withExistEmail() {
        when(mockUserRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException(""));
        final AlreadyExistException exception = assertThrows(
                AlreadyExistException.class,
                () -> userServiceWithMock.createUser(userDto));
        assertEquals("Пользователь с E-mail=" + userDto.getEmail() + " уже существует!",
                exception.getMessage());
    }

    @Test
    void shouldReturnUser_whenFindUserById() {
        when(mockUserRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(mapper.toUser(userDto)));
        User user = userServiceWithMock.findUserById(1L);
        verify(mockUserRepository, Mockito.times(1))
                .findById(1L);
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }
}
