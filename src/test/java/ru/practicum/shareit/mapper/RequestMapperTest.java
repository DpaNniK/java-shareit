package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestMapperTest {

    public Item item;
    public User user;
    public ItemRequest request;

    @BeforeEach
    void setValues() {
        this.user = new User();
        user.setId(1);
        user.setEmail("mail@mail.ru");
        user.setName("name");
        this.request = new ItemRequest();
        request.setId(1);
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());
        request.setDescription("Desc");
        this.item = new Item();
        item.setRequestId(1);
        item.setId(1);
        item.setName("name");
        item.setDescription("desc");
        item.setAvailable(true);
    }

    @Test
    public void toRequestDtoTest() {
        RequestDto requestDto = RequestMapper.toRequestDto(request, new ArrayList<>());
        assertEquals(requestDto.getId(), request.getId());
        assertEquals(requestDto.getCreated().truncatedTo(ChronoUnit.MINUTES),
                request.getCreated().truncatedTo(ChronoUnit.MINUTES));
        assertEquals(requestDto.getDescription(), request.getDescription());
    }

    @Test
    public void toItemRequestDtoTest() {
        ItemRequestDto itemRequestDto =  RequestMapper.toItemRequestDto(item);
        assertEquals(itemRequestDto.getId(), request.getId());
        assertEquals(itemRequestDto.getDescription(), item.getDescription());
        assertEquals(itemRequestDto.getName(), item.getName());
        assertEquals(itemRequestDto.getRequestId(), item.getRequestId());
    }
}
