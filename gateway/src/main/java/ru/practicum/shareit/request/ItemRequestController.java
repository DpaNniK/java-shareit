package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@RequestHeader("X-Sharer-User-Id") Integer creatorId,
                                                    @RequestBody @Valid RequestDto request) {
        return requestClient.createItemRequest(creatorId, request);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestForUser(@RequestHeader("X-Sharer-User-Id") Integer creatorId) {
        return requestClient.getItemRequestForUser(creatorId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getItemsWithPagination(@RequestParam(value = "from", required = false) Integer from,
                                                         @RequestParam(value = "size", required = false) Integer size,
                                                         @RequestHeader("X-Sharer-User-Id") Integer userId) {
        return requestClient.getItemsWithPagination(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                 @PathVariable Integer requestId) {
        return requestClient.getRequestById(userId, requestId);
    }
}
