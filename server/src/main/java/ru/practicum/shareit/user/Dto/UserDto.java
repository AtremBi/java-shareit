package ru.practicum.shareit.user.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    @Email
    @NotNull
    private String email;
}
