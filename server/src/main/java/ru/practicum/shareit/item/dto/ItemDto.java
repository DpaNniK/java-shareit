package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@Data
public class ItemDto {
    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private Integer requestId;
    private Booking lastBooking;
    private Booking nextBooking;
    private Collection<CommentResponseDto> comments;
}
