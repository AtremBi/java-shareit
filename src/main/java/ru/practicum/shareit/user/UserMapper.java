package ru.practicum.shareit.user;

import ru.practicum.shareit.user.Dto.UserDto;

import java.util.ArrayList;
import java.util.List;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static List<UserDto> toUserDto(List<User> users) {
        List<UserDto> usersDtos = new ArrayList<>();
        users.forEach(user -> {
            usersDtos.add(toUserDto(user));
        });
        return usersDtos;
    }

    public static User toUser(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }
}
