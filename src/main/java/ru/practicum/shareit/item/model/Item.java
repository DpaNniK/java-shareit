package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Data
public class Item {
    private Integer id;
    private String name;
    private String description;
    private boolean available;
    private User owner;
    private final ItemRequest requestId;
}
