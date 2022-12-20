package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {
    Item createItem(Integer userId, Item item);

    Item updateItem(Integer itemId, Integer userId, Item item);

    Item updateNameItem(Integer itemId, Integer userId, Item item);

    Item updateDescriptionItem(Integer itemId, Integer userId, Item item);

    Item updateStatusItem(Integer itemId, Integer userId, Item item);

    Item getItemById(Integer userId, Integer itemId);

    Collection<Item> getAllItemsOwner(Integer userId);

    Collection<Item> searchItemByText(Integer userId, String text);

    void deleteAllItem();
}
