package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.marker.ValidItemUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                             @RequestBody @Valid ItemDto itemDto) {
        return itemClient.createItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                @PathVariable Integer itemId, @RequestBody CommentDto comment) {
        return itemClient.createComment(userId, itemId, comment);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                             @RequestBody @Validated(ValidItemUpdate.class) ItemDto itemDto,
                                             @PathVariable Integer itemId) {
        return itemClient.updateItem(userId, itemDto, itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                              @PathVariable Integer itemId) {
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsOwner(@PositiveOrZero
                                                   @RequestParam(value = "from", required = false) Integer from,
                                                   @Positive
                                                   @RequestParam(value = "size", required = false) Integer size,
                                                   @RequestHeader("X-Sharer-User-Id") Integer userId) {
        return itemClient.getAllItemsOwner(from, size, userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemByText(@PositiveOrZero
                                                   @RequestParam(value = "from", required = false) Integer from,
                                                   @Positive
                                                   @RequestParam(value = "size", required = false) Integer size,
                                                   @RequestHeader("X-Sharer-User-Id") Integer userId,
                                                   @RequestParam String text) {
        return itemClient.searchItemByText(from, size, userId, text);
    }
}
