package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.Dto.UserDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UsersInMemoryStorage implements UserStorage {
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
        return getUsers().stream().anyMatch(u -> u.getEmail().equals(userDto.getEmail()));
    }
}