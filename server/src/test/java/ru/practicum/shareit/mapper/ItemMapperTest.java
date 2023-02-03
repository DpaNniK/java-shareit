package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemMapperTest {
    public Item item;
    public User user;

    @BeforeEach
    void setValues() {
        this.item = new Item();
        item.setRequestId(1);
        item.setId(1);
        item.setName("name");
        item.setDescription("desc");
        item.setAvailable(true);
        this.user = new User();
        user.setId(1);
        user.setEmail("mail@mail.ru");
        user.setName("name");
    }

    @Test
    public void toItemDtoTest() {
        ItemDto itemDto = ItemMapper.toItemDto(item, user, new ArrayList<>(), new ArrayList<>());
        assertEquals(itemDto.getId(), item.getId());
        assertEquals(itemDto.getName(), item.getName());
        assertEquals(itemDto.getDescription(), item.getDescription());
        assertEquals(itemDto.getAvailable(), item.isAvailable());
    }

    @Test
    public void toItemTest() {
        ItemDto itemDto = ItemMapper.toItemDto(item, user, new ArrayList<>(), new ArrayList<>());
        ItemMapper.toItem(itemDto);
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.isAvailable(), itemDto.getAvailable());
    }
}
