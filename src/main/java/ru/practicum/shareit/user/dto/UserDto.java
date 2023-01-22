package ru.practicum.shareit.user.dto;

import lombok.Data;
import ru.practicum.shareit.user.marker.ValidUserUpdate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class UserDto {
    private Integer id;
    @NotNull
    private String name;
    @Email(groups = ValidUserUpdate.class)
    @Email
    @NotNull
    private String email;
}
