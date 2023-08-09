package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exeptions.AlreadyExistException;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.user.Dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userStorage;
    public UserDto createUser(UserDto userDto) {
        try {
            return UserMapper.toUserDto(userStorage.save(UserMapper.toUser(userDto)));
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistException("Пользователь с E-mail=" +
                    userDto.getEmail() + " уже существует!");
        }
    }

    public UserDto getUserById(Long userId) {
        return UserMapper.toUserDto(userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден")));
    }

    public User findUserById(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public List<UserDto> getUsers() {
        return UserMapper.toUserDto(userStorage.findAll());
    }

    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        user.setId(userId);
        getUserById(userId);
        User oldUser = UserMapper.toUser(getUserById(userId));
        if (user.getName() != null) {
            oldUser.setName(user.getName());
        }
        if (user.getEmail() != null && getUserById(user.getId()).getEmail().equals(user.getEmail())) {
            oldUser.setEmail(user.getEmail());
        } else if (user.getEmail() != null) {
            checkEmail(user);
            oldUser.setEmail(user.getEmail());
        }
        return createUser(UserMapper.toUserDto(oldUser));
    }

    public void deleteUser(Long userId) {
        userStorage.delete(userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден")));
    }

    private void checkEmail(User user) {
        if (getUsers().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new AlreadyExistException("Пользователь с таким email уже существует");
        }
    }

}
