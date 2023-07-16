package ru.practicum.shareit.user;

import ru.practicum.shareit.user.Dto.UserDto;

import java.util.List;

public interface UserStorage {

    User createUser(User user);

    User getUserById(Long userId);

    List<User> getUsers();

    User updateUser(User user);

    void deleteUser(Long userId);

    boolean isEmailExist(UserDto userDto);
}
