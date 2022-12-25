package ru.practicum.shareit.request;

import lombok.Data;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private final Integer id;
    private String description;
    private User requestingUser;
    private LocalDateTime created;
}
