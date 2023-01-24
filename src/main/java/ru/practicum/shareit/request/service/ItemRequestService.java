package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collection;

public interface ItemRequestService {

    RequestDto createItemRequest(Integer userId, ItemRequest request);

    Collection<RequestDto> getItemRequestForUser(Integer userId);

    Collection<RequestDto> getItemsWithPagination(Integer userId, Integer from, Integer size);

    Collection<RequestDto> getAllRequestItems();

    RequestDto getRequestById(Integer requestId, Integer userId);
}
