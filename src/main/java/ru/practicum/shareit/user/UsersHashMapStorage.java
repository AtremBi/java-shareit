package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UsersHashMapStorage implements UserStorage {
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

}
