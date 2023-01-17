package ru.practicum.shareit.item.dto;

import jdk.jfr.BooleanFlag;
import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Data
public class ItemDto {
    private Integer id;
    @NotBlank
    private String name;
    @NotNull
    private String description;
    @BooleanFlag()
    @NotNull()
    private Boolean available;
    private User owner;
    private Integer requestId;
    private Booking lastBooking;
    private Booking nextBooking;
    private Collection<CommentResponseDto> comments;
}
