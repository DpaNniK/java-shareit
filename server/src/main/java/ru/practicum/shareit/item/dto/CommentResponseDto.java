package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private Integer id;
    private String text;
    private String authorName;
    private LocalDateTime created;
}
