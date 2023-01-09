package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {

    Item createItem(Integer userId, ItemDto itemDto);

    Item updateItem(Integer itemId, Integer userId, ItemDto itemDto);

    ItemDto getItemById(Integer itemId, Integer userId);

    Collection<ItemDto> getAllItemsOwner(Integer userId);

    Collection<Item> searchItemByText(Integer userId, String text);

    void changeItemStatus(Integer itemId, Integer userId, boolean approved);

    CommentResponseDto createComment(Integer userId, Integer itemId, String text);

    Collection<CommentResponseDto> getCommentList(Integer itemId);
}
