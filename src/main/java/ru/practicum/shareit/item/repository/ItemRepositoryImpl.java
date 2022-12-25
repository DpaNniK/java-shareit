package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    HashMap<Integer, Item> itemHashMap = new HashMap<>();

    @Override
    public Item createItem(Integer userId, Item item) {
        itemHashMap.put(item.getId(), item);
        return itemHashMap.get(item.getId());
    }

    @Override
    public Item updateItem(Integer itemId, Integer userId, Item item) {
        itemHashMap.put(itemId, item);
        return itemHashMap.get(item.getId());
    }

    @Override
    public Item updateNameItem(Integer itemId, Integer userId, Item item) {
        itemHashMap.get(itemId).setName(item.getName());
        return itemHashMap.get(itemId);
    }

    @Override
    public Item updateDescriptionItem(Integer itemId, Integer userId, Item item) {
        itemHashMap.get(itemId).setDescription(item.getDescription());
        return itemHashMap.get(itemId);
    }

    @Override
    public Item updateStatusItem(Integer itemId, Integer userId, Item item) {
        itemHashMap.get(itemId).setAvailable(item.isAvailable());
        return itemHashMap.get(itemId);
    }

    @Override
    public Item getItemById(Integer userId, Integer itemId) {
        return itemHashMap.get(itemId);
    }

    @Override
    public Collection<Item> getAllItemsOwner(Integer userId) {
        Collection<Item> itemsOwner = new ArrayList<>();
        for (Item item : itemHashMap.values()) {
            if (Objects.equals(item.getOwner().getId(), userId)) itemsOwner.add(item);
        }
        return itemsOwner;
    }

    @Override
    public Collection<Item> searchItemByText(Integer userId, String text) {
        Collection<Item> itemsOwner = new ArrayList<>();
        if (text.isEmpty()) return new ArrayList<>();

        for (Item item : itemHashMap.values()) {
            if (item.getName().toLowerCase().contains(text) && item.isAvailable()) {
                itemsOwner.add(item);
            } else {
                if (item.getDescription().toLowerCase().contains(text) && item.isAvailable()) {
                    itemsOwner.add(item);
                }
            }
        }
        return itemsOwner;
    }

    @Override
    public void deleteAllItem() {
        itemHashMap.clear();
    }

    @Override
    public Map<Integer, Item> getItemsMap() {
        return itemHashMap;
    }
}
