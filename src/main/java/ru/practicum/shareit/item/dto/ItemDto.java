package ru.practicum.shareit.item.dto;

import jdk.jfr.BooleanFlag;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ItemDto {
    private Integer id;
    @NotBlank
    private final String name;
    @NotNull
    private final String description;
    @BooleanFlag()
    @NotNull()
    private final Boolean available;
    private User owner;
    private final Integer requestId;
}
