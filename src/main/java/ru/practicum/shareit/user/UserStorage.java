package ru.practicum.shareit.user;

import java.util.List;

public interface UserStorage {

    User createUser(User user);

    User getUserById(Long userId);

    List<User> getUsers();

    User updateUser(User user);

    void deleteUser(Long userId);
}
