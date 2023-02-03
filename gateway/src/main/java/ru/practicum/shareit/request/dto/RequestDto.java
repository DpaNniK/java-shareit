package ru.practicum.shareit.request.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class RequestDto {
    private Integer id;
    @NotNull
    private String description;
    @CreationTimestamp
    private LocalDateTime created;
    private Collection<ItemRequestDto> items;
}
