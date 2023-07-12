package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exeptions.AlreadyExistException;
import ru.practicum.shareit.exeptions.NotFoundException;
import ru.practicum.shareit.user.Dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public UserDto createUser(UserDto userDto) {
        if (userStorage.isEmailExist(userDto)) {
            return UserMapper.toUserDto(userStorage.createUser(UserMapper.toUser(userDto)));
        } else {
            throw new AlreadyExistException("Пользователь - " + userDto + " уже существует");
        }
    }

    public UserDto getUserById(Long userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return UserMapper.toUserDto(user);
    }

    public List<UserDto> getUsers() {
        return UserMapper.toUserDto(userStorage.getUsers());
    }

    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        user.setId(userId);
        getUserById(userId);
        User oldUser = UserMapper.toUser(getUserById(userId));
        if (user.getName() != null) {
            oldUser.setName(user.getName());
        }
        if (user.getEmail() != null && getUserById(user.getId()).getEmail().equals(user.getEmail())
                || user.getEmail() != null && userStorage.isEmailExist(userDto)) {
            oldUser.setEmail(user.getEmail());
        }
        return UserMapper.toUserDto(userStorage.updateUser(oldUser));
    }

    public void deleteUser(Long userId) {
        userStorage.getUserById(userId);
        userStorage.deleteUser(userId);
    }

}
