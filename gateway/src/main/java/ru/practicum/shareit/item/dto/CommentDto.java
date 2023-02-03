package ru.practicum.shareit.item.dto;

import lombok.Data;


import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Integer id;
    private String text;
    private LocalDateTime created;
}
