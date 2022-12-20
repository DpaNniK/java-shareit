package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.Collection;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private Integer id = 1;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item createItem(Integer userId, ItemDto itemDto) {
        itemDto.setId(id);
        Item item = itemRepository.createItem(userId, ItemMapper.toItem(itemDto));
        id++;
        return item;
    }

    @Override
    public Item updateItem(Integer itemId, Integer userId, ItemDto itemDto) {
        itemDto.setId(itemId);
        if (itemDto.getName() == null && itemDto.getDescription() == null)
            return itemRepository.updateStatusItem(itemId, userId, ItemMapper.toItem(itemDto));
        if (itemDto.getAvailable() == null && itemDto.getDescription() == null)
            return itemRepository.updateNameItem(itemId, userId, ItemMapper.toItem(itemDto));
        if (itemDto.getAvailable() == null && itemDto.getName() == null)
            return itemRepository.updateDescriptionItem(itemId, userId, ItemMapper.toItem(itemDto));

        return itemRepository.updateItem(itemId, userId, ItemMapper.toItem(itemDto));
    }

    @Override
    public Item getItemById(Integer userId, Integer itemId) {
        return itemRepository.getItemById(userId, itemId);
    }

    @Override
    public Collection<Item> getAllItemsOwner(Integer userId) {
        return itemRepository.getAllItemsOwner(userId);
    }

    @Override
    public Collection<Item> searchItemByText(Integer userId, String text) {
        return itemRepository.searchItemByText(userId, text.toLowerCase());
    }

    @Override
    public void deleteAllItem() {
        id = 1;
        itemRepository.deleteAllItem();
    }
}
