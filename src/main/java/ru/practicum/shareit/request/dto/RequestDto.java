package ru.practicum.shareit.request.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class RequestDto {
    private Integer id;
    private String description;
    private LocalDateTime created;
    private Collection<ItemRequestDto> items;
}
