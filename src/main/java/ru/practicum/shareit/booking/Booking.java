package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
public class Booking {
    private Integer id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Item item;
    private User booker;
    private Status status;
}
