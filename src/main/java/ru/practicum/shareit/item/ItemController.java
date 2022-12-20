package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.marker.ValidItemUpdate;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private ItemService itemService;

    @PostMapping
    public Item createItem(@RequestHeader("X-Sharer-User-Id") Integer userId
            , @RequestBody @Valid ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public Item updateItem(@RequestHeader("X-Sharer-User-Id") Integer userId
            , @RequestBody @Validated(ValidItemUpdate.class) ItemDto itemDto, @PathVariable Integer itemId) {
        return itemService.updateItem(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public Item getItemById(@RequestHeader("X-Sharer-User-Id") Integer userId, @PathVariable Integer itemId) {
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public Collection<Item> getAllItemsOwner(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        return itemService.getAllItemsOwner(userId);
    }

    @GetMapping("/search")
    private Collection<Item> searchItemByText(@RequestHeader("X-Sharer-User-Id") Integer userId
            , @RequestParam String text) {
        return itemService.searchItemByText(userId, text);
    }
}
