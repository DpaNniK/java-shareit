package ru.practicum.shareit.item.dto;

import jdk.jfr.BooleanFlag;
import lombok.Data;

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
    private Integer requestId;
    private Collection<CommentResponseDto> comments;
}
