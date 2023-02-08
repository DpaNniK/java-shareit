package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public Item createItem(@RequestHeader("X-Sharer-User-Id") Integer userId,
                           @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto createComment(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                            @PathVariable Integer itemId,
                                            @RequestBody CommentDto comment) {
        return itemService.createComment(userId, itemId, comment.getText());
    }

    @PatchMapping("/{itemId}")
    public Item updateItem(@RequestHeader("X-Sharer-User-Id") Integer userId,
                           @RequestBody ItemDto itemDto,
                           @PathVariable Integer itemId) {
        return itemService.updateItem(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader("X-Sharer-User-Id") Integer userId,
                               @PathVariable Integer itemId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public Collection<ItemDto> getAllItemsOwner(@RequestParam(value = "from", required = false) Integer from,
                                                @RequestParam(value = "size", required = false) Integer size,
                                                @RequestHeader("X-Sharer-User-Id") Integer userId) {
        if (from == null) return itemService.getAllItemsOwner(userId);
        return itemService.getAllItemsWithPagination(userId, from, size);
    }

    @GetMapping("/search")
    public Collection<Item> searchItemByText(@RequestParam(value = "from", required = false) Integer from,
                                             @RequestParam(value = "size", required = false) Integer size,
                                             @RequestHeader("X-Sharer-User-Id") Integer userId,
                                             @RequestParam String text) {
        if (from == null) return itemService.searchItemByText(userId, text);
        return itemService.searchItemByTextWithPagination(userId, from, size, text);
    }
}
