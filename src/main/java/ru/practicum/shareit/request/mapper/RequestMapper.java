package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@UtilityClass
public class RequestMapper {
    public static RequestDto toRequestDto(Request request, Collection<Item> items) {
        RequestDto requestDto = new RequestDto();
        requestDto.setDescription(request.getDescription());
        requestDto.setCreated(LocalDateTime.now());
        requestDto.setId(request.getId());
        requestDto.setItems(new ArrayList<>());
        if (items != null) {
            items.forEach(item -> requestDto.getItems().add(toItemRequestDto(item)));
        }
        return requestDto;
    }

    public static ItemRequestDto toItemRequestDto(Item item) {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(item.getId());
        itemRequestDto.setName(item.getName());
        itemRequestDto.setDescription(item.getDescription());
        itemRequestDto.setRequestId(item.getRequestId());
        itemRequestDto.setAvailable(item.isAvailable());
        return itemRequestDto;
    }
}
