package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exeptions.AlreadyExistException;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.user.Dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public UserDto createUser(UserDto userDto) {
        if (notExistEmail(userDto)) {
            return UserMapper.toUserDto(userStorage.createUser(UserMapper.toUser(userDto)));
        } else {
            throw new AlreadyExistException("Пользователь - " + userDto + " уже существует");
        }
    }

    public UserDto getUserById(Long userId) {
        if (checkUserInStorage(userId)) {
            return UserMapper.toUserDto(userStorage.getUserById(userId));
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    public List<UserDto> getUsers() {
        List<UserDto> userDtos = new ArrayList<>();
        userStorage.getUsers().forEach(user -> {
            userDtos.add(UserMapper.toUserDto(user));
        });
        return userDtos;
    }

    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        user.setId(userId);
        if (checkUserInStorage(user.getId())) {
            User oldUser = UserMapper.toUser(getUserById(userId));
            if (user.getName() != null) {
                oldUser.setName(user.getName());
            }
            if (user.getEmail() != null && getUserById(user.getId()).getEmail().equals(user.getEmail())
                    || user.getEmail() != null && notExistEmail(userDto)) {
                oldUser.setEmail(user.getEmail());
            }
            return UserMapper.toUserDto(userStorage.updateUser(oldUser));
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    public void deleteUser(Long userId) {
        if (checkUserInStorage(userId)) {
            userStorage.deleteUser(userId);
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private boolean notExistEmail(UserDto userDto) {
        if (userStorage.getUsers().stream().noneMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
            return true;
        } else {
            throw new AlreadyExistException("Пользователь с таким email уже существует");
        }
    }

    private boolean checkUserInStorage(Long userId) {
        return userStorage.getUserById(userId) != null;
    }
}
