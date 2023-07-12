package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exeptions.AlreadyExistException;
import ru.practicum.shareit.user.Dto.UserDto;

import java.util.*;

@Component
public class UsersInMemory implements UserStorage {
    private Long id = 1L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User createUser(User user) {
        user.setId(id++);
        users.put(user.getId(), user);
        return getUserById(user.getId());
    }

    @Override
    public User getUserById(Long userId) {
        return users.get(userId);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return getUserById(user.getId());
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }

    @Override
    public boolean isEmailExist(UserDto userDto) {
        if (getUsers().stream().noneMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
            return true;
        } else {
            throw new AlreadyExistException("Пользователь с таким email уже существует");
        }
    }
}
