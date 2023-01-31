package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public RequestDto createItemRequest(@RequestHeader("X-Sharer-User-Id") Integer creatorId,
                                        @RequestBody @Valid ItemRequest request) {
        return itemRequestService.createItemRequest(creatorId, request);
    }

    @GetMapping
    public Collection<RequestDto> getItemRequestForUser(@RequestHeader("X-Sharer-User-Id") Integer creatorId) {
        return itemRequestService.getItemRequestForUser(creatorId);
    }

    @GetMapping("/all")
    public Collection<RequestDto> getItemsWithPagination(@RequestParam(value = "from", required = false) Integer from,
                                                         @RequestParam(value = "size", required = false) Integer size,
                                                         @RequestHeader("X-Sharer-User-Id") Integer userId) {
        if (from == null) return itemRequestService.getAllRequestItems();
        return itemRequestService.getItemsWithPagination(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestDto getRequestById(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                     @PathVariable Integer requestId) {
        return itemRequestService.getRequestById(requestId, userId);
    }
}