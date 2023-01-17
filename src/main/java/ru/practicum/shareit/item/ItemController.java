package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.marker.ValidItemUpdate;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public Item createItem(@RequestHeader("X-Sharer-User-Id") Integer userId
            , @RequestBody @Valid ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto createComment(@RequestHeader("X-Sharer-User-Id") Integer userId
            , @PathVariable Integer itemId, @RequestBody Comment comment) {
        return itemService.createComment(userId, itemId, comment.getText());
    }

    @PatchMapping("/{itemId}")
    public Item updateItem(@RequestHeader("X-Sharer-User-Id") Integer userId
            , @RequestBody @Validated(ValidItemUpdate.class) ItemDto itemDto, @PathVariable Integer itemId) {
        return itemService.updateItem(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader("X-Sharer-User-Id") Integer userId
            , @PathVariable Integer itemId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public Collection<ItemDto> getAllItemsOwner(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        return itemService.getAllItemsOwner(userId);
    }

    @GetMapping("/search")
    private Collection<Item> searchItemByText(@RequestHeader("X-Sharer-User-Id") Integer userId
            , @RequestParam String text) {
        return itemService.searchItemByText(userId, text);
    }
}
