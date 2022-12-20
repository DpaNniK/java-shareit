package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {
    Item createItem(Integer userId, ItemDto itemDto);

    Item updateItem(Integer itemId, Integer userId, ItemDto itemDto);

    Item getItemById(Integer userId, Integer itemId);

    Collection<Item> getAllItemsOwner(Integer userId);

    Collection<Item> searchItemByText(Integer userId, String text);

    void deleteAllItem();
}
