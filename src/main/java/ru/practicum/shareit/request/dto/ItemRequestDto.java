package ru.practicum.shareit.request.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ItemRequestDto {
    Integer id;
    String name;
    String description;
    boolean available;
    Integer requestId;
}
